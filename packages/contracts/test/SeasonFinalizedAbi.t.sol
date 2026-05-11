// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {Vm} from "forge-std/Vm.sol";
import {ClanWorld} from "../src/ClanWorld.sol";
import {ClanWorldConstants} from "../src/IClanWorld.sol";

contract SeasonFinalizedAbiHarness is ClanWorld {
    function setCurrentTick(uint64 tick) external {
        _world.currentTick = tick;
        _world.nextHeartbeatAtTick = tick + 1;
    }
}

contract SeasonFinalizedAbiTest is Test {
    function test_seasonFinalizedTopicMatchesThreeArgumentAbi() public {
        SeasonFinalizedAbiHarness world = new SeasonFinalizedAbiHarness();
        world.setCurrentTick(ClanWorldConstants.SEASON_DURATION_TICKS);

        bytes32 expectedTopic = keccak256("SeasonFinalized(uint64,uint32[],uint256[])");
        vm.recordLogs();
        world.finalizeSeason();

        Vm.Log[] memory logs = vm.getRecordedLogs();
        bool found;
        for (uint256 i = 0; i < logs.length; i++) {
            if (logs[i].topics.length > 0 && logs[i].topics[0] == expectedTopic) {
                found = true;
                break;
            }
        }

        assertTrue(found, "SeasonFinalized topic must match current ABI");
    }
}
