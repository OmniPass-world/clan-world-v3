# Issue 326: Unified World Map Plan

## 1. Component Extraction Strategy

Use the existing `WorldMap` component as the shared map surface for `/`, `/cockpit`, and the Android webview path that loads `/cockpit`.

Do not extract a new `WorldMapCanvas` as the first move. `WorldMap` is currently the canonical map boundary: it owns Convex queries, cached snapshot hydration, PixiJS application setup, viewport persistence, ghost layer handoff, HUD overlays, event ticker, notice panel, and the version badge. Splitting that now would create a high-risk refactor in the largest frontend file and would force the implementer to decide which overlays belong to "canvas" versus "wrapper" before the product boundary is actually unclear. The issue goal is component unification, not decomposition.

The implementation should instead make the cockpit map area render the same `WorldMap` element that `MainApp` renders:

- Keep root route: `App.tsx` -> `MainApp` -> `WorldMapBoundary` -> `WorldMap`.
- Keep or update cockpit desktop center cell: `Cockpit.tsx` -> center map cell -> `WorldMapBoundary` -> `WorldMap`.
- Keep or update cockpit mobile top region: `MobileCockpitLayout.tsx` -> world map region -> `WorldMapBoundary` -> `WorldMap`.
- Avoid cockpit-specific map forks, prop-driven feature toggles, or duplicated badge/ghost/HUD code.

At current HEAD, `Cockpit.tsx` and `MobileCockpitLayout.tsx` already import and mount `WorldMap`. Treat that as the intended direction and harden it rather than replacing it with another wrapper. If the target branch for the PR differs, replace any cockpit-specific map implementation with the direct `WorldMap` mount pattern above.

Small extraction that is acceptable: create a tiny presentational `CockpitWorldMapFrame` only if it removes duplicated frame styles/test ids between desktop and mobile. That frame must not own map behavior. It may provide border/background/overflow and render:

```tsx
<WorldMapBoundary>
  <WorldMap />
</WorldMapBoundary>
```

Avoid extracting Pixi internals in this issue. A future decomposition of `WorldMap.tsx` can happen after the map is already unified and covered by route-level tests.

## 2. Mount Lifecycle

The route model is path-based, not router-driven SPA navigation. `App.tsx` reads `window.location.pathname` and chooses exactly one top-level branch: `/cockpit`, `/agents/:id`, `/owner`, or root `MainApp`. That means a normal browser navigation between `/` and `/cockpit` is a full route load and only one visible `WorldMap` instance should exist at a time.

Implementation invariants:

- A single React root and a single `ConvexProvider` are created in `main.tsx`.
- Within any route render, mount exactly one `WorldMap` instance.
- Do not render root `MainApp` behind cockpit chrome.
- Do not introduce a hidden/offscreen `WorldMap` for preloading.
- Do not share a live PixiJS `Application` instance between routes.

Convex subscriptions:

- `WorldMap` calls `useSafeQuery(api.getSnapshot.getSnapshot)` and `useSafeQuery(api.events.getRecentChainEvents)`.
- `CockpitHeader` separately calls `getSnapshot` for the memory wipe/tick display.
- This means cockpit has at least one additional header subscription compared with root. Do not add any new duplicate snapshot subscriptions as part of map unification.
- If later optimization is needed, hoist snapshot data into a shared hook/provider for cockpit chrome and map, but that is not required for this issue and would expand scope.

Pixi lifecycle:

- Each `WorldMap` mount creates one Pixi `Application`, one `Viewport`, ticker callbacks, event listeners, ResizeObserver, texture/sprite state, and WebGL context.
- Existing cleanup removes ticker callbacks, destroys snow effects, removes event listeners, clears refs, destroys Pixi display objects/filters, and calls `app.destroy(true)` with a StrictMode guard.
- The cockpit implementation must not wrap `WorldMap` in a React key that changes during ordinary tab/header/collapse state updates. A changing key would remount Pixi and cause stutter, duplicate asset work, and possible WebGL churn.
- The existing `WorldMapBoundary` may change its internal key only for WebGL recovery retries. That is intentional.

Viewport restoration:

- `WorldMap` persists pan/zoom to `sessionStorage` under `VIEWPORT_STORAGE_KEY`.
- `MapGhostLayer` reads the same key so the ghost layer matches the canvas during warmup.
- Root and cockpit currently share this viewport key. Keep that for "same map everywhere" behavior: a user who pans on root then opens cockpit in the same tab should see the same world view.
- If product later wants independent root/cockpit view memory, add a separate scoped key in a follow-up. Do not do that here because it weakens the desired identical-map outcome.

Navigation test expectation:

- In a single page, there should be one `canvas` under `/`.
- In a single page, there should be one `canvas` under `/cockpit`.
- Reloading or navigating between the paths should tear down the old Pixi app and create a new one, with no second canvas left in the DOM.

## 3. Mobile and Android Constraints

The Android app path loads `/cockpit`, so cockpit mobile behavior is the Android webview behavior unless the native app wraps it with additional constraints. The web implementation must keep the cockpit chrome and mobile layout while putting the canonical `WorldMap` in the map region.

Mobile/PWA behavior that still applies:

- `index.html` locks browser zoom with `maximum-scale=1.0`, `user-scalable=no`, `viewport-fit=cover`.
- `html`, `body`, and `#root` use `touch-action: none` and hidden overflow.
- `body` applies top safe-area padding, and `#root` subtracts the top inset.
- `.cw-fullheight` uses `100vh` with `100dvh` override for modern mobile viewport correctness.
- `Cockpit` applies `paddingBottom: env(safe-area-inset-bottom, 0px)`.
- `MobileCockpitLayout` keeps the vertical split, map region, collapsible pager, scroll-snap elder pages, `WebkitOverflowScrolling: touch`, active-clan persistence, and collapsed-state persistence.
- `WorldMap` sets canvas `touchAction = 'none'`, prevents multi-touch browser escape on `touchstart`, and delegates pan/pinch/wheel to `pixi-viewport`.
- `WorldMap` caps Pixi resolution at `Math.min(devicePixelRatio, 2)` to reduce mobile GPU pressure.
- `MapGhostLayer` uses the actual map container dimensions after mount and starts with `visualViewport`/window dimensions so first paint is reasonable on mobile.
- `WorldMapBoundary` provides scoped WebGL context-loss recovery for the map area without tearing down the whole cockpit.

Mobile behavior that becomes redundant or should not be added:

- No cockpit-specific canvas gesture handler. `pixi-viewport` is the shared pan/pinch source of truth.
- No separate Android-only map implementation or static screenshot fallback for normal operation.
- No cockpit-only version badge. `VersionBadge` is already rendered inside `WorldMap`.
- No duplicate ghost layer in cockpit. `WorldMap` owns `MapGhostLayer`.
- No extra viewport restoration logic in cockpit. `WorldMap` and `MapGhostLayer` own it.

Sizing requirements for cockpit:

- Every parent between cockpit shell and `WorldMap` must provide nonzero height.
- Keep `flex: 1`, `minHeight: 0`, `position: relative`, and `overflow: hidden` on the map region.
- Keep `WorldMap` as a direct child of the boundary/frame where possible. The boundary deliberately returns a Fragment so it does not break flex/grid sizing.
- If adding `CockpitWorldMapFrame`, ensure the frame has stable height and does not insert an unconstrained wrapper that causes `ResizeObserver` to report `0x0`.

Android verification without a device:

- Use Playwright mobile project (`Pixel 5`) against `/cockpit`.
- Use Chromium device emulation with touch enabled and a narrow viewport to exercise the `MobileCockpitLayout` path.
- Verify map canvas appears in the mobile map region, the collapse toggle remains usable, and the pager still scroll-snaps.
- Optionally run the web app in Android Studio emulator if available, but do not make that a hard CI gate for this PR.

Uncertainty: I did not inspect the native Android app entry in this pass after the stop instruction. The web-facing assumption is based on the issue statement and the PWA manifest start URL pointing at `/cockpit`.

## 4. State Coupling

Cockpit state should remain cockpit chrome state. `WorldMap` state should remain map state.

Current cockpit state slices:

- Desktop layout: fixed elder slots and initial tab choices.
- Mobile layout: `activeClanId`, `collapsed`, scroll debounce/programmatic scroll refs, active page dots.
- `MiniCockpit`: active tab per elder.
- `CockpitHeader`: memory wipe countdown and connection status.
- Terminal tabs: iframe connection/reload behavior.

Current `WorldMap` state slices:

- Pixi readiness/init errors/WebGL lost.
- Selected clan/base for map HUD readout.
- Viewport pan/zoom persisted in `sessionStorage`.
- Cached snapshot and no-chain-data grace placeholder.
- Animation refs/tickers for travel, bubbles, combat, bandits, winter, etc.

Do not plumb map interactions back to cockpit chrome for this issue. Tapping a base can keep updating the `WorldMap` internal selected-clan HUD, but it should not switch the active mobile elder page, change cockpit tabs, or update header state. That keeps the shared map identical across root and cockpit.

Cross-cutting state to watch:

- Snapshot data: `WorldMap` and `CockpitHeader` both read snapshot data. This is acceptable, but avoid adding a third cockpit-level `getSnapshot` consumer for map-related data.
- Local/session storage keys: cockpit mobile uses `cockpit-mobile-active-clan` and `cockpit-mobile-collapsed`; map uses `VIEWPORT_STORAGE_KEY` and cached snapshot keys. Keep them separate.
- Pointer/touch events: the mobile collapse toggle overlays the map region. It needs a higher z-index and should receive taps; the map canvas should receive gestures elsewhere.
- Error boundaries: root and cockpit should both wrap `WorldMap` in `WorldMapBoundary` so failures degrade consistently. Cockpit also has `CockpitErrorBoundary` for full-panel crashes.

If product later wants map selection to drive cockpit panels, implement it as a deliberate follow-up with an explicit callback prop or shared store. Do not introduce that coupling while unifying the visual map surface.

## 5. Performance

`WorldMap` is heavy. The plan should preserve the existing single-instance behavior and avoid remount churn.

Hot paths and risks:

- Pixi `Application.init()` creates WebGL context and uploads large map textures.
- `world-map.png` and `world-map-winter.png` are large assets.
- Spritesheets, base sprites, bandit sprites, filters, and snow particles allocate GPU resources.
- Per-frame ticker callbacks run for day/night, travel, zone pulse, combat, bandit animation, burst particles, and winter fade/snow.
- `ResizeObserver` relayout can be expensive if parent layout oscillates.
- Convex ticks and agent logs can trigger React recomputation and Pixi reconciliation effects.
- Terminal iframes are also heavy; current cockpit keeps only clan 1 on terminal by default and the other panels on lightweight tabs. Preserve that.

Implementation guidance:

- Do not mount more than one `WorldMap` on `/cockpit`.
- Do not remount `WorldMap` when switching cockpit tabs, changing active mobile page, collapsing/expanding the pager, or updating connection status.
- Keep map parent dimensions stable. The mobile collapse animation changes the map region height; that should trigger resize, not remount.
- Avoid passing freshly-created props to `WorldMap`; ideally keep it prop-free in this PR.
- Avoid adding route-level lazy loading unless the implementer measures a real bundle problem. Since both `/` and `/cockpit` need the same map, route-splitting the map can improve non-map routes but does not change the cockpit/root shared cost.
- Keep `devicePixelRatio` cap and do not raise Pixi resolution for cockpit.
- Avoid cockpit-specific overlays inside the map area beyond the existing collapse toggle. More DOM overlays increase hit-testing and layout work on mobile.

Performance acceptance checks:

- `/cockpit` desktop has exactly one map canvas.
- `/cockpit` mobile has exactly one map canvas.
- Mobile collapse/expand does not create a second canvas and does not recreate the first canvas.
- Basic drag/pinch remains responsive in Playwright mobile/manual browser testing.
- No obvious console errors around WebGL context loss, destroyed Pixi objects, or ResizeObserver loops.

## 6. Test Plan

Keep tests minimal and targeted, consistent with the hackathon testing rule.

Update or add Playwright coverage under `apps/web/tests/e2e`.

Desktop cockpit map unification:

- Extend `04-cockpit-shell.spec.ts` or add a focused test.
- Navigate to `/cockpit` in the desktop project.
- Assert cockpit root/grid are visible.
- Assert `[data-testid="cockpit-worldmap"]` is visible.
- Assert exactly one `canvas` exists inside the cockpit map cell.
- Assert `[data-testid="version-badge"]` is visible within the cockpit page. This catches the original issue class: features shipped inside `WorldMap` appear in cockpit.
- Assert `[data-testid="map-ghost-layer"]` is either visible during warmup or absent after handoff; do not make this timing-sensitive in the shell test.

Mobile cockpit map unification:

- Add a test that runs only for the mobile project or uses an explicit mobile viewport.
- Navigate to `/cockpit`.
- Assert `[data-testid="cockpit-mobile-layout"]` and `[data-testid="cockpit-mobile-worldmap-region"]` are visible.
- Assert exactly one `canvas` exists inside the mobile worldmap region.
- Assert `[data-testid="version-badge"]` is visible.
- Click the collapse toggle, assert `data-collapsed` changes, then assert the canvas count is still one.
- Optionally swipe/scroll the pager and assert active page changes while canvas count remains one.

Root/cockpit parity smoke:

- Add a small parity helper or two separate assertions:
- `/` renders one canvas and `version-badge`.
- `/cockpit` renders one canvas and `version-badge`.
- Avoid screenshot pixel matching as a required gate. Pixi timing and animation make it flaky. Use DOM/canvas existence and shared test ids.

Gate-skip behavior:

- Existing canvas warmth tests skip unless `VITE_CLANWORLD_DEMO_MODE=false`; keep that pattern.
- New cockpit shell/parity tests should run in default demo mode, because the goal is component mounting and shared overlays, not live backend data.
- If a test needs cached snapshot behavior, copy the skip-gating style from `06-canvas-warmth.spec.ts`; otherwise avoid that dependency.

Manual verification:

- Run Playwright desktop and mobile projects for the cockpit shell spec.
- Open `/` and `/cockpit` locally; confirm pan, zoom, ghost handoff, version badge, HUD overlays, and event ticker are present.
- In mobile emulation, confirm the collapse pill sits above the map and does not block pan/pinch outside the pill.
- In a deployed/staging build, open `https://app.clan-world.com/` and `https://app.clan-world.com/cockpit` and compare the map features manually.

## 7. Rollout

Use a single PR. The desired code delta should be small: direct component reuse plus tests. A phased rollout or feature flag would keep two map paths alive and undermine the goal.

Recommended PR shape:

1. Confirm cockpit desktop and mobile map regions mount direct `WorldMap`.
2. If needed, remove any cockpit-specific map clone or placeholder implementation.
3. Optionally introduce a tiny `CockpitWorldMapFrame` to share border/frame markup, with no map behavior.
4. Ensure root and cockpit both use `WorldMapBoundary`.
5. Add Playwright coverage for desktop and mobile cockpit map mounting and `version-badge` presence.
6. Run targeted checks:
   - `pnpm --filter @clan-world/web build`
   - `pnpm --filter @clan-world/web test:e2e -- 04-cockpit-shell.spec.ts` or the repo's current equivalent script/filter
   - mobile Playwright project for the new cockpit mobile test

No feature flag:

- A flag would require maintaining both old and new map surfaces.
- The cockpit route is already intended to show the live map, and `WorldMapBoundary` provides a degraded fallback for map failures.
- If a rollback is needed, revert the PR.

Android webview verification without a device:

- Treat `/cockpit` mobile Playwright coverage as the main automated proxy.
- Check `apps/web/public/manifest.webmanifest` start URL remains `/cockpit`.
- Use Chromium Pixel 5 emulation for layout, touch, safe-area-adjacent behavior, and canvas presence.
- If the native Android app has CI/emulator coverage available, add it only if cheap. Do not block the web unification PR on device lab access.

## 8. Risks, Alternatives Considered, and Rejections

Risk: `WorldMap` overlays may feel crowded inside cockpit map cells.

- The root map includes `TopHud`, `EventTicker`, `WorldNoticePanel`, selected-clan HUD, no-chain placeholder, ghost layer, and `VersionBadge`.
- This is intentional for identical map behavior.
- If cockpit wants to hide some overlays, that is a product decision for a later issue. Hiding them now would recreate divergence.

Risk: duplicate Convex reads in cockpit.

- `WorldMap` and `CockpitHeader` both read snapshot data.
- This is existing cockpit chrome behavior, not introduced by map unification.
- Avoid adding more subscriptions in this PR. Consider a later shared snapshot provider only if profiling shows it matters.

Risk: WebGL/GPU pressure on Android.

- `WorldMap` is heavier than a bespoke lightweight cockpit map.
- The canonical map already has mitigations: DPR cap, ghost layer, WebGL context loss boundary, cleanup, and single-instance mount.
- The PR must verify no double mount and no remount during mobile layout interactions.

Risk: mobile resize/collapse stutter.

- Collapsing the mobile pager changes map height and triggers Pixi resize/relayout.
- This is acceptable if it resizes the existing app. Do not key/remount the map on collapse changes.

Risk: shared viewport state between root and cockpit surprises users.

- This is acceptable for "same map everywhere" and helps the ghost/canvas handoff stay consistent.
- Revisit only if UX explicitly asks for independent cockpit viewport memory.

Alternative rejected: duplicate `WorldMap` into a cockpit clone.

- This directly violates the issue goal.
- Version badge and future features would continue to require two edits.
- It doubles the maintenance surface.

Alternative rejected: extract `WorldMapCanvas` now and make `WorldMap` a desktop wrapper.

- This may be a good long-term architecture, but it is too broad for this issue.
- The current `WorldMap` owns both Pixi internals and user-visible map overlays. Splitting it now risks regressions in ghost layer, viewport restore, WebGL cleanup, and HUD behavior.
- It would force premature API design for overlay visibility and data ownership.

Alternative rejected: render root `/` inside cockpit via iframe.

- This would isolate state badly, create nested browsing context issues, add another Convex/Pixi lifecycle, complicate touch handling, and harm Android performance.

Alternative rejected: feature flag the unified map.

- It preserves the old divergent path and increases test matrix size.
- The route-level error boundary is the rollback safety net; source control handles full rollback.

Alternative rejected: hoist one persistent `WorldMap` above route branches and visually move it between root/cockpit.

- This would fight the current path-based routing model.
- It risks hidden maps, stale chrome, complex layout portals, and harder lifecycle cleanup.
- A clean mount per route is simpler and safer as long as only one map exists at a time.

