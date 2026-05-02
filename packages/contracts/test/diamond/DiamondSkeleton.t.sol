// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {Diamond} from "../../src/diamond/Diamond.sol";
import {IDiamondCut} from "../../src/diamond/IDiamondCut.sol";
import {IDiamondLoupe} from "../../src/diamond/IDiamondLoupe.sol";
import {DiamondCutFacet} from "../../src/diamond/facets/DiamondCutFacet.sol";
import {DiamondLoupeFacet} from "../../src/diamond/facets/DiamondLoupeFacet.sol";

contract PingFacet {
    function ping() external pure returns (uint256) {
        return 42;
    }
}

interface IPing {
    function ping() external view returns (uint256);
}

contract DiamondSkeletonTest is Test {
    Diamond diamond;
    DiamondCutFacet cutFacet;

    function setUp() public {
        cutFacet = new DiamondCutFacet();
        diamond = new Diamond(address(this), address(cutFacet));
    }

    function testOwnerCanAddAndRouteFacetSelector() public {
        PingFacet pingFacet = new PingFacet();
        bytes4[] memory selectors = new bytes4[](1);
        selectors[0] = PingFacet.ping.selector;

        IDiamondCut.FacetCut[] memory cut = new IDiamondCut.FacetCut[](1);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: address(pingFacet),
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: selectors
        });

        IDiamondCut(address(diamond)).diamondCut(cut, address(0), "");

        assertEq(IPing(address(diamond)).ping(), 42);
    }

    function testLoupeReportsAddedSelector() public {
        DiamondLoupeFacet loupeFacet = new DiamondLoupeFacet();
        bytes4[] memory selectors = new bytes4[](4);
        selectors[0] = IDiamondLoupe.facets.selector;
        selectors[1] = IDiamondLoupe.facetFunctionSelectors.selector;
        selectors[2] = IDiamondLoupe.facetAddresses.selector;
        selectors[3] = IDiamondLoupe.facetAddress.selector;

        IDiamondCut.FacetCut[] memory cut = new IDiamondCut.FacetCut[](1);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: address(loupeFacet),
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: selectors
        });

        IDiamondCut(address(diamond)).diamondCut(cut, address(0), "");

        assertEq(IDiamondLoupe(address(diamond)).facetAddress(IDiamondLoupe.facets.selector), address(loupeFacet));
        assertEq(IDiamondLoupe(address(diamond)).facetAddresses().length, 2);
    }

    function testOnlyOwnerCanDiamondCut() public {
        PingFacet pingFacet = new PingFacet();
        bytes4[] memory selectors = new bytes4[](1);
        selectors[0] = PingFacet.ping.selector;

        IDiamondCut.FacetCut[] memory cut = new IDiamondCut.FacetCut[](1);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: address(pingFacet),
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: selectors
        });

        vm.prank(address(0xBEEF));
        vm.expectRevert();
        IDiamondCut(address(diamond)).diamondCut(cut, address(0), "");
    }
}
