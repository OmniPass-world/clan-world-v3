# apps/web — AGENTS.md

Vite + React frontend. The user-facing surface for the live game, cockpit, and owner tooling, running directly in a browser against Base Sepolia-backed state.

## What this package does

- Renders the world map (8 regions), clan panels, mission queue, whisper feed.
- Subscribes to Convex queries via `IConvexClient` for live snapshot + `getCombinedComms`.
- Calls `IChainClient` for any direct chain reads (rare — most reads go through Convex).
- Renders cockpit and owner-editor routes without a platform-specific gate.

## Wave 0 status

The app renders the live Pixi map/cockpit surfaces and can fall back to explicit demo mode when requested.

## Key files

- `src/main.tsx` — entry point, Convex provider wiring, and degraded backend fallback.
- `src/App.tsx` — Wave 0 page, will become the world map shell.
- `vite.config.ts` — port read from `PORT` env (per `~/claudes-world` ADR 0003).
- `index.html` — viewport meta tag set for mobile-friendly browser use.

## Local conventions

- **No state management library** until the world map needs it. React state + Convex live queries are enough through M5.
- **Pixi for the canvas only.** UI chrome (panels, modals) stays in plain React.
- **Region polygons are SVG-authored** in `assets/regions/` (not Pixi sprites); they're the "if behind, cut to" fallback in `BUILD_PLAN.md`.
- **No CSS framework** for Wave 0–1; inline styles are fine. Add Tailwind only if a designer joins the team.

## How it interacts with adapters

- **`IConvexClient`** — primary data source. Frontend imports `createConvexClient()` from `@clan-world/shared/adapters` and subscribes via the returned interface. Stub mode returns mock data so the frontend can render before Convex is wired.
- **`IChainClient`** — used only for direct reads where Convex hasn't indexed yet (e.g., the very first tick before the indexer cron runs). Stub returns hardcoded `tick=0`.
- **`ILLMClient`** — never used by the frontend. Whisper text is elder-direct via the `sendWhisper` mutation and read through Convex.
- **`IKeeper`** — never used by the frontend.

## Running

```bash
pnpm --filter @clan-world/web dev   # Vite dev, port from $PORT
pnpm --filter @clan-world/web build
```

For demo runs, use the deployed URL directly in a browser — see `docs/guides/stream-frontend.md`.
