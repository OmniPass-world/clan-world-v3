// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {IDiamondCut} from "./IDiamondCut.sol";
import {LibDiamond} from "./lib/LibDiamond.sol";

contract Diamond {
    constructor(address contractOwner, address diamondCutFacet) payable {
        LibDiamond.setContractOwner(contractOwner);

        bytes4[] memory selectors = new bytes4[](1);
        selectors[0] = IDiamondCut.diamondCut.selector;

        IDiamondCut.FacetCut[] memory cut = new IDiamondCut.FacetCut[](1);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: diamondCutFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: selectors
        });

        LibDiamond.diamondCut(cut, address(0), "");
    }

    receive() external payable {}

    fallback() external payable {
        LibDiamond.DiamondStorage storage ds = LibDiamond.diamondStorage();
        address facet = ds.selectorToFacetAndPosition[msg.sig].facetAddress;
        require(facet != address(0), "Diamond: function does not exist");

        assembly {
            calldatacopy(0, 0, calldatasize())
            let result := delegatecall(gas(), facet, 0, calldatasize(), 0, 0)
            returndatacopy(0, 0, returndatasize())
            switch result
            case 0 { revert(0, returndatasize()) }
            default { return(0, returndatasize()) }
        }
    }
}
