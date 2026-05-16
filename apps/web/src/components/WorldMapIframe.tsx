/**
 * Cockpit-mounted world map, served via an `<iframe src="/map" />`.
 *
 * Phase 1.11 (issue #354) introduced the URL rename:
 *   - `/`     → Cockpit (this surface's parent)
 *   - `/map`  → Raw world map (this iframe's target)
 *
 * The cockpit iframes `/map` instead of mounting `<WorldMap />` directly so
 * the web cockpit and the Android `MapWebView` share the exact same render
 * pipeline (one Pixi canvas + one set of overlays), keeping the two
 * surfaces visually identical and avoiding canvas-context contention if
 * the parent ever mounted two `<WorldMap />` instances simultaneously.
 *
 * The `data-testid` is preserved from the previous direct-mount path so
 * existing e2e selectors (`page.getByTestId('cockpit-worldmap')`) continue
 * to match without churn — they just look at the iframe wrapper now and
 * inspect the canvas inside via `.contentFrame().locator('canvas')` when
 * pixel-level checks are needed.
 */
export function WorldMapIframe() {
  return (
    <iframe
      src="/map"
      title="Clan World map"
      data-testid="cockpit-worldmap-iframe"
      style={{
        width: '100%',
        height: '100%',
        border: 'none',
        display: 'block',
        background: '#000',
      }}
      // Allow the embedded Pixi canvas + Convex client to do their thing
      // without sandbox restrictions. The iframe is same-origin so no
      // cross-origin permissions are required.
      allow="autoplay; fullscreen"
    />
  );
}
