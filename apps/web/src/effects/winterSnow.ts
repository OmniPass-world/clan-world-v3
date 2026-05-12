import { Application, Container, Graphics, Sprite, Texture } from 'pixi.js';

/**
 * Winter snowfall particle overlay.
 *
 * Hand-rolled (no `@pixi/particle-emitter` dependency) — we pre-allocate a
 * pool of `Sprite` instances sharing one generated white-circle texture and
 * recycle them as they exit the bottom of the viewport. The emitter renders
 * in screen space (added as a direct child of `app.stage`, not the
 * pixi-viewport) so particles stay visually consistent regardless of
 * pan / zoom.
 *
 * Lifecycle:
 *   - `createWinterSnow(app, opts)` builds the pool + texture, registers a
 *     ticker callback, attaches a screen-space Container above the viewport,
 *     and returns a `WinterSnowHandle`.
 *   - Call `handle.setActive(true | false)` when `worldSnapshot.winterActive`
 *     flips. The container fades over ~1s on deactivate; ticker stops once
 *     fade completes.
 *   - Call `handle.destroy()` on Pixi teardown to remove the ticker callback
 *     and dispose the container/texture.
 *
 * Performance:
 *   - One generated 4×4 white-disc texture, shared by all sprites.
 *   - ~100 sprites at default density on a typical desktop viewport.
 *   - Per-frame work is purely position math (`x += vx`, sine-of-time drift) —
 *     no allocations, no Graphics redraws.
 *
 * Layer order:
 *   - Stage children: [viewport, snowContainer] — snow renders above the
 *     world (terrain, regions, bubbles) but below the React DOM UI overlay
 *     (TopHud, EventTicker, status badges), which is Liam's design intent.
 */

export interface WinterSnowOptions {
  /** Approximate particle count. The pool is pre-allocated to this size. */
  particleCount?: number;
  /** Vertical fall speed range in px/sec (min, max). */
  fallSpeedRange?: [number, number];
  /** Horizontal drift amplitude in px. Sway = sin(time + seed) * amplitude. */
  driftAmplitude?: number;
  /** Particle radius range in px (min, max). Larger = closer flakes. */
  radiusRange?: [number, number];
  /** Base alpha range (min, max). */
  alphaRange?: [number, number];
  /** Fade-out duration in ms when deactivating. */
  fadeOutMs?: number;
}

interface SnowParticle {
  sprite: Sprite;
  /** Phase seed for sine drift — keeps each flake out of lockstep. */
  seed: number;
  /** Vertical velocity in px/sec. */
  vy: number;
  /** Base x (drift is added on top of this). */
  baseX: number;
  /** Drift amplitude assigned per-particle so flakes don't sway identically. */
  amp: number;
  /** Per-particle drift frequency multiplier. */
  freq: number;
  /** Base alpha for this particle (so they're not all uniform). */
  baseAlpha: number;
}

export interface WinterSnowHandle {
  container: Container;
  /** Enable / disable snowfall. Idempotent. Fades over `fadeOutMs` on disable. */
  setActive: (active: boolean) => void;
  destroy: () => void;
}

const DEFAULTS = {
  particleCount: 100,
  fallSpeedRange: [40, 90] as [number, number],
  driftAmplitude: 18,
  radiusRange: [1.0, 2.4] as [number, number],
  alphaRange: [0.45, 0.95] as [number, number],
  fadeOutMs: 1000,
};

/** Pick a uniform random number in [min, max]. */
function rand(min: number, max: number): number {
  return min + Math.random() * (max - min);
}

/**
 * Build a small white-disc texture once via `Graphics` → `generateTexture`,
 * shared by every particle Sprite. Pixi 8 `Graphics` uses the `circle()` +
 * `fill()` fluent API; resolution=2 keeps the disc crisp on hi-dpi.
 */
function buildSnowflakeTexture(app: Application): Texture {
  const g = new Graphics();
  // 4px radius gives us headroom for the upper end of `radiusRange` — sprites
  // are scaled down per-particle, so oversampling here yields crisper output
  // than scaling up a tiny texture.
  g.circle(0, 0, 4).fill({ color: 0xffffff, alpha: 1 });
  const tex = app.renderer.generateTexture({ target: g, resolution: 2 });
  g.destroy();
  return tex;
}

/**
 * Reset one particle to the top of the viewport with a fresh randomised x,
 * speed, and drift profile. Called both on initial pool fill and when a
 * particle exits the bottom of the screen (recycling — no GC churn).
 */
function spawnParticle(
  p: SnowParticle,
  screenW: number,
  screenH: number,
  opts: Required<WinterSnowOptions>,
  initial: boolean,
): void {
  p.baseX = rand(-20, screenW + 20);
  // On first spawn, distribute particles randomly across the screen so the
  // effect doesn't have a "waterfall building up" flash on activate. On
  // recycling, start just above the top edge.
  const y = initial ? rand(-screenH * 0.2, screenH) : rand(-30, -5);
  p.sprite.x = p.baseX;
  p.sprite.y = y;
  p.vy = rand(opts.fallSpeedRange[0], opts.fallSpeedRange[1]);
  const r = rand(opts.radiusRange[0], opts.radiusRange[1]);
  // Texture native radius is 4 (see buildSnowflakeTexture); scale to target.
  p.sprite.scale.set(r / 4);
  p.baseAlpha = rand(opts.alphaRange[0], opts.alphaRange[1]);
  p.sprite.alpha = p.baseAlpha;
  p.seed = Math.random() * Math.PI * 2;
  p.amp = rand(opts.driftAmplitude * 0.4, opts.driftAmplitude);
  // Slow flakes drift slower; fast ones can sway faster. Map vy → freq.
  p.freq = rand(0.6, 1.6);
}

export function createWinterSnow(
  app: Application,
  options: WinterSnowOptions = {},
): WinterSnowHandle {
  const opts: Required<WinterSnowOptions> = { ...DEFAULTS, ...options };

  const container = new Container();
  // `eventMode = 'none'` ensures snow never intercepts pointer events meant
  // for clan bases / regions underneath.
  container.eventMode = 'none';
  container.visible = false;
  container.alpha = 0;

  const texture = buildSnowflakeTexture(app);
  const particles: SnowParticle[] = [];

  let screenW = app.renderer.screen.width;
  let screenH = app.renderer.screen.height;

  for (let i = 0; i < opts.particleCount; i++) {
    const sprite = new Sprite(texture);
    sprite.anchor.set(0.5);
    container.addChild(sprite);
    const p: SnowParticle = {
      sprite,
      seed: 0,
      vy: 0,
      baseX: 0,
      amp: 0,
      freq: 1,
      baseAlpha: 1,
    };
    spawnParticle(p, screenW, screenH, opts, true);
    particles.push(p);
  }

  /**
   * Activation state machine: `idle` (hidden, ticker not running) →
   * `active` (visible, full alpha, ticker animating) → `fading` (visible,
   * alpha ramping to 0, ticker still animating) → `idle`. Re-activating
   * mid-fade cancels the fade and ramps back up from the current alpha
   * (no blink), and the drift clock is preserved across cycles so flake
   * sine phase doesn't snap.
   */
  type Phase = 'idle' | 'active' | 'fading';
  let phase: Phase = 'idle';
  // Drift clock — monotonic across the lifetime of this handle. Never reset
  // after construction; resetting it would discontinuously snap every flake's
  // horizontal sine drift on every activate/deactivate (visible jump).
  const startMs = performance.now();
  // Envelope clocks — separate from `startMs` so fade timing can resume from
  // any current alpha without disturbing the drift phase. The fade-in/out
  // envelopes are parameterised so that `alpha(fadeStartMs) === fadeStartAlpha`
  // and `alpha(fadeStartMs + duration*(1 - startAlpha)) === target`, i.e. the
  // remaining-duration math accounts for the alpha we're already at.
  const FADE_IN_MS = 400;
  let fadeInStartMs = 0;
  let fadeInStartAlpha = 0;
  let fadeOutStartMs = 0;
  let fadeOutStartAlpha = 0;
  let tickerRegistered = false;

  const tick = (): void => {
    const now = performance.now();
    const dtMs = app.ticker.deltaMS;
    const dtSec = dtMs / 1000;

    // Idle short-circuit — skip dims read + envelope math + particle loop.
    if (phase === 'idle') {
      return;
    }

    // Track latest screen dims — cheap, supports resize without an explicit
    // re-init pass. Used to recycle particles + clamp baseX on respawn.
    screenW = app.renderer.screen.width;
    screenH = app.renderer.screen.height;

    // Alpha envelope for fade-out / fade-in. Both envelopes are computed from
    // the alpha value at the start of the fade — so a fade-in that begins at
    // alpha=0.6 only needs 40% of FADE_IN_MS to reach 1.0, and a fade-out from
    // alpha=0.3 only needs 30% of fadeOutMs to reach 0. This makes mid-fade
    // direction reversals seamless (no blink).
    if (phase === 'fading') {
      const remainingMs = opts.fadeOutMs * fadeOutStartAlpha;
      const fadeT = remainingMs > 0 ? (now - fadeOutStartMs) / remainingMs : 1;
      if (fadeT >= 1) {
        container.alpha = 0;
        container.visible = false;
        phase = 'idle';
        // Leave ticker registered — registering/unregistering Pixi tickers
        // every season swap is more expensive than letting the callback
        // early-return while idle.
        return;
      }
      container.alpha = fadeOutStartAlpha * (1 - fadeT);
    } else {
      // phase === 'active' — fade IN from current alpha to 1.0.
      const remainingMs = FADE_IN_MS * (1 - fadeInStartAlpha);
      const fadeT = remainingMs > 0 ? Math.min(1, (now - fadeInStartMs) / remainingMs) : 1;
      container.alpha = fadeInStartAlpha + (1 - fadeInStartAlpha) * fadeT;
    }

    // Per-particle: gravity-driven fall + sine drift, recycle on bottom exit.
    // tSec uses the monotonic `startMs` so sine phase is continuous across
    // any number of activate/deactivate cycles (no horizontal jump).
    const tSec = (now - startMs) / 1000;
    for (const p of particles) {
      p.sprite.y += p.vy * dtSec;
      p.sprite.x = p.baseX + Math.sin(tSec * p.freq + p.seed) * p.amp;
      // Recycle past bottom — small slack so the recycle frame isn't visible.
      if (p.sprite.y > screenH + 10) {
        spawnParticle(p, screenW, screenH, opts, false);
      }
    }
  };

  app.ticker.add(tick);
  tickerRegistered = true;

  const setActive = (active: boolean): void => {
    if (active) {
      if (phase === 'active') return;
      // Preserve the current alpha as the fade-in start — if we were mid
      // fade-out at e.g. alpha=0.5, the fade-in resumes from 0.5 (no blink).
      // If we were idle (alpha=0), this is the normal cold-start case.
      fadeInStartAlpha = container.alpha;
      fadeInStartMs = performance.now();
      phase = 'active';
      container.visible = true;
      // NOTE: `startMs` (drift clock) is intentionally NOT touched here —
      // resetting it would snap every particle's sine phase, causing all 100
      // flakes to horizontally jump on every reactivate.
    } else {
      if (phase === 'idle') return;
      // Mid fade-in or already fading — start a fresh fade-out from the
      // current alpha. This is symmetric with the fade-in resume above and
      // also prevents a blink if a season flip happens during fade-in.
      fadeOutStartAlpha = container.alpha;
      fadeOutStartMs = performance.now();
      phase = 'fading';
    }
  };

  const destroy = (): void => {
    if (tickerRegistered) {
      app.ticker.remove(tick);
      tickerRegistered = false;
    }
    container.destroy({ children: true });
    texture.destroy(true);
  };

  return { container, setActive, destroy };
}
