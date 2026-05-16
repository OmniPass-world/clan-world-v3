import { Component } from 'react';
import type { ReactNode } from 'react';
import { Cockpit } from './pages/Cockpit';
import { OwnerEditor } from './pages/OwnerEditor';
import { AgentControlPage } from './pages/agent/AgentControlPage';
import { WorldMapEmbed } from './components/WorldMapEmbed';

class CockpitErrorBoundary extends Component<{ children: ReactNode }, { hasError: boolean }> {
  override state = { hasError: false };
  static getDerivedStateFromError(): { hasError: boolean } {
    return { hasError: true };
  }
  override componentDidCatch(error: unknown, info: unknown) {
    console.error('[Cockpit] uncaught error — full panel crash:', error, info);
  }
  override render() {
    if (this.state.hasError) {
      return (
        <main
          className="cw-fullheight"
          style={{
            background: '#0a0a0a',
            width: '100vw',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexDirection: 'column',
            gap: '16px',
            color: 'white',
            fontFamily: 'monospace',
            textAlign: 'center',
          }}
        >
          <div style={{ fontSize: '32px', opacity: 0.5 }}>◈</div>
          <div style={{ fontSize: '13px', letterSpacing: '0.2em', textTransform: 'uppercase' }}>
            Cockpit offline
          </div>
          <button
            type="button"
            onClick={() => window.location.reload()}
            style={{
              padding: '8px 20px',
              border: '1px solid rgba(255,255,255,0.25)',
              borderRadius: '4px',
              background: 'transparent',
              color: 'rgba(255,255,255,0.7)',
              fontFamily: 'monospace',
              fontSize: '11px',
              letterSpacing: '0.1em',
              textTransform: 'uppercase',
              cursor: 'pointer',
            }}
          >
            Tap to reload
          </button>
        </main>
      );
    }
    return this.props.children;
  }
}

// Env flags moved to ./config/env to break the WorldMap ↔ App circular
// dependency (PR #133 review MUST FIX #3). Re-exported here for any external
// callers; new internal callers should import directly from ./config/env.
import { DEMO_MODE } from './config/env';
export { DEMO_MODE };

/**
 * Top-level route decision. Lightweight path-based routing avoids a router
 * dep for a single side route. Reading window.location once at render is
 * fine here — the cockpit is the default surface and the world map lives
 * on `/map`, so we never need client-side navigation between them.
 *
 * Phase 1.11 (issue #354) URL rename:
 *   - `/`         → Cockpit (was `/cockpit`)
 *   - `/map`      → World map (was `/`); also embedded as an iframe inside
 *                   the cockpit for Android parity
 *   - `/cockpit`  → 301-style client redirect to `/` for a 30-day back-compat
 *                   window
 *   - `/elder-N`  → ttyd (Caddy-handled, no React route — see #348)
 *   - `/owner`    → owner editor (unchanged)
 *   - `/agents/:id` → single-agent control page (unchanged)
 */
function isMapRoute(): boolean {
  return (
    typeof window !== 'undefined' &&
    (window.location.pathname === '/map' ||
      window.location.pathname.startsWith('/map/'))
  );
}

function isLegacyCockpitRoute(): boolean {
  return (
    typeof window !== 'undefined' &&
    (window.location.pathname === '/cockpit' ||
      window.location.pathname.startsWith('/cockpit/'))
  );
}

function isOwnerRoute(): boolean {
  return (
    typeof window !== 'undefined' &&
    window.location.pathname.startsWith('/owner')
  );
}

/**
 * /agents/:agentId — single-agent control page (mobile-portrait first).
 * Pure mock — no real Solana wallet, no Convex. Routed here BEFORE any
 * World-App / IDKit hooks fire so it works in a plain browser tab.
 */
function parseAgentRoute(): number | null {
  if (typeof window === 'undefined') return null;
  const m = window.location.pathname.match(/^\/agents\/(\d+)\/?$/);
  const raw = m?.[1];
  if (!raw) return null;
  const id = parseInt(raw, 10);
  return Number.isFinite(id) ? id : null;
}

export function App() {
  // Back-compat: `/cockpit*` is the V2-era cockpit URL. After the #354
  // rename it lives at root. We do a client-side redirect (replace, no
  // history entry) so existing bookmarks + PWA installs continue to work.
  // The Caddy front door also serves a 301 for the same path; this branch
  // covers any client-side navigation that bypasses Caddy (e.g. local dev).
  if (isLegacyCockpitRoute()) {
    if (typeof window !== 'undefined') {
      const suffix =
        window.location.pathname === '/cockpit'
          ? '/'
          : window.location.pathname.replace(/^\/cockpit/, '') || '/';
      const dest = suffix + window.location.search + window.location.hash;
      window.location.replace(dest);
    }
    // Render nothing during the redirect — the location.replace above
    // tears down this tree immediately.
    return null;
  }
  if (isMapRoute()) {
    return <MainApp />;
  }
  const agentId = parseAgentRoute();
  if (agentId !== null) {
    return (
      <CockpitErrorBoundary>
        <AgentControlPage agentId={agentId} />
      </CockpitErrorBoundary>
    );
  }
  if (isOwnerRoute()) {
    return <OwnerEditor />;
  }
  // Default route (`/`) is now the cockpit. Map content is embedded inside
  // it as an iframe pointing at `/map` to match Android-webview parity.
  return (
    <CockpitErrorBoundary>
      <Cockpit />
    </CockpitErrorBoundary>
  );
}

/**
 * `/map` — raw world-map surface (no cockpit chrome).
 *
 * This is the route the cockpit iframes for web/Android parity. It is also
 * directly reachable for users who want the full-bleed map view without
 * the cockpit panels.
 */
function MainApp() {
  return (
    <main
      className="cw-fullheight"
      style={{
        background: '#0a0a0a',
        width: '100vw',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
      }}
    >
      <WorldMapEmbed />
    </main>
  );
}
