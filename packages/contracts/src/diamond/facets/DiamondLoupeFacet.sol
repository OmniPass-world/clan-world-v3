// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {IDiamondLoupe} from "../IDiamondLoupe.sol";
import {LibDiamond} from "../lib/LibDiamond.sol";

contract DiamondLoupeFacet is IDiamondLoupe {
    function facets() external view override returns (Facet[] memory facets_) {
        LibDiamond.DiamondStorage storage ds = LibDiamond.diamondStorage();
        uint256 facetCount = ds.facetAddresses.length;
        facets_ = new Facet[](facetCount);

        for (uint256 i = 0; i < facetCount; i++) {
            address facetAddress_ = ds.facetAddresses[i];
            facets_[i].facetAddress = facetAddress_;
            facets_[i].functionSelectors = ds.facetFunctionSelectors[facetAddress_].functionSelectors;
        }
    }

    function facetFunctionSelectors(address facet) external view override returns (bytes4[] memory selectors) {
        selectors = LibDiamond.diamondStorage().facetFunctionSelectors[facet].functionSelectors;
    }

    function facetAddresses() external view override returns (address[] memory facetAddresses_) {
        facetAddresses_ = LibDiamond.diamondStorage().facetAddresses;
    }

    function facetAddress(bytes4 selector) external view override returns (address facet) {
        facet = LibDiamond.diamondStorage().selectorToFacetAndPosition[selector].facetAddress;
    }
}
