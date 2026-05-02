// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ClanOrder, OrderResult} from "../../IClanWorld.sol";
import {LibSubmitOrders} from "../lib/LibSubmitOrders.sol";

contract SubmitOrdersFacet {
    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
        external
        returns (OrderResult[] memory results)
    {
        return LibSubmitOrders.submitClanOrders(clanId, orders);
    }
}
