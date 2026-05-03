// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {LibDiamond} from "../../src/diamond/lib/LibDiamond.sol";
import {LibStorage} from "../../src/diamond/lib/LibStorage.sol";

contract StorageSlotProbe {
    function appStorageSlot() external pure returns (bytes32 slot) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        assembly {
            slot := s.slot
        }
    }

    function diamondStorageSlot() external pure returns (bytes32 slot) {
        LibDiamond.DiamondStorage storage ds = LibDiamond.diamondStorage();
        assembly {
            slot := ds.slot
        }
    }
}

contract StorageLayoutGuardTest is Test {
    function testDiamondAndAppStorageSlotsArePinned() public {
        StorageSlotProbe probe = new StorageSlotProbe();

        bytes32 appSlot = probe.appStorageSlot();
        bytes32 diamondSlot = probe.diamondStorageSlot();

        assertEq(appSlot, keccak256("clan.world.app.storage.v1"), "app storage slot");
        assertEq(diamondSlot, keccak256("clan.world.diamond.storage.v1"), "diamond storage slot");
        assertTrue(appSlot != diamondSlot, "storage slots must not collide");
    }
}
