// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {LibDiamond} from "../lib/LibDiamond.sol";

contract OwnershipFacet {
    function owner() external view returns (address) {
        return LibDiamond.diamondStorage().contractOwner;
    }

    function transferOwnership(address newOwner) external {
        require(newOwner != address(0), "Diamond: owner cannot be zero");
        LibDiamond.enforceIsContractOwner();
        LibDiamond.setContractOwner(newOwner);
    }
}
