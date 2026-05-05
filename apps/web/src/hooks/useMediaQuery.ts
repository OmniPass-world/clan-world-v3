import { useEffect, useState } from 'react';

/**
 * React hook that mirrors a CSS media query into component state.
 *
 * Uses `window.matchMedia` and subscribes to the change event so React
 * re-renders when the viewport crosses the breakpoint. SSR-safe:
 * returns `false` on the server (no `window`).
 *
 * Used by Cockpit.tsx to branch between the desktop 3-col 2-row grid and
 * the mobile swipe-paginated layout below 900px viewport.
 */
export function useMediaQuery(query: string): boolean {
  const [matches, setMatches] = useState<boolean>(() => {
    if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
      return false;
    }
    return window.matchMedia(query).matches;
  });

  useEffect(() => {
    if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
      return;
    }
    const mql = window.matchMedia(query);
    // Sync once on subscribe in case the query string changed between renders.
    setMatches(mql.matches);
    const handler = (e: MediaQueryListEvent) => setMatches(e.matches);
    mql.addEventListener('change', handler);
    return () => mql.removeEventListener('change', handler);
  }, [query]);

  return matches;
}
