// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {Clansman, ClansmanState, Mission} from "../../src/IClanWorld.sol";
import {LibMission} from "../../src/diamond/lib/LibMission.sol";

contract LibMissionTest is Test {
    function testEffectiveRegionForTravelingMission() public pure {
        Clansman memory cs;
        cs.state = ClansmanState.TRAVELING;
        cs.currentRegion = 1;

        Mission memory mission;
        mission.active = true;
        mission.startRegion = 1;
        mission.targetRegion = 4;
        mission.arrivalTick = 10;

        assertEq(LibMission.effectiveRegion(cs, mission, 9), 1);
        assertEq(LibMission.effectiveRegion(cs, mission, 10), 4);
        assertEq(LibMission.effectiveRegion(cs, mission, 11), 4);
    }

    function testEffectiveRegionFallsBackToCurrentRegion() public pure {
        Clansman memory cs;
        cs.state = ClansmanState.WAITING;
        cs.currentRegion = 6;

        Mission memory mission;
        mission.active = true;
        mission.startRegion = 1;
        mission.targetRegion = 4;
        mission.arrivalTick = 10;

        assertEq(LibMission.effectiveRegion(cs, mission, 9), 6);

        cs.state = ClansmanState.TRAVELING;
        mission.active = false;
        assertEq(LibMission.effectiveRegion(cs, mission, 9), 6);
    }
}
