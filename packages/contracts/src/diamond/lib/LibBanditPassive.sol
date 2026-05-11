// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {BanditState, BanditTroop, ClanWorldConstants} from "../../IClanWorld.sol";
import {LibBanditCleanup} from "./LibBanditCleanup.sol";
import {LibBanditLifecycle} from "./LibBanditLifecycle.sol";
import {LibBanditTargets} from "./LibBanditTargets.sol";
import {LibSettlement} from "./LibSettlement.sol";
import {LibStorage} from "./LibStorage.sol";

library LibBanditPassive {
    event BanditStateChanged(
        uint32 indexed banditId, BanditState oldState, BanditState newState, uint8 region, uint64 atTick
    );

    function advancePassiveBanditStates(LibStorage.AppStorage storage s, uint64 closedTick) public {
        require(s.world.currentTick == closedTick, "ClanWorld: bandit advance tick mismatch");
        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
            uint32[] storage regionBandits = s.banditsByRegion[region];
            for (uint256 i = regionBandits.length; i > 0; i--) {
                uint32 banditId = regionBandits[i - 1];
                BanditTroop storage bandit = s.bandits[banditId];
                uint8 regionBefore = bandit.region;

                if (bandit.state == BanditState.Spawned && closedTick > bandit.tickEnteredState) {
                    LibBanditLifecycle.transitionBanditState(s, banditId, BanditState.Camped);
                } else if (
                    bandit.state == BanditState.Camped
                        && closedTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS
                ) {
                    LibSettlement.eagerSettleBanditCandidateRegion(s, bandit.region);
                    if (s.bandits[banditId].id == ClanWorldConstants.BANDIT_ID_NULL) continue;
                    uint32 targetClanId = LibBanditTargets.pickBanditAttackTarget(s, bandit);
                    if (targetClanId == ClanWorldConstants.CLAN_ID_NULL) {
                        if (
                            LibBanditLifecycle.recordBanditAttackAttempt(s, banditId)
                                >= ClanWorldConstants.BANDIT_MAX_ATTACK_ATTEMPTS
                        ) {
                            LibBanditCleanup.terminalEscapeBandit(s, banditId, closedTick);
                            continue;
                        }
                        bandit.tickEnteredState = s.world.currentTick;
                        bandit.targetClanId = ClanWorldConstants.CLAN_ID_NULL;
                        LibBanditCleanup.moveBanditToRampageNextRegion(s, banditId);
                        emit BanditStateChanged(
                            banditId, BanditState.Camped, BanditState.Camped, bandit.region, s.world.currentTick
                        );
                    } else {
                        LibBanditLifecycle.transitionBanditToAttacking(s, banditId, targetClanId);
                    }
                }

                if (s.bandits[banditId].id == ClanWorldConstants.BANDIT_ID_NULL) continue;
                if (regionBefore != s.bandits[banditId].region) continue;
            }
        }
    }
}
