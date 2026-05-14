#!/usr/bin/env node
/**
 * Verifies that every committed ABI under `packages/contracts/abi/` matches the
 * freshly compiled artifact in `out/`. Pulls the target list from the shared
 * `scripts/abi-targets.mjs` so it cannot drift from `gen-contract-abi.mjs`.
 *
 * Usage: from `packages/contracts/`, run `pnpm check:abi`. Builds the canonical
 * sources via forge, then JSON-diffs each artifact's `.abi` against the
 * committed JSON entirely in Node — no shell interpolation of paths.
 */
import { spawnSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';

import { abiTargets, repoRoot } from '../../../scripts/abi-targets.mjs';

const contractsRoot = path.join(repoRoot, 'packages/contracts');

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

function readAbi(filePath, kind) {
  const raw = fs.readFileSync(filePath, 'utf8');
  const parsed = JSON.parse(raw);
  if (!Array.isArray(parsed.abi)) {
    throw new Error(`${kind} ${path.relative(repoRoot, filePath)} is missing an "abi" array`);
  }
  return parsed.abi;
}

let failed = false;

for (const { sourcePath, artifactPath, targetPath } of abiTargets) {
  const artifactRel = path.relative(repoRoot, artifactPath);
  const targetRel = path.relative(repoRoot, targetPath);

  if (!fs.existsSync(artifactPath)) {
    console.error(
      `MISSING artifact: ${artifactRel}\n` +
        `  forge built but did not emit this artifact. Likely cause: ` +
        `sourcePath="${sourcePath}" in scripts/abi-targets.mjs does not match the actual ` +
        `Solidity source file. Verify the file exists under packages/contracts/.`,
    );
    failed = true;
    continue;
  }
  if (!fs.existsSync(targetPath)) {
    console.error(`MISSING committed ABI: ${targetRel} (run pnpm codegen)`);
    failed = true;
    continue;
  }

  let artifactAbi;
  let committedAbi;
  try {
    artifactAbi = readAbi(artifactPath, 'artifact');
    committedAbi = readAbi(targetPath, 'committed ABI');
  } catch (err) {
    console.error(err.message);
    failed = true;
    continue;
  }

  const artifactJson = JSON.stringify(artifactAbi);
  const committedJson = JSON.stringify(committedAbi);
  if (artifactJson !== committedJson) {
    console.error(`ABI MISMATCH: ${targetRel} differs from ${artifactRel}`);
    console.error('Run `pnpm codegen` from the repo root to refresh committed ABIs.');
    failed = true;
  }
}

if (failed) {
  process.exit(1);
}

console.log(`OK: ${abiTargets.length} ABI target(s) match their compiled artifacts.`);
