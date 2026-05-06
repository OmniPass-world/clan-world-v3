// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ClanState, IClanWorldEvents} from "../../IClanWorld.sol";
import {LibGameRules} from "../lib/LibGameRules.sol";
import {LibSettlement} from "../lib/LibSettlement.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract ClanOwnershipFacet is IClanWorldEvents {
    function transferClanOwnership(uint32 clanId, address newOwner) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);
        LibGameRules.requireWorldNotPaused(s);
        LibGameRules.requireNoPendingSeasonFinalization(s);

        require(s.clans[clanId].clanId != 0, "ClanWorld: clan not found");
        require(s.clans[clanId].owner == msg.sender, "ClanWorld: not clan owner");
        require(newOwner != address(0), "ClanWorld: zero address");
        require(newOwner != s.clans[clanId].owner, "ClanWorld: same owner");

        LibSettlement.SettlementSimulation memory sim = LibSettlement.simulateToTick(s, clanId, s.world.currentTick);
        LibSettlement.commitSimulation(s, sim);

        require(s.clans[clanId].clanState != ClanState.DEAD, "ClanWorld: clan dead");
        address oldOwner = s.clans[clanId].owner;
        s.clans[clanId].owner = newOwner;
        s.clans[clanId].ownerNonce++;
        emit ClanOwnershipTransferred(clanId, oldOwner, newOwner, s.clans[clanId].ownerNonce);

        LibStorage.exitNonReentrant(s);
    }
}
