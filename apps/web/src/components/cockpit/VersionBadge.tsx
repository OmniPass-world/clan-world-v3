/**
 * VersionBadge — subtle top-right overlay on the map area showing the build
 * version. Sourced from `VITE_APP_VERSION` injected at build time (set by
 * `.github/workflows/deploy-prod.yml` to the pushed tag, e.g. `v2.2.0`).
 *
 * Why: Vercel has shipped stale builds without anyone noticing during UAT.
 * A visible version string makes that class of regression instantly obvious.
 *
 * Falls back to `'dev'` so local browsing never shows an empty/blank badge.
 * See issue #312.
 */
export function VersionBadge() {
  const version = import.meta.env.VITE_APP_VERSION || 'dev';
  return (
    <div
      data-testid="version-badge"
      style={{
        position: 'absolute',
        top: 8,
        right: 8,
        padding: '2px 6px',
        fontFamily: 'monospace',
        fontSize: 10,
        color: '#e8e2c8',
        background: 'rgba(13, 26, 13, 0.35)',
        borderRadius: 3,
        opacity: 0.6,
        pointerEvents: 'none',
        zIndex: 2,
        letterSpacing: '0.02em',
      }}
    >
      {version}
    </div>
  );
}
