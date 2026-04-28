import { test, expect } from '@playwright/test';

// NOTE: Demo-mode wiring is Phase 4 work. Today the only demo-related env is
// `VITE_DEMO_BYPASS_WORLD_GUARD` (apps/web/src/App.tsx) which bypasses the
// "open in World App" guard but does NOT toggle a mock-clan dataset.
//
// `VITE_CLANWORLD_DEMO_MODE` referenced in the harness brief does not exist
// yet. These tests are placeholders documenting the contract; once the demo
// flag is wired they should be flipped from `test.skip` to live assertions.

test.describe('demo mode wiring (Phase 4)', () => {
  test.skip(
    'demo-mode=true → mock clans visible (e.g. Storm Riders)',
    async ({ page }) => {
      // Future shape:
      //   await page.goto('/');
      //   const bubbles = page.locator('[data-testid="clan-bubble"]');
      //   await expect(bubbles).toHaveCount(8);
      //   await expect(page.getByText(/Storm Riders/)).toBeVisible();
      void page;
    },
  );

  test.skip(
    'demo-mode=false → empty world map / "no chain data yet" placeholder',
    async ({ page }) => {
      // Future shape:
      //   await page.goto('/');
      //   const placeholder = page.getByText(/no chain data yet/i);
      //   const emptyMap = page.locator('[data-testid="world-map-empty"]');
      //   await expect(placeholder.or(emptyMap)).toBeVisible();
      void page;
    },
  );
});
