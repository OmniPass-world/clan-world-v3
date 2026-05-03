import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const outDir = fileURLToPath(new URL('../out/', import.meta.url));
const limit = 24_576;

function* jsonFiles(dir) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      yield* jsonFiles(fullPath);
    } else if (entry.isFile() && entry.name.endsWith('.json')) {
      yield fullPath;
    }
  }
}

function isProductionArtifact(sourceFile) {
  if (sourceFile.endsWith('.t.sol')) return false;

  return (
    sourceFile.endsWith('Facet.sol') ||
    sourceFile.startsWith('Lib') ||
    sourceFile === 'Diamond.sol' ||
    sourceFile === 'ClanWorldDiamondInit.sol' ||
    sourceFile === 'ClanWorldLens.sol' ||
    sourceFile === 'MinimalERC20.sol' ||
    sourceFile === 'StubPool.sol'
  );
}

let failed = false;
const rows = [];

for (const file of jsonFiles(outDir)) {
  const artifact = JSON.parse(fs.readFileSync(file, 'utf8'));
  const sourceFile = path.basename(path.dirname(file));
  if (!isProductionArtifact(sourceFile)) continue;

  const object = artifact.deployedBytecode?.object;
  if (!object || object === '0x') continue;

  const size = (object.length - 2) / 2;
  const name = artifact.contractName ?? path.basename(file, '.json');
  rows.push({ name, size, sourceFile });
  if (size > limit) failed = true;
}

rows.sort((a, b) => b.size - a.size);
for (const row of rows) {
  const marker = row.size > limit ? 'FAIL' : ' ok ';
  console.log(`${marker} ${String(row.size).padStart(6)} ${row.name} (${row.sourceFile})`);
}

if (failed) {
  console.error(`One or more deployable contract artifacts exceed ${limit} bytes.`);
  process.exit(1);
}
