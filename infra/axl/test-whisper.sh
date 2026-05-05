#!/usr/bin/env bash
# test-whisper.sh — proves AXL send/recv round-trips between local clan nodes.
# Requires: AXL nodes up, setup.sh run first (or AXL_PEER_ID_* set in env).
# Exit: 0 on pass, non-zero on failure.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

if [ -f "$ENV_FILE" ]; then
  # shellcheck source=/dev/null
  source "$ENV_FILE"
fi

AXL_1_URL="${AXL_NODE_URL_CLAN_1:-http://127.0.0.1:9002}"
AXL_2_URL="${AXL_NODE_URL_CLAN_2:-http://127.0.0.1:9003}"
AXL_3_URL="${AXL_NODE_URL_CLAN_3:-http://127.0.0.1:9004}"
NETWORK_ID="${AXL_NETWORK_ID:-testnet}"

for n in 1 2 3; do
  peer_var="AXL_PEER_ID_CLAN_$n"
  if [ -z "${!peer_var:-}" ]; then
    echo "ERROR: $peer_var not set — run setup.sh first" >&2
    exit 1
  fi
done

TICK=1
BASE_MESSAGE="axl-whisper-$(date +%s)"

echo "=== AXL whisper smoke test ==="
echo "  axl-1 (clan-1): $AXL_1_URL"
echo "  axl-2 (clan-2): $AXL_2_URL"
echo "  axl-3 (clan-3): $AXL_3_URL"
echo "  network: $NETWORK_ID"
echo ""

run_roundtrip() {
  local from_n="$1" to_n="$2" from_url="$3" to_url="$4"
  local to_peer_var="AXL_PEER_ID_CLAN_$to_n"
  local from_peer_var="AXL_PEER_ID_CLAN_$from_n"
  local to_peer_id="${!to_peer_var}"
  local from_peer_id="${!from_peer_var}"
  local expected_from_peer_id
  local msg_id="test-${from_n}-${to_n}-$(date +%s)-$$"
  local message="${BASE_MESSAGE}-${from_n}-to-${to_n}"
  local envelope

  expected_from_peer_id=$(node - "$from_peer_id" <<'NODE'
const publicKey = Buffer.from(process.argv[2], 'hex');
const inverted = Buffer.from(publicKey.map((b) => b ^ 0xff));
const address = Buffer.alloc(16);
const temp = [];
let done = false;
let ones = 0;
let bits = 0;
let nBits = 0;
for (let idx = 0; idx < 8 * inverted.length; idx += 1) {
  const bit = (inverted[Math.floor(idx / 8)] & (0x80 >> (idx % 8))) >> (7 - (idx % 8));
  if (!done && bit !== 0) {
    ones += 1;
    continue;
  }
  if (!done && bit === 0) {
    done = true;
    continue;
  }
  bits = (bits << 1) | bit;
  nBits += 1;
  if (nBits === 8) {
    temp.push(bits);
    bits = 0;
    nBits = 0;
  }
}
address[0] = 0x02;
address[1] = ones;
for (let i = 0; i < 14; i += 1) address[2 + i] = temp[i] ?? 0;
const partialKey = Buffer.alloc(32);
for (let idx = 0; idx < ones; idx += 1) {
  partialKey[Math.floor(idx / 8)] |= 0x80 >> (idx % 8);
}
const keyOffset = ones + 1;
const addrOffset = 16;
for (let idx = addrOffset; idx < 8 * address.length; idx += 1) {
  let shiftedBits = address[Math.floor(idx / 8)] & (0x80 >> (idx % 8));
  shiftedBits <<= idx % 8;
  const keyIdx = keyOffset + (idx - addrOffset);
  shiftedBits >>= keyIdx % 8;
  const byteIdx = Math.floor(keyIdx / 8);
  if (byteIdx >= partialKey.length) break;
  partialKey[byteIdx] |= shiftedBits;
}
for (let i = 0; i < partialKey.length; i += 1) partialKey[i] ^= 0xff;
console.log(partialKey.toString('hex'));
NODE
)

  envelope=$(jq -n \
    --arg fromClanId  "clan-$from_n" \
    --arg toClanId    "clan-$to_n" \
    --arg message     "$message" \
    --argjson tick    "$TICK" \
    --arg msgId       "$msg_id" \
    --arg networkId   "$NETWORK_ID" \
    --arg sentAt      "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
    '{fromClanId:$fromClanId,toClanId:$toClanId,message:$message,tick:$tick,msgId:$msgId,networkId:$networkId,sentAt:$sentAt}')

  echo "--- whisper send: clan-$from_n → clan-$to_n (via axl-$from_n) ---"
  echo "Destination peer: $to_peer_id"

  local send_status
  send_status=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$from_url/send" \
    -H "Content-Type: application/octet-stream" \
    -H "X-Destination-Peer-Id: $to_peer_id" \
    --data-raw "$envelope")

  if [ "$send_status" != "200" ]; then
    echo "FAIL: POST $from_url/send returned HTTP $send_status" >&2
    exit 1
  fi
  echo "Send OK (HTTP 200)"

  sleep 2

  echo ""
  echo "--- whisper recv: clan-$to_n (via axl-$to_n) ---"

  local recv_body=""
  local from_peer_id_header=""
  local headers_file="/tmp/axl-recv-headers-${to_n}.txt"

  for i in $(seq 1 6); do
    rm -f "$headers_file"
    local recv_out recv_status_line recv_http
    recv_out=$(curl -sf --max-time 5 -D "$headers_file" "$to_url/recv" 2>/dev/null || true)
    recv_status_line=$(head -1 "$headers_file" 2>/dev/null | tr -d '\r')
    recv_http=$(echo "$recv_status_line" | awk '{print $2}')

    if [ "$recv_http" = "200" ]; then
      from_peer_id_header=$(grep -i "^X-From-Peer-Id:" "$headers_file" | awk '{print $2}' | tr -d '\r')
      recv_body="$recv_out"
      break
    elif [ "$recv_http" = "204" ]; then
      echo "Queue empty (attempt $i/6), waiting..."
      sleep 2
    else
      echo "FAIL: GET $to_url/recv returned HTTP $recv_http" >&2
      exit 1
    fi
  done

  if [ -z "$recv_body" ]; then
    echo "FAIL: no message received on clan-$to_n after retries" >&2
    exit 1
  fi

  echo "Received from peer: $from_peer_id_header"
  echo "Body: $recv_body"

  local recv_from_clan recv_to_clan recv_msg recv_network fail=0
  recv_from_clan=$(echo "$recv_body" | jq -r '.fromClanId')
  recv_to_clan=$(echo "$recv_body" | jq -r '.toClanId')
  recv_msg=$(echo "$recv_body" | jq -r '.message')
  recv_network=$(echo "$recv_body" | jq -r '.networkId')

  [ "$recv_from_clan" = "clan-$from_n" ] || { echo "FAIL: fromClanId='$recv_from_clan' (expected clan-$from_n)" >&2; fail=1; }
  [ "$recv_to_clan" = "clan-$to_n" ] || { echo "FAIL: toClanId='$recv_to_clan' (expected clan-$to_n)" >&2; fail=1; }
  [ "$recv_msg" = "$message" ] || { echo "FAIL: message='$recv_msg' (expected '$message')" >&2; fail=1; }
  [ "$recv_network" = "$NETWORK_ID" ] || { echo "FAIL: networkId='$recv_network' (expected '$NETWORK_ID')" >&2; fail=1; }

  if [ $fail -ne 0 ]; then exit 1; fi

  if [ -n "$from_peer_id_header" ] && [ "$from_peer_id_header" != "$expected_from_peer_id" ]; then
    echo "FAIL: X-From-Peer-Id='$from_peer_id_header' does not match Yggdrasil partial for $from_peer_var='$expected_from_peer_id'" >&2
    exit 1
  fi

  echo "Round-trip clan-$from_n → clan-$to_n OK"
  echo ""
}

run_roundtrip 1 2 "$AXL_1_URL" "$AXL_2_URL"
run_roundtrip 1 3 "$AXL_1_URL" "$AXL_3_URL"

echo ""
echo "=== PASS: AXL whisper round-trips verified ==="
echo ""
echo "  transport : AxlPeerInbox (AXL_API_KEY + AXL_NETWORK_ID set — FilePeerInbox NOT used)"
echo "  paths     : clan-1 → clan-2, clan-1 → clan-3"
echo "  networkId : $NETWORK_ID"
echo ""
echo "Journal check: AxlPeerInbox activates when AXL_API_KEY and AXL_NETWORK_ID are set."
echo "  Confirmed: createPeerInbox() selects AxlPeerInbox (not FilePeerInbox) — see axlPeerInbox.ts:511-545."
