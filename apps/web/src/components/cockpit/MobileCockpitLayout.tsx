import { useEffect, useRef, useState, useCallback } from 'react';
import { tokens, ELDERS, type ElderDef } from '../../styles/cockpit-tokens';
import { MiniCockpit } from './MiniCockpit';
import { WorldMapIframe } from '../WorldMapIframe';

const STORAGE_KEY_CLAN = 'cockpit-mobile-active-clan';
const STORAGE_KEY_COLLAPSED = 'cockpit-mobile-collapsed';

type ClanId = 1 | 2 | 3 | 4;

const VALID_CLAN_IDS: ReadonlySet<number> = new Set([1, 2, 3, 4]);

/**
 * Read the persisted active clan id from localStorage, or fall back to 1.
 * Wrapped in try/catch — Safari private mode throws on access. SSR-safe:
 * returns the default if `window` is undefined.
 */
function readActiveClan(): ClanId {
  if (typeof window === 'undefined') return 1;
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY_CLAN);
    const parsed = raw == null ? NaN : parseInt(raw, 10);
    if (VALID_CLAN_IDS.has(parsed)) {
      return parsed as ClanId;
    }
  } catch {
    // Safari private mode / disabled storage — fall through to default.
  }
  return 1;
}

function readCollapsed(): boolean {
  if (typeof window === 'undefined') return false;
  try {
    return window.localStorage.getItem(STORAGE_KEY_COLLAPSED) === '1';
  } catch {
    return false;
  }
}

function writeStorage(key: string, value: string): void {
  if (typeof window === 'undefined') return;
  try {
    window.localStorage.setItem(key, value);
  } catch {
    // ignore — private mode / disabled storage
  }
}

/**
 * Cockpit layout for viewports ≤900px. Vertical stack:
 *   - top half: WorldMap + collapse-toggle button
 *   - bottom half: page-indicator dots + horizontal scroll-snap pager
 *     of one MiniCockpit per elder (clan 1..4)
 *
 * Horizontal swipe between elders uses native CSS `scroll-snap-type: x mandatory`
 * — no manual touch-event handling. iOS gives us inertia + snap for free.
 *
 * The bottom panel can be collapsed (height 0) via a tap on the toggle button
 * anchored at the bottom of the world-map area; both the active clan and the
 * collapsed state persist in localStorage across reloads.
 */
export function MobileCockpitLayout() {
  const [activeClanId, setActiveClanId] = useState<ClanId>(() => readActiveClan());
  const [collapsed, setCollapsed] = useState<boolean>(() => readCollapsed());
  const scrollRef = useRef<HTMLDivElement | null>(null);
  // Suppress the onScroll handler while we're programmatically scrolling
  // in response to a state change (page-dot click or initial mount).
  // Without this, the scroll handler would race the state and bounce.
  const programmaticScrollRef = useRef(false);
  const scrollDebounceRef = useRef<number | null>(null);

  // Persist activeClanId on change.
  useEffect(() => {
    writeStorage(STORAGE_KEY_CLAN, String(activeClanId));
  }, [activeClanId]);

  // Persist collapsed on change.
  useEffect(() => {
    writeStorage(STORAGE_KEY_COLLAPSED, collapsed ? '1' : '0');
  }, [collapsed]);

  // Scroll to active clan panel when activeClanId changes (programmatic).
  // Uses the panel's offsetLeft so it works even before layout settles.
  useEffect(() => {
    const container = scrollRef.current;
    if (!container) return;
    const targetIndex = activeClanId - 1;
    const targetEl = container.children[targetIndex] as HTMLElement | undefined;
    if (!targetEl) return;
    programmaticScrollRef.current = true;
    container.scrollTo({ left: targetEl.offsetLeft, behavior: 'smooth' });
    // Release the programmatic-flag after the smooth-scroll settles.
    // 400ms is comfortably longer than the snap animation on all browsers we target.
    const t = window.setTimeout(() => {
      programmaticScrollRef.current = false;
    }, 400);
    return () => window.clearTimeout(t);
  }, [activeClanId]);

  // Derive activeClanId from scroll position (debounced).
  // Picks the panel whose center is closest to the container's viewport center.
  const handleScroll = useCallback(() => {
    if (programmaticScrollRef.current) return;
    if (scrollDebounceRef.current != null) {
      window.clearTimeout(scrollDebounceRef.current);
    }
    scrollDebounceRef.current = window.setTimeout(() => {
      const container = scrollRef.current;
      if (!container) return;
      const containerCenter = container.scrollLeft + container.clientWidth / 2;
      let bestIdx = 0;
      let bestDist = Infinity;
      for (let i = 0; i < container.children.length; i++) {
        const child = container.children[i] as HTMLElement;
        const childCenter = child.offsetLeft + child.clientWidth / 2;
        const d = Math.abs(childCenter - containerCenter);
        if (d < bestDist) {
          bestDist = d;
          bestIdx = i;
        }
      }
      const newClan = (bestIdx + 1) as ClanId;
      setActiveClanId((prev) => (prev === newClan ? prev : newClan));
    }, 150);
  }, []);

  useEffect(() => {
    return () => {
      if (scrollDebounceRef.current != null) {
        window.clearTimeout(scrollDebounceRef.current);
      }
    };
  }, []);

  const activeElder = ELDERS.find((e) => e.clanId === activeClanId) ?? ELDERS[0];
  const accent = activeElder?.accent ?? tokens.text.accent;

  const toggleCollapsed = useCallback(() => setCollapsed((c) => !c), []);

  return (
    <div
      data-testid="cockpit-mobile-layout"
      data-collapsed={collapsed}
      data-active-clan={activeClanId}
      style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        minHeight: 0,
        overflow: 'hidden',
      }}
    >
      {/* Top half: world map + collapse toggle.
          When collapsed=true, this region grows to fill the available space. */}
      <div
        data-testid="cockpit-mobile-worldmap-region"
        style={{
          position: 'relative',
          flex: collapsed ? '1 1 100%' : '0 0 50%',
          minHeight: 0,
          transition: 'flex-basis 250ms ease',
          border: `1px solid ${tokens.border.iron}`,
          borderRadius: tokens.radius.md,
          overflow: 'hidden',
          background: '#000',
        }}
      >
        <WorldMapIframe />
        <CollapseToggle collapsed={collapsed} onToggle={toggleCollapsed} accent={accent} />
      </div>

      {/* Bottom half: page dots + horizontal scroll-snap pager. */}
      <div
        data-testid="cockpit-mobile-pager-region"
        style={{
          flex: collapsed ? '0 0 0px' : '1 1 50%',
          minHeight: 0,
          overflow: 'hidden',
          display: 'flex',
          flexDirection: 'column',
          transition: 'flex-basis 250ms ease',
          // Hide the pager content during collapse — avoids a flash of
          // half-rendered tabs as the panel shrinks.
          opacity: collapsed ? 0 : 1,
          pointerEvents: collapsed ? 'none' : 'auto',
        }}
        aria-hidden={collapsed}
      >
        <PageIndicatorDots
          elders={ELDERS}
          activeClanId={activeClanId}
          onSelect={(id) => setActiveClanId(id)}
        />
        <div
          ref={scrollRef}
          data-testid="cockpit-mobile-pager"
          onScroll={handleScroll}
          style={{
            // `position: relative` makes this element the offsetParent for
            // its children, so `child.offsetLeft` measures from the
            // container's own left edge — correct relative to scrollLeft
            // regardless of any upstream padding or sidebar later added
            // above the cockpit-root.
            position: 'relative',
            flex: 1,
            display: 'flex',
            flexDirection: 'row',
            overflowX: 'auto',
            overflowY: 'hidden',
            scrollSnapType: 'x mandatory',
            WebkitOverflowScrolling: 'touch',
            scrollBehavior: 'smooth',
            minHeight: 0,
          }}
        >
          {ELDERS.map((elder) => (
            <div
              key={elder.clanId}
              data-testid={`cockpit-mobile-page-${elder.clanId}`}
              style={{
                flex: '0 0 100%',
                width: '100%',
                minWidth: '100%',
                scrollSnapAlign: 'start',
                scrollSnapStop: 'always',
                display: 'flex',
                flexDirection: 'column',
                minHeight: 0,
              }}
            >
              {/* MiniCockpit defaults to the terminal tab for clan 1
                  and vault for the rest, mirroring the desktop layout's
                  initial tab assignments. */}
              <MiniCockpit elder={elder} initialTab={elder.clanId === 1 ? 'terminal' : 'vault'} />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

interface CollapseToggleProps {
  collapsed: boolean;
  onToggle: () => void;
  accent: string;
}

/**
 * Thin pill anchored to the bottom-center of the world-map region. Tap to
 * toggle the bottom-pager between expanded (50%) and collapsed (0).
 */
function CollapseToggle({ collapsed, onToggle, accent }: CollapseToggleProps) {
  return (
    <button
      type="button"
      data-testid="cockpit-mobile-collapse-toggle"
      data-collapsed={collapsed}
      onClick={onToggle}
      aria-label={collapsed ? 'Expand elder panel' : 'Collapse elder panel'}
      aria-expanded={!collapsed}
      style={{
        position: 'absolute',
        bottom: '8px',
        left: '50%',
        transform: 'translateX(-50%)',
        height: '28px',
        minWidth: '64px',
        padding: '0 14px',
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '6px',
        border: `1px solid ${tokens.border.iron}`,
        borderRadius: '14px',
        background: 'rgba(10,10,10,0.72)',
        backdropFilter: 'blur(6px)',
        WebkitBackdropFilter: 'blur(6px)',
        color: accent,
        fontFamily: tokens.font.mono,
        fontSize: '11px',
        letterSpacing: '0.08em',
        textTransform: 'uppercase',
        cursor: 'pointer',
        zIndex: 10,
        boxShadow: '0 2px 8px rgba(0,0,0,0.6)',
      }}
    >
      <span aria-hidden style={{ fontSize: '10px', lineHeight: 1 }}>
        {collapsed ? '▲' : '▼'}
      </span>
      <span>{collapsed ? 'show' : 'hide'}</span>
    </button>
  );
}

interface PageIndicatorDotsProps {
  elders: ReadonlyArray<ElderDef>;
  activeClanId: ClanId;
  onSelect: (id: ClanId) => void;
}

/**
 * Apple-home-screen-style row of dots, one per elder. The active dot
 * picks up the elder's accent color; inactive dots are muted iron.
 */
function PageIndicatorDots({ elders, activeClanId, onSelect }: PageIndicatorDotsProps) {
  return (
    <div
      data-testid="cockpit-mobile-page-dots"
      role="group"
      aria-label="Elder selector"
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '6px',
        height: '24px',
        flexShrink: 0,
        background: tokens.bg.ironDeep,
        borderBottom: `1px solid ${tokens.border.iron}`,
      }}
    >
      {elders.map((elder) => {
        const active = elder.clanId === activeClanId;
        return (
          <button
            key={elder.clanId}
            type="button"
            aria-current={active ? 'true' : undefined}
            aria-label={`Show ${elder.name}`}
            data-testid={`cockpit-mobile-page-dot-${elder.clanId}`}
            data-active={active}
            onClick={() => onSelect(elder.clanId as ClanId)}
            style={{
              width: '8px',
              height: '8px',
              padding: 0,
              border: 'none',
              borderRadius: '50%',
              background: active ? elder.accent : tokens.text.onIronDim,
              opacity: active ? 1 : 0.4,
              cursor: 'pointer',
              transition: 'background 150ms ease, opacity 150ms ease',
            }}
          />
        );
      })}
    </div>
  );
}
