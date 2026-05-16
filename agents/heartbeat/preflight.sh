#!/usr/bin/env bash
# preflight.sh — env validation + chain-ID assertion + diamond owner check.
#
# Phase 1.10 (issue #353). Runs ONCE at container start before the heartbeat
# loop begins. Fails loud (non-zero exit) on any invariant violation so the
# `restart: on-failure:0` compose policy keeps the failure visible to the
# operator instead of crash-looping silently.
#
# Required env (all must be non-empty):
#   CHAIN_NETWORK                  dev | prod
#   RPC_URL                        resolved by entrypoint.sh from DEV/PROD_RPC_URL
#   CLAN_WORLD_CONTRACT_ADDRESS    diamond address (0x...)
#   DEPLOYER_PRIVATE_KEY           caller key (any wallet with sufficient ETH +
#                                  caller authorization on the engine)
#   WEBHOOK_SHARED_SECRET          HMAC-SHA256 key for the Convex webhook
#   CONVEX_WEBHOOK_URL             POST target, e.g. http://convex-backend:3210/api/heartbeat-webhook
#
# Optional env:
#   HEARTBEAT_INTERVAL_SECONDS     loop interval; defaults to 60 in entrypoint
#
# Chain-ID assertion:
#   CHAIN_NETWORK=dev   →  observed chain_id ∈ {84532, 31337}
#   CHAIN_NETWORK=prod  →  observed chain_id == 84532  (Base Sepolia)

set -euo pipefail

log() { printf '[preflight] %s\n' "$*"; }
err() { printf '[preflight] ERROR: %s\n' "$*" >&2; }

# Support file-based secrets (Docker secret mounts). entrypoint.sh hydrates
# these before calling preflight, but handle them here too for standalone use.
if [[ -z "${DEPLOYER_PRIVATE_KEY:-}" && -n "${DEPLOYER_PRIVATE_KEY_FILE:-}" && -r "${DEPLOYER_PRIVATE_KEY_FILE}" ]]; then
  DEPLOYER_PRIVATE_KEY="$(cat "${DEPLOYER_PRIVATE_KEY_FILE}")"
  export DEPLOYER_PRIVATE_KEY
fi
if [[ -z "${WEBHOOK_SHARED_SECRET:-}" && -n "${WEBHOOK_SHARED_SECRET_FILE:-}" && -r "${WEBHOOK_SHARED_SECRET_FILE}" ]]; then
  WEBHOOK_SHARED_SECRET="$(cat "${WEBHOOK_SHARED_SECRET_FILE}")"
  export WEBHOOK_SHARED_SECRET
fi

# ---------------------------------------------------------------------------
# 1. Required env vars
# ---------------------------------------------------------------------------
required_vars=(
  CHAIN_NETWORK
  RPC_URL
  CLAN_WORLD_CONTRACT_ADDRESS
  DEPLOYER_PRIVATE_KEY
  WEBHOOK_SHARED_SECRET
  CONVEX_WEBHOOK_URL
)

missing=0
for v in "${required_vars[@]}"; do
  if [ -z "${!v:-}" ]; then
    err "required env var $v is unset or empty"
    missing=1
  fi
done

if [ "$missing" -ne 0 ]; then
  err "aborting — fix env and restart"
  exit 1
fi

# ---------------------------------------------------------------------------
# 2. CHAIN_NETWORK whitelist (Finding 9 — no fallback)
# ---------------------------------------------------------------------------
case "$CHAIN_NETWORK" in
  dev|prod) ;;
  *)
    err "CHAIN_NETWORK=$CHAIN_NETWORK is not one of {dev, prod}"
    exit 1
    ;;
esac

# ---------------------------------------------------------------------------
# 3. Diamond address format
# ---------------------------------------------------------------------------
if ! [[ "$CLAN_WORLD_CONTRACT_ADDRESS" =~ ^0x[0-9a-fA-F]{40}$ ]]; then
  err "CLAN_WORLD_CONTRACT_ADDRESS is not a 0x-prefixed 40-hex-char address: $CLAN_WORLD_CONTRACT_ADDRESS"
  exit 1
fi

# ---------------------------------------------------------------------------
# 4. Chain ID assertion — fail loud if observed != expected for profile
# ---------------------------------------------------------------------------
log "querying chain_id from $RPC_URL ..."
observed_chain_id="$(cast chain-id --rpc-url "$RPC_URL" 2>&1)" || {
  err "cast chain-id failed against $RPC_URL: $observed_chain_id"
  exit 1
}

if ! [[ "$observed_chain_id" =~ ^[0-9]+$ ]]; then
  err "cast chain-id returned non-numeric value: $observed_chain_id"
  exit 1
fi

case "$CHAIN_NETWORK" in
  dev)
    # dev tolerates Base-Sepolia (forked via anvil) OR plain anvil chain-id.
    if [ "$observed_chain_id" != "84532" ] && [ "$observed_chain_id" != "31337" ]; then
      err "CHAIN_NETWORK=dev expects chain_id ∈ {84532, 31337}; got $observed_chain_id"
      err "abort — probable RPC misconfiguration"
      exit 1
    fi
    ;;
  prod)
    if [ "$observed_chain_id" != "84532" ]; then
      err "CHAIN_NETWORK=prod expects chain_id = 84532 (Base Sepolia); got $observed_chain_id"
      err "abort — pointing prod heartbeat at non-Base-Sepolia is unsafe"
      exit 1
    fi
    ;;
esac

log "chain_id ok — $observed_chain_id (CHAIN_NETWORK=$CHAIN_NETWORK)"

# ---------------------------------------------------------------------------
# 5. Diamond owner check — confirm contract responds to owner() and is
#    not the zero-address (catches: wrong address, undeployed, fresh anvil
#    without contract code at the address).
# ---------------------------------------------------------------------------
log "checking diamond owner() at $CLAN_WORLD_CONTRACT_ADDRESS ..."
owner_raw="$(cast call "$CLAN_WORLD_CONTRACT_ADDRESS" 'owner()(address)' \
  --rpc-url "$RPC_URL" 2>&1)" || {
  err "cast call owner() failed: $owner_raw"
  err "is the diamond deployed at $CLAN_WORLD_CONTRACT_ADDRESS on chain_id $observed_chain_id?"
  exit 1
}

# cast normally emits a 0x-prefixed address; the literal zero address means
# the diamond either has no owner set OR the call returned uninitialised
# storage (i.e. there is no contract at that slot).
owner="${owner_raw,,}"
if [ "$owner" = "0x0000000000000000000000000000000000000000" ]; then
  err "diamond.owner() == zero-address — contract probably not deployed"
  exit 1
fi

log "diamond owner: $owner"

# ---------------------------------------------------------------------------
# 6. Caller (private key) sanity — derive the address and log it so the
#    operator can sanity-check the funding account on the chain explorer.
# ---------------------------------------------------------------------------
caller="$(cast wallet address --private-key "$DEPLOYER_PRIVATE_KEY" 2>/dev/null)" || {
  err "DEPLOYER_PRIVATE_KEY is malformed — cast wallet address rejected it"
  exit 1
}
log "caller wallet: $caller"

# ---------------------------------------------------------------------------
# 7. Webhook secret length sanity — openssl rand -hex 32 produces 64 chars.
#    Anything <32 chars is almost certainly a misconfiguration (e.g. a
#    placeholder copied from the example without rotation).
# ---------------------------------------------------------------------------
secret_len="${#WEBHOOK_SHARED_SECRET}"
if [ "$secret_len" -lt 32 ]; then
  err "WEBHOOK_SHARED_SECRET is suspiciously short ($secret_len chars) — refusing to start"
  err "expected ≥32 chars; bootstrap via 'make -C agents bootstrap-bus-secrets'"
  exit 1
fi

# ---------------------------------------------------------------------------
# 8. Webhook URL format — must be http(s) and end with /api/heartbeat-webhook
#    (defensive — surfaces typos before the first tick).
# ---------------------------------------------------------------------------
if ! [[ "$CONVEX_WEBHOOK_URL" =~ ^https?:// ]]; then
  err "CONVEX_WEBHOOK_URL must be http:// or https:// — got: $CONVEX_WEBHOOK_URL"
  exit 1
fi

log "preflight ok — entering heartbeat loop"
