import { useEffect, useRef, useState } from 'react';
import {
  CACHE_KEY,
  PAYLOAD_VERSION,
  MAX_CACHE_AGE_MS,
  FRESHNESS_THRESHOLD_MS,
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
 *
 * "Last known good" guard (issue #283 / PR #272 super-swarm):
 * During a websocket reconnect or post-reset indexing window the live
 * payload can briefly be `{ clans: [] }` even though the previously-cached
 * payload had clans. Writing that transient empty payload through would
 * (a) immediately flip the UI into "no clans" placeholder mode and
 * (b) destroy the very cache that masks the NEXT reconnect gap. So when
 * live has zero clans, cached has clans, and the cached payload is younger
 * than `FRESHNESS_THRESHOLD_MS`, we keep the cached state and skip the
 * write. Once the cache ages past the freshness window we yield to the
 * live empty state — a real post-reset realm should not be resurrected
 * indefinitely.
 */

// Throttle localStorage writes — Convex emits a fresh snapshot every heartbeat
// tick so without throttling we'd JSON.stringify + setItem on every tick, which
// can block the main thread on mobile Safari.
const WRITE_THROTTLE_MS = 30 * 1000; // 30 seconds

/**
 * Duck-type check for "this snapshot has a clans array with at least one
 * entry." Kept loose on purpose — the hook is generic over the snapshot
 * shape, and the `.clans` property is the only field we need to reason
 * about for the last-known-good guard. Anything without a clans array
 * falls through to the normal write path (the guard simply doesn't fire).
 */
function snapshotHasClans(value: unknown): boolean {
  if (typeof value !== 'object' || value === null) return false;
  const clans = (value as { clans?: unknown }).clans;
  return Array.isArray(clans) && clans.length > 0;
}

/**
 * Inverse of `snapshotHasClans` for "the snapshot definitively has zero
 * clans" — i.e. the `.clans` property exists and is an empty array.
 * Crucially this returns false for snapshots that don't carry `.clans`
 * at all, so a snapshot shape without that field never trips the guard.
 */
function snapshotHasEmptyClans(value: unknown): boolean {
  if (typeof value !== 'object' || value === null) return false;
  const clans = (value as { clans?: unknown }).clans;
  return Array.isArray(clans) && clans.length === 0;
}

type CachedEntry<T> = { data: T; ts: number };

export function useCachedSnapshot<T>(live: T | undefined): T | undefined {
  // Track the cached payload's original timestamp alongside the data so the
  // last-known-good guard can reason about cache age without a separate
  // localStorage re-read. The companion ref mirrors the latest committed
  // value so the live-update useEffect can read it without taking a
  // dependency on `cachedEntry` (which would cause re-render loops).
  const [cachedEntry, setCachedEntry] = useState<CachedEntry<T> | undefined>(() => {
    try {
      const raw = localStorage.getItem(CACHE_KEY);
      if (!raw) return undefined;
      const parsed = JSON.parse(raw) as CachedSnapshot<T>;
      if (!parsed || typeof parsed.ts !== 'number') return undefined;
      if (parsed.v !== PAYLOAD_VERSION) return undefined;
      if (Date.now() - parsed.ts > MAX_CACHE_AGE_MS) return undefined;
      return { data: parsed.data, ts: parsed.ts };
    } catch {
      return undefined;
    }
  });
  const cachedEntryRef = useRef<CachedEntry<T> | undefined>(cachedEntry);

  // Track last localStorage write timestamp across renders without
  // triggering re-renders. Separate from cachedEntry.ts because the
  // throttle is about write frequency, not cache age.
  const lastWriteTsRef = useRef<number>(0);

  useEffect(() => {
    if (live === undefined) return;
    const now = Date.now();

    // Last-known-good guard. If live is an empty-clans payload, cached has
    // clans, AND the cached payload is still inside the freshness window,
    // treat the live empty as a transient reconnect artifact. We skip BOTH
    // the localStorage write and the setState call — the existing cache
    // stays exactly as-is (same data, same ts), so the freshness window
    // continues to run from the original write, not from "now".
    const currentCached = cachedEntryRef.current;
    const lastKnownGoodGuardActive = (
      snapshotHasEmptyClans(live)
      && currentCached !== undefined
      && snapshotHasClans(currentCached.data)
      && now - currentCached.ts < FRESHNESS_THRESHOLD_MS
    );
    if (lastKnownGoodGuardActive) {
      return;
    }

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
    const nextEntry: CachedEntry<T> = { data: live, ts: now };
    cachedEntryRef.current = nextEntry;
    setCachedEntry(nextEntry);
  }, [live]);

  // Return semantics:
  //  - If live is undefined (typical reconnect gap), serve the cached data
  //    so the UI renders instantly instead of flashing the "no chain data"
  //    placeholder.
  //  - If live is defined AND the last-known-good guard would fire for it,
  //    ALSO serve the cached data — surfacing a transient `{ clans: [] }`
  //    to the consumer would flip the WorldMap's `hasClans` derivation to
  //    false and trip the 5-second placeholder timer (defeats the guard).
  //    The guard re-evaluates from the live useEffect on every tick, so as
  //    soon as live recovers (non-empty clans) we return live again.
  //  - Otherwise return live verbatim.
  if (live === undefined) return cachedEntry?.data;
  if (
    cachedEntry !== undefined
    && snapshotHasEmptyClans(live)
    && snapshotHasClans(cachedEntry.data)
    && Date.now() - cachedEntry.ts < FRESHNESS_THRESHOLD_MS
  ) {
    return cachedEntry.data;
  }
  return live;
}
