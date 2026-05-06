// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {IClanWorldEvents} from "../../IClanWorld.sol";
import {LibDiamond} from "../lib/LibDiamond.sol";
import {LibStorage} from "../lib/LibStorage.sol";
import {LibWorldClock} from "../lib/LibWorldClock.sol";

contract WorldPauseFacet is IClanWorldEvents {
    function pauseWorld() external {
        LibDiamond.enforceIsContractOwner();
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);
        require(!s.world.worldPaused, "ClanWorld: already paused");

        s.world.worldPaused = true;
        s.world.pausedAtTs = uint64(block.timestamp);

        emit WorldPaused(s.world.currentTick, s.world.pausedAtTs);
        LibStorage.exitNonReentrant(s);
    }

    function unpauseWorld() external {
        LibDiamond.enforceIsContractOwner();
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);
        require(s.world.worldPaused, "ClanWorld: not paused");

        uint64 durationSeconds = uint64(block.timestamp) - s.world.pausedAtTs;
        s.world.worldPaused = false;
        s.world.pausedAtTs = 0;
        LibWorldClock.scheduleNextHeartbeat(s);

        emit WorldUnpaused(s.world.currentTick, durationSeconds, s.world.nextHeartbeatAtTs);
        LibStorage.exitNonReentrant(s);
    }

    function isWorldPaused() external view returns (bool) {
        return LibStorage.appStorage().world.worldPaused;
    }
}
