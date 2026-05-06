// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Clan, ClanState, IClanWorldEvents, ResourceType} from "../../IClanWorld.sol";
import {LibGameRules} from "../lib/LibGameRules.sol";
import {LibSettlement} from "../lib/LibSettlement.sol";
import {LibSettlementMath} from "../lib/LibSettlementMath.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract BundleTransferFacet is IClanWorldEvents {
    function transferBundle(
        uint32 fromClanId,
        uint32 toClanId,
        uint256 gold,
        uint256 blueprint,
        uint256 wood,
        uint256 iron,
        uint256 wheat,
        uint256 fish
    ) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);
        LibGameRules.requireWorldNotPaused(s);
        LibGameRules.requireNoPendingSeasonFinalization(s);
        require(gold > 0 || blueprint > 0 || wood > 0 || iron > 0 || wheat > 0 || fish > 0, "ClanWorld: empty bundle");
        require(fromClanId != toClanId, "ClanWorld: same clan");

        require(s.clans[fromClanId].clanId != 0, "ClanWorld: from clan not found");
        require(s.clans[fromClanId].owner == msg.sender, "ClanWorld: not clan owner");
        require(s.clans[toClanId].clanId != 0, "ClanWorld: to clan not found");

        _settleClan(s, fromClanId);
        _settleClan(s, toClanId);

        require(
            s.clans[fromClanId].lastSettledTick == s.world.currentTick
                && s.clans[toClanId].lastSettledTick == s.world.currentTick,
            "ERR_MUST_SETTLE_FIRST"
        );
        require(s.clans[fromClanId].clanState != ClanState.DEAD, "ClanWorld: clan dead");

        Clan storage fromClan = s.clans[fromClanId];
        Clan storage toClan = s.clans[toClanId];
        _requireBundleBalances(s, fromClanId, fromClan, gold, blueprint, wood, iron, wheat, fish);

        if (gold > 0) fromClan.goldBalance -= gold;
        if (blueprint > 0) fromClan.blueprintBalance -= blueprint;
        if (wood > 0) fromClan.vaultWood -= wood;
        if (iron > 0) fromClan.vaultIron -= iron;
        if (wheat > 0) fromClan.vaultWheat -= wheat;
        if (fish > 0) fromClan.vaultFish -= fish;

        if (gold > 0) toClan.goldBalance += gold;
        if (blueprint > 0) toClan.blueprintBalance += blueprint;
        if (wood > 0) toClan.vaultWood += wood;
        if (iron > 0) toClan.vaultIron += iron;
        if (wheat > 0) toClan.vaultWheat += wheat;
        if (fish > 0) toClan.vaultFish += fish;

        if (gold > 0) emit GoldTransferred(fromClanId, toClanId, gold, s.world.currentTick);
        if (blueprint > 0) emit BlueprintTransferred(fromClanId, toClanId, blueprint, s.world.currentTick);
        if (wood > 0) {
            emit VaultResourceTransferred(fromClanId, toClanId, ResourceType.Wood, wood, s.world.currentTick);
        }
        if (iron > 0) {
            emit VaultResourceTransferred(fromClanId, toClanId, ResourceType.Iron, iron, s.world.currentTick);
        }
        if (wheat > 0) {
            emit VaultResourceTransferred(fromClanId, toClanId, ResourceType.Wheat, wheat, s.world.currentTick);
        }
        if (fish > 0) {
            emit VaultResourceTransferred(fromClanId, toClanId, ResourceType.Fish, fish, s.world.currentTick);
        }
        LibStorage.exitNonReentrant(s);
    }

    function _settleClan(LibStorage.AppStorage storage s, uint32 clanId) private {
        if (s.clans[clanId].clanId == 0) return;
        if (s.clans[clanId].clanState == ClanState.DEAD) return;
        if (s.clans[clanId].lastSettledTick >= s.world.currentTick) return;

        LibSettlement.SettlementSimulation memory sim = LibSettlement.simulateToTick(s, clanId, s.world.currentTick);
        LibSettlement.commitSimulation(s, sim);
        emit ClanSettled(clanId, s.clans[clanId].lastSettledTick);
    }

    function _requireBundleBalances(
        LibStorage.AppStorage storage s,
        uint32 fromClanId,
        Clan storage fromClan,
        uint256 gold,
        uint256 blueprint,
        uint256 wood,
        uint256 iron,
        uint256 wheat,
        uint256 fish
    ) private view {
        if (gold > 0) require(fromClan.goldBalance >= gold, "ClanWorld: insufficient gold");
        if (blueprint > 0) {
            require(_spendableBlueprint(s, fromClanId, fromClan) >= blueprint, "ClanWorld: insufficient blueprints");
        }
        if (wood > 0) {
            require(
                _spendableVaultResource(s, fromClanId, fromClan, ResourceType.Wood) >= wood,
                "ClanWorld: insufficient wood"
            );
        }
        if (iron > 0) {
            require(
                _spendableVaultResource(s, fromClanId, fromClan, ResourceType.Iron) >= iron,
                "ClanWorld: insufficient iron"
            );
        }
        if (wheat > 0) {
            require(
                _spendableVaultResource(s, fromClanId, fromClan, ResourceType.Wheat) >= wheat,
                "ClanWorld: insufficient wheat"
            );
        }
        if (fish > 0) {
            require(
                _spendableVaultResource(s, fromClanId, fromClan, ResourceType.Fish) >= fish,
                "ClanWorld: insufficient fish"
            );
        }
    }

    function _spendableBlueprint(LibStorage.AppStorage storage s, uint32 clanId, Clan storage clan)
        private
        view
        returns (uint256)
    {
        return LibSettlementMath.spendableAfterReleasing(clan.blueprintBalance, s.reservedBlueprintByClan[clanId], 0);
    }

    function _spendableVaultResource(
        LibStorage.AppStorage storage s,
        uint32 clanId,
        Clan storage clan,
        ResourceType resource
    ) private view returns (uint256) {
        if (resource == ResourceType.Wood) {
            return LibSettlementMath.spendableAfterReleasing(clan.vaultWood, s.reservedWoodByClan[clanId], 0);
        }
        if (resource == ResourceType.Iron) {
            return LibSettlementMath.spendableAfterReleasing(clan.vaultIron, s.reservedIronByClan[clanId], 0);
        }
        if (resource == ResourceType.Wheat) {
            return LibSettlementMath.spendableAfterReleasing(clan.vaultWheat, s.reservedWheatByClan[clanId], 0);
        }
        if (resource == ResourceType.Fish) {
            return clan.vaultFish;
        }
        return 0;
    }
}
