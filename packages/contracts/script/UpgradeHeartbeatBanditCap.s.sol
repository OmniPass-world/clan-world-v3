// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Script, console} from "forge-std/Script.sol";
import {DiamondSelectors} from "./DiamondSelectors.sol";
import {IDiamondCut} from "../src/diamond/IDiamondCut.sol";
import {HeartbeatConfigFacet} from "../src/diamond/facets/HeartbeatConfigFacet.sol";
import {HeartbeatFacet} from "../src/diamond/facets/HeartbeatFacet.sol";

/// @notice Deploys new HeartbeatFacet + HeartbeatConfigFacet (both linked
///         to the new LibBanditSpawning bytecode with the maxBanditTier
///         clamp), then diamondCuts the live diamond to REPLACE the
///         currently-routed Heartbeat selectors and ADD the two new
///         HeartbeatConfigFacet selectors (`maxBanditTier()` /
///         `setMaxBanditTier(uint8)`).
///
/// @dev Required env:
///   - DEPLOYER_PRIVATE_KEY     — diamond owner key
///   - CLAN_WORLD_DIAMOND_ADDRESS (preferred) or CLAN_WORLD_CONTRACT_ADDRESS — the target diamond
///
/// On-chain pre-state (Base Sepolia, captured 2026-05-11):
///   - heartbeat() (0x3defb962)                  → 0xfA742EEF25e6a16506f7bCc40e3710EC5A4B41eE (REPLACE)
///   - heartbeatIntervalSeconds (0x648d6c85)     → 0xdE3Ed840BF083BC288Baa01b80989cfD392C1047 (REPLACE)
///   - setHeartbeatIntervalSeconds (0xa3783d06)  → 0xdE3Ed840BF083BC288Baa01b80989cfD392C1047 (REPLACE)
///   - clansmanCooldownSeconds (0xf91ffcc0)      → 0xdE3Ed840BF083BC288Baa01b80989cfD392C1047 (REPLACE)
///   - setClansmanCooldownSeconds (0x9fcadb74)   → 0xdE3Ed840BF083BC288Baa01b80989cfD392C1047 (REPLACE)
///   - banditSpawnTriggered (0x17fca79b)         → 0xdE3Ed840BF083BC288Baa01b80989cfD392C1047 (REPLACE)
///   - triggerBanditSpawn (0x1f79b9b0)           → 0xdE3Ed840BF083BC288Baa01b80989cfD392C1047 (REPLACE)
///   - maxBanditTier (0x16fd8fc6)                → address(0)                                  (ADD)
///   - setMaxBanditTier (0x90fab46c)             → address(0)                                  (ADD)
contract UpgradeHeartbeatBanditCap is Script {
    function run() external returns (HeartbeatFacet newHeartbeat, HeartbeatConfigFacet newHeartbeatConfig) {
        uint256 deployerPrivateKey = vm.envUint("DEPLOYER_PRIVATE_KEY");
        // Support either env var name (CLAN_WORLD_DIAMOND_ADDRESS or CLAN_WORLD_CONTRACT_ADDRESS).
        address diamond = _diamondAddress();

        // Split the 8-entry heartbeatConfigSelectors() array into:
        //   - first 6 → REPLACE (already routed to old HeartbeatConfigFacet)
        //   - last 2  → ADD     (new in this upgrade: maxBanditTier + setMaxBanditTier)
        bytes4[] memory allConfig = DiamondSelectors.heartbeatConfigSelectors();
        require(allConfig.length == 8, "heartbeatConfigSelectors len drift");

        bytes4[] memory replaceConfig = new bytes4[](6);
        for (uint256 i = 0; i < 6; i++) {
            replaceConfig[i] = allConfig[i];
        }

        bytes4[] memory addConfig = new bytes4[](2);
        addConfig[0] = allConfig[6]; // maxBanditTier()
        addConfig[1] = allConfig[7]; // setMaxBanditTier(uint8)

        vm.startBroadcast(deployerPrivateKey);

        newHeartbeat = new HeartbeatFacet();
        newHeartbeatConfig = new HeartbeatConfigFacet();

        IDiamondCut.FacetCut[] memory cut = new IDiamondCut.FacetCut[](3);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: address(newHeartbeat),
            action: IDiamondCut.FacetCutAction.Replace,
            functionSelectors: DiamondSelectors.heartbeatSelectors()
        });
        cut[1] = IDiamondCut.FacetCut({
            facetAddress: address(newHeartbeatConfig),
            action: IDiamondCut.FacetCutAction.Replace,
            functionSelectors: replaceConfig
        });
        cut[2] = IDiamondCut.FacetCut({
            facetAddress: address(newHeartbeatConfig),
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: addConfig
        });
        IDiamondCut(diamond).diamondCut(cut, address(0), "");

        vm.stopBroadcast();

        console.log("DIAMOND:                       ", diamond);
        console.log("NEW_HEARTBEAT_FACET_ADDRESS:   ", address(newHeartbeat));
        console.log("NEW_HEARTBEAT_CONFIG_FACET:    ", address(newHeartbeatConfig));
    }

    function _diamondAddress() private view returns (address) {
        // Prefer CLAN_WORLD_DIAMOND_ADDRESS (per ops runbook); fall back to
        // CLAN_WORLD_CONTRACT_ADDRESS (the .env.local in this repo).
        try vm.envAddress("CLAN_WORLD_DIAMOND_ADDRESS") returns (address d) {
            return d;
        } catch {
            return vm.envAddress("CLAN_WORLD_CONTRACT_ADDRESS");
        }
    }
}
