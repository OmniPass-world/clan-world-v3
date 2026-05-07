// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Clan, ClanState, ResourceType} from "../../IClanWorld.sol";
import {LibGameRules} from "./LibGameRules.sol";
import {LibSettlement} from "./LibSettlement.sol";
import {LibSettlementMath} from "./LibSettlementMath.sol";
import {LibStorage} from "./LibStorage.sol";

library LibDirectTransfers {
    event GoldTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
    event VaultResourceTransferred(
        uint32 indexed fromClanId, uint32 indexed toClanId, ResourceType resource, uint256 amount, uint64 atTick
    );
    event BlueprintTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
    event ClanSettled(uint32 indexed clanId, uint64 settledAtTick);

    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount, address caller) public {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        _requireTransferReady(s, fromClanId, toClanId, caller);
        require(amount > 0, "ClanWorld: zero amount");
        require(s.clans[fromClanId].goldBalance >= amount, "ClanWorld: insufficient gold");

        s.clans[fromClanId].goldBalance -= amount;
        s.clans[toClanId].goldBalance += amount;

        emit GoldTransferred(fromClanId, toClanId, amount, s.world.currentTick);
    }

    function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount, address caller)
        public
    {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        _requireTransferReady(s, fromClanId, toClanId, caller);
        require(amount > 0, "ClanWorld: zero amount");

        Clan storage fromClan = s.clans[fromClanId];
        Clan storage toClan = s.clans[toClanId];
        if (resource == ResourceType.Wood) {
            require(_deductVaultResource(s, fromClanId, fromClan, resource, amount), "ClanWorld: insufficient wood");
            toClan.vaultWood += amount;
        } else if (resource == ResourceType.Iron) {
            require(_deductVaultResource(s, fromClanId, fromClan, resource, amount), "ClanWorld: insufficient iron");
            toClan.vaultIron += amount;
        } else if (resource == ResourceType.Wheat) {
            require(_deductVaultResource(s, fromClanId, fromClan, resource, amount), "ClanWorld: insufficient wheat");
            toClan.vaultWheat += amount;
        } else if (resource == ResourceType.Fish) {
            require(_deductVaultResource(s, fromClanId, fromClan, resource, amount), "ClanWorld: insufficient fish");
            toClan.vaultFish += amount;
        } else {
            revert("ClanWorld: invalid resource");
        }

        emit VaultResourceTransferred(fromClanId, toClanId, resource, amount, s.world.currentTick);
    }

    function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount, address caller) public {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        _requireTransferReady(s, fromClanId, toClanId, caller);
        require(amount > 0, "ClanWorld: zero amount");
        require(_deductBlueprint(s, fromClanId, amount), "ClanWorld: insufficient blueprints");

        s.clans[toClanId].blueprintBalance += amount;

        emit BlueprintTransferred(fromClanId, toClanId, amount, s.world.currentTick);
    }

    function _requireTransferReady(
        LibStorage.AppStorage storage s,
        uint32 fromClanId,
        uint32 toClanId,
        address caller
    ) private {
        LibGameRules.requireWorldNotPaused(s);
        LibGameRules.requireNoPendingSeasonFinalization(s);
        require(fromClanId != toClanId, "ClanWorld: same clan");

        require(s.clans[fromClanId].clanId != 0, "ClanWorld: from clan not found");
        require(s.clans[fromClanId].owner == caller, "ClanWorld: not clan owner");
        require(s.clans[toClanId].clanId != 0, "ClanWorld: to clan not found");

        _settleClan(s, fromClanId);
        _settleClan(s, toClanId);

        require(
            s.clans[fromClanId].lastSettledTick == s.world.currentTick
                && s.clans[toClanId].lastSettledTick == s.world.currentTick,
            "ERR_MUST_SETTLE_FIRST"
        );
        require(s.clans[fromClanId].clanState != ClanState.DEAD, "ClanWorld: clan dead");
    }

    function _settleClan(LibStorage.AppStorage storage s, uint32 clanId) private {
        if (s.clans[clanId].clanId == 0) return;
        if (s.clans[clanId].clanState == ClanState.DEAD) return;
        if (s.clans[clanId].lastSettledTick >= s.world.currentTick) return;

        LibSettlement.SettlementSimulation memory sim = LibSettlement.simulateToTick(s, clanId, s.world.currentTick);
        LibSettlement.commitSimulation(s, sim);
        emit ClanSettled(clanId, s.clans[clanId].lastSettledTick);
    }

    function _deductBlueprint(LibStorage.AppStorage storage s, uint32 clanId, uint256 amount) private returns (bool) {
        if (
            LibSettlementMath.spendableAfterReleasing(
                    s.clans[clanId].blueprintBalance, s.reservedBlueprintByClan[clanId], 0
                ) < amount
        ) {
            return false;
        }

        s.clans[clanId].blueprintBalance -= amount;
        return true;
    }

    function _deductVaultResource(
        LibStorage.AppStorage storage s,
        uint32 clanId,
        Clan storage clan,
        ResourceType resource,
        uint256 amount
    ) private returns (bool) {
        if (resource == ResourceType.Wood) {
            if (LibSettlementMath.spendableAfterReleasing(clan.vaultWood, s.reservedWoodByClan[clanId], 0) < amount) {
                return false;
            }
            clan.vaultWood -= amount;
            return true;
        }
        if (resource == ResourceType.Iron) {
            if (LibSettlementMath.spendableAfterReleasing(clan.vaultIron, s.reservedIronByClan[clanId], 0) < amount) {
                return false;
            }
            clan.vaultIron -= amount;
            return true;
        }
        if (resource == ResourceType.Wheat) {
            if (LibSettlementMath.spendableAfterReleasing(clan.vaultWheat, s.reservedWheatByClan[clanId], 0) < amount) {
                return false;
            }
            clan.vaultWheat -= amount;
            return true;
        }
        if (resource == ResourceType.Fish) {
            if (clan.vaultFish < amount) return false;
            clan.vaultFish -= amount;
            return true;
        }
        return false;
    }

}
