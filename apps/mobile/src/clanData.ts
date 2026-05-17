// Helpers that merge real Convex `worldSnapshot.clans[i]` data with the
// static display registry from cockpit-tokens (4 real clans) and the rich
// mock fallback fields from data.ts (ELO, casualty %, strategy, etc.).
//
// The cockpit web app uses `apps/web/src/styles/cockpit-tokens.ts` as the
// canonical name/archetype/glyph source. We duplicate the 4 entries here so
// the mobile app and the cockpit show the same names/archetypes when the
// user opens the WebView. Refactor to a shared package later if it drifts.

import type {
  ArchetypeKey,
  HistoryRow,
  Inft,
  MemoryOp,
  Movement,
  Strategy,
} from './data';
import type { Doc } from '../../server/convex/_generated/dataModel';
import { ARCHETYPE_GLYPHS } from './data';
import type { ForgedInft } from './storage';

/**
 * Live Convex clan slice from `worldSnapshot.clans[i]`. Subset of fields we
 * actually need for slice 1 — the snapshot has more.
 */
type ConvexSnapshotClan = Doc<'worldSnapshot'>['clans'][number];

export type SnapshotClan = Pick<
  ConvexSnapshotClan,
  | 'id'
  | 'name'
  | 'treasury'
  | 'goldBalance'
  | 'blueprintBalance'
  | 'vaultWood'
  | 'vaultIron'
  | 'vaultWheat'
  | 'vaultFish'
  | 'baseRegion'
  | 'baseLevel'
  | 'wallLevel'
  | 'monumentLevel'
  | 'livingClansmen'
  | 'owner'
>;

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
  /** Resource fallback when the live snapshot has zeros or no data. */
  resources: { gold: number; wood: number; iron: number; wheat: number; fish: number; blueprint: number };
  movements: Movement[];
  history: HistoryRow[];
  kvState: Record<string, string>;
  memory: MemoryOp[];
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
    resources: { gold: 6, wood: 0, iron: 4, wheat: 1, fish: 2, blueprint: 0 },
    movements: [
      { t: 'T263', text: 'Raid · -3 wheat from clan-2', kind: 'live' },
      { t: 'T262', text: 'Bandit ambush · -1 living', kind: 'danger' },
      { t: 'T261', text: 'Spear-shaft drilled · +1 wall', kind: 'live' },
    ],
    history: [
      { season: 18, rank: 4, gold: 4, event: 'Early gambit failed at tick 12.', when: '24d ago' },
      { season: 17, rank: 1, gold: 32, event: 'Burned the eastern village before dawn.', when: '52d ago' },
      { season: 16, rank: 6, gold: 0, event: 'Bandit horde overran the wall.', when: '79d ago' },
    ],
    kvState: {
      mood: 'aggressive',
      trustedClans: '[]',
      grudgeAgainst: '0x21b8…aa44',
      lastWhisperFrom: 'orchestrator',
      pactExpires: 'none',
    },
    memory: [
      { t: 'T263', op: 'WRITE', text: 'tracked clan-2 wheat surplus' },
      { t: 'T262', op: 'READ', text: 'consult strategy.aggression for raid' },
      { t: 'T261', op: 'WRITE', text: 'incremented victory count' },
      { t: 'T260', op: 'READ', text: 'recall best raid window from S17' },
    ],
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
    resources: { gold: 3, wood: 4, iron: 1, wheat: 7, fish: 3, blueprint: 2 },
    movements: [
      { t: 'T263', text: 'Granary built · +1 monument', kind: 'live' },
      { t: 'T261', text: 'Trade · +2 iron from Verdant', kind: 'live' },
      { t: 'T259', text: 'Repaired wall · +1 wall', kind: 'live' },
    ],
    history: [
      { season: 6, rank: 2, gold: 14, event: 'Held the granary through winter.', when: '18d ago' },
      { season: 5, rank: 1, gold: 28, event: 'Won via diplomacy. No casualties.', when: '40d ago' },
      { season: 4, rank: 3, gold: 6, event: 'Stockpiled wheat for the long siege.', when: '62d ago' },
    ],
    kvState: {
      mood: 'measured',
      trustedClans: '[verdant]',
      grudgeAgainst: 'none',
      lastWhisperFrom: 'owner',
      pactExpires: 'T+12',
    },
    memory: [
      { t: 'T263', op: 'WRITE', text: 'noted granary completion at NW' },
      { t: 'T262', op: 'READ', text: 'recall trade ratio with verdant' },
      { t: 'T260', op: 'WRITE', text: 'updated wheat reserve target' },
      { t: 'T258', op: 'READ', text: 'consult strategy.cautious for parley' },
    ],
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
    resources: { gold: 4, wood: 1, iron: 2, wheat: 5, fish: 0, blueprint: 1 },
    movements: [
      { t: 'T259', text: 'Bandit raid · -2 wood', kind: 'danger' },
      { t: 'T258', text: 'Trade · +3 iron from Verdant', kind: 'live' },
      { t: 'T257', text: 'Built granary · +1 monument', kind: 'live' },
    ],
    history: [
      { season: 11, rank: 2, gold: 14, event: 'Held the wall through three sieges.', when: '14d ago' },
      { season: 10, rank: 1, gold: 28, event: 'Won via diplomacy. No casualties.', when: '32d ago' },
      { season: 9, rank: 5, gold: 0, event: 'Betrayed at tick 240.', when: '58d ago' },
      { season: 8, rank: 3, gold: 6, event: 'Bandit raid wiped the granary.', when: '79d ago' },
    ],
    kvState: {
      mood: 'cautious',
      trustedClans: '[verdant, ironguard]',
      grudgeAgainst: '0x9a…f7',
      lastWhisperFrom: 'owner',
      pactExpires: 'T+8',
    },
    memory: [
      { t: 'T259', op: 'WRITE', text: 'noted bandit camp · NE quadrant' },
      { t: 'T258', op: 'READ', text: 'recall pact with verdant · expires T267' },
      { t: 'T256', op: 'WRITE', text: 'updated grudge tally vs 0x9a…f7' },
      { t: 'T255', op: 'READ', text: 'consult strategy.honesty for trade' },
    ],
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
    resources: { gold: 8, wood: 5, iron: 3, wheat: 6, fish: 4, blueprint: 3 },
    movements: [
      { t: 'T263', text: 'Monument raised · +1 monument', kind: 'live' },
      { t: 'T260', text: 'Trade · -2 wood for +3 iron', kind: 'live' },
      { t: 'T258', text: 'Pact extended with Crimson · +5 ticks', kind: 'live' },
    ],
    history: [
      { season: 13, rank: 1, gold: 36, event: 'Won by monument supremacy.', when: '11d ago' },
      { season: 12, rank: 2, gold: 18, event: 'Anchored the western alliance.', when: '36d ago' },
      { season: 11, rank: 1, gold: 30, event: 'Three trade pacts, no battles.', when: '60d ago' },
    ],
    kvState: {
      mood: 'patient',
      trustedClans: '[crimson, ironguard]',
      grudgeAgainst: 'none',
      lastWhisperFrom: 'orchestrator',
      pactExpires: 'T+18',
    },
    memory: [
      { t: 'T263', op: 'WRITE', text: 'monument 5 reached, log of materials' },
      { t: 'T262', op: 'READ', text: 'recall best trade rate with crimson' },
      { t: 'T260', op: 'WRITE', text: 'extended pact with crimson by 5 ticks' },
      { t: 'T257', op: 'READ', text: 'consult strategy.builder for next plan' },
    ],
  },
};

// ── Merge: Convex snapshot + display + mock fallback → Inft (mobile UI shape) ──

const safeNum = (s: string | undefined, fallback = 0): number => {
  if (!s) return fallback;
  const n = Number(s);
  return Number.isFinite(n) ? n : fallback;
};

/** Fall back to mock value when the real number is 0 or missing — keeps
 *  the Hero readable when the live world is sparse / between seasons. */
const realOrFallback = (real: number, mock: number): number =>
  real > 0 ? real : mock;

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
      gold: realOrFallback(safeNum(snapshotClan?.goldBalance), fallback.resources.gold),
      wood: realOrFallback(safeNum(snapshotClan?.vaultWood), fallback.resources.wood),
      iron: realOrFallback(safeNum(snapshotClan?.vaultIron), fallback.resources.iron),
      wheat: realOrFallback(safeNum(snapshotClan?.vaultWheat), fallback.resources.wheat),
      fish: realOrFallback(safeNum(snapshotClan?.vaultFish), fallback.resources.fish),
      blueprint: realOrFallback(
        safeNum(snapshotClan?.blueprintBalance),
        fallback.resources.blueprint,
      ),
    },
    bestMonument: snapshotClan?.monumentLevel ?? fallback.bestMonument,
    bestBase: snapshotClan?.baseLevel ?? fallback.bestBase,
    casualtyPct: fallback.casualtyPct,
    minted: 'live realm',
    owner: snapshotClan?.owner ?? '0x4f2a…81d3',
    teeAttested: true,
    description: fallback.description,
    strategy: fallback.strategy,
    movements: fallback.movements,
    history: fallback.history,
    kvState: fallback.kvState,
    memory: fallback.memory,
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

/** Returns true if this Inft is a hall-only INFT — either a user-forged
 *  Elder or one of the static EXTRA_INFTS. Hall-only INFTs can be loaded
 *  into the Hero but cannot enter the cockpit (they aren't in the live
 *  Convex world). */
export const isForgedInft = (inft: Inft): boolean =>
  inft.id.startsWith('forged-') || inft.id.startsWith('extra-');

/** Returns the integer clanId for a real-clan Inft, or null if it's forged. */
export const realClanIdOf = (inft: Inft): number | null => {
  const m = inft.id.match(/^clan-(\d+)$/);
  return m ? Number(m[1]) : null;
};
