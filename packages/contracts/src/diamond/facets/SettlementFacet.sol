// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ClanState, IClanWorldEvents} from "../../IClanWorld.sol";
import {LibGameRules} from "../lib/LibGameRules.sol";
import {LibSettlement} from "../lib/LibSettlement.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract SettlementFacet is IClanWorldEvents {
    function settleClan(uint32 clanId) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);
        LibGameRules.requireWorldNotPaused(s);
        LibGameRules.requireNoPendingSeasonFinalization(s);
        _settleClan(s, clanId);
        LibStorage.exitNonReentrant(s);
    }

    function settleClansman(uint32 clansmanId) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);
        LibGameRules.requireWorldNotPaused(s);
        LibGameRules.requireNoPendingSeasonFinalization(s);

        uint32 clanId = s.clansmen[clansmanId].clanId;
        if (s.clansmen[clansmanId].clansmanId != 0) {
            _settleClan(s, clanId);
        }

        LibStorage.exitNonReentrant(s);
    }

    function _settleClan(LibStorage.AppStorage storage s, uint32 clanId) private {
        if (s.clans[clanId].clanId == 0) return;
        if (s.clans[clanId].clanState == ClanState.DEAD) return;
        uint64 fromTick = s.clans[clanId].lastSettledTick;
        if (fromTick >= s.world.currentTick) return;

        LibSettlement.SettlementSimulation memory sim = LibSettlement.simulateToTick(s, clanId, s.world.currentTick);
        LibSettlement.commitSimulation(s, sim);
        emit ClanSettled(clanId, s.clans[clanId].lastSettledTick);
    }
}
