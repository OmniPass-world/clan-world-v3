// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {
    Clan,
    ClanOrder,
    ClanState,
    MarketExecutionMode,
    OrderResult,
    StatusCode
} from "../../IClanWorld.sol";
import {LibGameRules} from "./LibGameRules.sol";
import {LibSettlement} from "./LibSettlement.sol";
import {LibStorage} from "./LibStorage.sol";
import {LibSubmitOrderProcessing} from "./LibSubmitOrderProcessing.sol";

library LibSubmitOrders {
    event ClanSettled(uint32 indexed clanId, uint64 settledToTick);

    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
        public
        returns (OrderResult[] memory results)
    {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);
        LibGameRules.requireNoPendingSeasonFinalization(s);

        Clan storage clan = s.clans[clanId];
        require(clan.clanId != 0, "ClanWorld: clan not found");
        require(clan.owner == msg.sender, "ClanWorld: not clan owner");
        require(clan.clanState == ClanState.ACTIVE, "ClanWorld: clan dead");

        if (s.world.currentTick > clan.lastSettledTick + LibGameRules.MAX_LAZY_SETTLE_BACKLOG) {
            results = new OrderResult[](orders.length);
            for (uint256 i = 0; i < orders.length; i++) {
                results[i] = OrderResult({
                    clansmanId: orders[i].clansmanId,
                    status: StatusCode.ERR_MUST_SETTLE_FIRST,
                    cooldownEndsAtTs: 0,
                    missionNonce: 0,
                    marketMode: MarketExecutionMode.None
                });
            }
            LibStorage.exitNonReentrant(s);
            return results;
        }

        _settleClan(s, clanId);

        results = new OrderResult[](orders.length);
        if (clan.clanState == ClanState.DEAD) {
            for (uint256 i = 0; i < orders.length; i++) {
                results[i] = OrderResult({
                    clansmanId: orders[i].clansmanId,
                    status: StatusCode.ERR_CLAN_DEAD,
                    cooldownEndsAtTs: 0,
                    missionNonce: 0,
                    marketMode: MarketExecutionMode.None
                });
            }
            LibStorage.exitNonReentrant(s);
            return results;
        }

        for (uint256 i = 0; i < orders.length; i++) {
            results[i] = LibSubmitOrderProcessing.processOrder(s, clanId, clan, orders[i]);
        }

        LibStorage.exitNonReentrant(s);
        return results;
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
