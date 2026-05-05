import { useQuery } from 'convex/react';

/**
 * Wraps `useQuery` so synchronous throws from Convex (server errors,
 * quota-exceeded, schema mismatches, etc.) become `undefined` returns —
 * routing the call site through its existing "loading / unavailable"
 * fallback path instead of unmounting via the outer error boundary.
 *
 * Same call signature as `useQuery`. Pass `'skip'` as args to skip the
 * query entirely (Convex convention).
 *
 * Background: convex@1.17.4 `useQuery` synchronously rethrows server
 * errors during render (see `dist/cjs/react/client.js:368-371`). Without
 * this wrapper, a Convex outage bubbles to `CockpitErrorBoundary`, which
 * re-renders the tree and re-throws → infinite "COCKPIT OFFLINE" loop.
 */
export const useSafeQuery: typeof useQuery = ((query: unknown, ...args: unknown[]) => {
  try {
    // Cast through `any` to preserve the per-overload return type via the outer
    // `typeof useQuery` annotation while TypeScript can't unify generic args
    // through a rest parameter.
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return (useQuery as any)(query, ...args);
  } catch (err) {
    if (import.meta.env.DEV) {
      // eslint-disable-next-line no-console
      console.warn('[useSafeQuery] convex query threw — treating as undefined:', err);
    }
    return undefined;
  }
}) as typeof useQuery;
