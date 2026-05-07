// Metro config for monorepo. Walks up to the workspace root so Metro can
// resolve hoisted Expo / React Native deps from the repo-level node_modules,
// not just apps/mobile/node_modules. Mirrors the pattern from
// https://docs.expo.dev/guides/monorepos/.

const { getDefaultConfig } = require('expo/metro-config');
const path = require('path');

const projectRoot = __dirname;
const workspaceRoot = path.resolve(projectRoot, '../..');

const config = getDefaultConfig(projectRoot);

// 1. Watch all files within the workspace
config.watchFolders = [workspaceRoot];

// 2. Resolve modules from the project's node_modules first, then the workspace root
config.resolver.nodeModulesPaths = [
  path.resolve(projectRoot, 'node_modules'),
  path.resolve(workspaceRoot, 'node_modules'),
];

// 3. Force Metro to resolve symlinks (pnpm uses them heavily)
config.resolver.disableHierarchicalLookup = false;

module.exports = config;
