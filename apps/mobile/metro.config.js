// Metro config for monorepo. Walks up to the workspace root so Metro can
// resolve hoisted Expo / React Native deps from the repo-level node_modules,
// not just apps/mobile/node_modules. Mirrors the pattern from
// https://docs.expo.dev/guides/monorepos/.

const { getDefaultConfig } = require('expo/metro-config');
const path = require('path');

const projectRoot = __dirname;
const workspaceRoot = path.resolve(projectRoot, '../..');

const config = getDefaultConfig(projectRoot);

// Watch the entire workspace
config.watchFolders = [workspaceRoot];

// Resolve modules from the project's node_modules first, then the workspace root
config.resolver.nodeModulesPaths = [
  path.resolve(projectRoot, 'node_modules'),
  path.resolve(workspaceRoot, 'node_modules'),
];

// Force singletons. The workspace root has react@19 (apps/landing,
// gold-bridge); mobile uses react@18. Without forcing, hoisted RN/Expo
// packages resolve `react` → workspace-root 19 while mobile resolves it →
// local 18, putting BOTH in the bundle. React's element `$$typeof` symbol
// differs between 18 and 19, so cross-version elements fail with "Objects
// are not valid as a React child" at runtime.
//
// We use resolveRequest to intercept just these specific package names and
// force them to mobile's local copy regardless of who's importing.
const SINGLETON_PACKAGES = new Set([
  'react',
  'react-native',
  '@babel/runtime',
]);

const previousResolveRequest = config.resolver.resolveRequest;

config.resolver.resolveRequest = (context, moduleName, platform) => {
  // Force exact-name singletons to mobile's local node_modules
  if (SINGLETON_PACKAGES.has(moduleName)) {
    return context.resolveRequest(
      { ...context, originModulePath: path.join(projectRoot, 'index.js') },
      moduleName,
      platform,
    );
  }
  // Also force scoped-package internals (e.g. `react/jsx-runtime`,
  // `@babel/runtime/helpers/foo`) so subpath imports honor the singleton.
  for (const pkg of SINGLETON_PACKAGES) {
    if (moduleName === pkg || moduleName.startsWith(pkg + '/')) {
      return context.resolveRequest(
        { ...context, originModulePath: path.join(projectRoot, 'index.js') },
        moduleName,
        platform,
      );
    }
  }
  return previousResolveRequest
    ? previousResolveRequest(context, moduleName, platform)
    : context.resolveRequest(context, moduleName, platform);
};

config.resolver.unstable_enableSymlinks = true;

module.exports = config;
