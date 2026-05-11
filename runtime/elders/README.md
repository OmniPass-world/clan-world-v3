# ClanWorld Elders — runtime harness

Source tree for the 4-Elder ClanWorld cluster. Deploy to any machine:

    git clone <repo>
    cd runtime/elders
    make install DEST=$HOME/clan-world
    cd $HOME/clan-world
    make elders-up
    make ttyd-up

## What install does

Materializes `$DEST/` with:
- Shared parent config (`AGENTS.md`, `.claude/settings.json`)
- Shared Claude Code plugin (`plugins/clan-world-elder/`) with skills, hook, CLI wrappers, and MCP server
- Per-elder dirs (`elder-1/` through `elder-4/`) with `run.sh` and credentials symlink
- systemd unit files (`~/.config/systemd/user/ttyd-elder-{1..4}.service`)

Idempotent: re-running safe; never clobbers `.env`, `agent-directive.secret.md`, or `state/`.

## First run after install

Each elder needs one-time OAuth setup (interactive browser dance):

    make -C $HOME/clan-world elder-1   # spawns session, triggers CC OAuth wizard
    # complete browser auth, Ctrl-C when done
    make -C $HOME/clan-world elders-up # now all 4 start cleanly

See ADR 0013 for OAuth-MAX constraint — never use `ANTHROPIC_API_KEY`.

## Runtime tool access

Elders load `plugins/clan-world-elder/` with `claude --plugin-dir`, which starts
the `elder` MCP stdio server from the plugin `.mcp.json`. Use MCP tools for
structured calls, including `post_bulletin` for public bulletin messages.

The Bash fallback is intentionally tiny: exact `date` plus approved `elder`
subcommands. Order JSON for the CLI fallback must be written with Claude
`Write`/`Edit` under `/tmp/elder-N/`, then submitted with
`elder clan submit-orders /tmp/elder-N/orders.json`.

## Shell shortcuts

    source $HOME/clan-world/aliases.sh
    # adds: elder-1..4, elders-up, elders-down, elders-status, elder-up <N>

## Personalities

Each elder's personality is in `personalities/elder-N.md`. Install copies it to
`$DEST/elder-N/.claude/CLAUDE.md` (only on first install — won't overwrite runtime edits).
Archetypes defined in `personalities.yaml`.

## Docker (future)

Issue #151 tracks containerization. This make-based harness is the reference deploy layout.
