// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ClanState, IClanWorldEvents} from "../../IClanWorld.sol";
import {LibGameRules} from "../lib/LibGameRules.sol";
import {LibSettlement} from "../lib/LibSettlement.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract GoldTransferFacet is IClanWorldEvents {
    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);
        LibGameRules.requireWorldNotPaused(s);
        LibGameRules.requireNoPendingSeasonFinalization(s);
        require(amount > 0, "ClanWorld: zero amount");
        require(fromClanId != toClanId, "ClanWorld: same clan");

        require(s.clans[fromClanId].clanId != 0, "ClanWorld: from clan not found");
        require(s.clans[fromClanId].owner == msg.sender, "ClanWorld: not clan owner");
        require(s.clans[toClanId].clanId != 0, "ClanWorld: to clan not found");

        _settleClan(s, fromClanId);
        _settleClan(s, toClanId);

        require(
            s.clans[fromClanId].lastSettledTick == s.world.currentTick
                && s.clans[toClanId].lastSettledTick == s.world.currentTick,
            "ERR_MUST_SETTLE_FIRST"
        );
        require(s.clans[fromClanId].clanState != ClanState.DEAD, "ClanWorld: clan dead");
        require(s.clans[fromClanId].goldBalance >= amount, "ClanWorld: insufficient gold");

        s.clans[fromClanId].goldBalance -= amount;
        s.clans[toClanId].goldBalance += amount;

        emit GoldTransferred(fromClanId, toClanId, amount, s.world.currentTick);
        LibStorage.exitNonReentrant(s);
    }

    function _settleClan(LibStorage.AppStorage storage s, uint32 clanId) private {
        if (s.clans[clanId].clanId == 0) return;
        if (s.clans[clanId].clanState == ClanState.DEAD) return;
        if (s.clans[clanId].lastSettledTick >= s.world.currentTick) return;

        LibSettlement.SettlementSimulation memory sim = LibSettlement.simulateToTick(s, clanId, s.world.currentTick);
        LibSettlement.commitSimulation(s, sim);
        emit ClanSettled(clanId, s.clans[clanId].lastSettledTick);
    }
}
