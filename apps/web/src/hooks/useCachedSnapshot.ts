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
 * Validates EVERY clan entry (not just the first) — the clans array is small
 * (~5 entries in the current world) so the O(N) cost is trivial, and catches
 * the case where a poisoned cache has a valid first entry but a malformed
 * second that would crash downstream `clan.foo` loops.
 *
 * Exported so `MapGhostLayer`'s parallel cache read path can apply the same
 * envelope check before iterating clans (kept in lockstep with this hook).
 */
export function isValidSnapshotShape(data: unknown): boolean {
  if (data === null || typeof data !== 'object') return false;
  const obj = data as Record<string, unknown>;
  if (typeof obj.tick !== 'number') return false;
  if (!Array.isArray(obj.clans)) return false;
  for (let i = 0; i < obj.clans.length; i++) {
    const c = obj.clans[i];
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

/**
 * The shape validator that runs on the cached `data` payload before the hook
 * hydrates state with it. Defaults to `isValidSnapshotShape` because today's
 * only caller hydrates `api.getSnapshot.getSnapshot`. Callers that wrap a
 * different shape can pass their own predicate to override; pass `() => true`
 * to opt out of inner-shape validation entirely (envelope checks still apply).
 */
export function useCachedSnapshot<T>(
  live: T | undefined,
  validate: (data: unknown) => boolean = isValidSnapshotShape,
): T | undefined {
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
      if (!validate(parsed.data)) return undefined;
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
