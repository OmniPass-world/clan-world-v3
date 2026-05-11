// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {BanditTroop, Clan, ClanState, ClanWorldConstants} from "../../IClanWorld.sol";
import {LibScoring} from "../../lib/LibScoring.sol";
import {LibStorage} from "./LibStorage.sol";

library LibBanditTargets {
    function pickBanditAttackTarget(LibStorage.AppStorage storage s, BanditTroop storage bandit)
        public
        view
        returns (uint32 targetClanId)
    {
        uint256 bestLootValue;

        for (uint256 i = 0; i < s.allClanIds.length; i++) {
            uint32 clanId = s.allClanIds[i];
            Clan storage clan = s.clans[clanId];
            if (clan.clanState == ClanState.DEAD || clan.baseRegion != bandit.region || clan.livingClansmen == 0) {
                continue;
            }

            uint256 lootValue = LibScoring.lootValue(clan);
            if (targetClanId == ClanWorldConstants.CLAN_ID_NULL || lootValue > bestLootValue) {
                bestLootValue = lootValue;
                targetClanId = clanId;
            } else if (lootValue == bestLootValue && clanId < targetClanId) {
                targetClanId = clanId;
            }
        }
    }
}
