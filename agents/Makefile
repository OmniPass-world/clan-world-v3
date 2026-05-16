# agents/Makefile — ClanWorld Elder Infrastructure operator entrypoint
#
# Issue #355 (Phase 1.12). Codifies all operator-facing lifecycle, bootstrap,
# wipe, and smoke-test targets for the dockerized elder stack. The compose
# project name is `clan-world` (declared in docker-compose.yml), so volumes
# created by compose are prefixed `clan-world_*`.
#
# PROFILE is REQUIRED on every state-mutating target (Finding 33 fix). No
# default — fail loud. On the VPS, almost certainly PROFILE=prod.
#
# Run from the repo root: `make -C agents <target> PROFILE=dev|prod`
# Or `cd agents && make <target> PROFILE=...`
#
# All paths in compose-volume names + secrets files are relative to the
# REPO ROOT (the parent of this Makefile), because docker-compose.yml lives
# at the repo root and `docker compose` is invoked from there.

# ---------------------------------------------------------------------------
# Locate the repo root (parent of agents/) and always invoke compose from
# there so relative paths in docker-compose.yml resolve correctly regardless
# of which directory the operator invokes `make` from.
# ---------------------------------------------------------------------------
REPO_ROOT := $(abspath $(dir $(lastword $(MAKEFILE_LIST)))/..)
COMPOSE   := docker compose
DC        := cd $(REPO_ROOT) && $(COMPOSE)

ELDERS := elder-1 elder-2 elder-3 elder-4

# PROFILE is REQUIRED. No default — fail loud.
PROFILE ?=

# Pinned Convex CLI version — used by bootstrap-convex-bus-secrets.
CONVEX_CLI_PINNED_VERSION ?= 1.17.4

# wipe-elder-N LEVEL default = session (matches prior CC --continue behavior)
LEVEL ?= session

.PHONY: help \
        check-profile confirm-banner setup \
        up down status logs \
        pause unpause pause-% unpause-% \
        pause-heartbeat unpause-heartbeat \
        reset-% restart-% wipe-% \
        reset-anvil \
        link-mounts \
        bootstrap-convex-admin-key bootstrap-bus-secrets bootstrap-convex-dashboard-auth \
        bootstrap-deployer-key bootstrap-convex-bus-secrets \
        oauth-bootstrap oauth-bootstrap-% \
        install-caddy-snippet uninstall-caddy-snippet check-caddy-snippet \
        smoke-test build

# ---------------------------------------------------------------------------
# help
# ---------------------------------------------------------------------------
help:
	@echo "ClanWorld Elder Infrastructure — agents/Makefile"
	@echo ""
	@echo "USAGE"
	@echo "  make <target> PROFILE=dev|prod"
	@echo ""
	@echo "  PROFILE is REQUIRED on state-mutating targets. No default."
	@echo "  On the VPS, almost certainly PROFILE=prod."
	@echo ""
	@echo "LIFECYCLE"
	@echo "  setup PROFILE=...              One-time bring-up: bootstrap secrets + secret dirs"
	@echo "  up PROFILE=...                 docker compose --profile <P> up -d (with confirmation banner)"
	@echo "  down                           docker compose down (idempotent; no profile required)"
	@echo "  status                         compose ps + per-elder tmux/supervisor health"
	@echo "  logs ELDER=elder-2             tail one service"
	@echo ""
	@echo "PAUSE / RESUME"
	@echo "  pause-elder-2                  SIGSTOP supervisor inside elder-2 (Finding 24 fix)"
	@echo "  unpause-elder-2                SIGCONT supervisor"
	@echo "  pause                          fan-out SIGSTOP across all elders"
	@echo "  unpause                        fan-out SIGCONT across all elders"
	@echo "  pause-heartbeat                stop heartbeat service (coexist window)"
	@echo "  unpause-heartbeat              start heartbeat service"
	@echo ""
	@echo "RESET / WIPE"
	@echo "  reset-elder-3                  soft reset: kill tmux session, restart container"
	@echo "  restart-elder-3                full container restart (no wipe)"
	@echo "  wipe-elder-3 LEVEL=workspace   clear /workspace/* only, keep ~/.claude (sessions + creds)"
	@echo "  wipe-elder-3 LEVEL=session     clear /workspace/* + ~/.claude/projects (keep creds + skills)"
	@echo "  wipe-elder-3 LEVEL=full        destroy named volumes; oauth-bootstrap required after"
	@echo "  reset-anvil PROFILE=dev        wipe anvil fork state + restart (dev only)"
	@echo ""
	@echo "BOOTSTRAP (one-time per stack — re-run with FORCE=1 to overwrite)"
	@echo "  bootstrap-convex-admin-key     generate + persist Convex admin key (chmod 0600)"
	@echo "  bootstrap-bus-secrets          generate BUS operator + per-elder secrets (chmod 0600)"
	@echo "  bootstrap-deployer-key         paste deployer private key → agents/secrets/deployer-private.key"
	@echo "  bootstrap-convex-bus-secrets   inject bus secrets into self-hosted Convex via npx convex env set"
	@echo "  bootstrap-convex-dashboard-auth generate basic-auth credentials for dashboard"
	@echo "  oauth-bootstrap                walk every elder through OAuth flow"
	@echo "  oauth-bootstrap-elder-1        single-elder OAuth flow"
	@echo ""
	@echo "HOST CADDY"
	@echo "  install-caddy-snippet          add 'import .../agents/shared/caddy.conf' to host Caddyfile"
	@echo "  uninstall-caddy-snippet        remove the import line"
	@echo "  check-caddy-snippet            report-only: is the import line present?"
	@echo ""
	@echo "DEV HELPERS"
	@echo "  link-mounts                    snapshot named-volume contents into agents/.docker-mounts/"
	@echo "                                 (uses helper container — Finding 34 fix; no sudo needed)"
	@echo "  smoke-test                     run end-to-end stack health check (bin/check-stack-health.sh)"
	@echo "  build                          docker compose build for the selected profile"

# ---------------------------------------------------------------------------
# PROFILE gate + confirmation banner
# ---------------------------------------------------------------------------
check-profile:
	@if [ -z "$(PROFILE)" ]; then \
		echo "ERROR: PROFILE=dev|prod required. On VPS, almost certainly PROFILE=prod." >&2; \
		exit 1; \
	fi
	@if [ "$(PROFILE)" != "dev" ] && [ "$(PROFILE)" != "prod" ]; then \
		echo "ERROR: PROFILE must be dev or prod (got: $(PROFILE))" >&2; \
		exit 1; \
	fi

# Loud confirmation banner shown BEFORE any container starts (Finding 33 fix).
# Pulls CHAIN_NETWORK / RPC / contract / Convex from the env so the operator
# sees exactly what they're about to spin up.
confirm-banner: check-profile
	@echo "===================================="
	@echo " ClanWorld stack bring-up"
	@echo " PROFILE:        $(PROFILE)"
	@echo " CHAIN_NETWORK:  $${CHAIN_NETWORK:-<unset>}"
	@if [ "$(PROFILE)" = "dev" ]; then \
		echo " RPC URL:        $${DEV_RPC_URL:-<unset — anvil-fork will be used>}"; \
	else \
		echo " RPC URL:        $${PROD_RPC_URL:-<unset — bring-up WILL fail>}"; \
	fi
	@echo " Contract:       $${CLAN_WORLD_CONTRACT_ADDRESS:-<unset>}"
	@echo " Convex:         $${CONVEX_SELF_HOSTED_URL:-http://convex-backend:3210}"
	@echo "===================================="

# ---------------------------------------------------------------------------
# Lifecycle
# ---------------------------------------------------------------------------
setup: check-profile
	@echo "[setup] bootstrapping ClanWorld stack for PROFILE=$(PROFILE)"
	@mkdir -p $(REPO_ROOT)/agents/secrets
	@chmod 0700 $(REPO_ROOT)/agents/secrets
	@echo "[setup] creating + chowning per-elder bind-mount dirs (requires sudo)"
	@for n in 1 2 3 4; do \
	  for d in home-claude workspace; do \
	    sudo mkdir -p /var/lib/clan-world/agents/elder-$$n/$$d; \
	    sudo chown 1000:1000 /var/lib/clan-world/agents/elder-$$n/$$d; \
	  done; \
	done
	@echo "[setup] bind-mount dirs created"
	$(MAKE) bootstrap-convex-admin-key
	$(MAKE) bootstrap-bus-secrets
	@if [ ! -s "$(REPO_ROOT)/agents/secrets/deployer-private.key" ]; then \
		$(MAKE) bootstrap-deployer-key; \
	fi
	@echo "[setup] done."
	@echo "[setup] Next:"
	@echo "  1. make oauth-bootstrap                  # per-elder OAuth credentials"
	@echo "  2. make up PROFILE=$(PROFILE)            # spin up the stack"
	@echo "  3. make smoke-test                       # verify all green"

up: confirm-banner
	$(DC) --profile $(PROFILE) up -d

down:
	$(DC) down

status:
	@$(DC) ps
	@echo "---"
	@for e in $(ELDERS); do \
		echo -n "$$e tmux:       "; \
		$(DC) exec -T $$e tmux ls 2>/dev/null | head -1 || echo DEAD; \
		echo -n "$$e supervisor: "; \
		$(DC) exec -T $$e pgrep -f 'tsx.*elder-runtime/src/main.ts' > /dev/null 2>&1 \
			&& echo OK || echo DEAD; \
	done

logs:
	@if [ -z "$(ELDER)" ]; then \
		echo "ERROR: ELDER=<service-name> required (e.g. ELDER=elder-2 or ELDER=heartbeat)" >&2; \
		exit 1; \
	fi
	$(DC) logs -f $(ELDER)

# ---------------------------------------------------------------------------
# Pause / unpause — SIGSTOP supervisor inside container (Finding 24 fix).
# `docker compose pause` freezes the WHOLE container (incl. tmux + ttyd),
# which kills the operator's shell. SIGSTOP on just the supervisor process
# keeps tmux + ttyd alive so the operator can still attach + inspect.
# ---------------------------------------------------------------------------
pause-elder-%:
	$(DC) exec -T $* sh -c 'kill -STOP $$(pgrep -f 'tsx.*elder-runtime/src/main.ts')'

unpause-elder-%:
	$(DC) exec -T $* sh -c 'kill -CONT $$(pgrep -f 'tsx.*elder-runtime/src/main.ts')'

pause:
	@for e in $(ELDERS); do $(MAKE) pause-elder-$$e || true; done

unpause:
	@for e in $(ELDERS); do $(MAKE) unpause-elder-$$e || true; done

pause-heartbeat:
	$(DC) stop heartbeat

unpause-heartbeat:
	$(DC) start heartbeat

# ---------------------------------------------------------------------------
# Reset / restart / wipe
# ---------------------------------------------------------------------------
# reset-elder-N: kill tmux session, restart container so run.sh re-attaches
# with --continue (preserves CC conversation history under ~/.claude/projects).
reset-%:
	-$(DC) exec -T $* tmux kill-session -t $* 2>/dev/null || true
	$(DC) restart $*

restart-%:
	$(DC) restart $*

# wipe-elder-N LEVEL=workspace|session|full (Finding 35 fix).
#   workspace = clear /workspace/*, keep ~/.claude (skills, projects, creds)
#   session   = clear /workspace/* + ~/.claude/projects (keep creds + skills + plugins),
#               then reseed workspace bootstrap files from /opt/clan-world/shared/elder-bootstrap/
#   full      = destroy elder-N-home + elder-N-workspace named volumes; oauth-bootstrap required
wipe-%:
	@if [ "$(LEVEL)" = "workspace" ]; then \
		echo "[wipe] $* LEVEL=workspace — clearing /workspace/*"; \
		$(DC) exec -T $* sh -c 'rm -rf /workspace/*' || exit 1; \
		$(MAKE) reset-$*; \
	elif [ "$(LEVEL)" = "session" ]; then \
		echo "[wipe] $* LEVEL=session — clearing /workspace/* + ~/.claude/projects"; \
		$(DC) exec -T $* sh -c 'rm -rf /home/elder/.claude/projects /workspace/*' || exit 1; \
		$(DC) exec -T $* sh -c 'if [ -f /opt/clan-world/shared/elder-bootstrap/workspace-CLAUDE.md ]; then cp /opt/clan-world/shared/elder-bootstrap/workspace-CLAUDE.md /workspace/CLAUDE.md; fi' || true; \
		$(DC) exec -T $* sh -c 'if [ -f /opt/clan-world/shared/elder-bootstrap/workspace-ANCIENT_WISDOM.md ]; then cp /opt/clan-world/shared/elder-bootstrap/workspace-ANCIENT_WISDOM.md /workspace/ANCIENT_WISDOM.md; fi' || true; \
		$(MAKE) reset-$*; \
	elif [ "$(LEVEL)" = "full" ]; then \
		echo "[wipe] $* LEVEL=full — destroying named volumes (compose project: clan-world)"; \
		$(DC) stop $* || true; \
		$(DC) rm -f $* || true; \
		docker volume rm clan-world_$*-home clan-world_$*-workspace 2>/dev/null || true; \
		echo "[wipe] $* FULL WIPE done."; \
		echo "[wipe] NEXT: run \`make oauth-bootstrap-$*\` BEFORE \`make up\`; volumes will be recreated empty."; \
	else \
		echo "ERROR: LEVEL must be workspace, session, or full (got: $(LEVEL))" >&2; \
		exit 1; \
	fi

# Dev-only: wipe anvil fork state. Useful when fork chain state drifts from
# real Base Sepolia and you want a fresh fork pinned to FORK_BLOCK_NUMBER.
reset-anvil: check-profile
	@if [ "$(PROFILE)" != "dev" ]; then \
		echo "ERROR: reset-anvil is dev-only (PROFILE=dev)" >&2; \
		exit 1; \
	fi
	$(DC) --profile dev stop anvil-fork
	-docker volume rm clan-world_anvil_data 2>/dev/null || true
	$(DC) --profile dev up -d anvil-fork

# ---------------------------------------------------------------------------
# link-mounts — helper-container snapshot (Finding 34 fix).
#
# Named-volume mountpoints under /var/lib/docker/volumes/... are root-owned
# and unreadable to the operator's UID. Rather than symlinking into that
# tree (which would require sudo to read), we run a tiny busybox container
# that has access to the named volume + writes a chowned copy into the host
# bind-mount agents/.docker-mounts/. Trade-off: snapshots get stale; re-run
# `make link-mounts` on demand. The agents/.docker-mounts/ dir is gitignored.
# ---------------------------------------------------------------------------
link-mounts:
	@mkdir -p $(REPO_ROOT)/agents/.docker-mounts
	@for e in $(ELDERS); do \
		echo "[link-mounts] snapshotting $$e-workspace + $$e-home"; \
		docker run --rm \
			-v clan-world_$$e-workspace:/src-workspace:ro \
			-v clan-world_$$e-home:/src-home:ro \
			-v $(REPO_ROOT)/agents/.docker-mounts:/dst \
			busybox sh -c "\
				rm -rf /dst/$$e-workspace /dst/$$e-home && \
				cp -a /src-workspace /dst/$$e-workspace && \
				cp -a /src-home /dst/$$e-home && \
				chown -R $$(id -u):$$(id -g) /dst/$$e-workspace /dst/$$e-home \
			" || echo "[link-mounts] WARN: $$e snapshot skipped (volume may not exist yet)"; \
	done
	@echo "[link-mounts] snapshots at $(REPO_ROOT)/agents/.docker-mounts/"

# ---------------------------------------------------------------------------
# Bootstrap targets
#
# All write to agents/secrets/<name>.key with chmod 0600. Idempotent: bail
# fast if a key already exists, unless FORCE=1 is set. The compose file
# expects these exact filenames (see docker-compose.yml `secrets:` block).
# ---------------------------------------------------------------------------
bootstrap-convex-admin-key:
	@mkdir -p $(REPO_ROOT)/agents/secrets
	@chmod 0700 $(REPO_ROOT)/agents/secrets
	@KEY=$(REPO_ROOT)/agents/secrets/convex-admin.key; \
	if [ -s "$$KEY" ] && [ "$(FORCE)" != "1" ]; then \
		echo "[bootstrap] $$KEY exists; skipping. Re-run with FORCE=1 to regenerate."; \
		exit 0; \
	fi; \
	echo "[bootstrap] bringing up convex-backend (transient) to mint admin key..."; \
	$(DC) up -d --no-deps convex-backend; \
	sleep 5; \
	$(DC) exec -T convex-backend ./generate_admin_key.sh > "$$KEY"; \
	chmod 0600 "$$KEY"; \
	FP=$$(sha256sum "$$KEY" | cut -d' ' -f1); \
	echo "$$FP" > "$$KEY.fp"; \
	chmod 0644 "$$KEY.fp"; \
	echo "[bootstrap] convex-admin.key generated."; \
	echo "[bootstrap]   path:        $$KEY"; \
	echo "[bootstrap]   fingerprint: $$FP"; \
	echo "[bootstrap] BACK THIS UP NOW — the key is unrecoverable if lost."

bootstrap-bus-secrets:
	@mkdir -p $(REPO_ROOT)/agents/secrets
	@chmod 0700 $(REPO_ROOT)/agents/secrets
	@OP=$(REPO_ROOT)/agents/secrets/bus-operator.key; \
	if [ -s "$$OP" ] && [ "$(FORCE)" != "1" ]; then \
		echo "[bootstrap] $$OP exists; skipping. Re-run with FORCE=1 to regenerate."; \
	else \
		openssl rand -hex 32 > "$$OP"; \
		chmod 0600 "$$OP"; \
		echo "[bootstrap] bus-operator.key generated"; \
	fi
	@WH=$(REPO_ROOT)/agents/secrets/webhook-shared.key; \
	if [ -s "$$WH" ] && [ "$(FORCE)" != "1" ]; then \
		echo "[bootstrap] $$WH exists; skipping. Re-run with FORCE=1 to regenerate."; \
	else \
		openssl rand -hex 32 > "$$WH"; \
		chmod 0600 "$$WH"; \
		echo "[bootstrap] webhook-shared.key generated"; \
	fi
	@for n in 1 2 3 4; do \
		EKEY=$(REPO_ROOT)/agents/secrets/bus-elder-$$n.key; \
		if [ -s "$$EKEY" ] && [ "$(FORCE)" != "1" ]; then \
			echo "[bootstrap] $$EKEY exists; skipping. Re-run with FORCE=1 to regenerate."; \
		else \
			openssl rand -hex 32 > "$$EKEY"; \
			chmod 0600 "$$EKEY"; \
			echo "[bootstrap] bus-elder-$$n.key generated"; \
		fi; \
	done

bootstrap-convex-dashboard-auth:
	@mkdir -p $(REPO_ROOT)/agents/secrets
	@chmod 0700 $(REPO_ROOT)/agents/secrets
	@DEST=$(REPO_ROOT)/agents/secrets/convex-dashboard-auth.json; \
	if [ -s "$$DEST" ] && [ "$(FORCE)" != "1" ]; then \
		echo "[bootstrap] $$DEST exists; skipping. Re-run with FORCE=1 to regenerate."; \
		exit 0; \
	fi; \
	printf "Username: " >&2; \
	read u; \
	printf "Password: " >&2; \
	stty -echo 2>/dev/null || true; \
	read p; \
	stty echo 2>/dev/null || true; \
	echo >&2; \
	if [ -z "$$u" ] || [ -z "$$p" ]; then \
		echo "ERROR: username + password required" >&2; \
		exit 1; \
	fi; \
	HASH=$$(printf '%s' "$$p" | sha256sum | cut -d' ' -f1); \
	printf '{"username":"%s","password_sha256":"%s"}\n' "$$u" "$$HASH" > "$$DEST"; \
	chmod 0600 "$$DEST"; \
	echo "[bootstrap] convex-dashboard-auth.json saved to $$DEST"

bootstrap-deployer-key:
	@mkdir -p $(REPO_ROOT)/agents/secrets
	@chmod 0700 $(REPO_ROOT)/agents/secrets
	@DEST=$(REPO_ROOT)/agents/secrets/deployer-private.key; \
	if [ -s "$$DEST" ] && [ "$(FORCE)" != "1" ]; then \
		echo "[bootstrap] $$DEST exists; skipping. Re-run with FORCE=1 to overwrite."; \
		exit 0; \
	fi; \
	echo "[bootstrap] Paste the deployer private key (0x-prefixed hex, input hidden):" >&2; \
	stty -echo 2>/dev/null || true; \
	read key; \
	stty echo 2>/dev/null || true; \
	echo >&2; \
	if [ -z "$$key" ]; then \
		echo "ERROR: empty key" >&2; \
		exit 1; \
	fi; \
	printf '%s\n' "$$key" > "$$DEST"; \
	chmod 0600 "$$DEST"; \
	echo "[bootstrap] deployer-private.key saved to $$DEST"

bootstrap-convex-bus-secrets:
	@INSTANCE_URL="$${CONVEX_SELF_HOSTED_URL:-http://localhost:38046}"; \
	ADMIN_KEY="$$(cat $(REPO_ROOT)/agents/secrets/convex-admin.key 2>/dev/null)"; \
	if [ -z "$$ADMIN_KEY" ]; then \
		echo "ERROR: agents/secrets/convex-admin.key not found — run bootstrap-convex-admin-key first" >&2; \
		exit 1; \
	fi; \
	echo "[bootstrap] injecting bus secrets into self-hosted Convex at $$INSTANCE_URL"; \
	npx convex@$(CONVEX_CLI_PINNED_VERSION) env set BUS_OPERATOR_SECRET \
		"$$(cat $(REPO_ROOT)/agents/secrets/bus-operator.key)" \
		--instance-url "$$INSTANCE_URL" --admin-key "$$ADMIN_KEY"; \
	for n in 1 2 3 4; do \
		npx convex@$(CONVEX_CLI_PINNED_VERSION) env set BUS_ELDER_SECRET_$$n \
			"$$(cat $(REPO_ROOT)/agents/secrets/bus-elder-$$n.key)" \
			--instance-url "$$INSTANCE_URL" --admin-key "$$ADMIN_KEY"; \
	done; \
	npx convex@$(CONVEX_CLI_PINNED_VERSION) env set WEBHOOK_SHARED_SECRET \
		"$$(cat $(REPO_ROOT)/agents/secrets/webhook-shared.key)" \
		--instance-url "$$INSTANCE_URL" --admin-key "$$ADMIN_KEY"; \
	echo "[bootstrap] bus secrets injected (6 vars: BUS_OPERATOR_SECRET + BUS_ELDER_SECRET_1..4 + WEBHOOK_SHARED_SECRET)"

# Per-elder OAuth bootstrap. Writes to agents/elder-N/.env (already gitignored
# by agents/.gitignore line `elder-*/.env`). The container's run.sh sources
# /agent/.env, which is mounted from agents/elder-N/.env via env_file in
# compose. This target is interactive (operator pastes OAuth token).
oauth-bootstrap:
	@for n in 1 2 3 4; do $(MAKE) oauth-bootstrap-elder-$$n; done

oauth-bootstrap-%:
	@mkdir -p $(REPO_ROOT)/agents/$*
	@DEST=$(REPO_ROOT)/agents/$*/.env; \
	if [ -s "$$DEST" ] && [ "$(FORCE)" != "1" ]; then \
		echo "[oauth] $$DEST already populated; skipping. Re-run with FORCE=1 to overwrite."; \
		exit 0; \
	fi; \
	echo "[oauth] $* — paste CLAUDE_CODE_OAUTH_TOKEN (input hidden):" >&2; \
	stty -echo 2>/dev/null || true; \
	read token; \
	stty echo 2>/dev/null || true; \
	echo >&2; \
	if [ -z "$$token" ]; then \
		echo "ERROR: empty token" >&2; \
		exit 1; \
	fi; \
	CLAN_ID=$$(echo "$*" | sed 's/elder-//'); \
	{ \
		echo "# Generated by \`make oauth-bootstrap-$*\` — do not commit."; \
		echo "CLAUDE_CODE_OAUTH_TOKEN=$$token"; \
		echo "ELDER_ID=$*"; \
		echo "CLAN_ID=$$CLAN_ID"; \
	} > "$$DEST"; \
	chmod 0600 "$$DEST"; \
	echo "[oauth] $* credentials saved to $$DEST"

# ---------------------------------------------------------------------------
# Host caddy snippet — delegates to bin/install-caddy-snippet.sh (Phase 1.5).
# ---------------------------------------------------------------------------
install-caddy-snippet:
	bash $(REPO_ROOT)/bin/install-caddy-snippet.sh

uninstall-caddy-snippet:
	bash $(REPO_ROOT)/bin/install-caddy-snippet.sh --uninstall

check-caddy-snippet:
	bash $(REPO_ROOT)/bin/install-caddy-snippet.sh --check

# ---------------------------------------------------------------------------
# Build + smoke test
# ---------------------------------------------------------------------------
build: check-profile
	$(DC) --profile $(PROFILE) build

smoke-test:
	bash $(REPO_ROOT)/bin/check-stack-health.sh
