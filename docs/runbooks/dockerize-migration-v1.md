# ClanWorld — Dockerize Migration v1 (Phase 2 Cutover Runbook)

**What this migrates:** hosted Convex + bare-metal `clanworld-runner.service` + heartbeat cron → self-hosted Convex + dockerized elder containers + heartbeat service, all running on the same VPS.

**Strategy:** parallel-coexist with explicit validation gate + 24h soak. Legacy services stay live through Step 8. Disabled only after `make smoke-test` returns clean AND a 30-min coexist observation passes. Not a big-bang cutover.

**When to use:** scheduled Phase 2 cutover on the production VPS, after rehearsal transcript is signed off.

**When NOT to use:** quick partial restarts, single-service updates, dev iteration. For those, use the `agents/Makefile` targets directly.

---

## Pinned versions

Verify these match your local `.env` before starting. Divergence = abort.

| Variable | Pin |
|---|---|
| `CONVEX_BACKEND_TAG` | `dd00d1f30042cedab25b8cc76a23c40cc2abbd90` |
| `CONVEX_DASHBOARD_TAG` | `dd00d1f30042cedab25b8cc76a23c40cc2abbd90` |
| `CONVEX_CLI_PINNED_VERSION` | `1.17.4` |

```bash
# Verify pins match .env.template at HEAD
grep -E "CONVEX_(BACKEND|DASHBOARD|CLI_PINNED)_" .env
```

---

## Prerequisites

| Requirement | How to verify |
|---|---|
| VPS SSH access | `ssh clan-world-vps` |
| `gh` CLI authenticated | `gh auth status` |
| `docker` + compose v2 | `docker compose version` (need ≥ 2.20) |
| `cast` (Foundry) | `cast --version` |
| `npx convex` at pinned version | `npx convex@1.17.4 --version` |
| `.env` populated | `CHAIN_NETWORK=prod`, `PROD_RPC_URL`, `CLAN_WORLD_CONTRACT_ADDRESS` |
| No hosted Convex outage | check `status.convex.dev` |
| Rehearsal transcript signed off | `docs/runbooks/dockerize-migration-v1-rehearsal-transcript.md` |

---

## Step 0 — Pre-flight

**Goal:** all secrets bootstrapped, no missing files before any container starts.

```bash
# From repo root:
make -C agents bootstrap-convex-admin-key
make -C agents bootstrap-bus-secrets
make -C agents oauth-bootstrap
```

**Verify:**
```bash
ls -l agents/secrets/
# Expect: convex-admin.key, bus-operator.key, bus-elder-{1..4}.key, webhook-shared.key
# All chmod 0600
```

**Rollback:** N/A (pre-flight only; no state changed yet).

---

## Step 1 — Rehearsal (mandatory)

**Goal:** dry-run the full import + schema fingerprint check against a throwaway self-hosted instance. Rehearsal is non-destructive and isolated from the prod stack.

```bash
# Stand up rehearsal stack (different compose project, rehearsal-suffixed volumes)
docker compose -f docker-compose.rehearsal.yml up -d

# Wait for convex-backend to be healthy
docker compose -f docker-compose.rehearsal.yml ps

# Export a recent hosted Convex snapshot
npx convex@1.17.4 export --path /tmp/clanworld-rehearsal-$(date +%Y%m%d-%H%M%S).zip \
  --include-file-storage

# Capture hosted schema fingerprint
npx convex@1.17.4 schema --json | sha256sum > /tmp/hosted-schema.sha256

# Deploy schema to rehearsal stack
CONVEX_SELF_HOSTED_URL=http://localhost:38050 \
  npx convex@1.17.4 deploy --admin-key "$(cat agents/secrets/convex-admin.key)"

# Capture rehearsal fingerprint
CONVEX_SELF_HOSTED_URL=http://localhost:38050 \
  npx convex@1.17.4 schema --json | sha256sum > /tmp/rehearsal-schema.sha256

# Validation gate — MUST be empty
diff /tmp/hosted-schema.sha256 /tmp/rehearsal-schema.sha256

# Import export into rehearsal
npx convex@1.17.4 import --replace-all \
  --instance-url http://localhost:38050 \
  /tmp/clanworld-rehearsal-*.zip

# Capture rehearsal run output to transcript template
# (Paste full terminal output into the transcript file)

# Tear down rehearsal — volumes are named with _rehearsal suffix, no prod data touched
docker compose -f docker-compose.rehearsal.yml down -v
```

**Verify:** `diff` above is empty. Import command exits 0. Transcript template filled in and operator sign-off recorded.

**Rollback:** Rehearsal stack is a separate compose project (`clan-world-rehearsal`) with dedicated volumes. `down -v` wipes everything. No prod rollback needed.

---

## Step 2 — Export hosted Convex

**Goal:** capture the authoritative hosted snapshot + schema fingerprint before touching anything.

```bash
BACKUP=/tmp/clanworld-backup-$(date +%Y%m%d-%H%M%S).zip
npx convex@1.17.4 export --path "$BACKUP" --include-file-storage
echo "Backup: $BACKUP ($(du -sh "$BACKUP" | cut -f1))"

# Schema fingerprint
npx convex@1.17.4 schema --json | sha256sum > /tmp/hosted-schema.sha256
cat /tmp/hosted-schema.sha256
```

**Verify:** `$BACKUP` exists and size > 0 (`ls -lh "$BACKUP"`).

**Rollback:** N/A (read-only operation).

---

## Step 3 — Stand up self-hosted Convex

**Goal:** prod self-hosted Convex backend healthy, schema deployed, fingerprint matches hosted.

```bash
make -C agents up PROFILE=prod

# Wait for healthcheck
docker compose --profile prod ps convex-backend
# Status must show "(healthy)" — allow up to 60s

# Deploy schema
make -C agents convex-deploy PROFILE=prod
# (or: CONVEX_SELF_HOSTED_URL=http://localhost:38046 \
#   npx convex@1.17.4 deploy --admin-key "$(cat agents/secrets/convex-admin.key)")

# Capture self-hosted fingerprint
CONVEX_SELF_HOSTED_URL=http://localhost:38046 \
  npx convex@1.17.4 schema --json | sha256sum > /tmp/selfhosted-schema.sha256

# Validation gate — MUST be empty; abort if diverged
diff /tmp/hosted-schema.sha256 /tmp/selfhosted-schema.sha256
```

**Verify:** `diff` empty. `docker compose --profile prod ps` shows `convex-backend` healthy.

**Rollback:**
```bash
make -C agents down
```

---

## Step 4 — Import hosted data

**Goal:** self-hosted Convex contains all hosted data; row counts match.

```bash
npx convex@1.17.4 import --replace-all \
  --instance-url http://localhost:38046 \
  /tmp/clanworld-backup-*.zip

# Re-verify schema fingerprint post-import
CONVEX_SELF_HOSTED_URL=http://localhost:38046 \
  npx convex@1.17.4 schema --json | sha256sum > /tmp/selfhosted-post-import.sha256
diff /tmp/hosted-schema.sha256 /tmp/selfhosted-post-import.sha256

# Sanity-check row counts (store output for comparison)
docker compose exec convex-backend \
  convex-backend --row-counts | tee /tmp/selfhosted-rowcounts.txt
```

**Verify:** both `diff` commands empty. Row counts in `/tmp/selfhosted-rowcounts.txt` non-zero for expected tables (`agentCommands`, `elderHeartbeat`, etc.).

**Rollback:**
```bash
make -C agents down
docker volume rm clan-world_convex_data
make -C agents up PROFILE=prod
# Then retry Step 4
```

---

## Step 5 — Swap Vercel deploy targets

**Goal:** the live web app points at self-hosted Convex via the Caddy/cloudflared tunnel URL.

```bash
# Note current hosted URL before changing it
grep NEXT_PUBLIC_CONVEX_URL apps/web/.env.production

# Update to self-hosted public URL (e.g. https://convex.clan-world.com)
# Edit apps/web/.env.production: NEXT_PUBLIC_CONVEX_URL=https://convex.clan-world.com
$EDITOR apps/web/.env.production

# Deploy to Vercel prod
vercel deploy --prod --cwd apps/web
```

**Verify:** open the live site in a browser. Open DevTools → Network → filter `convex.clan-world.com`. Confirm WebSocket connects and requests succeed.

**Rollback:**
```bash
# Revert .env.production to hosted URL
$EDITOR apps/web/.env.production
vercel deploy --prod --cwd apps/web
```

---

## Step 6 — Deploy host Caddy snippet

**Goal:** host Caddy reverse-proxies `/` and `wss://` for each elder and for the self-hosted Convex endpoint.

```bash
# Backup current Caddyfile
sudo cp /etc/caddy/Caddyfile \
  /etc/caddy/Caddyfile.pre-dockerize-$(date +%Y%m%d-%H%M%S)

# Install snippet (adds `import .../agents/shared/caddy.conf` to Caddyfile)
make -C agents install-caddy-snippet

# Validate
sudo caddy validate --config /etc/caddy/Caddyfile

# Reload
sudo systemctl reload caddy

# Verify
curl -I https://elder-1.clan-world.com/
# Expect: HTTP 200 or 101 (WebSocket upgrade)
```

**Verify:** `curl` returns 200/101. `make -C agents check-caddy-snippet` prints "installed".

**Rollback:**
```bash
sudo cp /etc/caddy/Caddyfile.pre-dockerize-* /etc/caddy/Caddyfile
sudo systemctl reload caddy
```

---

## Step 7 — 30-minute coexist observation

**Goal:** confirm both legacy and new stacks operate without errors for 30 minutes before any service is disabled.

**Legacy (still running):**
- `clanworld-runner.service` (systemd)
- heartbeat cron
- hosted Convex (cloud)

**New (also running):**
- `docker compose --profile prod` stack
- elder containers (elder-1..4)
- heartbeat container

```bash
# Monitor both sides simultaneously in two terminals:
journalctl -u clanworld-runner.service -f
# and:
docker compose logs -f
```

**Observe for 30 minutes:**
- No unhandled errors in either log stream
- Elder agents responding to tmux commands (`docker compose exec elder-1 tmux attach-session -t elder-1`)
- Heartbeat ticking in both stacks without collision

**If errors observed:** stop here. Run rollback for whichever side failed (Step 3, 5, or 6 as applicable). Do NOT proceed to Step 8.

**Rollback:** see rollback instructions under the failed step.

---

## Step 8 — Validation gate

**Goal:** automated smoke test passes before legacy is disabled.

```bash
make -C agents smoke-test
```

Smoke test script: `bin/check-stack-health.sh`. Checks: container health, tmux session alive, supervisor process alive, Convex reachable, heartbeat row updated within last 120s.

**Verify:** command exits 0 with no FAIL lines.

**Rollback:** do NOT proceed to Step 9. Investigate the failing assertion. Re-run `make smoke-test` after fixing. Do not time-box this step — a failed smoke test means the new stack is not ready.

---

## Step 9 — Disable legacy services

**Goal:** legacy systemd runner + heartbeat cron stopped and disabled.

```bash
sudo systemctl stop clanworld-runner.service
sudo systemctl disable clanworld-runner.service

# Disable hosted heartbeat cron (check both system and user crontab)
sudo crontab -l | grep -v clanworld-heartbeat | sudo crontab -
crontab -l | grep -v clanworld-heartbeat | crontab -

# Verify all legacy processes gone
pgrep -af clanworld-runner
# Expect: no output
```

**Verify:** `systemctl status clanworld-runner.service` shows `inactive (dead)`. No heartbeat entries in `crontab -l`.

**Rollback:**
```bash
sudo systemctl enable --now clanworld-runner.service
# Re-add heartbeat cron line (restore from backup or re-run bootstrap)
```

---

## Step 10 — 24-hour soak

**Goal:** confirm the new stack sustains normal operation for 24 hours before any legacy state is archived.

- Watch dashboards: `make -C agents status`
- Confirm elder heartbeats updating: check `elderHeartbeat` table in Convex dashboard (`http://localhost:38048`)
- Confirm agent activity: `docker compose logs --since 1h elder-1 elder-2 elder-3 elder-4`
- Capture any incidents to a follow-up GH issue with label `phase-2-soak`
- **DO NOT delete legacy state during soak** — it is the last clean rollback point

**Rollback during soak:**
```bash
# Re-enable legacy (Step 9 rollback)
sudo systemctl enable --now clanworld-runner.service
# Re-enable heartbeat cron
# Swap Vercel env back (Step 5 rollback)
```

---

## Step 11 — Archive legacy state

**Goal:** legacy runner state archived (not deleted) after clean 24h soak.

```bash
ARCHIVE_DATE=$(date +%Y%m%d)
mkdir -p ~/.world/archive

# Archive runner state
mv ~/code/clan-world/legacy-runner-state \
  ~/.world/archive/runner-state-pre-dockerize-${ARCHIVE_DATE}

# Document archive path
cat >> docs/operations/dockerize-cutover-${ARCHIVE_DATE}.md <<EOF
# ClanWorld Dockerize Cutover — ${ARCHIVE_DATE}

Legacy runner state archived to:
  ~/.world/archive/runner-state-pre-dockerize-${ARCHIVE_DATE}

Cutover completed by: <operator>
Soak period: <start> – <end>
All checks passed: yes
EOF
```

**Verify:** `ls ~/.world/archive/runner-state-pre-dockerize-${ARCHIVE_DATE}` exists. `~/code/clan-world/legacy-runner-state` is gone.

**Rollback:**
```bash
mv ~/.world/archive/runner-state-pre-dockerize-${ARCHIVE_DATE} \
  ~/code/clan-world/legacy-runner-state
```

---

## Troubleshooting

### Schema fingerprint diverged after Step 3 or Step 4

```bash
diff /tmp/hosted-schema.sha256 /tmp/selfhosted-schema.sha256
```

Indicates a schema migration is needed before import. Locate the targeted migration script under `apps/server/convex/migrations/` or file a follow-up issue with label `schema-migration`. Do NOT proceed past Step 3 with a diverged fingerprint.

### Self-hosted Convex unhealthy

```bash
docker compose logs convex-backend | tail -50
```

Common causes:
- `CONVEX_INSTANCE_NAME` not set in `.env` → add it and `make down && make up PROFILE=prod`
- Admin key file missing or empty → re-run `make -C agents bootstrap-convex-admin-key FORCE=1`
- Port 38046 already in use → `lsof -i :38046`; stop the conflicting process

### Vercel post-swap can't reach self-hosted

1. Check Caddy snippet is installed: `make -C agents check-caddy-snippet`
2. Check cloudflared/Caddy logs: `sudo journalctl -u caddy -f`
3. Verify self-hosted Convex is reachable from the VPS itself: `curl http://localhost:38046/api/query`
4. Check tunnel binding — the public URL in Vercel must match the Caddy snippet's hostname

### Agent containers can't reach Convex

Elder containers resolve `convex-backend` via Docker DNS on the `clan-world-internal` network. Verify:
```bash
docker compose exec elder-1 curl -s http://convex-backend:3210/api/query
```
If that fails: confirm `CONVEX_DEPLOY_URL=http://convex-backend:3210` in the elder service env and that all containers are on the same compose network.

### Row counts after import mismatch

Re-run the import (it is idempotent with `--replace-all`):
```bash
npx convex@1.17.4 import --replace-all \
  --instance-url http://localhost:38046 \
  /tmp/clanworld-backup-*.zip
```
If mismatch persists after second attempt: abort, run Step 3 rollback (`make down && docker volume rm clan-world_convex_data`), then retry from Step 3.

### Elder container restart loop on boot

Check entrypoint logs for "did not become ready" or "FATAL":
```bash
docker compose logs elder-1 | grep -E "FATAL|ready|ERROR" | tail -20
```
Common causes:
- `ELDER_N` not set → verify `environment.ELDER_N` in compose
- Secret file missing → verify `agents/secrets/bus-elder-N.key` exists
- Stale supervisor lock → `docker compose exec elder-1 rm -f /workspace/.runtime/supervisor.lock`

---

## Cross-links

- `docs/plans/dockerize-elder-infra-v1.md` — the plan this runbook implements
- `docs/runbooks/fresh-vps-bootstrap.md` — initial VPS setup
- `docs/runbooks/anvil-fork-dev-rpc.md` — dev RPC setup
- `docs/runbooks/soft-game-reset.md` — soft reset without full cutover
- `agents/Makefile` — canonical lifecycle targets
- `docker-compose.rehearsal.yml` — throwaway rehearsal overlay
- `docs/runbooks/dockerize-migration-v1-rehearsal-transcript.md` — rehearsal transcript template
