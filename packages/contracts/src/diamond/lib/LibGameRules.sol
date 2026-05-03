// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ActionType} from "../../IClanWorld.sol";
import {LibStorage} from "./LibStorage.sol";

library LibGameRules {
    uint64 internal constant DEPOSIT_DURATION_TICKS = 1;
    uint64 internal constant BUILDING_DURATION_TICKS = 1;
    uint8 internal constant MAX_CLANS = 12;
    uint8 internal constant MONUMENT_MAX_LEVEL = 10;
    uint64 internal constant MAX_LAZY_SETTLE_BACKLOG = 200;

    function requireNoPendingSeasonFinalization(LibStorage.AppStorage storage s) internal view {
        require(
            !(s.world.currentTick >= s.world.seasonEndTick && !s.world.seasonFinalized),
            "ClanWorld: finalize season first"
        );
    }

    function wallUpgradeCost(uint8 currentLevel) internal pure returns (uint256 wood, uint256 iron) {
        if (currentLevel == 0) return (20e18, 0);
        if (currentLevel == 1) return (35e18, 0);
        if (currentLevel == 2) return (30e18, 5e18);
        if (currentLevel == 3) return (40e18, 10e18);
        if (currentLevel == 4) return (50e18, 15e18);
        return (0, 0);
    }

    function baseUpgradeCost(uint8 currentLevel) internal pure returns (uint256 wood, uint256 iron, uint256 wheat) {
        if (currentLevel == 1) return (40e18, 0, 20e18);
        if (currentLevel == 2) return (60e18, 5e18, 30e18);
        if (currentLevel == 3) return (80e18, 10e18, 40e18);
        if (currentLevel == 4) return (100e18, 15e18, 50e18);
        return (0, 0, 0);
    }

    function monumentUpgradeCost(uint8 currentLevel)
        internal
        pure
        returns (uint256 wood, uint256 iron, uint256 wheat, uint256 blueprint)
    {
        if (currentLevel == 0) return (30e18, 0, 20e18, 0);
        if (currentLevel == 1) return (50e18, 0, 30e18, 0);
        if (currentLevel == 2) return (70e18, 5e18, 40e18, 0);
        if (currentLevel == 3) return (90e18, 10e18, 50e18, 0);
        if (currentLevel == 4) return (120e18, 15e18, 60e18, 0);
        if (currentLevel == 5) return (150e18, 20e18, 80e18, 0);
        if (currentLevel >= 6 && currentLevel < MONUMENT_MAX_LEVEL) return (200e18, 25e18, 100e18, 1e18);
        return (0, 0, 0, 0);
    }

    function actionDuration(ActionType action) internal pure returns (uint64) {
        if (
            action == ActionType.ChopWood || action == ActionType.MineIron || action == ActionType.FishDocks
                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
        ) {
            return 4;
        }

        if (action == ActionType.DepositResources || action == ActionType.WithdrawResources) {
            return DEPOSIT_DURATION_TICKS;
        }

        if (
            action == ActionType.UpgradeWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
        ) {
            return BUILDING_DURATION_TICKS;
        }

        if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
            return 1;
        }

        return 0;
    }
}
