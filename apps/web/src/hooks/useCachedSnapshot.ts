import { useEffect, useRef, useState } from 'react';
import {
  CACHE_KEY,
  PAYLOAD_VERSION,
  MAX_CACHE_AGE_MS,
  type CachedSnapshot,
} from './snapshotCacheConstants';

/**
 * Lightweight runtime check that a parsed cache payload's `data` field matches
 * the shape the rest of the app expects from `getSnapshot`. We deliberately
 * keep this thin: `tick` (number) + a `clans` array whose entries each have a
 * string `id` and string `name`. That's enough to catch the major shape
 * changes (field rename, table swap, schema-migration without `PAYLOAD_VERSION`
 * bump) without becoming a maintenance burden as the snapshot grows new
 * optional fields.
 *
 * On false, `useCachedSnapshot` returns `undefined` from its initializer —
 * cache miss → the live Convex query takes over. Better to flash the "no chain
 * data yet" placeholder than hydrate a malformed object and crash downstream
 * `clan.foo` reads.
 *
 * Exported for unit tests + for `MapGhostLayer`'s parallel cache read path
 * which currently relies on a duck-typed inline check.
 */
export function isValidSnapshotShape(data: unknown): boolean {
  if (data === null || typeof data !== 'object') return false;
  const obj = data as Record<string, unknown>;
  if (typeof obj.tick !== 'number') return false;
  if (!Array.isArray(obj.clans)) return false;
  // Don't iterate the whole array — first entry is enough to detect a
  // schema shift, and keeps the guard O(1) on the hot path.
  if (obj.clans.length > 0) {
    const c = obj.clans[0];
    if (c === null || typeof c !== 'object') return false;
    const clan = c as Record<string, unknown>;
    if (typeof clan.id !== 'string') return false;
    if (typeof clan.name !== 'string') return false;
  }
  return true;
}

/**
 * Snapshot cache for the WorldMap's Convex `getSnapshot` query.
 *
 * Why: on iOS Safari (and any browser that aggressively pauses background
 * tabs), returning to the PWA briefly leaves the Convex `useQuery` hook
 * returning `undefined` while the websocket reconnects. That gap previously
 * surfaced as a white "no chain data yet" flash. We instead show the
 * previously-cached state instantly (read synchronously from localStorage
 * during useState's initializer) until fresh data arrives.
 *
 * Generic over the snapshot type so we don't pull the Convex API type into
 * this hook — keeps it framework-agnostic and easy to unit-test.
 *
 * Cache key + payload version + max age live in `./snapshotCacheConstants`
 * because `MapGhostLayer.tsx` reads the same cache before PixiJS warms up
 * and the two MUST stay in lockstep on schema migrations.
 */

// Throttle localStorage writes — Convex emits a fresh snapshot every heartbeat
// tick so without throttling we'd JSON.stringify + setItem on every tick, which
// can block the main thread on mobile Safari.
const WRITE_THROTTLE_MS = 30 * 1000; // 30 seconds

export function useCachedSnapshot<T>(live: T | undefined): T | undefined {
  const [cached, setCached] = useState<T | undefined>(() => {
    try {
      const raw = localStorage.getItem(CACHE_KEY);
      if (!raw) return undefined;
      const parsed = JSON.parse(raw) as CachedSnapshot<T>;
      if (!parsed || typeof parsed.ts !== 'number') return undefined;
      if (parsed.v !== PAYLOAD_VERSION) return undefined;
      if (Date.now() - parsed.ts > MAX_CACHE_AGE_MS) return undefined;
      // Inner-shape guard — catches schema migrations that landed without a
      // PAYLOAD_VERSION bump. Cache miss is safe; the live Convex query
      // overwrites within a frame or two.
      if (!isValidSnapshotShape(parsed.data)) return undefined;
      return parsed.data;
    } catch {
      return undefined;
    }
  });

  // Track last write timestamp across renders without triggering re-renders.
  const lastWriteTsRef = useRef<number>(0);

  useEffect(() => {
    if (live === undefined) return;
    const now = Date.now();
    if (now - lastWriteTsRef.current >= WRITE_THROTTLE_MS) {
      // Update timestamp BEFORE attempting the write so failed writes
      // (private-mode / quota) are also throttled, not retried every tick.
      lastWriteTsRef.current = now;
      try {
        const payload: CachedSnapshot<T> = { v: PAYLOAD_VERSION, ts: now, data: live };
        localStorage.setItem(CACHE_KEY, JSON.stringify(payload));
      } catch {
        // quota or private-mode — ignore
      }
    }
    setCached(live);
  }, [live]);

  return live ?? cached;
}
