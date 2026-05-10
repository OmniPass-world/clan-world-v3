// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {IClanWorldEvents} from "../../IClanWorld.sol";
import {LibAdminRecovery} from "../lib/LibAdminRecovery.sol";
import {LibDiamond} from "../lib/LibDiamond.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract AdminRecoveryFacet is IClanWorldEvents {
    function reviveDeadClansmen(uint32 clanId) external {
        LibDiamond.enforceIsContractOwner();
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);

        uint32[] storage clansmanIds = s.clanClansmanIds[clanId];
        for (uint256 i = 0; i < clansmanIds.length; i++) {
            (uint32 revivedClanId, bool revived) = LibAdminRecovery.reviveClansman(s, clansmanIds[i]);
            if (revived) emit ClansmanRevived(revivedClanId, clansmanIds[i], s.world.currentTick);
        }

        LibStorage.exitNonReentrant(s);
    }

    function reviveClansman(uint32 clansmanId) external {
        LibDiamond.enforceIsContractOwner();
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);

        (uint32 clanId, bool revived) = LibAdminRecovery.reviveClansman(s, clansmanId);
        if (revived) emit ClansmanRevived(clanId, clansmanId, s.world.currentTick);

        LibStorage.exitNonReentrant(s);
    }

    function injectClanResources(uint32 clanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish) external {
        LibDiamond.enforceIsContractOwner();
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);

        if (LibAdminRecovery.injectClanResources(s, clanId, wood, iron, wheat, fish)) {
            emit ResourcesInjected(clanId, wood, iron, wheat, fish, s.world.currentTick);
        }

        LibStorage.exitNonReentrant(s);
    }
}
