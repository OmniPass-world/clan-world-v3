// Convex API surface for the mobile app.
//
// We deliberately do NOT import the server's generated `_generated/api.d.ts`
// here. That file's transitive type imports walk into apps/server's runtime
// modules, which depend on @clan-world/shared paths the mobile package
// doesn't resolve. Pulling those into mobile's typecheck graph causes
// tsc errors that don't reflect a real problem.
//
// Slice 1 trade: typed at runtime via `anyApi` (no compile-time field
// names). Slice 2 upgrade path: the server can emit a slimmer
// `mobile-api.d.ts` that re-exports only the public functions mobile uses,
// and we swap to that here for full typesafety.
import { anyApi } from 'convex/server';

export const api = anyApi as unknown as {
  getSnapshot: { getSnapshot: any };
  memory: { getByClan: any; getEventsByClan: any };
};
