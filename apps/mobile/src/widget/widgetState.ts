// Single source of truth for what the home-screen widget should render.
// Resolves the loaded INFT (real clan / forged / extra) and merges in
// raid state. Invoked both by the foreground push path (App.tsx) and the
// background task handler (when Android wakes the widget for an update).

import {
  getActiveRaid,
  getForgedInfts,
  getLoadedInftId,
  getWalletPubkey,
  realClanIdFromInftId,
} from '../storage';
import { findExtraInft } from '../extraInfts';
import { ARCHETYPE_GLYPHS, type ArchetypeKey } from '../data';
import { REAL_CLAN_DISPLAY } from '../clanData';

export type WidgetState = {
  /** 'raid' overrides everything else; 'loaded' shows the hero card; 'empty' nudges to open the app. */
  mode: 'raid' | 'loaded' | 'empty';
  /** Display copy fields (already resolved) */
  clanName?: string;
  archetypeName?: string;
  archetypeGlyph?: string;
  archetypeColor?: string;
  /** Game state (only on 'loaded' mode for real clans) */
  tick?: number;
  season?: number;
  seasonPct?: number;
  /** Compact resource line, e.g. "GOLD 4 · WOOD 1 · IRON 2" */
  resourceLine?: string;
  /** Raid metadata (only on 'raid' mode) */
  raidVictim?: string;
  raidTick?: number;
};

const MOCK_RESOURCE_BY_CLAN: Record<number, { gold: number; wood: number; iron: number }> = {
  1: { gold: 6, wood: 0, iron: 4 },
  2: { gold: 3, wood: 4, iron: 1 },
  3: { gold: 4, wood: 1, iron: 2 },
  4: { gold: 8, wood: 5, iron: 3 },
};

export const computeWidgetState = (): WidgetState => {
  const raid = getActiveRaid();
  if (raid) {
    return {
      mode: 'raid',
      raidVictim: raid.victim,
      raidTick: raid.tick,
    };
  }

  const loadedId = getLoadedInftId();
  if (!loadedId) return { mode: 'empty' };

  // Real clan loaded → live tick + vault from mock fallbacks (the widget
  // doesn't have access to Convex; it shows last-known state).
  const realClanId = realClanIdFromInftId(loadedId);
  if (realClanId !== null) {
    const display = REAL_CLAN_DISPLAY.find((d) => d.clanId === realClanId);
    if (display) {
      const arch = ARCHETYPE_GLYPHS[display.archetype as ArchetypeKey];
      const res = MOCK_RESOURCE_BY_CLAN[realClanId];
      return {
        mode: 'loaded',
        clanName: display.name,
        archetypeName: arch.name,
        archetypeGlyph: arch.mark,
        archetypeColor: arch.color,
        tick: 263,
        season: 1,
        seasonPct: 0.74,
        resourceLine: res
          ? `GOLD ${res.gold} · WOOD ${res.wood} · IRON ${res.iron}`
          : undefined,
      };
    }
  }

  // Forged INFT loaded → use stored data
  if (loadedId.startsWith('forged-')) {
    const pubkey = getWalletPubkey();
    if (pubkey) {
      const f = getForgedInfts(pubkey).find((entry) => entry.id === loadedId);
      if (f) {
        const arch = ARCHETYPE_GLYPHS[f.archetype];
        return {
          mode: 'loaded',
          clanName: f.name,
          archetypeName: arch.name,
          archetypeGlyph: arch.mark,
          archetypeColor: arch.color,
        };
      }
    }
  }

  // Pre-populated extra Elder loaded
  if (loadedId.startsWith('extra-')) {
    const x = findExtraInft(loadedId);
    if (x) {
      const arch = ARCHETYPE_GLYPHS[x.archetype];
      return {
        mode: 'loaded',
        clanName: x.name,
        archetypeName: arch.name,
        archetypeGlyph: arch.mark,
        archetypeColor: arch.color,
      };
    }
  }

  return { mode: 'empty' };
};
