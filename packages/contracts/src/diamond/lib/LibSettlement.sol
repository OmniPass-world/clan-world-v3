// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Clan, ClanWorldConstants, Clansman, Mission, WheatPlot} from "../../IClanWorld.sol";
import {LibStorage} from "./LibStorage.sol";

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
}
