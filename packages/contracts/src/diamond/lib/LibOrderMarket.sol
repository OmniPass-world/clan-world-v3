// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {
    ActionType,
    Clan,
    ClanOrder,
    ClanWorldConstants,
    Clansman,
    ClansmanState,
    MarketExecutionMode,
    Mission,
    ResourceType,
    ScheduledMarketAction,
    StatusCode
} from "../../IClanWorld.sol";
import {StubPool} from "../../StubPool.sol";
import {LibSettlementMath} from "./LibSettlementMath.sol";
import {LibStorage} from "./LibStorage.sol";

library LibOrderMarket {
    event ScheduledMarketActionCommitted(
        uint64 indexed executeAtTick,
        uint64 indexed commitSequence,
        uint32 indexed clanId,
        uint32 clansmanId,
        ActionType action,
        address marketToken,
        uint256 marketAmount,
        uint256 maxGoldIn
    );
    event ImmediateMarketActionExecuted(
        uint32 indexed clanId,
        uint32 indexed clansmanId,
        ActionType action,
        uint8 resourceIn,
        uint256 amountIn,
        uint8 resourceOut,
        uint256 amountOut,
        uint64 tick
    );
    event ScheduledMarketActionExecuted(
        uint32 indexed clanId,
        uint32 clansmanId,
        ActionType action,
        uint8 resourceIn,
        uint256 amountIn,
        uint8 resourceOut,
        uint256 amountOut,
        uint64 settledAtTick
    );
    event MarketActionFailed(
        uint32 indexed clanId,
        uint32 indexed clansmanId,
        ActionType action,
        MarketExecutionMode mode,
        StatusCode reason,
        uint64 tick
    );

    function validateMarketOrder(
        LibStorage.AppStorage storage s,
        Clansman storage cs,
        ClanOrder calldata order,
        uint8 gotoRegion
    ) public view returns (StatusCode) {
        if (s.treasury.woodToken == address(0)) {
            return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
        }
        if (gotoRegion != ClanWorldConstants.REGION_UNICORN_TOWN) return StatusCode.ERR_INVALID_REGION;
        if (order.marketAmount == 0) return StatusCode.ERR_MARKET_ZERO_AMOUNT;
        address tok = order.marketToken;
        if (
            tok == address(0) || tok == s.treasury.goldToken
                || (tok != s.treasury.woodToken
                    && tok != s.treasury.ironToken
                    && tok != s.treasury.wheatToken
                    && tok != s.treasury.fishToken)
        ) return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
        if (order.action == ActionType.MarketBuy && order.maxGoldIn == 0) return StatusCode.ERR_SLIPPAGE_REQUIRED;
        if (order.action == ActionType.MarketBuy && order.marketAmount > remainingCarryForToken(s, cs, tok)) {
            return StatusCode.ERR_CARRY_FULL;
        }
        if (order.action == ActionType.MarketSell && !hasCarryBalance(s, cs, tok, order.marketAmount)) {
            return StatusCode.ERR_MISSING_RESOURCES;
        }
        return StatusCode.OK;
    }

    function enqueueScheduledMarketAction(
        LibStorage.AppStorage storage s,
        uint32 clanId,
        uint32 clansmanId,
        Mission storage m
    ) public {
        uint64 executeAtTick = m.settlesAtTick;
        ScheduledMarketAction memory sma = ScheduledMarketAction({
            executeAtTick: executeAtTick,
            commitSequence: s.marketMissionCommitSequence[clansmanId],
            missionNonce: m.nonce,
            clanId: clanId,
            clansmanId: clansmanId,
            action: m.action,
            marketToken: m.marketToken,
            marketAmount: m.marketAmount,
            maxGoldIn: m.maxGoldIn
        });
        s.scheduledMarketActions[executeAtTick].push(sma);
        emit ScheduledMarketActionCommitted(
            executeAtTick, sma.commitSequence, clanId, clansmanId, m.action, m.marketToken, m.marketAmount, m.maxGoldIn
        );
    }

    function executeScheduledMarketActions(LibStorage.AppStorage storage s, uint64 tick) public {
        ScheduledMarketAction[] storage actions = s.scheduledMarketActions[tick];
        uint256 len = actions.length;
        if (len == 0) return;

        ScheduledMarketAction[] memory arr = new ScheduledMarketAction[](len);
        for (uint256 i = 0; i < len; i++) {
            arr[i] = actions[i];
        }
        for (uint256 i = 1; i < len; i++) {
            ScheduledMarketAction memory key = arr[i];
            uint256 j = i;
            while (j > 0 && arr[j - 1].commitSequence > key.commitSequence) {
                arr[j] = arr[j - 1];
                j--;
            }
            arr[j] = key;
        }

        for (uint256 i = 0; i < len; i++) {
            ScheduledMarketAction memory sma = arr[i];
            Clansman storage cs = s.clansmen[sma.clansmanId];
            if (cs.clanId != sma.clanId || cs.state == ClansmanState.DEAD) {
                emit MarketActionFailed(
                    sma.clanId,
                    sma.clansmanId,
                    sma.action,
                    MarketExecutionMode.Scheduled,
                    StatusCode.ERR_INVALID_CLANSMAN,
                    tick
                );
                continue;
            }

            Mission storage m = s.missions[sma.clansmanId];
            if (m.action != sma.action || m.nonce != sma.missionNonce) {
                emit MarketActionFailed(
                    sma.clanId,
                    sma.clansmanId,
                    sma.action,
                    MarketExecutionMode.Scheduled,
                    StatusCode.ERR_INVALID_ACTION,
                    tick
                );
                continue;
            }

            StatusCode status = sma.action == ActionType.MarketSell
                ? executeMarketSell(s, tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount)
                : executeMarketBuy(
                    s, tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.maxGoldIn
                );
            if (status != StatusCode.OK) {
                handleMarketFailureAt(
                    s, sma.clanId, sma.clansmanId, sma.action, MarketExecutionMode.Scheduled, status, tick
                );
            }
        }

        delete s.scheduledMarketActions[tick];
    }

    function executeImmediateMarket(
        LibStorage.AppStorage storage s,
        uint32 clanId,
        ClanOrder calldata order,
        uint32 clansmanId
    ) public returns (StatusCode) {
        if (order.action == ActionType.MarketSell) {
            return executeImmediateMarketSell(s, clanId, clansmanId, order.marketToken, order.marketAmount);
        }
        return executeImmediateMarketBuy(s, clanId, clansmanId, order.marketToken, order.marketAmount, order.maxGoldIn);
    }

    function executeImmediateMarketSell(
        LibStorage.AppStorage storage s,
        uint32 clanId,
        uint32 clansmanId,
        address token,
        uint256 amount
    ) public returns (StatusCode) {
        if (!s.treasury.poolsSeeded) {
            return handleMarketFailure(
                s,
                clanId,
                clansmanId,
                ActionType.MarketSell,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN
            );
        }
        address poolAddr = poolFor(s, token);
        if (poolAddr == address(0)) {
            return handleMarketFailure(
                s,
                clanId,
                clansmanId,
                ActionType.MarketSell,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN
            );
        }

        Clan storage clan = s.clans[clanId];
        Clansman storage cs = s.clansmen[clansmanId];
        if (!deductFromCarry(s, cs, token, amount)) {
            return handleMarketFailure(
                s,
                clanId,
                clansmanId,
                ActionType.MarketSell,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_MISSING_RESOURCES
            );
        }

        try StubPool(poolAddr).swapExactInForOut(amount, 0) returns (uint256 goldOut) {
            clan.goldBalance += goldOut;
            emit ImmediateMarketActionExecuted(
                clanId,
                clansmanId,
                ActionType.MarketSell,
                marketResourceForToken(s, token),
                amount,
                ClanWorldConstants.RESOURCE_GOLD,
                goldOut,
                s.world.currentTick
            );
            return StatusCode.OK;
        } catch {
            addToCarry(s, cs, token, amount);
            return handleMarketFailure(
                s,
                clanId,
                clansmanId,
                ActionType.MarketSell,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_LIQUIDITY_INSUFFICIENT
            );
        }
    }

    function executeImmediateMarketBuy(
        LibStorage.AppStorage storage s,
        uint32 clanId,
        uint32 clansmanId,
        address token,
        uint256 amountOut,
        uint256 maxGoldIn
    ) public returns (StatusCode) {
        if (!s.treasury.poolsSeeded) {
            return handleMarketFailure(
                s,
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN
            );
        }
        address poolAddr = poolFor(s, token);
        if (poolAddr == address(0)) {
            return handleMarketFailure(
                s,
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN
            );
        }

        Clansman storage cs = s.clansmen[clansmanId];
        if (amountOut > remainingCarryForToken(s, cs, token)) {
            return handleMarketFailure(
                s, clanId, clansmanId, ActionType.MarketBuy, MarketExecutionMode.Immediate, StatusCode.ERR_CARRY_FULL
            );
        }

        uint256 goldIn;
        try StubPool(poolAddr).getAmountInForExactOut(amountOut) returns (uint256 quotedGoldIn) {
            goldIn = quotedGoldIn;
        } catch {
            return handleMarketFailure(
                s,
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_LIQUIDITY_INSUFFICIENT
            );
        }

        Clan storage clan = s.clans[clanId];
        if (goldIn > maxGoldIn) {
            return handleMarketFailure(
                s,
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_MAX_GOLD_IN_EXCEEDED
            );
        }
        if (clan.goldBalance < goldIn) {
            return handleMarketFailure(
                s,
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_NOT_ENOUGH_GOLD
            );
        }

        try StubPool(poolAddr).swapExactOutForInWithMaxIn(amountOut, maxGoldIn) returns (uint256 actualGoldIn) {
            clan.goldBalance -= actualGoldIn;
            addToCarry(s, cs, token, amountOut);
            emit ImmediateMarketActionExecuted(
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                ClanWorldConstants.RESOURCE_GOLD,
                actualGoldIn,
                marketResourceForToken(s, token),
                amountOut,
                s.world.currentTick
            );
            return StatusCode.OK;
        } catch {
            return handleMarketFailure(
                s,
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_LIQUIDITY_INSUFFICIENT
            );
        }
    }

    function executeMarketSell(
        LibStorage.AppStorage storage s,
        uint64 closedTick,
        uint32 clanId,
        uint32 clansmanId,
        address token,
        uint256 amount
    ) public returns (StatusCode) {
        if (!s.treasury.poolsSeeded) return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
        address poolAddr = poolFor(s, token);
        if (poolAddr == address(0)) return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;

        Clan storage clan = s.clans[clanId];
        Clansman storage cs = s.clansmen[clansmanId];
        if (!deductFromCarry(s, cs, token, amount)) return StatusCode.ERR_MISSING_RESOURCES;

        try StubPool(poolAddr).sellResource(amount) returns (uint256 goldOut) {
            clan.goldBalance += goldOut;
            emit ScheduledMarketActionExecuted(
                clanId,
                clansmanId,
                ActionType.MarketSell,
                marketResourceForToken(s, token),
                amount,
                ClanWorldConstants.RESOURCE_GOLD,
                goldOut,
                closedTick
            );
            return StatusCode.OK;
        } catch {
            addToCarry(s, cs, token, amount);
            return StatusCode.ERR_LIQUIDITY_INSUFFICIENT;
        }
    }

    function executeMarketBuy(
        LibStorage.AppStorage storage s,
        uint64 closedTick,
        uint32 clanId,
        uint32 clansmanId,
        address token,
        uint256 amountOut,
        uint256 maxGoldIn
    ) public returns (StatusCode) {
        if (!s.treasury.poolsSeeded) return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
        address poolAddr = poolFor(s, token);
        if (poolAddr == address(0)) return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;

        Clansman storage cs = s.clansmen[clansmanId];
        if (amountOut > remainingCarryForToken(s, cs, token)) return StatusCode.ERR_CARRY_FULL;

        uint256 goldIn;
        try StubPool(poolAddr).quoteBuy(amountOut) returns (uint256 quotedGoldIn) {
            goldIn = quotedGoldIn;
        } catch {
            return StatusCode.ERR_LIQUIDITY_INSUFFICIENT;
        }

        Clan storage clan = s.clans[clanId];
        if (goldIn > maxGoldIn) return StatusCode.ERR_MAX_GOLD_IN_EXCEEDED;
        if (clan.goldBalance < goldIn) return StatusCode.ERR_NOT_ENOUGH_GOLD;

        try StubPool(poolAddr).swapExactOutForInWithMaxIn(amountOut, maxGoldIn) returns (uint256 actualGoldIn) {
            clan.goldBalance -= actualGoldIn;
            addToCarry(s, cs, token, amountOut);
            emit ScheduledMarketActionExecuted(
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                ClanWorldConstants.RESOURCE_GOLD,
                actualGoldIn,
                marketResourceForToken(s, token),
                amountOut,
                closedTick
            );
            return StatusCode.OK;
        } catch {
            return StatusCode.ERR_LIQUIDITY_INSUFFICIENT;
        }
    }

    function hasCarryBalance(LibStorage.AppStorage storage s, Clansman storage cs, address token, uint256 amount)
        public
        view
        returns (bool)
    {
        if (token == s.treasury.woodToken) return cs.carryWood >= amount;
        if (token == s.treasury.ironToken) return cs.carryIron >= amount;
        if (token == s.treasury.wheatToken) return cs.carryWheat >= amount;
        if (token == s.treasury.fishToken) return cs.carryFish >= amount;
        return false;
    }

    function remainingCarryForToken(LibStorage.AppStorage storage s, Clansman storage cs, address token)
        public
        view
        returns (uint256)
    {
        if (token == s.treasury.woodToken) {
            return LibSettlementMath.remainingCapacity(cs.carryWood, ClanWorldConstants.WOOD_CAP);
        }
        if (token == s.treasury.ironToken) {
            return LibSettlementMath.remainingCapacity(cs.carryIron, ClanWorldConstants.IRON_CAP);
        }
        if (token == s.treasury.wheatToken) {
            return LibSettlementMath.remainingCapacity(cs.carryWheat, ClanWorldConstants.WHEAT_CAP);
        }
        if (token == s.treasury.fishToken) {
            return LibSettlementMath.remainingCapacity(cs.carryFish, ClanWorldConstants.FISH_CAP);
        }
        return 0;
    }

    function handleMarketFailure(
        LibStorage.AppStorage storage s,
        uint32 clanId,
        uint32 clansmanId,
        ActionType action,
        MarketExecutionMode mode,
        StatusCode reason
    ) public returns (StatusCode) {
        Clansman storage cs = s.clansmen[clansmanId];
        if (cs.clansmanId != 0 && cs.state != ClansmanState.DEAD) {
            cs.state = ClansmanState.WAITING;
            cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
        }
        emit MarketActionFailed(clanId, clansmanId, action, mode, reason, s.world.currentTick);
        return reason;
    }

    function handleMarketFailureAt(
        LibStorage.AppStorage storage s,
        uint32 clanId,
        uint32 clansmanId,
        ActionType action,
        MarketExecutionMode mode,
        StatusCode reason,
        uint64 tick
    ) public returns (StatusCode) {
        Clansman storage cs = s.clansmen[clansmanId];
        if (cs.clansmanId != 0 && cs.state != ClansmanState.DEAD) {
            cs.state = ClansmanState.WAITING;
            cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
        }
        emit MarketActionFailed(clanId, clansmanId, action, mode, reason, tick);
        return reason;
    }

    function poolFor(LibStorage.AppStorage storage s, address token) public view returns (address) {
        if (token == s.treasury.woodToken) return s.treasury.woodGoldPool;
        if (token == s.treasury.ironToken) return s.treasury.ironGoldPool;
        if (token == s.treasury.wheatToken) return s.treasury.wheatGoldPool;
        if (token == s.treasury.fishToken) return s.treasury.fishGoldPool;
        return address(0);
    }

    function marketResourceForToken(LibStorage.AppStorage storage s, address token) public view returns (uint8) {
        if (token == s.treasury.woodToken) return uint8(ResourceType.Wood);
        if (token == s.treasury.ironToken) return uint8(ResourceType.Iron);
        if (token == s.treasury.wheatToken) return uint8(ResourceType.Wheat);
        if (token == s.treasury.fishToken) return uint8(ResourceType.Fish);
        if (token == s.treasury.goldToken) return ClanWorldConstants.RESOURCE_GOLD;
        revert("ClanWorld: invalid market resource");
    }

    function deductFromCarry(LibStorage.AppStorage storage s, Clansman storage cs, address token, uint256 amount)
        public
        returns (bool)
    {
        if (token == s.treasury.woodToken) {
            if (cs.carryWood < amount) return false;
            cs.carryWood -= amount;
            return true;
        }
        if (token == s.treasury.ironToken) {
            if (cs.carryIron < amount) return false;
            cs.carryIron -= amount;
            return true;
        }
        if (token == s.treasury.wheatToken) {
            if (cs.carryWheat < amount) return false;
            cs.carryWheat -= amount;
            return true;
        }
        if (token == s.treasury.fishToken) {
            if (cs.carryFish < amount) return false;
            cs.carryFish -= amount;
            return true;
        }
        return false;
    }

    function addToCarry(LibStorage.AppStorage storage s, Clansman storage cs, address token, uint256 amount) public {
        if (token == s.treasury.woodToken) {
            cs.carryWood += amount;
            return;
        }
        if (token == s.treasury.ironToken) {
            cs.carryIron += amount;
            return;
        }
        if (token == s.treasury.wheatToken) {
            cs.carryWheat += amount;
            return;
        }
        if (token == s.treasury.fishToken) {
            cs.carryFish += amount;
            return;
        }
    }
}
