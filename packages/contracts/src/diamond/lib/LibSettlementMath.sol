// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {WithdrawResourcesData} from "../../IClanWorld.sol";

library LibSettlementMath {
    function addTicksClamped(uint64 tick, uint64 delta) internal pure returns (uint64) {
        if (type(uint64).max - tick < delta) return type(uint64).max;
        return tick + delta;
    }

    function hasWithdrawRequest(WithdrawResourcesData memory req) internal pure returns (bool) {
        return req.wood > 0 || req.iron > 0 || req.wheat > 0 || req.fish > 0;
    }

    function remainingCapacity(uint256 carried, uint256 cap) internal pure returns (uint256) {
        if (carried >= cap) return 0;
        return cap - carried;
    }

    function spendableAfterReleasing(uint256 vault, uint256 reserved, uint256 released)
        internal
        pure
        returns (uint256)
    {
        uint256 adjustedReserved = subtractHeld(reserved, released);
        if (vault <= adjustedReserved) return 0;
        return vault - adjustedReserved;
    }

    function subtractHeld(uint256 held, uint256 amount) internal pure returns (uint256) {
        return held > amount ? held - amount : 0;
    }

    function min(uint256 a, uint256 b) internal pure returns (uint256) {
        return a < b ? a : b;
    }
}
