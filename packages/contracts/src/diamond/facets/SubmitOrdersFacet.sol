// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {
    ActionType,
    Clan,
    ClanOrder,
    ClanState,
    ClanWorldConstants,
    Clansman,
    ClansmanState,
    IClanWorldEvents,
    MarketExecutionMode,
    Mission,
    OrderResult,
    StatusCode,
    WithdrawResourcesData
} from "../../IClanWorld.sol";
import {LibGameRules} from "../lib/LibGameRules.sol";
import {LibOrderDefenders} from "../lib/LibOrderDefenders.sol";
import {LibOrderMarket} from "../lib/LibOrderMarket.sol";
import {LibOrderUpgrades} from "../lib/LibOrderUpgrades.sol";
import {LibSeason} from "../lib/LibSeason.sol";
import {LibSettlement} from "../lib/LibSettlement.sol";
import {LibSettlementMath} from "../lib/LibSettlementMath.sol";
import {LibStorage} from "../lib/LibStorage.sol";
import {LibOrderTravel} from "../lib/LibOrderTravel.sol";

contract SubmitOrdersFacet is IClanWorldEvents {
    struct OrderCtx {
        uint8 gotoRegion;
        uint8 fromRegion;
        uint8 travelTicks;
        uint64 arrivalTick;
        uint64 newNonce;
        uint32 targetClanId;
        bool wasActive;
        uint64 oldNonce;
    }

    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
        external
        returns (OrderResult[] memory results)
    {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);
        LibGameRules.requireNoPendingSeasonFinalization(s);

        Clan storage clan = s.clans[clanId];
        require(clan.clanId != 0, "ClanWorld: clan not found");
        require(clan.owner == msg.sender, "ClanWorld: not clan owner");
        require(clan.clanState == ClanState.ACTIVE, "ClanWorld: clan dead");

        if (s.world.currentTick > clan.lastSettledTick + LibGameRules.MAX_LAZY_SETTLE_BACKLOG) {
            results = new OrderResult[](orders.length);
            for (uint256 i = 0; i < orders.length; i++) {
                results[i] = OrderResult({
                    clansmanId: orders[i].clansmanId,
                    status: StatusCode.ERR_MUST_SETTLE_FIRST,
                    cooldownEndsAtTs: 0,
                    missionNonce: 0,
                    marketMode: MarketExecutionMode.None
                });
            }
            LibStorage.exitNonReentrant(s);
            return results;
        }

        _settleClan(s, clanId);

        results = new OrderResult[](orders.length);
        if (clan.clanState == ClanState.DEAD) {
            for (uint256 i = 0; i < orders.length; i++) {
                results[i] = OrderResult({
                    clansmanId: orders[i].clansmanId,
                    status: StatusCode.ERR_CLAN_DEAD,
                    cooldownEndsAtTs: 0,
                    missionNonce: 0,
                    marketMode: MarketExecutionMode.None
                });
            }
            LibStorage.exitNonReentrant(s);
            return results;
        }

        for (uint256 i = 0; i < orders.length; i++) {
            results[i] = _processOrder(s, clanId, clan, orders[i]);
        }

        LibStorage.exitNonReentrant(s);
        return results;
    }

    function _processOrder(LibStorage.AppStorage storage s, uint32 clanId, Clan storage clan, ClanOrder calldata order)
        private
        returns (OrderResult memory result)
    {
        result.clansmanId = order.clansmanId;

        Clansman storage cs = s.clansmen[order.clansmanId];
        if (cs.clansmanId == 0 || cs.clanId != clanId) {
            result.status = StatusCode.ERR_INVALID_CLANSMAN;
            return result;
        }
        if (cs.state == ClansmanState.DEAD) {
            result.status = StatusCode.ERR_CLANSMAN_DEAD;
            return result;
        }

        if (order.action == ActionType.DefendBase) {
            StatusCode defendErr = _validateDefendBaseOrder(s, clan, order, order.gotoRegion);
            if (defendErr != StatusCode.OK) {
                result.status = defendErr;
                return result;
            }

            uint32 defendTargetClanId = order.targetClanId == 0 ? clanId : order.targetClanId;
            Mission storage currentM = s.missions[order.clansmanId];
            if (
                currentM.active && currentM.action == ActionType.DefendBase && currentM.targetRegion == order.gotoRegion
                    && currentM.targetClanId == defendTargetClanId
            ) {
                result.status = StatusCode.OK;
                result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
                result.missionNonce = currentM.nonce;
                return result;
            }
        }

        bool isMarketAction = order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell;
        if (block.timestamp < cs.cooldownEndsAtTs) {
            result.status = StatusCode.ERR_COOLDOWN_ACTIVE;
            result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
            result.missionNonce = cs.lastMissionNonce;
            return result;
        }

        OrderCtx memory ctx;
        ctx.fromRegion = cs.currentRegion;
        ctx.gotoRegion = order.gotoRegion;
        ctx.targetClanId =
            order.action == ActionType.DefendBase && order.targetClanId == 0 ? clanId : order.targetClanId;

        if (
            order.action != ActionType.DefendBase
                && (ctx.gotoRegion == ClanWorldConstants.REGION_NOOP || ctx.gotoRegion == ctx.fromRegion)
        ) ctx.gotoRegion = ctx.fromRegion;

        if (ctx.gotoRegion > 8) {
            result.status = StatusCode.ERR_INVALID_REGION;
            return result;
        }

        StatusCode actionErr = _validateAction(s, clan, cs, order, ctx.gotoRegion);
        if (actionErr != StatusCode.OK) {
            result.status = actionErr;
            return result;
        }

        if (
            isMarketAction && ctx.fromRegion == ClanWorldConstants.REGION_UNICORN_TOWN
                && cs.state == ClansmanState.WAITING && block.timestamp >= cs.cooldownEndsAtTs
        ) {
            ctx.newNonce = cs.lastMissionNonce + 1;
            cs.lastMissionNonce = ctx.newNonce;

            StatusCode marketStatus = LibOrderMarket.executeImmediateMarket(s, clanId, order, cs.clansmanId);
            if (marketStatus == StatusCode.OK) {
                cs.state = ClansmanState.WAITING;
                cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
            }
            LibOrderDefenders.clearDefender(s, cs.clansmanId);

            result.status = marketStatus;
            result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
            result.missionNonce = ctx.newNonce;
            result.marketMode = MarketExecutionMode.Immediate;
            return result;
        }

        ctx.wasActive = s.missions[order.clansmanId].active;
        ctx.oldNonce = s.missions[order.clansmanId].nonce;
        ctx.travelTicks = LibOrderTravel.travelTicks(ctx.fromRegion, ctx.gotoRegion);
        ctx.arrivalTick = LibSettlementMath.addTicksClamped(s.world.currentTick, uint64(ctx.travelTicks));
        ctx.newNonce = cs.lastMissionNonce + 1;
        cs.lastMissionNonce = ctx.newNonce;

        if (ctx.wasActive) {
            LibOrderUpgrades.refundUpgradeReservation(s, order.clansmanId, s.missions[order.clansmanId].action);
        }
        if (order.action == ActionType.UpgradeWall) {
            LibOrderUpgrades.reserveWallUpgrade(s, clan, clanId, order.clansmanId, ctx.newNonce);
        }
        if (order.action == ActionType.UpgradeBase) {
            LibOrderUpgrades.reserveBaseUpgrade(s, clan, clanId, order.clansmanId, ctx.newNonce);
        }
        if (order.action == ActionType.UpgradeMonument) {
            LibOrderUpgrades.reserveMonumentUpgrade(s, clan, clanId, order.clansmanId, ctx.newNonce);
        }

        Mission storage mission = s.missions[order.clansmanId];
        _installMission(s, mission, order, cs, ctx);

        if (ctx.travelTicks > 0) {
            cs.state = ClansmanState.TRAVELING;
        } else {
            cs.state = ClansmanState.ACTING;
            cs.currentRegion = ctx.gotoRegion;
        }
        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;

        LibOrderDefenders.clearDefender(s, cs.clansmanId);
        if (order.action == ActionType.DefendBase && ctx.travelTicks == 0) {
            LibOrderDefenders.registerDefender(s, ctx.gotoRegion, clanId, cs.clansmanId);
        }

        if (ctx.wasActive) emit MissionInterrupted(clanId, order.clansmanId, ctx.oldNonce, ctx.newNonce);
        emit MissionAssigned(
            clanId,
            order.clansmanId,
            ctx.newNonce,
            order.action,
            ctx.fromRegion,
            ctx.gotoRegion,
            s.world.currentTick,
            ctx.arrivalTick
        );

        result.status = StatusCode.OK;
        result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
        result.missionNonce = ctx.newNonce;
        result.marketMode = isMarketAction ? MarketExecutionMode.Scheduled : MarketExecutionMode.None;
        if (isMarketAction) LibOrderMarket.enqueueScheduledMarketAction(s, clanId, cs.clansmanId, mission);
    }

    function _settleClan(LibStorage.AppStorage storage s, uint32 clanId) private {
        if (s.clans[clanId].clanId == 0) return;
        if (s.clans[clanId].clanState == ClanState.DEAD) return;
        if (s.clans[clanId].lastSettledTick >= s.world.currentTick) return;

        LibSettlement.SettlementSimulation memory sim = LibSettlement.simulateToTick(s, clanId, s.world.currentTick);
        LibSettlement.commitSimulation(s, sim);
        emit ClanSettled(clanId, s.clans[clanId].lastSettledTick);
    }

    function _installMission(
        LibStorage.AppStorage storage s,
        Mission storage m,
        ClanOrder calldata order,
        Clansman storage cs,
        OrderCtx memory ctx
    ) private {
        m.active = true;
        m.nonce = ctx.newNonce;
        m.clansmanId = cs.clansmanId;
        m.submittedAtTick = s.world.currentTick;
        m.executesAtTick = ctx.arrivalTick;
        m.settlesAtTick = order.action == ActionType.DefendBase
            ? type(uint64).max
            : (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
                ? ctx.arrivalTick
                : LibSettlementMath.addTicksClamped(ctx.arrivalTick, LibGameRules.actionDuration(order.action));
        m.startRegion = ctx.fromRegion;
        m.targetRegion = ctx.gotoRegion;
        m.action = order.action;
        m.startTick = s.world.currentTick;
        m.arrivalTick = ctx.arrivalTick;
        m.actionStartTick = ctx.arrivalTick;
        m.missionSeed = keccak256(abi.encode(s.world.currentTickSeed, cs.clansmanId, ctx.newNonce));
        m.marketMode = (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
            ? MarketExecutionMode.Scheduled
            : MarketExecutionMode.None;
        m.targetClanId = ctx.targetClanId;
        m.marketToken = order.marketToken;
        m.marketAmount = order.marketAmount;
        m.maxGoldIn = order.maxGoldIn;
        m.withdrawResources = order.withdrawResources;

        if (m.marketMode == MarketExecutionMode.Scheduled) {
            s.marketMissionCommitSequence[cs.clansmanId] = s.world.nextCommitSequence++;
        } else {
            delete s.marketMissionCommitSequence[cs.clansmanId];
        }
    }

    function _validateAction(
        LibStorage.AppStorage storage s,
        Clan storage clan,
        Clansman storage cs,
        ClanOrder calldata order,
        uint8 gotoRegion
    ) private view returns (StatusCode) {
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
        if (action == ActionType.DefendBase) return _validateDefendBaseOrder(s, clan, order, gotoRegion);
        if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
            return LibOrderMarket.validateMarketOrder(s, cs, order, gotoRegion);
        }
        return StatusCode.OK;
    }

    function _validateDefendBaseOrder(
        LibStorage.AppStorage storage s,
        Clan storage clan,
        ClanOrder calldata order,
        uint8 gotoRegion
    ) private view returns (StatusCode) {
        uint32 targetClanId = order.targetClanId == 0 ? clan.clanId : order.targetClanId;
        Clan storage targetClan = s.clans[targetClanId];
        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
            return StatusCode.ERR_INVALID_TARGET;
        }
        if (gotoRegion != targetClan.baseRegion) return StatusCode.ERR_INVALID_REGION;
        return StatusCode.OK;
    }
}
