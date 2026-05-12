/**
 * Per-clan clansman sprite-sheet slicer + direction lookup.
 *
 * Liam-provided sheet layout (one PNG per clan, 1122 × 1402 px):
 *   - 4 columns × 5 rows
 *   - Each cell is one walk-cycle frame
 *   - Rows top→bottom = N, NE, E, SE, S
 *   - SW / W / NW are NOT in the sheet — derive by horizontally mirroring
 *     the matching E-side row at render time via `sprite.scale.x = -1`.
 *
 * Frame size = 1122 / 4 ≈ 280.5 px wide × 1402 / 5 ≈ 280.4 px tall.
 * We use floating-point arithmetic and let Pixi's UV math handle the
 * sub-pixel offsets; the result reads as 280×280 cells in practice.
 *
 * Clan → sheet mapping (chosen by color/heraldry match to current 4 active
 * clans; the other 3 are staged for future clans):
 *   clan-iron     → clan-iron-walk.png      green + tree-emblem shield + hammer (forest home)
 *   clan-ember    → clan-ember-walk.png     red + red T-shield + axe (aggressive)
 *   clan-dawn     → clan-dawn-walk.png      gold + sun shield + mace (Dawn Watch / builder)
 *   clan-storm    → clan-storm-walk.png     blue + crescent + hook (tide-warden fisher)
 *   clan-cream    → clan-cream-walk.png     gold/cream tunic + lantern (staged, unused)
 *   clan-stoneroot→ clan-stoneroot-walk.png orange-brown + T-shield + pickaxe (staged)
 *   clan-doomweb  → clan-doomweb-walk.png   purple robe + scroll + star shield (staged)
 *
 * Only the first 4 are wired into `MOCK_CLANS` today. The latter 3 PNGs ship
 * in `src/assets/clansmen/` so future clan additions can reference them
 * without another asset PR.
 */

import { Assets, Rectangle, Texture, type TextureSource } from 'pixi.js';

import clanIronSheet from '../assets/clansmen/clan-iron-walk.png';
import clanEmberSheet from '../assets/clansmen/clan-ember-walk.png';
import clanDawnSheet from '../assets/clansmen/clan-dawn-walk.png';
import clanStormSheet from '../assets/clansmen/clan-storm-walk.png';
import clanCreamSheet from '../assets/clansmen/clan-cream-walk.png';
import clanStonerootSheet from '../assets/clansmen/clan-stoneroot-walk.png';
import clanDoomwebSheet from '../assets/clansmen/clan-doomweb-walk.png';

/** Sheet grid dimensions. Static — all 7 sheets share the same layout. */
export const SHEET_COLS = 4;
export const SHEET_ROWS = 5;
export const SHEET_PIXEL_WIDTH = 1122;
export const SHEET_PIXEL_HEIGHT = 1402;
export const FRAME_WIDTH = SHEET_PIXEL_WIDTH / SHEET_COLS;
export const FRAME_HEIGHT = SHEET_PIXEL_HEIGHT / SHEET_ROWS;

/**
 * Walk-cycle target frame rate. Tunable by Liam — 8 fps matches the existing
 * "bob" idle animation cadence visually.
 */
export const WALK_FPS = 8;
export const WALK_FRAME_MS = 1000 / WALK_FPS;

/** 8-direction bucket. Only the first 5 are stored in sheets; the rest mirror. */
export type ClansmanDirection = 'N' | 'NE' | 'E' | 'SE' | 'S' | 'SW' | 'W' | 'NW';

/** Sheet-stored directions (rows top→bottom). */
const SHEET_ROW_BY_DIRECTION: Record<'N' | 'NE' | 'E' | 'SE' | 'S', number> = {
  N: 0,
  NE: 1,
  E: 2,
  SE: 3,
  S: 4,
};

/**
 * Mirror map for the 3 directions NOT stored in the sheet — value is the
 * E-side row whose textures are reused with `sprite.scale.x = -1`.
 */
const MIRROR_SOURCE: Record<'NW' | 'W' | 'SW', 'NE' | 'E' | 'SE'> = {
  NW: 'NE',
  W: 'E',
  SW: 'SE',
};

/**
 * Per-clan frame data: 5 directions × 4 frames each. Mirrored directions
 * are reused from the corresponding E-side entry via the `flippedFromX`
 * accessor (no extra Texture allocations).
 */
export interface ClansmanFrameSet {
  /** Indexed by sheet-row direction (N/NE/E/SE/S). Each entry is exactly 4 textures. */
  rows: Record<'N' | 'NE' | 'E' | 'SE' | 'S', Texture[]>;
}

/** Returned by `framesForDirection` — caller flips horizontally when `mirror=true`. */
export interface DirectionFrameLookup {
  frames: Texture[];
  /** When true, set sprite.scale.x = -|scale.x| to derive NW/W/SW from NE/E/SE. */
  mirror: boolean;
}

/** Asset path lookup. Bundler-resolved imports keep the assets in the build graph. */
const SHEET_PATH_BY_CLAN: Record<string, string> = {
  'clan-iron': clanIronSheet,
  'clan-ember': clanEmberSheet,
  'clan-dawn': clanDawnSheet,
  'clan-storm': clanStormSheet,
  // Staged (not wired into MOCK_CLANS yet) — keep imports so Vite ships the assets:
  'clan-cream': clanCreamSheet,
  'clan-stoneroot': clanStonerootSheet,
  'clan-doomweb': clanDoomwebSheet,
};

const frameSetCache: Record<string, ClansmanFrameSet | undefined> = {};
const frameSetPromiseCache: Record<string, Promise<ClansmanFrameSet> | undefined> = {};

/** Whether this clanId has an animated walk sheet wired in. */
export function hasClansmanSpriteSheet(clanId: string): boolean {
  return clanId in SHEET_PATH_BY_CLAN;
}

/** Synchronous accessor — returns undefined until load resolves. */
export function getClansmanFrameSet(clanId: string): ClansmanFrameSet | undefined {
  return frameSetCache[clanId];
}

/**
 * Load + slice a clan's sprite sheet into 20 Textures (4 cols × 5 rows).
 * Idempotent + promise-cached so concurrent calls share a single Assets.load.
 *
 * No-op (resolves to a rejected sentinel) for clanIds that have no sheet
 * registered — callers fall back to the legacy single-PNG path.
 */
export function loadClansmanSpriteSheet(clanId: string): Promise<ClansmanFrameSet> {
  if (frameSetCache[clanId]) return Promise.resolve(frameSetCache[clanId]!);
  if (frameSetPromiseCache[clanId]) return frameSetPromiseCache[clanId]!;
  const path = SHEET_PATH_BY_CLAN[clanId];
  if (!path) {
    return Promise.reject(new Error(`clansmanSpriteSheet: no sheet registered for clanId=${clanId}`));
  }
  frameSetPromiseCache[clanId] = Assets.load(path)
    .then((texture: Texture) => {
      const source: TextureSource = texture.source;
      const buildRow = (rowIdx: number): Texture[] => {
        const frames: Texture[] = [];
        for (let col = 0; col < SHEET_COLS; col++) {
          const frame = new Rectangle(
            col * FRAME_WIDTH,
            rowIdx * FRAME_HEIGHT,
            FRAME_WIDTH,
            FRAME_HEIGHT,
          );
          frames.push(new Texture({ source, frame }));
        }
        return frames;
      };
      const set: ClansmanFrameSet = {
        rows: {
          N: buildRow(SHEET_ROW_BY_DIRECTION.N),
          NE: buildRow(SHEET_ROW_BY_DIRECTION.NE),
          E: buildRow(SHEET_ROW_BY_DIRECTION.E),
          SE: buildRow(SHEET_ROW_BY_DIRECTION.SE),
          S: buildRow(SHEET_ROW_BY_DIRECTION.S),
        },
      };
      frameSetCache[clanId] = set;
      return set;
    })
    .catch((err) => {
      frameSetPromiseCache[clanId] = undefined;
      throw err;
    });
  return frameSetPromiseCache[clanId]!;
}

/**
 * Map a screen-space movement vector to an 8-direction bucket. `dx`/`dy` are
 * unnormalized — only the angle matters. Returns 'S' for a zero-vector so
 * callers always have a stable last-direction even when idle.
 *
 * Coordinate convention: PixiJS / canvas — +x right, +y DOWN. So a unit
 * vector pointing screen-up (visually N) has dy<0.
 */
export function directionForVector(dx: number, dy: number): ClansmanDirection {
  if (dx === 0 && dy === 0) return 'S';
  // atan2 returns angle in radians, range (-π, π], measured from +x axis.
  // Convert to degrees + flip y so the result is in screen-up = positive.
  const deg = (Math.atan2(-dy, dx) * 180) / Math.PI;
  // Map [-180, 180] → 1 of 8 22.5°-wide buckets centered on each cardinal.
  // Boundaries at ±22.5, ±67.5, ±112.5, ±157.5.
  if (deg >= -22.5 && deg < 22.5) return 'E';
  if (deg >= 22.5 && deg < 67.5) return 'NE';
  if (deg >= 67.5 && deg < 112.5) return 'N';
  if (deg >= 112.5 && deg < 157.5) return 'NW';
  if (deg >= -67.5 && deg < -22.5) return 'SE';
  if (deg >= -112.5 && deg < -67.5) return 'S';
  if (deg >= -157.5 && deg < -112.5) return 'SW';
  return 'W';
}

/**
 * Pick the right frame array + mirror flag for a target direction. Mirrored
 * directions (NW, W, SW) reuse the matching E-side row's Textures with a
 * horizontal flip — cheaper than baking 3 more sets of mirrored Textures.
 */
export function framesForDirection(
  set: ClansmanFrameSet,
  direction: ClansmanDirection,
): DirectionFrameLookup {
  if (direction === 'N' || direction === 'NE' || direction === 'E' || direction === 'SE' || direction === 'S') {
    return { frames: set.rows[direction], mirror: false };
  }
  const source = MIRROR_SOURCE[direction];
  return { frames: set.rows[source], mirror: true };
}

/**
 * Per-marker animation state. Stored on the LiveClansmanMarker (see
 * WorldMap.tsx) so the tick loop can keep it consistent across syncs.
 */
export interface ClansmanAnimState {
  /** Last known screen position — null until first position update. */
  lastX: number | null;
  lastY: number | null;
  /** Last direction the clansman faced. Holds across idle frames. */
  direction: ClansmanDirection;
  /** Current frame index 0..3. */
  frame: number;
  /** Accumulated wall-clock ms since last frame advance. */
  frameAccumMs: number;
  /** Last performance.now() seen — used to derive frameAccumMs delta. */
  lastTickMs: number;
  /** Whether the clansman moved on the previous update (drives play vs pause). */
  wasWalking: boolean;
}

/** Fresh-state factory — used at marker creation. */
export function makeClansmanAnimState(): ClansmanAnimState {
  return {
    lastX: null,
    lastY: null,
    direction: 'S',
    frame: 0,
    frameAccumMs: 0,
    lastTickMs: performance.now(),
    wasWalking: false,
  };
}

/**
 * Movement-vector epsilon — below this per-update displacement we treat the
 * clansman as idle (pause on frame 0). The map renders at ~0.5–1.5 world
 * units per pixel and the position update fires per render tick (~60Hz), so
 * 0.05 px/tick filters out the sub-pixel bob/jitter but still triggers on
 * actual route motion (which moves at least ~0.5 px/tick at the slowest).
 */
export const WALK_EPSILON_PX = 0.05;
