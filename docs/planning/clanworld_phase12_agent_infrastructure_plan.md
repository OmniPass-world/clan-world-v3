# ClanWorld Phase 12 — Agent Infrastructure Rework Plan

**Issue:** #282  
**Status:** Draft  
**Scope:** Runtime harness at `/home/claude/clan-world/` — NOT the code repo at `/home/claude/code/clan-world/`

---

## Background

Current layout uses per-elder `CLAUDE_CONFIG_DIR` pointing to `elder-N/.claude/`. This gives full isolation per elder (credentials, session state, memory, transcripts) but forces duplication of any shared config. The rework proposes collapsing the config dir to a single shared location — **however, `CLAUDE_CONFIG_DIR` relocates the entire `~/.claude` tree, not just OAuth credentials**. This means shared `CLAUDE_CONFIG_DIR` would also share session history, memory, and transcripts across elders. Whether to proceed with this approach, keep per-elder isolation, or use a hybrid is an **open decision (see Section 7 DECISION NEEDED block)**. Per-elder identity would move to env vars; per-elder writable skill directories would live at `elder-N/.claude/skills/`. Parent-walk discovery handles config union automatically.

---

## 1. Migration Steps

Execute in order. Do NOT skip the backup step.

1. **Backup current layout**
   - **Quiesce first:** stop all running elder sessions before copying (kill tmux panes or stop systemd services — a live write during backup can corrupt the copy).
   ```bash
   # Stop all elder sessions, then back up:
   cp -rP /home/claude/clan-world/ /home/claude/clan-world.bak-phase12/
   ```
   Use `cp -rP` (not `cp -r`) to preserve symlinks faithfully — `-P` ensures symlinks are copied as symlinks, not dereferenced.

   Keep until all 4 elders are confirmed booting in new layout.

   Explicit restore command if rollback needed:
   ```bash
   rm -rf /home/claude/clan-world/agents && cp -rP /home/claude/clan-world.bak-phase12/. /home/claude/clan-world/
   ```
   > ⚠️ Verify the target path before running the restore — double-check `/home/claude/clan-world/` is the correct destination.

2. **Create shared config dir**
   ```
   mkdir -p /home/claude/clan-world/agents/.claude/skills
   mkdir -p /home/claude/clan-world/agents/.claude/hooks
   ```

3. **Move shared AGENTS.md and CLAUDE.md symlink**
   - Copy `/home/claude/clan-world/AGENTS.md` → `/home/claude/clan-world/agents/.claude/CLAUDE.md` (Claude Code discovers `CLAUDE.md`, not `AGENTS.md`, via parent-walk)
   - Retain `/home/claude/clan-world/AGENTS.md` at root and ensure the `CLAUDE.md` symlink pointing to it also remains — parent-walk discovers `CLAUDE.md` (the symlink), not `AGENTS.md` directly. Do NOT remove the symlink.

4. **Create shared settings.json at agents/.claude/**
   - Write `/home/claude/clan-world/agents/.claude/settings.json` with all shared env vars (see Section 3)
   - This replaces the minimal `{"theme":"dark","skipDangerousModePermissionPrompt":true}` currently in each per-elder `.claude/settings.json`

5. **Move any shared skills into agents/.claude/skills/**
   - If skills exist at `/home/claude/clan-world/elder-N/.claude/skills/` (currently empty), relocate to `/home/claude/clan-world/agents/.claude/skills/`
   - Add `/skill-creator` stub (see Section 5)

6. **Create per-elder directory structure**
   For each N in {1, 2, 3, 4}:
   ```
   mkdir -p /home/claude/clan-world/agents/elder-N/.claude/skills
   mkdir -p /home/claude/clan-world/agents/elder-N/state/plugin-cache
   mkdir -p /home/claude/clan-world/agents/elder-N/state/outbox
   ```

7. **Write per-elder settings.json (env vars + skills pointer only)**
   `/home/claude/clan-world/agents/elder-N/.claude/settings.json` — see Section 3 for exact keys.

8. **Migrate per-elder CLAUDE.md personality overlay**
   - Copy `/home/claude/clan-world/elder-N/.claude/CLAUDE.md` → `/home/claude/clan-world/agents/elder-N/.claude/CLAUDE.md`
   - These are the personality/archetype files. Keep verbatim — content does not change.

9. **Migrate per-elder agent-directive.secret.md**
   - Copy `/home/claude/clan-world/elder-N/agent-directive.secret.md` → `/home/claude/clan-world/agents/elder-N/agent-directive.secret.md`

10. **Update elder-1 run.sh only** (do NOT update elders 2–4 yet)
    - Change `CLAUDE_CONFIG_DIR` from `"$AGENT_DIR/.claude"` to `/home/claude/clan-world/agents/.claude`
    - Update `AGENT_DIR` to resolve to new path: `/home/claude/clan-world/agents/elder-1`
    - Update `PATH` injection to still point at `/home/claude/code/omnipass-world/clan-world/packages/agents/bin`
    - Remove elder-1 `.credentials.json` symlink logic (credentials now live in shared config dir — single symlink there)
    - Update `mkdir -p` paths to new state dirs

11. **Consolidate .credentials.json symlink for elder-1**
    - Remove elder-1 symlink at `/home/claude/clan-world/elder-1/.claude/.credentials.json`
    - Create single symlink: `/home/claude/clan-world/agents/.claude/.credentials.json` → `/home/claude/.claude/.credentials.json`

12. **GATE: Verify elder-1 in new layout — STOP if any test plan step 1–5 fails**
    - Run elder-1 in new layout, confirm both `agents/.claude/settings.json` and `agents/elder-1/.claude/settings.json` are loaded
    - Execute test plan steps 1–5 (Section 9): boot, env checks, auto-memory, shared CLAUDE.md, per-elder personality
    - **Do NOT proceed to step 13 until all gate steps pass**
    - See full test plan (Section 9) for exact verification steps

13. **Migrate elders 2, 3, 4** (only after elder-1 gate passes):
    - Repeat steps 10–11 for each of elder-2, elder-3, elder-4
    - Remove per-elder symlinks at `/home/claude/clan-world/elder-N/.claude/.credentials.json` (for N=2,3,4)
    - The single shared symlink at `agents/.claude/.credentials.json` already covers all 4

14. **Update Makefile** at `/home/claude/clan-world/Makefile` — elder targets reference old paths.

15. **Delete old layout** (after all 4 confirmed):
    - Remove `/home/claude/clan-world/elder-N/` directories (the originals, not the new `agents/elder-N/`)

---

## 2. File-level Changes Table

| File | Action | Notes |
|------|--------|-------|
| `/home/claude/clan-world/AGENTS.md` | Keep in place | Canonical instruction file; NOT directly discovered by parent-walk (Claude Code looks for `CLAUDE.md`, not `AGENTS.md`) |
| `/home/claude/clan-world/CLAUDE.md` | Keep in place | **Symlink to AGENTS.md** — parent-walk discovers `CLAUDE.md` (via this symlink), not `AGENTS.md` directly. The symlink must remain intact for root-level discovery to work. |
| `/home/claude/clan-world/agents/.claude/CLAUDE.md` | Create new | Copy of AGENTS.md content for discovery inside agents/.claude/ scope |
| `/home/claude/clan-world/agents/.claude/settings.json` | Create new | All shared env vars (see Section 3); replaces per-elder duplication |
| `/home/claude/clan-world/agents/.claude/.credentials.json` | Create new symlink | Points to `/home/claude/.claude/.credentials.json`; single source of truth |
| `/home/claude/clan-world/agents/.claude/skills/` | Create dir | Shared skills visible to all elders |
| `/home/claude/clan-world/agents/.claude/skills/skill-creator/SKILL.md` | Create new | Scoped skill-creator (see Section 5) |
| `/home/claude/clan-world/agents/elder-N/.claude/settings.json` | Create new (×4) | Env vars only: ELDER_NAME, ELDER_NUMBER, skills path pointer |
| `/home/claude/clan-world/agents/elder-N/.claude/CLAUDE.md` | Move (×4) | Personality overlay; content unchanged from `/home/claude/clan-world/elder-N/.claude/CLAUDE.md` |
| `/home/claude/clan-world/agents/elder-N/.claude/skills/` | Create dir (×4) | Per-elder writable skills; NOT shared |
| `/home/claude/clan-world/agents/elder-N/agent-directive.secret.md` | Move (×4) | From old `elder-N/` path |
| `/home/claude/clan-world/agents/elder-N/run.sh` | Rewrite (×4) | CLAUDE_CONFIG_DIR → shared; AGENT_DIR → new path; remove per-elder credentials symlink |
| `/home/claude/clan-world/agents/elder-N/state/` | Create dir (×4) | plugin-cache, outbox subdirs |
| `/home/claude/clan-world/elder-N/.claude/.credentials.json` | Delete (×4) | Replaced by single shared symlink |
| `/home/claude/clan-world/Makefile` | Update | Elder target paths change from `elder-N/` to `agents/elder-N/` |
| `/home/claude/clan-world/bin/` | Keep | Shared CLI helpers; path unchanged |

---

## 3. Env Var Verification

### Placement key

- **Shared** = `/home/claude/clan-world/agents/.claude/settings.json` (`env` block)
- **Per-elder** = `/home/claude/clan-world/agents/elder-N/.claude/settings.json` (`env` block)
- **run.sh export** = explicit `export VAR=value` in run.sh before `exec claude`

| Env Var / Key | What it does | Placement | Status |
|---------------|-------------|-----------|--------|
| `CLAUDE_CODE_DISABLE_AUTO_MEMORY=1` | Disables Claude Code's auto memory system — prevents the agent from writing unsolicited memory entries | Shared settings.json `env` block | Verified in use |
| `autoMemory: false` | settings.json key with same effect as above — belt+suspenders redundancy | Shared settings.json top-level key | Verified in use |
| `claudeMdExcludes: ["/home/claude/.claude/*"]` | Excludes host-level CLAUDE.md from elder context — prevents PM/orchestrator instructions from leaking into elder sessions | Shared settings.json top-level key | Verified in use |
| `CLAUDE_CODE_DISABLE_POLICY_SKILLS=1` | Skips Anthropic policy skills injection into session context | Shared settings.json `env` block | Verified in use |
| `CLAUDE_CODE_HIDE_ACCOUNT_INFO=1` | Suppresses account/auth display in Claude Code header | run.sh export | Verified — already in current run.sh |
| `ENABLE_CLAUDEAI_MCP_SERVERS=false` | Disables claude.ai MCP servers — prevents external MCP connections elder sessions don't need | Shared settings.json `env` block | Verified in use |
| `IS_DEMO=1` | Demo mode flag — modifies elder CLI or game logic behavior in demo scenarios | Per-elder settings.json `env` block (varies by elder) OR run.sh | Verified flag exists; placement TBD per-elder vs shared |
| `CLAUDE_CODE_PLUGIN_CACHE_DIR` | Isolates plugin cache per elder — prevents cross-session plugin state contamination | run.sh export (`$AGENT_DIR/state/plugin-cache`) | Verified — already in current run.sh |

> ⚠️ UNVERIFIED: `CLAUDE_CODE_HIDE_CWD` — this var name does not appear in the current run.sh or any discovered settings.json. It may not be a real Claude Code env var, or it may be named differently. Verify against `claude --help` or Claude Code source before adding to any config.

> ⚠️ UNVERIFIED: `CLAUDE_CODE_NO_FLICKER` — same as above. Does not appear in current elder config. Likely non-standard. Do not add until verified against the current claude CLI version.

### Per-elder identity vars (run.sh exports, not settings.json)

These are elder-specific and must remain in run.sh because settings.json `env` blocks don't easily support per-instance overrides without per-file duplication:

- `ELDER_NUMBER` — integer (1–4)
- `ELDER_N` — same value, alias for elder CLI subcommands
- `AGENT_NAME` — `"elder-N"`
- `AGENT_ROLE` — clan-specific string (e.g., `"ClanWorld Elder (clan 1)"`)
- `ANTHROPIC_MODEL` — defaults to `claude-sonnet-4-6`; override via env for Opus
- `TZ` — `"America/New_York"`

---

## 4. --output-format Experiment

### Proposed A/B split

- **Group A** (elders 1 and 3): launch with `--output-format <value>` flag
- **Group B** (elders 2 and 4): launch with no flag (current default behavior)

> ⚠️ DECISION NEEDED: verify the exact `--output-format` value that strips the coding system prompt. Current candidates are `stream-json` and a custom value. Run `claude --help` and `claude --output-format --help` to enumerate valid values before adding to any run.sh. Do NOT add an unverified flag — an invalid value will cause elders to fail to start.

> ⚠️ UNVERIFIED: Verify exact `--output-format` value that removes coding system prompt artifacts. Known values include `stream-json`, `json`, `text` — none are documented as removing the coding prompt specifically.

### How to observe behavior delta

Both groups write all responses to `state/outbox/` already (outbox dir created in migration step 6). Add elder-id prefix to filenames at write time so logs don't collide when two elders are running:

> Note: the current `run.sh` creates `state/outbox/` dirs but does not write experiment logs automatically. Logging must be added to the run.sh or elder CLAUDE.md instructions — add this to implementation scope.

- Naming convention: `state/outbox/elder-N_tick-NNNN_TIMESTAMP.jsonl`

Compare across groups:
1. **Response length** — token count per tick response (Group A expected shorter if coding prompt removed)
2. **Code-block frequency** — count of triple-backtick blocks per tick response
3. **Strategic narrative depth** — qualitative: does Group A produce more clan-voice prose and fewer implementation details?

### What to measure

Collect a minimum of 10 ticks per elder before drawing conclusions. The coding system prompt removal hypothesis is that elder responses shift from tool-use + code-heavy to narrative + order-submission only.

---

## 5. Scoped /skill-creator for Elders

### Design

Elders can write new skills during a session. Skills must land in the elder's own `.claude/skills/` directory — NOT in the shared `agents/.claude/skills/` dir (which is orchestrator-managed).

- **Skill location (shared, read by all elders):** `/home/claude/clan-world/agents/.claude/skills/skill-creator/SKILL.md`
- **Output target (per-elder, writable):** `./.claude/skills/` relative to the elder's cwd (`/home/claude/clan-world/agents/elder-N/.claude/skills/`)

### SKILL.md stub outline

```markdown
# skill-creator

Writes a new skill to this elder's personal skills directory.

## Trigger
User or runner invokes `/skill-creator <skill-name>`.

## Steps
1. Identify skill name and purpose from invocation args.
2. Write skill file to `./.claude/skills/<skill-name>/SKILL.md` (relative to cwd).
   - CRITICAL: path MUST be relative to cwd (`agents/elder-N/`). Do NOT write to shared `agents/.claude/skills/`.
   - Do NOT write to any path outside `./.claude/skills/`.
3. Confirm write by reading back the file.
4. Report skill path and one-line summary.

## Constraints
- Output dir: `./.claude/skills/` only (relative to cwd). Hard constraint — not negotiable.
- No external network calls.
- Skill content must follow SKILL.md format used in the host harness.
```

The skill prompt must enforce the relative path constraint explicitly because a misconfigured write to `agents/.claude/skills/` would affect all elders and is orchestrator-only territory.

---

## 6. Skill Dependency Tree (Issues #275–#281)

| Skill | Issue | Depends on | Can draft now? |
|-------|-------|-----------|----------------|
| `elder-base-context` | #275 | Nothing — foundational boot context skill | Yes |
| `cleared-context-start` | #276 | `elder-base-context` (#275) — must boot before cleared-context resume | Yes (after #275) |
| `final-tick-continuity` | #277 | `elder-base-context` (#275), `elder memory save` CLI command | Yes |
| `uniswap-market-overview` | #278 | `elder world snapshot` + Unicorn Town indexer getters (Phase 4 complete) | Yes |
| `uniswap-sell-immediate` | #279 | `uniswap-market-overview` (#278), `elder clan submit-orders` | Yes (after #278) |
| `uniswap-sell-scheduled` | #280 | `uniswap-sell-immediate` (#279), market scheduler in Phase 5 | Stub only — Phase 5 not shipped |
| `uniswap-arb-camping` | #281 | `uniswap-market-overview` (#278), 0G adapter (Phase 7 stub exists), AXL whisper (Phase 8 stub exists) | Stub only — 0G/AXL stubs exist but not production |

Note: 0G adapter = Phase 7 (stub exists, not production). AXL whisper = Phase 8 (stub exists, not production). Skills #280 and #281 should be drafted as stubs with clear `> ⚠️ STUB: depends on Phase N` callouts at the top so elders know not to invoke them in production ticks.

---

## 7. Parent-Walk Behavior Analysis

> ⚠️ DECISION NEEDED: **`CLAUDE_CONFIG_DIR` relocates the entire `~/.claude` tree**, not just OAuth credentials. This includes settings, session history, memory, plugins, transcripts, and skill state. Sharing `CLAUDE_CONFIG_DIR` across all 4 elders means sharing ALL that state — sessions, memory, and transcripts would not be isolated per elder. This may invalidate the core design premise.
>
> Options to evaluate:
> 1. **Keep per-elder `CLAUDE_CONFIG_DIR`** (current approach) — full isolation, but duplication of shared config. Shared config must be maintained 4× or symlinked.
> 2. **Shared `CLAUDE_CONFIG_DIR` with explicit per-elder overrides** — risks session/memory cross-contamination unless Claude Code supports sub-scoping within a shared config dir.
> 3. **Hybrid** — per-elder `CLAUDE_CONFIG_DIR` pointing to per-elder dirs, but each per-elder dir symlinks shared assets (skills/, CLAUDE.md, settings base).
>
> **Liam must decide before implementation proceeds.**

### Discovery path with proposed layout

Claude Code walks upward from the process cwd at launch. With cwd = `/home/claude/clan-world/agents/elder-1`:

```
/home/claude/clan-world/agents/elder-1/.claude/    ← discovered (per-elder: CLAUDE.md, settings.json, skills/)
/home/claude/clan-world/agents/.claude/             ← discovered (shared: CLAUDE.md, settings.json, skills/)
/home/claude/clan-world/.claude/                    ← discovered IF EXISTS (currently AGENTS.md + CLAUDE.md symlink at root, not inside .claude/)
/home/claude/.claude/                               ← host scope (orchestrator config — EXCLUDED via claudeMdExcludes)
```

Both per-elder and shared configs are discovered and **merged** — this is the desired union behavior. The per-elder `.claude/settings.json` wins on any conflicting key (closer to cwd = higher precedence).

> ⚠️ IMPORTANT: The live root at `/home/claude/clan-world/` currently has a `.claude/` directory that is already being discovered via parent-walk. Before migration, inventory its contents (`settings.json`, `skills/` entries). These will CONTINUE to be discovered by elder sessions after migration (since cwd will be `~/clan-world/agents/elder-N/` which walks up through `~/clan-world/agents/` and `~/clan-world/` — the root `.claude/` is still on the walk path). Either migrate/remove it or explicitly account for it layering on top of the new shared config.

### CLAUDE_CONFIG_DIR is NOT narrow OAuth-only

`CLAUDE_CONFIG_DIR` relocates the ENTIRE `~/.claude` tree — credentials, session state, plugin cache, settings, memory, transcripts, and skill state. It does NOT affect which `settings.json` or `CLAUDE.md` files are discovered via parent-walk (those are separate mechanisms). The proposed change (CLAUDE_CONFIG_DIR → shared) is a PROPOSED OPTION pending verification — see the DECISION NEEDED block above.

### Conflict resolution

| Scenario | Resolution |
|----------|------------|
| Per-elder `settings.json` has key X; shared `settings.json` also has key X | Per-elder wins (closer to cwd) |
| Shared `settings.json` has key X; per-elder does not | Shared value used |
| Per-elder `CLAUDE.md` and shared `CLAUDE.md` both exist | Both loaded; per-elder appended after shared (Claude Code concatenates in walk order) |

### Design constraint

Keep per-elder `settings.json` minimal: only `ELDER_NUMBER`, `ELDER_NAME`, `ELDER_N`, `AGENT_ROLE`, and a `skills` workspace pointer. All shared config (env vars, excludes, auto-memory flags, model config) belongs in `agents/.claude/settings.json`. This ensures there are no key conflicts and the merge behavior is predictable.

---

## 8. Plugin and Credential Isolation

### OAuth token sharing

> Note: the framing below describes the **shared `CLAUDE_CONFIG_DIR` option** (Option 2 from Section 7). If Liam chooses Option 1 (keep per-elder) or Option 3 (hybrid), credential handling stays as-is and this section applies only to the shared symlink pattern.

If shared `CLAUDE_CONFIG_DIR` is adopted — all 4 elders pointing to `/home/claude/clan-world/agents/.claude/`:

- All 4 elders read `.credentials.json` from the same location
- Token refresh writes back to the same file — last-writer-wins if two elders refresh simultaneously
- This is acceptable: all elders share Liam's single MAX subscription; the token is the same for all

> ⚠️ UNVERIFIED: whether simultaneous token refresh from two elder processes causes a race condition on `.credentials.json`. If elders all start within the same minute, test with 2 concurrent elders before assuming it is safe.

Mitigation options (pick one before implementation):

a. **Read-only token via env var** — set `CLAUDE_CODE_OAUTH_TOKEN` env var (already in run.sh via .env.local); if this env var is set, Claude Code uses it directly and does not write to `.credentials.json`. Verify this prevents file writes entirely.

b. **flock on credential file** — wrap any credential-refresh-triggering operations with `flock /home/claude/clan-world/agents/.claude/.credentials.json <command>`.

c. **Per-elder credential files** — keep `.credentials.json` per-elder via symlinks into a shared source, same as current approach.

> ⚠️ DECISION NEEDED: Verify whether `CLAUDE_CODE_OAUTH_TOKEN` env var suppresses all `.credentials.json` writes. If yes, option (a) is simplest.

### Credentials file consolidation

- Remove 4 per-elder symlinks at `/home/claude/clan-world/elder-N/.claude/.credentials.json`
- Create single symlink: `/home/claude/clan-world/agents/.claude/.credentials.json` → `/home/claude/.claude/.credentials.json`
- All elders then read through the same symlink → same token → consistent auth

### Plugin cache isolation (KEEP per-elder)

Even though `CLAUDE_CONFIG_DIR` becomes shared, `CLAUDE_CODE_PLUGIN_CACHE_DIR` must remain per-elder:

```bash
export CLAUDE_CODE_PLUGIN_CACHE_DIR="$AGENT_DIR/state/plugin-cache"
```

- Path: `/home/claude/clan-world/agents/elder-N/state/plugin-cache`
- Reason: plugin pollers that write state to a shared cache directory can cross-kill each other

### Telegram plugin

Elders do not use the Telegram plugin (no inbound Telegram channel for game sessions). `TELEGRAM_STATE_DIR` can be removed from elder run.sh to reduce noise.

---

## 9. Test Plan

Execute in order. Stop at any failing step — do not proceed to the next elder until the current one passes.

1. **Elder-1 boot solo** — run `/home/claude/clan-world/agents/elder-1/run.sh`, confirm Claude Code prompt appears without errors.

2. **ELDER_NAME env check** — inside the elder session, run `echo $AGENT_NAME`; confirm output is `elder-1`. Run `echo $ELDER_NUMBER`; confirm `1`.

3. **Auto-memory disabled** — inside session, run `echo $CLAUDE_CODE_DISABLE_AUTO_MEMORY`; confirm output is `1`.

4. **Shared CLAUDE.md loaded** — ask the elder "what are you?"; confirm the response references shared base context (ClanWorld Elder role, world mechanics) without orchestrator/PM instructions. Shared `CLAUDE.md` is discovered; host `.claude/CLAUDE.md` is excluded via `claudeMdExcludes`.

5. **Per-elder personality loaded** — confirm response includes clan-specific content (Storm Riders archetype for elder-1). This comes from `agents/elder-1/.claude/CLAUDE.md`.

6. **skill-creator scoped write** — invoke `/skill-creator test-skill` inside elder-1. Verify the new skill file lands at `/home/claude/clan-world/agents/elder-1/.claude/skills/test-skill/SKILL.md`. Verify it does NOT appear at `/home/claude/clan-world/agents/.claude/skills/`.

7. **Concurrent elder isolation** — boot elder-1 and elder-2 simultaneously (two terminal sessions). Confirm:
   - Each responds with its own clan identity (`AGENT_NAME` values differ)
   - No cross-contamination: elder-1 does not see elder-2's state outbox files
   - Plugin cache dirs are separate (`state/plugin-cache` in each elder's own dir)

8. **--output-format flag (Group A test)** — boot elder-1 with the verified `--output-format` flag. Issue a tick response prompt.

   Pass criterion: elder-1 (with `--output-format` experiment flag) produces ≥2 consecutive responses that contain NO markdown code blocks (``` fences) when given a purely strategic prompt (e.g., "what resources should we prioritize this tick?"). Elder-2 (without flag) produces ≥1 code block in same scenario. Log to `state/outbox/` for comparison.

9. **Credentials symlink** — confirm `/home/claude/clan-world/agents/.claude/.credentials.json` resolves to `/home/claude/.claude/.credentials.json` and elder-1 authenticates without the interactive OAuth wizard.

10. **Rollback smoke test** — stop elder-1, restore `/home/claude/clan-world.bak-phase12/elder-1/` as the active layout, confirm elder-1 still boots on the old config. This validates the backup is usable before deleting the old layout.
    > ⚠️ If migration step 11 already ran (per-elder `.credentials.json` symlink removed), the restored backup dir will reference a now-deleted symlink path. Re-create it manually: `ln -sf /home/claude/.claude/.credentials.json /home/claude/clan-world/elder-1/.claude/.credentials.json`. Rollback must include this symlink repair or elder-1 will fail OAuth on the old config.

---

## 10. Risk Register

| Risk | Prob | Impact | Mitigation | Rollback |
|------|------|--------|-----------|---------|
| Shared `CLAUDE_CONFIG_DIR` breaks per-elder OAuth refresh (race on `.credentials.json`) | Med | High | Use Section 8 options a/b/c (OAUTH_TOKEN env var suppresses writes; flock; or keep per-elder files). Do not rely on stagger-by-5s alone. Test 2 concurrent elders before running all 4. | Revert `CLAUDE_CONFIG_DIR` to per-elder in each run.sh; re-create per-elder `.credentials.json` symlinks |
| Parent-walk does not union `settings.json` correctly (per-elder overrides too aggressively) | Med | High | Test elder-1 alone first; inspect loaded config with `claude config list` or equivalent; verify shared env vars are active | Revert directory layout for that elder; debug settings merge before migrating others |
| Per-elder `skills/` directory not auto-discovered by Claude Code | Low | Med | Run `/skill-creator` test (step 6 of test plan); skills are discovered by convention (`skills/<name>/SKILL.md`) relative to `.claude/` dirs on parent-walk path — no `skillsDirectory` settings key exists | Symlink `agents/elder-N/.claude/skills/` into `agents/.claude/skills/` under a namespaced subdirectory |
| `--output-format` flag unsupported in current claude CLI version | Med | Low | Run `claude --help` before adding to run.sh; if not listed, omit; the A/B experiment degrades gracefully | Remove flag from Group A (elder-1 and elder-3) run.sh |
| Plugin pollers cross-kill when sharing config dir | Low | High | Keep `CLAUDE_CODE_PLUGIN_CACHE_DIR` per-elder regardless of `CLAUDE_CONFIG_DIR` change; test concurrent boot (step 7 of test plan) | Already mitigated by per-elder plugin cache — if issues persist, investigate plugin-specific state files in the shared dir |
| Makefile elder targets break after path migration | Med | Low | Update Makefile paths as part of migration step 14; test `make elder-1` before declaring migration done | Revert Makefile to old paths; old paths still exist in backup |
| Old `elder-N/` dirs deleted before backup verified | Low | Critical | Backup step is step 1; old dirs deleted only after ALL 4 elders pass test plan | Restore from `/home/claude/clan-world.bak-phase12/` |
| `CLAUDE_CODE_HIDE_CWD` / `CLAUDE_CODE_NO_FLICKER` added but invalid | Med | Low | Do not add unverified var names to any config; verify each against claude CLI before including | Remove unknown vars from settings.json; no restart needed |
| `claudeMdExcludes` glob fails silently | Low | High | Test exclusion on elder-1 boot: confirm host-level `/home/claude/.claude/CLAUDE.md` is NOT echoed in elder context | Remove the exclude entry and use `CLAUDE_CODE_DISABLE_AUTO_MEMORY=1` + personality-only per-elder CLAUDE.md to limit scope |

---

*Plan authored: 2026-04-30. Review: Codex (2 rounds) + Gemini (2 rounds) — CLEAN.*
