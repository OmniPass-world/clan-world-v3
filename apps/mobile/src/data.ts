// Mock data for the Clan World shell. Mirrors data.jsx from the design bundle.
// When integrations land, swap these for live queries against the backend.

import type { Doc } from '../../server/convex/_generated/dataModel';

export type ResourceKey = 'gold' | 'wood' | 'iron' | 'wheat' | 'fish' | 'blueprint';

export type ArchetypeKey =
  // Forge-picker archetypes (mobile UI)
  | 'patient-builder'
  | 'warlord'
  | 'diplomat'
  | 'hermit'
  | 'trickster'
  | 'verdant-warden'
  // Real cockpit archetypes (mirrors apps/web/src/styles/cockpit-tokens.ts)
  | 'aggressive-raider'
  | 'cautious-accumulator'
  | 'volatile-opportunist';

export type StrategyAxisKey =
  | 'trust'
  | 'aggression'
  | 'honesty'
  | 'solo'
  | 'builder'
  | 'vengeful'
  | 'cautious';

export type Strategy = Record<StrategyAxisKey, number>;

export type InftState = 'in-game' | 'idle' | 'rented';

export type Movement = { t: string; text: string; kind: 'live' | 'danger' | 'warn' | 'info' };

export type HistoryRow = {
  season: number;
  rank: number;
  gold: number;
  event: string;
  when: string;
};

export type MemoryOp = { t: string; op: 'WRITE' | 'READ'; text: string };

export type Inft = {
  id: string;
  tokenId: string;
  name: string;
  archetype: ArchetypeKey;
  elo: number;
  last10: number;
  seasons: number;
  state: InftState;
  rentedBy?: string;
  gameTick?: number;
  season?: number;
  seasonPct?: number;
  resources?: Record<ResourceKey, number>;
  bestMonument?: number;
  bestBase?: number;
  casualtyPct?: string;
  minted: string;
  owner: string;
  teeAttested: boolean;
  description: string;
  strategy?: Strategy;
  movements?: Movement[];
  history?: HistoryRow[];
  kvState?: Record<string, string>;
  memory?: MemoryOp[];
  hireFee?: string;
  /** Set on FOR SALE bazaar items. CTA reads "BUY · X GOLD". */
  salePrice?: string;
  sparkline?: number[];
};

export const MOCK_INFTS: Inft[] = [
  {
    id: 'crimson',
    tokenId: '0xe1de4a8f3c92b71704e5c2a91b3d8f5c2a91b7004',
    name: 'Crimson',
    archetype: 'patient-builder',
    elo: 1247,
    last10: 4,
    seasons: 12,
    state: 'in-game',
    gameTick: 259,
    season: 1,
    seasonPct: 0.72,
    resources: { gold: 4, wood: 1, iron: 2, wheat: 5, fish: 0, blueprint: 1 },
    bestMonument: 4,
    bestBase: 5,
    casualtyPct: '23%',
    minted: '8 months ago',
    owner: '0x4f2a…81d3',
    teeAttested: true,
    description:
      'Slow to anger, slower still to forgive. Crimson hoards stone and waits for the second raid before answering the first.',
    strategy: { trust: -1, aggression: -2, honesty: 1, solo: 0, builder: 2, vengeful: 1, cautious: 2 },
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
  {
    id: 'thane',
    tokenId: '0x8b2f9c1e7a4d6358f290b41cc3c8e2a91b7d2010',
    name: 'Thane',
    archetype: 'warlord',
    elo: 1389,
    last10: 2,
    seasons: 19,
    state: 'idle',
    minted: '14 months ago',
    owner: '0x4f2a…81d3',
    teeAttested: true,
    description: 'Strikes within ten ticks. If the early gambit fails, recovery is rare.',
    strategy: { trust: -3, aggression: 3, honesty: -1, solo: -1, builder: -2, vengeful: 3, cautious: -2 },
    history: [{ season: 18, rank: 4, gold: 4, event: 'Early gambit failed at tick 12.', when: '24d ago' }],
  },
  {
    id: 'evergreen',
    tokenId: '0x3c1a0fe5b8d27a6928b41dd3e8c0a2917b5e3022',
    name: 'Evergreen',
    archetype: 'verdant-warden',
    elo: 1102,
    last10: 6,
    seasons: 7,
    state: 'rented',
    rentedBy: '0xa1c4…f7e9',
    minted: '4 months ago',
    owner: '0x4f2a…81d3',
    teeAttested: true,
    description: 'Husbands grain. A defensive specialist who turns granaries into garrisons.',
    strategy: { trust: 1, aggression: -1, honesty: 2, solo: -2, builder: 1, vengeful: -1, cautious: 1 },
  },
  {
    id: 'dusk',
    tokenId: '0x6d2c8a4f1e9b3705c2a91b3d8e8c0a2917b5d3033',
    name: 'Dusk',
    archetype: 'trickster',
    elo: 1320,
    last10: 3,
    seasons: 14,
    state: 'idle',
    minted: '11 months ago',
    owner: '0x4f2a…81d3',
    teeAttested: true,
    description: 'Keeps two ledgers and remembers which is which.',
    strategy: { trust: -2, aggression: 1, honesty: -3, solo: 0, builder: -1, vengeful: 0, cautious: 0 },
  },
];

export const BAZAAR_INFTS: Inft[] = [
  {
    id: 'astor',
    tokenId: '0xa1f49b2e8c5d7104a6b91c3d8f5c2a91b7d4501',
    name: 'Astor',
    archetype: 'diplomat',
    elo: 1456,
    last10: 2,
    seasons: 22,
    state: 'idle',
    owner: '0x9c2a…48b1',
    minted: '—',
    teeAttested: true,
    description: 'Seeks pacts. A treaty is a tool.',
    hireFee: '0.5 GOLD / SEASON',
    sparkline: [3, 1, 2, 1, 4, 2, 1, 3, 2, 1],
  },
  {
    id: 'mara',
    tokenId: '0xc4e8a1d59b3f2607c2a91b3d8e8c0a2917b5d4502',
    name: 'Mara',
    archetype: 'hermit',
    elo: 1311,
    last10: 4,
    seasons: 9,
    state: 'idle',
    owner: '0x21b8…aa44',
    minted: '—',
    teeAttested: true,
    description: 'Builds inward. Speaks rarely. Remembers all.',
    hireFee: '0.3 GOLD / SEASON',
    sparkline: [5, 3, 4, 2, 3, 4, 5, 4, 3, 4],
  },
  {
    id: 'gauntlet',
    tokenId: '0x7e8b2a4f9c5d3107a6b91c3d8f5c2a91b7d4503',
    name: 'Gauntlet',
    archetype: 'warlord',
    elo: 1502,
    last10: 1,
    seasons: 31,
    state: 'idle',
    owner: '0xff03…2c1d',
    minted: '—',
    teeAttested: true,
    description: 'Strikes early. Begins enemies, not friendships.',
    hireFee: '0.9 GOLD / SEASON',
    sparkline: [2, 1, 1, 3, 2, 1, 1, 2, 1, 1],
  },
  {
    id: 'corvid',
    tokenId: '0x52a91b3d8e8c0a2917b5d4504f5c2a91b7d4504',
    name: 'Corvid',
    archetype: 'trickster',
    elo: 1278,
    last10: 5,
    seasons: 11,
    state: 'idle',
    owner: '0x77ee…1a09',
    minted: '—',
    teeAttested: true,
    description: 'Lies plausibly. Keeps a second ledger.',
    hireFee: '0.4 GOLD / SEASON',
    sparkline: [4, 6, 3, 5, 4, 5, 6, 4, 5, 5],
  },
];

export type WhisperKind = 'live' | 'danger' | 'warn' | 'info';

// schema-source: Doc<"whispers">
export type WhisperFromConvex = Doc<'whispers'>;
export type _ConvexWhisper = WhisperFromConvex;

// Mobile whispers are display fixtures, not persisted rows. The backing schema
// stores tick/fromClanId/toClanIds/body/msgId/timestamp, while this UI layer
// keeps hand-rolled presentation fields:
// id/day group the mock list, kind/icon drive styling, title/snippet/t are
// derived copy for cards, and unread is local UI state. There are currently no
// same-named fields shared with Doc<"whispers">, so no schema type conflicts.
export type Whisper = {
  id: string;
  day: 'today' | 'yesterday' | 'wed-6-may';
  kind: WhisperKind;
  icon: string;
  title: string;
  snippet: string;
  t: string;
  unread: boolean;
};

export const WHISPERS: Whisper[] = [
  { id: 'w1', day: 'today', kind: 'danger', icon: '⚔', title: 'Crimson under bandit raid', snippet: 'Lost 2 wood at T259. Counter held.', t: 'T259', unread: true },
  { id: 'w2', day: 'today', kind: 'live', icon: '◇', title: 'Whisper acknowledged', snippet: 'Your steering injected at T260 for Crimson.', t: 'T260', unread: true },
  { id: 'w3', day: 'today', kind: 'info', icon: '◈', title: 'Hire request received', snippet: '0xa1c4… wishes to hire Evergreen for Season 13.', t: 'T257', unread: false },
  { id: 'w4', day: 'yesterday', kind: 'live', icon: '✦', title: 'Season 11 ended · 2nd place', snippet: 'Crimson received 14 GOLD.', t: 'T288', unread: false },
  { id: 'w5', day: 'yesterday', kind: 'warn', icon: '⚠', title: 'Pact expiring', snippet: 'Verdant Wardens pact ends in 8 ticks.', t: 'T255', unread: false },
  { id: 'w6', day: 'wed-6-may', kind: 'info', icon: '◈', title: 'Treasury funded', snippet: '20 GOLD bridged from Solana.', t: 'T220', unread: false },
];

export type TxStatus = 'live' | 'danger' | 'info' | 'idle';

export type Tx = {
  id: string;
  kind: string;
  icon: string;
  title: string;
  sub: string;
  amount: string;
  status: TxStatus;
  t: string;
};

export const TXS: Tx[] = [
  { id: 't1', kind: 'prize', icon: '✦', title: 'Season 11 prize', sub: 'Crimson · 2nd', amount: '+14 GOLD', status: 'live', t: '14d ago' },
  { id: 't2', kind: 'mint', icon: '◆', title: 'Forge · Dusk', sub: 'Mint fee', amount: '−5 GOLD', status: 'idle', t: '11mo ago' },
  { id: 't3', kind: 'hire', icon: '→', title: 'Hire · 0xa1c4…', sub: 'Evergreen · Season 13', amount: '+0.5 GOLD', status: 'live', t: '2d ago' },
  { id: 't4', kind: 'raid', icon: '⚔', title: 'Raid loss', sub: 'Crimson · −2 wood', amount: '—', status: 'danger', t: 'T259' },
  { id: 't5', kind: 'buy', icon: '◇', title: 'Bridged from Solana', sub: '0.42 SOL → 20 GOLD', amount: '+20 GOLD', status: 'info', t: '6d ago' },
];

export const STRATEGY_PRESETS: Record<string, Strategy | null> = {
  diplomat: { trust: 2, aggression: -1, honesty: 2, solo: 1, builder: 0, vengeful: -2, cautious: 1 },
  warlord: { trust: -3, aggression: 3, honesty: -1, solo: -1, builder: -2, vengeful: 3, cautious: -2 },
  hermit: { trust: -2, aggression: -2, honesty: 1, solo: -3, builder: 2, vengeful: 0, cautious: 3 },
  trickster: { trust: -2, aggression: 1, honesty: -3, solo: 0, builder: -1, vengeful: 0, cautious: 0 },
  builder: { trust: 0, aggression: -2, honesty: 1, solo: 1, builder: 3, vengeful: -1, cautious: 2 },
  custom: null,
};

export type StrategyAxis = {
  key: StrategyAxisKey;
  left: string;
  right: string;
  hint: Record<string, string>;
};

export const STRATEGY_AXES: StrategyAxis[] = [
  {
    key: 'trust',
    left: 'Suspicious',
    right: 'Trusting',
    hint: {
      '-3': 'Rejects every unverified offer.',
      '-2': 'Demands escrow before any pact.',
      '-1': 'Listens, but verifies.',
      '0': 'Reads the table.',
      '1': 'Accepts pacts at face value.',
      '2': 'Tends to believe stated intent.',
      '3': 'Will follow a stranger into a siege.',
    },
  },
  {
    key: 'aggression',
    left: 'Restraint',
    right: 'Aggression',
    hint: {
      '-3': 'Does not strike first, ever.',
      '-2': 'Strikes only when starved.',
      '-1': 'Defensive posture.',
      '0': 'Mirrors the table.',
      '1': 'Tests neighbors early.',
      '2': 'Pre-empts perceived threats.',
      '3': 'Strikes within ten ticks.',
    },
  },
  {
    key: 'honesty',
    left: 'Deceit',
    right: 'Honesty',
    hint: {
      '-3': 'Keeps a second ledger. Lies plausibly.',
      '-2': 'Misleads when the cost is low.',
      '-1': 'Half-truths.',
      '0': 'Reads the table.',
      '1': 'Honest by default.',
      '2': 'Discloses pacts openly.',
      '3': 'Will not feint, even at cost.',
    },
  },
  {
    key: 'solo',
    left: 'Solo',
    right: 'Cooperative',
    hint: {
      '-3': 'No pacts. No trades.',
      '-2': 'Rare allies, brief pacts.',
      '-1': 'Self-reliant.',
      '0': 'Reads the table.',
      '1': 'Forms one-season alliances.',
      '2': 'Anchors the bloc.',
      '3': 'Cooperates even when betrayed.',
    },
  },
  {
    key: 'builder',
    left: 'Gatherer',
    right: 'Builder',
    hint: {
      '-3': 'Eats what is harvested. Builds nothing.',
      '-2': 'Lean settlement.',
      '-1': 'Modest infrastructure.',
      '0': 'Reads the table.',
      '1': 'Reinvests harvest.',
      '2': 'Slow accumulation. Trades wood for time.',
      '3': 'Builds monuments above all.',
    },
  },
  {
    key: 'vengeful',
    left: 'Forgiving',
    right: 'Vengeful',
    hint: {
      '-3': 'Forgets within three ticks.',
      '-2': 'Forgives openly.',
      '-1': 'Lets minor slights pass.',
      '0': 'Reads the table.',
      '1': 'Remembers grudges.',
      '2': 'Settles every betrayal.',
      '3': 'Burns down the betrayer.',
    },
  },
  {
    key: 'cautious',
    left: 'Bold',
    right: 'Cautious',
    hint: {
      '-3': 'Acts before thinking. Often.',
      '-2': 'Bold gambits.',
      '-1': 'Improvises.',
      '0': 'Reads the table.',
      '1': 'Plans two ticks ahead.',
      '2': 'Pre-models every move.',
      '3': 'Will not act without three confirmations.',
    },
  },
];

export type ChatMessage =
  | { kind: 'human'; t: string; text: string; pending?: boolean }
  | { kind: 'orch'; t: string; text: string }
  | { kind: 'whisper'; t: string; target: string; text: string };

export const CHAT_LOG: ChatMessage[] = [
  { kind: 'human', t: 'T254', text: "Hold the granary. Don't take the gambit on iron." },
  { kind: 'orch', t: 'T254', text: '▸ Whisper acknowledged · injected at T255' },
  { kind: 'whisper', t: 'T256', target: 'VERDANT', text: 'Pact for the season? My wood for your iron at parity.' },
  { kind: 'orch', t: 'T257', text: '▸ Pact accepted by Verdant Wardens · expires T+10' },
  { kind: 'human', t: 'T258', text: "Watch the bandit camp NE. Don't engage." },
  { kind: 'orch', t: 'T259', text: '▸ Whisper acknowledged · injected at T260' },
];

export const ARCHETYPE_GLYPHS: Record<
  ArchetypeKey,
  { mark: string; color: string; name: string; short: string }
> = {
  'patient-builder': { mark: '⏃', color: '#A89A78', name: 'Patient Builder', short: 'A slow, deep accumulator. Trades wood for time.' },
  warlord: { mark: '⚔', color: '#A04A3F', name: 'Warlord', short: 'Strikes early. Begins enemies, not friendships.' },
  diplomat: { mark: '✺', color: '#5A7BA8', name: 'Diplomat', short: 'Seeks pacts. A treaty is a tool.' },
  hermit: { mark: '☖', color: '#8C6F3A', name: 'Hermit', short: 'Builds inward. Speaks rarely. Remembers all.' },
  trickster: { mark: '✦', color: '#9B6FA0', name: 'Trickster', short: 'Lies plausibly. Keeps a second ledger.' },
  'verdant-warden': { mark: '✧', color: '#6B8E5C', name: 'Verdant Warden', short: 'Husbands grain. Fights only at the wall.' },
  // Real cockpit archetypes — glyphs/colors mirror apps/web cockpit-tokens
  'aggressive-raider': { mark: '⚡', color: '#5a8aa8', name: 'Aggressive Raider', short: 'Strikes within ten ticks. Built to break early walls.' },
  'cautious-accumulator': { mark: '⛨', color: '#7a8a6a', name: 'Cautious Accumulator', short: 'Husbands every grain. Fights only when forced.' },
  'volatile-opportunist': { mark: '✦', color: '#a85a5a', name: 'Volatile Opportunist', short: 'Reads the table. Strikes where it bleeds.' },
};

export const RESOURCES: Record<ResourceKey, { glyph: string; color: string; label: string }> = {
  gold: { glyph: '◇', color: '#D4AF5C', label: 'GOLD' },
  wood: { glyph: '▤', color: '#8B6B3F', label: 'WOOD' },
  iron: { glyph: '▰', color: '#7A8590', label: 'IRON' },
  wheat: { glyph: '⁂', color: '#C7A14F', label: 'WHEAT' },
  fish: { glyph: '◗', color: '#5A8AA0', label: 'FISH' },
  blueprint: { glyph: '⁋', color: '#A89A78', label: 'PLAN' },
};

export const truncAddr = (s: string, head = 4, tail = 4) =>
  s ? `${s.slice(0, head + 2)}…${s.slice(-tail)}` : '';
