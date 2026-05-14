import { tokens, ELDERS } from '../styles/cockpit-tokens';
import { MiniCockpit } from '../components/cockpit/MiniCockpit';
import { CockpitHeader } from '../components/cockpit/CockpitHeader';
import { MobileCockpitLayout } from '../components/cockpit/MobileCockpitLayout';
import { WorldMap } from '../WorldMap';
import { WorldMapBoundary } from '../components/cockpit/shared/WorldMapBoundary';
import { useMediaQuery } from '../hooks/useMediaQuery';

/**
 * Cockpit shell.
 *
 * Two layouts behind a single 900px breakpoint:
 *
 *   - Desktop (>900px) — original 3-col 2-row grid:
 *
 *       1  |    | 2
 *       -- | 5  | --
 *       3  |    | 4
 *
 *     Panels 1-4 are mini-cockpits (one per Elder, hardcoded clans 1-4).
 *     Panel 5 is the existing WorldMap pixi canvas, spanning both rows.
 *
 *   - Mobile (≤900px) — vertical stack with world map on top + a horizontal
 *     scroll-snap pager of one mini-cockpit per elder on the bottom.
 *     Implemented in `MobileCockpitLayout`. See that file for the full spec.
 *
 * The structural difference between the two is too large for pure CSS
 * (grid → horizontal scroll-snap), so we branch on a media-query hook.
 *
 * Phase A.5b: terminal tabs render a live ttyd iframe; tick counter +
 * connection indicator hoisted to a single app-level CockpitHeader.
 *
 * Phase B (separate PR) wires real Convex data + tick subscription.
 */
export function Cockpit() {
  const isMobile = useMediaQuery('(max-width: 900px)');

  return (
    <main
      data-testid="cockpit-root"
      className="cw-fullheight"
      style={{
        background: tokens.bg.void,
        width: '100vw',
        paddingBottom: 'env(safe-area-inset-bottom)',
        boxSizing: 'border-box',
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      <CockpitHeader />

      {/* Themed scrollbars scoped to the cockpit subtree.
          Matches the iron palette so default light-grey browser bars don't
          clash with the dark cockpit chrome. Standard (Firefox) +
          ::-webkit-scrollbar (Chromium/Safari) covered.
          NOTE: does NOT affect the ttyd terminal scrollbar — that lives
          inside an iframe document we can't style from this parent. */}
      <style>{`
        [data-testid="cockpit-root"] {
          scrollbar-width: thin;
          scrollbar-color: ${tokens.border.iron} transparent;
        }
        [data-testid="cockpit-root"] ::-webkit-scrollbar {
          width: 8px;
          height: 8px;
        }
        [data-testid="cockpit-root"] ::-webkit-scrollbar-track {
          background: transparent;
        }
        [data-testid="cockpit-root"] ::-webkit-scrollbar-thumb {
          background: ${tokens.border.iron};
          border-radius: 4px;
        }
        [data-testid="cockpit-root"] ::-webkit-scrollbar-thumb:hover {
          background: ${tokens.text.onIronDim};
        }
        [data-testid="cockpit-root"] ::-webkit-scrollbar-corner {
          background: transparent;
        }
      `}</style>

      {isMobile ? <MobileCockpitLayout /> : <DesktopCockpitLayout />}
    </main>
  );
}

/**
 * Original desktop layout — 3-col 2-row grid with the world map center
 * spanning both rows and four mini-cockpits at the corners. Untouched
 * by the mobile redesign other than being extracted into its own
 * component for the media-query branch.
 */
function DesktopCockpitLayout() {
  // Elder slot mapping — physical grid position → clanId.
  // Row 1: clan 1 left, clan 2 right.
  // Row 2: clan 3 left, clan 4 right.
  // Lookup by clanId so TS keeps strict-mode happy without non-null assertions
  // on a tuple-from-array destructure.
  const findElder = (clanId: number) => {
    const e = ELDERS.find((x) => x.clanId === clanId);
    if (!e) {
      throw new Error(`ELDERS roster missing clanId ${clanId} — check cockpit-tokens.ts`);
    }
    return e;
  };
  const el1 = findElder(1);
  const el2 = findElder(2);
  const el3 = findElder(3);
  const el4 = findElder(4);

  return (
    <>
      {/* Desktop grid: 3 cols × 2 rows, center spans both rows.
          The mobile React branch in <Cockpit /> above takes priority at
          ≤900px, so this CSS only ever applies at viewports >900px and
          doesn't need its own breakpoint fallback. The previous 960px
          @media single-column stack is removed because (a) it's no longer
          reachable on phones — the React branch wins — and (b) keeping it
          would regress 901..960px viewports into a stacked layout that
          violates the "desktop must remain identical above 900px" spec. */}
      <style>{`
        .cockpit-grid {
          flex: 1;
          display: grid;
          gap: 0px;
          padding: 0px;
          min-height: 0;
          grid-template-columns: 1fr 1fr 1fr;
          grid-template-rows: minmax(240px, 1fr) minmax(240px, 1fr);
          grid-template-areas:
            "p1 p5 p2"
            "p3 p5 p4";
        }
        .cockpit-cell-1, .cockpit-cell-2, .cockpit-cell-3, .cockpit-cell-4 {
          min-width: 0; min-height: 0;
          display: flex; flex-direction: column;
        }
        .cockpit-cell-1 > *, .cockpit-cell-2 > *, .cockpit-cell-3 > *, .cockpit-cell-4 > * {
          flex: 1; min-height: 0;
        }
        .cockpit-cell-1 { grid-area: p1; }
        .cockpit-cell-2 { grid-area: p2; }
        .cockpit-cell-3 { grid-area: p3; }
        .cockpit-cell-4 { grid-area: p4; }
        .cockpit-cell-5 { grid-area: p5; min-width: 0; min-height: 0; position: relative; }
      `}</style>

      <div className="cockpit-grid" data-testid="cockpit-grid">
        <div className="cockpit-cell-1">
          <MiniCockpit elder={el1} />
        </div>
        <div className="cockpit-cell-2">
          <MiniCockpit elder={el2} initialTab="vault" />
        </div>
        <div className="cockpit-cell-3">
          <MiniCockpit elder={el3} initialTab="vault" />
        </div>
        <div className="cockpit-cell-4">
          <MiniCockpit elder={el4} initialTab="vault" />
        </div>
        <div
          className="cockpit-cell-5"
          data-testid="cockpit-worldmap"
          style={{
            border: `1px solid ${tokens.border.iron}`,
            borderRadius: tokens.radius.md,
            overflow: 'hidden',
            background: '#000',
          }}
        >
          {/* WorldMap is full-bleed; sits inside the center cell.
              The component handles its own pixi canvas sizing via ResizeObserver.
              Wrapped in a boundary so a backend-less load (judge route in plain
              browser) renders a degraded placeholder instead of unmounting the
              whole cockpit tree. */}
          <WorldMapBoundary>
            <WorldMap />
          </WorldMapBoundary>
        </div>
      </div>
    </>
  );
}
