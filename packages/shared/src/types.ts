// Minimal shared types. Wave 0 placeholders — real shapes are defined in the v4
// state schema spec (docs/planning/V1/01 Blockchain Game Spec/
// clanworld_v4_2_state_schema_interface_spec.md). Expand as streams need them.

export type Tick = number;

/** Mirrors ClanWorldConstants.REGION_FOREST in packages/contracts/src/IClanWorld.sol. */
export const REGION_FOREST = 1;

export interface TickEpoch {
  /** Unix seconds when this tick window started. */
  startedAt: number;
  /** Duration of one tick in milliseconds. 20000 for Submission 1, 60000 for Submission 2 live. */
  durationMs: number;
}

export interface Region {
  id: string;
  name: string;
  ownerClanId: string | null;
}

export interface Clan {
  id: string;
  name: string;
  /** Decimal string representation of wei amount; consumers parse with BigInt() if arithmetic needed. */
  treasury: string;
  /** Decimal string representation of 18-decimal game balances indexed from chain. */
  goldBalance?: string;
  blueprintBalance?: string;
  vaultWood?: string;
  vaultIron?: string;
  vaultWheat?: string;
  vaultFish?: string;
  /** ClanWorld region id for live chain rows; legacy fixtures may use region keys. */
  baseRegion?: number | string;
  baseLevel?: number;
  wallLevel?: number;
  monumentLevel?: number;
  livingClansmen?: number;
  owner?: string;
  /** Raw indexed ClanFullView.clansmen rows from chain, used by live map rendering. */
  clansmen?: unknown[];
}

export interface WorldSnapshot {
  tick: Tick;
  tickEpoch: TickEpoch;
  regions: Region[];
  clans: Clan[];
  /**
   * Season + winter timers surfaced from `worldSnapshot` (Phase 4.4+).
   * All optional for backward compat with legacy rows that pre-date the
   * persisted-fields migration. When `getSnapshot()` returns these, they
   * are authoritative chain values from `_world.*` — see
   * `apps/server/convex/getSnapshot.ts` and ClanWorld._worldStateView.
   */
  currentSeasonNumber?: number;
  seasonStartTick?: number;
  seasonEndTick?: number;
  winterActive?: boolean;
  winterStartsAtTick?: number;
  winterEndsAtTick?: number;
}

export interface ClanFullView {
  clan: Clan;
  controlledRegions: Region[];
  /** Pending orders this clan submitted for the next tick. */
  pendingOrders: ClanOrder[];
  /** Inbound whispers since the last view. */
  whispers: Whisper[];
}

export interface ClanOrder {
  kind: 'mission' | 'transfer' | 'mint';
  payload: Record<string, unknown>;
}

export interface Whisper {
  fromClanId: string;
  toClanId: string;
  text: string;
  tick: Tick;
}
