# Clan World — Soft Mid-Season Reset Runbook

Use this when the game is in a death-spiral or stalled state mid-season but you DO NOT want to redeploy the diamond (preserves clan IDs, owner addresses, season counter, on-chain history). This pattern uses `AdminRecoveryFacet` + `WorldPauseFacet` to do live recovery without touching the contracts.

For a FULL reset (new diamond, fresh Convex, season=1), see `docs/runbooks/full-game-reset.md`. This file is the lighter-weight alternative for mid-season unstucks.

## Decision matrix — soft vs full

Use the **soft** reset (this runbook) when ALL of the following are true:

- Same season number is fine to keep
- Diamond ABI hasn't changed (no facet selectors added/removed)
- You want existing clan owners (wallet addresses) preserved
- You don't need a fresh genesis tick

Use the **full** reset (`docs/runbooks/full-game-reset.md`) when ANY of the following is true:

- New facet selectors added (diamond cut needed)
- Clan IDs need to change
- Season counter needs to reset
- Storage schema migration required

## Sequence (with exact commands)

Run these steps **in this order**. Skipping or reordering the pause/heartbeat steps is what causes most failed soft-resets.

## 1. Stop the off-chain heartbeat loop

The heartbeat loop calls `heartbeat()` every ~5s. While running, it consumes resources (starvation, bandit attacks, cold damage) on every tick. Revives + injects that complete during a heartbeat window will be eaten before you can verify them.

```bash
tmux kill-session -t clanworld-heartbeat 2>/dev/null || true
pkill -f clanworld-heartbeat-with-finalize 2>/dev/null
# Verify dead:
pgrep -af clanworld-heartbeat-with-finalize | command grep -v grep
```

## 2. Pause the world on-chain

```bash
DIAMOND=<your-diamond-address>
RPC=https://sepolia.base.org  # see "RPC selection" gotcha below
DEPLOYER_KEY=$(cat ~/.secrets/clanworld-v3-deployer.key)
export PATH="$HOME/.foundry/bin:$PATH"

cast send "$DIAMOND" "pauseWorld()" --rpc-url "$RPC" --private-key "$DEPLOYER_KEY"
# Verify:
cast call "$DIAMOND" "isWorldPaused()(bool)" --rpc-url "$RPC"
# Should print: true
```

This prevents `heartbeat()` from running — so even if the off-chain loop is still alive somewhere, the chain side rejects with "rate limited".

## 3. Revive + inject per clan

For each clan ID (typically 1..4):

```bash
for clan in 1 2 3 4; do
  cast send "$DIAMOND" "reviveDeadClansmen(uint32)" "$clan" \
    --rpc-url "$RPC" --private-key "$DEPLOYER_KEY"
  cast send "$DIAMOND" "injectClanResources(uint32,uint256,uint256,uint256,uint256,uint256,uint256)" \
    "$clan" <wood> <iron> <wheat> <fish> <gold> <blueprint> \
    --rpc-url "$RPC" --private-key "$DEPLOYER_KEY"
done
```

**Argument order matters:** `(clanId, wood, iron, wheat, fish, gold, blueprint)` — all uint256, all raw integer counts (NOT e18-scaled).

The functions are owner-gated. Pre-check the deployer key matches the diamond owner: `cast call "$DIAMOND" "owner()(address)" --rpc-url "$RPC"` should equal `cast wallet address $(cat ~/.secrets/clanworld-v3-deployer.key)`.

## 4. Verify state held WHILE PAUSED

```bash
for clan in 1 2 3 4; do
  cast call "$DIAMOND" \
    "getClan(uint32)((uint32,uint256,address,uint8,uint8,uint8,uint8,uint8,uint8,uint64,uint64,uint16,uint64,uint256,uint256,uint256,uint256,uint256,uint256))" \
    "$clan" --rpc-url "$RPC"
done
```

The decoded tuple positions are (in source order from `IClanWorld.sol::struct Clan`):

1. clanId
2. iftTokenId
3. owner
4. clanState (enum: 0=ACTIVE, 1=DEAD; verify against the latest `ClanState` enum)
5. baseRegion
6. baseLevel
7. wallLevel
8. monumentLevel
9. **livingClansmen** — must be > 0 after revive
10. lastSettledTick
11. starvationStartsAtTick
12. coldDamage
13. ownerNonce
14. goldBalance
15. blueprintBalance
16. **vaultWood** — must match your inject value
17. **vaultIron**
18. **vaultWheat**
19. **vaultFish**

If any of fields 9, 16-19 don't match expected: re-run the failed tx (see "RPC drops" gotcha below).

## 5. Bandit-tier and other config (optional)

Set max bandit difficulty if needed:

```bash
cast send "$DIAMOND" "setMaxBanditTier(uint8)" 1 \
  --rpc-url "$RPC" --private-key "$DEPLOYER_KEY"
# Verify:
cast call "$DIAMOND" "maxBanditTier()(uint8)" --rpc-url "$RPC"
```

Valid range is `[1, 5]`. `1` = weakest bandits (good while tuning elder strategy).

## 6. Unpause the world

```bash
cast send "$DIAMOND" "unpauseWorld()" --rpc-url "$RPC" --private-key "$DEPLOYER_KEY"
cast call "$DIAMOND" "isWorldPaused()(bool)" --rpc-url "$RPC"
# Should print: false
```

`unpauseWorld()` also reschedules `nextHeartbeatAtTs`, so heartbeat works immediately.

## 7. Restart off-chain processes

```bash
# Heartbeat
tmux new-session -d -s clanworld-heartbeat \
  -c ~/code/clan-world/clan-world-game \
  ~/bin/clanworld-heartbeat-with-finalize

# Elder runner v2 (tick-driven, Convex polling)
# Skip if already running: pgrep -f clanworld-elder-runner-v2
tmux new-session -d -s clanworld-elder-runner \
  -c ~ \
  ~/bin/clanworld-elder-runner-v2

# Elders themselves should already be in tmux (elder-1..4).
# If they're stuck on "Dead. Holding." stale state, restart each:
for n in 1 2 3 4; do
  tmux kill-session -t "elder-$n" 2>/dev/null
  tmux new-session -d -s "elder-$n" -c ~/clan-world/elder-$n "bash run.sh"
done
```

## 8. Verify off-chain caught up

```bash
# Wait 30-60s then:
cast call "$DIAMOND" "getWorldState()((uint64,uint64,uint64,bool,uint64))" --rpc-url "$RPC"
# Tick should be > the pre-reset tick

# Convex snapshot should reflect on-chain state:
curl -s "https://valuable-kudu-985.convex.cloud/api/query" \
  -X POST -H "Content-Type: application/json" \
  -d '{"path":"getSnapshot:getSnapshot","args":{}}' \
  | jq '.value.worldSnapshot.tick'
# Should match (or be 1-2 behind) the on-chain tick
```

## Operational gotchas (captured from live runs)

### Race: heartbeat eats your reset

If the heartbeat loop is running when you call `reviveDeadClansmen` + `injectClanResources`, each subsequent heartbeat tick can consume the freshly-injected resources (starvation, bandit attacks). Your `status=1` event-emitting tx WILL have completed, but state may show the reset never happened.

**Always pause the world BEFORE running recovery txs.** Do not skip step 1+2.

### RPC selection: Infura vs public Base Sepolia

The repo's `.env.local` ships with `RPC_URL_PRIMARY=https://base-sepolia.infura.io/v3/<key>`. Infura has aggressive per-key rate limits — bulk reset operations (8 txs × 4 clans + verification queries) will exhaust the quota within 1-2 minutes. After that, the heartbeat loop's silent `read_world_state failed; sleeping` loop will spin indefinitely without alerting.

**During admin operations and recovery, swap to the public Base Sepolia RPC:**

```bash
# In .env.local:
RPC_URL_PRIMARY=https://sepolia.base.org
RPC_URL_FALLBACK=https://base-sepolia.infura.io/v3/<key>  # (or leave Infura as fallback)
```

The public RPC is slower but rate-limit-free at game cadence. Swap back to Infura when not actively debugging if you want lower per-tx latency.

### `cast send` loop silently drops txs

When iterating `cast send` in a bash loop against the same RPC + same private key, some txs go through and some get dropped (no error, just no submission, no tx-hash output). Nonces stay in lock-step with what landed, so the "dropped" tx is just gone — no orphan nonce issue.

**Always verify state per-clan after a batch run.** Re-run any missing tx individually:

```bash
# Verify each clan was modified
for clan in 1 2 3 4; do
  cast call "$DIAMOND" "getClan(uint32)(...struct...)" $clan --rpc-url "$RPC" | head -1
done
# Re-run individual cast send for any clan that didn't change
```

A `forge script` with explicit nonce sequencing is more robust for high-volume bulk admin operations, but for 8-16 calls the cast-and-verify loop is fine.

### Owner check before pause

If the deployer key does not match `owner()` on the diamond, ALL admin functions revert. Check upfront:

```bash
cast call "$DIAMOND" "owner()(address)" --rpc-url "$RPC"
cast wallet address "$(cat ~/.secrets/clanworld-v3-deployer.key)"
# Two outputs must match exactly.
```

### Convex snapshot freshness

The cockpit reads from Convex, not the chain directly. After your on-chain reset, Convex doesn't update until the next heartbeat tick fires — which writes events that the Convex indexer ingests. So:

1. Restart heartbeat (step 7)
2. Wait for ≥1 heartbeat to complete (~5-10s)
3. Re-query Convex snapshot

If Convex is stale after multiple heartbeat cycles, the indexer's webhook may have a stale `CONVEX_WEBHOOK_URL` env. Check it matches the current Convex deployment.

### Stale Vercel JS bundle masks fresh data

If `app.clan-world.com` shows obviously wrong state (e.g. old polygon coords, missing UI elements you know shipped), the bundled JS is stale. Verify by checking the deployed `index-*.js` file vs current `git log` on main. To force a fresh deploy:

- Local: `cd apps/web && vercel --prod` (requires Vercel CLI auth)
- CI: push a new tag (workflow fires on `push: tags: ['v*']`)

A Convex-fresh / Vercel-stale combo will show LATEST on-chain data inside an OLD UI — confusing but explains discrepancies.
