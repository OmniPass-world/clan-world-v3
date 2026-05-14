#!/usr/bin/env node
/**
 * Verifies that every committed ABI under `packages/contracts/abi/` matches the
 * freshly compiled artifact in `out/`. Pulls the target list from the shared
 * `scripts/abi-targets.mjs` so it cannot drift from `gen-contract-abi.mjs`.
 *
 * Usage: from `packages/contracts/`, run `pnpm check:abi`. Assumes `forge build`
 * has already been invoked by the calling script for the required sources.
 */
import { spawnSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

import { abiTargets, repoRoot } from '../../../scripts/abi-targets.mjs';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const contractsRoot = path.resolve(__dirname, '..');

const jqCheck = spawnSync('bash', ['-c', 'command -v jq >/dev/null 2>&1'], { stdio: 'ignore' });
if (jqCheck.status !== 0) {
  console.error('jq not installed; cannot check ABI');
  process.exit(1);
}

// Build every source that has a committed ABI target.
const sources = abiTargets.map(({ sourcePath }) => sourcePath);
const build = spawnSync(
  'bash',
  ['scripts/forge.sh', 'build', ...sources],
  { cwd: contractsRoot, stdio: 'inherit' },
);
if (build.status !== 0) {
  process.exit(build.status ?? 1);
}

let failed = false;

for (const { artifactPath, targetPath } of abiTargets) {
  const artifactRel = path.relative(repoRoot, artifactPath);
  const targetRel = path.relative(repoRoot, targetPath);

  if (!fs.existsSync(artifactPath)) {
    console.error(`MISSING artifact: ${artifactRel} (run forge build)`);
    failed = true;
    continue;
  }
  if (!fs.existsSync(targetPath)) {
    console.error(`MISSING committed ABI: ${targetRel} (run pnpm codegen)`);
    failed = true;
    continue;
  }

  const diff = spawnSync(
    'bash',
    ['-c', `diff <(jq ".abi" "${artifactPath}") <(jq ".abi" "${targetPath}")`],
    { stdio: 'inherit' },
  );

  if (diff.status !== 0) {
    console.error(`ABI MISMATCH: ${targetRel} differs from ${artifactRel}`);
    console.error('Run `pnpm codegen` from the repo root to refresh committed ABIs.');
    failed = true;
  }
}

if (failed) {
  process.exit(1);
}

console.log(`OK: ${abiTargets.length} ABI target(s) match their compiled artifacts.`);
