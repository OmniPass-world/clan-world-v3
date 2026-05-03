// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {
    BanditState,
    BanditTroop,
    Clan,
    Clansman,
    ClansmanState,
    ClanState,
    ClanWorldConstants
} from "../../IClanWorld.sol";
import {LibScoring} from "../../lib/LibScoring.sol";
import {RNG} from "../../lib/RNG.sol";
import {LibStorage} from "./LibStorage.sol";

library LibBanditSpawning {
    uint64 internal constant MIN_SPAWN_COOLDOWN_TICKS = ClanWorldConstants.BANDIT_COOLDOWN_TICKS;
    uint16 internal constant BANDIT_SPAWN_PROBABILITY_INCREMENT_BPS = 1000;
    uint16 internal constant BANDIT_SPAWN_MAX_PROBABILITY_BPS = 8000;
    uint8 internal constant MAX_BANDITS_PER_REGION = 1;
    uint8 internal constant MAX_TOTAL_BANDITS = 1;
    uint256 internal constant MAX_BANDIT_SPAWN_SCAN_PER_REGION = 8;
    uint256 internal constant MAX_BANDIT_SPAWN_CLANSMEN_SCAN_PER_REGION = MAX_BANDIT_SPAWN_SCAN_PER_REGION * 4;
    uint8 internal constant BANDIT_TIER_COUNT = 5;

    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);

    function evaluateBanditSpawns(LibStorage.AppStorage storage s, bytes32 tickSeed) public {
        bool forcedSpawn = s.forceBanditSpawnNextHeartbeat;
        uint256[] memory regionWeights = banditSpawnRegionWeights(s);
        if (s.activeBanditCount >= MAX_TOTAL_BANDITS) {
            if (forcedSpawn) {
                s.forceBanditSpawnNextHeartbeat = false;
                s.world.currentBanditSpawnChanceBps = 0;
            }
            refreshBanditSpawnWorldPreview(s, regionWeights);
            return;
        }

        uint256[] memory candidateWeights = new uint256[](8);
        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
            if (!isBanditAllowedRegion(region)) continue;
            uint256 weight = regionWeights[region - 1];
            if (weight == 0 || s.banditsByRegion[region].length >= MAX_BANDITS_PER_REGION) continue;

            LibStorage.BanditSpawnState storage spawnState = s.banditSpawnByRegion[region];
            if (!forcedSpawn && s.world.currentTick < spawnState.lastSpawnTick + MIN_SPAWN_COOLDOWN_TICKS) continue;

            spawnState.probabilityAccum =
                forcedSpawn ? uint16(10000) : incrementBanditSpawnProbability(spawnState.probabilityAccum);
            if (banditSpawnRollPasses(tickSeed, region, spawnState.probabilityAccum)) {
                candidateWeights[region - 1] = weight;
            }
        }

        uint8 selectedRegion = selectBanditSpawnRegion(tickSeed, candidateWeights);
        if (selectedRegion != ClanWorldConstants.REGION_NOOP) {
            uint8 tier = banditSpawnTier(tickSeed, selectedRegion);
            spawnBandit(s, selectedRegion, tier, getBanditAttackPower(tier));
        }

        if (forcedSpawn) {
            s.forceBanditSpawnNextHeartbeat = false;
        }
        refreshBanditSpawnWorldPreview(s, regionWeights);
    }

    function spawnBandit(LibStorage.AppStorage storage s, uint8 region, uint8 tier, uint32 strength)
        public
        returns (uint32 id)
    {
        require(
            region >= ClanWorldConstants.REGION_FOREST && region <= ClanWorldConstants.REGION_DEEP_SEA,
            "ClanWorld: invalid bandit region"
        );
        require(
            region != ClanWorldConstants.REGION_UNICORN_TOWN && region != ClanWorldConstants.REGION_DEEP_SEA,
            "ClanWorld: forbidden bandit region"
        );
        require(strength > 0, "ClanWorld: invalid bandit strength");

        id = s.nextBanditId++;
        s.bandits[id] = BanditTroop({
            id: id,
            region: region,
            state: BanditState.Spawned,
            targetClanId: 0,
            tickEnteredState: s.world.currentTick,
            strength: strength,
            tier: tier,
            attackAttemptsMade: 0,
            carryWood: 0,
            carryIron: 0,
            carryWheat: 0,
            carryFish: 0,
            carryGold: 0
        });
        s.banditsByRegion[region].push(id);
        s.activeBanditCount += 1;
        s.banditSpawnByRegion[region].lastSpawnTick = s.world.currentTick;
        s.banditSpawnByRegion[region].probabilityAccum = 0;
        if (s.world.activeBanditId == ClanWorldConstants.BANDIT_ID_NULL) {
            s.world.activeBanditId = id;
        }

        emit BanditSpawned(id, region, tier, uint16Clamp(strength));
    }

    function banditSpawnRegionWeights(LibStorage.AppStorage storage s) public view returns (uint256[] memory weights) {
        weights = new uint256[](8);
        uint256 clanCount = s.allClanIds.length;
        if (clanCount == 0) return weights;

        uint256 scanCount = clanCount < MAX_BANDIT_SPAWN_SCAN_PER_REGION ? clanCount : MAX_BANDIT_SPAWN_SCAN_PER_REGION;
        uint256 startIndex = uint256(s.world.currentTick) % clanCount;
        uint256 clansmenScanned;
        for (uint256 i = 0; i < scanCount; i++) {
            Clan storage clan = s.clans[s.allClanIds[(startIndex + i) % clanCount]];
            if (clan.clanState == ClanState.DEAD) continue;

            if (
                clan.baseRegion >= ClanWorldConstants.REGION_FOREST
                    && clan.baseRegion <= ClanWorldConstants.REGION_DEEP_SEA && isBanditAllowedRegion(clan.baseRegion)
            ) {
                weights[clan.baseRegion - 1] += 100 + (LibScoring.lootValue(clan) / 1e18);
            }

            uint32[] storage clansmanIds = s.clanClansmanIds[clan.clanId];
            for (
                uint256 j = 0;
                j < clansmanIds.length && clansmenScanned < MAX_BANDIT_SPAWN_CLANSMEN_SCAN_PER_REGION;
                j++
            ) {
                clansmenScanned += 1;
                Clansman storage cs = s.clansmen[clansmanIds[j]];
                if (
                    cs.state != ClansmanState.DEAD && cs.currentRegion >= ClanWorldConstants.REGION_FOREST
                        && cs.currentRegion <= ClanWorldConstants.REGION_DEEP_SEA
                        && isBanditAllowedRegion(cs.currentRegion)
                ) {
                    weights[cs.currentRegion - 1] += 25;
                }
            }
        }
    }

    function refreshBanditSpawnWorldPreview(LibStorage.AppStorage storage s, uint256[] memory regionWeights) public {
        uint64 nextEligibleTick = type(uint64).max;
        uint16 maxChance = 0;

        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
            LibStorage.BanditSpawnState storage spawnState = s.banditSpawnByRegion[region];
            uint64 eligibleTick = spawnState.lastSpawnTick + MIN_SPAWN_COOLDOWN_TICKS;
            if (
                s.activeBanditCount < MAX_TOTAL_BANDITS && regionWeights[region - 1] > 0
                    && s.banditsByRegion[region].length < MAX_BANDITS_PER_REGION && eligibleTick < nextEligibleTick
            ) {
                nextEligibleTick = eligibleTick;
            }
            if (spawnState.probabilityAccum > maxChance) {
                maxChance = spawnState.probabilityAccum;
            }
        }

        s.world.nextBanditSpawnEligibleTick = nextEligibleTick == type(uint64).max ? 0 : nextEligibleTick;
        s.world.currentBanditSpawnChanceBps = s.forceBanditSpawnNextHeartbeat ? 10000 : maxChance;
    }

    function incrementBanditSpawnProbability(uint16 probabilityAccum) public pure returns (uint16) {
        uint256 next = uint256(probabilityAccum) + BANDIT_SPAWN_PROBABILITY_INCREMENT_BPS;
        if (next > BANDIT_SPAWN_MAX_PROBABILITY_BPS) return BANDIT_SPAWN_MAX_PROBABILITY_BPS;
        return uint16(next);
    }

    function banditSpawnRollPasses(bytes32 tickSeed, uint8 region, uint16 probabilityAccum) public pure returns (bool) {
        return banditSpawnRoll(tickSeed, region) < uint256(probabilityAccum);
    }

    function banditSpawnRoll(bytes32 tickSeed, uint8 region) public pure returns (uint256) {
        uint256 nonce = uint256(keccak256(abi.encodePacked("bandit_spawn", region)));
        return RNG.rngBounded(tickSeed, RNG.DOMAIN_BANDIT_SPAWN, nonce, 10000);
    }

    function selectBanditSpawnRegion(bytes32 tickSeed, uint256[] memory weights) public pure returns (uint8) {
        uint256 selected = RNG.rngWeightedPick(
            tickSeed, RNG.DOMAIN_BANDIT_SPAWN, uint256(keccak256(abi.encodePacked("bandit_spawn_region"))), weights
        );
        if (weights.length == 0 || weights[selected] == 0) return ClanWorldConstants.REGION_NOOP;
        return uint8(selected + 1);
    }

    function banditSpawnTier(bytes32 tickSeed, uint8 region) public pure returns (uint8) {
        uint256 nonce = uint256(keccak256(abi.encodePacked("bandit_spawn_tier", region)));
        uint256 roll = RNG.rngBounded(tickSeed, RNG.DOMAIN_BANDIT_SPAWN, nonce, BANDIT_TIER_COUNT);
        return uint8(roll + 1);
    }

    function isBanditAllowedRegion(uint8 region) public pure returns (bool) {
        return region != ClanWorldConstants.REGION_UNICORN_TOWN && region != ClanWorldConstants.REGION_DEEP_SEA;
    }

    function getBanditAttackPower(uint8 tier) public pure returns (uint16) {
        if (tier == 1) return 30;
        if (tier == 2) return 45;
        if (tier == 3) return 60;
        if (tier == 4) return 80;
        if (tier == 5) return 95;
        return 0;
    }

    function uint16Clamp(uint32 value) public pure returns (uint16) {
        if (value > type(uint16).max) return type(uint16).max;
        return uint16(value);
    }
}
