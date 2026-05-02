# Convex Real Indexer

The v1.0.1 indexer is feature-flagged so the fake heartbeat path can stay in
place during production soak.

## Flags

- `CLANWORLD_USE_REAL_INDEXER=true` enables receipt decoding in the heartbeat
  webhook, the 5s snapshot refresher, and the 3s log poller.
- `CLANWORLD_USE_FAKE_HEARTBEAT=true` keeps the MUST-13 fake tick cron alive for
  demos and fallback environments.
- Do not enable both in production. They coexist only so migration and rollback
  do not require deleting code.

## Required Env

- `RPC_URL_PRIMARY`: RPC used by Convex actions for receipts, logs, and snapshot
  reads.
- `CLAN_WORLD_CONTRACT_ADDRESS`: engine address to read and filter logs.
- `WEBHOOK_SHARED_SECRET`: bearer token shared by the heartbeat loop and Convex
  webhook.

## Migration Path

1. Deploy with both flags unset and verify existing fake paths still build.
2. Set `CLANWORLD_USE_REAL_INDEXER=true` in the Convex deployment.
3. Update `scripts/start-heartbeat-loop.sh` callers so webhook payloads include
   `txHash` and `blockNumber`.
4. Let `chainEvents`, projection tables, and `eventCheckpoint` run for a while.
5. In v1.1+, remove fake tick code after production confidence is boring.
