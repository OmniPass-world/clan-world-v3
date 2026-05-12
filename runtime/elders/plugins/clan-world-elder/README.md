# ClanWorld Elder Claude Code Plugin

Bundles the shared Elder skills, Bash guard hook, `elder` CLI wrappers, and
the `elder` MCP stdio server.

The runtime harness loads this plugin directly with:

    claude --plugin-dir "$HOME/clan-world/plugins/clan-world-elder"

The MCP server inherits `ELDER_N`, `CLAN_WORLD_REPO`, and runtime paths from the
per-elder `run.sh` environment, so each Elder gets the same plugin bundle with a
different identity.
