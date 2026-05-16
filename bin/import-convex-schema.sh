#!/usr/bin/env bash
# import-convex-schema.sh — deploy Convex functions to the self-hosted backend.
#
# Phase 1.4 (#347). Wraps `npx convex deploy` against a self-hosted target.
# Idempotent: a successful deploy of an unchanged tree is a no-op (the CLI
# detects no diff). We additionally skip the deploy step if the functions
# directory's tracked-content hash matches a checkpoint recorded after the
# last successful deploy.
#
# USAGE
#   bin/import-convex-schema.sh                  # deploy if needed
#   bin/import-convex-schema.sh --force          # deploy unconditionally
#   bin/import-convex-schema.sh --dry-run        # report-only, do nothing
#   bin/import-convex-schema.sh --check          # exit 0 if synced, 1 if dirty
#
# REQUIRED ENV (read from .env or shell)
#   CONVEX_SELF_HOSTED_URL          - http://convex-backend:3210 from inside
#                                     compose, http://127.0.0.1:PORT from host
#   CONVEX_SELF_HOSTED_ADMIN_KEY    - the admin key string, OR
#   CONVEX_SELF_HOSTED_ADMIN_KEY_FILE - path to file containing the admin key
#                                     (preferred — file mode is checked, env
#                                     vars leak through `docker inspect` and
#                                     `ps`)
#   CONVEX_CLI_PINNED_VERSION       - aborts if `npx convex --version` mismatches
#
# EXIT CODES
#   0  success / already in sync / dry-run
#   1  deploy failed
#   2  preflight failed (missing env, wrong CLI version)
#   3  --check found drift
#   4  not in a clan-world repo
#
# AUTHORS
#   Phase 1.4 (#347). Companion: bin/migrate-from-hosted-convex.sh.

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
readonly SCRIPT_DIR
REPO_ROOT="$(cd -- "${SCRIPT_DIR}/.." && pwd)"
readonly REPO_ROOT
readonly CONVEX_DIR="${REPO_ROOT}/apps/server"
readonly STATE_DIR="${REPO_ROOT}/agents/runtime/convex"
readonly LAST_DEPLOY_HASH="${STATE_DIR}/last-deploy.sha256"

MODE="deploy"
case "${1:-}" in
    --force)   MODE="force"   ;;
    --dry-run) MODE="dry-run" ;;
    --check)   MODE="check"   ;;
    "")        MODE="deploy"  ;;
    *)         echo "unknown flag: $1" >&2; exit 2 ;;
esac

log() { printf '[import-convex-schema] %s\n' "$*" >&2; }
die() { log "ERROR: $*"; exit "${2:-1}"; }

# --- Preflight ----------------------------------------------------------

[[ -d "${CONVEX_DIR}/convex" ]] || die "no convex/ dir at ${CONVEX_DIR}/convex — wrong repo?" 4
[[ -f "${CONVEX_DIR}/package.json" ]] || die "no package.json at ${CONVEX_DIR}/package.json" 4

# Load .env if present — non-fatal, env vars from caller win.
if [[ -f "${REPO_ROOT}/.env" ]]; then
    set -o allexport
    # shellcheck disable=SC1091
    source "${REPO_ROOT}/.env"
    set +o allexport
fi

: "${CONVEX_SELF_HOSTED_URL:?CONVEX_SELF_HOSTED_URL required (e.g. http://127.0.0.1:38046)}"

# Resolve admin key: file > env var. Never echo the key to stdout/stderr.
ADMIN_KEY=""
if [[ -n "${CONVEX_SELF_HOSTED_ADMIN_KEY_FILE:-}" ]]; then
    KEY_PATH="${CONVEX_SELF_HOSTED_ADMIN_KEY_FILE}"
    # Resolve relative paths against REPO_ROOT.
    [[ "${KEY_PATH}" = /* ]] || KEY_PATH="${REPO_ROOT}/${KEY_PATH}"
    [[ -f "${KEY_PATH}" ]] || die "admin key file not found: ${KEY_PATH} (run \`make bootstrap-convex-admin-key\`)" 2
    # Permissions sanity: file should be 600 or 400.
    PERMS="$(stat -c '%a' "${KEY_PATH}" 2>/dev/null || stat -f '%Lp' "${KEY_PATH}")"
    if [[ "${PERMS}" != "600" && "${PERMS}" != "400" ]]; then
        log "WARN: admin key file ${KEY_PATH} has perms ${PERMS}; expected 600 or 400"
    fi
    ADMIN_KEY="$(cat "${KEY_PATH}")"
elif [[ -n "${CONVEX_SELF_HOSTED_ADMIN_KEY:-}" ]]; then
    ADMIN_KEY="${CONVEX_SELF_HOSTED_ADMIN_KEY}"
else
    die "neither CONVEX_SELF_HOSTED_ADMIN_KEY_FILE nor CONVEX_SELF_HOSTED_ADMIN_KEY is set" 2
fi
[[ -n "${ADMIN_KEY}" ]] || die "admin key resolved to empty string" 2

# Verify CLI version matches the pin (Finding 11 fix).
if [[ -n "${CONVEX_CLI_PINNED_VERSION:-}" ]]; then
    cd "${CONVEX_DIR}"
    OBSERVED="$(npx convex --version 2>/dev/null | awk '{print $NF}' | tr -d 'v')"
    if [[ "${OBSERVED}" != "${CONVEX_CLI_PINNED_VERSION}" ]]; then
        die "convex CLI version mismatch: observed='${OBSERVED}' pinned='${CONVEX_CLI_PINNED_VERSION}'; run \`pnpm install --filter @clan-world/server\`" 2
    fi
fi

# --- Idempotency check --------------------------------------------------

mkdir -p "${STATE_DIR}"

# Hash the convex/ functions dir contents (sorted, deterministic) — covers
# all .ts files in the convex/ tree EXCEPT generated artifacts. The CLI's
# own diff detection is the canonical "is anything new?" check, but skipping
# the whole `npx convex deploy` invocation when nothing changed is faster
# and noiseless in CI.
current_hash() {
    find "${CONVEX_DIR}/convex" -type f \
        \( -name '*.ts' -o -name '*.json' \) \
        -not -path '*/_generated/*' \
        -not -name '*.test.ts' \
        -print0 \
    | sort -z \
    | xargs -0 sha256sum \
    | sha256sum \
    | awk '{print $1}'
}

CURRENT_HASH="$(current_hash)"
STORED_HASH=""
[[ -f "${LAST_DEPLOY_HASH}" ]] && STORED_HASH="$(cat "${LAST_DEPLOY_HASH}")"

if [[ "${MODE}" == "check" ]]; then
    if [[ "${CURRENT_HASH}" == "${STORED_HASH}" && -n "${STORED_HASH}" ]]; then
        log "synced (sha256=${CURRENT_HASH:0:12})"
        exit 0
    else
        log "DRIFT — local=${CURRENT_HASH:0:12} deployed=${STORED_HASH:0:12}"
        exit 3
    fi
fi

if [[ "${MODE}" != "force" && "${CURRENT_HASH}" == "${STORED_HASH}" && -n "${STORED_HASH}" ]]; then
    log "no changes since last deploy (sha256=${CURRENT_HASH:0:12}); skipping (use --force to redeploy)"
    exit 0
fi

if [[ "${MODE}" == "dry-run" ]]; then
    log "DRY-RUN: would deploy ${CONVEX_DIR}/convex to ${CONVEX_SELF_HOSTED_URL}"
    log "local hash:    ${CURRENT_HASH}"
    log "deployed hash: ${STORED_HASH:-<none>}"
    exit 0
fi

# --- Deploy -------------------------------------------------------------

log "deploying convex/ to ${CONVEX_SELF_HOSTED_URL} (admin key from ${CONVEX_SELF_HOSTED_ADMIN_KEY_FILE:-env})"

# `npx convex deploy` honors CONVEX_SELF_HOSTED_URL + CONVEX_SELF_HOSTED_ADMIN_KEY.
# The CLI infers self-hosted mode from the presence of these envs.
cd "${CONVEX_DIR}"
CONVEX_SELF_HOSTED_URL="${CONVEX_SELF_HOSTED_URL}" \
CONVEX_SELF_HOSTED_ADMIN_KEY="${ADMIN_KEY}" \
    npx convex deploy --yes

# Record successful deploy hash.
echo "${CURRENT_HASH}" > "${LAST_DEPLOY_HASH}"
log "deploy OK; recorded hash ${CURRENT_HASH:0:12}"
