#!/usr/bin/env bash
# bin/check-stack-health.sh — programmatic smoke check for the dockerized
# ClanWorld stack. Invoked by `make smoke-test` (Phase 1.12, Finding 39 fix).
#
# Returns 0 only if ALL of the following pass:
#   1. All 4 elder tmux sessions alive
#   2. All 4 elder supervisor processes running
#   3. convex-backend container healthy + /version reachable on the internal net
#   4. heartbeat container last-success file age < 120s
#   5. host caddy snippet installed in /etc/caddy/Caddyfile
#
# Non-zero exit with per-check pass/fail summary on any failure. Designed to
# be safe to run repeatedly during a coexist window — only reads state.
#
# Usage:
#   bin/check-stack-health.sh
#   bin/check-stack-health.sh --quiet     # only print failures
#
# Exit codes:
#   0   all checks pass
#   1   one or more checks failed
#   2   pre-flight failed (docker compose unavailable, repo root unfindable, ...)

set -uo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd -- "${SCRIPT_DIR}/.." && pwd)"
readonly SCRIPT_DIR REPO_ROOT

ELDERS=(elder-1 elder-2 elder-3 elder-4)
CADDYFILE="${CADDYFILE:-/etc/caddy/Caddyfile}"
SNIPPET_PATH="${REPO_ROOT}/agents/shared/caddy.conf"
IMPORT_LINE="import ${SNIPPET_PATH}"
HEARTBEAT_MAX_AGE_SEC="${HEARTBEAT_MAX_AGE_SEC:-120}"

QUIET=0
if [[ "${1:-}" == "--quiet" ]]; then QUIET=1; fi

# --- formatting --------------------------------------------------------------

if [[ -t 1 ]]; then
    C_GREEN=$'\033[32m'
    C_RED=$'\033[31m'
    C_YELLOW=$'\033[33m'
    C_RESET=$'\033[0m'
else
    C_GREEN=""; C_RED=""; C_YELLOW=""; C_RESET=""
fi

PASSED=0
FAILED=0
FAILURES=()

pass() {
    PASSED=$((PASSED + 1))
    [[ "$QUIET" -eq 1 ]] || printf '  %sPASS%s  %s\n' "$C_GREEN" "$C_RESET" "$*"
}

fail() {
    FAILED=$((FAILED + 1))
    FAILURES+=("$*")
    printf '  %sFAIL%s  %s\n' "$C_RED" "$C_RESET" "$*"
}

warn() {
    printf '  %sWARN%s  %s\n' "$C_YELLOW" "$C_RESET" "$*"
}

section() {
    printf '\n%s\n' "[$*]"
}

# --- pre-flight --------------------------------------------------------------

if ! command -v docker >/dev/null 2>&1; then
    echo "ERROR: docker not in PATH" >&2
    exit 2
fi

if ! docker compose version >/dev/null 2>&1; then
    echo "ERROR: 'docker compose' subcommand unavailable" >&2
    exit 2
fi

# All compose commands run from the repo root so relative paths in
# docker-compose.yml resolve correctly.
cd "$REPO_ROOT" || { echo "ERROR: cannot cd to $REPO_ROOT" >&2; exit 2; }

if [[ ! -f docker-compose.yml ]]; then
    echo "ERROR: docker-compose.yml not found at $REPO_ROOT" >&2
    exit 2
fi

# --- 1. elder tmux sessions --------------------------------------------------

section "elder tmux sessions"
for e in "${ELDERS[@]}"; do
    if docker compose exec -T "$e" tmux has-session -t "$e" 2>/dev/null; then
        pass "$e tmux session alive"
    else
        fail "$e tmux session DEAD or container down"
    fi
done

# --- 2. elder supervisor processes ------------------------------------------

section "elder supervisor processes"
for e in "${ELDERS[@]}"; do
    if docker compose exec -T "$e" pgrep -f /opt/elder-runtime/main.js >/dev/null 2>&1; then
        pass "$e supervisor running"
    else
        fail "$e supervisor NOT running"
    fi
done

# --- 3. convex-backend reachable --------------------------------------------

section "convex-backend"
convex_state=$(docker compose ps --format '{{.Name}} {{.State}}' 2>/dev/null \
    | awk '$1 ~ /convex-backend/ { print $2 }' | head -1)
if [[ "$convex_state" == "running" ]]; then
    pass "convex-backend container running"
else
    fail "convex-backend container state=${convex_state:-<missing>}"
fi

# /version is an internal port; reach it through the container's own loopback.
if docker compose exec -T convex-backend wget -qO- --timeout=5 http://localhost:3210/version >/dev/null 2>&1; then
    pass "convex-backend /version reachable internally"
elif docker compose exec -T convex-backend curl -sf --max-time 5 http://localhost:3210/version >/dev/null 2>&1; then
    pass "convex-backend /version reachable internally"
else
    fail "convex-backend /version unreachable on internal port 3210"
fi

# --- 4. heartbeat freshness --------------------------------------------------

section "heartbeat"
heartbeat_state=$(docker compose ps --format '{{.Name}} {{.State}}' 2>/dev/null \
    | awk '$1 ~ /heartbeat/ { print $2 }' | head -1)
if [[ "$heartbeat_state" == "running" ]]; then
    pass "heartbeat container running"
    # Read /tmp/last-heartbeat-success inside the container; compare to NOW.
    age=$(docker compose exec -T heartbeat sh -c '
        if [ -f /tmp/last-heartbeat-success ]; then
            echo $(( $(date +%s) - $(cat /tmp/last-heartbeat-success) ))
        else
            echo MISSING
        fi
    ' 2>/dev/null | tr -d '\r')
    case "$age" in
        MISSING)
            fail "heartbeat /tmp/last-heartbeat-success file missing — no tick yet"
            ;;
        ''|*[!0-9]*)
            fail "heartbeat age unreadable (got: $age)"
            ;;
        *)
            if [[ "$age" -lt "$HEARTBEAT_MAX_AGE_SEC" ]]; then
                pass "heartbeat last tick ${age}s ago (< ${HEARTBEAT_MAX_AGE_SEC}s)"
            else
                fail "heartbeat STALE: last tick ${age}s ago (>= ${HEARTBEAT_MAX_AGE_SEC}s)"
            fi
            ;;
    esac
else
    fail "heartbeat container state=${heartbeat_state:-<missing>}"
fi

# --- 5. host caddy snippet installed -----------------------------------------

section "host caddy snippet"
if [[ ! -f "$CADDYFILE" ]]; then
    warn "$CADDYFILE not found — skipping caddy check (may be a dev box)"
elif grep -Fxq "$IMPORT_LINE" "$CADDYFILE" 2>/dev/null; then
    pass "import line present in $CADDYFILE"
else
    fail "import line MISSING from $CADDYFILE — run \`make install-caddy-snippet\`"
fi

# --- summary -----------------------------------------------------------------

printf '\n----------------------------------\n'
printf 'PASSED: %d   FAILED: %d\n' "$PASSED" "$FAILED"

if [[ "$FAILED" -gt 0 ]]; then
    printf '\nFailures:\n'
    for f in "${FAILURES[@]}"; do
        printf '  - %s\n' "$f"
    done
    exit 1
fi

printf '\n%sAll checks passed.%s\n' "$C_GREEN" "$C_RESET"
exit 0
