import { test, expect, type Page } from '@playwright/test';

async function stubTerminalIframes(page: Page): Promise<void> {
  await page.route('**/cockpit.clan-world.com/elder-*-tty/**', (route) =>
    route.fulfill({
      status: 200,
      contentType: 'text/html',
      body: '<!DOCTYPE html><html><body data-stub="ttyd"></body></html>',
    }),
  );
}

/**
 * Phase 1.11 URL rename (issue #354):
 *   - `/`     → Cockpit (was `/cockpit`). Map is iframed in via `<iframe src="/map" />`.
 *   - `/map`  → Raw world map surface (was `/`).
 *   - `/cockpit` → client-side redirect to `/` for back-compat.
 *
 * These tests cover the new layout: same map render pipeline serves both
 * the cockpit's iframe and the standalone `/map` URL, so the version
 * badge + canvas count stay 1:1 across both surfaces.
 */
test.describe('unified world map routes', () => {
  test.beforeEach(async ({ page }) => {
    await stubTerminalIframes(page);
  });

  test('/map and the cockpit iframe expose the same version badge text', async ({ page }) => {
    await page.goto('/map');
    const mapBadge = page.getByTestId('version-badge');
    await expect(mapBadge).toBeVisible();
    await expect(page.locator('canvas')).toHaveCount(1, { timeout: 15_000 });
    const mapVersion = (await mapBadge.textContent())?.trim();
    expect(mapVersion).toBeTruthy();

    // Cockpit at `/` iframes `/map`. Find the iframe, then assert the
    // version badge inside it matches.
    await page.goto('/');
    const iframeLocator = page.getByTestId('cockpit-worldmap-iframe');
    await expect(iframeLocator).toBeVisible();
    const mapFrame = page.frameLocator('[data-testid="cockpit-worldmap-iframe"]');
    const cockpitBadge = mapFrame.getByTestId('version-badge');
    await expect(cockpitBadge).toBeVisible();
    await expect(mapFrame.locator('canvas')).toHaveCount(1, { timeout: 15_000 });
    await expect(cockpitBadge).toHaveText(mapVersion ?? '');
  });

  test('desktop cockpit mounts the world map via iframe to /map', async ({ page }, testInfo) => {
    test.skip(testInfo.project.name.includes('mobile'), 'desktop-only cockpit layout check');

    await page.goto('/');

    const mapCell = page.getByTestId('cockpit-worldmap');
    await expect(page.getByTestId('cockpit-root')).toBeVisible();
    await expect(page.getByTestId('cockpit-grid')).toBeVisible();
    await expect(mapCell).toBeVisible();

    // The cockpit no longer mounts <WorldMap /> directly — it embeds /map
    // via an iframe so web and Android share the same render path.
    const iframeEl = mapCell.getByTestId('cockpit-worldmap-iframe');
    await expect(iframeEl).toBeVisible();
    await expect(iframeEl).toHaveAttribute('src', '/map');

    // Canvas + version badge live INSIDE the iframe now. We must not see a
    // duplicate canvas in the outer document (that would mean WorldMap got
    // re-mounted in the cockpit by accident).
    await expect(page.locator('canvas')).toHaveCount(0);
    const mapFrame = page.frameLocator('[data-testid="cockpit-worldmap-iframe"]');
    await expect(mapFrame.locator('canvas')).toHaveCount(1, { timeout: 15_000 });
    await expect(mapFrame.getByTestId('version-badge')).toBeVisible();
  });

  test('mobile cockpit iframes /map through the collapse toggle', async ({ page }, testInfo) => {
    test.skip(!testInfo.project.name.includes('mobile'), 'mobile-only cockpit layout check');

    await page.goto('/');

    const layout = page.getByTestId('cockpit-mobile-layout');
    const mapRegion = page.getByTestId('cockpit-mobile-worldmap-region');
    const toggle = page.getByTestId('cockpit-mobile-collapse-toggle');

    await expect(layout).toBeVisible();
    await expect(mapRegion).toBeVisible();

    // Mobile cockpit also iframes /map; the canvas lives in the iframe.
    const iframeEl = mapRegion.getByTestId('cockpit-worldmap-iframe');
    await expect(iframeEl).toBeVisible();
    await expect(iframeEl).toHaveAttribute('src', '/map');

    const mapFrame = page.frameLocator('[data-testid="cockpit-worldmap-iframe"]');
    await expect(mapFrame.locator('canvas')).toHaveCount(1, { timeout: 15_000 });
    await expect(mapFrame.getByTestId('version-badge')).toBeVisible();

    const before = await layout.getAttribute('data-collapsed');
    await toggle.click();
    await expect(layout).not.toHaveAttribute('data-collapsed', before ?? '');
    // Iframe stays mounted through the collapse animation — no remount
    // (would re-warm Pixi otherwise).
    await expect(iframeEl).toBeVisible();
    await expect(mapFrame.locator('canvas')).toHaveCount(1);
  });

  test('legacy /cockpit redirects to /', async ({ page }) => {
    // 30-day back-compat window per issue #354. The legacy URL must end
    // at `/` (which renders the cockpit). The redirect is implemented
    // client-side in App.tsx via window.location.replace, so we just
    // assert the final pathname after the navigation settles.
    await page.goto('/cockpit');
    await expect.poll(() => new URL(page.url()).pathname, { timeout: 5_000 }).toBe('/');
    await expect(page.getByTestId('cockpit-root')).toBeVisible();
  });
});
