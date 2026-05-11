# Clan World — Full Game Reset Runbook

Use this when we want a totally fresh live realm: new Base Sepolia diamond, backup diamond, empty Convex state, regenerated contract artifacts, restarted heartbeat/runner/Elder sessions, and a fresh frontend deploy.

This is the "do the whole reset now" checklist. For deeper setup details, see:

- `docs/runbooks/fresh-vps-bootstrap.md`
- `docs/runbooks/base-sepolia-deployment.md`
- `docs/runbooks/diamond-migration.md`
- `docs/guides/stream-ops.md`

## Reset Inputs

Record these before touching live state:

```bash
export RPC_URL_PRIMARY="https://base-sepolia.infura.io/v3/<key>"
export SOLANA_RPC_URL="https://mainnet.helius-rpc.com/?api-key=<key>"
export DEPLOYER_PRIVATE_KEY="0x..."
export WEBHOOK_SHARED_SECRET="..."
```

The active reset on 2026-05-11 used Infura for Base Sepolia and Helius for Solana reads.

## 0. Preflight

1. Work from a clean checkout on `main`.
2. Confirm `pnpm install --frozen-lockfile`, `forge build`, and `pnpm typecheck` are green.
3. Confirm these CLIs are authenticated or available: `forge`, `cast`, `pnpm`, `npx convex`, `vercel`, `tmux`, `jq`, `curl`.
4. Check deployer and elder wallet balances on Base Sepolia.
5. Confirm Convex deployment slug and app URLs.
6. Save the current active diamond, lens, deploy block, and git commit in the reset notes.

## 1. Stop Live Writers

Stop anything that can send deployer-wallet txs or write stale state while the reset is underway:

```bash
tmux kill-session -t clanworld-heartbeat 2>/dev/null || true
tmux kill-session -t clanworld-runner 2>/dev/null || true
tmux kill-session -t clanworld-finalize 2>/dev/null || true

for n in 1 2 3 4; do
  tmux kill-session -t "elder-$n" 2>/dev/null || true
  tmux kill-session -t "clanworld-elder-$n" 2>/dev/null || true
done
```

Wait at least 60 seconds after stopping heartbeat before deploying, so any in-flight nonce settles.

## 2. Deploy Active And Backup Diamonds

Deploy the active diamond:

```bash
cd packages/contracts
forge build
forge script script/Deploy.s.sol \
  --rpc-url "$RPC_URL_PRIMARY" \
  --private-key "$DEPLOYER_PRIVATE_KEY" \
  --broadcast \
  --slow
```

Save:

- Diamond address
- Lens address
- Deploy transaction hash
- Deploy block number
- Facet addresses

Then run the same deploy command again for the backup diamond and save the same values. Do not point Convex or the frontend at the backup unless the active diamond fails.

## 3. Wire Local Env

Update local runtime env files:

```bash
export CLAN_WORLD_CONTRACT_ADDRESS="0x<active-diamond>"
export CLAN_WORLD_LENS_ADDRESS="0x<active-lens>"
export INDEXER_START_BLOCK="<active-deploy-block>"

# Root runtime env
perl -0pi -e 's/^RPC_URL_PRIMARY=.*/RPC_URL_PRIMARY=$ENV{RPC_URL_PRIMARY}/m' .env.local
perl -0pi -e 's/^SOLANA_RPC_URL=.*/SOLANA_RPC_URL=$ENV{SOLANA_RPC_URL}/m' .env.local
perl -0pi -e 's/^CLAN_WORLD_CONTRACT_ADDRESS=.*/CLAN_WORLD_CONTRACT_ADDRESS=$ENV{CLAN_WORLD_CONTRACT_ADDRESS}/m' .env.local
perl -0pi -e 's/^CLAN_WORLD_LENS_ADDRESS=.*/CLAN_WORLD_LENS_ADDRESS=$ENV{CLAN_WORLD_LENS_ADDRESS}/m' .env.local

# Elder runtime dirs, if present
for n in 1 2 3 4; do
  test -f "$HOME/clan-world/elder-$n/.env" || continue
  perl -0pi -e 's/^RPC_URL_PRIMARY=.*/RPC_URL_PRIMARY=$ENV{RPC_URL_PRIMARY}/m' "$HOME/clan-world/elder-$n/.env"
  perl -0pi -e 's/^CLAN_WORLD_CONTRACT_ADDRESS=.*/CLAN_WORLD_CONTRACT_ADDRESS=$ENV{CLAN_WORLD_CONTRACT_ADDRESS}/m' "$HOME/clan-world/elder-$n/.env"
done
```

If an env key is missing, add it rather than leaving an old default in place.

## 4. Regenerate Artifacts And Types

Refresh contract outputs and TypeScript generated files after the deploy source is final:

```bash
pnpm gen:contract-abi
pnpm gen:chainclient-abi
pnpm --filter @clan-world/server codegen
pnpm typecheck
```

Commit any generated ABI/type changes before redeploying the app.

## 5. Reconfigure And Flush Convex

Set Convex production env values together:

```bash
cd apps/server
npx convex env set RPC_URL_PRIMARY "$RPC_URL_PRIMARY" --prod
npx convex env set SOLANA_RPC_URL "$SOLANA_RPC_URL" --prod
npx convex env set CLAN_WORLD_CONTRACT_ADDRESS "$CLAN_WORLD_CONTRACT_ADDRESS" --prod
npx convex env set CLAN_WORLD_LENS_ADDRESS "$CLAN_WORLD_LENS_ADDRESS" --prod
npx convex env set INDEXER_START_BLOCK "$INDEXER_START_BLOCK" --prod
npx convex env set CLANWORLD_USE_REAL_INDEXER true --prod
npx convex env set CLANWORLD_USE_FAKE_HEARTBEAT false --prod
npx convex env set WEBHOOK_SHARED_SECRET "$WEBHOOK_SHARED_SECRET" --prod
npx convex deploy --prod
```

Flush all game/indexer state for the old realm. At minimum clear:

- `worldSnapshot`
- `chainEvents`
- `tickHistory`
- `eventCheckpoint`
- `clanView`
- `marketState`
- `banditView`
- `pricePoint`
- `goldQuote`
- `goldQuoteSample`
- `kickstartTokens`
- `kickstartWatchedTokens`
- `agentLogs`
- `inftTokens`
- `inftTransfers`
- `memoryEntries`
- `bulletins`
- `memoryEvents`
- `whispers`
- `orchEvents`
- `humanSteeringMessages`

Then trigger one indexer refresh or wait for the first heartbeat webhook.

## 6. Mint Four Clans

Derive or load the four elder addresses, then mint one clan per elder:

```bash
for owner in "$ELDER_1_ADDRESS" "$ELDER_2_ADDRESS" "$ELDER_3_ADDRESS" "$ELDER_4_ADDRESS"; do
  cast send "$CLAN_WORLD_CONTRACT_ADDRESS" "mintClan(address)" "$owner" \
    --rpc-url "$RPC_URL_PRIMARY" \
    --private-key "$DEPLOYER_PRIVATE_KEY"
done
```

If the demo needs clansmen immediately, spawn the expected starting units after minting:

```bash
for clan_id in 1 2 3 4; do
  for i in 1 2 3 4; do
    cast send "$CLAN_WORLD_CONTRACT_ADDRESS" "spawnClansman(uint32,uint32)" "$clan_id" "$i" \
      --rpc-url "$RPC_URL_PRIMARY" \
      --private-key "$DEPLOYER_PRIVATE_KEY"
  done
done
```

Verify:

```bash
cast call "$CLAN_WORLD_CONTRACT_ADDRESS" "getClanIds()(uint32[])" --rpc-url "$RPC_URL_PRIMARY"
```

## 7. Restart Heartbeat, Runner, Elders, And TTYD

Restart heartbeat:

```bash
tmux new-session -d -s clanworld-heartbeat -c "$PWD" \
  'export PATH="$HOME/.foundry/bin:$PATH"; bash scripts/start-heartbeat-loop.sh 2>&1 | tee -a /tmp/clanworld-heartbeat.log'
```

Restart runner and four Elder sessions using the current package scripts or host wrappers. Confirm each new process reads the new `.env.local` / elder `.env` values.

For TTYD or any service binding a port, resolve the current worktree port first:

```bash
PORT=$(port-for clan-world-ttyd)
```

Then start the service with that port.

Clear old runner/Elder inbox artifacts before the first tick if they contain stale situation blocks, old diamond addresses, or old clan IDs.

## 8. Deploy Web App

Deploy the frontend after Convex has the new env and fresh state:

```bash
pnpm build
vercel --prod
```

If any Vercel project env var points directly at the diamond or lens, update it before deploying. The main game frontend should normally read the realm through Convex.

## 9. Smoke Tests

Verify all of these before calling the reset complete:

```bash
cast code "$CLAN_WORLD_CONTRACT_ADDRESS" --rpc-url "$RPC_URL_PRIMARY"
cast call "$CLAN_WORLD_CONTRACT_ADDRESS" "owner()(address)" --rpc-url "$RPC_URL_PRIMARY"
cast call "$CLAN_WORLD_CONTRACT_ADDRESS" "getClanIds()(uint32[])" --rpc-url "$RPC_URL_PRIMARY"
tail -n 80 /tmp/clanworld-heartbeat.log
tmux ls
```

Also verify in the app:

- New tick number advances.
- Four clans exist.
- Four Elder agents are alive and assigned to the new clans.
- Convex world snapshot references the new diamond.
- TTYD terminals are reachable.

## 10. Post-Reset Notes

After the live reset, update this section or a dated reset report with:

- Active diamond and lens
- Backup diamond and lens
- Deploy block numbers
- Git commit and release tag
- Convex deployment slug
- Vercel production URL
- Any commands that differed from this runbook
- Any failures, rate limits, missing env vars, or process names discovered during the run
