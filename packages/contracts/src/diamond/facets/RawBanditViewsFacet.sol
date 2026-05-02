// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {BanditState, BanditTroop, ClanWorldConstants} from "../../IClanWorld.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract RawBanditViewsFacet {
    function getBandit(uint32 banditId) public view returns (BanditTroop memory) {
        BanditTroop memory bandit = LibStorage.appStorage().bandits[banditId];
        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state == BanditState.None) {
            return BanditTroop({
                id: 0,
                region: 0,
                state: BanditState.None,
                targetClanId: 0,
                tickEnteredState: 0,
                strength: 0,
                tier: 0,
                attackAttemptsMade: 0,
                carryWood: 0,
                carryIron: 0,
                carryWheat: 0,
                carryFish: 0,
                carryGold: 0
            });
        }
        return bandit;
    }

    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory) {
        return getBandit(banditId);
    }

    function getBanditsInRegion(uint8 region) external view returns (uint32[] memory) {
        return LibStorage.appStorage().banditsByRegion[region];
    }
}
