// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {DeployDiamond} from "./DeployDiamond.s.sol";

/// @notice Canonical deploy entrypoint. Kept for operator muscle memory, but it
///         now deploys the diamond stack instead of the oversized monolith.
contract Deploy is DeployDiamond {}
