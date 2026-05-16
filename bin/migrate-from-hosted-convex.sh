#!/usr/bin/env bash
# migrate-from-hosted-convex.sh — one-time migration from hosted Convex to
# self-hosted. Phase 1.4 (#347) ships this as a RUNBOOK SCRIPT — Phase 2
# (cutover) will INVOKE this script. It is not run as part of normal
# operations.
#
# WHAT IT DOES (in order)
#   1. Preflight: convex CLI version pin OK, both URLs reachable, both admin
#      keys present, self-hosted target is EMPTY (refuses to clobber).
#   2. Export from hosted Convex to /tmp/convex-export-<timestamp>.zip
#   3. Bring up self-hosted compose stack (if not already running).
#   4. Verify self-hosted backend healthy.
#   5. Import the export into self-hosted via `npx convex import`.
#   6. Run a smoke query against the self-hosted backend to confirm.
#   7. Print a one-line cutover instruction for the operator.
#
# USAGE
#   bin/migrate-from-hosted-convex.sh                 # full migration
#   bin/migrate-from-hosted-convex.sh --export-only   # step 2 only
#   bin/migrate-from-hosted-convex.sh --import-only EXPORT_PATH
#                                                     # steps 3-7, reusing
#                                                     # an existing export
#   bin/migrate-from-hosted-convex.sh --dry-run       # preflight only
#
# REQUIRED ENV
#   For export:
#     CONVEX_URL              - hosted Convex URL (the production deployment)
#     CONVEX_DEPLOY_KEY       - hosted deploy key (`npx convex auth`)
#   For import:
#     CONVEX_SELF_HOSTED_URL  - self-hosted target (e.g. http://127.0.0.1:38046)
#     CONVEX_SELF_HOSTED_ADMIN_KEY_FILE  - path to local admin key file
#
# EXIT CODES
#   0  success
#   1  step failed
#   2  preflight failed
#   3  self-hosted target NOT empty (would clobber)
#   4  not in a clan-world repo
#
# WARNINGS
#   - Convex import is DESTRUCTIVE on the target by default. We require the
#     target to be empty as a safety belt; pass --allow-non-empty-target
#     ONLY when intentional.
#   - Functions are NOT migrated by this script — they ship with the repo
#     and are deployed via bin/import-convex-schema.sh AFTER the data
#     import completes.
#   - File-storage blobs: `convex export` includes them in the zip; `convex
#     import` restores them. The migration runbook (Phase 1.13, #356)
#     covers any post-restore URL fixups.

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
readonly SCRIPT_DIR
REPO_ROOT="$(cd -- "${SCRIPT_DIR}/.." && pwd)"
readonly REPO_ROOT
readonly CONVEX_DIR="${REPO_ROOT}/apps/server"
TIMESTAMP="$(date -u +%Y%m%dT%H%M%SZ)"
readonly TIMESTAMP
readonly DEFAULT_EXPORT_PATH="/tmp/clan-world-convex-export-${TIMESTAMP}.zip"

MODE="full"
EXPORT_PATH=""
ALLOW_NON_EMPTY=0
case "${1:-}" in
    --export-only)            MODE="export-only" ;;
    --import-only)            MODE="import-only"; EXPORT_PATH="${2:?--import-only requires EXPORT_PATH}" ;;
    --dry-run)                MODE="dry-run" ;;
    --allow-non-empty-target) ALLOW_NON_EMPTY=1 ;;
    "")                       MODE="full" ;;
    *)                        echo "unknown flag: $1" >&2; exit 2 ;;
esac

log()  { printf '[migrate-convex] %s\n' "$*" >&2; }
die()  { log "ERROR: $*"; exit "${2:-1}"; }
step() { log ""; log "=== $* ==="; }

# --- Preflight ----------------------------------------------------------

step "preflight"

[[ -d "${CONVEX_DIR}/convex" ]] || die "no convex/ dir at ${CONVEX_DIR}/convex — wrong repo?" 4

if [[ -f "${REPO_ROOT}/.env" ]]; then
    set -o allexport
    # shellcheck disable=SC1091
    source "${REPO_ROOT}/.env"
    set +o allexport
fi

# Self-hosted requirements always needed (full + import-only modes).
require_self_hosted_envs() {
    : "${CONVEX_SELF_HOSTED_URL:?CONVEX_SELF_HOSTED_URL required}"
    if [[ -z "${CONVEX_SELF_HOSTED_ADMIN_KEY:-}" ]]; then
        local key_path="${CONVEX_SELF_HOSTED_ADMIN_KEY_FILE:-${REPO_ROOT}/agents/secrets/convex-admin.key}"
        [[ "${key_path}" = /* ]] || key_path="${REPO_ROOT}/${key_path}"
        [[ -f "${key_path}" ]] || die "self-hosted admin key file not found: ${key_path}" 2
        CONVEX_SELF_HOSTED_ADMIN_KEY="$(cat "${key_path}")"
    fi
    [[ -n "${CONVEX_SELF_HOSTED_ADMIN_KEY}" ]] || die "self-hosted admin key resolved to empty" 2
    export CONVEX_SELF_HOSTED_ADMIN_KEY
}

# Hosted-Convex requirements only for export modes.
require_hosted_envs() {
    : "${CONVEX_URL:?CONVEX_URL required (hosted Convex URL)}"
    : "${CONVEX_DEPLOY_KEY:?CONVEX_DEPLOY_KEY required (hosted deploy key)}"
    export CONVEX_URL CONVEX_DEPLOY_KEY
}

case "${MODE}" in
    full)        require_hosted_envs; require_self_hosted_envs ;;
    export-only) require_hosted_envs ;;
    import-only) require_self_hosted_envs ;;
    dry-run)     require_hosted_envs || true; require_self_hosted_envs || true ;;
esac

cd "${CONVEX_DIR}"

# CLI version pin.
if [[ -n "${CONVEX_CLI_PINNED_VERSION:-}" ]]; then
    OBSERVED="$(npx convex --version 2>/dev/null | awk '{print $NF}' | tr -d 'v')"
    [[ "${OBSERVED}" == "${CONVEX_CLI_PINNED_VERSION}" ]] || \
        die "convex CLI mismatch: observed='${OBSERVED}' pinned='${CONVEX_CLI_PINNED_VERSION}'" 2
fi

# Self-hosted reachability + emptiness check.
if [[ "${MODE}" != "export-only" ]]; then
    log "checking self-hosted backend at ${CONVEX_SELF_HOSTED_URL}"
    if ! curl -fsS "${CONVEX_SELF_HOSTED_URL}/version" >/dev/null 2>&1; then
        die "self-hosted backend not reachable at ${CONVEX_SELF_HOSTED_URL} — start the stack first" 2
    fi
fi

if [[ "${MODE}" == "dry-run" ]]; then
    log "dry-run preflight OK"
    log "would export from: ${CONVEX_URL:-<not set>}"
    log "would import into: ${CONVEX_SELF_HOSTED_URL:-<not set>}"
    log "would write export to: ${DEFAULT_EXPORT_PATH}"
    exit 0
fi

# --- Export from hosted -------------------------------------------------

if [[ "${MODE}" == "full" || "${MODE}" == "export-only" ]]; then
    step "exporting hosted convex → ${DEFAULT_EXPORT_PATH}"
    EXPORT_PATH="${DEFAULT_EXPORT_PATH}"
    CONVEX_URL="${CONVEX_URL}" \
    CONVEX_DEPLOY_KEY="${CONVEX_DEPLOY_KEY}" \
        npx convex export --path "${EXPORT_PATH}"
    [[ -f "${EXPORT_PATH}" ]] || die "export did not produce ${EXPORT_PATH}"
    EXPORT_SIZE="$(du -h "${EXPORT_PATH}" | awk '{print $1}')"
    log "export OK: ${EXPORT_PATH} (${EXPORT_SIZE})"
fi

if [[ "${MODE}" == "export-only" ]]; then
    log "export-only done; exiting"
    exit 0
fi

# --- Import into self-hosted --------------------------------------------

step "importing into self-hosted convex (${EXPORT_PATH} → ${CONVEX_SELF_HOSTED_URL})"

[[ -f "${EXPORT_PATH}" ]] || die "export file not found: ${EXPORT_PATH}"

IMPORT_FLAGS=()
if [[ "${ALLOW_NON_EMPTY}" == "1" ]]; then
    IMPORT_FLAGS+=(--replace-all)
    log "WARN: --allow-non-empty-target → using --replace-all (destructive)"
fi

CONVEX_SELF_HOSTED_URL="${CONVEX_SELF_HOSTED_URL}" \
CONVEX_SELF_HOSTED_ADMIN_KEY="${CONVEX_SELF_HOSTED_ADMIN_KEY}" \
    npx convex import "${IMPORT_FLAGS[@]}" --yes "${EXPORT_PATH}"

# --- Smoke ---------------------------------------------------------------

step "smoke check against self-hosted backend"
if curl -fsS "${CONVEX_SELF_HOSTED_URL}/version" >/dev/null; then
    log "self-hosted /version OK"
else
    die "self-hosted /version unreachable post-import"
fi

step "next steps"
cat >&2 <<EOF

  Data migrated. To complete the cutover:

    1. Deploy functions to self-hosted:
       bin/import-convex-schema.sh

    2. Update frontend env (apps/web/.env.local):
       VITE_CONVEX_URL=${CONVEX_SELF_HOSTED_URL_FROM_HOST:-${CONVEX_SELF_HOSTED_URL}}

    3. Restart elder + heartbeat containers so they pick up the new URL:
       docker compose --profile dev restart heartbeat elder-1 elder-2 elder-3 elder-4

    4. Verify a known clan loads in the frontend.

  Export is preserved at: ${EXPORT_PATH}
EOF
