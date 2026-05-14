#!/usr/bin/env node
import { spawnSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';

import { abiTargets, repoRoot } from './abi-targets.mjs';

if (process.argv.includes('--build')) {
  const sources = abiTargets.map(({ sourcePath }) => sourcePath);
  const contractsRoot = path.join(repoRoot, 'packages/contracts');
  const build = spawnSync(
    'bash',
    ['scripts/forge.sh', 'build', ...sources],
    { cwd: contractsRoot, stdio: 'inherit' },
  );
  if (build.status !== 0) {
    process.exit(build.status ?? 1);
  }
}

for (const { artifactPath, targetPath } of abiTargets) {
  if (!fs.existsSync(artifactPath)) {
    throw new Error(`Missing ${path.relative(repoRoot, artifactPath)}. Run forge build first.`);
  }

  const artifact = JSON.parse(fs.readFileSync(artifactPath, 'utf8'));
  if (!Array.isArray(artifact.abi)) {
    throw new Error(`${path.relative(repoRoot, artifactPath)} must contain an "abi" array`);
  }

  fs.mkdirSync(path.dirname(targetPath), { recursive: true });
  fs.writeFileSync(targetPath, `${JSON.stringify({ abi: artifact.abi }, null, 2)}\n`);

  console.log(`Updated ${path.relative(repoRoot, targetPath)} from ${path.relative(repoRoot, artifactPath)}.`);
}
