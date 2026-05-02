// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {IDiamondCut} from "../IDiamondCut.sol";

library LibDiamond {
    bytes32 internal constant DIAMOND_STORAGE_SLOT = keccak256("clan.world.diamond.storage.v1");

    struct FacetAddressAndPosition {
        address facetAddress;
        uint96 selectorPosition;
    }

    struct FacetFunctionSelectors {
        bytes4[] functionSelectors;
        uint256 facetAddressPosition;
    }

    struct DiamondStorage {
        mapping(bytes4 => FacetAddressAndPosition) selectorToFacetAndPosition;
        mapping(address => FacetFunctionSelectors) facetFunctionSelectors;
        address[] facetAddresses;
        mapping(bytes4 => bool) supportedInterfaces;
        address contractOwner;
    }

    event OwnershipTransferred(address indexed previousOwner, address indexed newOwner);

    error DiamondCutFacetCannotBeZero();
    error DiamondCutSelectorCannotBeZero();
    error DiamondCutSelectorAlreadyExists(bytes4 selector);
    error DiamondCutSelectorDoesNotExist(bytes4 selector);
    error DiamondCutReplaceFacetMustChange(bytes4 selector);
    error DiamondCutRemoveFacetAddressMustBeZero();
    error DiamondCutInvalidAction();
    error DiamondInitFailed(address init, bytes data);
    error DiamondNotOwner(address caller);

    function diamondStorage() internal pure returns (DiamondStorage storage ds) {
        bytes32 slot = DIAMOND_STORAGE_SLOT;
        assembly {
            ds.slot := slot
        }
    }

    function setContractOwner(address newOwner) internal {
        DiamondStorage storage ds = diamondStorage();
        address previousOwner = ds.contractOwner;
        ds.contractOwner = newOwner;
        emit OwnershipTransferred(previousOwner, newOwner);
    }

    function enforceIsContractOwner() internal view {
        address owner = diamondStorage().contractOwner;
        if (msg.sender != owner) {
            revert DiamondNotOwner(msg.sender);
        }
    }

    function diamondCut(IDiamondCut.FacetCut[] memory cuts, address init, bytes memory data) internal {
        for (uint256 i = 0; i < cuts.length; i++) {
            IDiamondCut.FacetCutAction action = cuts[i].action;
            if (action == IDiamondCut.FacetCutAction.Add) {
                addFunctions(cuts[i].facetAddress, cuts[i].functionSelectors);
            } else if (action == IDiamondCut.FacetCutAction.Replace) {
                replaceFunctions(cuts[i].facetAddress, cuts[i].functionSelectors);
            } else if (action == IDiamondCut.FacetCutAction.Remove) {
                removeFunctions(cuts[i].facetAddress, cuts[i].functionSelectors);
            } else {
                revert DiamondCutInvalidAction();
            }
        }

        emit IDiamondCut.DiamondCut(cuts, init, data);
        initializeDiamondCut(init, data);
    }

    function addFunctions(address facetAddress, bytes4[] memory selectors) internal {
        if (facetAddress == address(0)) {
            revert DiamondCutFacetCannotBeZero();
        }

        DiamondStorage storage ds = diamondStorage();
        uint96 selectorPosition = uint96(ds.facetFunctionSelectors[facetAddress].functionSelectors.length);
        if (selectorPosition == 0) {
            addFacet(ds, facetAddress);
        }

        for (uint256 i = 0; i < selectors.length; i++) {
            bytes4 selector = selectors[i];
            if (selector == bytes4(0)) {
                revert DiamondCutSelectorCannotBeZero();
            }
            if (ds.selectorToFacetAndPosition[selector].facetAddress != address(0)) {
                revert DiamondCutSelectorAlreadyExists(selector);
            }
            addFunction(ds, selector, selectorPosition, facetAddress);
            selectorPosition++;
        }
    }

    function replaceFunctions(address facetAddress, bytes4[] memory selectors) internal {
        if (facetAddress == address(0)) {
            revert DiamondCutFacetCannotBeZero();
        }

        DiamondStorage storage ds = diamondStorage();
        uint96 selectorPosition = uint96(ds.facetFunctionSelectors[facetAddress].functionSelectors.length);
        if (selectorPosition == 0) {
            addFacet(ds, facetAddress);
        }

        for (uint256 i = 0; i < selectors.length; i++) {
            bytes4 selector = selectors[i];
            address oldFacetAddress = ds.selectorToFacetAndPosition[selector].facetAddress;
            if (oldFacetAddress == address(0)) {
                revert DiamondCutSelectorDoesNotExist(selector);
            }
            if (oldFacetAddress == facetAddress) {
                revert DiamondCutReplaceFacetMustChange(selector);
            }
            removeFunction(ds, oldFacetAddress, selector);
            addFunction(ds, selector, selectorPosition, facetAddress);
            selectorPosition++;
        }
    }

    function removeFunctions(address facetAddress, bytes4[] memory selectors) internal {
        if (facetAddress != address(0)) {
            revert DiamondCutRemoveFacetAddressMustBeZero();
        }

        DiamondStorage storage ds = diamondStorage();
        for (uint256 i = 0; i < selectors.length; i++) {
            bytes4 selector = selectors[i];
            address oldFacetAddress = ds.selectorToFacetAndPosition[selector].facetAddress;
            if (oldFacetAddress == address(0)) {
                revert DiamondCutSelectorDoesNotExist(selector);
            }
            removeFunction(ds, oldFacetAddress, selector);
        }
    }

    function addFacet(DiamondStorage storage ds, address facetAddress) private {
        ds.facetFunctionSelectors[facetAddress].facetAddressPosition = ds.facetAddresses.length;
        ds.facetAddresses.push(facetAddress);
    }

    function addFunction(
        DiamondStorage storage ds,
        bytes4 selector,
        uint96 selectorPosition,
        address facetAddress
    ) private {
        ds.selectorToFacetAndPosition[selector].selectorPosition = selectorPosition;
        ds.facetFunctionSelectors[facetAddress].functionSelectors.push(selector);
        ds.selectorToFacetAndPosition[selector].facetAddress = facetAddress;
    }

    function removeFunction(DiamondStorage storage ds, address facetAddress, bytes4 selector) private {
        FacetFunctionSelectors storage facet = ds.facetFunctionSelectors[facetAddress];
        uint256 selectorPosition = ds.selectorToFacetAndPosition[selector].selectorPosition;
        uint256 lastSelectorPosition = facet.functionSelectors.length - 1;

        if (selectorPosition != lastSelectorPosition) {
            bytes4 lastSelector = facet.functionSelectors[lastSelectorPosition];
            facet.functionSelectors[selectorPosition] = lastSelector;
            ds.selectorToFacetAndPosition[lastSelector].selectorPosition = uint96(selectorPosition);
        }

        facet.functionSelectors.pop();
        delete ds.selectorToFacetAndPosition[selector];

        if (facet.functionSelectors.length == 0) {
            uint256 lastFacetAddressPosition = ds.facetAddresses.length - 1;
            uint256 facetAddressPosition = facet.facetAddressPosition;
            if (facetAddressPosition != lastFacetAddressPosition) {
                address lastFacetAddress = ds.facetAddresses[lastFacetAddressPosition];
                ds.facetAddresses[facetAddressPosition] = lastFacetAddress;
                ds.facetFunctionSelectors[lastFacetAddress].facetAddressPosition = facetAddressPosition;
            }
            ds.facetAddresses.pop();
            delete ds.facetFunctionSelectors[facetAddress].facetAddressPosition;
        }
    }

    function initializeDiamondCut(address init, bytes memory data) private {
        if (init == address(0)) {
            return;
        }

        (bool success,) = init.delegatecall(data);
        if (!success) {
            revert DiamondInitFailed(init, data);
        }
    }
}
