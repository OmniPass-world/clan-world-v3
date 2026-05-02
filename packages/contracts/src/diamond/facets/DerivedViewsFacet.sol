// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

/// @notice Future diamond home for simulation-heavy derived reads.
/// @dev Keep settlement simulation here once AppStorage migration is live. Pure
///      scoring helpers belong in LibScoring so the lens and facet share exact
///      score/loot semantics without copying tiny rule code.
contract DerivedViewsFacet {
    function derivedViewsFacetVersion() external pure returns (bytes4) {
        return bytes4(keccak256("DerivedViewsFacet.v1"));
    }
}
