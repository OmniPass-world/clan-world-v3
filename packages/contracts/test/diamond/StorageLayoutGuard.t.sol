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

    function writeWorldTailSentinels() external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        s.world.activeBanditId = 0xaabbccdd;
        s.world.nextCommitSequence = 0x1122334455667788;
        s.world.worldPaused = true;
        s.world.pausedAtTs = 0x99aabbccddeeff00;
        s.treasury.treasuryOwner = address(0x1234);
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

    function testWorldPauseFieldsPackIntoExistingWorldTailSlot() public {
        StorageSlotProbe probe = new StorageSlotProbe();
        probe.writeWorldTailSentinels();

        uint256 appSlot = uint256(probe.appStorageSlot());
        // StoredWorldState starts at AppStorage slot 0. Slots 0-5 hold the
        // earlier world fields through `currentTickSeed`; slot 6 packs
        // `activeBanditId`, `nextCommitSequence`, `worldPaused`, and
        // `pausedAtTs`. Treasury is the next AppStorage field, so it must still
        // begin at appSlot + 7 after appending the pause fields.
        uint256 worldTailSlot = uint256(appSlot) + 6;
        uint256 expectedTail = uint256(0xaabbccdd) | (uint256(0x1122334455667788) << 32) | (uint256(1) << 96)
            | (uint256(0x99aabbccddeeff00) << 104);

        assertEq(uint256(vm.load(address(probe), bytes32(worldTailSlot))), expectedTail, "world tail packed slot");
        assertEq(
            address(uint160(uint256(vm.load(address(probe), bytes32(appSlot + 7))))),
            address(0x1234),
            "treasury still starts after world"
        );
    }
}
