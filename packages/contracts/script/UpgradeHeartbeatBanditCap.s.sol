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

        // Explicit `.selector` references for each REPLACE/ADD entry —
        // resilient to selector reordering in DiamondSelectors. The previous
        // version index-split the 8-entry heartbeatConfigSelectors() array
        // at position 6 (0..5 → REPLACE, 6..7 → ADD); if any selector was
        // re-ordered inside DiamondSelectors.heartbeatConfigSelectors(),
        // the script would silently mis-classify which to REPLACE vs ADD
        // (super-swarm v2.6.0 MED from codex 5.5 + gemini).
        //
        // The 6 REPLACE selectors are the pre-existing on-chain methods
        // (probed 2026-05-11, see header notes). The 2 ADD selectors are
        // the new bandit-tier cap surface.
        bytes4[] memory replaceConfig = new bytes4[](6);
        replaceConfig[0] = HeartbeatConfigFacet.heartbeatIntervalSeconds.selector;
        replaceConfig[1] = HeartbeatConfigFacet.setHeartbeatIntervalSeconds.selector;
        replaceConfig[2] = HeartbeatConfigFacet.clansmanCooldownSeconds.selector;
        replaceConfig[3] = HeartbeatConfigFacet.setClansmanCooldownSeconds.selector;
        replaceConfig[4] = HeartbeatConfigFacet.banditSpawnTriggered.selector;
        replaceConfig[5] = HeartbeatConfigFacet.triggerBanditSpawn.selector;

        bytes4[] memory addConfig = new bytes4[](2);
        addConfig[0] = HeartbeatConfigFacet.maxBanditTier.selector;
        addConfig[1] = HeartbeatConfigFacet.setMaxBanditTier.selector;

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
