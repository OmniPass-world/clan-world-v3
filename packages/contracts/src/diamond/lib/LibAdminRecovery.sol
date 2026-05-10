// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {
    ActionType,
    Clan,
    ClanState,
    Clansman,
    ClansmanState,
    Mission,
    ScheduledMarketAction
} from "../../IClanWorld.sol";
import {LibOrderDefenders} from "./LibOrderDefenders.sol";
import {LibOrderUpgrades} from "./LibOrderUpgrades.sol";
import {LibStorage} from "./LibStorage.sol";

library LibAdminRecovery {
    function reviveClansman(LibStorage.AppStorage storage s, uint32 clansmanId)
        internal
        returns (uint32 clanId, bool revived)
    {
        Clansman storage cs = s.clansmen[clansmanId];
        clanId = cs.clanId;
        if (clanId == 0 || cs.clansmanId == 0 || cs.state != ClansmanState.DEAD) return (clanId, false);

        Clan storage clan = s.clans[clanId];
        if (clan.clanId == 0) return (clanId, false);

        uint8 livingBefore = clan.livingClansmen;
        _clearClansmanRecoveryState(s, clansmanId);

        cs.state = ClansmanState.WAITING;
        cs.currentRegion = clan.baseRegion;
        cs.cooldownEndsAtTs = 0;
        cs.lastMissionNonce = s.world.currentTick;
        cs.carryWood = 0;
        cs.carryIron = 0;
        cs.carryWheat = 0;
        cs.carryFish = 0;

        clan.livingClansmen = livingBefore + 1;
        clan.coldDamage = 0;
        clan.starvationStartsAtTick = 0;
        if (clan.clanState == ClanState.DEAD && livingBefore == 0) {
            clan.clanState = ClanState.ACTIVE;
        }

        return (clanId, true);
    }

    function injectClanResources(
        LibStorage.AppStorage storage s,
        uint32 clanId,
        uint256 wood,
        uint256 iron,
        uint256 wheat,
        uint256 fish
    ) internal returns (bool injected) {
        Clan storage clan = s.clans[clanId];
        if (clan.clanId == 0) return false;

        clan.vaultWood += wood;
        clan.vaultIron += iron;
        clan.vaultWheat += wheat;
        clan.vaultFish += fish;
        return true;
    }

    function _clearClansmanRecoveryState(LibStorage.AppStorage storage s, uint32 clansmanId) private {
        Mission memory mission = s.missions[clansmanId];

        LibOrderDefenders.clearDefender(s, clansmanId);
        LibOrderUpgrades.refundUpgradeReservation(s, clansmanId, mission.action);
        LibOrderUpgrades.clearWallUpgradeReservation(s, clansmanId);
        LibOrderUpgrades.clearBaseUpgradeReservation(s, clansmanId);
        LibOrderUpgrades.clearMonumentUpgradeReservation(s, clansmanId);

        if (mission.active && (mission.action == ActionType.MarketBuy || mission.action == ActionType.MarketSell)) {
            _purgeScheduledMarketActions(s, clansmanId, mission.settlesAtTick);
        }

        delete s.marketMissionCommitSequence[clansmanId];
        delete s.missions[clansmanId];
    }

    function _purgeScheduledMarketActions(LibStorage.AppStorage storage s, uint32 clansmanId, uint64 tick) private {
        ScheduledMarketAction[] storage actions = s.scheduledMarketActions[tick];
        uint256 i = 0;
        while (i < actions.length) {
            if (actions[i].clansmanId == clansmanId) {
                actions[i] = actions[actions.length - 1];
                actions.pop();
            } else {
                i++;
            }
        }
    }
}
