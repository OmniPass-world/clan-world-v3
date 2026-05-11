// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {BanditState, BanditTroop, ClanWorldConstants} from "../../IClanWorld.sol";
import {LibBanditCleanup} from "./LibBanditCleanup.sol";
import {LibStorage} from "./LibStorage.sol";

library LibBanditLifecycle {
    event BanditStateChanged(
        uint32 indexed banditId, BanditState oldState, BanditState newState, uint8 region, uint64 atTick
    );

    function transitionBanditToAttacking(LibStorage.AppStorage storage s, uint32 id, uint32 targetClanId) public {
        require(targetClanId != ClanWorldConstants.CLAN_ID_NULL, "ClanWorld: invalid bandit target");
        s.bandits[id].targetClanId = targetClanId;
        transitionBanditState(s, id, BanditState.Attacking);
    }

    function transitionBanditState(LibStorage.AppStorage storage s, uint32 id, BanditState newState) public {
        BanditTroop storage bandit = s.bandits[id];
        require(bandit.id != ClanWorldConstants.BANDIT_ID_NULL, "ClanWorld: bandit not found");
        require(newState != BanditState.None, "ClanWorld: invalid bandit transition");

        BanditState oldState = bandit.state;
        require(isValidBanditTransition(s, bandit, newState), "ClanWorld: invalid bandit transition");

        if (newState == BanditState.Defeated) {
            emit BanditStateChanged(id, oldState, newState, bandit.region, s.world.currentTick);
            LibBanditCleanup.deleteBandit(s, id);
            return;
        }

        bandit.state = newState;
        bandit.tickEnteredState = s.world.currentTick;
        if (newState != BanditState.Attacking) {
            bandit.targetClanId = ClanWorldConstants.CLAN_ID_NULL;
        }

        emit BanditStateChanged(id, oldState, newState, bandit.region, s.world.currentTick);

        if (oldState == BanditState.Attacking && newState == BanditState.Camped) {
            LibBanditCleanup.moveBanditToRampageNextRegion(s, id);
        }
    }

    function isValidBanditTransition(LibStorage.AppStorage storage s, BanditTroop storage bandit, BanditState newState)
        public
        view
        returns (bool)
    {
        if (bandit.state == BanditState.Spawned) {
            return newState == BanditState.Camped && s.world.currentTick >= bandit.tickEnteredState + 1;
        }
        if (bandit.state == BanditState.Attacking) {
            return newState == BanditState.Defeated || newState == BanditState.Camped;
        }
        if (bandit.state == BanditState.Camped) {
            return newState == BanditState.Attacking && bandit.targetClanId != ClanWorldConstants.CLAN_ID_NULL
                && s.world.currentTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS;
        }
        return false;
    }

    function recordBanditAttackAttempt(LibStorage.AppStorage storage s, uint32 banditId)
        public
        returns (uint8 attemptsMade)
    {
        BanditTroop storage bandit = s.bandits[banditId];
        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL) {
            return 0;
        }
        attemptsMade = bandit.attackAttemptsMade + 1;
        bandit.attackAttemptsMade = attemptsMade;
    }

}
