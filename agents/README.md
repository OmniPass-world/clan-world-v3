# agents/ — Elder container infrastructure

This directory contains everything needed to bring up the Elder agent containers (`elder-1` through `elder-4`) that play ClanWorld on behalf of human clan owners. The Makefile in this directory (see issue #355) is the canonical operator entry point.

## Layout

```
agents/
  .gitignore                  # ignores runtime/, .env, .docker-mounts/
  README.md                   # this file
  Makefile                    # operator commands (issue #355, separate from this slice)
  docker-compose.elders.yml   # service definitions (issue #344/#345, generated)

  shared/                     # shared, committed base — same content in every Elder container
    run.sh                              # container entrypoint: load env, --continue or fresh claude
    APPENDED_SYSTEM_PROMPT.md           # appended via --append-system-prompt (game context, MCP tools)
    home-claude/                        # shared CC harness state, R/O overlay into /home/elder/.claude/
      settings.json                       # CC permission deny rules (block credentials, env leak, etc)
      CLAUDE.md                           # shared system prompt: elder role + tool index + workflow
      skills/                             # shared base skills
        lean-tick/SKILL.md
        research-mindset/SKILL.md
        README.md                         # placeholder explaining shared-base skills go here
    elder-bootstrap/                    # seed files used by `make wipe-elder-N` to re-init /workspace/
      workspace-CLAUDE.md
      workspace-ANCIENT_WISDOM.md         # the prompt-to-future-self template
      workspace-README.md

  elder-1/                    # per-elder
    .env.template                       # template — copy to .env (gitignored) and fill in secrets
    .env                                  # GITIGNORED — copy .env.template, fill in secrets, docker compose reads this
    seed/                               # OPTIONAL per-elder bootstrap overrides
      .gitkeep
  elder-2/  ...  elder-3/  ...  elder-4/

  # runtime/ — gitignored, created by docker compose at bring-up
  # .docker-mounts/ — gitignored, created by `make link-mounts` for inspectability
```

## Mount layout (per-elder)

At runtime, each `elder-N` container sees:

| Host path | Container path | Mode |
|-----------|---------------|------|
| `/var/lib/clan-world/agents/elder-N/home-claude` | `/home/elder/.claude` | R/W |
| `/var/lib/clan-world/agents/elder-N/workspace` | `/workspace` | R/W |
| `./agents/shared/home-claude/CLAUDE.md` | `/home/elder/.claude/CLAUDE.md` | R/O overlay |
| `./agents/shared/home-claude/skills/` | `/home/elder/.claude/skills/` | R/O overlay |

The R/W host paths are created by `make bootstrap` (#355). The R/O overlays shadow `CLAUDE.md`
and `skills/` inside the R/W home directory.
`settings.json` is instead seeded by `run.sh` at every container start (atomic cp from
`/opt/clan-world/shared/home-claude/settings.json`); it remains mutable during the session
and resets on restart.

> **Ownership:** The Makefile bootstrap (`make bootstrap`) creates these host paths and sets `elder:elder`
> (uid/gid 1000) ownership before `docker compose up`. Without this step, the container's `elder` user
> cannot write to `/home/elder/.claude` or `/workspace`.

## Dev workflow

1. Copy each `elder-N/.env.template` → `elder-N/.env`, fill in `ANTHROPIC_OAUTH_TOKEN` and `BUS_ELDER_SECRET`.
2. `make up` — bring up all 4 elder containers + supporting services.
3. `make status` — see container + tmux + plugin states.
4. `make logs ELDER=elder-2` — tail one elder's run.sh + claude output.
5. `make reset-elder-3` — soft reset (kill tmux, keep conversation, restart with `--continue`).
6. `make wipe-elder-3` — hard reset (delete sessions + workspace, re-seed from `shared/elder-bootstrap/`).

The Makefile lives next to what it orchestrates. See issue #355 for the full target list and the operator-side compose plumbing.

## Shared vs per-elder

- `shared/` is the **committed, immutable** base. Every elder gets the same content R/O-overlaid into its container. Editing a file under `shared/` propagates to every elder on next container restart.
- `elder-N/` is **per-elder**. The only committed contents are `.env.template` and (optionally) `seed/` overrides for that specific elder's personality. Runtime state (`runtime/elder-N/.claude/`, `runtime/elder-N/workspace/`) is **gitignored** — it's where the CC harness writes credentials, conversation logs, and the agent's own working files.

## Relationship to host `~/.claude/`

The container's `/home/elder/.claude/` is **independent** of any human developer's `~/.claude/`. The container mounts:
- `agents/shared/home-claude/CLAUDE.md` → `/home/elder/.claude/CLAUDE.md` (R/O)
- `agents/shared/home-claude/skills/` → `/opt/clan-world/shared/skills/` (R/O — runtime-merged into per-elder skills via the seed manifest)
- `runtime/elder-N/.claude/` → `/home/elder/.claude/projects/` and `/home/elder/.claude/.credentials.json` (R/W, per-elder)
- `settings.json` is seeded by `run.sh` at container start (not a bind mount — mutable during session, resets on restart)

This keeps a clean separation: shared base files are version-controlled and inspectable; per-elder runtime state is mutable and gitignored.

## Submodule note

For v1 this lives in-repo. A follow-up issue (TBD) will convert `agents/` to a git submodule so the elder-infra lifecycle is decoupled from the game-contract lifecycle.

## Self-hosted Convex bootstrap (Phase 1.4, #347)

The compose stack ships two Convex containers: `convex-backend` (the Rust binary + SQLite) and `convex-dashboard` (the Next.js admin UI). Both are pinned to a specific git SHA — see `CONVEX_BACKEND_TAG` / `CONVEX_DASHBOARD_TAG` in `.env.template`.

### First-time setup (one-shot per host)

```bash
# 1. Bring up the backend so it can generate its INSTANCE_SECRET.
docker compose --profile dev up -d convex-backend

# 2. Generate the admin key. This script is provided by Phase 1.12 (#355);
#    the canonical recipe it wraps is:
#       docker compose exec convex-backend ./generate_admin_key.sh \
#         | sudo tee /etc/clan-world/secrets/convex-admin.key >/dev/null
#       sudo chmod 600 /etc/clan-world/secrets/convex-admin.key
#       ln -sf /etc/clan-world/secrets/convex-admin.key \
#         agents/secrets/convex-admin.key
make bootstrap-convex-admin-key

# 3. Bring up the dashboard now that the backend is healthy.
docker compose --profile dev up -d convex-dashboard

# 4. Deploy the convex/ functions into the self-hosted backend.
bin/import-convex-schema.sh

# 5. Open the dashboard, paste the key from agents/secrets/convex-admin.key
#    when prompted.
xdg-open "http://127.0.0.1:${CONVEX_DASHBOARD_HOST_PORT:-38048}"
```

### Daily operations

| What                       | How                                                          |
|----------------------------|--------------------------------------------------------------|
| Deploy schema/functions    | `bin/import-convex-schema.sh` (idempotent — skips if no diff)|
| Check sync status          | `bin/import-convex-schema.sh --check` (exit 3 if drift)      |
| Snapshot the data volume   | `bin/backup-convex.sh` (pause → tar.zst → unpause)           |
| Migrate from hosted Convex | `bin/migrate-from-hosted-convex.sh` (Phase 2 cutover only)   |
| Restart backend safely     | `docker compose stop convex-backend && docker compose start convex-backend` — INSTANCE_SECRET persists, same admin key |
| Inspect a query / data     | dashboard at `http://127.0.0.1:${CONVEX_DASHBOARD_HOST_PORT}`|

### Files

```
agents/secrets/
  .gitignore                       # only .template + README.md committed
  convex-admin-key.template        # docs the convex-admin.key layout
  convex-admin.key                 # GENERATED — gitignored, chmod 600
bin/
  import-convex-schema.sh          # npx convex deploy wrapper
  migrate-from-hosted-convex.sh    # one-time migration runbook
  backup-convex.sh                 # paused-snapshot of convex_data volume
```

### Data location

Inside the container: `/convex/data/`. From the host: docker volume `clan-world_convex_data`. The `bin/backup-convex.sh` script tars the volume contents while the backend is paused.

### Admin-key safety

- The key is derived from `INSTANCE_NAME + INSTANCE_SECRET`, both persisted in the `convex_data` volume. **Volume survives → captured admin key stays valid across restarts.** (Note: `generate_admin_key.sh` embeds a random nonce, so a fresh exec returns a textually different but functionally equivalent key. All keys derived from the same `INSTANCE_SECRET` unlock the same instance.)
- The dashboard does NOT receive the key via env. The operator pastes it into the dashboard UI on first visit (Next.js stores it in browser `localStorage`).
- The CLI tools read the key from `agents/secrets/convex-admin.key` (file) — env-var fallback exists but file is preferred (env leaks via `docker inspect`).
