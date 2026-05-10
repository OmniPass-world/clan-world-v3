// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ActiveBanditView, BanditState, BanditTroop, ClanWorldConstants} from "../../IClanWorld.sol";
import {LibScoring} from "../../lib/LibScoring.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract BanditViewsFacet {
    function getActiveBanditView() external view returns (ActiveBanditView memory) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        BanditTroop memory bandit = s.bandits[s.world.activeBanditId];
        uint64 nextActionTick = 0;
        uint8 maxAttemptsRemaining = 0;
        bool exists = bandit.id != ClanWorldConstants.BANDIT_ID_NULL && bandit.state != BanditState.None;
        if (exists) {
            if (bandit.state == BanditState.Spawned) {
                nextActionTick = bandit.tickEnteredState + 1;
            } else if (bandit.state == BanditState.Camped) {
                nextActionTick = bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS;
            }
            if (bandit.attackAttemptsMade < ClanWorldConstants.BANDIT_MAX_ATTACK_ATTEMPTS) {
                maxAttemptsRemaining = ClanWorldConstants.BANDIT_MAX_ATTACK_ATTEMPTS - bandit.attackAttemptsMade;
            }
        }

        return ActiveBanditView({
            exists: exists,
            banditId: bandit.id,
            state: bandit.state,
            currentRegion: bandit.region,
            attackAttemptsMade: bandit.attackAttemptsMade,
            maxAttemptsRemaining: maxAttemptsRemaining,
            stateEnteredTick: bandit.tickEnteredState,
            nextActionTick: nextActionTick,
            tier: bandit.tier,
            attackPower: LibScoring.banditAttackPower(bandit.strength),
            carryWood: bandit.carryWood,
            carryIron: bandit.carryIron,
            carryWheat: bandit.carryWheat,
            carryFish: bandit.carryFish,
            projectedTargetClanId: bandit.targetClanId,
            projectedTargetLootValue: 0
        });
    }
}
