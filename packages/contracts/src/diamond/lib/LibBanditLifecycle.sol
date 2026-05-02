// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {BanditState, BanditTroop, ClanWorldConstants} from "../../IClanWorld.sol";
import {LibStorage} from "./LibStorage.sol";

library LibBanditLifecycle {
    event BanditStateChanged(
        uint32 indexed banditId, BanditState oldState, BanditState newState, uint8 region, uint64 atTick
    );
    event BanditMoved(uint32 indexed banditId, uint8 fromRegion, uint8 toRegion, uint64 atTick);

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

    function transitionBanditState(LibStorage.AppStorage storage s, uint32 id, BanditState newState) public {
        BanditTroop storage bandit = s.bandits[id];
        require(bandit.id != ClanWorldConstants.BANDIT_ID_NULL, "ClanWorld: bandit not found");
        require(newState != BanditState.None, "ClanWorld: invalid bandit transition");

        BanditState oldState = bandit.state;
        require(isValidBanditTransition(s, bandit, newState), "ClanWorld: invalid bandit transition");

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
