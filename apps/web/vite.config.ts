import { defineConfig, type Plugin } from 'vite';
import react from '@vitejs/plugin-react';
import { execSync } from 'node:child_process';
import path from 'node:path';

// Canonical port resolved from port-for registry (clan-world slot 587, env=dev).
// Falls back to FALLBACK_PORT (canonical value from .world/ports.yml) when port-for is
// unavailable (CI, non-do-box hosts). PORT env override takes precedence.
const FALLBACK_PORT = 58740;
const DEFAULT_PORT = (() => {
  if (process.env.PORT) {
    const p = parseInt(process.env.PORT, 10);
    return Number.isNaN(p) ? FALLBACK_PORT : p;
  }
  try {
    const port = parseInt(execSync('port-for clan-world-frontend-dev', { encoding: 'utf8' }).trim(), 10);
    return Number.isNaN(port) ? FALLBACK_PORT : port;
  } catch {
    return FALLBACK_PORT;
  }
})();

/**
 * Inject `<link rel="preload" as="image">` for the map-BG asset into index.html.
 *
 * Why: MapGhostLayer.tsx renders the world-map.png background ASAP and ideally
 * with `decoding="async"` so the first React paint doesn't block on bitmap
 * decode. For that to be safe on cold cache (no HTTP entry), the browser needs
 * to start fetching+decoding the PNG as early as possible. A `<link rel=preload>`
 * in the HTML head gives the browser the URL during HTML parse — before any
 * JS executes — so the fetch races with React bootstrap instead of waiting for
 * mount. See issues #271, #298.
 *
 * Vite hashes the asset at build time (`world-map.<hash>.png`), so the link
 * href can't be static. This `transformIndexHtml` hook computes the right href
 * for both dev (unhashed) and build (hashed) modes:
 *   - In build: `ctx.bundle` is populated → scan for the emitted asset whose
 *     filename matches `world-map-*.png` (or `world-map.png` if hash disabled)
 *     and use its bundle key as the href.
 *   - In dev: `ctx.bundle` is undefined → use the source path so the dev
 *     server's middleware resolves it via the import graph.
 *
 * `order: 'pre'` ensures the link is injected before Vite's own asset
 * transforms run on index.html.
 */
function mapBgPreloadPlugin(): Plugin {
  return {
    name: 'inject-map-bg-preload',
    transformIndexHtml: {
      order: 'pre',
      handler(html, ctx) {
        let href: string | null = null;
        if (ctx.bundle) {
          // Build: find the emitted hashed asset. Match `world-map` followed
          // by EITHER `.png` (no hash) or `-<hash>.png` (Vite's default
          // emitFileName scheme: name-hash.ext). Restrict to the assets dir
          // and forbid additional name segments after `world-map` — otherwise
          // sibling assets like `world-map-winter-*.png` would also match.
          // The hash itself is base64url-ish ([A-Za-z0-9_-]+) but never
          // contains a hyphen at the start (Vite uses underscores in the
          // middle of the hash); requiring a single `-` then non-hyphen chars
          // then `.png` is enough to disambiguate from `world-map-winter-*`.
          const key = Object.keys(ctx.bundle).find((p) =>
            /(^|\/)assets\/world-map(?!-winter)(?:-[A-Za-z0-9_-]+)?\.png$/.test(p),
          );
          if (key) {
            href = `/${key}`;
          }
        } else {
          // Dev: Vite serves the source asset directly via /src/assets/...
          href = '/src/assets/world-map.png';
        }
        if (!href) return html;
        const preloadTag = `    <link rel="preload" as="image" href="${href}" />`;
        return html.replace('</head>', `${preloadTag}\n  </head>`);
      },
    },
  };
}

// envDir: load .env.local from the monorepo root, not the web app folder.
// .env.local lives at <repo-root>/.env.local and is shared with server/agents.
// Without this, VITE_* values are undefined at build time and the prod bundle
// has no Convex URL or demo-mode configuration baked in.
export default defineConfig({
  plugins: [react(), mapBgPreloadPlugin()],
  envDir: path.resolve(__dirname, '../..'),
  server: {
    port: DEFAULT_PORT,
    host: '127.0.0.1',
  },
});
