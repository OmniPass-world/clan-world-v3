// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {
    ActionType,
    BanditState,
    BanditTroop,
    Clan,
    ClanState,
    ClanWorldConstants,
    Clansman,
    ClansmanState,
    Mission,
    WithdrawResourcesData,
    WheatPlot,
    WheatPlotState
} from "../../IClanWorld.sol";
import {RNG} from "../../lib/RNG.sol";
import {LibBanditLifecycle} from "./LibBanditLifecycle.sol";
import {LibGameRules} from "./LibGameRules.sol";
import {LibOrderDefenders} from "./LibOrderDefenders.sol";
import {LibStorage} from "./LibStorage.sol";
import {LibSeason} from "./LibSeason.sol";
import {LibSettlementMath} from "./LibSettlementMath.sol";

library LibSettlement {
    uint8 internal constant WALL_MAX_LEVEL = 5;
    uint8 internal constant BASE_MAX_LEVEL = 5;

    event MissionCompleted(uint32 indexed clanId, uint32 indexed clansmanId, uint64 missionNonce, ActionType action);
    event WorkerArrived(uint32 indexed clanId, uint32 indexed clansmanId, uint8 region, uint64 tick);
    event ResourcesGathered(
        uint32 indexed clanId,
        uint32 indexed clansmanId,
        ActionType action,
        uint256 woodGained,
        uint256 ironGained,
        uint256 wheatGained,
        uint256 fishGained,
        uint256 goldBonus,
        uint64 atTick
    );
    event ResourcesDeposited(
        uint32 indexed clanId,
        uint32 indexed clansmanId,
        uint256 woodDelta,
        uint256 ironDelta,
        uint256 wheatDelta,
        uint256 fishDelta,
        uint64 atTick
    );
    event ResourcesWithdrawn(
        uint32 indexed clanId,
        uint32 indexed clansmanId,
        uint256 woodDelta,
        uint256 ironDelta,
        uint256 wheatDelta,
        uint256 fishDelta,
        uint64 atTick
    );
    event BanditEscaped(uint32 indexed banditId, uint64 atTick);
    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);

    enum SettlementLogKind {
        None,
        ResourcesGathered,
        ResourcesDeposited,
        ResourcesWithdrawn
    }

    struct SettlementLog {
        SettlementLogKind kind;
        uint32 clansmanId;
        ActionType action;
        uint256 wood;
        uint256 iron;
        uint256 wheat;
        uint256 fish;
        uint256 gold;
        uint64 tick;
    }

    struct SettlementSimulation {
        Clan clan;
        Clansman[] clansmen;
        Mission[] missions;
        WheatPlot[2] wheatPlots;
        SettlementLog[] logs;
        uint256 logCount;
        uint64[11] simMonumentReachedAt;
        bool[] simWallReservationCleared;
        bool[] simBaseReservationCleared;
        bool[] simMonumentReservationCleared;
        bool[] simDefenderRegistrationCleared;
        uint64[] simWorkerArrivedTick;
        uint64[] simMissionCompletedNonce;
        bool simClanDied;
        uint256 reservedWood;
        uint256 reservedIron;
        uint256 reservedWheat;
        uint256 reservedBlueprint;
    }

    function loadSimulation(LibStorage.AppStorage storage s, uint32 clanId)
        internal
        view
        returns (SettlementSimulation memory sim)
    {
        sim.clan = s.clans[clanId];
        if (sim.clan.clanId == ClanWorldConstants.CLAN_ID_NULL) return sim;

        uint32[] storage clansmanIds = s.clanClansmanIds[clanId];
        sim.clansmen = new Clansman[](clansmanIds.length);
        sim.missions = new Mission[](clansmanIds.length);
        sim.simWallReservationCleared = new bool[](clansmanIds.length);
        sim.simBaseReservationCleared = new bool[](clansmanIds.length);
        sim.simMonumentReservationCleared = new bool[](clansmanIds.length);
        sim.simDefenderRegistrationCleared = new bool[](clansmanIds.length);
        sim.simWorkerArrivedTick = new uint64[](clansmanIds.length);
        sim.simMissionCompletedNonce = new uint64[](clansmanIds.length);

        for (uint256 i = 0; i < clansmanIds.length; i++) {
            uint32 clansmanId = clansmanIds[i];
            sim.clansmen[i] = s.clansmen[clansmanId];
            sim.missions[i] = s.missions[clansmanId];
        }

        sim.wheatPlots[0] = s.wheatPlots[clanId][0];
        sim.wheatPlots[1] = s.wheatPlots[clanId][1];
        sim.reservedWood = s.reservedWoodByClan[clanId];
        sim.reservedIron = s.reservedIronByClan[clanId];
        sim.reservedWheat = s.reservedWheatByClan[clanId];
        sim.reservedBlueprint = s.reservedBlueprintByClan[clanId];
    }

    function simulateToTick(LibStorage.AppStorage storage s, uint32 clanId, uint64 toTick)
        internal
        view
        returns (SettlementSimulation memory sim)
    {
        sim = loadSimulation(s, clanId);
        if (sim.clan.clanId == ClanWorldConstants.CLAN_ID_NULL) return sim;

        uint64 fromTick = sim.clan.lastSettledTick;
        if (fromTick >= toTick) return sim;
        if (toTick > fromTick + LibGameRules.MAX_LAZY_SETTLE_BACKLOG) {
            toTick = fromTick + LibGameRules.MAX_LAZY_SETTLE_BACKLOG;
        }
        sim.logs = new SettlementLog[](sim.clansmen.length * (toTick - fromTick));

        for (uint64 tick = fromTick; tick < toTick; tick++) {
            applyUpkeep(s, sim, tick);
            if (sim.clan.clanState == ClanState.DEAD) break;
            regrowWheatPlots(sim, tick);
            for (uint256 i = 0; i < sim.clansmen.length; i++) {
                settleMissionForClansman(s, sim, i, tick, tick + 1);
            }
        }

        if (toTick > fromTick && !LibSeason.isWinterActiveAt(toTick) && LibSeason.isWinterActiveAt(toTick - 1)) {
            sim.clan.coldDamage = 0;
        }

        sim.clan.lastSettledTick = toTick;
    }

    function commitSimulation(LibStorage.AppStorage storage s, SettlementSimulation memory sim) internal {
        uint32 clanId = sim.clan.clanId;
        if (clanId == ClanWorldConstants.CLAN_ID_NULL) return;

        s.clans[clanId] = sim.clan;
        s.wheatPlots[clanId][0] = sim.wheatPlots[0];
        s.wheatPlots[clanId][1] = sim.wheatPlots[1];

        for (uint256 i = 0; i < sim.clansmen.length; i++) {
            uint32 clansmanId = sim.clansmen[i].clansmanId;
            s.clansmen[clansmanId] = sim.clansmen[i];
            s.missions[clansmanId] = sim.missions[i];

            if (sim.simWallReservationCleared[i]) {
                clearWallUpgradeReservation(s, clansmanId);
            }
            if (sim.simBaseReservationCleared[i]) {
                clearBaseUpgradeReservation(s, clansmanId);
            }
            if (sim.simMonumentReservationCleared[i]) {
                clearMonumentUpgradeReservation(s, clansmanId);
            }
            if (sim.simDefenderRegistrationCleared[i]) {
                LibOrderDefenders.clearDefender(s, clansmanId);
            }
            if (sim.simWorkerArrivedTick[i] != 0) {
                emit WorkerArrived(clanId, clansmanId, sim.clansmen[i].currentRegion, sim.simWorkerArrivedTick[i]);
                if (sim.missions[i].active && sim.missions[i].action == ActionType.DefendBase) {
                    LibOrderDefenders.registerDefender(s, sim.clansmen[i].currentRegion, clanId, clansmanId);
                }
            }
            emitSettlementLogsFor(sim, clansmanId);
            if (sim.simMissionCompletedNonce[i] != 0) {
                emit MissionCompleted(clanId, clansmanId, sim.simMissionCompletedNonce[i], sim.missions[i].action);
            }
        }

        s.reservedWoodByClan[clanId] = sim.reservedWood;
        s.reservedIronByClan[clanId] = sim.reservedIron;
        s.reservedWheatByClan[clanId] = sim.reservedWheat;
        s.reservedBlueprintByClan[clanId] = sim.reservedBlueprint;
        if (sim.simClanDied) {
            releaseDefendersForDeadTarget(s, clanId, sim.clan.baseRegion);
            abortBanditAttacksForDeadTarget(s, clanId, ClanWorldConstants.BANDIT_ID_NULL, sim.clan.lastSettledTick);
        }
        for (uint8 level = 1; level < sim.simMonumentReachedAt.length; level++) {
            if (sim.simMonumentReachedAt[level] != 0 && s.monumentLevelReachedAt[clanId][level] == 0) {
                s.monumentLevelReachedAt[clanId][level] = sim.simMonumentReachedAt[level];
            }
        }
    }

    function emitSettlementLogsFor(SettlementSimulation memory sim, uint32 clansmanId) internal {
        for (uint256 i = 0; i < sim.logCount; i++) {
            SettlementLog memory entry = sim.logs[i];
            if (entry.clansmanId != clansmanId) continue;
            if (entry.kind == SettlementLogKind.ResourcesGathered) {
                emit ResourcesGathered(
                    sim.clan.clanId,
                    entry.clansmanId,
                    entry.action,
                    entry.wood,
                    entry.iron,
                    entry.wheat,
                    entry.fish,
                    entry.gold,
                    entry.tick
                );
            } else if (entry.kind == SettlementLogKind.ResourcesDeposited) {
                emit ResourcesDeposited(
                    sim.clan.clanId, entry.clansmanId, entry.wood, entry.iron, entry.wheat, entry.fish, entry.tick
                );
            } else if (entry.kind == SettlementLogKind.ResourcesWithdrawn) {
                emit ResourcesWithdrawn(
                    sim.clan.clanId, entry.clansmanId, entry.wood, entry.iron, entry.wheat, entry.fish, entry.tick
                );
            }
        }
    }

    function recordSettlementLog(
        SettlementSimulation memory sim,
        SettlementLogKind kind,
        uint32 clansmanId,
        ActionType action,
        uint256 wood,
        uint256 iron,
        uint256 wheat,
        uint256 fish,
        uint256 gold,
        uint64 tick
    ) internal pure {
        if (sim.logCount >= sim.logs.length) return;
        sim.logs[sim.logCount++] = SettlementLog({
            kind: kind,
            clansmanId: clansmanId,
            action: action,
            wood: wood,
            iron: iron,
            wheat: wheat,
            fish: fish,
            gold: gold,
            tick: tick
        });
    }

    function applyUpkeep(LibStorage.AppStorage storage s, SettlementSimulation memory sim, uint64 tick) internal view {
        bool winter = LibSeason.isWinterActiveAt(tick);
        if (!winter && tick > 0 && LibSeason.isWinterActiveAt(tick - 1)) {
            sim.clan.coldDamage = 0;
        }

        if (sim.clan.livingClansmen == 0) return;

        uint256 wheatNeeded = uint256(sim.clan.livingClansmen) * ClanWorldConstants.WHEAT_UPKEEP_PER_CLANSMAN;
        uint256 fishNeeded = uint256(sim.clan.livingClansmen) * ClanWorldConstants.FISH_UPKEEP_PER_CLANSMAN;
        if (winter) {
            wheatNeeded = wheatNeeded * ClanWorldConstants.WINTER_UPKEEP_MULTIPLIER_BPS / 10000;
            fishNeeded = fishNeeded * ClanWorldConstants.WINTER_UPKEEP_MULTIPLIER_BPS / 10000;
        }

        uint256 spendableWheat = LibSettlementMath.spendableAfterReleasing(sim.clan.vaultWheat, sim.reservedWheat, 0);
        bool hadEnoughWheat = spendableWheat >= wheatNeeded;
        bool hadEnoughFish = sim.clan.vaultFish >= fishNeeded;

        if (hadEnoughWheat) {
            sim.clan.vaultWheat -= wheatNeeded;
        } else {
            sim.clan.vaultWheat -= spendableWheat;
        }
        sim.clan.vaultFish = hadEnoughFish ? sim.clan.vaultFish - fishNeeded : 0;

        bool starving = !hadEnoughWheat || !hadEnoughFish;
        if (starving && sim.clan.starvationStartsAtTick == 0) {
            sim.clan.starvationStartsAtTick = tick + 1;
        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
            sim.clan.starvationStartsAtTick = 0;
        }

        uint8 livingBeforeStarvation = sim.clan.livingClansmen;
        if (winter && starving) {
            (, uint64 winterStartsAtTick,) = LibSeason.winterWindowForTick(tick);
            uint64 effectiveStarvationStartsAtTick = sim.clan.starvationStartsAtTick > winterStartsAtTick
                ? sim.clan.starvationStartsAtTick
                : winterStartsAtTick;
            if (effectiveStarvationStartsAtTick < tick) {
                killNextClansmanFromStarvation(s, sim);
            }
        }
        if (sim.clan.clanState == ClanState.DEAD) return;

        if (winter) {
            uint256 woodNeeded = ClanWorldConstants.WINTER_WOOD_BURN_PER_BASE + uint256(livingBeforeStarvation)
                * ClanWorldConstants.WINTER_WOOD_BURN_PER_CLANSMAN;
            uint256 spendableWood =
                LibSettlementMath.spendableAfterReleasing(sim.clan.vaultWood, sim.reservedWood, 0);
            if (spendableWood >= woodNeeded) {
                sim.clan.vaultWood -= woodNeeded;
            } else {
                sim.clan.vaultWood -= spendableWood;
                uint16 oldColdDamage = sim.clan.coldDamage;
                if (sim.clan.coldDamage < type(uint16).max) {
                    sim.clan.coldDamage += 1;
                }
                applyColdDamageConsequence(s, sim, tick, oldColdDamage);
            }
        }
    }

    function applyColdDamageConsequence(
        LibStorage.AppStorage storage s,
        SettlementSimulation memory sim,
        uint64 tick,
        uint16 oldColdDamage
    ) internal view {
        uint16 newColdDamage = sim.clan.coldDamage;
        if (newColdDamage == oldColdDamage) return;

        if (sim.clan.wallLevel > 0) {
            if (
                newColdDamage / ClanWorldConstants.COLD_DAMAGE_PER_WALL_DEGRADATION
                    <= oldColdDamage / ClanWorldConstants.COLD_DAMAGE_PER_WALL_DEGRADATION
            ) return;

            sim.clan.wallLevel--;
            return;
        }

        if (
            newColdDamage / ClanWorldConstants.COLD_DAMAGE_PER_CLANSMAN_DEATH
                <= oldColdDamage / ClanWorldConstants.COLD_DAMAGE_PER_CLANSMAN_DEATH
        ) return;

        killRandomClansmanFromCold(s, sim, tick, newColdDamage);
    }

    function killRandomClansmanFromCold(
        LibStorage.AppStorage storage s,
        SettlementSimulation memory sim,
        uint64 tick,
        uint16 coldDamage
    ) internal view {
        if (sim.clan.livingClansmen == 0) return;

        uint256 livingCount = 0;
        for (uint256 i = 0; i < sim.clansmen.length; i++) {
            if (sim.clansmen[i].state != ClansmanState.DEAD) {
                livingCount++;
            }
        }
        if (livingCount == 0) return;

        uint256 pick = RNG.rngBounded(
            s.tickSeeds[tick],
            RNG.DOMAIN_COLD_DAMAGE,
            uint256(keccak256(abi.encodePacked(sim.clan.clanId, tick, coldDamage))),
            livingCount
        );

        uint256 seen = 0;
        for (uint256 i = 0; i < sim.clansmen.length; i++) {
            if (sim.clansmen[i].state == ClansmanState.DEAD) continue;
            if (seen != pick) {
                seen++;
                continue;
            }

            markClansmanDead(s, sim, i);
            if (sim.clan.livingClansmen == 0) {
                markClanDead(s, sim);
            }
            return;
        }
    }

    function killNextClansmanFromStarvation(LibStorage.AppStorage storage s, SettlementSimulation memory sim)
        internal
        view
    {
        if (sim.clan.livingClansmen == 0) return;

        for (uint256 i = 0; i < sim.clansmen.length; i++) {
            if (sim.clansmen[i].state == ClansmanState.DEAD) continue;

            markClansmanDead(s, sim, i);
            if (sim.clan.livingClansmen == 0) {
                markClanDead(s, sim);
            }
            return;
        }
    }

    function markClansmanDead(LibStorage.AppStorage storage s, SettlementSimulation memory sim, uint256 index)
        internal
        view
    {
        if (sim.clansmen[index].state == ClansmanState.DEAD) return;

        sim.clansmen[index].state = ClansmanState.DEAD;
        sim.clansmen[index].cooldownEndsAtTs = 0;
        if (sim.clan.livingClansmen > 0) {
            sim.clan.livingClansmen--;
        }
        if (sim.missions[index].active) {
            if (sim.missions[index].action == ActionType.DefendBase) {
                sim.simDefenderRegistrationCleared[index] = true;
            }
            refundSimUpgradeReservation(s, sim, index, sim.missions[index].action);
            sim.missions[index].active = false;
        }
    }

    function markClanDead(LibStorage.AppStorage storage s, SettlementSimulation memory sim) internal view {
        if (sim.clan.clanId == ClanWorldConstants.CLAN_ID_NULL || sim.clan.clanState == ClanState.DEAD) return;

        sim.clan.clanState = ClanState.DEAD;
        sim.clan.vaultWood = 0;
        sim.clan.vaultWheat = 0;
        sim.clan.vaultFish = 0;
        sim.clan.vaultIron = 0;
        sim.clan.starvationStartsAtTick = 0;
        sim.clan.livingClansmen = 0;

        for (uint256 i = 0; i < sim.clansmen.length; i++) {
            sim.clansmen[i].state = ClansmanState.DEAD;
            sim.clansmen[i].cooldownEndsAtTs = 0;
            if (sim.missions[i].active) {
                if (sim.missions[i].action == ActionType.DefendBase) {
                    sim.simDefenderRegistrationCleared[i] = true;
                }
                refundSimUpgradeReservation(s, sim, i, sim.missions[i].action);
                sim.missions[i].active = false;
            }
        }
        sim.simClanDied = true;
    }

    function regrowWheatPlots(SettlementSimulation memory sim, uint64 tick) internal pure {
        for (uint256 pi = 0; pi < 2; pi++) {
            WheatPlot memory plot = sim.wheatPlots[pi];
            if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
                plot.state = WheatPlotState.Harvestable;
                plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
                plot.regrowUntilTick = 0;
                sim.wheatPlots[pi] = plot;
            }
        }
    }

    function settleMissionForClansman(
        LibStorage.AppStorage storage s,
        SettlementSimulation memory sim,
        uint256 index,
        uint64 fromTick,
        uint64 toTick
    ) internal view {
        Clansman memory cs = sim.clansmen[index];
        Mission memory m = sim.missions[index];

        if (cs.state == ClansmanState.DEAD) {
            if (m.active) {
                m.active = false;
            }
            sim.missions[index] = m;
            return;
        }

        if (!m.active) return;

        for (uint64 tick = fromTick; tick < toTick; tick++) {
            bytes32 tickSeed = s.tickSeeds[tick];

            if (cs.state == ClansmanState.TRAVELING && tick >= m.arrivalTick) {
                cs.state = ClansmanState.ACTING;
                cs.currentRegion = m.targetRegion;
                sim.simWorkerArrivedTick[index] = tick;
            }

            if (m.action == ActionType.DefendBase) continue;

            if (cs.state == ClansmanState.ACTING && tick >= m.settlesAtTick) {
                uint64 nonceBefore = m.nonce;
                (cs, m) = resolveAction(s, sim, cs, m, tick, tickSeed);
                if (!m.active) {
                    sim.simMissionCompletedNonce[index] = nonceBefore;
                }
                if (m.active && LibGameRules.actionDuration(m.action) > 0) {
                    m.settlesAtTick = LibSettlementMath.addTicksClamped(tick, LibGameRules.actionDuration(m.action));
                }
            }

            if (!m.active) break;
        }

        sim.clansmen[index] = cs;
        sim.missions[index] = m;
    }

    function resolveAction(
        LibStorage.AppStorage storage s,
        SettlementSimulation memory sim,
        Clansman memory cs,
        Mission memory m,
        uint64 tick,
        bytes32 tickSeed
    ) internal view returns (Clansman memory, Mission memory) {
        bool starving = sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= tick;
        ActionType action = m.action;

        if (action == ActionType.ChopWood) {
            (cs, m) = gatherWood(sim, cs, m, tick, starving, tickSeed);
        } else if (action == ActionType.MineIron) {
            (cs, m) = gatherIron(sim, cs, m, tick, starving, tickSeed);
        } else if (action == ActionType.FishDocks) {
            (cs, m) = gatherFish(sim, cs, m, tick, starving, tickSeed, ClanWorldConstants.FISH_DOCKS_BPS);
        } else if (action == ActionType.FishDeepSea) {
            (cs, m) = gatherFish(sim, cs, m, tick, starving, tickSeed, ClanWorldConstants.FISH_DEEP_BPS);
        } else if (action == ActionType.HarvestWheat) {
            (cs, m) = gatherWheat(sim, cs, m, tick, starving);
        } else if (action == ActionType.DepositResources) {
            (cs, m) = doDeposit(sim, cs, m, tick);
        } else if (action == ActionType.WithdrawResources) {
            (cs, m) = doWithdrawResources(s, sim, cs, m, tick);
        } else if (
            action == ActionType.UpgradeWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
        ) {
            (cs, m) = doBuilding(s, sim, cs, m, action, tick);
        } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
            (cs, m) = completeMission(cs, m);
        }

        return (cs, m);
    }

    function gatherWood(
        SettlementSimulation memory sim,
        Clansman memory cs,
        Mission memory m,
        uint64 tick,
        bool starving,
        bytes32 tickSeed
    ) internal view returns (Clansman memory, Mission memory) {
        uint256 remaining = ClanWorldConstants.WOOD_CAP - cs.carryWood;
        if (remaining == 0) return completeMission(cs, m);

        uint256 amount =
            ClanWorldConstants.WOOD_YIELD_PER_TICK * uint256(LibGameRules.actionDuration(ActionType.ChopWood));
        bytes32 critRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
        if (uint256(critRng) % 10000 < ClanWorldConstants.WOOD_CRIT_BPS) {
            amount *= 2;
        }
        if (starving) amount = amount / 2;
        if (amount > remaining) amount = remaining;
        cs.carryWood += amount;
        recordSettlementLog(
            sim, SettlementLogKind.ResourcesGathered, cs.clansmanId, ActionType.ChopWood, amount, 0, 0, 0, 0, tick
        );

        if (cs.carryWood >= ClanWorldConstants.WOOD_CAP) {
            return completeMission(cs, m);
        }
        return (cs, m);
    }

    function gatherIron(
        SettlementSimulation memory sim,
        Clansman memory cs,
        Mission memory m,
        uint64 tick,
        bool starving,
        bytes32 tickSeed
    ) internal view returns (Clansman memory, Mission memory) {
        uint256 remaining = ClanWorldConstants.IRON_CAP - cs.carryIron;
        if (remaining == 0) return completeMission(cs, m);

        uint256 amount =
            ClanWorldConstants.IRON_YIELD_PER_TICK * uint256(LibGameRules.actionDuration(ActionType.MineIron));
        if (starving) amount = amount / 2;
        if (amount > remaining) amount = remaining;
        cs.carryIron += amount;

        bytes32 goldRng = keccak256(abi.encode("iron_gold_bonus", tickSeed, cs.clansmanId, m.nonce, tick));
        uint256 goldBonus;
        if (uint256(goldRng) % 10000 < ClanWorldConstants.GOLD_FROM_IRON_BPS) {
            goldBonus = ClanWorldConstants.GOLD_FROM_IRON_AMOUNT;
            sim.clan.goldBalance += goldBonus;
        }
        recordSettlementLog(
            sim, SettlementLogKind.ResourcesGathered, cs.clansmanId, ActionType.MineIron, 0, amount, 0, 0, goldBonus, tick
        );

        if (cs.carryIron >= ClanWorldConstants.IRON_CAP) {
            return completeMission(cs, m);
        }
        return (cs, m);
    }

    function gatherFish(
        SettlementSimulation memory sim,
        Clansman memory cs,
        Mission memory m,
        uint64 tick,
        bool starving,
        bytes32 tickSeed,
        uint256 successBps
    ) internal view returns (Clansman memory, Mission memory) {
        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
        if (remaining == 0) return completeMission(cs, m);

        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
        uint256 amount = uint256(fishRng) % 10000 < successBps
            ? ClanWorldConstants.FISH_YIELD_PER_TICK * uint256(LibGameRules.actionDuration(m.action))
            : 0;
        if (starving) amount = amount / 2;
        if (amount > remaining) amount = remaining;
        if (amount > 0) {
            cs.carryFish += amount;
            recordSettlementLog(
                sim, SettlementLogKind.ResourcesGathered, cs.clansmanId, m.action, 0, 0, 0, amount, 0, tick
            );
        }

        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
            return completeMission(cs, m);
        }
        return (cs, m);
    }

    function gatherWheat(
        SettlementSimulation memory sim,
        Clansman memory cs,
        Mission memory m,
        uint64 tick,
        bool starving
    ) internal view returns (Clansman memory, Mission memory) {
        uint256 remaining = ClanWorldConstants.WHEAT_CAP - cs.carryWheat;
        if (remaining == 0) return completeMission(cs, m);

        uint256 plotIdx;
        if (m.targetRegion == ClanWorldConstants.REGION_WEST_FARMS) {
            plotIdx = 0;
        } else if (m.targetRegion == ClanWorldConstants.REGION_EAST_FARMS) {
            plotIdx = 1;
        } else {
            return completeMission(cs, m);
        }

        WheatPlot memory plot = sim.wheatPlots[plotIdx];
        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
            return completeMission(cs, m);
        }

        uint256 amount =
            ClanWorldConstants.WHEAT_YIELD_PER_TICK * uint256(LibGameRules.actionDuration(ActionType.HarvestWheat));
        if (starving) amount = amount / 2;
        if (amount > remaining) amount = remaining;
        if (amount > plot.remainingWheat) amount = plot.remainingWheat;

        cs.carryWheat += amount;
        plot.remainingWheat -= amount;
        recordSettlementLog(
            sim, SettlementLogKind.ResourcesGathered, cs.clansmanId, ActionType.HarvestWheat, 0, 0, amount, 0, 0, tick
        );

        if (plot.remainingWheat == 0) {
            plot.state = WheatPlotState.Regrowing;
            plot.regrowUntilTick = LibSettlementMath.addTicksClamped(tick, ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS);
        }
        sim.wheatPlots[plotIdx] = plot;

        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
            return completeMission(cs, m);
        }
        return (cs, m);
    }

    function doDeposit(SettlementSimulation memory sim, Clansman memory cs, Mission memory m, uint64 tick)
        internal
        view
        returns (Clansman memory, Mission memory)
    {
        if (cs.currentRegion != sim.clan.baseRegion) return completeMission(cs, m);

        bool hasAnything = cs.carryWood > 0 || cs.carryIron > 0 || cs.carryWheat > 0 || cs.carryFish > 0;
        if (!hasAnything) return completeMission(cs, m);

        uint256 wood = cs.carryWood;
        uint256 iron = cs.carryIron;
        uint256 wheat = cs.carryWheat;
        uint256 fish = cs.carryFish;
        recordSettlementLog(
            sim, SettlementLogKind.ResourcesDeposited, cs.clansmanId, m.action, wood, iron, wheat, fish, 0, tick
        );

        sim.clan.vaultWood += cs.carryWood;
        sim.clan.vaultIron += cs.carryIron;
        sim.clan.vaultWheat += cs.carryWheat;
        sim.clan.vaultFish += cs.carryFish;

        cs.carryWood = 0;
        cs.carryIron = 0;
        cs.carryWheat = 0;
        cs.carryFish = 0;

        return completeMission(cs, m);
    }

    function doWithdrawResources(
        LibStorage.AppStorage storage,
        SettlementSimulation memory sim,
        Clansman memory cs,
        Mission memory m,
        uint64 tick
    ) internal view returns (Clansman memory, Mission memory) {
        if (cs.currentRegion != sim.clan.baseRegion) return completeMission(cs, m);

        WithdrawResourcesData memory req = m.withdrawResources;
        if (!LibSettlementMath.hasWithdrawRequest(req)) return completeMission(cs, m);

        uint256 spendableWood = LibSettlementMath.spendableAfterReleasing(sim.clan.vaultWood, sim.reservedWood, 0);
        uint256 spendableIron = LibSettlementMath.spendableAfterReleasing(sim.clan.vaultIron, sim.reservedIron, 0);
        uint256 spendableWheat = sim.clan.vaultWheat > sim.reservedWheat ? sim.clan.vaultWheat - sim.reservedWheat : 0;

        if (
            spendableWood < req.wood || spendableIron < req.iron || spendableWheat < req.wheat
                || sim.clan.vaultFish < req.fish
        ) return completeMission(cs, m);

        if (
            req.wood > LibSettlementMath.remainingCapacity(cs.carryWood, ClanWorldConstants.WOOD_CAP)
                || req.iron > LibSettlementMath.remainingCapacity(cs.carryIron, ClanWorldConstants.IRON_CAP)
                || req.wheat > LibSettlementMath.remainingCapacity(cs.carryWheat, ClanWorldConstants.WHEAT_CAP)
                || req.fish > LibSettlementMath.remainingCapacity(cs.carryFish, ClanWorldConstants.FISH_CAP)
        ) return completeMission(cs, m);

        sim.clan.vaultWood -= req.wood;
        sim.clan.vaultIron -= req.iron;
        sim.clan.vaultWheat -= req.wheat;
        sim.clan.vaultFish -= req.fish;

        cs.carryWood += req.wood;
        cs.carryIron += req.iron;
        cs.carryWheat += req.wheat;
        cs.carryFish += req.fish;
        recordSettlementLog(
            sim,
            SettlementLogKind.ResourcesWithdrawn,
            cs.clansmanId,
            m.action,
            req.wood,
            req.iron,
            req.wheat,
            req.fish,
            0,
            tick
        );

        return completeMission(cs, m);
    }

    function doBuilding(
        LibStorage.AppStorage storage s,
        SettlementSimulation memory sim,
        Clansman memory cs,
        Mission memory m,
        ActionType action,
        uint64 tick
    ) internal view returns (Clansman memory, Mission memory) {
        bool finished = true;
        if (cs.currentRegion == sim.clan.baseRegion) {
            if (action == ActionType.UpgradeWall) {
                finished = settleWallUpgrade(s, sim, cs.clansmanId, m.nonce);
            } else if (action == ActionType.UpgradeBase) {
                finished = settleBaseUpgrade(s, sim, cs.clansmanId, m.nonce);
            } else if (action == ActionType.UpgradeMonument) {
                finished = settleMonumentUpgrade(s, sim, cs.clansmanId, m.nonce, tick);
            }
        }
        if (finished) return completeMission(cs, m);
        return (cs, m);
    }

    function settleWallUpgrade(
        LibStorage.AppStorage storage s,
        SettlementSimulation memory sim,
        uint32 clansmanId,
        uint64 missionNonce
    ) internal view returns (bool) {
        if (upgradeReservationCleared(sim, clansmanId, ActionType.UpgradeWall)) return true;
        LibStorage.WallUpgradeReservation memory held = s.wallUpgradeReservations[clansmanId];
        if (!held.active || held.clanId != sim.clan.clanId || held.missionNonce != missionNonce) return true;
        if (sim.clan.wallLevel >= WALL_MAX_LEVEL) {
            clearUpgradeReservation(sim, clansmanId, ActionType.UpgradeWall);
            sim.reservedWood = LibSettlementMath.subtractHeld(sim.reservedWood, held.woodCost);
            sim.reservedIron = LibSettlementMath.subtractHeld(sim.reservedIron, held.ironCost);
            return true;
        }
        if (held.fromLevel != sim.clan.wallLevel) {
            if (held.fromLevel < sim.clan.wallLevel) {
                clearUpgradeReservation(sim, clansmanId, ActionType.UpgradeWall);
                sim.reservedWood = LibSettlementMath.subtractHeld(sim.reservedWood, held.woodCost);
                sim.reservedIron = LibSettlementMath.subtractHeld(sim.reservedIron, held.ironCost);
            }
            return false;
        }

        (uint256 woodCost, uint256 ironCost) = LibGameRules.wallUpgradeCost(sim.clan.wallLevel);
        uint256 woodDebit = LibSettlementMath.min(held.woodCost, woodCost);
        uint256 ironDebit = LibSettlementMath.min(held.ironCost, ironCost);
        if (sim.clan.vaultWood < woodDebit || sim.clan.vaultIron < ironDebit) return false;

        clearUpgradeReservation(sim, clansmanId, ActionType.UpgradeWall);
        sim.clan.vaultWood -= woodDebit;
        sim.clan.vaultIron -= ironDebit;
        sim.reservedWood = LibSettlementMath.subtractHeld(sim.reservedWood, woodDebit);
        sim.reservedIron = LibSettlementMath.subtractHeld(sim.reservedIron, ironDebit);
        sim.clan.wallLevel += 1;
        return true;
    }

    function settleBaseUpgrade(
        LibStorage.AppStorage storage s,
        SettlementSimulation memory sim,
        uint32 clansmanId,
        uint64 missionNonce
    ) internal view returns (bool) {
        if (upgradeReservationCleared(sim, clansmanId, ActionType.UpgradeBase)) return true;
        LibStorage.BaseUpgradeReservation memory held = s.baseUpgradeReservations[clansmanId];
        if (!held.active || held.clanId != sim.clan.clanId || held.missionNonce != missionNonce) return true;
        if (sim.clan.baseLevel >= BASE_MAX_LEVEL) {
            clearUpgradeReservation(sim, clansmanId, ActionType.UpgradeBase);
            sim.reservedWood = LibSettlementMath.subtractHeld(sim.reservedWood, held.woodCost);
            sim.reservedIron = LibSettlementMath.subtractHeld(sim.reservedIron, held.ironCost);
            sim.reservedWheat = LibSettlementMath.subtractHeld(sim.reservedWheat, held.wheatCost);
            return true;
        }
        if (held.fromLevel != sim.clan.baseLevel) {
            if (held.fromLevel < sim.clan.baseLevel) {
                clearUpgradeReservation(sim, clansmanId, ActionType.UpgradeBase);
                sim.reservedWood = LibSettlementMath.subtractHeld(sim.reservedWood, held.woodCost);
                sim.reservedIron = LibSettlementMath.subtractHeld(sim.reservedIron, held.ironCost);
                sim.reservedWheat = LibSettlementMath.subtractHeld(sim.reservedWheat, held.wheatCost);
            }
            return false;
        }

        (uint256 woodCost, uint256 ironCost, uint256 wheatCost) = LibGameRules.baseUpgradeCost(sim.clan.baseLevel);
        uint256 woodDebit = LibSettlementMath.min(held.woodCost, woodCost);
        uint256 ironDebit = LibSettlementMath.min(held.ironCost, ironCost);
        uint256 wheatDebit = LibSettlementMath.min(held.wheatCost, wheatCost);
        if (sim.clan.vaultWood < woodDebit || sim.clan.vaultIron < ironDebit || sim.clan.vaultWheat < wheatDebit) {
            return false;
        }

        clearUpgradeReservation(sim, clansmanId, ActionType.UpgradeBase);
        sim.clan.vaultWood -= woodDebit;
        sim.clan.vaultIron -= ironDebit;
        sim.clan.vaultWheat -= wheatDebit;
        sim.reservedWood = LibSettlementMath.subtractHeld(sim.reservedWood, woodDebit);
        sim.reservedIron = LibSettlementMath.subtractHeld(sim.reservedIron, ironDebit);
        sim.reservedWheat = LibSettlementMath.subtractHeld(sim.reservedWheat, wheatDebit);
        sim.clan.baseLevel += 1;
        return true;
    }

    function settleMonumentUpgrade(
        LibStorage.AppStorage storage s,
        SettlementSimulation memory sim,
        uint32 clansmanId,
        uint64 missionNonce,
        uint64 tick
    ) internal view returns (bool) {
        if (upgradeReservationCleared(sim, clansmanId, ActionType.UpgradeMonument)) {
            return true;
        }
        LibStorage.MonumentUpgradeReservation memory held = s.monumentUpgradeReservations[clansmanId];
        if (!held.active || held.clanId != sim.clan.clanId || held.missionNonce != missionNonce) return true;
        if (sim.clan.monumentLevel >= LibGameRules.MONUMENT_MAX_LEVEL) {
            clearUpgradeReservation(sim, clansmanId, ActionType.UpgradeMonument);
            sim.reservedWood = LibSettlementMath.subtractHeld(sim.reservedWood, held.woodCost);
            sim.reservedIron = LibSettlementMath.subtractHeld(sim.reservedIron, held.ironCost);
            sim.reservedWheat = LibSettlementMath.subtractHeld(sim.reservedWheat, held.wheatCost);
            sim.reservedBlueprint = LibSettlementMath.subtractHeld(sim.reservedBlueprint, held.blueprintCost);
            return true;
        }
        if (held.fromLevel != sim.clan.monumentLevel) {
            if (held.fromLevel < sim.clan.monumentLevel) {
                clearUpgradeReservation(sim, clansmanId, ActionType.UpgradeMonument);
                sim.reservedWood = LibSettlementMath.subtractHeld(sim.reservedWood, held.woodCost);
                sim.reservedIron = LibSettlementMath.subtractHeld(sim.reservedIron, held.ironCost);
                sim.reservedWheat = LibSettlementMath.subtractHeld(sim.reservedWheat, held.wheatCost);
                sim.reservedBlueprint = LibSettlementMath.subtractHeld(sim.reservedBlueprint, held.blueprintCost);
            }
            return false;
        }

        (uint256 woodCost, uint256 ironCost, uint256 wheatCost, uint256 blueprintCost) =
            LibGameRules.monumentUpgradeCost(sim.clan.monumentLevel);
        uint256 woodDebit = LibSettlementMath.min(held.woodCost, woodCost);
        uint256 ironDebit = LibSettlementMath.min(held.ironCost, ironCost);
        uint256 wheatDebit = LibSettlementMath.min(held.wheatCost, wheatCost);
        uint256 blueprintDebit = LibSettlementMath.min(held.blueprintCost, blueprintCost);
        if (
            sim.clan.vaultWood < woodDebit || sim.clan.vaultIron < ironDebit || sim.clan.vaultWheat < wheatDebit
                || sim.clan.blueprintBalance < blueprintDebit
        ) return false;

        clearUpgradeReservation(sim, clansmanId, ActionType.UpgradeMonument);
        sim.clan.vaultWood -= woodDebit;
        sim.clan.vaultIron -= ironDebit;
        sim.clan.vaultWheat -= wheatDebit;
        sim.clan.blueprintBalance -= blueprintDebit;
        sim.reservedWood = LibSettlementMath.subtractHeld(sim.reservedWood, woodDebit);
        sim.reservedIron = LibSettlementMath.subtractHeld(sim.reservedIron, ironDebit);
        sim.reservedWheat = LibSettlementMath.subtractHeld(sim.reservedWheat, wheatDebit);
        sim.reservedBlueprint = LibSettlementMath.subtractHeld(sim.reservedBlueprint, blueprintDebit);
        sim.clan.monumentLevel += 1;
        sim.simMonumentReachedAt[sim.clan.monumentLevel] = tick;
        return true;
    }

    function upgradeReservationCleared(SettlementSimulation memory sim, uint32 clansmanId, ActionType action)
        internal
        pure
        returns (bool)
    {
        (uint256 index, bool found) = simClansmanIndex(sim, clansmanId);
        if (!found) return false;
        if (action == ActionType.UpgradeWall) return sim.simWallReservationCleared[index];
        if (action == ActionType.UpgradeBase) return sim.simBaseReservationCleared[index];
        if (action == ActionType.UpgradeMonument) return sim.simMonumentReservationCleared[index];
        return false;
    }

    function clearUpgradeReservation(SettlementSimulation memory sim, uint32 clansmanId, ActionType action)
        internal
        pure
    {
        (uint256 index, bool found) = simClansmanIndex(sim, clansmanId);
        if (!found) return;
        if (action == ActionType.UpgradeWall) {
            sim.simWallReservationCleared[index] = true;
        } else if (action == ActionType.UpgradeBase) {
            sim.simBaseReservationCleared[index] = true;
        } else if (action == ActionType.UpgradeMonument) {
            sim.simMonumentReservationCleared[index] = true;
        }
    }

    function refundSimUpgradeReservation(
        LibStorage.AppStorage storage s,
        SettlementSimulation memory sim,
        uint256 index,
        ActionType action
    ) internal view {
        uint32 clansmanId = sim.clansmen[index].clansmanId;
        if (action == ActionType.UpgradeWall) {
            LibStorage.WallUpgradeReservation memory held = s.wallUpgradeReservations[clansmanId];
            if (!held.active || held.clanId != sim.clan.clanId) return;
            sim.simWallReservationCleared[index] = true;
            sim.reservedWood = LibSettlementMath.subtractHeld(sim.reservedWood, held.woodCost);
            sim.reservedIron = LibSettlementMath.subtractHeld(sim.reservedIron, held.ironCost);
        } else if (action == ActionType.UpgradeBase) {
            LibStorage.BaseUpgradeReservation memory held = s.baseUpgradeReservations[clansmanId];
            if (!held.active || held.clanId != sim.clan.clanId) return;
            sim.simBaseReservationCleared[index] = true;
            sim.reservedWood = LibSettlementMath.subtractHeld(sim.reservedWood, held.woodCost);
            sim.reservedIron = LibSettlementMath.subtractHeld(sim.reservedIron, held.ironCost);
            sim.reservedWheat = LibSettlementMath.subtractHeld(sim.reservedWheat, held.wheatCost);
        } else if (action == ActionType.UpgradeMonument) {
            LibStorage.MonumentUpgradeReservation memory held = s.monumentUpgradeReservations[clansmanId];
            if (!held.active || held.clanId != sim.clan.clanId) return;
            sim.simMonumentReservationCleared[index] = true;
            sim.reservedWood = LibSettlementMath.subtractHeld(sim.reservedWood, held.woodCost);
            sim.reservedIron = LibSettlementMath.subtractHeld(sim.reservedIron, held.ironCost);
            sim.reservedWheat = LibSettlementMath.subtractHeld(sim.reservedWheat, held.wheatCost);
            sim.reservedBlueprint = LibSettlementMath.subtractHeld(sim.reservedBlueprint, held.blueprintCost);
        }
    }

    function simClansmanIndex(SettlementSimulation memory sim, uint32 clansmanId)
        internal
        pure
        returns (uint256, bool)
    {
        for (uint256 i = 0; i < sim.clansmen.length; i++) {
            if (sim.clansmen[i].clansmanId == clansmanId) return (i, true);
        }
        return (0, false);
    }

    function completeMission(Clansman memory cs, Mission memory m)
        internal
        pure
        returns (Clansman memory, Mission memory)
    {
        cs.state = ClansmanState.WAITING;
        m.active = false;
        return (cs, m);
    }

    function releaseDefendersForDeadTarget(LibStorage.AppStorage storage s, uint32 deadClanId, uint8 baseRegion) internal {
        for (uint256 i = 0; i < s.allClanIds.length; i++) {
            uint32 defenderClanId = s.allClanIds[i];
            if (defenderClanId == deadClanId) continue;

            uint32[] storage csIds = s.clanClansmanIds[defenderClanId];
            for (uint256 j = 0; j < csIds.length; j++) {
                uint32 clansmanId = csIds[j];
                Mission storage mission = s.missions[clansmanId];
                if (
                    mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == deadClanId
                ) {
                    if (s.clansmanDefendingRegion[clansmanId] == baseRegion) {
                        LibOrderDefenders.clearDefender(s, clansmanId);
                    }
                    mission.active = false;

                    Clansman storage defender = s.clansmen[clansmanId];
                    if (defender.state != ClansmanState.DEAD) {
                        defender.state = ClansmanState.WAITING;
                    }
                }
            }
        }
    }

    function abortBanditAttacksForDeadTarget(
        LibStorage.AppStorage storage s,
        uint32 deadClanId,
        uint32 excludedBanditId,
        uint64 tick
    ) internal {
        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
            uint32[] storage regionBandits = s.banditsByRegion[region];
            for (uint256 i = 0; i < regionBandits.length; i++) {
                uint32 banditId = regionBandits[i];
                if (banditId == excludedBanditId) continue;

                BanditTroop storage bandit = s.bandits[banditId];
                if (bandit.state == BanditState.Attacking && bandit.targetClanId == deadClanId) {
                    LibBanditLifecycle.transitionBanditState(s, banditId, BanditState.Escaped);
                    emit BanditEscaped(banditId, tick);
                    emit BanditTargetDied(banditId, deadClanId, tick);
                }
            }
        }
    }

    function clearWallUpgradeReservation(LibStorage.AppStorage storage s, uint32 clansmanId) internal {
        LibStorage.WallUpgradeReservation storage reservation = s.wallUpgradeReservations[clansmanId];
        if (!reservation.active) return;

        uint32 clanId = reservation.clanId;
        if (s.pendingWallUpgradesByClan[clanId] > 0) {
            s.pendingWallUpgradesByClan[clanId] -= 1;
        }
        s.reservedWoodByClan[clanId] =
            LibSettlementMath.subtractHeld(s.reservedWoodByClan[clanId], reservation.woodCost);
        s.reservedIronByClan[clanId] =
            LibSettlementMath.subtractHeld(s.reservedIronByClan[clanId], reservation.ironCost);

        delete s.wallUpgradeReservations[clansmanId];
    }

    function clearBaseUpgradeReservation(LibStorage.AppStorage storage s, uint32 clansmanId) internal {
        LibStorage.BaseUpgradeReservation storage reservation = s.baseUpgradeReservations[clansmanId];
        if (!reservation.active) return;

        uint32 clanId = reservation.clanId;
        if (s.pendingBaseUpgradesByClan[clanId] > 0) {
            s.pendingBaseUpgradesByClan[clanId] -= 1;
        }
        s.reservedWoodByClan[clanId] =
            LibSettlementMath.subtractHeld(s.reservedWoodByClan[clanId], reservation.woodCost);
        s.reservedIronByClan[clanId] =
            LibSettlementMath.subtractHeld(s.reservedIronByClan[clanId], reservation.ironCost);
        s.reservedWheatByClan[clanId] =
            LibSettlementMath.subtractHeld(s.reservedWheatByClan[clanId], reservation.wheatCost);

        delete s.baseUpgradeReservations[clansmanId];
    }

    function clearMonumentUpgradeReservation(LibStorage.AppStorage storage s, uint32 clansmanId) internal {
        LibStorage.MonumentUpgradeReservation storage reservation = s.monumentUpgradeReservations[clansmanId];
        if (!reservation.active) return;

        uint32 clanId = reservation.clanId;
        if (s.pendingMonumentUpgradesByClan[clanId] > 0) {
            s.pendingMonumentUpgradesByClan[clanId] -= 1;
        }
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
}
