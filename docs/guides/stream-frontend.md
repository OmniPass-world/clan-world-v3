# Stream: Frontend (Vite + React)

How to work on `apps/web/`.

## Setup

```bash
pnpm install
pnpm --filter @clan-world/web dev
```

The dev server reads `PORT` from env (per `~/claudes-world` ADR 0003 port allocation). Default 5173 if unset.

## MOCK_MODE

Run with stubs end-to-end:

```bash
CLAN_WORLD_USE_STUB_CONVEX=true \
CLAN_WORLD_USE_STUB_CHAIN=true \
  pnpm --filter @clan-world/web dev
```

This is the default for early Wave 0–1 work — the frontend renders mock snapshots without needing Convex or chain wired.

## Region polygons

Regions are SVG-authored, not Pixi sprites:

```
apps/web/src/assets/regions/
  region-1.svg
  region-2.svg
  ...
  region-8.svg
```

(Wave 1+) An `RegionSvg` component imports the SVG inline and overlays clan-color fills based on `WorldSnapshot.regions[].ownerClanId`. SVG keeps the cut-list shallow — if Pixi work is behind, we can ship region polygons as plain SVG without a canvas at all.

## Pixi (Wave 2+)

Pixi is for the canvas only — animated elements, sprite-based units, particle effects. The UI chrome (panels, modals, whisper feed) stays in plain React. The Pixi canvas is a sibling element in the `<App>` tree, not the root.

```tsx
// apps/web/src/canvas/WorldCanvas.tsx (Wave 2)
import { Application } from 'pixi.js';
// ...
```

Don't reach for `pixi-react` until you've prototyped the canvas in vanilla Pixi. Wrappers add weight without much benefit at our scale.

## State management

- React state + Convex live queries are enough through M5.
- If you find yourself reaching for Zustand or Redux, reconsider — the Convex subscription IS the state store.
- For derived values (e.g., "list of clans owned by current user"), use `useMemo` with the live snapshot.

## Build & deploy

```bash
pnpm --filter @clan-world/web build   # → apps/web/dist/
```

Static output. Deploy to Vercel or Cloudflare Pages.

## Mobile testing

Test on a real phone and in Chrome devtools mobile emulation. The frontend should work in an ordinary browser URL.
