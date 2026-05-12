#!/usr/bin/env node
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const repoRoot = path.resolve(__dirname, '..');
const abiTargets = [
  {
    artifactPath: path.join(repoRoot, 'packages/contracts/out/IClanWorld.sol/IClanWorld.json'),
    targetPath: path.join(repoRoot, 'packages/contracts/abi/IClanWorld.json'),
  },
  {
    artifactPath: path.join(repoRoot, 'packages/contracts/out/IClanWorldLens.sol/IClanWorldLens.json'),
    targetPath: path.join(repoRoot, 'packages/contracts/abi/IClanWorldLens.json'),
  },
  // Diamond / Ownership facet ABIs consumed by apps/dev-ui (generic write/read UI over the
  // ClanWorld diamond). Kept canonical here so dev-ui never vendors a stale copy.
  {
    artifactPath: path.join(repoRoot, 'packages/contracts/out/IDiamondLoupe.sol/IDiamondLoupe.json'),
    targetPath: path.join(repoRoot, 'packages/contracts/abi/IDiamondLoupe.json'),
  },
  {
    artifactPath: path.join(repoRoot, 'packages/contracts/out/IDiamondCut.sol/IDiamondCut.json'),
    targetPath: path.join(repoRoot, 'packages/contracts/abi/IDiamondCut.json'),
  },
  {
    artifactPath: path.join(repoRoot, 'packages/contracts/out/OwnershipFacet.sol/OwnershipFacet.json'),
    targetPath: path.join(repoRoot, 'packages/contracts/abi/OwnershipFacet.json'),
  },
];

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
