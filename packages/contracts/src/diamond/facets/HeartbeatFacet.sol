// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {IClanWorldEvents} from "../../IClanWorld.sol";
import {LibHeartbeat} from "../lib/LibHeartbeat.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract HeartbeatFacet is IClanWorldEvents {
    function heartbeat() external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);
        LibHeartbeat.tick(s);
        LibStorage.exitNonReentrant(s);
    }
}
