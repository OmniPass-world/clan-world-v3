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
    MarketExecutionMode,
    Mission,
    OrderResult,
    StatusCode
} from "../../IClanWorld.sol";
import {LibGameRules} from "./LibGameRules.sol";
import {LibOrderDefenders} from "./LibOrderDefenders.sol";
import {LibOrderMarket} from "./LibOrderMarket.sol";
import {LibOrderUpgrades} from "./LibOrderUpgrades.sol";
import {LibSettlement} from "./LibSettlement.sol";
import {LibSettlementMath} from "./LibSettlementMath.sol";
import {LibStorage} from "./LibStorage.sol";
import {LibOrderTravel} from "./LibOrderTravel.sol";
import {LibSubmitOrderValidation} from "./LibSubmitOrderValidation.sol";

library LibSubmitOrders {
    event ClanSettled(uint32 indexed clanId, uint64 settledToTick);
    event MissionAssigned(
        uint32 indexed clanId,
        uint32 indexed clansmanId,
        uint64 missionNonce,
        ActionType action,
        uint8 startRegion,
        uint8 targetRegion,
        uint64 startTick,
        uint64 arrivalTick
    );
    event MissionInterrupted(
        uint32 indexed clanId, uint32 indexed clansmanId, uint64 oldMissionNonce, uint64 newMissionNonce
    );

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
        public
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
            StatusCode defendErr = LibSubmitOrderValidation.validateDefendBaseOrder(s, clan, order, order.gotoRegion);
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

        StatusCode actionErr = LibSubmitOrderValidation.validateAction(s, clan, cs, order, ctx.gotoRegion);
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
}
