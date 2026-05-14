import { useEffect, useRef, useState } from 'react';
import {
  CACHE_KEY,
  PAYLOAD_VERSION,
  MAX_CACHE_AGE_MS,
  type CachedSnapshot,
} from './snapshotCacheConstants';

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
