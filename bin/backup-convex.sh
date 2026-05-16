#!/usr/bin/env bash
# backup-convex.sh — snapshot the self-hosted Convex SQLite + file storage.
#
# Phase 1.4 (#347). Pauses the backend (SQLite is safe to snapshot while
# paused — `docker compose pause` is the freeze primitive recommended by the
# Convex self-host research synthesis), tars the volume, compresses, and
# unpauses.
#
# USAGE
#   bin/backup-convex.sh                     # default destination
#   bin/backup-convex.sh -o /path/to/backup  # override destination dir
#   bin/backup-convex.sh --no-pause          # snapshot live (UNSAFE — only for
#                                            #   spot checks; risks DB corruption)
#   bin/backup-convex.sh --keep N            # rotate, keep last N backups
#                                            #   (default: 14)
#
# OUTPUT
#   <dest>/convex-<timestamp>.tar.zst
#   <dest>/convex-<timestamp>.sha256
#
#   Where <timestamp> = YYYYMMDDTHHMMSSZ (UTC).
#
# DEFAULT DESTINATION
#   /var/lib/clan-world/backups/convex/      (created if missing; chmod 700)
#
# EXIT CODES
#   0  success
#   1  backup failed (compose unpaused before exit regardless)
#   2  precondition failed (no compose stack, no volume, etc.)
#
# DEPENDENCIES
#   - docker, docker compose
#   - zstd (apt-get install zstd; smaller + faster than gzip for SQLite)
#   - sha256sum (coreutils)
#
# CRON
#   This script is invocation-only — automation (cron / systemd timer) is
#   deferred to a follow-up. Manual recipe to put behind a daily timer in
#   America/New_York: see docs/runbooks/convex-backup.md (TBD).

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
readonly SCRIPT_DIR
REPO_ROOT="$(cd -- "${SCRIPT_DIR}/.." && pwd)"
readonly REPO_ROOT

DEST_DIR="/var/lib/clan-world/backups/convex"
KEEP_COUNT=14
DO_PAUSE=1

while [[ $# -gt 0 ]]; do
    case "$1" in
        -o|--output) DEST_DIR="${2:?-o requires path}"; shift 2 ;;
        --no-pause)  DO_PAUSE=0; shift ;;
        --keep)      KEEP_COUNT="${2:?--keep requires N}"; shift 2 ;;
        -h|--help)
            sed -n '2,30p' "$0"
            exit 0
            ;;
        *) echo "unknown flag: $1" >&2; exit 2 ;;
    esac
done

TIMESTAMP="$(date -u +%Y%m%dT%H%M%SZ)"
readonly TIMESTAMP
readonly TAR_PATH="${DEST_DIR}/convex-${TIMESTAMP}.tar.zst"
readonly SHA_PATH="${DEST_DIR}/convex-${TIMESTAMP}.sha256"
readonly VOLUME_NAME="clan-world_convex_data"
readonly BACKEND_SVC="convex-backend"

log()  { printf '[backup-convex] %s\n' "$*" >&2; }
die()  { log "ERROR: $*"; exit "${2:-1}"; }

# --- Preflight ----------------------------------------------------------

cd "${REPO_ROOT}"

command -v docker  >/dev/null   || die "docker not in PATH" 2
command -v zstd    >/dev/null   || die "zstd not in PATH; apt install zstd" 2

# Verify the volume exists (compose stack must be up at least once).
docker volume inspect "${VOLUME_NAME}" >/dev/null 2>&1 \
    || die "docker volume '${VOLUME_NAME}' not found — has the stack been brought up?" 2

# Prepare destination.
if [[ ! -d "${DEST_DIR}" ]]; then
    log "creating ${DEST_DIR}"
    if ! mkdir -p "${DEST_DIR}" 2>/dev/null; then
        die "cannot create ${DEST_DIR}; if /var/lib is root-owned, re-run with sudo or use -o ~/backups/convex" 2
    fi
    chmod 700 "${DEST_DIR}"
fi

# --- Pause backend ------------------------------------------------------

cleanup_unpause() {
    if [[ "${DO_PAUSE}" == "1" ]]; then
        log "unpausing ${BACKEND_SVC} (cleanup)"
        docker compose unpause "${BACKEND_SVC}" >/dev/null 2>&1 || true
    fi
}
trap cleanup_unpause EXIT

if [[ "${DO_PAUSE}" == "1" ]]; then
    # Check backend is actually running before pausing; compose pause fails
    # otherwise.
    if ! docker compose ps --status running --services 2>/dev/null | grep -qx "${BACKEND_SVC}"; then
        log "${BACKEND_SVC} not running — running snapshot without pause/unpause"
        DO_PAUSE=0
    else
        log "pausing ${BACKEND_SVC}"
        docker compose pause "${BACKEND_SVC}"
    fi
fi

# --- Snapshot via helper container --------------------------------------

# Stream tar of the volume into zstd on the host. We use a minimal busybox
# container to do the tar (no extra deps to install) and pipe through host
# zstd. This avoids the alpine `apk add zstd` step that fails when the
# container has no network access — and pause+rebuild sandboxes are the
# common case here.
log "snapshotting volume ${VOLUME_NAME} → ${TAR_PATH}"

# busybox:1.36 is ~5 MB, ships tar, and is widely cached. Stream stdout to
# host zstd which writes the final compressed file. Bind /dst is unused in
# this pipe-mode but kept commented for the alternative in-container mode.
docker run --rm \
    -v "${VOLUME_NAME}:/src:ro" \
    --entrypoint /bin/busybox \
    busybox:1.36 \
    tar -C /src -cf - . \
    | zstd -19 -T0 -o "${TAR_PATH}"

# --- Unpause (also handled by trap; do it eagerly for shorter freeze) ---

if [[ "${DO_PAUSE}" == "1" ]]; then
    log "unpausing ${BACKEND_SVC}"
    docker compose unpause "${BACKEND_SVC}"
    DO_PAUSE=0  # disarm the trap
fi

# --- Hash + verify ------------------------------------------------------

[[ -f "${TAR_PATH}" ]] || die "snapshot file missing: ${TAR_PATH}"
sha256sum "${TAR_PATH}" | awk '{print $1}' > "${SHA_PATH}"
SIZE="$(du -h "${TAR_PATH}" | awk '{print $1}')"
log "snapshot OK: ${TAR_PATH} (${SIZE}, sha256=$(cut -c1-12 "${SHA_PATH}"))"

# --- Rotate -------------------------------------------------------------

if [[ "${KEEP_COUNT}" -gt 0 ]]; then
    log "rotating: keeping last ${KEEP_COUNT} backups in ${DEST_DIR}"
    # find -printf '%T@ %p\n' gives mtime+path, sort newest-first, skip first
    # N, then delete the remaining paths plus their .sha256 sidecars. Names
    # are deterministic (timestamp-based) so SC2012 doesn't bite either way.
    mapfile -t old_tars < <(
        find "${DEST_DIR}" -maxdepth 1 -type f -name 'convex-*.tar.zst' -printf '%T@ %p\n' 2>/dev/null \
            | sort -nr \
            | tail -n +"$((KEEP_COUNT + 1))" \
            | awk '{print $2}'
    )
    for f in "${old_tars[@]}"; do
        [[ -f "$f" ]] || continue
        log "  rm $(basename "$f")"
        rm -f "$f" "${f%.tar.zst}.sha256"
    done
fi

log "done"
