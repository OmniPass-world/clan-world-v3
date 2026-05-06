import { Component } from 'react';
import type { ReactNode } from 'react';
import { WorldMap } from './WorldMap';
import { Cockpit } from './pages/Cockpit';
import { OwnerEditor } from './pages/OwnerEditor';
import { WorldMapBoundary } from './components/cockpit/shared/WorldMapBoundary';

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
          style={{
            background: '#0a0a0a',
            width: '100vw',
            height: '100vh',
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
 * fine here — the cockpit is a standalone judge view, not a SPA tab inside
 * the main app, so we never need to navigate between them client-side.
 */
function isCockpitRoute(): boolean {
  return (
    typeof window !== 'undefined' &&
    window.location.pathname.startsWith('/cockpit')
  );
}

function isOwnerRoute(): boolean {
  return (
    typeof window !== 'undefined' &&
    window.location.pathname.startsWith('/owner')
  );
}

export function App() {
  if (isCockpitRoute()) {
    return (
      <CockpitErrorBoundary>
        <Cockpit />
      </CockpitErrorBoundary>
    );
  }
  if (isOwnerRoute()) {
    return <OwnerEditor />;
  }
  return <MainApp />;
}

function MainApp() {
  return (
    <main
      style={{
        background: '#0a0a0a',
        width: '100vw',
        height: '100vh',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
      }}
    >
      <WorldMapBoundary>
        <WorldMap />
      </WorldMapBoundary>
    </main>
  );
}
