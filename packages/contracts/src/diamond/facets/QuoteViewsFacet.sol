// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {LibScoring} from "../../lib/LibScoring.sol";
import {LibTravel} from "../../lib/LibTravel.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract QuoteViewsFacet {
    function getBanditTargetPreview(uint32 banditId) external view returns (uint32 previewClanId) {
        return LibStorage.appStorage().bandits[banditId].targetClanId;
    }

    function quoteTravel(uint8 srcRegion, uint8 dstRegion) external pure returns (uint8 travelTicks, bytes8 path) {
        return LibTravel.quoteTravel(srcRegion, dstRegion);
    }

    function quoteLootValueRaw(uint32 clanId) external view returns (uint256 lootValue) {
        return LibScoring.lootValue(LibStorage.appStorage().clans[clanId]);
    }
}
