import { useEffect, useState } from 'react';

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
 */

// Bump this on snapshot shape changes (Convex schema migrations).
const CACHE_KEY = 'cw-snapshot-v1';
// Drop cache older than this — avoids surfacing wildly stale state.
const MAX_CACHE_AGE_MS = 60 * 60 * 1000; // 1 hour

type Cached<T> = { ts: number; data: T };

export function useCachedSnapshot<T>(live: T | undefined): T | undefined {
  const [cached, setCached] = useState<T | undefined>(() => {
    try {
      const raw = localStorage.getItem(CACHE_KEY);
      if (!raw) return undefined;
      const parsed = JSON.parse(raw) as Cached<T>;
      if (!parsed || typeof parsed.ts !== 'number') return undefined;
      if (Date.now() - parsed.ts > MAX_CACHE_AGE_MS) return undefined;
      return parsed.data;
    } catch {
      return undefined;
    }
  });

  useEffect(() => {
    if (live === undefined) return;
    try {
      const payload: Cached<T> = { ts: Date.now(), data: live };
      localStorage.setItem(CACHE_KEY, JSON.stringify(payload));
    } catch {
      // quota or private-mode — ignore
    }
    setCached(live);
  }, [live]);

  return live ?? cached;
}
