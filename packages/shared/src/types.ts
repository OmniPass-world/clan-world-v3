import type {
  SnapshotClan,
  SnapshotRegion,
  TickEpoch as SdkTickEpoch,
  WorldSnapshot as SdkWorldSnapshot,
} from '@clan-world/sdk/convex';
export * from '@clan-world/sdk/constants';

// Shared adapter-facing types. Convex-backed snapshot slices are derived from
// @clan-world/sdk; adapter-only command types stay local to shared.

export type Tick = number;

export type TickEpoch = SdkTickEpoch;

export type Region = SnapshotRegion;

export type Clan = Omit<SnapshotClan, 'baseRegion'> & {
  /** ClanWorld region id for live chain rows; legacy fixtures may use region keys. */
  baseRegion?: SnapshotClan['baseRegion'] | string;
};

export type WorldSnapshot = Omit<SdkWorldSnapshot, 'tick' | 'tickEpoch' | 'regions' | 'clans'> & {
  tick: Tick;
  tickEpoch: TickEpoch;
  regions: Region[];
  clans: Clan[];
};

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
