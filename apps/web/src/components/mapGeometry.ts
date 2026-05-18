/**
 * mapGeometry — pure-data / pure-function constants shared between
 * WorldMap.tsx (PixiJS canvas) and MapGhostLayer.tsx (static HTML ghost).
 *
 * IMPORTANT: this module must remain PixiJS-free. It must never import from
 * `pixi.js`, `@pixi/*`, `pixi-viewport`, or any file that transitively pulls
 * in those packages. The whole point is that MapGhostLayer can import these
 * constants without dragging the PixiJS bundle into its own chunk.
 */

import {
  REGION_EAST_DOCKS,
  REGION_EAST_FARMS,
  REGION_FOREST,
  REGION_MOUNTAINS,
  REGION_WEST_DOCKS,
  REGION_WEST_FARMS,
} from '@clan-world/shared/generated/constants';

// ---------------------------------------------------------------------------
// World dimensions
// ---------------------------------------------------------------------------

export const MAP_WIDTH = 1086;
export const MAP_HEIGHT = 1448;

// ---------------------------------------------------------------------------
// Region centers (normalised world coords)
// Mirrors REGIONS[*].{nx, ny} in WorldMap.tsx.
// ---------------------------------------------------------------------------

export const REGION_CENTERS_BY_KEY: Readonly<Record<string, { nx: number; ny: number }>> = {
  'forest':       { nx: 280 / MAP_WIDTH, ny: 245 / MAP_HEIGHT },
  'mountains':    { nx: 854 / MAP_WIDTH, ny: 245 / MAP_HEIGHT },
  'unicorn-town': { nx: 482 / MAP_WIDTH, ny: 500 / MAP_HEIGHT },
  'west-farms':   { nx: 280 / MAP_WIDTH, ny: 760 / MAP_HEIGHT },
  'east-farms':   { nx: 834 / MAP_WIDTH, ny: 760 / MAP_HEIGHT },
  'west-docks':   { nx: 293 / MAP_WIDTH, ny: 1115 / MAP_HEIGHT },
  'east-docks':   { nx: 874 / MAP_WIDTH, ny: 1115 / MAP_HEIGHT },
  'deep-sea':     { nx: 540 / MAP_WIDTH, ny: 1095 / MAP_HEIGHT },
};

// ---------------------------------------------------------------------------
// On-chain baseRegion id → region key
// Mirrors LIVE_CLAN_REGION_BY_ID in WorldMap.tsx.
// ---------------------------------------------------------------------------

export const LIVE_CLAN_REGION_BY_ID: Readonly<Record<number, string>> = {
  [Number(REGION_FOREST)]: 'forest',
  [Number(REGION_MOUNTAINS)]: 'mountains',
  [Number(REGION_WEST_FARMS)]: 'west-farms',
  [Number(REGION_EAST_FARMS)]: 'east-farms',
  [Number(REGION_WEST_DOCKS)]: 'west-docks',
  [Number(REGION_EAST_DOCKS)]: 'east-docks',
};

// ---------------------------------------------------------------------------
// Base PNG paths by visual index
// Mirrors MOCK_CLANS / LIVE_CLAN_META in WorldMap.tsx.
// ---------------------------------------------------------------------------

export const BASE_PNG_BY_VISUAL_INDEX: readonly string[] = [
  '/bases/cobalt-keep.png',     // Iron Guard
  '/bases/bone-standard.png',   // Ember Hand
  '/bases/gilded-hold.png',     // Dawn Watch
  '/bases/tide-wardens.png',    // Storm Riders
  '/bases/cobalt-keep.png',     // Deployer Keep (uses iron's frame)
];

// Fallback home region by visual index — matches MOCK_CLANS' homeRegion when
// the snapshot doesn't carry a baseRegion (defensive only; live snapshots
// generally do).
export const FALLBACK_HOME_REGION_BY_INDEX: readonly string[] = [
  'forest',
  'mountains',
  'west-farms',
  'east-farms',
  'forest',
];

// ---------------------------------------------------------------------------
// Visual scale step function
// Mirrors the stepwise formula in WorldMap.tsx (NOT a linear function).
// Levels 1-2 → 0.675, 3-4 → 0.81, 5 → 0.9. Keep in sync.
// ---------------------------------------------------------------------------

export function baseVisualScale(level: number): number {
  return level <= 2 ? 0.675 : level <= 4 ? 0.81 : 0.9;
}
