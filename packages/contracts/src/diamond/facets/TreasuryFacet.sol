// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {IClanWorldEvents, PoolSeedConfig} from "../../IClanWorld.sol";
import {MinimalERC20} from "../../MinimalERC20.sol";
import {StubPool} from "../../StubPool.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract TreasuryFacet is IClanWorldEvents {
    modifier nonReentrant() {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);
        _;
        LibStorage.exitNonReentrant(s);
    }

    function initTreasury(address[6] calldata tokens, address[4] calldata pools) external nonReentrant {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        require(!s.treasury.poolsSeeded && s.treasury.woodToken == address(0), "ClanWorld: treasury already init");
        require(msg.sender == s.treasury.treasuryOwner, "ClanWorld: not owner");
        _validateTreasuryInit(tokens, pools);

        s.treasury.woodToken = tokens[0];
        s.treasury.ironToken = tokens[1];
        s.treasury.wheatToken = tokens[2];
        s.treasury.fishToken = tokens[3];
        s.treasury.goldToken = tokens[4];
        s.treasury.blueprintToken = tokens[5];

        s.treasury.woodGoldPool = pools[0];
        s.treasury.wheatGoldPool = pools[1];
        s.treasury.fishGoldPool = pools[2];
        s.treasury.ironGoldPool = pools[3];
    }

    function seedPools(PoolSeedConfig calldata cfg) external nonReentrant {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        require(msg.sender == s.treasury.treasuryOwner, "ClanWorld: not owner");
        require(!s.treasury.poolsSeeded, "ClanWorld: pools already seeded");
        require(s.treasury.woodToken != address(0), "ClanWorld: treasury not init");

        _transferSeed(s.treasury.woodToken, s.treasury.woodGoldPool, cfg.woodSeed);
        _transferSeed(s.treasury.goldToken, s.treasury.woodGoldPool, cfg.goldSeedForWood);
        _transferSeed(s.treasury.wheatToken, s.treasury.wheatGoldPool, cfg.wheatSeed);
        _transferSeed(s.treasury.goldToken, s.treasury.wheatGoldPool, cfg.goldSeedForWheat);
        _transferSeed(s.treasury.fishToken, s.treasury.fishGoldPool, cfg.fishSeed);
        _transferSeed(s.treasury.goldToken, s.treasury.fishGoldPool, cfg.goldSeedForFish);
        _transferSeed(s.treasury.ironToken, s.treasury.ironGoldPool, cfg.ironSeed);
        _transferSeed(s.treasury.goldToken, s.treasury.ironGoldPool, cfg.goldSeedForIron);

        StubPool(s.treasury.woodGoldPool).seed(cfg.woodSeed, cfg.goldSeedForWood);
        StubPool(s.treasury.wheatGoldPool).seed(cfg.wheatSeed, cfg.goldSeedForWheat);
        StubPool(s.treasury.fishGoldPool).seed(cfg.fishSeed, cfg.goldSeedForFish);
        StubPool(s.treasury.ironGoldPool).seed(cfg.ironSeed, cfg.goldSeedForIron);

        s.treasury.poolsSeeded = true;

        emit PoolsSeeded(
            s.treasury.woodGoldPool, s.treasury.wheatGoldPool, s.treasury.fishGoldPool, s.treasury.ironGoldPool
        );
    }

    function _validateTreasuryInit(address[6] calldata tokens, address[4] calldata pools) private view {
        for (uint256 i = 0; i < tokens.length; i++) {
            require(tokens[i] != address(0), "ClanWorld: zero treasury token");
            _requireContract(tokens[i], "ClanWorld: treasury token not contract");
            for (uint256 j = i + 1; j < tokens.length; j++) {
                require(tokens[i] != tokens[j], "ClanWorld: duplicate treasury token");
            }
        }

        for (uint256 i = 0; i < pools.length; i++) {
            require(pools[i] != address(0), "ClanWorld: zero treasury pool");
            _requireContract(pools[i], "ClanWorld: treasury pool not contract");
            for (uint256 j = i + 1; j < pools.length; j++) {
                require(pools[i] != pools[j], "ClanWorld: duplicate treasury pool");
            }
        }

        _requirePoolWiring(pools[0], tokens[0], tokens[4]);
        _requirePoolWiring(pools[1], tokens[2], tokens[4]);
        _requirePoolWiring(pools[2], tokens[3], tokens[4]);
        _requirePoolWiring(pools[3], tokens[1], tokens[4]);
    }

    function _requirePoolWiring(address pool, address tokenA, address tokenB) private view {
        require(StubPool(pool).TOKEN_A() == tokenA, "ClanWorld: treasury pool token A mismatch");
        require(StubPool(pool).TOKEN_B() == tokenB, "ClanWorld: treasury pool token B mismatch");
        require(StubPool(pool).ENGINE() == address(this), "ClanWorld: treasury pool engine mismatch");
    }

    function _requireContract(address target, string memory message) private view {
        uint256 size;
        assembly {
            size := extcodesize(target)
        }
        require(size > 0, message);
    }

    function _transferSeed(address token, address pool, uint256 amount) private {
        require(token != address(0) && pool != address(0), "ClanWorld: treasury not init");
        require(MinimalERC20(token).transferFrom(msg.sender, pool, amount), "ClanWorld: seed transfer failed");
    }
}
