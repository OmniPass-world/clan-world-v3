// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ClanWorldConstants} from "../../IClanWorld.sol";
import {LibDiamond} from "../lib/LibDiamond.sol";
import {LibStorage} from "../lib/LibStorage.sol";
import {LibWorldClock} from "../lib/LibWorldClock.sol";

contract HeartbeatConfigFacet {
    uint64 private constant MAX_HEARTBEAT_INTERVAL_SECONDS = 1 hours;
    uint64 private constant MAX_CLANSMAN_COOLDOWN_SECONDS = 1 hours;

    event HeartbeatIntervalUpdated(uint64 oldIntervalSeconds, uint64 newIntervalSeconds, uint64 nextHeartbeatAtTs);
    event ClansmanCooldownUpdated(uint64 oldCooldownSeconds, uint64 newCooldownSeconds);
    event BanditSpawnTriggered(uint64 currentTick);

    function heartbeatIntervalSeconds() external view returns (uint64) {
        return LibWorldClock.heartbeatIntervalSeconds(LibStorage.appStorage());
    }

    function setHeartbeatIntervalSeconds(uint64 intervalSeconds) external {
        LibDiamond.enforceIsContractOwner();
        require(
            intervalSeconds > 0 && intervalSeconds <= MAX_HEARTBEAT_INTERVAL_SECONDS,
            "ClanWorld: invalid heartbeat interval"
        );

        LibStorage.AppStorage storage s = LibStorage.appStorage();
        uint64 oldIntervalSeconds = LibWorldClock.heartbeatIntervalSeconds(s);
        s.heartbeatIntervalSeconds = intervalSeconds;

        uint64 nextHeartbeatAtTs = uint64(block.timestamp) + intervalSeconds;
        if (s.world.nextHeartbeatAtTs > nextHeartbeatAtTs) {
            s.world.nextHeartbeatAtTs = nextHeartbeatAtTs;
        }

        emit HeartbeatIntervalUpdated(oldIntervalSeconds, intervalSeconds, s.world.nextHeartbeatAtTs);
    }

    function clansmanCooldownSeconds() external view returns (uint64) {
        return _clansmanCooldownSeconds(LibStorage.appStorage());
    }

    function setClansmanCooldownSeconds(uint64 cooldownSeconds) external {
        LibDiamond.enforceIsContractOwner();
        require(
            cooldownSeconds > 0 && cooldownSeconds <= MAX_CLANSMAN_COOLDOWN_SECONDS,
            "ClanWorld: invalid clansman cooldown"
        );

        LibStorage.AppStorage storage s = LibStorage.appStorage();
        uint64 oldCooldownSeconds = _clansmanCooldownSeconds(s);
        s.clansmanCooldownSeconds = cooldownSeconds;

        emit ClansmanCooldownUpdated(oldCooldownSeconds, cooldownSeconds);
    }

    function banditSpawnTriggered() external view returns (bool) {
        return LibStorage.appStorage().forceBanditSpawnNextHeartbeat;
    }

    /// @notice Owner-only pause allowlist action.
    /// @dev Allowed during pause. This sets a queued flag
    ///      (`forceBanditSpawnNextHeartbeat = true`) that fires on the first
    ///      post-unpause heartbeat. Owners can use it to stage recovery scenarios;
    ///      the spawn itself does not happen until the world is unpaused. See the
    ///      contracts README pause allowlist for the full policy.
    function triggerBanditSpawn() external {
        LibDiamond.enforceIsContractOwner();

        LibStorage.AppStorage storage s = LibStorage.appStorage();
        s.forceBanditSpawnNextHeartbeat = true;
        s.world.currentBanditSpawnChanceBps = 10000;

        emit BanditSpawnTriggered(s.world.currentTick);
    }

    function _clansmanCooldownSeconds(LibStorage.AppStorage storage s) private view returns (uint64) {
        uint64 configuredCooldownSeconds = s.clansmanCooldownSeconds;
        if (configuredCooldownSeconds == 0) {
            return ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
        }
        return configuredCooldownSeconds;
    }
}
