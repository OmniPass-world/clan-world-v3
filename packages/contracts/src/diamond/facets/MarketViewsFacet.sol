// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {MarketState, PoolReserves} from "../../IClanWorld.sol";
import {LibStorage} from "../lib/LibStorage.sol";
import {StubPool} from "../../StubPool.sol";

contract MarketViewsFacet {
    function getMarketState() external view returns (MarketState memory) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        return MarketState({
            wood: _poolReserves(s.treasury.woodToken, s.treasury.woodGoldPool),
            wheat: _poolReserves(s.treasury.wheatToken, s.treasury.wheatGoldPool),
            fish: _poolReserves(s.treasury.fishToken, s.treasury.fishGoldPool),
            iron: _poolReserves(s.treasury.ironToken, s.treasury.ironGoldPool),
            currentTick: s.world.currentTick,
            currentTickQueue: s.scheduledMarketActions[s.world.currentTick],
            nextTickQueue: s.scheduledMarketActions[s.world.currentTick + 1]
        });
    }

    function _poolReserves(address resourceToken, address poolAddr) private view returns (PoolReserves memory pr) {
        pr.resourceToken = resourceToken;
        if (poolAddr == address(0) || resourceToken == address(0)) {
            return pr;
        }
        (uint256 rA, uint256 rB) = StubPool(poolAddr).getReserves();
        pr.resourceReserve = rA;
        pr.goldReserve = rB;
        pr.spotPriceGoldPerResource = rA > 0 ? (rB * 1e18) / rA : 0;
    }
}
