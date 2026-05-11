// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {BanditState} from "../../IClanWorld.sol";

library LibBanditEvents {
    event BanditStateChanged(
        uint32 indexed banditId, BanditState oldState, BanditState newState, uint8 region, uint64 atTick
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
}
