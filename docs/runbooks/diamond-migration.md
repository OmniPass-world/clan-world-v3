# ClanWorld Diamond Migration Runbook

When the active diamond becomes unrecoverable (e.g. stuck at season-end-not-finalized with finalize() exceeding per-tx gas limit), this runbook captures every config touch point needed to point the stack at a different diamond on Base Sepolia.

This was first authored 2026-05-09 when v4 diamond `0xAd03010c…` got stuck and we migrated to backup diamond `0x2709eEB…`.

## When to use this

- Active diamond is stuck and cannot be recovered in-place
- Need to point the stack at a previously-deployed backup diamond
- Need to deploy a fresh diamond (this runbook covers the post-deploy reconfiguration; for the deploy itself, see `base-sepolia-deployment.md`)

## What this DOESN'T cover

- The deploy itself (see `base-sepolia-deployment.md`)
- Recovering admin access on a diamond owned by a different key — see "If owner key is missing" below
- Front-end ABI mismatches when migrating to an OLDER deployed diamond — see "ABI mismatch handling" below

---

## Diamond inventory (Base Sepolia, as of 2026-05-09)

| Address | State | Owner | Notes |
|---|---|---|---|
| `0xAd03010c1608538040D5186B09437bb36983AE85` | Stuck at season-end | `0x02C4…267c7` (deployer) | finalizeSeason() needs >14M gas; cannot finalize |
| `0x2709eEB245105F701962e9c6CaC5FA610A4D5e6a` | Mid-season-2, 4 clans, 16 clansmen | `0xB60679…283b` | Older ABI; **verified on Basescan** |
| `0xC012275376b867944cd874FB2d600d6dA3B4A56e` | No code | — | Address in v4 git history; never deployed |

To verify any diamond on a fresh chain check:

```bash
cast code <address> --rpc-url $RPC_URL_PRIMARY    # has code?
cast call <address> "owner()(address)" --rpc-url $RPC_URL_PRIMARY    # who controls admin?
cast call <address> "getClanIds()(uint32[])" --rpc-url $RPC_URL_PRIMARY    # clans present?
```

---

## Pre-flight: confirm transferOwnership path (if needed)

If the backup diamond is owned by a different key than your deployer, you have three options:

1. **Find the original owner key** and use it directly for ongoing admin
2. **Transfer ownership** to your deployer (via the original owner key) — requires one tx
3. **Live without admin** — only public functions available (heartbeat, finalizeSeason, settleClan, per-clan-owner actions)

To transfer ownership:

```bash
# Verify the OwnershipFacet exposes transferOwnership(address)
# Selector 0xf2fde38b on the diamond's facets() loupe is the indicator
cast call $DIAMOND "facets()((address,bytes4[])[])" --rpc-url $RPC_URL_PRIMARY | grep f2fde38b

# Then, FROM THE OWNER KEY:
cast send $DIAMOND "transferOwnership(address)" $NEW_OWNER \
  --rpc-url $RPC_URL_PRIMARY --private-key $CURRENT_OWNER_KEY

# Verify
cast call $DIAMOND "owner()(address)" --rpc-url $RPC_URL_PRIMARY
```

Or via Basescan UI if the contract is verified:

1. Visit `https://sepolia.basescan.org/address/<diamond>#writeContract`
2. Connect a wallet with the current owner key
3. Find `transferOwnership` → input new owner address → write

---

## Migration checklist

Set environment variables for clarity:

```bash
NEW_DIAMOND=0x...  # new active diamond
OLD_DIAMOND=0x...  # diamond being migrated AWAY from
```

### 1. Local `.env.local`

Two locations matter:

```bash
# Active dev workspace
sed -i "s/^CLAN_WORLD_CONTRACT_ADDRESS=.*/CLAN_WORLD_CONTRACT_ADDRESS=$NEW_DIAMOND/" \
  ~/code/<workspace>/.env.local

# Server's apps/server (Convex local dev) reads from same .env.local via envDir
# No separate edit needed when envDir is monorepo root
```

Verify:

```bash
grep CLAN_WORLD_CONTRACT_ADDRESS .env.local
```

### 2. Elder agent envs (4 files)

Each elder runtime dir has its own `.env`:

```bash
for n in 1 2 3 4; do
  sed -i "s/^CLAN_WORLD_CONTRACT_ADDRESS=.*/CLAN_WORLD_CONTRACT_ADDRESS=$NEW_DIAMOND/" \
    ~/clan-world/elder-$n/.env
done

# Verify
for n in 1 2 3 4; do
  grep CLAN_WORLD_CONTRACT_ADDRESS ~/clan-world/elder-$n/.env
done
```

If you change elder ENV mid-session, kill + restart each elder's tmux session to pick up the new value.

### 3. Convex deployment env vars

The Convex deployment has its own env-var store, separate from `.env.local`. The indexer reads chain state via `process.env.CLAN_WORLD_CONTRACT_ADDRESS` (or whatever the indexer uses — check `apps/server/convex/indexer.ts`).

```bash
cd apps/server
npx convex env set CLAN_WORLD_CONTRACT_ADDRESS $NEW_DIAMOND --prod
npx convex env list --prod
```

Other variables that may need updating:

- `RPC_URL_PRIMARY` — usually unchanged unless network changes
- `WEBHOOK_SHARED_SECRET` — only changes on key rotation
- Any contract addresses for tokens/pools if the new diamond uses different deployments

### 4. Reset Convex tracking state

The indexer's tracking pointers are in Convex tables. After repointing, the indexer's last-seen-block is for the OLD engine and won't apply cleanly to the new one. Clear:

```bash
# From Convex dashboard, or via a one-shot mutation:
# - worldSnapshot — clear (or insert a fresh row matching the new chain state)
# - chainEvents — clear (or filter by engineAddress != $NEW_DIAMOND)
# - any indexer-pointer table (last-indexed-block, etc.)
```

Specific table names live in `apps/server/convex/schema.ts`. Search for `worldSnapshot` and any tables with names containing "indexer", "pointer", "lastBlock", etc.

### 5. Restart heartbeat loop

```bash
# Kill any existing
tmux kill-session -t clanworld-heartbeat 2>/dev/null

# Restart with new env (reads .env.local on launch)
tmux new-session -d -s clanworld-heartbeat -c <repo-root> \
  'export PATH="$HOME/.foundry/bin:$PATH"; bash scripts/start-heartbeat-loop.sh 2>&1 | tee /tmp/clanworld-heartbeat.log'

# Verify it's firing
tail -F /tmp/clanworld-heartbeat.log
```

### 6. Web app (`apps/web` on Vercel)

Web reads state via Convex queries (NOT directly from chain). After updating Convex env (step 3) + clearing Convex state (step 4), the web auto-picks up the new state on next query.

**No rebuild required** unless the web reads a contract-specific build-time env var (e.g. `VITE_CLANWORLD_DIAMOND`). Check:

```bash
grep -r "VITE_.*DIAMOND\|VITE_.*CONTRACT" apps/web/src/
```

If any are present, update them in the Vercel project's env config:

```bash
vercel env rm VITE_CLANWORLD_DIAMOND production --yes
vercel env add VITE_CLANWORLD_DIAMOND production <<< "$NEW_DIAMOND"
# then redeploy
vercel --prod
```

Otherwise just hard-refresh the browser (Cmd+Shift+R) to bust caches.

### 7. clan-world-mobile (Android)

The native Compose app calls Convex via `CLANWORLD_CONVEX_URL` build-time env. Does NOT directly call the diamond. **No change needed** unless an in-app debug screen surfaces the contract address.

### 8. kickstart-token-tracker

Lives in its own repo (`clan-world/kickstart-token-tracker`). No diamond dependency. **No change needed.**

### 9. ABI mismatch handling (CRITICAL when migrating to OLDER diamond)

If the new diamond's contract version is older than what the indexer/web/mobile expects, ABI calls will fail or return garbage:

- `getWorldState()` returning fewer struct fields than expected
- Function selectors that don't exist on the older facets (revert with `Diamond: function does not exist`)
- Event signatures that differ (indexer decodes garbage)

**Mitigation steps:**

1. Identify which contract version the new diamond was deployed from. Check git log for the commit that produced its bytecode (correlate with deploy date if known).
2. Roll the indexer code back to that version, OR patch it to be tolerant of missing struct fields:
   - Wrap struct decodes in try/catch
   - Default missing fields to safe values (0, false, empty arrays)
3. Audit every `cast call` / `viem` ABI call in the codebase against the deployed selectors:
   ```bash
   cast call $DIAMOND "facets()((address,bytes4[])[])" --rpc-url $RPC_URL_PRIMARY \
     | grep -oE "0x[a-f0-9]{8}" | sort -u > /tmp/deployed-selectors.txt

   # Compare against expected selectors per the current ABI:
   cd packages/contracts && forge inspect ClanWorldDiamond methodIdentifiers \
     | jq -r 'keys[]' | sort -u > /tmp/expected-selectors.txt

   diff /tmp/deployed-selectors.txt /tmp/expected-selectors.txt
   ```
4. For any v-current selectors NOT on the deployed diamond, the calling code needs guards / fallbacks.

### 10. Mint new clans / inject resources / revive (admin-only)

Once admin access is confirmed (deployer == owner), the following recovery actions are available:

```bash
# Force rapid demo heartbeat (1s instead of 60s)
cast send $DIAMOND "setHeartbeatIntervalSeconds(uint64)" 1 \
  --rpc-url $RPC_URL_PRIMARY --private-key $DEPLOYER_PRIVATE_KEY

# Force rapid mission cooldown (1s)
cast send $DIAMOND "setClansmanCooldownSeconds(uint64)" 1 \
  --rpc-url $RPC_URL_PRIMARY --private-key $DEPLOYER_PRIVATE_KEY

# Mint a new clan
cast send $DIAMOND "mintClan(address)" $CLAN_OWNER_ADDR \
  --rpc-url $RPC_URL_PRIMARY --private-key $DEPLOYER_PRIVATE_KEY

# Spawn clansmen for a clan (revive dead, inject new)
cast send $DIAMOND "spawnClansman(uint32,uint32)" $CLAN_ID $COUNT \
  --rpc-url $RPC_URL_PRIMARY --private-key $DEPLOYER_PRIVATE_KEY

# Trigger forced bandit spawn (one-shot)
cast send $DIAMOND "triggerBanditSpawn()" \
  --rpc-url $RPC_URL_PRIMARY --private-key $DEPLOYER_PRIVATE_KEY
```

If admin is locked out (you can't transferOwnership + don't hold the owner key), the only public functions remain heartbeat / finalizeSeason / settleClan / per-clan-owner mission submissions.

---

## Quick smoke-test post-migration

```bash
# 1. Diamond is reachable + has expected clans
cast call $NEW_DIAMOND "getClanIds()(uint32[])" --rpc-url $RPC_URL_PRIMARY

# 2. World state is sane (currentTick, season state)
cast call $NEW_DIAMOND "getWorldState()" --rpc-url $RPC_URL_PRIMARY

# 3. Heartbeat is firing + producing TickAdvanced events
tail -F /tmp/clanworld-heartbeat.log

# 4. Convex worldSnapshot.tick is advancing
curl -sS "$CONVEX_URL/api/query" -H "Content-Type: application/json" \
  -d '{"path":"getSnapshot:getSnapshot","args":{}}' | jq '.value.tick'

# 5. Web UI shows fresh tick on next reload
open https://app.clan-world.com
```

If any step fails, work backwards:

- Web frozen → check Convex worldSnapshot
- Convex frozen → check chainEvents table for new TickAdvanced
- chainEvents stale → check heartbeat log
- Heartbeat reverting → check getWorldState for stuck conditions (worldPaused, season-finalize-pending)
- getWorldState reverts → ABI mismatch; see step 9

---

## Lessons learned from 2026-05-09 incident

- finalizeSeason() in current contract version requires more gas than Base Sepolia's per-tx limit allows when 8 clans need long settlement backlogs. Not recoverable via direct call.
- The capped `MAX_LAZY_SETTLE_BACKLOG` (200) bounds work per clan but doesn't bound total work across 8 clans + the ranking re-simulation.
- Backup diamonds need their owner key documented. The 0x2709eEB diamond's owner (0xB60679…) was undocumented and almost cost a full redeploy.
- The `/.vercel/project.json` files for app/landing should be tracked so new clones can deploy without re-linking.
- Reset Convex worldSnapshot when migrating diamonds — stale tick values block UI from reflecting reality.
