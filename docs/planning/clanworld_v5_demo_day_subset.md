# ClanWorld v5 — Demo-Day Animation Subset

Status: Hackathon scope-cut, hard prioritization
Deadline: **May 3, noon ET** (~22h from this doc)
Source: `clanworld_v5_animation_spec.md` (full v5 north star — post-hackathon target)
Owner: Frontend implementation agent, working in `apps/web/src/WorldMap.tsx` and adjacents.

This doc answers one question: *"Of the v5 spec, what do we actually ship by tomorrow noon?"*

The DA review on v5 (codex 5.5 + gemini-3-pro) converged on **NEEDS WORK due to scope** — the full spec is 3-6 weeks of focused frontend + art work. This subset is the ruthless cut. It's the v5 *vibe* on a 22h budget.

-----

## 0. The premium-feel checklist for demo

If these seven things are true on stage tomorrow, the demo *feels* like the v5 spec without being it:

- [ ] Buildings breathe — invisible-until-missing 1px sway
- [ ] Phase-offset env motion — wheat / smoke / waves do NOT march in sync
- [ ] Day/night cycle — visible without being jarring; window glow at night
- [ ] Carry indicators above each gathering clansman — fill bar visualizes the loop
- [ ] Tap-to-zoom + selection ring — players can navigate the world cleanly
- [ ] Counter ticks on resource changes (rolling number + delta floater)
- [ ] One combat moment — short vignette near tick close, NOT full-tick choreography

Everything else from v5 ships post-hackathon.

-----

## 1. Ship list

### 1.1 Z-sort architecture fix (BLOCKING — do first)

**Source:** v5 §14 (now corrected in main spec).
**Why first:** Every other animation depends on entities Y-sorting correctly. If clansmen render on top of buildings unconditionally (the current bug), nothing else looks right.

**Implementation:**
- Single `worldDynamic` Pixi Container with `sortableChildren = true`.
- Environment motion sprites (wheat, waves), buildings, clansmen, bandits all parent into `worldDynamic`.
- Each entity sets `zIndex = Math.round(entity.y)` on every position update.
- Building overlays (smoke, banners, glow) become **children** of their host building, NOT siblings.
- Carry indicators become **children** of their host clansman, NOT siblings.

**Effort:** 2-3h. Refactor of existing WorldMap.tsx layer creation. Risky if poorly done; use a test region with ≥3 buildings + ≥3 clansmen at varying Y to verify visually.

**Acceptance:** A clansman walking south past a building's south face renders on top; walking north past the building's north face renders behind. Test with the existing dev seed.

### 1.2 Building breathe

**Source:** v5 §5.1.
**Why high-ROI:** Single biggest premium-feel cue. Code is ~10 lines.

**Implementation:**
```typescript
// Per building, assigned once at spawn:
building.userData.phaseOffset = ((gridX * 73) ^ (gridY * 31)) % 4000

// On each render frame:
building.y = baseY + Math.round(Math.sin((now + phaseOffset) / 4000) * 1)
```

`baseY` is the building's authoritative Y position. The 1px offset is render-only. Round to integer pixels.

**Effort:** 30min.

**Acceptance:** Pause the game (zero state changes). Watch the screen for 8 seconds. Buildings should visibly breathe at desync'd phases. If they all rise and fall together → phaseOffset is broken.

### 1.3 Phase-offset on existing env motion

**Source:** v5 §1.4.
**Why:** Cheap fix that prevents the "marching band" tell.

**Implementation:** Audit existing env motion sprites in WorldMap.tsx (smoke, wheat, waves if present). Each sprite computes its animation frame from:
```typescript
const phaseOffset = ((gridX * 73) ^ (gridY * 31)) % 1000
const frame = Math.floor((now + phaseOffset) / (1000 / fps)) % frameCount
```

If env motion isn't yet implemented for any element, do NOT add new motion as part of this task — the breathe in §1.2 is enough ambient motion.

**Effort:** 30min if env motion exists; skip if not.

**Acceptance:** Zoom into a wheat field (or smoke chimneys, or waves). Adjacent tiles should be on different frames at any given moment.

### 1.4 Day/night ColorMatrixFilter + window glow

**Source:** v5 §10.
**Why:** Single GPU filter, atmospheric, sells "world" not "diorama."

**Implementation:**
- One `PIXI.ColorMatrixFilter` on `worldDynamic` and `terrainBackground`. NOT on HUD.
- 30 ticks per cycle. Use existing `currentTick` from snapshot + sub-tick interpolation.
- 4 keyframes (dawn / day / dusk / night) per v5 §10.2. Lerp between them.
- Window glow: each base has a single overlay sprite, alpha = `clamp(1 - daylightBrightness, 0, 1)`.

**Effort:** 2h (filter + keyframe lerp). Window glow sprite per base needs ONE 24x24 art asset (yellow-window overlay) — author or paint-by-hand if no one's available.

**Skip if:** Window glow art doesn't exist by T-12h. Ship just the filter.

**Acceptance:** Sit on the world view for 4 minutes (= 1 day cycle on Sub2). Watch the world tint go through warm → neutral → amber → blue → back. At night, base windows glow if asset exists.

### 1.5 Carry indicators (wheelbarrow / fill bar)

**Source:** v5 §6.4.
**Why:** Visualizes the gathering loop — players can SEE clansmen accumulating resources. Without this, the gathering economy is invisible.

**Implementation:**
- Per clansman with `carry.* > 0`: render a child sprite 4px above the head.
- Fill bar: 16px × 3px. Background: ink color. Fill: parchment cream, width = `16 * fillRatio`.
- `fillRatio = Math.max(carry.wood / WOOD_CAP, carry.iron / IRON_CAP, carry.wheat / WHEAT_CAP, carry.fish / FISH_CAP)`.
- Animate fill changes: 400ms easeOutQuad on growth, 200ms easeInQuad on drain.
- Hide entirely when `fillRatio === 0`.

**Effort:** 1.5h. Pure code, no art. If a wheelbarrow icon exists, use it; else just the bar.

**Acceptance:** Send a clansman to gather wood. Watch the bar fill as the gather mission progresses. Watch it drain when they deposit at the base.

### 1.6 Tap-to-zoom + selection ring

**Source:** v5 §8.1, §13.1.
**Why:** Without selection feedback, the world feels uninspectable. With it, the demo presenter can "show me Clan 3" naturally.

**Implementation:**
- On sprite tap (clansman, building): tween camera over 400ms easeInOutQuad to `{x: sprite.x, y: sprite.y, zoom: 2.0}`.
- Spawn a `selectionRing` sprite as a child of the tapped sprite. Ring rotates at 1Hz, pulses alpha at 0.5Hz.
- Tap on empty terrain: do nothing (don't deselect).
- Esc key or "Clear Selection" button: hide ring, zoom out to fit-world over 400ms.

**Effort:** 2h. Selection ring needs ONE 32x32 art asset (rotating dashes). If not authorable, use a simple PIXI.Graphics circle with stroke dasharray — uglier but functional.

**Acceptance:** Tap a clansman — camera centers + zooms; ring appears. Tap another — camera moves to new target; ring follows. Press Esc — zooms out.

### 1.7 Counter ticks (rolling number + delta floater)

**Source:** v5 §12.1.
**Why:** Resource counters are the most-watched HUD element. Static numbers feel cheap; rolling numbers feel premium.

**Implementation:**
- Per HUD counter (wood, iron, wheat, fish, gold per clan): wrap in a `<RollingNumber>` React component.
- On value change: tween from old to new over `min(400ms, 100ms + log(|delta|) * 40ms)` with easeOutQuad.
- Spawn a `+N` or `-N` floater above the counter. Drift up 16px, fade alpha 1 → 0 over 800ms. Green for positive, red for negative.

**Effort:** 1.5h. Pure React + CSS. No art needed.

**Acceptance:** Clansman deposits 50 wood. Wood counter rolls from old to new+50 over ~300ms. A green "+50" rises above the counter and fades.

### 1.8 Combat vignette (the codex alternative)

**Source:** v5 §9, modified per codex DA recommendation (Alternative #2).
**Why:** Full-tick choreography is 1+ week of work. A 3-4s vignette near tick close gets the "combat happened!" beat without the cost.

**Implementation:**
- When the next tick contains a known combat outcome: at `tickClose - 4s`, fade dim overlay (alpha 0 → 0.55 over 600ms) on `worldDynamic` (NOT on `combatHighlight`).
- Reparent combatants to `combatHighlight` per v5 §14.3.
- Combatants do a simple advance + clash + flash:
  - 0-1500ms: walk toward base center.
  - 1500-2000ms: idle at center, brief.
  - 2000-2200ms: full-screen white flash (200ms in, 200ms out).
  - 2200ms+: resolution per v5 §9.10 (success: bandits death + cheer) or §9.11 (failure: clansmen knockback + wall drop). Cap entire resolution at 1500ms.
- At tick close: dim fades out, combatants reparent back to `worldDynamic`.

**Effort:** 3h. Skip motion blur, skip whirlwind, skip slow-circle. Just dim → advance → flash → resolution.

**Skip if:** T-6h and items 1.1-1.7 aren't all done. This is the LAST priority.

**Acceptance:** Trigger a combat tick (use the existing dev seed). At tick - 4s, world dims, combatants advance, flash, resolve. Whole thing fits in the 4 seconds before tick close.

-----

## 2. Skip list — explicit non-goals

These are out-of-scope for tomorrow. They land post-hackathon as part of v5 proper.

- **Strategic 8x8 atlas (v5 §1.5):** No art bandwidth to author. Detail atlas at all zooms; tolerate downscale ugliness at zoom <1x.
- **Full combat choreography 10-phase (v5 §9):** Replaced by §1.8 vignette.
- **Submission 2 transfer demo cinematic (v5 §11.5):** Depends on iNFT transfer mechanic existing in chain client. Defer to v1.2 if Track 2 is even pursued.
- **Cross-fade transitions (v5 §3):** Per gemini DA — cross-fading two pixel sprites looks ghosted, not premium. Use snap with anticipation frame later when art exists; for demo, just snap.
- **Bandit threat 20s sequence (v5 §11.1):** Single skull icon over targeted base + the §1.8 vignette is enough.
- **Monument tier-up cinematic (v5 §11.4):** No camera takeover. Standard upgrade burst (v5 §11.3) reused if monument levels up.
- **Speech bubble anti-occlusion (v5 §7.4):** Static positioning above sprite head is fine for demo. The O(N²) layout dance comes later.
- **Per-tier sprite frame counts (v5 §2.3, §2.4):** Use whatever sprites exist now. If no walk/idle/work cycles, sprites stay 1-frame.
- **Particle pool of 32 (v5 §6.3):** Spawn fresh containers; if perf cratered, reconsider. 4-second vignette + counter floaters won't exhaust GC.
- **Asset pipeline validation (v5 §17):** Manual atlas naming. CI validation post-hackathon.

-----

## 3. Effort budget

| Item | Effort | Hard-deadline cut? |
|---|---|---|
| 1.1 Z-sort fix | 2-3h | NO — blocking, do first |
| 1.2 Building breathe | 0.5h | NO — too cheap to skip |
| 1.3 Phase-offset env | 0.5h (or skip) | YES if no env motion exists |
| 1.4 Day/night + window glow | 2h | window glow YES if no art |
| 1.5 Carry indicators | 1.5h | NO — gathering is invisible without |
| 1.6 Tap-zoom + selection ring | 2h | ring art YES if no art |
| 1.7 Counter ticks | 1.5h | NO — pure code |
| 1.8 Combat vignette | 3h | YES if T-6h and 1.1-1.7 incomplete |

**Total**: 13-13.5h ship-everything, 8-9h ship-minus-vignette.

With 22h to deadline, there's slack for one round of "it doesn't look right, redo." Don't burn it on §1.8 if §1.1 isn't solid.

-----

## 4. Files to touch

Primary:
- `apps/web/src/WorldMap.tsx` — Pixi scene graph, layer restructure (§1.1), breathe (§1.2), phase-offset (§1.3), day/night filter (§1.4), carry indicators (§1.5), tap-to-zoom (§1.6), combat vignette (§1.8).

Secondary:
- `apps/web/src/components/cockpit/*` — counter ticks (§1.7) wherever resource counters live.
- `apps/web/public/assets/` (or wherever atlases go) — window glow + selection ring sprites if authored.

Out of scope:
- `apps/server/convex/*` — animation is pure rendering. NO state-shape changes.
- `packages/contracts/*` — animation is pure client. NO contract changes.

-----

## 5. Validation before demo

Run through the §0 checklist with someone who hasn't seen the implementation. If they pause on any line, that line is the regression.

If short on time, skip §1.8 (combat vignette) and ship 1.1-1.7. The world will still feel premium even without the combat moment, because the breathe + day/night + carry indicators are doing the heavy lifting.

-----

## Appendix: DA review pointers

Full reviews:
- `/tmp/da-clanworld-v5-codex.md` — codex 5.5 (recommends tiered split, short-form combat, delayed-visual-tick netcode pattern)
- `/tmp/da-clanworld-v5-gemini-pro.md` — gemini 3 pro (caught Z-sort layer bug — fix is now in v5 §14, plus warned about cross-fade ghosting)

Both reviewers converged on **NEEDS WORK due to scope** — this subset is the response.
