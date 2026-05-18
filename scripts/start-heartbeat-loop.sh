#!/usr/bin/env bash
# chmod +x scripts/start-heartbeat-loop.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

export PATH="$HOME/.foundry/bin:$PATH"

# Source .env.local from repo root if it exists
if [ -f .env.local ]; then
  set -a
  # shellcheck source=/dev/null
  source .env.local
  set +a
fi

# Validate required env vars
REQUIRED_VARS=(
  RPC_URL_PRIMARY
  CONVEX_DEPLOY_URL
  WEBHOOK_SHARED_SECRET
  DEPLOYER_PRIVATE_KEY
  CLAN_WORLD_CONTRACT_ADDRESS
)

missing=0
for var in "${REQUIRED_VARS[@]}"; do
  if [ -z "${!var:-}" ]; then
    echo "ERROR: required env var $var is not set" >&2
    missing=1
  fi
done

if [ "$missing" -eq 1 ]; then
  echo ""
  echo "Usage: set the following env vars before running (or put them in .env.local):"
  for var in "${REQUIRED_VARS[@]}"; do
    echo "  $var"
  done
  exit 1
fi
if ! command -v jq >/dev/null 2>&1; then
  echo "ERROR: required executable jq is not installed" >&2
  exit 1
fi

HEARTBEAT_INTERVAL=$(grep "^export const HEARTBEAT_INTERVAL_SECONDS" packages/sdk/src/generated/constants.ts | grep -oE '[0-9]+')
HEARTBEAT_SLEEP_SECONDS="${HEARTBEAT_SLEEP_SECONDS:-$((HEARTBEAT_INTERVAL + 5))}"
CONVEX_WEBHOOK_URL="${CONVEX_WEBHOOK_URL:-${CONVEX_DEPLOY_URL/.convex.cloud/.convex.site}}"

echo "Starting heartbeat loop (interval: ${HEARTBEAT_SLEEP_SECONDS}s; avoids on-chain ${HEARTBEAT_INTERVAL}s heartbeat guard)"
echo "  engine: $CLAN_WORLD_CONTRACT_ADDRESS"
echo "  rpc:    $RPC_URL_PRIMARY"
echo "  convex: $CONVEX_DEPLOY_URL"
echo "  webhook: $CONVEX_WEBHOOK_URL"

while true; do
  heartbeat_stderr="$(mktemp)"
  heartbeat_json="$(mktemp)"
  if ! cast send "$CLAN_WORLD_CONTRACT_ADDRESS" "heartbeat()" \
    --rpc-url "$RPC_URL_PRIMARY" \
    --private-key "$DEPLOYER_PRIVATE_KEY" \
    --json \
    > "$heartbeat_json" \
    2> >(tee "$heartbeat_stderr" >&2); then
    if grep -qi "RateLimited" "$heartbeat_stderr"; then
      echo "heartbeat rate-limited; continuing" >&2
    else
      rm -f "$heartbeat_stderr" "$heartbeat_json"
      exit 1
    fi
  fi
  rm -f "$heartbeat_stderr"

  tx_hash="$(jq -r '.. | objects | .transactionHash? // .hash? // empty' "$heartbeat_json" | grep -E '^0x[0-9a-fA-F]{64}$' | tail -1)"
  block_number_hex="$(jq -r '.. | objects | .blockNumber? // empty' "$heartbeat_json" | tail -1)"
  rm -f "$heartbeat_json"

  if [ -z "$tx_hash" ]; then
    echo "heartbeat tx hash unavailable from cast JSON; webhook POST skipped" >&2
    sleep "$HEARTBEAT_SLEEP_SECONDS"
    continue
  fi
  if [[ "$block_number_hex" =~ ^0x[0-9a-fA-F]+$ ]]; then
    block_number="$((block_number_hex))"
  elif [[ "$block_number_hex" =~ ^[0-9]+$ ]]; then
    block_number="$block_number_hex"
  else
    block_number="null"
  fi

  curl -sS --fail -X POST "$CONVEX_WEBHOOK_URL/api/heartbeat-webhook" \
    -H "Authorization: Bearer $WEBHOOK_SHARED_SECRET" \
    -H "Content-Type: application/json" \
    -d "{\"chain\":\"base-sepolia\",\"engineAddress\":\"$CLAN_WORLD_CONTRACT_ADDRESS\",\"txHash\":\"$tx_hash\",\"blockNumber\":$block_number,\"firedAtTs\":$(date +%s),\"source\":\"foundry-loop\"}" \
    || echo "webhook POST failed (continuing)" >&2

  # Add a small cushion to avoid rate-limit collisions with the on-chain heartbeat guard.
  sleep "$HEARTBEAT_SLEEP_SECONDS"
done
