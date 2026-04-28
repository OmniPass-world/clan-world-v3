// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import {Script, console} from "forge-std/Script.sol";
import {IClanWorld} from "../src/IClanWorld.sol";

contract Heartbeat is Script {
    function run() external {
        uint256 pk = vm.envUint("DEPLOYER_PRIVATE_KEY");
        address engine = vm.envAddress("CLAN_WORLD_CONTRACT_ADDRESS");

        vm.startBroadcast(pk);
        IClanWorld(engine).heartbeat();
        vm.stopBroadcast();

        console.log("heartbeat() fired on", engine);
    }
}
