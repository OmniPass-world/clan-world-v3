// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ActionType, WorldState} from "../../IClanWorld.sol";
import {LibTravel} from "../../lib/LibTravel.sol";
import {LibGameRules} from "../lib/LibGameRules.sol";
import {LibSeason} from "../lib/LibSeason.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract RawWorldViewsFacet {
    function getWorldState() external view returns (WorldState memory ws) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        ws.currentTick = s.world.currentTick;
        ws.seasonStartTick = s.world.seasonStartTick;
        ws.seasonEndTick = s.world.seasonEndTick;
        ws.seasonFinalized = s.world.seasonFinalized;
        ws.currentSeasonNumber = s.world.currentSeasonNumber;
        ws.nextHeartbeatAtTick = s.world.nextHeartbeatAtTick;
        ws.nextHeartbeatAtTs = s.world.nextHeartbeatAtTs;
        ws.nextBanditSpawnEligibleTick = s.world.nextBanditSpawnEligibleTick;
        ws.currentBanditSpawnChanceBps = s.world.currentBanditSpawnChanceBps;
        ws.currentTickSeed = s.world.currentTickSeed;
        ws.activeBanditId = s.world.activeBanditId;
        ws.nextCommitSequence = s.world.nextCommitSequence;
        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) =
            LibSeason.winterWindowForTick(s.world.currentTick);
    }

    function isWinter() external view returns (bool) {
        return LibSeason.isWinterActiveAt(LibStorage.appStorage().world.currentTick);
    }

    function getWallUpgradeCost(uint8 currentLevel) external pure returns (uint256 wood, uint256 iron) {
        return LibGameRules.wallUpgradeCost(currentLevel);
    }

    function getBaseUpgradeCost(uint8 currentLevel) external pure returns (uint256 wood, uint256 iron, uint256 wheat) {
        return LibGameRules.baseUpgradeCost(currentLevel);
    }

    function getMonumentUpgradeCost(uint8 currentLevel)
        external
        pure
        returns (uint256 wood, uint256 iron, uint256 wheat, uint256 blueprint)
    {
        return LibGameRules.monumentUpgradeCost(currentLevel);
    }

    function getActionDuration(ActionType action) external pure returns (uint64) {
        return LibGameRules.actionDuration(action);
    }

    function getTravelTicks(uint8 fromRegion, uint8 toRegion) external pure returns (uint64) {
        return uint64(LibTravel.travelTicksBetween(fromRegion, toRegion));
    }
}
