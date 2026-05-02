// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {BanditState, BanditTroop, Clan, ClanState, ClanWorldConstants} from "../../IClanWorld.sol";
import {LibScoring} from "../../lib/LibScoring.sol";
import {LibStorage} from "./LibStorage.sol";

library LibBanditLifecycle {
    event BanditStateChanged(
        uint32 indexed banditId, BanditState oldState, BanditState newState, uint8 region, uint64 atTick
    );
    event BanditMoved(uint32 indexed banditId, uint8 fromRegion, uint8 toRegion, uint64 atTick);
    event BanditEscaped(uint32 indexed banditId, uint64 atTick);
    event LootDistributed(
        uint32 indexed banditId,
        uint32[] rewardedClanIds,
        uint256 woodPerClan,
        uint256 wheatPerClan,
        uint256 fishPerClan,
        uint256 ironPerClan,
        uint256 goldPerClan,
        uint256 woodRemainder,
        uint256 wheatRemainder,
        uint256 fishRemainder,
        uint256 ironRemainder,
        uint256 goldRemainder
    );

    function advancePassiveBanditStates(LibStorage.AppStorage storage s, uint64 closedTick) public {
        require(s.world.currentTick == closedTick, "ClanWorld: bandit advance tick mismatch");
        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
            uint32[] storage regionBandits = s.banditsByRegion[region];
            uint256 i = 0;
            while (i < regionBandits.length) {
                uint32 banditId = regionBandits[i];
                BanditTroop storage bandit = s.bandits[banditId];
                uint8 regionBefore = bandit.region;

                if (bandit.state == BanditState.Spawned && closedTick > bandit.tickEnteredState) {
                    transitionBanditState(s, banditId, BanditState.Camped);
                } else if (
                    bandit.state == BanditState.Camped
                        && closedTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS
                ) {
                    uint32 targetClanId = pickBanditAttackTarget(s, bandit);
                    if (targetClanId == ClanWorldConstants.CLAN_ID_NULL) {
                        if (recordBanditAttackAttempt(s, banditId) >= ClanWorldConstants.BANDIT_MAX_ATTACK_ATTEMPTS) {
                            terminalEscapeBandit(s, banditId, closedTick);
                            continue;
                        }
                        transitionBanditState(s, banditId, BanditState.Resting);
                    } else {
                        transitionBanditToAttacking(s, banditId, targetClanId);
                    }
                } else if (
                    bandit.state == BanditState.Escaped
                        && closedTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS
                ) {
                    transitionBanditState(s, banditId, BanditState.Resting);
                } else if (
                    bandit.state == BanditState.Resting
                        && closedTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS
                ) {
                    transitionBanditState(s, banditId, BanditState.Camped);
                }

                if (s.bandits[banditId].id == ClanWorldConstants.BANDIT_ID_NULL) continue;
                if (regionBefore != s.bandits[banditId].region) continue;
                i++;
            }
        }
    }

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
            deleteBandit(s, id);
            return;
        }

        bandit.state = newState;
        bandit.tickEnteredState = s.world.currentTick;
        if (newState != BanditState.Attacking) {
            bandit.targetClanId = ClanWorldConstants.CLAN_ID_NULL;
        }

        emit BanditStateChanged(id, oldState, newState, bandit.region, s.world.currentTick);

        if (oldState == BanditState.Resting && newState == BanditState.Camped) {
            moveBanditToRampageNextRegion(s, id);
        }
    }

    function isValidBanditTransition(LibStorage.AppStorage storage s, BanditTroop storage bandit, BanditState newState)
        public
        view
        returns (bool)
    {
        if (bandit.state == BanditState.Spawned) {
            return newState == BanditState.Escaped
                || (newState == BanditState.Camped && s.world.currentTick >= bandit.tickEnteredState + 1);
        }
        if (bandit.state == BanditState.Attacking) {
            return
                newState == BanditState.Defeated || newState == BanditState.Escaped || newState == BanditState.Resting;
        }
        if (bandit.state == BanditState.Escaped) {
            return newState == BanditState.Resting;
        }
        if (bandit.state == BanditState.Resting) {
            return newState == BanditState.Escaped
                || (newState == BanditState.Camped
                    && s.world.currentTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS);
        }
        if (bandit.state == BanditState.Camped) {
            return newState == BanditState.Escaped || newState == BanditState.Resting
                || (newState == BanditState.Attacking
                    && bandit.targetClanId != ClanWorldConstants.CLAN_ID_NULL
                    && s.world.currentTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS);
        }
        return false;
    }

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

    function terminalEscapeBandit(LibStorage.AppStorage storage s, uint32 banditId, uint64 closedTick) public {
        BanditTroop storage bandit = s.bandits[banditId];
        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL) {
            return;
        }

        BanditState oldState = bandit.state;
        emit BanditStateChanged(banditId, oldState, BanditState.Escaped, bandit.region, closedTick);
        burnBanditCarry(s, banditId);
        emit BanditEscaped(banditId, closedTick);
        deleteBandit(s, banditId);
    }

    function burnBanditCarry(LibStorage.AppStorage storage s, uint32 banditId) public {
        BanditTroop storage bandit = s.bandits[banditId];
        uint32[] memory rewardedClanIds = new uint32[](0);
        emit LootDistributed(
            banditId,
            rewardedClanIds,
            0,
            0,
            0,
            0,
            0,
            bandit.carryWood,
            bandit.carryWheat,
            bandit.carryFish,
            bandit.carryIron,
            bandit.carryGold
        );
    }

    function deleteBandit(LibStorage.AppStorage storage s, uint32 id) public {
        BanditTroop storage bandit = s.bandits[id];
        uint8 region = bandit.region;
        uint32[] storage regionBandits = s.banditsByRegion[region];
        for (uint256 i = 0; i < regionBandits.length; i++) {
            if (regionBandits[i] == id) {
                regionBandits[i] = regionBandits[regionBandits.length - 1];
                regionBandits.pop();
                break;
            }
        }

        delete s.bandits[id];
        if (s.activeBanditCount > 0) {
            s.activeBanditCount -= 1;
        }
        if (s.world.activeBanditId == id) {
            s.world.activeBanditId = findOldestActiveBandit(s);
        }
    }

    function findOldestActiveBandit(LibStorage.AppStorage storage s) public view returns (uint32 oldestBanditId) {
        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
            uint32[] storage regionBandits = s.banditsByRegion[region];
            for (uint256 i = 0; i < regionBandits.length; i++) {
                uint32 candidateId = regionBandits[i];
                BanditTroop storage candidate = s.bandits[candidateId];
                if (candidate.id == ClanWorldConstants.BANDIT_ID_NULL || candidate.state == BanditState.None) {
                    continue;
                }
                if (oldestBanditId == ClanWorldConstants.BANDIT_ID_NULL || candidateId < oldestBanditId) {
                    oldestBanditId = candidateId;
                }
            }
        }
    }

    function moveBanditToRampageNextRegion(LibStorage.AppStorage storage s, uint32 id) public {
        BanditTroop storage bandit = s.bandits[id];
        uint8 fromRegion = bandit.region;
        uint8 toRegion = nextRampageRegion(fromRegion);
        if (fromRegion == toRegion) return;

        uint32[] storage fromBandits = s.banditsByRegion[fromRegion];
        for (uint256 i = 0; i < fromBandits.length; i++) {
            if (fromBandits[i] == id) {
                fromBandits[i] = fromBandits[fromBandits.length - 1];
                fromBandits.pop();
                break;
            }
        }

        bandit.region = toRegion;
        s.banditsByRegion[toRegion].push(id);
        emit BanditMoved(id, fromRegion, toRegion, s.world.currentTick);
    }

    function nextRampageRegion(uint8 currentRegion) public pure returns (uint8) {
        if (currentRegion == ClanWorldConstants.REGION_FOREST) return ClanWorldConstants.REGION_MOUNTAINS;
        if (currentRegion == ClanWorldConstants.REGION_MOUNTAINS) return ClanWorldConstants.REGION_EAST_FARMS;
        if (currentRegion == ClanWorldConstants.REGION_EAST_FARMS) return ClanWorldConstants.REGION_EAST_DOCKS;
        if (currentRegion == ClanWorldConstants.REGION_EAST_DOCKS) return ClanWorldConstants.REGION_WEST_DOCKS;
        if (currentRegion == ClanWorldConstants.REGION_WEST_DOCKS) return ClanWorldConstants.REGION_WEST_FARMS;
        return ClanWorldConstants.REGION_FOREST;
    }
}
