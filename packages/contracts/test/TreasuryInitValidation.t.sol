// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {ClanWorld} from "../src/ClanWorld.sol";
import {MinimalERC20} from "../src/MinimalERC20.sol";
import {StubPool} from "../src/StubPool.sol";

contract TreasuryInitValidationTest is Test {
    function _validInit(ClanWorld world) internal returns (address[6] memory tokens, address[4] memory pools) {
        MinimalERC20 woodToken = new MinimalERC20("Wood", "WOOD");
        MinimalERC20 ironToken = new MinimalERC20("Iron", "IRON");
        MinimalERC20 wheatToken = new MinimalERC20("Wheat", "WHEAT");
        MinimalERC20 fishToken = new MinimalERC20("Fish", "FISH");
        MinimalERC20 goldToken = new MinimalERC20("Gold", "GOLD");
        MinimalERC20 blueprintToken = new MinimalERC20("Blueprint", "BPRT");

        tokens = [
            address(woodToken),
            address(ironToken),
            address(wheatToken),
            address(fishToken),
            address(goldToken),
            address(blueprintToken)
        ];
        pools = [
            address(new StubPool(address(woodToken), address(goldToken), address(world))),
            address(new StubPool(address(wheatToken), address(goldToken), address(world))),
            address(new StubPool(address(fishToken), address(goldToken), address(world))),
            address(new StubPool(address(ironToken), address(goldToken), address(world)))
        ];
    }

    function test_initTreasuryRejectsZeroAndDuplicate() public {
        ClanWorld world = new ClanWorld();
        (address[6] memory tokens, address[4] memory pools) = _validInit(world);

        address[6] memory zeroToken = tokens;
        zeroToken[0] = address(0);
        vm.expectRevert(bytes("ClanWorld: zero treasury token"));
        world.initTreasury(zeroToken, pools);

        world = new ClanWorld();
        (tokens, pools) = _validInit(world);
        address[6] memory duplicateToken = tokens;
        duplicateToken[1] = duplicateToken[0];
        vm.expectRevert(bytes("ClanWorld: duplicate treasury token"));
        world.initTreasury(duplicateToken, pools);

        world = new ClanWorld();
        (tokens, pools) = _validInit(world);
        address[4] memory zeroPool = pools;
        zeroPool[0] = address(0);
        vm.expectRevert(bytes("ClanWorld: zero treasury pool"));
        world.initTreasury(tokens, zeroPool);

        world = new ClanWorld();
        (tokens, pools) = _validInit(world);
        address[4] memory duplicatePool = pools;
        duplicatePool[1] = duplicatePool[0];
        vm.expectRevert(bytes("ClanWorld: duplicate treasury pool"));
        world.initTreasury(tokens, duplicatePool);
    }
}
