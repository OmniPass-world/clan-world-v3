// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {
    ActionType,
    BanditState,
    BanditTroop,
    Clan,
    Clansman,
    ClansmanState,
    ClanState,
    ClanWorldConstants,
    DefenseContribution,
    Mission
} from "../../IClanWorld.sol";
import {LibSettlementMath} from "./LibSettlementMath.sol";
import {LibBanditLifecycle} from "./LibBanditLifecycle.sol";
import {LibOrderDefenders} from "./LibOrderDefenders.sol";
import {LibOrderUpgrades} from "./LibOrderUpgrades.sol";
import {LibStorage} from "./LibStorage.sol";

library LibBanditCombat {
    uint256 internal constant RESOURCE_UNIT = 1e18;
    uint256 internal constant BLUEPRINT_UNIT = 1e18;
    uint32 internal constant DEFEND_BASE_DEFENSE = 10;
    uint32 internal constant WAITING_HOME_DEFENSE = 5;
    uint32 internal constant WALL_HP_PER_LEVEL = 100;
    uint32 internal constant BASE_HP_PER_LEVEL = 25;
    uint32 internal constant CLANSMAN_HP = 100;

    event BanditAttackResolved(
        uint32 indexed banditId,
        uint32 indexed targetClanId,
        bool defended,
        uint16 attackPower,
        uint16 totalDefense,
        uint16 wallLevelAfter,
        uint256 stolenWood,
        uint256 stolenIron,
        uint256 stolenWheat,
        uint256 stolenFish,
        uint64 atTick
    );
    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
    event BanditEscaped(uint32 indexed banditId, uint64 atTick);
    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
    event WallDamagedByBandit(uint32 indexed clanId, uint8 newLevel, uint32 indexed banditId);
    event ClansmanKilledByBandit(uint32 indexed clanId, uint32 indexed clansmanId, uint32 indexed banditId);
    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint256 amount, uint64 tick);
    event LootDistributed(
        uint32 indexed banditId,
        uint32[] clanIdsRewarded,
        uint256 perClanWood,
        uint256 perClanWheat,
        uint256 perClanFish,
        uint256 perClanIron,
        uint256 perClanGold,
        uint256 burnedWood,
        uint256 burnedWheat,
        uint256 burnedFish,
        uint256 burnedIron,
        uint256 burnedGold
    );
    event LootDistributedToDefender(
        uint32 indexed banditId,
        uint32 indexed clanId,
        uint32 indexed clansmanId,
        uint256 wood,
        uint256 iron,
        uint256 wheat,
        uint256 fish
    );
    event ClanEliminated(uint32 indexed clanId, uint64 indexed tick);
    event ClanDied(uint32 indexed clanId, uint64 tick, string reason);

    function resolveAttackingBandits(LibStorage.AppStorage storage s, uint64 closedTick) public {
        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
            uint32[] storage regionBandits = s.banditsByRegion[region];
            uint256 i = 0;
            while (i < regionBandits.length) {
                uint32 banditId = regionBandits[i];
                BanditTroop storage bandit = s.bandits[banditId];
                bool shouldResolve = bandit.state == BanditState.Attacking && bandit.tickEnteredState == closedTick;
                if (shouldResolve) {
                    resolveBanditAttack(s, banditId, closedTick);
                    if (s.bandits[banditId].id == ClanWorldConstants.BANDIT_ID_NULL) {
                        continue;
                    }
                }
                i++;
            }
        }
    }

    function resolveBanditAttack(LibStorage.AppStorage storage s, uint32 banditId, uint64 closedTick) public {
        require(s.world.currentTick == closedTick, "ClanWorld: bandit attack tick mismatch");

        BanditTroop storage bandit = s.bandits[banditId];
        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state != BanditState.Attacking) return;
        if (bandit.tickEnteredState != closedTick) return;

        uint32 targetClanId = bandit.targetClanId;
        Clan storage targetClan = s.clans[targetClanId];
        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
            LibBanditLifecycle.transitionBanditState(s, banditId, BanditState.Camped);
            emit BanditEscaped(banditId, closedTick);
            return;
        }
        if (targetClan.lastSettledTick < s.world.currentTick) {
            LibBanditLifecycle.transitionBanditState(s, banditId, BanditState.Camped);
            return;
        }

        bytes32 tickSeed = s.world.currentTickSeed;
        uint32 banditAttackPower = bandit.strength;
        uint32 totalClansmanDefense = totalBanditClansmanDefense(s, targetClanId);
        bool defeated = uint256(totalClansmanDefense) >= uint256(banditAttackPower) * 2;

        uint32 wallDamage;
        uint32 baseAbsorbed;
        uint32 clansmanDamageAbsorbed;
        uint256 stolenWood;
        uint256 stolenIron;
        uint256 stolenWheat;
        uint256 stolenFish;
        if (!defeated) {
            (stolenWood, stolenIron, stolenWheat, stolenFish) = stealBanditVaultLoot(s, bandit, targetClan);
            uint32 incomingDamage =
                banditAttackPower > totalClansmanDefense ? banditAttackPower - totalClansmanDefense : 0;
            (incomingDamage, wallDamage) = applyBanditWallDamage(targetClan, targetClanId, banditId, incomingDamage);
            (incomingDamage, baseAbsorbed) = applyBanditBaseDefense(targetClan, incomingDamage);
            clansmanDamageAbsorbed =
                applyBanditClansmanCasualties(s, targetClan, targetClanId, banditId, incomingDamage, tickSeed);
        }

        uint32 totalDefense = totalClansmanDefense + wallDamage + baseAbsorbed + clansmanDamageAbsorbed;
        emit BanditAttackResolved(
            banditId,
            targetClanId,
            defeated,
            uint16Clamp(banditAttackPower),
            uint16Clamp(totalDefense),
            targetClan.wallLevel,
            stolenWood,
            stolenIron,
            stolenWheat,
            stolenFish,
            closedTick
        );

        if (defeated) {
            emit BanditDefeated(banditId, targetClanId, closedTick);
            distributeBanditLootToDefendingClans(s, banditId, targetClanId);
            targetClan.blueprintBalance += BLUEPRINT_UNIT;
            targetClan.goldBalance += 1e18;
            emit BlueprintEarned(targetClanId, banditId, BLUEPRINT_UNIT, closedTick);
            LibBanditLifecycle.transitionBanditState(s, banditId, BanditState.Defeated);
        } else if (
            LibBanditLifecycle.recordBanditAttackAttempt(s, banditId) >= ClanWorldConstants.BANDIT_MAX_ATTACK_ATTEMPTS
        ) {
            LibBanditLifecycle.terminalEscapeBandit(s, banditId, closedTick);
        } else {
            LibBanditLifecycle.transitionBanditState(s, banditId, BanditState.Camped);
        }
    }

    function stealBanditVaultLoot(LibStorage.AppStorage storage s, BanditTroop storage bandit, Clan storage targetClan)
        public
        returns (uint256 stolenWood, uint256 stolenIron, uint256 stolenWheat, uint256 stolenFish)
    {
        uint32 targetClanId = targetClan.clanId;
        uint256 spendableWood =
            LibSettlementMath.spendableAfterReleasing(targetClan.vaultWood, s.reservedWoodByClan[targetClanId], 0);
        uint256 spendableIron =
            LibSettlementMath.spendableAfterReleasing(targetClan.vaultIron, s.reservedIronByClan[targetClanId], 0);
        uint256 spendableWheat =
            LibSettlementMath.spendableAfterReleasing(targetClan.vaultWheat, s.reservedWheatByClan[targetClanId], 0);

        stolenWood = banditStealAmount(spendableWood);
        stolenIron = banditStealAmount(spendableIron);
        stolenWheat = banditStealAmount(spendableWheat);
        stolenFish = banditStealAmount(targetClan.vaultFish);

        targetClan.vaultWood -= stolenWood;
        targetClan.vaultIron -= stolenIron;
        targetClan.vaultWheat -= stolenWheat;
        targetClan.vaultFish -= stolenFish;

        bandit.carryWood += stolenWood;
        bandit.carryIron += stolenIron;
        bandit.carryWheat += stolenWheat;
        bandit.carryFish += stolenFish;
    }

    function totalBanditClansmanDefense(LibStorage.AppStorage storage s, uint32 targetClanId)
        public
        view
        returns (uint32 totalDefense)
    {
        DefenseContribution[] memory contributions = banditDefenseContributions(s, targetClanId);
        for (uint256 i = 0; i < contributions.length; i++) {
            totalDefense += contributions[i].defensePoints;
        }
    }

    function banditDefenseContributions(LibStorage.AppStorage storage s, uint32 targetClanId)
        public
        view
        returns (DefenseContribution[] memory contributions)
    {
        uint256 count = countBanditDefenseContributions(s, targetClanId);
        contributions = new DefenseContribution[](count);
        uint256 out;

        uint8 targetRegion = s.clans[targetClanId].baseRegion;
        uint32[] storage defendingClans = s.defendingClansByRegion[targetRegion];
        for (uint256 i = 0; i < defendingClans.length; i++) {
            uint32 defenderClanId = defendingClans[i];
            Clan storage defenderClan = s.clans[defenderClanId];
            if (defenderClan.clanState == ClanState.DEAD || isStarving(s, defenderClan)) continue;

            uint32[] storage clansmanIds = s.clanClansmanIds[defenderClanId];
            for (uint256 j = 0; j < clansmanIds.length; j++) {
                uint32 clansmanId = clansmanIds[j];
                Clansman storage cs = s.clansmen[clansmanId];
                Mission storage mission = s.missions[clansmanId];
                if (isExplicitBanditDefender(cs, mission, targetClanId, targetRegion)) {
                    contributions[out++] = DefenseContribution({
                        clansmanId: clansmanId, clanId: defenderClanId, defensePoints: uint16(DEFEND_BASE_DEFENSE)
                    });
                }
            }
        }

        Clan storage targetClan = s.clans[targetClanId];
        if (targetClan.clanState != ClanState.DEAD && !isStarving(s, targetClan)) {
            uint32[] storage targetClansmen = s.clanClansmanIds[targetClanId];
            for (uint256 i = 0; i < targetClansmen.length; i++) {
                uint32 clansmanId = targetClansmen[i];
                Clansman storage cs = s.clansmen[clansmanId];
                if (cs.state == ClansmanState.WAITING && cs.currentRegion == targetRegion) {
                    contributions[out++] = DefenseContribution({
                        clansmanId: clansmanId, clanId: targetClanId, defensePoints: uint16(WAITING_HOME_DEFENSE)
                    });
                }
            }
        }
    }

    function countBanditDefenseContributions(LibStorage.AppStorage storage s, uint32 targetClanId)
        public
        view
        returns (uint256 count)
    {
        uint8 targetRegion = s.clans[targetClanId].baseRegion;
        uint32[] storage defendingClans = s.defendingClansByRegion[targetRegion];
        for (uint256 i = 0; i < defendingClans.length; i++) {
            uint32 defenderClanId = defendingClans[i];
            Clan storage defenderClan = s.clans[defenderClanId];
            if (defenderClan.clanState == ClanState.DEAD || isStarving(s, defenderClan)) continue;

            uint32[] storage clansmanIds = s.clanClansmanIds[defenderClanId];
            for (uint256 j = 0; j < clansmanIds.length; j++) {
                Clansman storage cs = s.clansmen[clansmanIds[j]];
                Mission storage mission = s.missions[clansmanIds[j]];
                if (isExplicitBanditDefender(cs, mission, targetClanId, targetRegion)) count++;
            }
        }

        Clan storage targetClan = s.clans[targetClanId];
        if (targetClan.clanState == ClanState.DEAD || isStarving(s, targetClan)) return count;

        uint32[] storage targetClansmen = s.clanClansmanIds[targetClanId];
        for (uint256 i = 0; i < targetClansmen.length; i++) {
            Clansman storage cs = s.clansmen[targetClansmen[i]];
            if (cs.state == ClansmanState.WAITING && cs.currentRegion == targetRegion) count++;
        }
    }

    function applyBanditWallDamage(Clan storage clan, uint32 clanId, uint32 banditId, uint32 incomingDamage)
        public
        returns (uint32 remainingDamage, uint32 wallDamage)
    {
        remainingDamage = incomingDamage;
        if (remainingDamage == 0 || clan.wallLevel == 0) return (remainingDamage, 0);

        wallDamage = remainingDamage < WALL_HP_PER_LEVEL ? remainingDamage : WALL_HP_PER_LEVEL;
        remainingDamage -= wallDamage;
        if (wallDamage >= WALL_HP_PER_LEVEL) {
            if (clan.wallLevel > 0) clan.wallLevel--;
            emit WallDamagedByBandit(clanId, clan.wallLevel, banditId);
        }
    }

    function applyBanditBaseDefense(Clan storage clan, uint32 incomingDamage)
        public
        view
        returns (uint32 remainingDamage, uint32 baseAbsorbed)
    {
        remainingDamage = incomingDamage;
        if (remainingDamage == 0 || clan.baseLevel == 0) return (remainingDamage, 0);

        uint32 baseDefense = uint32(clan.baseLevel) * BASE_HP_PER_LEVEL;
        baseAbsorbed = remainingDamage < baseDefense ? remainingDamage : baseDefense;
        remainingDamage -= baseAbsorbed;
    }

    function applyBanditClansmanCasualties(
        LibStorage.AppStorage storage s,
        Clan storage clan,
        uint32 clanId,
        uint32 banditId,
        uint32 incomingDamage,
        bytes32 tickSeed
    ) public returns (uint32 damageAbsorbed) {
        uint32 remainingDamage = incomingDamage;
        uint32 killIndex = 0;
        while (remainingDamage > 0) {
            uint32 victimId = pickBanditClansmanVictim(s, clanId, banditId, killIndex, tickSeed);
            if (victimId == 0) break;

            Clansman storage victim = s.clansmen[victimId];
            markClansmanDead(s, clan, victim);

            uint32 absorbed = remainingDamage < CLANSMAN_HP ? remainingDamage : CLANSMAN_HP;
            damageAbsorbed += absorbed;
            remainingDamage -= absorbed;
            emit ClansmanKilledByBandit(clanId, victimId, banditId);
            killIndex++;

            if (clan.livingClansmen == 0) {
                markClanDead(s, clanId, "bandit", s.world.currentTick, banditId);
                break;
            }
        }
    }

    function distributeBanditLootToDefendingClans(LibStorage.AppStorage storage s, uint32 banditId, uint32 targetClanId)
        public
    {
        BanditTroop storage bandit = s.bandits[banditId];
        DefenseContribution[] memory contributions = banditDefenseContributions(s, targetClanId);
        uint256 nDefenders = contributions.length;
        uint32[] memory rewardedClanIds = new uint32[](nDefenders);

        uint256 dropWood = banditLootDrop(bandit.carryWood);
        uint256 dropIron = banditLootDrop(bandit.carryIron);
        uint256 dropWheat = banditLootDrop(bandit.carryWheat);
        uint256 dropFish = banditLootDrop(bandit.carryFish);
        uint256 dropGold = banditLootDrop(bandit.carryGold);

        uint256 perWood;
        uint256 perIron;
        uint256 perWheat;
        uint256 perFish;
        uint256 perGold;
        uint256 distributedWood;
        uint256 distributedIron;
        uint256 distributedWheat;
        uint256 distributedFish;
        uint256 distributedGold;
        if (nDefenders > 0) {
            perWood = perDefenderBanditLootShare(dropWood, nDefenders);
            perIron = perDefenderBanditLootShare(dropIron, nDefenders);
            perWheat = perDefenderBanditLootShare(dropWheat, nDefenders);
            perFish = perDefenderBanditLootShare(dropFish, nDefenders);
            perGold = perDefenderBanditLootShare(dropGold, nDefenders);

            for (uint256 i = 0; i < contributions.length; i++) {
                uint32 clansmanId = contributions[i].clansmanId;
                uint32 clanId = contributions[i].clanId;
                rewardedClanIds[i] = clanId;

                Clansman storage defender = s.clansmen[clansmanId];
                uint256 addedWood = addClansmanCarryCapped(defender.carryWood, perWood, ClanWorldConstants.WOOD_CAP);
                uint256 addedIron = addClansmanCarryCapped(defender.carryIron, perIron, ClanWorldConstants.IRON_CAP);
                uint256 addedWheat = addClansmanCarryCapped(defender.carryWheat, perWheat, ClanWorldConstants.WHEAT_CAP);
                uint256 addedFish = addClansmanCarryCapped(defender.carryFish, perFish, ClanWorldConstants.FISH_CAP);

                defender.carryWood += addedWood;
                defender.carryIron += addedIron;
                defender.carryWheat += addedWheat;
                defender.carryFish += addedFish;
                s.clans[clanId].goldBalance += perGold;

                distributedWood += addedWood;
                distributedIron += addedIron;
                distributedWheat += addedWheat;
                distributedFish += addedFish;
                distributedGold += perGold;

                emit LootDistributedToDefender(
                    banditId, clanId, clansmanId, addedWood, addedIron, addedWheat, addedFish
                );
            }
        }

        emit LootDistributed(
            banditId,
            rewardedClanIds,
            perWood,
            perWheat,
            perFish,
            perIron,
            perGold,
            bandit.carryWood - distributedWood,
            bandit.carryWheat - distributedWheat,
            bandit.carryFish - distributedFish,
            bandit.carryIron - distributedIron,
            bandit.carryGold - distributedGold
        );
    }

    function pickBanditClansmanVictim(
        LibStorage.AppStorage storage s,
        uint32 clanId,
        uint32 banditId,
        uint32 killIndex,
        bytes32 tickSeed
    ) public view returns (uint32 victimId) {
        uint32[] storage clansmanIds = s.clanClansmanIds[clanId];
        uint256 livingCount;
        for (uint256 i = 0; i < clansmanIds.length; i++) {
            if (s.clansmen[clansmanIds[i]].state != ClansmanState.DEAD) livingCount++;
        }
        if (livingCount == 0) return 0;

        uint256 pick =
            uint256(keccak256(abi.encode("bandit_clansman_kill", tickSeed, banditId, clanId, killIndex))) % livingCount;
        uint256 seen;
        for (uint256 i = 0; i < clansmanIds.length; i++) {
            uint32 candidateId = clansmanIds[i];
            if (s.clansmen[candidateId].state == ClansmanState.DEAD) continue;
            if (seen == pick) return candidateId;
            seen++;
        }
    }

    function markClansmanDead(LibStorage.AppStorage storage s, Clan storage clan, Clansman storage cs) public {
        if (cs.state == ClansmanState.DEAD) return;

        cs.state = ClansmanState.DEAD;
        cs.cooldownEndsAtTs = 0;
        if (clan.livingClansmen > 0) clan.livingClansmen--;

        Mission storage m = s.missions[cs.clansmanId];
        if (m.active) {
            if (m.action == ActionType.DefendBase) {
                LibOrderDefenders.clearDefender(s, cs.clansmanId);
            }
            LibOrderUpgrades.refundUpgradeReservation(s, cs.clansmanId, m.action);
            m.active = false;
        }
    }

    function markClanDead(LibStorage.AppStorage storage s, uint32 clanId, string memory reason, uint64 tick) public {
        markClanDead(s, clanId, reason, tick, ClanWorldConstants.BANDIT_ID_NULL);
    }

    function markClanDead(
        LibStorage.AppStorage storage s,
        uint32 clanId,
        string memory reason,
        uint64 tick,
        uint32 excludedBanditId
    ) public {
        Clan storage clan = s.clans[clanId];
        if (clan.clanId == ClanWorldConstants.CLAN_ID_NULL || clan.clanState == ClanState.DEAD) return;

        uint8 baseRegion = clan.baseRegion;
        clan.clanState = ClanState.DEAD;
        clan.vaultWood = 0;
        clan.vaultWheat = 0;
        clan.vaultFish = 0;
        clan.vaultIron = 0;
        clan.starvationStartsAtTick = 0;
        clan.livingClansmen = 0;

        uint32[] storage csIds = s.clanClansmanIds[clanId];
        for (uint256 i = 0; i < csIds.length; i++) {
            Clansman storage cs = s.clansmen[csIds[i]];
            cs.state = ClansmanState.DEAD;
            cs.cooldownEndsAtTs = 0;
            Mission storage m = s.missions[csIds[i]];
            if (m.active) {
                if (m.action == ActionType.DefendBase) {
                    LibOrderDefenders.clearDefender(s, csIds[i]);
                }
                LibOrderUpgrades.refundUpgradeReservation(s, csIds[i], m.action);
                m.active = false;
            }
        }

        releaseDefendersForDeadTarget(s, clanId, baseRegion);
        abortBanditAttacksForDeadTarget(s, clanId, excludedBanditId, tick);

        emit ClanEliminated(clanId, tick);
        emit ClanDied(clanId, tick, reason);
    }

    function releaseDefendersForDeadTarget(LibStorage.AppStorage storage s, uint32 deadClanId, uint8 baseRegion)
        internal
    {
        for (uint256 i = 0; i < s.allClanIds.length; i++) {
            uint32 defenderClanId = s.allClanIds[i];
            if (defenderClanId == deadClanId) continue;

            uint32[] storage csIds = s.clanClansmanIds[defenderClanId];
            for (uint256 j = 0; j < csIds.length; j++) {
                uint32 clansmanId = csIds[j];
                Mission storage mission = s.missions[clansmanId];
                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == deadClanId) {
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
                    LibBanditLifecycle.transitionBanditState(s, banditId, BanditState.Camped);
                    emit BanditEscaped(banditId, tick);
                    emit BanditTargetDied(banditId, deadClanId, tick);
                }
            }
        }
    }

    function isExplicitBanditDefender(
        Clansman storage cs,
        Mission storage mission,
        uint32 targetClanId,
        uint8 targetRegion
    ) public view returns (bool) {
        return cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
            && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId;
    }

    function isStarving(LibStorage.AppStorage storage s, Clan storage clan) public view returns (bool) {
        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= s.world.currentTick;
    }

    function banditStealAmount(uint256 vaultAmount) public pure returns (uint256) {
        return (vaultAmount * ClanWorldConstants.BANDIT_BASE_STEAL_BPS) / 10000;
    }

    function banditLootDrop(uint256 carryAmount) public pure returns (uint256) {
        return (carryAmount * ClanWorldConstants.BANDIT_DROP_TO_DEFENDERS_BPS) / 10000;
    }

    function perDefenderBanditLootShare(uint256 loot, uint256 nDefenders) public pure returns (uint256) {
        if (nDefenders == 1) return loot;
        return ((loot / RESOURCE_UNIT) / nDefenders) * RESOURCE_UNIT;
    }

    function addClansmanCarryCapped(uint256 currentCarry, uint256 amount, uint256 carryCap)
        public
        pure
        returns (uint256)
    {
        if (currentCarry >= carryCap) return 0;
        uint256 remaining = carryCap - currentCarry;
        return amount < remaining ? amount : remaining;
    }

    function uint16Clamp(uint32 value) public pure returns (uint16) {
        if (value > type(uint16).max) return type(uint16).max;
        return uint16(value);
    }
}
