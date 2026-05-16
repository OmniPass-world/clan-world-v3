import { useSafeQuery } from './useSafeQuery';
import { api } from '../../../server/convex/_generated/api';

/**
 * Live world tick from the Convex snapshot query. Returns 0 while the
 * query is loading (caller decides whether the loading state matters —
 * for the cockpit fade/visibility math, 0 yields "everything fresh"
 * which is the correct degraded behavior pre-snapshot).
 *
 * Shared between cockpit comms tab + bulletin flyout to avoid duplicate
 * definitions. See PR #423 R3 super-swarm + cloud Copilot finding.
 */
export function useCurrentWorldTick(): number {
  const snapshot = useSafeQuery(api.getSnapshot.getSnapshot);
  return typeof snapshot?.tick === 'number' && Number.isFinite(snapshot.tick)
    ? snapshot.tick
    : 0;
}
