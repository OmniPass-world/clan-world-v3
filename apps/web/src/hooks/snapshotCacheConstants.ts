/**
 * Shared cache key + payload-version + max-age constants for the WorldMap
 * snapshot localStorage cache. Imported by BOTH `useCachedSnapshot.ts` (the
 * hook that writes the cache) and `components/MapGhostLayer.tsx` (the static
 * ghost that reads it before PixiJS warms up).
 *
 * Why a shared module: these constants were previously duplicated across the
 * two files. If `PAYLOAD_VERSION` were bumped in the hook for a schema
 * migration but not in the ghost, the ghost would read stale data as if it
 * matched the new shape — silent corruption with no runtime error. Extracting
 * to one source of truth eliminates that drift hazard.
 *
 * Pure constants. NO imports from React, Pixi, Convex, or anything else —
 * keeps the module trivially tree-shakeable and safe to import from any layer.
 */

// Scope the cache key per backend so dev and prod (served from the same
// localhost origin during development) never share a localStorage slot.
const ENV_SCOPE = (import.meta.env.VITE_CONVEX_URL as string | undefined) ?? 'no-backend';

// Realm-scope the cache key by the diamond address too. The repo's
// full-game-reset runbook (docs/runbooks/full-game-reset.md) explicitly
// supports redeploying the diamond and re-pointing Convex at the new realm;
// without this discriminator a returning user would see the OLD realm's
// cached state for up to MAX_CACHE_AGE_MS (1 hour) while the new realm
// hydrates. Mirrors the server-side `CLAN_WORLD_CONTRACT_ADDRESS` env var.
const DIAMOND_SCOPE =
  (import.meta.env.VITE_CLAN_WORLD_CONTRACT_ADDRESS as string | undefined) ?? 'no-diamond';

/**
 * localStorage key under which the cached snapshot payload lives. Bumped via
 * `PAYLOAD_VERSION`, not via key rename — old entries are dropped on read
 * when the version doesn't match.
 */
export const CACHE_KEY = `cw-snapshot-v1:${ENV_SCOPE}:${DIAMOND_SCOPE}`;

/**
 * Increment when the cached payload SHAPE changes (Convex schema migration,
 * field rename, etc.). Entries with a different `v` are silently dropped on
 * read instead of causing a runtime crash or surfacing corrupt state.
 */
export const PAYLOAD_VERSION = 1;

/**
 * Drop cache older than this — avoids surfacing wildly stale state if a user
 * returns to the PWA after a long absence. 1 hour balances "instant hydration
 * for typical mobile-tab-pause scenarios" against "don't show day-old state".
 */
export const MAX_CACHE_AGE_MS = 60 * 60 * 1000; // 1 hour

/**
 * Cached-snapshot envelope. Generic over the snapshot type so consumers can
 * narrow `data` to their own typed shape without this module depending on
 * Convex / app types.
 */
export type CachedSnapshot<T> = { v: number; ts: number; data: T };
