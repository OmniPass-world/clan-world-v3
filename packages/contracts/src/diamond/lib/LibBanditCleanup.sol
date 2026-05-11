// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {BanditState, BanditTroop, ClanWorldConstants} from "../../IClanWorld.sol";
import {LibBanditEvents} from "./LibBanditEvents.sol";
import {LibSettlement} from "./LibSettlement.sol";
import {LibStorage} from "./LibStorage.sol";

library LibBanditCleanup {
    event BanditMoved(uint32 indexed banditId, uint8 fromRegion, uint8 toRegion, uint64 atTick);

    function terminalEscapeBandit(LibStorage.AppStorage storage s, uint32 banditId, uint64 closedTick) public {
        BanditTroop storage bandit = s.bandits[banditId];
        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL) {
            return;
        }

        BanditState oldState = bandit.state;
        LibBanditEvents.emitBanditStateChanged(banditId, oldState, BanditState.None, bandit.region, closedTick);
        burnBanditCarry(s, banditId);
        LibBanditEvents.emitBanditEscaped(banditId, closedTick);
        deleteBandit(s, banditId);
    }

    function burnBanditCarry(LibStorage.AppStorage storage s, uint32 banditId) public {
        BanditTroop storage bandit = s.bandits[banditId];
        uint32[] memory rewardedClanIds = new uint32[](0);
        LibBanditEvents.emitLootDistributed(
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
        removeBanditFromRegion(s, id, region);

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

        removeBanditFromRegion(s, id, fromRegion);

        bandit.region = toRegion;
        s.banditsByRegion[toRegion].push(id);
        emit BanditMoved(id, fromRegion, toRegion, s.world.currentTick);

        LibSettlement.eagerSettleBanditCandidateRegion(s, toRegion);
    }

    function nextRampageRegion(uint8 currentRegion) public pure returns (uint8) {
        if (currentRegion == ClanWorldConstants.REGION_FOREST) return ClanWorldConstants.REGION_MOUNTAINS;
        if (currentRegion == ClanWorldConstants.REGION_MOUNTAINS) return ClanWorldConstants.REGION_EAST_FARMS;
        if (currentRegion == ClanWorldConstants.REGION_EAST_FARMS) return ClanWorldConstants.REGION_EAST_DOCKS;
        if (currentRegion == ClanWorldConstants.REGION_EAST_DOCKS) return ClanWorldConstants.REGION_WEST_DOCKS;
        if (currentRegion == ClanWorldConstants.REGION_WEST_DOCKS) return ClanWorldConstants.REGION_WEST_FARMS;
        return ClanWorldConstants.REGION_FOREST;
    }

    function removeBanditFromRegion(LibStorage.AppStorage storage s, uint32 id, uint8 region) private {
        uint32[] storage regionBandits = s.banditsByRegion[region];
        for (uint256 i = 0; i < regionBandits.length; i++) {
            if (regionBandits[i] == id) {
                regionBandits[i] = regionBandits[regionBandits.length - 1];
                regionBandits.pop();
                break;
            }
        }
    }
}
