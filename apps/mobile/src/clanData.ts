// Helpers that merge real Convex `worldSnapshot.clans[i]` data with the
// static display registry from cockpit-tokens (4 real clans) and the rich
// mock fallback fields from data.ts (ELO, casualty %, strategy, etc.).
//
// The cockpit web app uses `apps/web/src/styles/cockpit-tokens.ts` as the
// canonical name/archetype/glyph source. We duplicate the 4 entries here so
// the mobile app and the cockpit show the same names/archetypes when the
// user opens the WebView. Refactor to a shared package later if it drifts.

import type { ArchetypeKey, Inft, Strategy } from './data';
import { ARCHETYPE_GLYPHS } from './data';
import type { ForgedInft } from './storage';

/**
 * Live Convex clan slice from `worldSnapshot.clans[i]`. Subset of fields we
 * actually need for slice 1 — the snapshot has more.
 */
export type SnapshotClan = {
  id: string;
  name: string;
  treasury: string;
  goldBalance?: string;
  blueprintBalance?: string;
  vaultWood?: string;
  vaultIron?: string;
  vaultWheat?: string;
  vaultFish?: string;
  baseRegion?: number;
  baseLevel?: number;
  wallLevel?: number;
  monumentLevel?: number;
  livingClansmen?: number;
  owner?: string;
};

/** Subset of the worldSnapshot we read for the Hero. */
export type SnapshotForHero = {
  tick: number;
  currentSeasonNumber?: number;
  seasonStartTick?: number;
  seasonEndTick?: number;
  clans: SnapshotClan[];
};

// ── Real-clan display registry (duplicated from apps/web cockpit-tokens) ──

export type RealClanDisplay = {
  clanId: number;
  name: string;
  archetype: ArchetypeKey;
  glyph: string;
  accent: string;
};

export const REAL_CLAN_DISPLAY: RealClanDisplay[] = [
  { clanId: 1, name: 'Storm Riders',    archetype: 'aggressive-raider',     glyph: '⚡', accent: '#5a8aa8' },
  { clanId: 2, name: 'Iron Guard',      archetype: 'cautious-accumulator',  glyph: '⛨', accent: '#7a8a6a' },
  { clanId: 3, name: 'Crimson',         archetype: 'volatile-opportunist',  glyph: '✦', accent: '#a85a5a' },
  { clanId: 4, name: 'Verdant Wardens', archetype: 'patient-builder',       glyph: '❦', accent: '#6aa888' },
];

// ── Mock fallback per real clanId ──
//
// These are the rich narrative fields that the existing data.ts mocks supply
// but Convex/chain doesn't (yet). Each real clan inherits a flavor pulled
// roughly from its archetype. Swap to real values when ELO etc. land.

const ZERO_STRAT: Strategy = {
  trust: 0, aggression: 0, honesty: 0, solo: 0,
  builder: 0, vengeful: 0, cautious: 0,
};

export type MockFallback = {
  description: string;
  elo: number;
  last10: number;
  seasons: number;
  bestMonument: number;
  bestBase: number;
  casualtyPct: string;
  strategy: Strategy;
};

export const MOCK_FALLBACKS: Record<number, MockFallback> = {
  1: {
    description:
      'Strikes within ten ticks. If the early gambit fails, recovery is rare.',
    elo: 1389,
    last10: 2,
    seasons: 19,
    bestMonument: 2,
    bestBase: 4,
    casualtyPct: '47%',
    strategy: { trust: -3, aggression: 3, honesty: -1, solo: -1, builder: -2, vengeful: 3, cautious: -2 },
  },
  2: {
    description:
      'Husbands grain. A defensive specialist who turns granaries into garrisons.',
    elo: 1102,
    last10: 6,
    seasons: 7,
    bestMonument: 3,
    bestBase: 5,
    casualtyPct: '12%',
    strategy: { trust: 1, aggression: -1, honesty: 2, solo: -2, builder: 1, vengeful: -1, cautious: 1 },
  },
  3: {
    description:
      'Slow to anger, slower still to forgive. Crimson hoards stone and waits for the second raid before answering the first.',
    elo: 1247,
    last10: 4,
    seasons: 12,
    bestMonument: 4,
    bestBase: 5,
    casualtyPct: '23%',
    strategy: { trust: -1, aggression: -2, honesty: 1, solo: 0, builder: 2, vengeful: 1, cautious: 2 },
  },
  4: {
    description:
      'Patient, methodical, generous in trade. The Verdant Wardens win seasons by building monuments while others bleed.',
    elo: 1320,
    last10: 3,
    seasons: 14,
    bestMonument: 5,
    bestBase: 4,
    casualtyPct: '14%',
    strategy: { trust: 2, aggression: -2, honesty: 2, solo: -1, builder: 3, vengeful: -2, cautious: 2 },
  },
};

// ── Merge: Convex snapshot + display + mock fallback → Inft (mobile UI shape) ──

const safeNum = (s: string | undefined, fallback = 0): number => {
  if (!s) return fallback;
  const n = Number(s);
  return Number.isFinite(n) ? n : fallback;
};

export const mergeRealClan = (
  display: RealClanDisplay,
  snapshotClan: SnapshotClan | undefined,
  snapshot: SnapshotForHero | null,
): Inft => {
  const fallback = MOCK_FALLBACKS[display.clanId]!;
  const arch = ARCHETYPE_GLYPHS[display.archetype];

  const seasonStart = snapshot?.seasonStartTick ?? 0;
  const seasonEnd = snapshot?.seasonEndTick ?? seasonStart + 100;
  const seasonRange = Math.max(1, seasonEnd - seasonStart);
  const seasonPct = snapshot
    ? Math.max(0, Math.min(1, (snapshot.tick - seasonStart) / seasonRange))
    : 0;

  return {
    id: `clan-${display.clanId}`,
    tokenId: `0xclan${display.clanId.toString().padStart(40, '0')}`,
    name: display.name,
    archetype: display.archetype,
    elo: fallback.elo,
    last10: fallback.last10,
    seasons: fallback.seasons,
    state: snapshotClan ? 'in-game' : 'idle',
    gameTick: snapshot?.tick,
    season: snapshot?.currentSeasonNumber,
    seasonPct,
    resources: {
      gold: safeNum(snapshotClan?.goldBalance),
      wood: safeNum(snapshotClan?.vaultWood),
      iron: safeNum(snapshotClan?.vaultIron),
      wheat: safeNum(snapshotClan?.vaultWheat),
      fish: safeNum(snapshotClan?.vaultFish),
      blueprint: safeNum(snapshotClan?.blueprintBalance),
    },
    bestMonument: snapshotClan?.monumentLevel ?? fallback.bestMonument,
    bestBase: snapshotClan?.baseLevel ?? fallback.bestBase,
    casualtyPct: fallback.casualtyPct,
    minted: 'live realm',
    owner: snapshotClan?.owner ?? '0x4f2a…81d3',
    teeAttested: true,
    description: arch.short || fallback.description,
    strategy: fallback.strategy,
    movements: [],
    history: [],
  };
};

/** Convert a forged INFT record (MMKV) into the mobile UI's `Inft` shape. */
export const forgedToInft = (forged: ForgedInft): Inft => {
  const arch = ARCHETYPE_GLYPHS[forged.archetype];
  return {
    id: forged.id,
    tokenId: forged.mintTxSig,
    name: forged.name,
    archetype: forged.archetype,
    elo: 1000,
    last10: 0,
    seasons: 0,
    state: 'idle',
    minted: new Date(forged.createdAt).toLocaleDateString(),
    owner: forged.pubkey,
    teeAttested: true,
    description: arch.short,
    strategy: forged.strategy,
    movements: [],
    history: [],
  };
};

/** Returns true if this Inft is a forged-only INFT (not in the live world). */
export const isForgedInft = (inft: Inft): boolean => inft.id.startsWith('forged-');

/** Returns the integer clanId for a real-clan Inft, or null if it's forged. */
export const realClanIdOf = (inft: Inft): number | null => {
  const m = inft.id.match(/^clan-(\d+)$/);
  return m ? Number(m[1]) : null;
};
