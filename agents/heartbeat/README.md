# clanworld/heartbeat — operator notes

Phase 1.10 of `docs/plans/dockerize-elder-infra-v1.md` (issue #353).

The heartbeat container ticks `heartbeat()` on the diamond every
`HEARTBEAT_INTERVAL_SECONDS` (default 60) and, after each successful on-chain
tick, posts an HMAC-SHA256-signed webhook to the self-hosted Convex backend.

This replaces the host-side `clanworld-heartbeat-loop` once the dockerized
stack is the source of truth (per the Phase 2 migration runbook).

## Image

Pure Debian-slim + `bash`, `cast` (foundry), `curl`, `jq`, `openssl`, `tini`.
No node runtime. Built and tagged `clanworld/heartbeat:dev` by docker-compose
during `make up PROFILE=dev`.

```bash
docker build -t clanworld/heartbeat:dev agents/heartbeat/
```

## Required env (container)

| Var                            | Notes                                                  |
| ------------------------------ | ------------------------------------------------------ |
| `CHAIN_NETWORK`                | `dev` or `prod` (no default — fail fast)               |
| `DEV_RPC_URL` / `PROD_RPC_URL` | one is required, matching `CHAIN_NETWORK`              |
| `CLAN_WORLD_CONTRACT_ADDRESS`  | diamond address                                        |
| `DEPLOYER_PRIVATE_KEY`         | caller key — supports `_FILE` via compose secrets      |
| `WEBHOOK_SHARED_SECRET`        | HMAC key — supports `_FILE` via compose secrets        |
| `CONVEX_DEPLOY_URL`            | derives `CONVEX_WEBHOOK_URL` if not set explicitly     |

Optional:

| Var                          | Default                                              |
| ---------------------------- | ---------------------------------------------------- |
| `CONVEX_WEBHOOK_URL`         | `${CONVEX_DEPLOY_URL%/}/api/heartbeat-webhook`       |
| `HEARTBEAT_INTERVAL_SECONDS` | `60`                                                 |
| `HEARTBEAT_TIMESTAMP_FILE`   | `/tmp/last-heartbeat-success`                        |

## Preflight invariants (all fail-loud)

1. `CHAIN_NETWORK ∈ {dev, prod}` and the matching RPC URL is non-empty.
2. `CLAN_WORLD_CONTRACT_ADDRESS` matches `^0x[0-9a-fA-F]{40}$`.
3. `cast chain-id --rpc-url $RPC_URL` returns a number AND, per profile:
   - `dev` → observed `∈ {84532, 31337}` (Base Sepolia or local anvil)
   - `prod` → observed `== 84532` (Base Sepolia)
4. `cast call $DIAMOND 'owner()(address)'` returns a non-zero address.
5. `DEPLOYER_PRIVATE_KEY` parses (`cast wallet address` succeeds).
6. `WEBHOOK_SHARED_SECRET` is at least 32 chars.
7. `CONVEX_WEBHOOK_URL` starts with `http://` or `https://`.

Any violation exits non-zero. Compose policy `restart: on-failure:0` keeps the
container in a visible failed state for operator intervention.

## Webhook payload

```json
{
  "chain": "base-sepolia",
  "engineAddress": "0x...diamond...",
  "txHash": "0x...",
  "blockNumber": 12345,
  "firedAtTs": 1716000000,
  "source": "heartbeat-container"
}
```

No `tick` field — Convex re-derives via `currentTick()` (AGENTS.md contract;
plan Finding 28).

## HMAC signing

`v1 = HMAC-SHA256(secret, "${t}.${body}")` in hex. Header:

```
X-Heartbeat-Signature: t=<unix-ts>,v1=<hex-hmac>
```

The Convex receiver validates `|now - t| ≤ 60s` (replay window) and that `v1`
matches over the exact body bytes. Body is built via
`jq -c -n --argjson ... '{...}'` so key ordering is canonical and free of
trailing whitespace. The signer (`webhook-sign.sh`) deliberately uses
`printf '%s'` (no trailing newline) so the server can sign over identical
bytes.

> **Caveat (in-flight).** The current Convex receiver at
> `apps/server/convex/http.ts` validates a flat `Authorization: Bearer
> $WEBHOOK_SHARED_SECRET` header — the HMAC + replay-window receiver-side
> migration is a separate change called out in the issue's "Files affected"
> list. Until that lands, run the receiver in compatibility mode (or land both
> changes in the same merge). See `webhook-sign.sh` for the canonical signing
> code the receiver must mirror.

## Webhook POST behaviour

- Method: `POST`
- Timeout: `--max-time 10`
- One retry after a 5-second backoff on transient failure.
- Webhook-side failure does **not** touch the healthcheck timestamp file —
  this propagates the failure to `docker compose ps` after ~120s of staleness
  even if the chain ticks keep working.

## Healthcheck

The compose service uses:

```bash
test -f /tmp/last-heartbeat-success \
  && [ $(($(date +%s) - $(cat /tmp/last-heartbeat-success))) -lt 120 ]
```

The container touches `/tmp/last-heartbeat-success` only after BOTH the chain
tick AND the webhook POST succeed end-to-end.

## On-chain tick failure modes (tolerated, not aborted)

- `RateLimited` — RPC rate limit, common on free Base Sepolia tier.
- `HeartbeatGuard` / `TooEarly` — the engine's on-chain interval guard fired
  (intended — we tick more often than the on-chain minimum during dev iteration).
- Generic RPC blip — logged with `[heartbeat] ERROR:` prefix.

Per-iteration failure does **not** crash the container; the next interval
will retry.

## Local testing

```bash
docker build -t clanworld/heartbeat:dev agents/heartbeat/

# Preflight fail-loud: missing CHAIN_NETWORK
docker run --rm clanworld/heartbeat:dev
# expect: [heartbeat] ERROR: CHAIN_NETWORK is required (dev|prod) — aborting

# Preflight fail-loud: missing DEV_RPC_URL
docker run --rm -e CHAIN_NETWORK=dev clanworld/heartbeat:dev
# expect: [heartbeat] ERROR: CHAIN_NETWORK=dev but DEV_RPC_URL is unset — aborting

# Standalone HMAC check
docker run --rm --entrypoint /opt/clan-world/heartbeat/webhook-sign.sh \
  clanworld/heartbeat:dev \
  "$(openssl rand -hex 32)" \
  "$(date +%s)" \
  '{"chain":"base-sepolia"}'
# expect: 64 hex chars
```
