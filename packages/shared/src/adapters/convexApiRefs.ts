import type { FunctionReference } from 'convex/server';
import { anyApi } from 'convex/server';
import type { WorldSnapshot } from '../types';

type PublicQuery<Args extends Record<string, unknown>, Result> = FunctionReference<'query', 'public', Args, Result>;
type PublicMutation<Args extends Record<string, unknown>, Result = null> = FunctionReference<'mutation', 'public', Args, Result>;

type SeedWhisperArgs = {
  secret: string;
  tick: number;
  fromClanId: number;
  toClanIds: number[];
  body: string;
  txHash?: string;
};

type SeedOrchEventArgs = {
  secret: string;
  tick: number;
  kind: 'world_event' | 'directive' | 'narration';
  body: string;
  targetClanId?: number;
};

type SeedHumanSteeringArgs = {
  secret: string;
  tick: number;
  targetClanId: number;
  body: string;
  sentBy?: string;
};

type SeedBulletinArgs = {
  secret: string;
  clanId: number;
  slot: number;
  body: string;
  dataHash?: string;
  txHash?: string;
};

type ClanWorldConvexApi = {
  getSnapshot: {
    getSnapshot: PublicQuery<Record<string, never>, WorldSnapshot>;
  };
  comms: {
    seedWhisper: PublicMutation<SeedWhisperArgs>;
    seedOrchEvent: PublicMutation<SeedOrchEventArgs>;
    seedHumanSteering: PublicMutation<SeedHumanSteeringArgs>;
  };
  bulletins: {
    seedBulletin: PublicMutation<SeedBulletinArgs>;
  };
};

// shared cannot import apps/server/convex/_generated/api: the server already
// depends on shared, and crossing that package boundary would create a cycle.
// Keep this narrow shim aligned with the public Convex functions this adapter
// calls, and let ConvexHttpClient enforce args/results from here onward.
export const convexApiRefs = anyApi as unknown as ClanWorldConvexApi;
