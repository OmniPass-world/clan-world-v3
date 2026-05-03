// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ResourceType, ScheduledMarketAction, TreasuryState} from "../../IClanWorld.sol";
import {StubPool} from "../../StubPool.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract RawTreasuryViewsFacet {
    function getTreasuryState() external view returns (TreasuryState memory) {
        return LibStorage.appStorage().treasury;
    }

    function getResourceToken(uint8 resourceType) external view returns (address) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        if (resourceType == uint8(ResourceType.Wood)) return s.treasury.woodToken;
        if (resourceType == uint8(ResourceType.Iron)) return s.treasury.ironToken;
        if (resourceType == uint8(ResourceType.Wheat)) return s.treasury.wheatToken;
        if (resourceType == uint8(ResourceType.Fish)) return s.treasury.fishToken;
        revert("ClanWorld: invalid resource");
    }

    function getPool(uint8 resourceType) external view returns (address) {
        return _poolForResourceType(resourceType);
    }

    function getPrice(uint8 resourceType, uint256 amountIn) external view returns (uint256 amountOut) {
        amountOut = StubPool(_poolForResourceType(resourceType)).getAmountOutForExactIn(amountIn);
    }

    function getScheduledMarketActionsForTick(uint64 tick) external view returns (ScheduledMarketAction[] memory) {
        return LibStorage.appStorage().scheduledMarketActions[tick];
    }

    function _poolForResourceType(uint8 resourceType) private view returns (address) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        if (resourceType == uint8(ResourceType.Wood)) return s.treasury.woodGoldPool;
        if (resourceType == uint8(ResourceType.Iron)) return s.treasury.ironGoldPool;
        if (resourceType == uint8(ResourceType.Wheat)) return s.treasury.wheatGoldPool;
        if (resourceType == uint8(ResourceType.Fish)) return s.treasury.fishGoldPool;
        revert("ClanWorld: invalid resource");
    }
}
