// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {
    ActionType,
    Clan,
    ClanOrder,
    ClanState,
    ClanWorldConstants,
    Clansman,
    StatusCode,
    WithdrawResourcesData
} from "../../IClanWorld.sol";
import {LibOrderMarket} from "./LibOrderMarket.sol";
import {LibOrderUpgrades} from "./LibOrderUpgrades.sol";
import {LibSeason} from "./LibSeason.sol";
import {LibSettlementMath} from "./LibSettlementMath.sol";
import {LibStorage} from "./LibStorage.sol";

library LibSubmitOrderValidation {
    function validateAction(
        LibStorage.AppStorage storage s,
        Clan storage clan,
        Clansman storage cs,
        ClanOrder calldata order,
        uint8 gotoRegion
    ) public view returns (StatusCode) {
        ActionType action = order.action;
        if (action == ActionType.None) return StatusCode.ERR_INVALID_ACTION;

        if (action == ActionType.DepositResources && gotoRegion != clan.baseRegion) {
            return StatusCode.ERR_NOT_AT_HOMEBASE;
        }
        if (action == ActionType.WithdrawResources) {
            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
            WithdrawResourcesData memory req = order.withdrawResources;
            if (!LibSettlementMath.hasWithdrawRequest(req)) return StatusCode.ERR_EMPTY_CARGO;
            LibOrderUpgrades.HeldUpgradeResources memory released =
                LibOrderUpgrades.releasedUpgradeResources(s, clan.clanId, cs.clansmanId);
            if (!LibOrderUpgrades.hasSpendableForWithdraw(s, clan.clanId, clan, req, released)) {
                return StatusCode.ERR_MISSING_RESOURCES;
            }
            if (!LibOrderUpgrades.hasCarryCapacityForWithdraw(cs, req)) return StatusCode.ERR_CARRY_FULL;
        }

        if (
            action == ActionType.UpgradeWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
        ) {
            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
        }
        if (action == ActionType.UpgradeWall) return LibOrderUpgrades.validateUpgradeWallOrder(s, clan, cs.clansmanId);
        if (action == ActionType.UpgradeBase) return LibOrderUpgrades.validateUpgradeBaseOrder(s, clan, cs.clansmanId);
        if (action == ActionType.UpgradeMonument) {
            return LibOrderUpgrades.validateUpgradeMonumentOrder(s, clan, cs.clansmanId);
        }

        if (action == ActionType.ChopWood && gotoRegion != ClanWorldConstants.REGION_FOREST) {
            return StatusCode.ERR_INVALID_REGION;
        }
        if (action == ActionType.MineIron && gotoRegion != ClanWorldConstants.REGION_MOUNTAINS) {
            return StatusCode.ERR_INVALID_REGION;
        }
        if (
            action == ActionType.FishDocks && gotoRegion != ClanWorldConstants.REGION_WEST_DOCKS
                && gotoRegion != ClanWorldConstants.REGION_EAST_DOCKS
        ) return StatusCode.ERR_INVALID_REGION;
        if (action == ActionType.FishDeepSea && gotoRegion != ClanWorldConstants.REGION_DEEP_SEA) {
            return StatusCode.ERR_INVALID_REGION;
        }
        if (action == ActionType.HarvestWheat) {
            if (
                gotoRegion != ClanWorldConstants.REGION_WEST_FARMS && gotoRegion != ClanWorldConstants.REGION_EAST_FARMS
            ) return StatusCode.ERR_INVALID_REGION;
            if (LibSeason.isWinterActiveAt(s.world.currentTick)) return StatusCode.ERR_WINTER_LOCKED;
        }
        if (action == ActionType.DefendBase) return validateDefendBaseOrder(s, clan, order, gotoRegion);
        if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
            return LibOrderMarket.validateMarketOrder(s, cs, order, gotoRegion);
        }
        return StatusCode.OK;
    }

    function validateDefendBaseOrder(
        LibStorage.AppStorage storage s,
        Clan storage clan,
        ClanOrder calldata order,
        uint8 gotoRegion
    ) public view returns (StatusCode) {
        uint32 targetClanId = order.targetClanId == 0 ? clan.clanId : order.targetClanId;
        Clan storage targetClan = s.clans[targetClanId];
        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
            return StatusCode.ERR_INVALID_TARGET;
        }
        if (gotoRegion != targetClan.baseRegion) return StatusCode.ERR_INVALID_REGION;
        return StatusCode.OK;
    }
}
