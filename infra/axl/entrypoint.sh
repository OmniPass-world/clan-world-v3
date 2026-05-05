#!/bin/sh
set -e

KEYSTORE=/keystore
KEY_FILE="$KEYSTORE/private.pem"

# Generate ed25519 key if not present (persisted via volume)
if [ ! -f "$KEY_FILE" ]; then
  echo "[axl-entrypoint] generating ed25519 keypair..."
  openssl genpkey -algorithm ed25519 -out "$KEY_FILE"
  chmod 600 "$KEY_FILE"
fi

# Build node-config.json — peer with sibling node(s) if AXL_PEER_ADDR(S) is set.
PEERS="[]"
PEER_ADDRS="${AXL_PEER_ADDRS:-${AXL_PEER_ADDR:-}}"
if [ -n "$PEER_ADDRS" ]; then
  PEERS=$(
    printf '%s\n' "$PEER_ADDRS" |
      awk -v RS=',' '
        BEGIN { printf "["; first=1 }
        {
          gsub(/^[[:space:]]+|[[:space:]]+$/, "", $0)
          if ($0 == "") next
          if (!first) printf ","
          printf "\"tls://%s\"", $0
          first=0
        }
        END { printf "]" }
      '
  )
fi

cat > "$KEYSTORE/node-config.json" <<EOF
{
  "PrivateKeyPath": "$KEY_FILE",
  "Peers": $PEERS,
  "Listen": ["tls://0.0.0.0:9001"],
  "bridge_addr": "0.0.0.0"
}
EOF

echo "[axl-entrypoint] starting AXL node (peers=$PEERS)..."
exec ./node -config "$KEYSTORE/node-config.json"
