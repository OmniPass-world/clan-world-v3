import { describe, expect, it } from 'vitest';
import { spawnSync } from 'node:child_process';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../..');
const guardPath = path.join(repoRoot, 'runtime/elders/parent/.claude/hooks/bash-guard.sh');

function runGuard(command: string) {
  return spawnSync('bash', [guardPath], {
    cwd: repoRoot,
    env: { ...process.env, ELDER_N: '1' },
    input: JSON.stringify({ tool_name: 'Bash', tool_input: { command } }),
    encoding: 'utf8',
  });
}

describe('Elder bash guard', () => {
  it('blocks bare shell variable expansion', () => {
    const out = runGuard('elder memory save leak $PRIVATE_KEY');

    expect(out.status).toBe(2);
    expect(out.stderr).toContain('shell variable expansion is not allowed');
  });

  it('allows a literal dollar sign that is not a variable reference', () => {
    const out = runGuard('elder bulletin post "cost $"');

    expect(out.status).toBe(0);
  });
});
