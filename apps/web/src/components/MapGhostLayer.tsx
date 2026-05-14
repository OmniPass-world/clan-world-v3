import { useEffect, useMemo, useRef, useState } from 'react';
import worldMapBg from '../assets/world-map.png';
import {
  MAP_WIDTH,
  MAP_HEIGHT,
  REGION_CENTERS_BY_KEY,
  LIVE_CLAN_REGION_BY_ID,
  BASE_PNG_BY_VISUAL_INDEX,
  FALLBACK_HOME_REGION_BY_INDEX,
  baseVisualScale as _baseVisualScale,
} from './mapGeometry';
import {
  CACHE_KEY as SNAPSHOT_CACHE_KEY,
  PAYLOAD_VERSION as SNAPSHOT_PAYLOAD_VERSION,
  MAX_CACHE_AGE_MS as SNAPSHOT_MAX_AGE_MS,
} from '../hooks/snapshotCacheConstants';
import { isValidSnapshotShape } from '../hooks/useCachedSnapshot';

/**
 * MapGhostLayer — static HTML+CSS "ghost" of the world map rendered ABOVE the
 * PixiJS canvas while WebGL is warming up, then fading out once pixiReady fires.
 *
 * Why: on iOS Safari (and other browsers that aggressively pause background
 * tabs), returning to the PWA goes through this sequence:
 *
 *   1. React mounts. (instant)
 *   2. `useCachedSnapshot` hydrates from localStorage. (instant — PR #269)
 *   3. PixiJS Application.init() — creates WebGL context, uploads textures,
 *      builds the scene graph. ~500ms-1s on cold mobile Safari.
 *   4. `pixiReady` flips true and the canvas paints its first frame.
 *
 * Between step 2 and step 4 the user sees a blank #1a2a1a background where
 * the map should be. This component fills that gap with plain <img> tags
 * (map background + clan base sprites) positioned using the same viewport
 * (cx, cy, scale) state that pixi-viewport already persists to
 * sessionStorage as `cw-viewport-v1`.
 *
 * The ghost is purely additive — it never touches the PixiJS pipeline and
 * does not depend on Pixi being present. When `pixiReady` flips true the
 * ghost fades out via CSS opacity + visibility and unmounts after the
 * transition completes.
 *
 * Coordinate math (matches pixi-viewport's moveCenter + setZoom):
 *
 *   transform-origin: 0 0;
 *   transform: translate(Sw/2, Sh/2) scale(s) translate(-cx, -cy);
 *
 * applied to children positioned at world coords (wx, wy) maps each child
 * to screen coord (Sw/2 + (wx-cx)*s, Sh/2 + (wy-cy)*s) — identical to
 * pixi-viewport's projection.
 */

const VIEWPORT_STORAGE_KEY = 'cw-viewport-v1';
// Snapshot cache key / payload version / max age are imported from the shared
// `hooks/snapshotCacheConstants` module so the ghost reads the EXACT same
// payload that `useCachedSnapshot` writes. Previously these were duplicated
// here — a schema-migration footgun if PAYLOAD_VERSION ever drifted.

// Fade duration matches PixiJS's first-frame budget on mobile. Long enough
// that the eye reads the swap as a cross-fade, short enough not to feel
// laggy. Keep in sync with the inline style transition below.
const FADE_OUT_MS = 200;

// REGION_CENTERS_BY_KEY, LIVE_CLAN_REGION_BY_ID, BASE_PNG_BY_VISUAL_INDEX,
// FALLBACK_HOME_REGION_BY_INDEX are imported from ./mapGeometry — the shared
// pure-data module used by both the ghost and WorldMap.tsx. No PixiJS imports
// allowed in that module (see its header comment).

type GhostClan = {
  id: string;
  homeRegionKey: string;
  basePng: string;
  level: number;
};

type CachedSnapshotPayload = {
  v: number;
  ts: number;
  data: {
    clans?: Array<{
      id?: string;
      baseRegion?: number;
      baseLevel?: number;
      monumentLevel?: number;
    }>;
  };
};

/**
 * Read the same cached snapshot localStorage entry that useCachedSnapshot
 * writes — we need it synchronously at first render to avoid a flash between
 * mount and the first useEffect tick. Mirrors that hook's validation logic
 * exactly so a payload deemed invalid there is also ignored here.
 */
function readCachedClans(): GhostClan[] {
  try {
    const raw = localStorage.getItem(SNAPSHOT_CACHE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw) as CachedSnapshotPayload;
    if (!parsed || typeof parsed.ts !== 'number') return [];
    if (parsed.v !== SNAPSHOT_PAYLOAD_VERSION) return [];
    if (Date.now() - parsed.ts > SNAPSHOT_MAX_AGE_MS) return [];
    // Apply the same inner-shape guard `useCachedSnapshot` uses, so a payload
    // that the hook would reject (e.g. schema migration without PAYLOAD_VERSION
    // bump) is also rejected here. Keeps the two cache readers in lockstep.
    if (!isValidSnapshotShape(parsed.data)) return [];
    const clans = parsed.data?.clans;
    if (!Array.isArray(clans) || clans.length === 0) return [];
    const out: GhostClan[] = [];
    for (let i = 0; i < clans.length; i++) {
      const c = clans[i];
      if (!c || typeof c.id !== 'string') continue;
      const regionId = typeof c.baseRegion === 'number' ? c.baseRegion : 0;
      const homeRegionKey =
        LIVE_CLAN_REGION_BY_ID[regionId]
        ?? FALLBACK_HOME_REGION_BY_INDEX[i]
        ?? FALLBACK_HOME_REGION_BY_INDEX[0]!;
      const basePng =
        BASE_PNG_BY_VISUAL_INDEX[i]
        ?? BASE_PNG_BY_VISUAL_INDEX[i % BASE_PNG_BY_VISUAL_INDEX.length]
        ?? BASE_PNG_BY_VISUAL_INDEX[0]!;
      const level = clamp(c.baseLevel ?? c.monumentLevel ?? 1, 1, 5);
      out.push({ id: c.id, homeRegionKey, basePng, level });
    }
    return out;
  } catch {
    return [];
  }
}

function clamp(n: number, lo: number, hi: number): number {
  if (!Number.isFinite(n)) return lo;
  return Math.max(lo, Math.min(hi, n));
}

/**
 * Append the level suffix to a base PNG path, mirroring `levelBasePng` in
 * WorldMap.tsx so the ghost sprite uses the same upgraded variant the canvas
 * will render. Levels are clamped to the 1-5 range that the asset library
 * actually ships.
 */
function levelBasePng(basePng: string, level: number): string {
  const clamped = clamp(level, 1, 5);
  return basePng.replace(/(?:-lv[1-5])?\.png$/, `-lv${clamped}.png`);
}

type ViewportState = { cx: number; cy: number; scale: number };

/**
 * Read the persisted (cx, cy, scale) the user last sat at. If absent or
 * malformed, fall back to fit-cover centered on the world — same default
 * pixi-viewport applies on cold init. We pass screen dimensions in so the
 * fallback scale matches what the canvas will compute.
 *
 * The saved scale is only accepted when it lies inside the same
 * [initialFitScale, initialFitScale * 4] band that WorldMap.tsx enforces at
 * L1742-1743. Outside that band pixi-viewport falls back to cold-init
 * fit-cover, so the ghost must also fall back or they'd paint at different
 * zoom levels and produce the exact snap this component is meant to prevent.
 */
function readViewportState(screenW: number, screenH: number): ViewportState {
  const fitScale = Math.max(screenW / MAP_WIDTH, screenH / MAP_HEIGHT);
  const fallback: ViewportState = {
    cx: MAP_WIDTH / 2,
    cy: MAP_HEIGHT / 2,
    scale: fitScale,
  };
  try {
    const raw = sessionStorage.getItem(VIEWPORT_STORAGE_KEY);
    if (!raw) return fallback;
    const saved = JSON.parse(raw) as Partial<ViewportState>;
    // Bounds-clamp: a poisoned saved center (cx=99999, cy=99999) would render
    // the ghost off-world while the canvas independently does the same — the
    // user sees a persisted blank-screen state. Reject out-of-range coords
    // and fall through to fit-cover so the ghost matches the canvas's own
    // post-clamp behavior (WorldMap.tsx L1730-1750).
    if (
      typeof saved.cx === 'number' && Number.isFinite(saved.cx) &&
      typeof saved.cy === 'number' && Number.isFinite(saved.cy) &&
      typeof saved.scale === 'number' && Number.isFinite(saved.scale) &&
      saved.scale >= fitScale &&
      saved.scale <= fitScale * 4 &&
      saved.cx >= 0 && saved.cx <= MAP_WIDTH &&
      saved.cy >= 0 && saved.cy <= MAP_HEIGHT
    ) {
      return { cx: saved.cx, cy: saved.cy, scale: saved.scale };
    }
  } catch {
    // ignore — fall through to fit-cover
  }
  return fallback;
}

interface MapGhostLayerProps {
  /**
   * The same `pixiReady` boolean that WorldMap flips true once PixiJS has
   * finished init + first relayout. When true, the ghost fades out and
   * unmounts after the transition completes.
   */
  pixiReady: boolean;
}

export function MapGhostLayer({ pixiReady }: MapGhostLayerProps) {
  // Read cached clans ONCE per mount. We don't subscribe to live snapshot
  // updates because (a) the canvas will take over within a frame or two
  // anyway and (b) re-renders on every Convex tick would defeat the
  // "static / cheap" goal of this layer. If the user is on a cold mount
  // with empty cache, this returns [] and we render the map background
  // only (the 5s grace-period placeholder from PR #269 handles that case).
  const cachedClans = useMemo(() => readCachedClans(), []);

  // Match the wrapper's pixel dimensions so the projection math uses real
  // screen coords (the same dims pixi-viewport's screenWidth/Height see).
  // We use the parent container as the reference — the ghost is positioned
  // inset: 0 inside the same canvasWrapRef container, so its bounding-box
  // equals the canvas's bounding-box.
  const rootRef = useRef<HTMLDivElement>(null);
  // Init synchronously from visualViewport (falling back to innerWidth/Height)
  // so the first paint uses real screen dimensions instead of 0x0
  // (ResizeObserver fires post-paint). On iOS Safari window.innerHeight
  // returns the largest viewport (URL bar hidden); visualViewport.height
  // gives the actual visible viewport, excluding URL bar and on-screen
  // keyboard. Safari 13+ supports visualViewport; we fall back gracefully.
  const [dims, setDims] = useState<{ w: number; h: number }>(() => {
    if (typeof window === 'undefined') return { w: 1, h: 1 };
    const vv = window.visualViewport;
    return {
      w: (vv?.width ?? window.innerWidth) || 1,
      h: (vv?.height ?? window.innerHeight) || 1,
    };
  });

  useEffect(() => {
    const el = rootRef.current;
    if (!el) return;
    // Refine to the actual container dimensions once mounted (may differ from
    // window dims if the map occupies a sub-region of the viewport).
    setDims({ w: el.clientWidth || 1, h: el.clientHeight || 1 });
    if (typeof ResizeObserver === 'undefined') return;
    const ro = new ResizeObserver(() => {
      // clientWidth/Height return CSS pixels — which is exactly the
      // coordinate space pixi-viewport's screenWidth/screenHeight use
      // (autoDensity:true makes Pixi treat resolution internally).
      setDims({ w: el.clientWidth || 1, h: el.clientHeight || 1 });
    });
    ro.observe(el);
    return () => ro.disconnect();
  }, []);

  // After pixiReady flips true, hold the ghost mounted just long enough
  // for the CSS opacity transition to complete, then unmount entirely so
  // we don't pay any layout cost for an invisible layer. Mirrors the
  // FADE_OUT_MS constant above.
  const [fullyHidden, setFullyHidden] = useState(false);
  useEffect(() => {
    if (!pixiReady) {
      // Re-entry / hot-reload safety: if pixiReady ever flips back to false
      // (e.g. WebGL context loss recovery in a future iteration), make sure
      // the ghost is visible again.
      setFullyHidden(false);
      return;
    }
    // Add 50ms headroom above the CSS transition duration so the browser
    // compositor always delivers the final opacity=0 frame before we unmount.
    const t = window.setTimeout(() => setFullyHidden(true), FADE_OUT_MS + 50);
    return () => window.clearTimeout(t);
  }, [pixiReady]);

  const screenW = dims.w;
  const screenH = dims.h;

  // Compute viewport (cx, cy, scale) once per mount + when screen dims
  // settle. We don't subscribe to viewport changes during the ghost's
  // lifetime — it's only on-screen for ~500ms and the user can't pan a
  // canvas that hasn't rendered yet.
  // NOTE: must stay above the `if (fullyHidden) return null` guard so
  // the hook call count is constant across renders (Rules of Hooks).
  const viewport = useMemo(
    () => readViewportState(screenW || 1, screenH || 1),
    [screenW, screenH],
  );

  if (fullyHidden) return null;

  // CSS transform that mirrors pixi-viewport's moveCenter(cx,cy) + setZoom(s).
  // transform-origin defaults to 50% 50% — we explicitly set 0 0 below so
  // the math collapses to (translate Sw/2,Sh/2) (scale s) (translate -cx,-cy).
  const transform =
    `translate(${screenW / 2}px, ${screenH / 2}px) ` +
    `scale(${viewport.scale}) ` +
    `translate(${-viewport.cx}px, ${-viewport.cy}px)`;

  // Match Pixi's base size formula:
  //   baseSize = Math.max(67, 101 * cappedSizeScale) * baseVisualScale(level)
  // where cappedSizeScale = min(scaleX, scaleY, 1.4) and scaleX/Y =
  // screen/world. The ghost is rendered in WORLD-pixel coords (the transform
  // above does the scaling), so we want the WORLD-pixel size of the sprite,
  // which is what Pixi sets entry.sprite.width/height to before the viewport
  // scales it for display. So we compute the same baseSize here but in
  // world-pixel units — independent of screenW/screenH. The only screen-size
  // dependency is the `cappedSizeScale` cap (1.4) — but since relayout runs
  // at WORLD_WIDTH x WORLD_HEIGHT (see relayout call in WorldMap.tsx around
  // L2172), scaleX = scaleY = 1 and cappedSizeScale = 1. So baseSize here is
  // simply max(67, 101) * levelScale = 101 * levelScale — same as canvas.
  //
  // baseVisualScale mirrors the stepwise function in WorldMap.tsx (NOT a
  // linear formula): levels 1-2 → 0.675, 3-4 → 0.81, 5 → 0.9. Keep in sync.
  const baseSizeFor = (level: number): number => {
    const lvl = clamp(Math.floor(level), 1, 5);
    return 101 * _baseVisualScale(lvl);
  };

  return (
    <div
      ref={rootRef}
      aria-hidden="true"
      style={{
        position: 'absolute',
        inset: 0,
        // ABOVE the PixiJS canvas. WorldMap pins the canvas at z-index: 1;
        // we sit at z-index: 2 so the ghost is visible on every frame from
        // mount until `pixiReady` triggers the fade-out. pointer-events: none
        // (already set below) ensures the ghost never intercepts taps.
        zIndex: 2,
        overflow: 'hidden',
        pointerEvents: 'none',
        background: '#1a2a1a', // matches PixiJS init background color
        // Fade out once the canvas takes over; unmount after transition
        // (handled by the fullyHidden → return null above).
        opacity: pixiReady ? 0 : 1,
        transition: `opacity ${FADE_OUT_MS}ms ease-out`,
      }}
    >
      {/* World-space wrapper — applies the same projection pixi-viewport
          uses. Anything positioned at (wx, wy) in WORLD coords lands at the
          right pixel on screen. transform-origin: 0 0 makes the math
          collapse to (translate Sw/2,Sh/2)(scale s)(translate -cx,-cy). */}
      <div
        style={{
          position: 'absolute',
          top: 0,
          left: 0,
          width: MAP_WIDTH,
          height: MAP_HEIGHT,
          transformOrigin: '0 0',
          transform,
          // Hint to the browser to compositor-promote this subtree so the
          // background image upload happens off the main thread. Without
          // this, large bitmaps can synchronously decode on first paint.
          willChange: 'transform',
        }}
      >
        <img
          src={worldMapBg}
          alt=""
          decoding="sync"
          draggable={false}
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: MAP_WIDTH,
            height: MAP_HEIGHT,
            // pixelated matches PixiJS's Nearest-filter rendering so there
            // is no visual change when the canvas takes over.
            imageRendering: 'pixelated',
            userSelect: 'none',
            // Prevent iOS Safari's long-press image menu from popping if
            // the ghost happens to be visible during the user's tap.
            WebkitTouchCallout: 'none',
          }}
        />

        {/* Clan base sprites — only when we have a non-empty cached
            snapshot. First-ever-visit (no cache) just shows the map bg,
            then the grace-period placeholder from PR #269 appears after
            5s if chain data still hasn't arrived. */}
        {cachedClans.map((clan) => {
          const region = REGION_CENTERS_BY_KEY[clan.homeRegionKey];
          if (!region) return null;
          const cx = region.nx * MAP_WIDTH;
          const cy = region.ny * MAP_HEIGHT;
          const size = baseSizeFor(clan.level);
          const baseY = cy + size * 0.15; // matches entry.baseY in WorldMap relayout
          const src = levelBasePng(clan.basePng, clan.level);
          return (
            <img
              key={clan.id}
              src={src}
              alt=""
              decoding="async"
              draggable={false}
              style={{
                position: 'absolute',
                // anchor.set(0.5, 1) in Pixi = bottom-center. We translate
                // from top-left so subtract half-width + full-height.
                left: cx - size / 2,
                top: baseY - size,
                width: size,
                height: size,
                userSelect: 'none',
                WebkitTouchCallout: 'none',
              }}
            />
          );
        })}
      </div>
    </div>
  );
}
