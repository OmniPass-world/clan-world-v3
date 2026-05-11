// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {BanditState} from "../../IClanWorld.sol";

library LibBanditEvents {
    event BanditStateChanged(
        uint32 indexed banditId, BanditState oldState, BanditState newState, uint8 region, uint64 atTick
    );
    event BanditEscaped(uint32 indexed banditId, uint64 atTick);
    event LootDistributed(
        uint32 indexed banditId,
        uint32[] clanIdsRewarded,
        uint256 perClanWood,
        uint256 perClanWheat,
        uint256 perClanFish,
        uint256 perClanIron,
        uint256 perClanGold,
        uint256 burnedWood,
        uint256 burnedWheat,
        uint256 burnedFish,
        uint256 burnedIron,
        uint256 burnedGold
    );

    function emitBanditStateChanged(
        uint32 banditId,
        BanditState oldState,
        BanditState newState,
        uint8 region,
        uint64 atTick
    ) internal {
        emit BanditStateChanged(banditId, oldState, newState, region, atTick);
    }

    function emitBanditEscaped(uint32 banditId, uint64 atTick) internal {
        emit BanditEscaped(banditId, atTick);
    }

    function emitLootDistributed(
        uint32 banditId,
        uint32[] memory clanIdsRewarded,
        uint256 perClanWood,
        uint256 perClanWheat,
        uint256 perClanFish,
        uint256 perClanIron,
        uint256 perClanGold,
        uint256 burnedWood,
        uint256 burnedWheat,
        uint256 burnedFish,
        uint256 burnedIron,
        uint256 burnedGold
    ) internal {
        emit LootDistributed(
            banditId,
            clanIdsRewarded,
            perClanWood,
            perClanWheat,
            perClanFish,
            perClanIron,
            perClanGold,
            burnedWood,
            burnedWheat,
            burnedFish,
            burnedIron,
            burnedGold
        );
    }
}
