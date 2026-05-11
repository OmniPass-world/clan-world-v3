import { describe, expect, it } from 'vitest';
import { spawnSync } from 'node:child_process';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../..');
const bashBin = '/usr/bin/bash';
const guardPath = path.join(
  repoRoot,
  'runtime/elders/plugins/clan-world-elder/hooks/bash-guard.sh',
);

function runGuardInput(input: string, env: NodeJS.ProcessEnv = {}) {
  return spawnSync(bashBin, [guardPath], {
    cwd: repoRoot,
    env: { ...process.env, ...env, ELDER_N: '1' },
    input,
    encoding: 'utf8',
  });
}

function runGuard(command: string, env: NodeJS.ProcessEnv = {}) {
  return runGuardInput(JSON.stringify({ tool_name: 'Bash', tool_input: { command } }), env);
}

describe('Elder bash guard', () => {
  it('blocks when jq is unavailable', () => {
    const binDir = fs.mkdtempSync(path.join(os.tmpdir(), 'elder-no-jq-'));
    try {
      fs.symlinkSync('/usr/bin/cat', path.join(binDir, 'cat'));
      const out = runGuard('date', { PATH: binDir });

      expect(out.status).toBe(2);
      expect(out.stderr).toContain('jq is required for the Elder bash guard');
    } finally {
      fs.rmSync(binDir, { recursive: true, force: true });
    }
  });

  it('blocks malformed hook input', () => {
    const out = runGuardInput('{not json');

    expect(out.status).toBe(2);
    expect(out.stderr).toContain('could not parse hook input');
  });

  it('does not treat --help-anything as a help flag', () => {
    const out = runGuard('elder world snapshot --help-anything');

    expect(out.status).toBe(2);
    expect(out.stderr).toContain('invalid elder world snapshot arguments');
  });

  it('blocks bare shell variable expansion', () => {
    const out = runGuard('elder memory save leak $PRIVATE_KEY');

    expect(out.status).toBe(2);
    expect(out.stderr).toContain('shell variable expansion is not allowed');
  });

  it('blocks numeric dollar references in bulletin content', () => {
    const out = runGuard('elder bulletin post "cost $5 wood"');

    expect(out.status).toBe(2);
    expect(out.stderr).toContain('shell variable expansion is not allowed');
  });

  it('installs the shared Elder plugin instead of standalone hook and MCP files', () => {
    const dest = fs.mkdtempSync(path.join(os.tmpdir(), 'clan-world-elders-'));
    const source = fs.mkdtempSync(path.join(os.tmpdir(), 'clan-world-elder-src-'));
    try {
      fs.mkdirSync(path.join(source, 'parent/.claude'), { recursive: true });
      fs.mkdirSync(path.join(source, 'ttyd'), { recursive: true });
      fs.mkdirSync(path.join(source, 'bin'), { recursive: true });
      fs.mkdirSync(path.join(source, 'template'), { recursive: true });
      fs.mkdirSync(path.join(source, 'personalities'), { recursive: true });
      fs.writeFileSync(path.join(source, 'parent/.claude/CLAUDE.md'), '# Test elder parent\n');
      fs.copyFileSync(
        path.join(repoRoot, 'runtime/elders/parent/.claude/settings.json'),
        path.join(source, 'parent/.claude/settings.json'),
      );
      fs.cpSync(
        path.join(repoRoot, 'runtime/elders/plugins/clan-world-elder'),
        path.join(source, 'plugins/clan-world-elder'),
        { recursive: true },
      );
      fs.writeFileSync(path.join(source, 'ttyd/index.html'), '<!doctype html>\n');
      fs.writeFileSync(path.join(source, 'Makefile'), '# test\n');
      fs.writeFileSync(path.join(source, 'aliases.sh'), '# test\n');
      fs.writeFileSync(path.join(source, 'bin/elder-inject-startup.sh'), '#!/usr/bin/env bash\n');
      fs.writeFileSync(path.join(source, 'template/run.sh.template'), '#!/usr/bin/env bash\n');
      fs.writeFileSync(path.join(source, 'personalities/elder-1.md'), '# Elder 1\n');

      const out = spawnSync('make', [
        'install',
        'ELDER_NUMBERS=1',
        `DEST=${dest}`,
        `REPO_DIR=${source}`,
        `CLAN_WORLD_REPO=${repoRoot}`,
        `SYSTEMD_USER_DIR=${path.join(dest, 'systemd-user')}`,
      ], {
        cwd: path.join(repoRoot, 'runtime/elders'),
        env: process.env,
        encoding: 'utf8',
      });

      expect(out.status, out.stderr || out.stdout).toBe(0);
      const installedGuard = path.join(dest, 'plugins/clan-world-elder/hooks/bash-guard.sh');
      expect(fs.statSync(installedGuard).isFile()).toBe(true);
      expect(fs.statSync(path.join(dest, 'plugins/clan-world-elder/.mcp.json')).isFile()).toBe(true);
      expect(fs.existsSync(path.join(dest, 'elder-1/.mcp.json'))).toBe(false);
      expect(fs.existsSync(path.join(dest, '.claude/hooks'))).toBe(false);
    } finally {
      fs.rmSync(dest, { recursive: true, force: true });
      fs.rmSync(source, { recursive: true, force: true });
    }
  });
});
