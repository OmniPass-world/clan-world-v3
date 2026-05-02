#!/usr/bin/env bash
set -euo pipefail

forge_bin="${FOUNDRY_FORGE:-}"
if [ -z "$forge_bin" ]; then
  if command -v forge >/dev/null 2>&1; then
    forge_bin="$(command -v forge)"
  elif [ -x "$HOME/.foundry/bin/forge" ]; then
    forge_bin="$HOME/.foundry/bin/forge"
  fi
fi

if [ -z "$forge_bin" ] || [ ! -x "$forge_bin" ]; then
  echo "forge not installed; install Foundry or set FOUNDRY_FORGE" >&2
  exit 1
fi

exec "$forge_bin" "$@"
