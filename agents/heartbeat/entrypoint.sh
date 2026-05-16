#!/usr/bin/env bash
# entrypoint.sh — heartbeat container main loop.
#
# Phase 1.10 (issue #353). Wires preflight (chain assertion + env validation)
# + cast send heartbeat() loop + HMAC-signed webhook POST + healthcheck
# timestamp file.
#
# Per the plan revision: CHAIN_NETWORK is REQUIRED (no fallback). Crash loud
# on missing env. `restart: on-failure:0` in compose keeps a failed preflight
# visible to the operator instead of crash-looping.
#
# Env contract (consumed):
#   CHAIN_NETWORK                    dev | prod (required)
#   DEV_RPC_URL                      required if CHAIN_NETWORK=dev
#   PROD_RPC_URL                     required if CHAIN_NETWORK=prod
#   CLAN_WORLD_CONTRACT_ADDRESS      diamond address
#   DEPLOYER_PRIVATE_KEY             caller key (env OR _FILE for compose secret)
#   WEBHOOK_SHARED_SECRET            HMAC key (env OR _FILE for compose secret)
#   CONVEX_DEPLOY_URL                base url for the Convex backend (used to
#                                    derive CONVEX_WEBHOOK_URL if not set)
#   CONVEX_WEBHOOK_URL               full POST URL (optional override)
#   HEARTBEAT_INTERVAL_SECONDS       loop interval (default 60)
#   HEARTBEAT_TIMESTAMP_FILE         path to touch on success (default
#                                    /tmp/last-heartbeat-success — Dockerfile)

# NOTE on errexit: the heartbeat tick itself is allowed to fail per-iteration
# (RPC blip, rate-limit, transient chain hiccup). We rely on explicit `|| ...`
# inside the loop to keep iterating. Preflight is sourced under set -e and
# will exit the entire process on any violation.
set -uo pipefail

log() { printf '[heartbeat] %s\n' "$*"; }
err() { printf '[heartbeat] ERROR: %s\n' "$*" >&2; }

# ---------------------------------------------------------------------------
# Compose secret file support (Finding 10 — secrets live in files, not env).
# For any var FOO that has a corresponding FOO_FILE pointing at a readable
# file, hydrate FOO from the file contents (newline-trimmed). This lets us
# keep WEBHOOK_SHARED_SECRET and DEPLOYER_PRIVATE_KEY out of `docker inspect`.
# ---------------------------------------------------------------------------
hydrate_from_file() {
  local var="$1"
  local file_var="${var}_FILE"
  local file_path="${!file_var:-}"
  if [ -n "$file_path" ] && [ -r "$file_path" ]; then
    # Trim ONLY trailing newlines from the file; preserve any internal bytes.
    local value
    value="$(tr -d '\r\n' < "$file_path")"
    if [ -n "$value" ]; then
      export "$var=$value"
      log "loaded $var from $file_path (${#value} chars)"
    fi
  fi
}

hydrate_from_file WEBHOOK_SHARED_SECRET
hydrate_from_file DEPLOYER_PRIVATE_KEY

# ---------------------------------------------------------------------------
# Resolve RPC_URL from CHAIN_NETWORK. NO cross-env fallback (Finding 48).
# ---------------------------------------------------------------------------
if [ -z "${CHAIN_NETWORK:-}" ]; then
  err "CHAIN_NETWORK is required (dev|prod) — aborting"
  exit 1
fi

case "$CHAIN_NETWORK" in
  dev)
    RPC_URL="${DEV_RPC_URL:-}"
    if [ -z "$RPC_URL" ]; then
      err "CHAIN_NETWORK=dev but DEV_RPC_URL is unset — aborting"
      exit 1
    fi
    ;;
  prod)
    RPC_URL="${PROD_RPC_URL:-}"
    if [ -z "$RPC_URL" ]; then
      err "CHAIN_NETWORK=prod but PROD_RPC_URL is unset — aborting"
      exit 1
    fi
    ;;
  *)
    err "CHAIN_NETWORK=$CHAIN_NETWORK is not one of {dev, prod} — aborting"
    exit 1
    ;;
esac
export RPC_URL

# Derive CONVEX_WEBHOOK_URL if not explicitly provided.
if [ -z "${CONVEX_WEBHOOK_URL:-}" ]; then
  if [ -z "${CONVEX_DEPLOY_URL:-}" ]; then
    err "neither CONVEX_WEBHOOK_URL nor CONVEX_DEPLOY_URL is set — aborting"
    exit 1
  fi
  # Strip trailing slashes, then append the canonical path.
  base="${CONVEX_DEPLOY_URL%/}"
  CONVEX_WEBHOOK_URL="${base}/api/heartbeat-webhook"
fi
export CONVEX_WEBHOOK_URL

# ---------------------------------------------------------------------------
# Preflight — chain-ID assertion + owner check + env sanity. This is the only
# block under strict set -e; loop body below tolerates per-iteration failure.
# ---------------------------------------------------------------------------
set -e
# shellcheck disable=SC1091  # path is image-internal, resolved at runtime
. /opt/clan-world/heartbeat/preflight.sh
set +e

# shellcheck disable=SC1091
. /opt/clan-world/heartbeat/webhook-sign.sh

INTERVAL_SECONDS="${HEARTBEAT_INTERVAL_SECONDS:-60}"
TIMESTAMP_FILE="${HEARTBEAT_TIMESTAMP_FILE:-/tmp/last-heartbeat-success}"

log "starting loop — interval=${INTERVAL_SECONDS}s"
log "  diamond: $CLAN_WORLD_CONTRACT_ADDRESS"
log "  rpc:     $RPC_URL"
log "  webhook: $CONVEX_WEBHOOK_URL"

# ---------------------------------------------------------------------------
# Webhook POST helper — emits the signed POST + a single retry on transient
# failure. Returns 0 on success, non-zero on both attempts failing. Logging
# is verbose so operators can grep success/fail without parsing curl output.
# ---------------------------------------------------------------------------
post_webhook() {
  local body="$1"
  local ts hmac sig
  ts="$(date +%s)"
  hmac="$(heartbeat_sign "$WEBHOOK_SHARED_SECRET" "$ts" "$body")" || {
    err "HMAC computation failed"
    return 1
  }
  sig="t=${ts},v1=${hmac}"

  local attempt rc=0
  for attempt in 1 2; do
    if curl -sS --fail --max-time 10 \
        -X POST "$CONVEX_WEBHOOK_URL" \
        -H "Content-Type: application/json" \
        -H "X-Heartbeat-Signature: $sig" \
        --data-raw "$body" \
        >/dev/null; then
      log "webhook POST ok (attempt $attempt)"
      return 0
    else
      rc=$?
      err "webhook POST failed (attempt $attempt, curl rc=$rc)"
      if [ "$attempt" -eq 1 ]; then
        sleep 5
      fi
    fi
  done
  return 1
}

# ---------------------------------------------------------------------------
# Main loop — cast send + parse tx + POST webhook + touch healthcheck file.
# Per-iteration failure is logged + tolerated; the next tick recovers.
# ---------------------------------------------------------------------------
while true; do
  iter_start="$(date '+%Y-%m-%d %H:%M:%S %Z')"

  cast_stderr="$(mktemp)"
  cast_json="$(mktemp)"

  if cast send "$CLAN_WORLD_CONTRACT_ADDRESS" "heartbeat()" \
      --rpc-url "$RPC_URL" \
      --private-key "$DEPLOYER_PRIVATE_KEY" \
      --json \
      > "$cast_json" \
      2> "$cast_stderr"; then

    # Parse tx hash + block number from cast --json output. cast's exact key
    # names have drifted historically — accept either transactionHash or hash.
    tx_hash="$(jq -r '.. | objects | .transactionHash? // .hash? // empty' "$cast_json" \
      | grep -E '^0x[0-9a-fA-F]{64}$' | tail -1 || true)"
    block_hex="$(jq -r '.. | objects | .blockNumber? // empty' "$cast_json" \
      | tail -1 || true)"

    if [ -z "$tx_hash" ]; then
      err "$iter_start tick succeeded but tx hash unparseable — webhook skipped"
      err "cast --json output: $(tr -d '\n' < "$cast_json" | head -c 400)"
    else
      if [[ "$block_hex" =~ ^0x[0-9a-fA-F]+$ ]]; then
        block_number="$((block_hex))"
      elif [[ "$block_hex" =~ ^[0-9]+$ ]]; then
        block_number="$block_hex"
      else
        block_number="null"
      fi

      # AGENTS.md payload contract (Finding 28 — no `tick` field, Convex
      # re-derives via currentTick()). Keys sorted so the body is canonical
      # for HMAC determinism (Convex receiver must serialise identically).
      body="$(jq -c -n \
        --arg chain "base-sepolia" \
        --arg engineAddress "$CLAN_WORLD_CONTRACT_ADDRESS" \
        --arg txHash "$tx_hash" \
        --argjson blockNumber "${block_number}" \
        --argjson firedAtTs "$(date +%s)" \
        --arg source "heartbeat-container" \
        '{chain:$chain, engineAddress:$engineAddress, txHash:$txHash, blockNumber:$blockNumber, firedAtTs:$firedAtTs, source:$source}')"

      log "$iter_start tick ok tx=${tx_hash:0:18}... block=${block_number}"

      if post_webhook "$body"; then
        # Healthcheck contract: touch only after BOTH the chain tick AND
        # webhook delivery succeed end-to-end. The compose healthcheck reads
        # the mtime and flags the container unhealthy after 120s of staleness.
        date +%s > "$TIMESTAMP_FILE" || true
      else
        err "$iter_start webhook POST failed both attempts — not touching healthcheck file"
      fi
    fi

  else
    rc=$?
    stderr_tail="$(tail -c 400 "$cast_stderr" 2>/dev/null || true)"
    if grep -qi "RateLimited" "$cast_stderr"; then
      log "$iter_start tick rate-limited; continuing"
    elif grep -qi "HeartbeatGuard\|TooEarly\|guard" "$cast_stderr"; then
      log "$iter_start tick guarded by on-chain interval; continuing"
    else
      err "$iter_start tick failed (rc=$rc): $stderr_tail"
    fi
  fi

  rm -f "$cast_stderr" "$cast_json"
  sleep "$INTERVAL_SECONDS"
done
