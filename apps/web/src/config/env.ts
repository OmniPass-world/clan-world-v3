/**
 * Shared environment-flag exports for `apps/web`.
 *
 * Why this file exists: previously `WorldMap.tsx` imported `DEMO_MODE` from
 * `./App`, while `App.tsx` (transitively, via `MainApp`) imported `WorldMap`.
 * That created a module cycle — bundlers can return undefined bindings during
 * initialization, and tests can't isolate either module without dragging the
 * other in. PR #133 review MUST FIX #3.
 *
 * Rule: env flags read at module-init time live here. Components import from
 * `./config/env` (a leaf module) — never from another component file.
 */

/**
 * DEMO_MODE — gates all mock/fake data in WorldMap.
 *
 * - true  (VITE_CLANWORLD_DEMO_MODE=true): render mock clans, bandits, walls,
 *   monuments, agentLogs. Set explicitly for local demo/UAT visibility,
 *   hackathon Loom recordings, or judges-without-World-App preview.
 * - false (env unset OR any value other than 'true'): canvas + scoreboard ONLY
 *   render real Convex/chain state. Empty world = empty UI.
 *
 * Default policy: missing/unset env var → DEMO_MODE off (false). Only an
 * explicit `'true'` string enables it.
 */
export const DEMO_MODE = import.meta.env.VITE_CLANWORLD_DEMO_MODE === 'true';
