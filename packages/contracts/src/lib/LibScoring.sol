// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Clan} from "../IClanWorld.sol";

library LibScoring {
    function lootValue(Clan memory clan) internal pure returns (uint256) {
        return clan.vaultWood + clan.vaultWheat + clan.vaultFish * 2 + clan.vaultIron * 4;
    }

    function packClanScore(Clan memory clan, uint64 monumentReachedAtTick, uint8 monumentLevel)
        internal
        pure
        returns (uint256 score, uint64, uint8)
    {
        uint256 loot = lootValue(clan);
        uint256 maxLootComponent = (uint256(1) << 176) - 1;
        if (loot > maxLootComponent) {
            loot = maxLootComponent;
        }

        uint256 timeComponent;
        if (monumentLevel > 0) {
            timeComponent = uint256(type(uint64).max) - uint256(monumentReachedAtTick);
        }

        score = (uint256(monumentLevel) << 248) | (timeComponent << 184) | (loot << 8) | clan.wallLevel;
        return (score, monumentReachedAtTick, monumentLevel);
    }

    function rankingComesAfter(uint32 leftClanId, uint256 leftScore, uint32 rightClanId, uint256 rightScore)
        internal
        pure
        returns (bool)
    {
        if (leftScore != rightScore) {
            return leftScore < rightScore;
        }
        return leftClanId > rightClanId;
    }

    function banditAttackPower(uint32 strength) internal pure returns (uint16) {
        if (strength > type(uint16).max) return type(uint16).max;
        return uint16(strength);
    }
}
