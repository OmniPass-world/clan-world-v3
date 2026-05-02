// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {
    IClanWorld,
    DerivedClanState,
    DerivedClansmanState,
    WorldSnapshot,
    ClanFullView,
    MarketState,
    ActiveBanditView,
    RegionOccupant
} from "./IClanWorld.sol";

/// @notice Stateless convenience-read surface for UI, indexers, and agents.
/// @dev The lens is intentionally not proxied. Upgrade by deploying a new lens
///      and updating off-chain config.
interface IClanWorldLens {
    function world() external view returns (IClanWorld);

    function getDerivedClanState(uint32 clanId) external view returns (DerivedClanState memory);

    function getDerivedClansmanState(uint32 clansmanId) external view returns (DerivedClansmanState memory);

    function getBanditTargetPreview(uint32 banditId) external view returns (uint32 previewClanId);

    function quoteLootValueRaw(uint32 clanId) external view returns (uint256 lootValue);

    function quoteLootValueSettled(uint32 clanId) external view returns (uint256 lootValue);

    function getClanScore(uint32 clanId)
        external
        view
        returns (uint256 score, uint64 monumentReachedAtTick, uint8 monumentLevel);

    function getRankings() external view returns (uint32[] memory clanIdsRanked, uint256[] memory scores);

    function getWorldSnapshot() external view returns (WorldSnapshot memory);

    function getClanFullView(uint32 clanId) external view returns (ClanFullView memory);

    function getMarketState() external view returns (MarketState memory);

    function getActiveBanditView() external view returns (ActiveBanditView memory);

    function getRegionPopulation(uint8 region) external view returns (RegionOccupant[] memory);
}
