import { ConvexHttpClient } from 'convex/browser';
import type { ClanFullView, Whisper, WorldSnapshot } from '../types';
import { readEnv } from './_env';
import { convexApiRefs } from './convexApiRefs';

export interface IConvexClient {
  getSnapshot(): Promise<WorldSnapshot>;
  getClanFullView(clanId: string): Promise<ClanFullView>;
  postLog(level: 'info' | 'warn' | 'error', message: string): Promise<void>;
  subscribeWhispers(clanId: string, onWhisper: (w: Whisper) => void): () => void;

  // Cockpit Comms write-side. Each method is best-effort: callers wrap in
  // try/catch + treat failures as non-fatal so the cockpit display stays
  // decoupled from the underlying domain action (a chain whisper succeeds
  // even if the cockpit feed write fails).
  postWhisper(args: {
    tick: number;
    fromClanId: number;
    toClanIds: number[];
    body: string;
    txHash?: string;
  }): Promise<void>;
  postOrchEvent(args: {
    tick: number;
    kind: 'world_event' | 'directive' | 'narration';
    body: string;
    targetClanId?: number;
  }): Promise<void>;
  postHumanSteering(args: {
    tick: number;
    targetClanId: number;
    body: string;
    sentBy?: string;
  }): Promise<void>;
  postBulletin(args: {
    clanId: number;
    slot: number;
    body: string;
    dataHash?: string;
    txHash?: string;
  }): Promise<void>;
}

class StubConvexClient implements IConvexClient {
  async getSnapshot(): Promise<WorldSnapshot> {
    return {
      tick: 0,
      tickEpoch: { startedAt: 0, durationMs: 20_000 },
      regions: [],
      clans: [],
    };
  }
  async getClanFullView(clanId: string): Promise<ClanFullView> {
    return {
      clan: { id: clanId, name: `Stub Clan ${clanId}`, treasury: '0' },
      controlledRegions: [],
      pendingOrders: [],
      whispers: [],
    };
  }
  async postLog(_level: 'info' | 'warn' | 'error', _message: string): Promise<void> {
    // no-op stub
  }
  subscribeWhispers(_clanId: string, _onWhisper: (w: Whisper) => void): () => void {
    return () => {
      // no-op unsubscribe
    };
  }

  async postWhisper(_args: { tick: number; fromClanId: number; toClanIds: number[]; body: string; txHash?: string }): Promise<void> {}
  async postOrchEvent(_args: { tick: number; kind: 'world_event' | 'directive' | 'narration'; body: string; targetClanId?: number }): Promise<void> {}
  async postHumanSteering(_args: { tick: number; targetClanId: number; body: string; sentBy?: string }): Promise<void> {}
  async postBulletin(_args: { clanId: number; slot: number; body: string; dataHash?: string; txHash?: string }): Promise<void> {}
}

const getSnapshotRef = convexApiRefs.getSnapshot.getSnapshot;
const seedWhisperRef = convexApiRefs.comms.seedWhisper;
const seedOrchEventRef = convexApiRefs.comms.seedOrchEvent;
const seedHumanSteeringRef = convexApiRefs.comms.seedHumanSteering;
const seedBulletinRef = convexApiRefs.bulletins.seedBulletin;

class RealConvexClient implements IConvexClient {
  private readonly http: ConvexHttpClient;

  constructor(url: string) {
    this.http = new ConvexHttpClient(url);
  }

  async getSnapshot(): Promise<WorldSnapshot> {
    try {
      return await this.http.query(getSnapshotRef);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      if (msg.includes('not found') || msg.includes('Could not find')) {
        console.warn('[RealConvexClient] getSnapshot query not found on server, using stub data');
        return { tick: 0, tickEpoch: { startedAt: 0, durationMs: 20_000 }, regions: [], clans: [] };
      }
      throw err;
    }
  }

  async getClanFullView(clanId: string): Promise<ClanFullView> {
    return {
      clan: { id: clanId, name: `Stub Clan ${clanId}`, treasury: '0' },
      controlledRegions: [],
      pendingOrders: [],
      whispers: [],
    };
  }

  async postLog(_level: 'info' | 'warn' | 'error', _message: string): Promise<void> {
    // Phase 4: postLog via Convex not yet used by CLI path
  }

  subscribeWhispers(_clanId: string, _onWhisper: (w: Whisper) => void): () => void {
    // ConvexHttpClient is non-reactive; WebSocket subscriptions need ConvexClient
    return () => {};
  }

  // Cockpit Comms write-side — best-effort, swallow + warn on failure so the
  // domain operation that triggered the post (chain whisper, orchestrator
  // tick, etc.) is never blocked by a Convex outage.
  //
  // Each call attaches `secret: INDEXER_SECRET` (read from env) so the
  // server-side mutation passes its `requireIndexerSecret` gate. If the env
  // var is missing the call is short-circuited with a single warn — same
  // shape the user sees when the deployment URL is not configured.
  private indexerSecret(): string | undefined {
    return readEnv('INDEXER_SECRET');
  }

  async postWhisper(args: { tick: number; fromClanId: number; toClanIds: number[]; body: string; txHash?: string }): Promise<void> {
    const secret = this.indexerSecret();
    if (!secret) {
      console.warn('[ConvexClient] postWhisper skipped: INDEXER_SECRET not set');
      return;
    }
    try {
      await this.http.mutation(seedWhisperRef, { secret, ...args });
    } catch (err) {
      console.warn('[ConvexClient] postWhisper failed (non-fatal):', err);
    }
  }

  async postOrchEvent(args: { tick: number; kind: 'world_event' | 'directive' | 'narration'; body: string; targetClanId?: number }): Promise<void> {
    const secret = this.indexerSecret();
    if (!secret) {
      console.warn('[ConvexClient] postOrchEvent skipped: INDEXER_SECRET not set');
      return;
    }
    try {
      await this.http.mutation(seedOrchEventRef, { secret, ...args });
    } catch (err) {
      console.warn('[ConvexClient] postOrchEvent failed (non-fatal):', err);
    }
  }

  async postHumanSteering(args: { tick: number; targetClanId: number; body: string; sentBy?: string }): Promise<void> {
    const secret = this.indexerSecret();
    if (!secret) {
      console.warn('[ConvexClient] postHumanSteering skipped: INDEXER_SECRET not set');
      return;
    }
    try {
      await this.http.mutation(seedHumanSteeringRef, { secret, ...args });
    } catch (err) {
      console.warn('[ConvexClient] postHumanSteering failed (non-fatal):', err);
    }
  }

  async postBulletin(args: { clanId: number; slot: number; body: string; dataHash?: string; txHash?: string }): Promise<void> {
    const secret = this.indexerSecret();
    if (!secret) {
      console.warn('[ConvexClient] postBulletin skipped: INDEXER_SECRET not set');
      return;
    }
    try {
      await this.http.mutation(seedBulletinRef, { secret, ...args });
    } catch (err) {
      console.warn('[ConvexClient] postBulletin failed (non-fatal):', err);
    }
  }
}

export function createConvexClient(): IConvexClient {
  if (readEnv('CLAN_WORLD_USE_STUB_CONVEX') === 'true') {
    return new StubConvexClient();
  }
  const url = readEnv('CONVEX_URL');
  if (!url) {
    console.warn('[ConvexClient] CONVEX_URL not set — using stub data. Set CLAN_WORLD_USE_STUB_CONVEX=true to silence.');
    return new StubConvexClient();
  }
  return new RealConvexClient(url);
}
