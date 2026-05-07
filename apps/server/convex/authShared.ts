/**
 * Shared auth helper for public mutations that must be gated by INDEXER_SECRET.
 *
 * Pattern mirrors `inft.ts.requireIndexerSecret`: callers (orchestrator,
 * indexer, demo seed scripts) pass `secret: <string>` as a mutation arg, and
 * the handler calls this helper before doing any writes. If INDEXER_SECRET is
 * unset on the Convex deployment, mutations reject all writes (fail-closed).
 *
 * Used by: bulletins.seedBulletin, comms.seed{Whisper,OrchEvent,HumanSteering}.
 *
 * For mutations that have NO external callers (memory.seedEntry,
 * memory.seedEvent, clansmen.seedClansmen) we use `internalMutation` instead;
 * those don't need this helper.
 */
export function requireIndexerSecret(supplied: string): void {
  const expected = process.env.INDEXER_SECRET;
  if (!expected) {
    throw new Error("INDEXER_SECRET is not configured on this Convex deployment");
  }
  if (supplied !== expected) {
    throw new Error("invalid indexer secret");
  }
}
