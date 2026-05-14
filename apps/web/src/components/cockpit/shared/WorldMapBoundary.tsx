import { Component, Fragment, type ReactNode } from 'react';
import { tokens } from '../../../styles/cockpit-tokens';

/**
 * Maximum number of auto-retry attempts within RETRY_WINDOW_MS before we stop
 * trying and require the user to tap the manual reload button. Tuned to
 * absorb transient mobile-Safari GPU resets (typically 1-2 in quick succession
 * after a memory-pressure event) without spinning forever on a truly broken
 * GPU.
 */
const MAX_AUTO_RETRIES = 3;

/**
 * Rolling window for the retry budget. After 60s of stability we forget old
 * retry timestamps so a fresh burst doesn't get penalised by ancient history.
 */
const RETRY_WINDOW_MS = 60_000;

/**
 * Delay before the first auto-retry kicks in. Gives the browser a beat to
 * actually finish reclaiming + restoring the GL context before we mount a
 * fresh canvas on top of it. Empirically anything under ~500ms tends to
 * immediately re-lose the new context on iOS Safari after a memory event.
 */
const RETRY_DELAY_MS = 1000;

/**
 * Detect whether the captured error is something we want to auto-retry.
 * Only WebGL-flavored failures qualify — generic errors (Convex unreachable,
 * asset load failure, programmer bugs) need user attention and would just
 * spin forever in a retry loop. Match is intentionally generous: covers the
 * exact string thrown from WorldMap.tsx as well as anything PixiJS or the
 * browser surfaces that mentions WebGL / GL / context loss.
 */
function isWebglError(message: string | undefined): boolean {
  if (!message) return false;
  const m = message.toLowerCase();
  return (
    m.includes('webgl context lost') ||
    m.includes('webgl context') ||
    m.includes('webglcontextlost') ||
    m.includes('context lost') ||
    m.includes('gl context')
  );
}

interface State {
  hasError: boolean;
  errorMessage?: string;
  /**
   * Bumped each time we auto-retry. Used as a React `key` on the children so
   * the WorldMap subtree fully unmounts + remounts (fresh Pixi `Application`,
   * fresh canvas, fresh GL context) instead of trying to reuse a destroyed
   * context.
   */
  retryKey: number;
  /**
   * Timestamps (ms since epoch) of recent auto-retry attempts, used to enforce
   * MAX_AUTO_RETRIES within RETRY_WINDOW_MS. Entries older than the window
   * are pruned before each new attempt.
   */
  retryTimestamps: number[];
  /**
   * True while a setTimeout-scheduled retry is pending. Used to render the
   * "Recovering…" state instead of the manual "Tap to reload" button while
   * auto-recovery is in flight.
   */
  retryPending: boolean;
}

/**
 * Tiny error boundary scoped to the cockpit's center cell. The WorldMap
 * pixi canvas can throw during init when Convex is unreachable / the
 * standalone judge route loads without a backend. In that case we want the
 * 4 mini-cockpits to keep rendering — only the map cell should degrade.
 *
 * Also handles WebGL context-loss recovery: on mobile Safari the GL context
 * can be reclaimed under memory pressure or tab backgrounding. When that
 * happens WorldMap throws a "WebGL context lost" error and lands here. For
 * those errors we auto-retry by bumping a remount key on the children — up
 * to MAX_AUTO_RETRIES within RETRY_WINDOW_MS — instead of forcing a full
 * page reload. Other error classes still require user action via the
 * "Tap to reload" button.
 */
export class WorldMapBoundary extends Component<
  { children: ReactNode },
  State
> {
  override state: State = {
    hasError: false,
    retryKey: 0,
    retryTimestamps: [],
    retryPending: false,
  };

  /**
   * Tracks the pending auto-retry timer so componentWillUnmount can cancel
   * it. Using a ref-style instance field rather than state because writing
   * it doesn't need to trigger a re-render.
   */
  private retryTimer: ReturnType<typeof setTimeout> | null = null;

  /**
   * True between mount and unmount. Guards the setTimeout callback so we
   * don't setState on a torn-down component (React 18 strict-mode double-
   * mount safety + general defence against unmount-during-retry).
   */
  private mounted = false;

  static getDerivedStateFromError(error: unknown): Partial<State> {
    const errorMessage =
      error instanceof Error ? error.message : String(error ?? 'unknown');
    return { hasError: true, errorMessage };
  }

  override componentDidMount() {
    this.mounted = true;
  }

  override componentDidCatch(error: unknown, info: unknown) {
    // Use console.error (not warn) so the boundary trip is loud in DevTools
    // and surfaces in error-tracking integrations. Includes React component
    // stack so we can see WHERE in the WorldMap tree the throw came from.
    console.error('[cockpit] WorldMap failed to mount:', error, info);

    // Auto-recovery for WebGL context loss: schedule a retry by bumping the
    // remount key. We do this from componentDidCatch (not getDerivedStateFromError)
    // because getDerivedStateFromError is supposed to be pure — side-effects
    // like setTimeout belong here.
    const message =
      error instanceof Error ? error.message : String(error ?? '');
    if (isWebglError(message)) {
      this.scheduleAutoRetry();
    }
  }

  override componentWillUnmount() {
    this.mounted = false;
    if (this.retryTimer !== null) {
      clearTimeout(this.retryTimer);
      this.retryTimer = null;
    }
  }

  /**
   * If the retry budget allows, schedule a remount of the children after
   * RETRY_DELAY_MS. Caller has already confirmed the error is WebGL-flavored.
   * Prunes stale timestamps outside RETRY_WINDOW_MS before counting, so a
   * user who hits the limit once and then runs cleanly for 60s gets a fresh
   * budget on the next incident.
   */
  private scheduleAutoRetry() {
    const now = Date.now();
    const recent = this.state.retryTimestamps.filter(
      t => now - t < RETRY_WINDOW_MS,
    );
    if (recent.length >= MAX_AUTO_RETRIES) {
      // Budget exhausted — let the manual "Tap to reload" UI take over.
      // Clear retryPending so the button (not the spinner) renders.
      if (this.state.retryPending) {
        this.setState({ retryPending: false });
      }
      return;
    }

    // Guard against double-scheduling (defensive — React 18 strict mode can
    // double-invoke componentDidCatch in dev). If a timer is already pending,
    // let it run.
    if (this.retryTimer !== null) return;

    this.setState({ retryPending: true });
    this.retryTimer = setTimeout(() => {
      this.retryTimer = null;
      if (!this.mounted) return;
      this.setState(prev => ({
        hasError: false,
        errorMessage: undefined,
        retryKey: prev.retryKey + 1,
        retryTimestamps: [
          ...prev.retryTimestamps.filter(t => Date.now() - t < RETRY_WINDOW_MS),
          Date.now(),
        ],
        retryPending: false,
      }));
    }, RETRY_DELAY_MS);
  }

  override render() {
    if (this.state.hasError) {
      const isWebgl = isWebglError(this.state.errorMessage);
      return (
        <div
          data-testid="cockpit-worldmap-fallback"
          style={{
            width: '100%',
            height: '100%',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background:
              'radial-gradient(ellipse at center, #1a1612 0%, #050505 80%)',
            color: tokens.text.onIron,
            fontFamily: tokens.font.display,
            letterSpacing: '0.2em',
            fontSize: '12px',
            textAlign: 'center',
            padding: tokens.space.xl,
          }}
        >
          <div>
            <div
              style={{
                fontSize: '32px',
                marginBottom: tokens.space.md,
                opacity: 0.6,
              }}
              aria-hidden
            >
              ◈
            </div>
            <div style={{ textTransform: 'uppercase' }}>
              {this.state.retryPending ? 'Recovering Map…' : 'World Map Offline'}
            </div>
            <div
              style={{
                fontFamily: tokens.font.mono,
                fontSize: '10px',
                marginTop: tokens.space.sm,
                color: tokens.text.muted,
                letterSpacing: '0.05em',
              }}
            >
              {this.state.retryPending
                ? 'Reclaiming WebGL context'
                : this.state.errorMessage
                  ? `error: ${this.state.errorMessage}`
                  : 'Standalone view — backend not reachable'}
            </div>
            {isWebgl && !this.state.retryPending && (
              <button
                type="button"
                onClick={() => window.location.reload()}
                style={{
                  marginTop: tokens.space.md,
                  padding: '6px 16px',
                  border: `1px solid ${tokens.border.ironLight}`,
                  borderRadius: tokens.radius.sm,
                  background: 'transparent',
                  color: tokens.text.accent,
                  fontFamily: tokens.font.mono,
                  fontSize: '10px',
                  letterSpacing: '0.08em',
                  textTransform: 'uppercase',
                  cursor: 'pointer',
                }}
              >
                Tap to reload
              </button>
            )}
          </div>
        </div>
      );
    }
    // `key` on a Fragment forces a full unmount+remount of the WorldMap
    // subtree after each auto-recovery so PixiJS gets a fresh canvas + fresh
    // GL context instead of trying to revive a destroyed one. Fragment (not
    // a div) is deliberate: the parent containers in MobileCockpitLayout,
    // Cockpit page, and the standalone /worldmap-only route all rely on
    // WorldMap's own root div being a direct flex/grid child — an extra
    // wrapper div would break flex sizing in App.tsx. `key` is stable across
    // non-error renders (retryKey only changes via scheduleAutoRetry) so
    // normal re-renders don't blow away the map for no reason.
    return <Fragment key={this.state.retryKey}>{this.props.children}</Fragment>;
  }
}
