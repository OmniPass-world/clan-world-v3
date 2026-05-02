// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {IDiamondCut} from "../IDiamondCut.sol";
import {LibDiamond} from "../lib/LibDiamond.sol";

contract DiamondCutFacet is IDiamondCut {
    function diamondCut(FacetCut[] calldata diamondCut_, address init, bytes calldata data) external override {
        LibDiamond.enforceIsContractOwner();
        LibDiamond.diamondCut(diamondCut_, init, data);
    }
}
