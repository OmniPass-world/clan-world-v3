#!/usr/bin/env node
/**
 * generate.mjs — generates packages/contract-types/src/*.ts from committed ABI JSON.
 * Source: packages/contracts/abi/*.json
 * Run: node scripts/generate.mjs
 * Check: node scripts/generate.mjs --check
 */
import { readFileSync, writeFileSync, existsSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const pkgRoot = resolve(__dirname, '..');
const repoRoot = resolve(pkgRoot, '../..');
const check = process.argv.includes('--check');
const errors = [];

function readAbi(name) {
  const path = resolve(repoRoot, 'packages/contracts/abi', `${name}.json`);
  const raw = JSON.parse(readFileSync(path, 'utf8'));
  // abi files may be plain arrays or { abi: [...] } objects
  return Array.isArray(raw) ? raw : raw.abi;
}

function generateFile(varName, abi) {
  const lines = [
    '// @generated — do not edit by hand. Run: pnpm --filter @clan-world/contract-types codegen',
    `export const ${varName} = ${JSON.stringify(abi, null, 2)} as const;`,
    '',
    `export type ${varName.charAt(0).toUpperCase() + varName.slice(1)}Type = typeof ${varName};`,
    '',
    '// Derived event name union',
    `export type ${varName.charAt(0).toUpperCase() + varName.slice(1)}EventName = (typeof ${varName})[number] extends infer T`,
    "  ? T extends { type: 'event'; name: string }",
    "    ? T['name']",
    '    : never',
    '  : never;',
    '',
    '// Derived function name union',
    `export type ${varName.charAt(0).toUpperCase() + varName.slice(1)}FunctionName = (typeof ${varName})[number] extends infer T`,
    "  ? T extends { type: 'function'; name: string }",
    "    ? T['name']",
    '    : never',
    '  : never;',
    '',
  ];
  return lines.join('\n');
}

const contracts = [
  { abiFile: 'IClanWorld', varName: 'iClanWorldAbi', outFile: 'IClanWorld.ts' },
  { abiFile: 'IClanWorldLens', varName: 'iClanWorldLensAbi', outFile: 'IClanWorldLens.ts' },
];

for (const { abiFile, varName, outFile } of contracts) {
  const abi = readAbi(abiFile);
  const content = generateFile(varName, abi);
  const outPath = resolve(pkgRoot, 'src', outFile);

  if (check) {
    if (!existsSync(outPath)) {
      errors.push(`MISSING: ${outPath} — run codegen`);
      continue;
    }
    const existing = readFileSync(outPath, 'utf8');
    if (existing !== content) {
      errors.push(`STALE: ${outPath} — run codegen`);
    }
  } else {
    writeFileSync(outPath, content, 'utf8');
    console.log(`wrote ${outFile}`);
  }
}

if (check) {
  if (errors.length > 0) {
    for (const e of errors) console.error(e);
    process.exit(1);
  }
  console.log('contract-types: up to date');
}
