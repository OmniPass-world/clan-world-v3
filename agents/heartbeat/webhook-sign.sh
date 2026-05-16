#!/usr/bin/env bash
# webhook-sign.sh — HMAC-SHA256 helper for the heartbeat → Convex webhook.
#
# Phase 1.10 (issue #353). Computes the canonical signature per the plan:
#
#   v1 = HMAC-SHA256(secret, "${t}.${body}")
#
# Header layout (Stripe-style — matches the Convex receiver contract):
#
#   X-Heartbeat-Signature: t=<unix-ts>,v1=<hex-hmac>
#
# Convex validates: (a) |now - t| ≤ 60s (replay window), (b) v1 matches.
#
# Usage as a sourced helper:
#
#   source /opt/clan-world/heartbeat/webhook-sign.sh
#   sig="$(heartbeat_sign "$WEBHOOK_SHARED_SECRET" "$timestamp" "$body")"
#
# Stdout: the hex HMAC only (caller composes the header).

set -euo pipefail

# heartbeat_sign <secret> <timestamp> <body>
#
# Returns the hex-encoded HMAC-SHA256 of "${timestamp}.${body}" using <secret>
# as the key. openssl is used (already in the image for ca-certificates +
# entropy); avoids a heavier python/node dep.
heartbeat_sign() {
  local secret="$1"
  local timestamp="$2"
  local body="$3"

  if [ -z "$secret" ] || [ -z "$timestamp" ] || [ -z "$body" ]; then
    printf 'heartbeat_sign: empty secret/timestamp/body\n' >&2
    return 1
  fi

  # printf "%s" avoids the trailing newline that `echo` injects — critical for
  # HMAC determinism. The Convex receiver MUST sign over the same byte string.
  printf '%s' "${timestamp}.${body}" \
    | openssl dgst -sha256 -hmac "$secret" -hex \
    | awk '{print $NF}'
}

# Allow invocation as a CLI for ad-hoc debugging:
#   webhook-sign.sh <secret> <timestamp> <body>
if [ "${BASH_SOURCE[0]}" = "$0" ]; then
  if [ "$#" -ne 3 ]; then
    printf 'usage: %s <secret> <timestamp> <body>\n' "$0" >&2
    exit 2
  fi
  heartbeat_sign "$1" "$2" "$3"
fi
