// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ClanState} from "../../IClanWorld.sol";
import {LibBanditCombat} from "./LibBanditCombat.sol";
import {LibBanditPassive} from "./LibBanditPassive.sol";
import {LibBanditSpawning} from "./LibBanditSpawning.sol";
import {LibGameRules} from "./LibGameRules.sol";
import {LibOrderMarket} from "./LibOrderMarket.sol";
import {LibSettlement} from "./LibSettlement.sol";
import {LibStorage} from "./LibStorage.sol";
import {LibWorldClock} from "./LibWorldClock.sol";
import {LibWorldEvents} from "./LibWorldEvents.sol";

library LibHeartbeat {
    event TickAdvanced(uint64 closedTick, uint64 openedTick, bytes32 tickSeed);
    event ClanSettled(uint32 indexed clanId, uint64 settledToTick);

    function tick(LibStorage.AppStorage storage s) internal {
        LibGameRules.requireWorldNotPaused(s);
        require(block.timestamp >= s.world.nextHeartbeatAtTs, "ClanWorld: heartbeat rate limited");

        if (isSeasonFinalizationPending(s)) {
            return;
        }
        if (isSeasonRolloverPending(s)) {
            LibWorldClock.scheduleNextHeartbeat(s);
            LibWorldEvents.rollSeason(s);
            return;
        }

        uint64 closedTick = s.world.currentTick;
        bytes32 closedTickSeed = s.world.currentTickSeed;
        LibWorldClock.scheduleNextHeartbeat(s);

        for (uint256 i = 0; i < s.allClanIds.length; i++) {
            uint32 clanId = s.allClanIds[i];
            if (s.clans[clanId].clanState != ClanState.DEAD && s.clans[clanId].lastSettledTick <= closedTick) {
                LibSettlement.SettlementSimulation memory sim = LibSettlement.simulateToTick(s, clanId, closedTick + 1);
                LibSettlement.commitSimulation(s, sim);
                emit ClanSettled(clanId, s.clans[clanId].lastSettledTick);
            }
        }

        LibOrderMarket.executeScheduledMarketActions(s, closedTick);
        LibBanditPassive.advancePassiveBanditStates(s, closedTick);
        LibBanditCombat.resolveAttackingBandits(s, closedTick);
        LibBanditSpawning.evaluateBanditSpawns(s, closedTickSeed);
        LibWorldEvents.resolveWorldEvents(s, closedTick);

        uint64 newTick = closedTick + 1;
        bytes32 newSeed = keccak256(abi.encode(block.prevrandao, closedTickSeed, closedTick));
        s.world.currentTick = newTick;
        s.tickSeeds[newTick] = newSeed;
        s.world.currentTickSeed = newSeed;
        s.world.nextHeartbeatAtTick = newTick + 1;

        emit TickAdvanced(closedTick, newTick, newSeed);
    }

    function isSeasonFinalizationPending(LibStorage.AppStorage storage s) internal view returns (bool) {
        return s.world.currentTick >= s.world.seasonEndTick && !s.world.seasonFinalized;
    }

    function isSeasonRolloverPending(LibStorage.AppStorage storage s) internal view returns (bool) {
        return s.world.currentTick >= s.world.seasonEndTick && s.world.seasonFinalized;
    }
}
