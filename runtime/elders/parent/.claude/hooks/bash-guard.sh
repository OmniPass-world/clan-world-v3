#!/usr/bin/env bash
set -euo pipefail

deny() {
  printf 'ClanWorld Elder Bash guard blocked this command: %s\n' "$1" >&2
  printf 'Allowed Bash surface: exact date, or approved elder subcommands. Use Read/Write/Edit for files under /tmp/elder-$ELDER_N/.\n' >&2
  exit 2
}

input="$(cat)"
if ! command -v jq >/dev/null 2>&1; then
  deny "jq is required for the Elder bash guard"
fi

tool_name="$(jq -re '.tool_name' <<<"$input" 2>/dev/null)" \
  || deny "could not parse hook input (missing or invalid .tool_name)"

if [[ "$tool_name" != "Bash" ]]; then
  exit 0
fi

command_text="$(jq -re '.tool_input.command' <<<"$input" 2>/dev/null)" \
  || deny "could not parse hook input (missing or invalid .tool_input.command)"

if [[ -z "$command_text" ]]; then
  deny "missing Bash command in hook input"
fi

if [[ "$command_text" == *$'\n'* || "$command_text" == *$'\r'* ]]; then
  deny "multi-line Bash is not allowed"
fi

for forbidden in '&&' '||' ';;' ';' '|' '>' '<' '`' '$(' '$[' '${' '*' '?' '[' ']' '~'; do
  if [[ "$command_text" == *"$forbidden"* ]]; then
    deny "shell syntax '$forbidden' is not allowed"
  fi
done

if [[ "$command_text" =~ \$[A-Za-z_0-9] ]]; then
  deny "shell variable expansion is not allowed"
fi

argv_json="$(
  python3 - "$command_text" <<'PY'
import json
import shlex
import sys

try:
    print(json.dumps(shlex.split(sys.argv[1])))
except ValueError as exc:
    print(json.dumps({"error": str(exc)}))
    sys.exit(1)
PY
)" || deny "could not parse command arguments"

if jq -e 'type == "object" and has("error")' <<<"$argv_json" >/dev/null; then
  deny "could not parse command arguments"
fi

argc="$(jq 'length' <<<"$argv_json")"
cmd="$(jq -r '.[0] // empty' <<<"$argv_json")"

is_int() {
  [[ "$1" =~ ^[0-9]+$ ]]
}

is_safe_atom() {
  [[ "$1" =~ ^[A-Za-z0-9._:/,@+=-]+$ ]]
}

is_tmp_order_path() {
  local path="$1"
  local elder_n="${ELDER_N:-${ELDER_NUMBER:-}}"
  [[ -n "$elder_n" ]] || return 1
  [[ "$path" =~ ^/tmp/elder-${elder_n}/[A-Za-z0-9._/-]+\.json$ ]] || return 1
  [[ "$path" != *"/../"* && "$path" != *"/.." && "$path" != *".."* ]]
}

arg() {
  jq -r ".[$1] // empty" <<<"$argv_json"
}

case "$cmd" in
  date)
    [[ "$argc" -eq 1 ]] || deny "date does not accept arguments in Elder sessions"
    exit 0
    ;;
  elder)
    ;;
  *)
    deny "only elder and date are allowed"
    ;;
esac

ns="$(arg 1)"
sub="$(arg 2)"

case "$ns" in
  --help|-h|--version|-v)
    [[ "$argc" -eq 2 ]] || deny "invalid elder top-level option"
    exit 0
    ;;
  world)
    [[ "$sub" == "snapshot" ]] || deny "only elder world snapshot is allowed"
    [[ "$argc" -eq 3 || ( "$argc" -eq 4 && "$(arg 3)" =~ ^(--help|-h)$ ) ]] || deny "invalid elder world snapshot arguments"
    ;;
  clan)
    case "$sub" in
      view)
        [[ "$argc" -eq 4 ]] || deny "invalid elder clan view arguments"
        third="$(arg 3)"
        if [[ "$third" != "--help" && "$third" != "-h" ]]; then
          is_int "$third" || deny "clan id must be numeric"
          [[ -z "${ELDER_N:-}" || "$third" == "$ELDER_N" ]] || deny "Elders may only view their own clan via Bash"
        fi
        ;;
      submit-orders)
        [[ "$argc" -eq 4 ]] || deny "invalid elder clan submit-orders arguments"
        third="$(arg 3)"
        if [[ "$third" != "--help" && "$third" != "-h" ]]; then
          is_tmp_order_path "$third" || deny "orders file must be /tmp/elder-$ELDER_N/*.json"
        fi
        ;;
      --help|-h)
        [[ "$argc" -eq 3 ]] || deny "invalid elder clan help arguments"
        ;;
      *)
        deny "unsupported elder clan subcommand"
        ;;
    esac
    ;;
  memory)
    case "$sub" in
      recall)
        [[ "$argc" -eq 4 ]] || deny "invalid elder memory recall arguments"
        third="$(arg 3)"
        [[ "$third" =~ ^(--help|-h)$ ]] || is_safe_atom "$third" || deny "memory key contains unsafe characters"
        ;;
      save)
        [[ "$argc" -ge 5 || ( "$argc" -eq 4 && "$(arg 3)" =~ ^(--help|-h)$ ) ]] || deny "invalid elder memory save arguments"
        third="$(arg 3)"
        [[ "$third" =~ ^(--help|-h)$ ]] || is_safe_atom "$third" || deny "memory key contains unsafe characters"
        ;;
      --help|-h)
        [[ "$argc" -eq 3 ]] || deny "invalid elder memory help arguments"
        ;;
      *)
        deny "unsupported elder memory subcommand"
        ;;
    esac
    ;;
  peer)
    case "$sub" in
      inbox)
        [[ "$argc" -eq 3 || ( "$argc" -eq 4 && "$(arg 3)" =~ ^(--help|-h)$ ) ]] || deny "invalid elder peer inbox arguments"
        ;;
      whisper)
        [[ "$argc" -ge 5 || ( "$argc" -eq 4 && "$(arg 3)" =~ ^(--help|-h)$ ) ]] || deny "invalid elder peer whisper arguments"
        third="$(arg 3)"
        [[ "$third" =~ ^(--help|-h)$ ]] || is_int "$third" || deny "peer whisper recipient must be numeric"
        ;;
      --help|-h)
        [[ "$argc" -eq 3 ]] || deny "invalid elder peer help arguments"
        ;;
      *)
        deny "unsupported elder peer subcommand"
        ;;
    esac
    ;;
  bulletin)
    case "$sub" in
      post)
        [[ "$argc" -ge 4 ]] || deny "invalid elder bulletin post arguments"
        ;;
      --help|-h)
        [[ "$argc" -eq 3 ]] || deny "invalid elder bulletin help arguments"
        ;;
      *)
        deny "unsupported elder bulletin subcommand"
        ;;
    esac
    ;;
  ack-clear|rules)
    [[ "$argc" -eq 2 || ( "$argc" -eq 3 && "$(arg 2)" =~ ^(--help|-h)$ ) ]] || deny "invalid elder $ns arguments"
    ;;
  *)
    deny "unsupported elder command"
    ;;
esac

exit 0
