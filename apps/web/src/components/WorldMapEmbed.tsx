import { WorldMap } from '../WorldMap';
import { WorldMapBoundary } from './cockpit/shared/WorldMapBoundary';

/**
 * Sanctioned route-level mount for the canonical world map surface.
 *
 * Phase 1.11 (issue #354) URL rename: this is now used by the `/map` route
 * only. The cockpit at `/` embeds the map via `<WorldMapIframe />` pointing
 * at `/map`, and the Android `MapWebView` loads the same `/map` URL — so
 * every map render in the app comes through this component exactly once
 * per surface.
 */
export function WorldMapEmbed() {
  return (
    <WorldMapBoundary>
      <WorldMap />
    </WorldMapBoundary>
  );
}
