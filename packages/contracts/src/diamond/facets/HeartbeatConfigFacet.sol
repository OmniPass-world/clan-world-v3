// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ClanWorldConstants} from "../../IClanWorld.sol";
import {LibDiamond} from "../lib/LibDiamond.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract HeartbeatConfigFacet {
    uint64 private constant MAX_HEARTBEAT_INTERVAL_SECONDS = 1 hours;

    event HeartbeatIntervalUpdated(uint64 oldIntervalSeconds, uint64 newIntervalSeconds, uint64 nextHeartbeatAtTs);

    function heartbeatIntervalSeconds() external view returns (uint64) {
        return _heartbeatIntervalSeconds(LibStorage.appStorage());
    }

    function setHeartbeatIntervalSeconds(uint64 intervalSeconds) external {
        LibDiamond.enforceIsContractOwner();
        require(
            intervalSeconds > 0 && intervalSeconds <= MAX_HEARTBEAT_INTERVAL_SECONDS,
            "ClanWorld: invalid heartbeat interval"
        );

        LibStorage.AppStorage storage s = LibStorage.appStorage();
        uint64 oldIntervalSeconds = _heartbeatIntervalSeconds(s);
        s.heartbeatIntervalSeconds = intervalSeconds;

        uint64 nextHeartbeatAtTs = uint64(block.timestamp) + intervalSeconds;
        if (s.world.nextHeartbeatAtTs > nextHeartbeatAtTs) {
            s.world.nextHeartbeatAtTs = nextHeartbeatAtTs;
        }

        emit HeartbeatIntervalUpdated(oldIntervalSeconds, intervalSeconds, s.world.nextHeartbeatAtTs);
    }

    function _heartbeatIntervalSeconds(LibStorage.AppStorage storage s) private view returns (uint64) {
        uint64 configuredIntervalSeconds = s.heartbeatIntervalSeconds;
        if (configuredIntervalSeconds == 0) {
            return ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS;
        }
        return configuredIntervalSeconds;
    }
}
