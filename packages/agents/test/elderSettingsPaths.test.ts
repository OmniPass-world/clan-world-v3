import { describe, expect, it } from 'vitest';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

// v2.5.1 hotfix regression guard.
//
// # The bug
//
// PR #237 ("Fix Elder runtime permission paths") introduced a typo in
// runtime/elders/parent/.claude/settings.json: every absolute path inside
// Read(...), Write(...), Edit(...), and Bash(...) entries got a DOUBLE
// leading slash (e.g. "Read(//tmp/elder-1/foo)", "Read(//home/...)").
//
// Claude Code's permission matcher treats the path-inside-parens as a
// literal glob, so "//tmp/..." does NOT match the runtime path "/tmp/...".
// Effect:
//
//   - Allow rules for "Read(//tmp/elder-N/...)" silently never matched
//   - DENY rules for credentials.json, .env, agent-directive secrets,
//     .claude/ tree etc. silently never matched
//
// This is a security boundary regression — every deny rule was a no-op.
// Caught by the v2.5.0 super-swarm review (codex 5.5).
//
// # The fix
//
// Every absolute path inside a permission entry must start with EXACTLY
// one slash. This test scans runtime/elders for any settings.json or
// settings.local.json file (parent + any per-elder copies) and fails if
// any allow/deny entry starts with "Read(//", "Write(//", "Edit(//", or
// "Bash(//".

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../..');
const elderRoot = path.join(repoRoot, 'runtime/elders');

interface SettingsBlock {
  permissions?: {
    allow?: unknown;
    deny?: unknown;
  };
}

function findSettingsFiles(root: string): string[] {
  const out: string[] = [];
  if (!fs.existsSync(root)) return out;
  const stack: string[] = [root];
  while (stack.length > 0) {
    const dir = stack.pop()!;
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) {
        // Skip node_modules + state caches.
        if (entry.name === 'node_modules' || entry.name === 'state') continue;
        stack.push(full);
      } else if (
        entry.isFile() &&
        (entry.name === 'settings.json' || entry.name === 'settings.local.json')
      ) {
        out.push(full);
      }
    }
  }
  return out;
}

const DOUBLE_SLASH_PERMISSION = /^(Read|Write|Edit|Bash)\(\/\//;

describe('Elder settings.json permission paths', () => {
  it('finds at least one source-of-truth settings.json under runtime/elders', () => {
    const files = findSettingsFiles(elderRoot);
    expect(files.length, `expected to find at least one settings.json under ${elderRoot}`).toBeGreaterThan(0);
  });

  it('no permission entry uses a double-slash path prefix', () => {
    const files = findSettingsFiles(elderRoot);
    const offenders: string[] = [];

    for (const file of files) {
      const raw = fs.readFileSync(file, 'utf8');
      let parsed: SettingsBlock;
      try {
        parsed = JSON.parse(raw) as SettingsBlock;
      } catch (err) {
        throw new Error(`Failed to parse JSON in ${file}: ${(err as Error).message}`);
      }
      const allow = Array.isArray(parsed.permissions?.allow) ? parsed.permissions!.allow : [];
      const deny = Array.isArray(parsed.permissions?.deny) ? parsed.permissions!.deny : [];

      const checkBucket = (bucket: string, entries: unknown[]) => {
        for (const entry of entries) {
          if (typeof entry !== 'string') continue;
          if (DOUBLE_SLASH_PERMISSION.test(entry)) {
            offenders.push(`${file} [${bucket}]: ${entry}`);
          }
        }
      };

      checkBucket('allow', allow);
      checkBucket('deny', deny);
    }

    expect(
      offenders,
      'Permission paths must start with EXACTLY one slash. Double-slash makes the ' +
        'matcher treat them as literal globs that never match real paths — silently ' +
        'breaking allow AND deny rules.\n\n' +
        'Offenders:\n  ' +
        offenders.join('\n  '),
    ).toEqual([]);
  });
});
