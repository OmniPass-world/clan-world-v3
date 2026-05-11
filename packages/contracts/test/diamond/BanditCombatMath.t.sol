// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {LibBanditCombat} from "../../src/diamond/lib/LibBanditCombat.sol";

contract BanditCombatMathTest is Test {
    function testPerDefenderBanditLootSharePreservesFractionalResourceUnits() public pure {
        uint256 stolenWood = 1e18;
        uint256 defenderDrop = stolenWood / 2;

        assertEq(LibBanditCombat.perDefenderBanditLootShare(defenderDrop, 2), 25e16);
    }
}
