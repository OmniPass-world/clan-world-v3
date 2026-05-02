// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {
    Clan,
    ClanState,
    ClanWorldConstants,
    Clansman,
    ClansmanState,
    IClanWorldEvents,
    WheatPlot,
    WheatPlotState
} from "../../IClanWorld.sol";
import {LibSeason} from "../lib/LibSeason.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract ClanLifecycleFacet is IClanWorldEvents {
    uint8 private constant MAX_CLANS = 12;

    function mintClan(address to) external returns (uint32 clanId, uint256 iftTokenId) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);

        _requireNoPendingSeasonFinalization(s);
        require(to != address(0), "ClanWorld: zero address");
        require(s.allClanIds.length < MAX_CLANS, "ClanWorld: max clans");

        clanId = s.nextClanId++;
        iftTokenId = uint256(clanId);
        uint8 baseRegion = _baseRegionForClan(clanId);

        Clan storage clan = s.clans[clanId];
        clan.clanId = clanId;
        clan.iftTokenId = iftTokenId;
        clan.owner = to;
        clan.clanState = ClanState.ACTIVE;
        clan.baseRegion = baseRegion;
        clan.baseLevel = 1;
        clan.livingClansmen = 4;
        clan.lastSettledTick = s.world.currentTick;
        clan.goldBalance = 3e18;
        clan.vaultWood = 20e18;
        clan.vaultWheat = 20e18;
        clan.vaultFish = 2e18;

        WheatPlotState startingPlotState =
            LibSeason.isWinterActiveAt(s.world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
        uint256 startingWheat =
            startingPlotState == WheatPlotState.WinterLocked ? 0 : ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;

        s.wheatPlots[clanId][0] = WheatPlot({
            state: startingPlotState,
            region: ClanWorldConstants.REGION_WEST_FARMS,
            remainingWheat: startingWheat,
            regrowUntilTick: 0
        });
        s.wheatPlots[clanId][1] = WheatPlot({
            state: startingPlotState,
            region: ClanWorldConstants.REGION_EAST_FARMS,
            remainingWheat: startingWheat,
            regrowUntilTick: 0
        });

        for (uint256 i = 0; i < 4; i++) {
            uint32 clansmanId = s.nextClansmanId++;
            Clansman storage cs = s.clansmen[clansmanId];
            cs.clansmanId = clansmanId;
            cs.clanId = clanId;
            cs.state = ClansmanState.WAITING;
            cs.currentRegion = baseRegion;
            s.clanClansmanIds[clanId].push(clansmanId);
        }

        s.allClanIds.push(clanId);

        emit ClanSpawned(clanId, to, iftTokenId, baseRegion, s.world.currentTick);
        LibStorage.exitNonReentrant(s);
    }

    function _baseRegionForClan(uint32 clanId) private pure returns (uint8) {
        uint8[6] memory spawnRegions = [
            ClanWorldConstants.REGION_FOREST,
            ClanWorldConstants.REGION_MOUNTAINS,
            ClanWorldConstants.REGION_WEST_FARMS,
            ClanWorldConstants.REGION_EAST_FARMS,
            ClanWorldConstants.REGION_WEST_DOCKS,
            ClanWorldConstants.REGION_EAST_DOCKS
        ];
        return spawnRegions[(clanId - 1) % 6];
    }

    function _requireNoPendingSeasonFinalization(LibStorage.AppStorage storage s) private view {
        require(
            !(s.world.currentTick + 1 >= s.world.seasonEndTick && !s.world.seasonFinalized),
            "ClanWorld: finalize season first"
        );
    }
}
