// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {IClanWorldEvents, ResourceType} from "../../IClanWorld.sol";
import {LibBundleTransfer} from "../lib/LibBundleTransfer.sol";
import {LibDirectTransfers} from "../lib/LibDirectTransfers.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract DirectTransfersFacet is IClanWorldEvents {
    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);
        LibDirectTransfers.transferGold(fromClanId, toClanId, amount, msg.sender);
        LibStorage.exitNonReentrant(s);
    }

    function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);
        LibDirectTransfers.transferVaultResource(fromClanId, toClanId, resource, amount, msg.sender);
        LibStorage.exitNonReentrant(s);
    }

    function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);
        LibDirectTransfers.transferBlueprint(fromClanId, toClanId, amount, msg.sender);
        LibStorage.exitNonReentrant(s);
    }

    function transferBundle(
        uint32 fromClanId,
        uint32 toClanId,
        uint256 gold,
        uint256 blueprint,
        uint256 wood,
        uint256 iron,
        uint256 wheat,
        uint256 fish
    ) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);
        LibBundleTransfer.transferBundle(fromClanId, toClanId, gold, blueprint, wood, iron, wheat, fish, msg.sender);
        LibStorage.exitNonReentrant(s);
    }
}
