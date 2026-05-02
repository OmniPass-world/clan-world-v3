// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {
    Clan,
    ClanState,
    ClanWorldConstants,
    Clansman,
    ClansmanState,
    Mission,
    WheatPlot,
    WheatPlotState
} from "../../IClanWorld.sol";
import {RNG} from "../../lib/RNG.sol";
import {LibStorage} from "./LibStorage.sol";
import {LibSeason} from "./LibSeason.sol";
import {LibSettlementMath} from "./LibSettlementMath.sol";

library LibSettlement {
    struct SettlementSimulation {
        Clan clan;
        Clansman[] clansmen;
        Mission[] missions;
        WheatPlot[2] wheatPlots;
        uint64[11] simMonumentReachedAt;
        bool[] simWallReservationCleared;
        bool[] simBaseReservationCleared;
        bool[] simMonumentReservationCleared;
        uint256 reservedWheat;
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

        for (uint256 i = 0; i < clansmanIds.length; i++) {
            uint32 clansmanId = clansmanIds[i];
            sim.clansmen[i] = s.clansmen[clansmanId];
            sim.missions[i] = s.missions[clansmanId];
        }

        sim.wheatPlots[0] = s.wheatPlots[clanId][0];
        sim.wheatPlots[1] = s.wheatPlots[clanId][1];
        sim.reservedWheat = s.reservedWheatByClan[clanId];
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

        for (uint64 tick = fromTick; tick < toTick; tick++) {
            applyUpkeep(s, sim, tick);
            if (sim.clan.clanState == ClanState.DEAD) break;
            regrowWheatPlots(sim, tick);
        }

        if (toTick > fromTick && !LibSeason.isWinterActiveAt(toTick) && LibSeason.isWinterActiveAt(toTick - 1)) {
            sim.clan.coldDamage = 0;
        }

        sim.clan.lastSettledTick = toTick;
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
                killNextClansmanFromStarvation(sim);
            }
        }
        if (sim.clan.clanState == ClanState.DEAD) return;

        if (winter) {
            uint256 woodNeeded = ClanWorldConstants.WINTER_WOOD_BURN_PER_BASE + uint256(livingBeforeStarvation)
                * ClanWorldConstants.WINTER_WOOD_BURN_PER_CLANSMAN;
            uint256 spendableWood =
                LibSettlementMath.spendableAfterReleasing(sim.clan.vaultWood, s.reservedWoodByClan[sim.clan.clanId], 0);
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

            markClansmanDead(sim, i);
            if (sim.clan.livingClansmen == 0) {
                markClanDead(sim);
            }
            return;
        }
    }

    function killNextClansmanFromStarvation(SettlementSimulation memory sim) internal pure {
        if (sim.clan.livingClansmen == 0) return;

        for (uint256 i = 0; i < sim.clansmen.length; i++) {
            if (sim.clansmen[i].state == ClansmanState.DEAD) continue;

            markClansmanDead(sim, i);
            if (sim.clan.livingClansmen == 0) {
                markClanDead(sim);
            }
            return;
        }
    }

    function markClansmanDead(SettlementSimulation memory sim, uint256 index) internal pure {
        if (sim.clansmen[index].state == ClansmanState.DEAD) return;

        sim.clansmen[index].state = ClansmanState.DEAD;
        sim.clansmen[index].cooldownEndsAtTs = 0;
        if (sim.clan.livingClansmen > 0) {
            sim.clan.livingClansmen--;
        }
        if (sim.missions[index].active) {
            sim.missions[index].active = false;
        }
    }

    function markClanDead(SettlementSimulation memory sim) internal pure {
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
                sim.missions[i].active = false;
            }
        }
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
}
