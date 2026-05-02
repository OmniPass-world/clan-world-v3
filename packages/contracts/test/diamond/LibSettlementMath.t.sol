// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {WithdrawResourcesData} from "../../src/IClanWorld.sol";
import {LibSettlementMath} from "../../src/diamond/lib/LibSettlementMath.sol";

contract LibSettlementMathTest is Test {
    function testAddTicksClamped() public pure {
        assertEq(LibSettlementMath.addTicksClamped(10, 5), 15);
        assertEq(LibSettlementMath.addTicksClamped(type(uint64).max - 1, 2), type(uint64).max);
    }

    function testSpendableAfterReleasing() public pure {
        assertEq(LibSettlementMath.spendableAfterReleasing(100, 40, 10), 70);
        assertEq(LibSettlementMath.spendableAfterReleasing(100, 40, 50), 100);
        assertEq(LibSettlementMath.spendableAfterReleasing(30, 40, 0), 0);
    }

    function testWithdrawRequestAndCapacityHelpers() public pure {
        WithdrawResourcesData memory empty;
        assertFalse(LibSettlementMath.hasWithdrawRequest(empty));

        WithdrawResourcesData memory req = WithdrawResourcesData({wood: 1, iron: 0, wheat: 0, fish: 0});
        assertTrue(LibSettlementMath.hasWithdrawRequest(req));
        assertEq(LibSettlementMath.remainingCapacity(3, 10), 7);
        assertEq(LibSettlementMath.remainingCapacity(10, 10), 0);
        assertEq(LibSettlementMath.remainingCapacity(11, 10), 0);
    }

    function testSubtractHeldAndMin() public pure {
        assertEq(LibSettlementMath.subtractHeld(10, 3), 7);
        assertEq(LibSettlementMath.subtractHeld(10, 11), 0);
        assertEq(LibSettlementMath.min(2, 5), 2);
        assertEq(LibSettlementMath.min(9, 4), 4);
    }
}
