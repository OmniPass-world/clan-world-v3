import { WorldMap } from '../WorldMap';
import { WorldMapBoundary } from './cockpit/shared/WorldMapBoundary';

/**
 * Sanctioned route-level mount for the canonical world map surface.
 * Use this for /, /cockpit, and Android webview-backed cockpit routes.
 */
export function WorldMapEmbed() {
  return (
    <WorldMapBoundary>
      <WorldMap />
    </WorldMapBoundary>
  );
}
