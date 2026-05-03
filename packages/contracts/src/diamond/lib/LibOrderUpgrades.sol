// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ActionType, Clan, Clansman, ClanWorldConstants, StatusCode, WithdrawResourcesData} from "../../IClanWorld.sol";
import {LibGameRules} from "./LibGameRules.sol";
import {LibSettlementMath} from "./LibSettlementMath.sol";
import {LibStorage} from "./LibStorage.sol";

library LibOrderUpgrades {
    uint8 private constant WALL_MAX_LEVEL = 5;
    uint8 private constant BASE_MAX_LEVEL = 5;

    struct HeldUpgradeResources {
        uint256 wood;
        uint256 iron;
        uint256 wheat;
        uint256 blueprint;
    }

    function validateUpgradeWallOrder(LibStorage.AppStorage storage s, Clan storage clan, uint32 clansmanId)
        public
        view
        returns (StatusCode)
    {
        uint8 pendingUpgrades = s.pendingWallUpgradesByClan[clan.clanId];
        HeldUpgradeResources memory released = releasedUpgradeResources(s, clan.clanId, clansmanId);
        LibStorage.WallUpgradeReservation storage existing = s.wallUpgradeReservations[clansmanId];
        if (existing.active && existing.clanId == clan.clanId) pendingUpgrades -= 1;
        if (pendingUpgrades > 0) return StatusCode.ERR_INVALID_ACTION;
        uint8 plannedCurrentLevel = clan.wallLevel + pendingUpgrades;
        if (plannedCurrentLevel >= WALL_MAX_LEVEL) return StatusCode.ERR_INVALID_ACTION;
        (uint256 woodCost, uint256 ironCost) = LibGameRules.wallUpgradeCost(plannedCurrentLevel);
        if (
            LibSettlementMath.spendableAfterReleasing(clan.vaultWood, s.reservedWoodByClan[clan.clanId], released.wood)
                    < woodCost
                || LibSettlementMath.spendableAfterReleasing(
                        clan.vaultIron, s.reservedIronByClan[clan.clanId], released.iron
                    ) < ironCost
        ) return StatusCode.ERR_MISSING_RESOURCES;
        return StatusCode.OK;
    }

    function validateUpgradeBaseOrder(LibStorage.AppStorage storage s, Clan storage clan, uint32 clansmanId)
        public
        view
        returns (StatusCode)
    {
        uint8 pendingUpgrades = s.pendingBaseUpgradesByClan[clan.clanId];
        HeldUpgradeResources memory released = releasedUpgradeResources(s, clan.clanId, clansmanId);
        LibStorage.BaseUpgradeReservation storage existing = s.baseUpgradeReservations[clansmanId];
        if (existing.active && existing.clanId == clan.clanId) pendingUpgrades -= 1;
        if (pendingUpgrades > 0) return StatusCode.ERR_INVALID_ACTION;
        uint8 plannedCurrentLevel = clan.baseLevel + pendingUpgrades;
        if (plannedCurrentLevel >= BASE_MAX_LEVEL) return StatusCode.ERR_INVALID_ACTION;
        (uint256 woodCost, uint256 ironCost, uint256 wheatCost) = LibGameRules.baseUpgradeCost(plannedCurrentLevel);
        if (
            LibSettlementMath.spendableAfterReleasing(clan.vaultWood, s.reservedWoodByClan[clan.clanId], released.wood)
                    < woodCost
                || LibSettlementMath.spendableAfterReleasing(
                        clan.vaultIron, s.reservedIronByClan[clan.clanId], released.iron
                    ) < ironCost
                || LibSettlementMath.spendableAfterReleasing(
                        clan.vaultWheat, s.reservedWheatByClan[clan.clanId], released.wheat
                    ) < wheatCost
        ) return StatusCode.ERR_MISSING_RESOURCES;
        return StatusCode.OK;
    }

    function validateUpgradeMonumentOrder(LibStorage.AppStorage storage s, Clan storage clan, uint32 clansmanId)
        public
        view
        returns (StatusCode)
    {
        uint8 pendingUpgrades = s.pendingMonumentUpgradesByClan[clan.clanId];
        HeldUpgradeResources memory released = releasedUpgradeResources(s, clan.clanId, clansmanId);
        LibStorage.MonumentUpgradeReservation storage existing = s.monumentUpgradeReservations[clansmanId];
        if (existing.active && existing.clanId == clan.clanId) pendingUpgrades -= 1;
        if (pendingUpgrades > 0) return StatusCode.ERR_INVALID_ACTION;
        uint8 plannedCurrentLevel = clan.monumentLevel + pendingUpgrades;
        if (plannedCurrentLevel >= LibGameRules.MONUMENT_MAX_LEVEL) return StatusCode.ERR_INVALID_ACTION;
        (uint256 woodCost, uint256 ironCost, uint256 wheatCost, uint256 blueprintCost) =
            LibGameRules.monumentUpgradeCost(plannedCurrentLevel);
        if (
            LibSettlementMath.spendableAfterReleasing(clan.vaultWood, s.reservedWoodByClan[clan.clanId], released.wood)
                    < woodCost
                || LibSettlementMath.spendableAfterReleasing(
                        clan.vaultIron, s.reservedIronByClan[clan.clanId], released.iron
                    ) < ironCost
                || LibSettlementMath.spendableAfterReleasing(
                        clan.vaultWheat, s.reservedWheatByClan[clan.clanId], released.wheat
                    ) < wheatCost
                || LibSettlementMath.spendableAfterReleasing(
                        clan.blueprintBalance, s.reservedBlueprintByClan[clan.clanId], released.blueprint
                    ) < blueprintCost
        ) return StatusCode.ERR_MISSING_RESOURCES;
        return StatusCode.OK;
    }

    function reserveWallUpgrade(
        LibStorage.AppStorage storage s,
        Clan storage clan,
        uint32 clanId,
        uint32 clansmanId,
        uint64 missionNonce
    ) public {
        uint8 plannedCurrentLevel = clan.wallLevel + s.pendingWallUpgradesByClan[clanId];
        (uint256 woodCost, uint256 ironCost) = LibGameRules.wallUpgradeCost(plannedCurrentLevel);
        s.pendingWallUpgradesByClan[clanId] += 1;
        s.reservedWoodByClan[clanId] += woodCost;
        s.reservedIronByClan[clanId] += ironCost;
        s.wallUpgradeReservations[clansmanId] = LibStorage.WallUpgradeReservation({
            active: true,
            clanId: clanId,
            missionNonce: missionNonce,
            fromLevel: plannedCurrentLevel,
            toLevel: plannedCurrentLevel + 1,
            woodCost: woodCost,
            ironCost: ironCost
        });
    }

    function reserveBaseUpgrade(
        LibStorage.AppStorage storage s,
        Clan storage clan,
        uint32 clanId,
        uint32 clansmanId,
        uint64 missionNonce
    ) public {
        uint8 plannedCurrentLevel = clan.baseLevel + s.pendingBaseUpgradesByClan[clanId];
        (uint256 woodCost, uint256 ironCost, uint256 wheatCost) = LibGameRules.baseUpgradeCost(plannedCurrentLevel);
        s.pendingBaseUpgradesByClan[clanId] += 1;
        s.reservedWoodByClan[clanId] += woodCost;
        s.reservedIronByClan[clanId] += ironCost;
        s.reservedWheatByClan[clanId] += wheatCost;
        s.baseUpgradeReservations[clansmanId] = LibStorage.BaseUpgradeReservation({
            active: true,
            clanId: clanId,
            missionNonce: missionNonce,
            fromLevel: plannedCurrentLevel,
            toLevel: plannedCurrentLevel + 1,
            woodCost: woodCost,
            ironCost: ironCost,
            wheatCost: wheatCost
        });
    }

    function reserveMonumentUpgrade(
        LibStorage.AppStorage storage s,
        Clan storage clan,
        uint32 clanId,
        uint32 clansmanId,
        uint64 missionNonce
    ) public {
        uint8 plannedCurrentLevel = clan.monumentLevel + s.pendingMonumentUpgradesByClan[clanId];
        (uint256 woodCost, uint256 ironCost, uint256 wheatCost, uint256 blueprintCost) =
            LibGameRules.monumentUpgradeCost(plannedCurrentLevel);
        s.pendingMonumentUpgradesByClan[clanId] += 1;
        s.reservedWoodByClan[clanId] += woodCost;
        s.reservedIronByClan[clanId] += ironCost;
        s.reservedWheatByClan[clanId] += wheatCost;
        s.reservedBlueprintByClan[clanId] += blueprintCost;
        s.monumentUpgradeReservations[clansmanId] = LibStorage.MonumentUpgradeReservation({
            active: true,
            clanId: clanId,
            missionNonce: missionNonce,
            fromLevel: plannedCurrentLevel,
            toLevel: plannedCurrentLevel + 1,
            woodCost: woodCost,
            ironCost: ironCost,
            wheatCost: wheatCost,
            blueprintCost: blueprintCost
        });
    }

    function refundUpgradeReservation(LibStorage.AppStorage storage s, uint32 clansmanId, ActionType action) public {
        if (action == ActionType.UpgradeWall) clearWallUpgradeReservation(s, clansmanId);
        else if (action == ActionType.UpgradeBase) clearBaseUpgradeReservation(s, clansmanId);
        else if (action == ActionType.UpgradeMonument) clearMonumentUpgradeReservation(s, clansmanId);
    }

    function clearWallUpgradeReservation(LibStorage.AppStorage storage s, uint32 clansmanId) public {
        LibStorage.WallUpgradeReservation storage reservation = s.wallUpgradeReservations[clansmanId];
        if (!reservation.active) return;
        uint32 clanId = reservation.clanId;
        if (s.pendingWallUpgradesByClan[clanId] > 0) s.pendingWallUpgradesByClan[clanId] -= 1;
        s.reservedWoodByClan[clanId] =
            LibSettlementMath.subtractHeld(s.reservedWoodByClan[clanId], reservation.woodCost);
        s.reservedIronByClan[clanId] =
            LibSettlementMath.subtractHeld(s.reservedIronByClan[clanId], reservation.ironCost);
        delete s.wallUpgradeReservations[clansmanId];
    }

    function clearBaseUpgradeReservation(LibStorage.AppStorage storage s, uint32 clansmanId) public {
        LibStorage.BaseUpgradeReservation storage reservation = s.baseUpgradeReservations[clansmanId];
        if (!reservation.active) return;
        uint32 clanId = reservation.clanId;
        if (s.pendingBaseUpgradesByClan[clanId] > 0) s.pendingBaseUpgradesByClan[clanId] -= 1;
        s.reservedWoodByClan[clanId] =
            LibSettlementMath.subtractHeld(s.reservedWoodByClan[clanId], reservation.woodCost);
        s.reservedIronByClan[clanId] =
            LibSettlementMath.subtractHeld(s.reservedIronByClan[clanId], reservation.ironCost);
        s.reservedWheatByClan[clanId] =
            LibSettlementMath.subtractHeld(s.reservedWheatByClan[clanId], reservation.wheatCost);
        delete s.baseUpgradeReservations[clansmanId];
    }

    function clearMonumentUpgradeReservation(LibStorage.AppStorage storage s, uint32 clansmanId) public {
        LibStorage.MonumentUpgradeReservation storage reservation = s.monumentUpgradeReservations[clansmanId];
        if (!reservation.active) return;
        uint32 clanId = reservation.clanId;
        if (s.pendingMonumentUpgradesByClan[clanId] > 0) s.pendingMonumentUpgradesByClan[clanId] -= 1;
        s.reservedWoodByClan[clanId] =
            LibSettlementMath.subtractHeld(s.reservedWoodByClan[clanId], reservation.woodCost);
        s.reservedIronByClan[clanId] =
            LibSettlementMath.subtractHeld(s.reservedIronByClan[clanId], reservation.ironCost);
        s.reservedWheatByClan[clanId] =
            LibSettlementMath.subtractHeld(s.reservedWheatByClan[clanId], reservation.wheatCost);
        s.reservedBlueprintByClan[clanId] =
            LibSettlementMath.subtractHeld(s.reservedBlueprintByClan[clanId], reservation.blueprintCost);
        delete s.monumentUpgradeReservations[clansmanId];
    }

    function releasedUpgradeResources(LibStorage.AppStorage storage s, uint32 clanId, uint32 clansmanId)
        public
        view
        returns (HeldUpgradeResources memory released)
    {
        LibStorage.WallUpgradeReservation storage wall = s.wallUpgradeReservations[clansmanId];
        if (wall.active && wall.clanId == clanId) {
            released.wood += wall.woodCost;
            released.iron += wall.ironCost;
        }
        LibStorage.BaseUpgradeReservation storage base = s.baseUpgradeReservations[clansmanId];
        if (base.active && base.clanId == clanId) {
            released.wood += base.woodCost;
            released.iron += base.ironCost;
            released.wheat += base.wheatCost;
        }
        LibStorage.MonumentUpgradeReservation storage monument = s.monumentUpgradeReservations[clansmanId];
        if (monument.active && monument.clanId == clanId) {
            released.wood += monument.woodCost;
            released.iron += monument.ironCost;
            released.wheat += monument.wheatCost;
            released.blueprint += monument.blueprintCost;
        }
    }

    function hasSpendableForWithdraw(
        LibStorage.AppStorage storage s,
        uint32 clanId,
        Clan storage clan,
        WithdrawResourcesData memory req,
        HeldUpgradeResources memory released
    ) public view returns (bool) {
        if (
            LibSettlementMath.spendableAfterReleasing(clan.vaultWood, s.reservedWoodByClan[clanId], released.wood)
                < req.wood
        ) return false;
        if (
            LibSettlementMath.spendableAfterReleasing(clan.vaultIron, s.reservedIronByClan[clanId], released.iron)
                < req.iron
        ) return false;
        if (
            LibSettlementMath.spendableAfterReleasing(clan.vaultWheat, s.reservedWheatByClan[clanId], released.wheat)
                < req.wheat
        ) return false;
        if (clan.vaultFish < req.fish) return false;
        return true;
    }

    function hasCarryCapacityForWithdraw(Clansman storage cs, WithdrawResourcesData memory req)
        public
        view
        returns (bool)
    {
        return req.wood <= LibSettlementMath.remainingCapacity(cs.carryWood, ClanWorldConstants.WOOD_CAP)
            && req.iron <= LibSettlementMath.remainingCapacity(cs.carryIron, ClanWorldConstants.IRON_CAP)
            && req.wheat <= LibSettlementMath.remainingCapacity(cs.carryWheat, ClanWorldConstants.WHEAT_CAP)
            && req.fish <= LibSettlementMath.remainingCapacity(cs.carryFish, ClanWorldConstants.FISH_CAP);
    }
}
