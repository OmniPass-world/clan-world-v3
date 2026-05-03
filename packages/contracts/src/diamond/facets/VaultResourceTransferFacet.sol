// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Clan, ClanState, IClanWorldEvents, ResourceType} from "../../IClanWorld.sol";
import {LibGameRules} from "../lib/LibGameRules.sol";
import {LibSettlement} from "../lib/LibSettlement.sol";
import {LibSettlementMath} from "../lib/LibSettlementMath.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract VaultResourceTransferFacet is IClanWorldEvents {
    function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);
        LibGameRules.requireNoPendingSeasonFinalization(s);
        require(amount > 0, "ClanWorld: zero amount");
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
