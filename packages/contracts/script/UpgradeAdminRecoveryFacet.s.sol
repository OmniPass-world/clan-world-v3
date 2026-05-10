// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Script, console} from "forge-std/Script.sol";
import {DiamondSelectors} from "./DiamondSelectors.sol";
import {IDiamondCut} from "../src/diamond/IDiamondCut.sol";
import {AdminRecoveryFacet} from "../src/diamond/facets/AdminRecoveryFacet.sol";

/// @notice Add-only DiamondCut for the owner-only admin recovery selectors.
/// @dev Run with `forge script` first; only use `--broadcast` from an approved ops runbook.
contract UpgradeAdminRecoveryFacet is Script {
    function run() external returns (AdminRecoveryFacet facet) {
        uint256 deployerPrivateKey = vm.envUint("DEPLOYER_PRIVATE_KEY");
        address diamond = vm.envAddress("CLAN_WORLD_DIAMOND_ADDRESS");

        vm.startBroadcast(deployerPrivateKey);

        facet = new AdminRecoveryFacet();
        IDiamondCut.FacetCut[] memory cut = new IDiamondCut.FacetCut[](1);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: address(facet),
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.adminRecoverySelectors()
        });
        IDiamondCut(diamond).diamondCut(cut, address(0), "");

        vm.stopBroadcast();

        console.log("ADMIN_RECOVERY_FACET_ADDRESS: ", address(facet));
    }
}
