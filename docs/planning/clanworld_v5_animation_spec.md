# Clanworld Animation Spec — v0.1

Status: Draft, hackathon scope
Purpose: Define every animation, timing, easing, and effect in the Clanworld client. Make the difference between "an onchain game prototype" and "an actually premium game" explicit and implementable.
Audience: Frontend implementation agents working on the Pixi rendering layer. Asset authors producing Aseprite atlases. Anyone integrating React HUD with the Pixi canvas.
Companion to:

- clanworld_frontend_spec.md — overall frontend architecture
- clanworld_v4_5_alignment_addendum.md — authoritative cross-stream addendum
- IClanWorld.sol — contract seam interface

This spec covers what the screen should do. Why state changes happen lives elsewhere; this doc starts after state has been derived and asks "given the state, how does the screen look and move?"

-----

## 0. One-paragraph summary

Pixel art at 24x24 with 4–8 fps sprite animation, rendered through PixiJS v8 at 60fps on top of a smoothly panning camera. Three independent clocks govern motion: the game tick clock (truth), the animation frame clock (chunky pixel charm), and the render frame clock (smooth camera). Position is a pure function of state and time, never stored. Every visible element breathes — buildings sway 1px, wheat phases per-tile, smoke drifts, banners flutter. State transitions cross-fade or inherit; they never snap. Combat is the hero moment, choreographed across the full 60-second tick from dim → advance → circle → whirlwind → flash → resolution. Day/night cycles tint the world via a single ColorMatrixFilter with cozy window-glow at night. The whole system is engineered to look alive, even when paused.

-----

## 1. Foundational principles

### 1.1 The three clocks

This is the single most important architectural idea. There are three independent clocks running in the Pixi layer, and conflating them is the #1 source of janky pixel art.

|Clock                    |Source                                    |Drives                                       |
|-------------------------|------------------------------------------|---------------------------------------------|
|**Game tick clock**      |Contract tickEpoch (60s Sub2, 20s Sub1)   |Mission progress, arrival times, combat phase|
|**Animation frame clock**|`Date.now()` independently                |Sprite frame index, phase-offset env motion  |
|**Render frame clock**   |Pixi ticker (60fps target, 30fps fallback)|Camera, lerping, fades, filters              |

Concretely:

```
// Animation frame clock — independent
const animationFrame = Math.floor(now / (1000 / fps[spriteKey])) % frameCount[spriteKey]

// Game tick clock — derives from contract
const travelProgress = clamp(
  (currentTick - mission.startTick + (now - tickEpoch.atMs) / tickEpoch.durationMs)
    / (mission.arrivalTick - mission.startTick),
  0, 1
)

// Render frame clock — drives smooth update
app.ticker.add((delta) => updateScene(delta))
```

The animation frame clock can desync from render rate without anyone noticing — that's the *point*. Pixel art looks more alive when chunky 6fps walk plays over a 60fps smooth pan than when everything moves at the same rate.

### 1.2 Determinism is for state, not for animation

Anything derived from now (wander seed, animation frames, speech bubble fade-in) is pure visual sugar and can drift between clients. That's fine. What must be deterministic is the *contract-derived* state: who's where, what tick they arrived, what they carry. Position is a pure function of state + time, but the position itself doesn't need to match across clients to the pixel.

Don't fight time. Let Date.now() drive frames. Don't try to derive sprite frames from block timestamp.

### 1.3 "Alive" is the goal, not "accurate"

Pixel art that's perfectly still looks dead. Every visible thing should breathe — even "idle" buildings have a 1-pixel vertical sway every 4 seconds, smoke drifts, flags flutter. The enemy is the frozen frame. If you pause the simulation and the screen looks like a screenshot, the aesthetic has failed.

Corollary: don't render at sub-pixel positions. Round to whole pixels (`Math.floor` or Math.round on x/y). Sub-pixel motion looks blurry on pixel art and breaks the aesthetic worse than visible "stepping."

### 1.4 The phase-offset rule

When you have N copies of the same animation (a wheat field, a row of waves), every instance must have a per-instance phase offset based on its grid position:

```
const phaseOffset = ((gridX * 73) ^ (gridY * 31)) % 1000  // ms
const animationFrame = Math.floor((now + phaseOffset) / (1000 / fps)) % frameCount
```

Without this, your wheat field looks like a marching band. With it, it looks alive. Apply this to every repeating env element.

### 1.5 Two-resolution system

Already locked. Strategic atlas: 8x8 sprites for zoom 0.5x–1.5x. Detail atlas: 24x24 for zoom 1.5x–4.0x. Cross-fade 200ms at zoom = 1.5x boundary. Both atlases load eagerly at app start (~500KB total).

Pixel art doesn't downscale gracefully. A 24x24 sprite at 0.33x = 8x8 mush with bilinear, pixel noise with nearest. Hand-author the strategic atlas separately. The cost is asset work, the benefit is crisp pixel art at every zoom.

### 1.6 No snap, ever

Sprite state transitions cross-fade or inherit. They never snap. See §3.5 for the full transition rules. The "no snap" principle is what separates this from looking like a state machine on screen.

-----

## 2. Sprite reference

### 2.1 Sprite key derivation

Every sprite has a spriteKey derived from (role, status, subAction, direction):

```
clansman_idle
clansman_walk_ne
clansman_walk_se
clansman_carry_walk_ne
clansman_carry_walk_se
clansman_mine
clansman_chop
clansman_harvest
clansman_fish
clansman_build
clansman_attack
clansman_cheer
clansman_die
bandit_idle
bandit_march
bandit_attack
bandit_death
```

Each key maps to: `{ atlas, framePrefix, frameCount, fps, loop, anchor }`.

NW and SW directions are mirror-flips of NE and SE respectively (`sprite.scale.x = -1`). Author NE and SE only.

### 2.2 Direction logic (4-way 45°)

The world is rendered in 3/4 top-down perspective. Walk animations are authored at 45° angles: NE (up-and-right) and SE (down-and-right). NW and SW are mirrors.

```
function deriveWalkKey(dx: number, dy: number, lastDirection: Direction) {
  // Hysteresis prevents flicker at near-cardinal travel
  if (Math.abs(dx) < 1 && Math.abs(dy) < 1) return lastDirection

  const goingEast = dx > 0
  const goingSouth = dy > 0
  const verticalAxis = goingSouth ? 'se' : 'ne'

  return {
    spriteKey: `clansman_walk_${verticalAxis}`,
    flipX: !goingEast, // flip NE → NW, SE → SW
  }
}
```

For idle, hold the direction the sprite was last walking (don't snap to a default). Idle animation itself is authored facing SE (most natural "looking out" pose for top-down).

### 2.3 Frame counts and FPS — clansman (24x24 detail)

|Key            |Frames|FPS|Loop|Notes                             |
|---------------|------|---|----|----------------------------------|
|`idle`         |4     |4  |yes |Gentle bob, faces SE always       |
|`walk_ne`      |4     |8  |yes |NW = mirrored                     |
|`walk_se`      |4     |8  |yes |SW = mirrored                     |
|`carry_walk_ne`|4     |6  |yes |Slower, laden                     |
|`carry_walk_se`|4     |6  |yes |Slower, laden                     |
|`mine`         |6     |8  |yes |Pickaxe arc                       |
|`chop`         |6     |8  |yes |Axe arc                           |
|`harvest`      |4     |6  |yes |Bend-and-rise                     |
|`fish`         |6     |4  |yes |Cast-and-wait, slow               |
|`build`        |6     |6  |yes |Hammer rhythm                     |
|`attack`       |4     |8  |yes |Combat swing                      |
|`cheer`        |4     |6  |yes |Post-victory, 2 loops then idle   |
|`die`          |4     |6  |no  |Hold last frame, then sprite hides|

### 2.4 Frame counts and FPS — bandit (32x32 detail)

|Key     |Frames|FPS|Loop|Notes                                      |
|--------|------|---|----|-------------------------------------------|
|`idle`  |4     |4  |yes |Menacing pace at camp                      |
|`march` |4     |6  |yes |Travel between regions, also combat advance|
|`attack`|4     |8  |yes |Combat swing                               |
|`death` |6     |8  |no  |Knockback then white-flash then shrink-fade|

### 2.5 Building animations

Most buildings are 1-frame static, with a child overlay container that animates. This separates the "what's there" (state-driven) from "what's moving" (clock-driven).

|Element        |Container|Frames            |FPS|Notes                                |
|---------------|---------|------------------|---|-------------------------------------|
|Base structure |static   |1                 |—  |Sprite swap on level change          |
|Wall           |static   |1 per level       |—  |Sprite swap on level change          |
|Monument tower |static   |1 per level (0–10)|—  |Sprite swap, see upgrade anim        |
|Forge fire glow|overlay  |4                 |4  |Additive blend, on Mountains/Monument|
|Chimney smoke  |overlay  |4                 |4  |Phase-offset, alpha 0.7              |
|Workshop crane |overlay  |8                 |4  |Slow arc swing                       |
|Windmill blade |overlay  |4                 |6  |Rotation, on West Farms              |
|Banner flutter |overlay  |4                 |4  |Phase per banner                     |
|Window glow    |overlay  |1                 |—  |Alpha tied to day/night, see §10     |

### 2.6 Anchor points

- Clansmen and bandits: (0.5, 1.0) — bottom-center. Makes terrain "feet" placement trivial. Speech bubbles, selection rings, shadows all anchor relative to this.
- Buildings: (0.5, 1.0) — they "stand on" their tile.
- Speech bubbles: (0.5, 1.0) — grow upward from above sprite head.

### 2.7 Atlas layout (Aseprite handoff)

- Strategic atlas: 256×256 max, packed 8x8 sprites + 16x16 buildings + 8x8 region accents + 12x12 bandits. All 1-frame.
- Detail atlas: 1024×1024 max, packed 24x24 clansman cycles + 48x48 base / 32x64 monument tower / 32x16 wall + 32x32 bandit + 16x16 env motion.
- Export: PNG + JSON-Hash, 1x scale, trim enabled, padding 1px.
- Frame naming: clansman_walk_ne_0, clansman_walk_ne_1, etc.
- Animation metadata in separate animations.json so adding a frame doesn't require code change.
- Validation: pnpm validate-atlas checks every key in animations.json resolves to actual frames in the atlas. CI fails on mismatch.
- Hot reload: Vite watches assets/atlas/, reloads on change in dev.

-----

## 3. State transitions

### 3.1 Cross-fade vs inheritance vs play-through

Three patterns for transitioning between sprite keys:

Cross-fade — for distinct actions where snap would be jarring.

- Hold last frame of outgoing key for 1 frame.
- Fade outgoing alpha 1→0 over 150ms.
- Fade incoming alpha 0→1 over 150ms.
- Both render simultaneously during overlap.
- Used for: walk → mine, walk → chop, idle → walk.

Inheritance — for related cycles where frame index can carry forward.

- Same frame index in old key maps to same frame index in new key.
- No fade. Just swap the texture source.
- Visual continuity, no rebake.
- Used for: walk → carry_walk (both 4-frame walks), walk_ne → walk_se when direction changes.

Play-through — for transitions that should feel sudden.

- New animation begins immediately, no fade.
- Used for: idle → die, any * → attack in combat.

### 3.2 Direction flip rule

Direction flip via sprite.scale.x *= -1 is *never* faded. The instant flip is part of the pixel-art language. Fading a flip looks worse than letting it pop.

Apply the flip on the *next* render frame after the direction-change check fires (not mid-animation-frame), so the flip lands cleanly on a sprite frame boundary.

### 3.3 Arrival at destination

When travelProgress >= 1.0, transition to:

1. Snap position to region anchor (1 frame).
2. Force idle for 1 second (60 render frames at 60fps).
3. Then transition (cross-fade) to whatever the mission action is.

The 1-second idle beat sells "arrival." Without it, sprites blip from traveling to working with no visual moment.

### 3.4 Action completion

When a mission completes (e.g., wood capacity reached, mission ends in `WAITING`):

- Cross-fade 150ms to idle.
- Hold idle facing direction = last walked direction.

### 3.5 Death

clansman_die plays once (4 frames at 6fps = 667ms), holds last frame, then on next state read the sprite is removed from the scene (state shows clansman dead/absent). No fade, just disappear after the death anim's last frame.

-----

## 4. Movement contracts

### 4.1 Wander (status: WAITING / GATHERING / etc.)

Already specced as deterministic seeded wander shifting on a slow timer. Refinements:

- Shift period: 5s for fully-idle (WAITING), 20s for "anchored" states (GATHERING, BUILDING — clansman is at a station, only small drift).
- Wander radius: 12px gathering, 24px waiting/idle, 32px celebrating.
- Wander interpolation: don't teleport between targets. Lerp from current to next over the shift window with easeInOutSine. Sprite strolls between wander points.
- Direction during wander: derive from velocity (`dx`, `dy`). Hysteresis: hold last direction if `|dx| < 1` and `|dy| < 1`.
- Wander seed: hash(clansmanId, Math.floor(now / shiftPeriodMs)). Deterministic per-clansman per-window.

### 4.2 Travel between regions

Already specced as easeInOutSine along leg, one leg per tick. Refinements:

- Path shape: pure linear lerp between region anchors looks robotic. Add slight bezier curve.
  - Control point = midpoint + perpendicular offset of 8–16px.
  - Offset sign derived from hash(clansmanId, legIndex) so it's deterministic per-clansman per-leg.
  - Different clansmen on the same route take slightly different paths.
- Per-clansman jitter: when a group travels together, add per-clansman position jitter (±4px perpendicular to travel direction, deterministic from clansmanId) so they don't form a perfect conga line.
- Leg start/end smoothing: at leg boundaries (tick → tick) you get a 1-frame velocity discontinuity. Either blend the last 100ms with first 100ms of next leg, or accept it — at 60fps, 1 frame is invisible.
- Direction during travel: compute from nextRenderPos - currentRenderPos. Apply 4-way logic from §2.2.
- Arrival: at progress >= 1, see §3.3.

### 4.3 Multi-leg routes

Some missions visit multiple regions. The state derivation function:

```
const totalDuration = mission.arrivalTick - mission.startTick
const legCount = mission.path.length - 1
const ticksPerLeg = totalDuration / legCount
const elapsedTicks = currentTick - mission.startTick + subTickProgress
const legIndex = Math.floor(elapsedTicks / ticksPerLeg)
const legProgress = (elapsedTicks % ticksPerLeg) / ticksPerLeg

const eased = easeInOutSine(legProgress)
return lerpBezier(
  mission.path[legIndex],
  mission.path[legIndex + 1],
  eased,
  perpendicularOffset(clansmanId, legIndex)
)
```

### 4.4 Returning home (carry mode)

Same logic as §4.2 but uses carry_walk_* instead of walk_*. Wheelbarrow indicator visible above sprite (see §6.4).

-----

## 5. Environment catalog

Every region should have ≥3 ambient motion sources, even when no clansmen are present. The world breathes regardless of player activity.

|Element         |Where                   |Frames|FPS|Loop|Notes                      |
|----------------|------------------------|------|---|----|---------------------------|
|Chimney smoke   |Forge, kitchen, monument|4     |4  |yes |Phase-offset, alpha 0.7    |
|Wheat sway      |West/East Farms tiles   |4     |4  |yes |Phase per tile             |
|Tree leaves     |Forest tiles            |4     |3  |yes |Phase per tree             |
|Tree shake      |Forest, when chopped    |4     |8  |no  |Triggered by gather event  |
|Wave            |Docks, Deep Sea         |4     |3  |yes |Phase per tile             |
|Boat bob        |Docks                   |3     |2  |yes |Vertical 1px               |
|Windmill        |West Farms              |4     |6  |yes |Rotation cycle             |
|Forge glow      |Mountains, monument     |4     |4  |yes |Additive blend             |
|Campfire        |Bandit camp, Forest     |4     |6  |yes |Additive flame             |
|Banner flutter  |Bases, monument         |4     |4  |yes |Phase per banner           |
|Unicorn idle    |Unicorn Town            |4     |4  |yes |Tail flick + ear twitch    |
|Sparkles        |Unicorn Town            |4     |6  |yes |Magic ambient              |
|Crane swing     |Workshop                |8     |4  |yes |Slow arc                   |
|Building breathe|All buildings           |—     |—  |—   |1px sin(t) y-offset, 0.25Hz|

### 5.1 Building breathe

Subtle and crucial. Every building has its rendered y position offset by:

```
sprite.y = baseY + Math.round(Math.sin((now + phaseOffset) / 4000) * 1)
```

One pixel of vertical breath at 0.25Hz. Invisible until you turn it off — then everything looks frozen. Apply to all buildings, banners, and large environment props.

### 5.2 Environmental triggers

Some env motion is event-triggered, not looped:

- Tree shake: when a clansman performs chop action on this tile, that tree plays tree_shake once.
- Splash: when fishing yields a catch, water tile plays a splash effect.
- Smoke burst: when a forge or kitchen processes (build action completes), an extra puff of smoke spawns.
- Wheat empty: when a wheat plot transitions to Regrowing, the tile sprite swaps from "full wheat" to "stubble."

These are rendering reactions to state changes, not driven by independent timers.

-----

## 6. Effects

### 6.1 Effect catalog

|Effect              |Trigger                        |Frames|FPS|Behavior                                            |
|--------------------|-------------------------------|------|---|----------------------------------------------------|
|`pixel_burst_gold`  |Trade complete, deposit        |6     |12 |Radial out, fade last 2 frames                      |
|`pixel_burst_blue`  |Whisper sent                   |4     |12 |Subtle, only visible at zoom                        |
|`combat_flash`      |Each combat hit                |6     |16 |Brief white flash on target                         |
|`combat_dust`       |Combat ongoing                 |4     |8  |Loops while combat active                           |
|`coin_explode`      |Bandit defeated                |8     |12 |Scatter + fall + fade                               |
|`red_pulse`         |Bandit threat marker           |—     |—  |Sin alpha overlay, 1Hz                              |
|`selection_ring`    |Tap-select sprite              |—     |—  |Rotating dashes, persists                           |
|`level_up_burst`    |Building upgrade               |8     |10 |Rays + sparkle                                      |
|`monument_grow`     |Monument tier up               |12    |8  |Tower extends, dust at base                         |
|`screen_shake`      |Failed defense, big event      |—     |—  |Camera offset jitter, decays 400ms                  |
|`screen_flash_red`  |Defense failure                |—     |—  |Full-screen red, 100ms in / 200ms out               |
|`screen_flash_white`|Critical hit, combat resolution|—     |—  |80ms in / 100ms hold / 800ms out                    |
|`whisper_motes`     |Agent thinking                 |3     |4  |Ambient on Elder portrait in HUD                    |
|`dust_puff`         |Defender knockback landing     |4     |8  |One-shot, 500ms                                     |
|`blueprint_drift`   |Blueprint Fragment awarded     |—     |—  |Icon rises from bandit corpse, drifts to vault, 1.5s|

### 6.2 Effect timing principle

The more important the event, the longer the effect, but never longer than 800ms. If something demands more, escalate to a screen-level effect (shake, flash) layered over the in-world effect.

|Event           |In-world              |Screen-level                 |
|----------------|----------------------|-----------------------------|
|Whisper sent    |200ms blue mote       |—                            |
|Trade success   |400ms gold burst      |—                            |
|Building upgrade|800ms burst           |—                            |
|Bandit defeated |600ms coin explode    |200ms white flash            |
|Defense failed  |400ms wall drop + dust|300ms red flash + 400ms shake|
|Monument tier-up|1200ms grow + sparkles|1.6s camera zoom-in/hold/out |

### 6.3 Particle pool pattern

Don't `new PixiContainer` for every burst. Pre-allocate a pool of 32 effect containers, recycle.

```
type EffectInstance = {
  sprite: AnimatedSprite
  startedAt: number
  duration: number
  type: EffectKey
  active: boolean
}

const pool: EffectInstance[] = Array.from({ length: 32 }, makeEffectInstance)

function spawnEffect(key: EffectKey, x: number, y: number) {
  const slot = pool.find(e => !e.active)
  if (!slot) return // pool full, drop
  slot.active = true
  slot.startedAt = performance.now()
  slot.sprite.position.set(x, y)
  // configure sprite with effect frames, etc.
}
```

Render loop ticks each active effect. Slot returns to pool when now - startedAt > duration.

### 6.4 Wheelbarrow / carry indicator

Above each clansman currently carrying resources, render a small "fill bar" or "wheelbarrow" overlay:

```
const fillRatio = Math.max(
  carry.wood / WOOD_CAP,
  carry.iron / IRON_CAP,
  carry.wheat / WHEAT_CAP,
  carry.fish / FISH_CAP
)
```

Fill bar: 16px wide × 3px tall, anchored 4px above clansman head. Fill segment width = 16 * fillRatio, parchment cream color. Empty segment, ink color. Outline 1px.

Animate fill changes:

- Fill (gather): bar grows to new ratio over 400ms easeOutQuad.
- Empty (deposit): bar shrinks to 0 over 200ms easeInQuad.

-----

## 7. Speech bubbles

### 7.1 Specifications

- Cap: 240 chars on wire (per Convex whispers schema), ~80 chars visible (3 lines wrapped at 28 chars).
- Truncation: longer than visible cap shows "…" at end.
- Typography: pixel font, 8px or 10px. Wrap at ~28 chars per line. Max 3 visible lines.
- Color: parchment cream #E8D8B5 fill, ink #2A1810 text. Tail same as fill.
- Outline: 1px black, 1px inner highlight (parchment cream). Don't anti-alias.
- Anchor: (0.5, 1.0). Grows upward from above sprite head + 4px gap.
- Tail: pointing down, 1px shifted from anchor.

### 7.2 Lifecycle timing

- Fade in: 150ms. Alpha 0→1, scale 0.9→1.0, easeOutBack (slight overshoot pop).
- Hold: 6500ms solid (assuming standard whisper).
- Fade out: 1350ms. Alpha 1→0, no scale change.
- Total: 8000ms.

The fade-in pop is critical — draws the eye. The fade-out is slow because players need time to read.

### 7.3 Stack and replacement rule

If a clansman is showing a bubble and a new whisper arrives:

- Old bubble fast-fades (200ms).
- New bubble fade-in begins after old is gone.
- Never two bubbles per sprite simultaneously.

### 7.4 Anti-occlusion

Bubbles render in their own Pixi layer above all sprites, below HUD. If two bubbles' bounding boxes intersect, one shifts up by its own height. Re-evaluate on each render frame (cheap because there are at most ~8 bubbles at once).

### 7.5 Edge clamping

If a bubble would render off-screen-right, anchor flips to right side of sprite (bubble grows leftward). Same for left/top edges. Re-evaluate on camera changes.

### 7.6 Attribution

Per the locked decision: speech bubble is attached to the *first clansman in the missions array* for the speaking clan. This keeps bubble placement stable as missions resolve and reorder.

### 7.7 Emergent behavior — whisper vs gossip rendering

If we want to give the judge UI a cue that kind = whisper is private and kind = gossip is broadcasty, render slightly differently:

- whisper (1-to-1): standard parchment bubble.
- gossip (1-to-few): bubble outline gets a 2px gold trim, slightly larger.
- taunt, threat: red ink color.
- otc_offer, mercenary_*: small icon prefix in bubble (coin, sword).

These are display variations only. The wire payload is identical.

-----

## 8. Camera

### 8.1 Zoom

- Range: 0.5× – 4.0×.
- Wheel/pinch zoom: continuous, clamped, smoothed (lerp current toward target by 0.2 each frame).
- Tap-to-zoom: 400ms easeInOutQuad tween to {x, y, zoom: 2.0}.
- Double-tap to zoom out: 400ms tween to fit-world view.
- Boundary cross-fade: at zoom = 1.5×, cross-fade strategic atlas → detail atlas over 200ms. Both layers render during transition; alpha-blend.
- Zoom focal point: zoom focuses on cursor/touch position, not screen center. Critical for natural pinch.

### 8.2 Pan

- Drag: 1:1 pixel mapping during drag.
- Momentum: on release, continue pan with velocity decaying at 0.92 per frame. Stop at velocity < 0.5px/frame.
- Bounds: world has hard bounds. Past bounds, apply rubber-band resistance (drag distance × 0.4) and snap back over 300ms easeOutCubic on release.
- Inertia interrupt: tap during momentum stops momentum immediately.

### 8.3 Follow

- Follow mode: when player taps a clansman or selects a clan, camera enters follow mode.
- Follow lerp: camera position toward target with factor 0.08 per frame (smooth, not instant).
- Exit follow: any pan gesture exits follow.
- During travel: follow keeps a traveling clansman framed without jarring snaps.

### 8.4 Cinematic camera moves

For demo "wow" moments, the camera takes over briefly:

- Monument tier-up: 0.8s zoom-in to monument, hold 1s, 0.8s zoom-out. Player input ignored during. Fires at most once per monument level per game.
- Combat begin: subtle zoom-in 5% over the slow-circle phase. Imperceptible per-frame, but combat *feels* closer at peak.
- Transfer demo (Sub2): full screen-dim, then portrait disintegration animation. See §11.5.

-----

## 9. Combat choreography (the hero moment)

This is the visual high point of the game. Combat happens deterministically onchain at tick close, but the *visual* plays out across the full 60-second tick (Sub2) or 20-second tick (Sub1). Times below assume 60s tick; scale proportionally for Sub1.

The combat outcome is known onchain at tick start. The animation just plays out the dramatic version of what already happened.

### 9.1 Phase timing

```
T+0s   ── Combat tick begins. World filter dims.
T+1s   ── "Combat begin" visual cue.
T+2s   ── Bandits start march toward base.
T+3s   ── Defenders converge from base perimeter.
T+15s  ── All combatants arrive at combat ring radius.
T+16s  ── Slow circle begins. ~15° per second.
T+30s  ── Circle accelerates. ~45° per second.
T+45s  ── Whirlwind phase. ~180° per second, motion blur.
T+55s  ── Whirlwind peaks. Camera shakes subtly.
T+58s  ── FLASH (full screen, 200ms).
T+58.2s ── Resolution animation begins (success or failure).
T+59.5s ── Resolution completes.
T+60s  ── Tick closes, world filter releases, normal state.
```

Combat phase derived value:

```
function computeCombatPhase(now: number, tickStartMs: number, tickDurationMs: number): CombatPhase {
  const p = (now - tickStartMs) / tickDurationMs
  if (p < 0.025) return 'dim'
  if (p < 0.25)  return 'advance'
  if (p < 0.5)   return 'slow_circle'
  if (p < 0.75)  return 'accelerate'
  if (p < 0.92)  return 'whirlwind'
  if (p < 0.97)  return 'flash'
  return 'resolution'
}
```

### 9.2 Phase 1 — The dimming (T+0s to T+1s)

The moment that says "stop watching everything, watch *this*."

- Full-screen dark overlay fades in over 800ms to alpha 0.55. Tint = ink blue #1A1A3A.
- Combatants and the targeted base render in worldCombatHighlight container, *above* the dim overlay. Unaffected.
- All other sprites in worldDim container render *below* overlay. Dimmed.
- HUD shifts: "Combat at Base 03" indicator appears in top bar.

Implementation:

```
// Two stacked world containers
const worldDim = new Container()           // everything by default
const worldCombatHighlight = new Container() // combat sprites
const dimOverlay = new Graphics()           // rendered between them

stage.addChild(worldDim)
stage.addChild(dimOverlay)
stage.addChild(worldCombatHighlight)

// On combat begin: move combatant sprites to worldCombatHighlight
// On combat end: move them back, fade dimOverlay alpha 0.55 → 0 over 600ms
```

### 9.3 Phase 2 — Bandit advance (T+2s to T+15s)

- Bandits start at staging position (bandit camp tile or just outside base region).
- Each bandit's combat-ring target position: baseAngle + (i / N) * 2π. Spread evenly.
- March uses bandit_march (4 frames, 6fps).
- Pulse glow during march: tint = lerp(0xff8888, 0xffffff, sin(now/400)). Period 800ms, 1.25Hz.
- Bandits move slowly. Cover the distance over 13 seconds. Suspense, not action-paced.

### 9.4 Phase 3 — Defender convergence (T+3s, parallel with Phase 2)

- Defenders (clansmen on defend_base + idle clansmen at home + mercenaries from other clans) leave wander positions, walk toward combat ring on the *opposite side* from each bandit.
- If 5 defenders vs 3 bandits, defenders fill arc gaps between bandits.
- walk_* animation, 4-direction. Face ring center.
- Stagger: each defender starts 200ms after previous so they don't all kick off in sync.

### 9.5 Phase 4 — The wait (T+15s to T+16s)

Brief 1-second beat. Everyone in position, idle, facing base. World dim, combat circle bright. Nothing moves except 4-fps idle bobs and bandit pulse glow.

This pause is critical — the calm before.

### 9.6 Phase 5 — Slow circle (T+16s to T+30s)

- Bandits and defenders orbit the base. Alternate around the ring (B D B D B D…).
- Angular velocity: 15°/s clockwise. Quarter-rotation = 6 seconds.
- Position: (baseX + r·cos(angle), baseY + r·sin(angle)). Combat ring radius ~80px.
- Sprites face *tangent* to circle (direction of motion). 4-way walk reads correctly as they orbit.
- walk_* animation throughout.
- Camera *very gently* zooms in by 5% over this phase. Imperceptible per-frame.

### 9.7 Phase 6 — Whirlwind acceleration (T+30s to T+45s)

- Angular velocity ramps 15°/s → 180°/s over 15 seconds. Easing: cubic.
- At ~60°/s, sprite frame rate accelerates: walk frames advance at 12fps instead of 8fps.
- At ~120°/s, motion blur: pre-rendered radial smear sprites overlay each combatant. Or use Pixi's BlurFilter with blur = lerp(0, 4, accelProgress).
- Particles spin out tangentially: small dust/spark sprites emit from ring, 4-frame, fade fast.
- Subtle whirlwind shape (concentric semi-transparent rings) appears underneath, rotating in *opposite* direction. Sells the vortex.

### 9.8 Phase 7 — Whirlwind peak (T+45s to T+55s)

- Angular velocity holds at 180°/s.
- Blur maxes out. Combatants are unrecognizable streaks.
- Camera shakes: subtle, ±2px offset, 3Hz.
- Base in center dims and glitches (1px x-offset jitter, 4Hz).

### 9.9 Phase 8 — The flash (T+55s to T+58s)

- Whirlwind continues but adds an *outer* expanding white ring at T+55s — shockwave warning.
- At T+58s exactly: full-screen white flash. Alpha 0→1 in 80ms (2 frames at 60fps), held 100ms, fade to 0 over 800ms.
- During the flash, combatant positions snap to resolution starting positions. Flash hides the snap.

### 9.10 Phase 9a — Defense success (T+58.2s to T+59.5s)

- Bandits launch radially outward from base center. Initial velocity ~200px/s.
- Bandits decelerate over 600ms (drag), travel ~60px, come to rest.
- Each bandit plays bandit_death (6 frames, 8fps):
  - Frames 1–2: ground pose, dazed.
  - Frames 3–4: white flash overlay (full sprite alpha-tinted white).
  - Frames 5–6: shrink to 30% scale, fade alpha 1→0.
- After death anim (~750ms), coin_explode spawns at each bandit's last position.
- Defenders play cheer (4 frames, 6fps, 2 loops = ~1.3s).
- Blueprint Fragment (if awarded): icon rises from bandit corpse, drifts to base, fades into vault. ~1.5s.

### 9.11 Phase 9b — Defense failure (T+58.2s to T+59.5s)

- Clansmen jump backward 20–30px (much less than bandits would be thrown). easeOutQuad over 300ms.
- On landing, each clansman's tile gets dust_puff (4 frames, 8fps, 500ms).
- Stagger: clansmen play idle with 1px horizontal jitter for 500ms (shaking it off).
- Wall sprite drops one level *visibly*: scale.y interpolates 1.0 → newRatio over 400ms easeInQuad. Small chunk-of-stone sprite falls from wall top, hits ground, dust.
- Resources fly out of base as small icon sprites (one per 20% stolen), drift to bandit positions over 800ms. Each bandit "absorbs" them (sprite shrinks to bandit, fades).
- Bandits converge to base center: brief huddle, sprites overlap at center 500ms.
- Bandits then march out toward next region. bandit_march. Camera doesn't follow.

### 9.12 Phase 10 — Cleanup (T+59.5s to T+60s)

- World dim overlay fades to 0 over 600ms.
- Surviving combatants return to wander positions (smooth walk).
- HUD combat indicator clears.
- Whisper feed populates with reactions (defenders, owners, gossiping clans).

### 9.13 State caching during combat

The combat outcome is in contract state at tick start. The animation needs "pre-combat" state through phases 1–8 and "post-combat" state in phase 9.

```
// At combat begin (phase = dim entered):
combatPreState = snapshot(currentClanState)

// Through phases 1–8: render from combatPreState
// At phase 9 entry: swap to live state

const renderState = (combatPhase < 'resolution') ? combatPreState : liveState
```

Pure rendering concern, doesn't touch contract logic.

### 9.14 No-combat tick handling

If a tick has no combat, combatPhase = 'none' and all the above is skipped. World renders normally.

-----

## 10. Day/night cycle

### 10.1 Implementation — single ColorMatrixFilter

PixiJS gives the right tool: ColorMatrixFilter applied to the world container (NOT HUD). GPU-accelerated, effectively free.

```
const dayNightFilter = new PIXI.ColorMatrixFilter()
worldContainer.filters = [dayNightFilter]
```

### 10.2 Keyframes

```
const DAYNIGHT_KEYFRAMES = {
  dawn:  { r: 1.10, g: 0.90, b: 0.80, brightness: 0.95, sat: 0.85 }, // warm pink
  day:   { r: 1.00, g: 1.00, b: 1.00, brightness: 1.00, sat: 1.00 }, // neutral
  dusk:  { r: 1.15, g: 0.85, b: 0.70, brightness: 0.85, sat: 0.95 }, // amber
  night: { r: 0.65, g: 0.70, b: 0.95, brightness: 0.55, sat: 0.70 }, // blue cool
}
```

### 10.3 Cycle timing

Tied to ticks for narrative coherence and free determinism:

```
const TICKS_PER_DAY_CYCLE = 30  // 30 ticks = 30 minutes on Sub2, 10 minutes on Sub1

function getDayNightProgress(currentTick: number, subTickProgress: number): number {
  return ((currentTick + subTickProgress) % TICKS_PER_DAY_CYCLE) / TICKS_PER_DAY_CYCLE
}
```

30 ticks per cycle = 30 minutes per "day" on Sub2, 10 minutes on Sub1. One season = 12 day cycles. Players see roughly 4–6 day cycles per typical play session.

### 10.4 Phase distribution

Equal day/night with quick transitions:

|Phase|% of cycle|Duration (Sub2)|
|-----|----------|---------------|
|Dawn |0–5%      |1.5 min        |
|Day  |5–50%     |13.5 min       |
|Dusk |50–55%    |1.5 min        |
|Night|55–100%   |13.5 min       |

### 10.5 Apply function

```
function applyDayNight(progress01: number) {
  const phase = progress01 * 4
  const idx = Math.floor(phase) % 4
  const t = phase - Math.floor(phase)
  const keys = ['dawn', 'day', 'dusk', 'night']
  const a = DAYNIGHT_KEYFRAMES[keys[idx]]
  const b = DAYNIGHT_KEYFRAMES[keys[(idx + 1) % 4]]

  const r = lerp(a.r, b.r, t) * lerp(a.brightness, b.brightness, t)
  const g = lerp(a.g, b.g, t) * lerp(a.brightness, b.brightness, t)
  const bl = lerp(a.b, b.b, t) * lerp(a.brightness, b.brightness, t)

  dayNightFilter.matrix = [
    r,  0,  0,  0,  0,
    0,  g,  0,  0,  0,
    0,  0,  bl, 0,  0,
    0,  0,  0,  1,  0,
  ]
}
```

### 10.6 Window glow (highest-ROI embellishment)

Each base has a "window glow" overlay sprite. Alpha = clamp(1 - daylight, 0, 1). As night falls, base windows light up warm yellow.

This single effect sells immersion at almost no extra art cost. Buildings look lived-in at night, which is incredibly cozy for top-down pixel art.

```
// daylight = brightness from current keyframe blend (0–1)
windowGlowSprite.alpha = clamp(1 - daylight, 0, 1)
```

### 10.7 Optional embellishments

If time allows:

- Star twinkle layer: night phase only. Starfield container above world, below HUD. Stars = small white pixel sprites with sin alpha twinkle, phase-offset. Show when night progress > 0.5.
- Smoke tint: chimney smoke alpha peaks at dawn/dusk against colored sky.
- Bandit night glow: bandits get a slight red glow during night phase. Cosmetic, makes threat feel worse.
- Cycle indicator in HUD: tiny sun/moon icon rotating around an arc. Players grok "we're at dusk."

### 10.8 Combat × day/night composition

Both filters operate on world container. Compose in order:

```
worldContainer.filters = [dayNightFilter, combatDimFilter]
```

A combat at dusk looks amber-and-dark; a combat at night is apocalyptically dark. Both atmospheric, no special-case code.

Cap rule: when combat starts during night, cap combat dim at min(0.55, 1 - currentBrightness) so combat is always at least readable.

-----

## 11. Demo "wow" moments

These are the beats the audience must feel. Choreograph them.

### 11.1 Bandit threat sequence (~20s arc, before Phase 1 of combat)

Plays during the *previous* tick when bandit camping completes and target is selected:

1. Bandit appears at edge of map: spawn poof + scary visual sting.
2. War banner unfurls over bandit (1s ease-out).
3. Skull icon appears over targeted base. red_pulse overlay begins.
4. HUD countdown: "Bandit attack in N ticks."
5. Bandit walks toward target with march.
6. Whisper feed: defenders' clans react ("OH GOD GO HELP" type messages).

Then the main combat choreography (§9) takes over.

### 11.2 Trade swirl over Unicorn Town

- Unicorn ambient sparkles intensify when a trade tx lands.
- Spiraling pixel-coin sprite orbits Unicorn Town for 1.5s.
- Both clans' carry indicators update mid-swirl.
- Subtle gold glow on Unicorn Town for 2s after.

### 11.3 Building upgrade

- 800ms level_up_burst around building.
- Old sprite cross-fades to new (150ms).
- Dust puff at base.
- Resource counter ticks down with "-50 wood" floater.
- New sprite scales 1.0 → 1.05 → 1.0 bounce.

### 11.4 Monument tier-up

- 1200ms monument_grow: tower extends upward, dust at base, sparkles cascade down.
- Camera *briefly* zooms to monument: 0.8s tween in, hold 1s, tween out 0.8s. Player input ignored during.
- Whisper feed shows ALL elders reacting.
- Fires at most once per monument level per game.

### 11.5 Submission 2 Transfer Demo (the Track 2 killer)

This is the dramatic moment for Submission 2. Choreograph cinematically:

1. Owner Editor UI shows current Elder portrait.
2. "Transfer iNFT" button pressed.
3. Screen dims. Elder portrait disintegrates into pixel motes that drift offscreen.
4. New owner address resolves: "Transferring to 0x…"
5. Pixel motes reform into the same Elder portrait under new owner.
6. Whisper appears: *"I remember the bandit at tick 412. The grudge holds."*
7. Cut to in-world: that Elder's clan continues operating with same memory, same vendetta.

This sells "intelligence transferred with the asset" better than any tech explanation.

### 11.6 Season finalization

When the 360-tick season ends:

- Camera pans across all bases, monument-tier-up style cinematography.
- Final leaderboard reveals position by position with delay (300ms between each), with each clan's heraldic shield enlarging briefly.
- 1st place gets a gold crown overlay, sparkles cascade.
- Prize pot distribution animates: gold flows from treasury to top-4 clan banks.

-----

## 12. HUD chrome animations

Less hot than world rendering but still critical for premium feel.

### 12.1 Counter ticks

When a resource count changes:

- Number rolls. 200ms tween from old → new with easeOutQuad.
- Duration scales with delta: min(400ms, 100ms + log(delta) * 40ms).
- Floater "+/-N" appears, drifts up, fades out over 800ms. Green +, red -.

### 12.2 Leaderboard reorder

When a clan rank changes:

- Row slides to new position over 600ms easeInOutCubic.
- Promoted clan: subtle gold flash on row.
- Demoted clan: brief dim (no flash).

### 12.3 Whisper feed scroll

- New whispers slide in from top, 300ms easeOutCubic.
- Old whispers scroll down.
- Auto-scroll if user is at top of feed; hold position if user has scrolled away.

### 12.4 Tab transition

200ms cross-fade between tab contents. Don't slide — tabs aren't laterally adjacent in the player's mental model.

### 12.5 Resource bar fill

Per-clan vault bars in HUD:

- Fill: smooth easeOutQuad over 400ms.
- Drain: easeInQuad over 200ms.

### 12.6 Tick countdown

- Small pixel timer in HUD: seconds until next tick.
- Updates every 100ms (not every frame).
- Last 3 seconds: pulse red, scale 1.0 → 1.1 → 1.0 each second.

### 12.7 Elder portrait

Each clan's Elder agent has a portrait in the HUD strip. Animations:

- Idle: gentle 1px y-bob, 0.25Hz.
- Thinking (whisper outbound or LLM call active): whisper_motes particles drift around portrait.
- Speaking (whisper just sent): subtle gold flash 200ms.
- Distressed (clan starving, base under attack): red tint pulse, 1Hz.
- Dead (clan eliminated): portrait greyscales, fades to 50% alpha.

-----

## 13. Tap / gesture feedback

### 13.1 Selection

- Tap on sprite: pulse a selection_ring underneath. Rotating dashes, 1Hz rotation, 0.5Hz pulse.
- Selection ring color: clan heraldic color (one of 8). Reads as "this is yours" / "this is who you tapped."
- Tap on building: white outline pulse for 200ms, info card slides in from bottom.
- Tap on empty terrain: nothing. Don't deselect; require explicit deselect action.

### 13.2 Long-press (mobile)

- 400ms long-press: shows quick info tooltip. Sprite gets subtle 1.05× scale and 100% brightness boost.
- Release: scale and brightness return over 150ms.

### 13.3 Bandit threat highlight

When bandit is targeting a base:

- Targeted base shows red_pulse overlay.
- Tapping it strongly highlights: white flash, then pulse intensifies.
- Player must feel the threat.

-----

## 14. Z-order / layering

```
Layer 0:  Terrain background (single sprite per region, near-static)
Layer 1:  Terrain accents (rocks, paths, decoration that's always behind everything dynamic)
Layer 2:  worldDynamic — single sortable container holding ALL Y-sortable entities:
            - Environment motion (wheat, waves, trees)
            - Buildings (with their overlays as children)
            - Banners (children of their host building)
            - Bandits, clansmen (with carry indicators as children)
            sortableChildren = true on this container only.
            Each entity's zIndex = entity.y (rounded).
Layer 3:  In-world effects (bursts, flashes, dust puffs)
Layer 4:  Selection rings
Layer 5:  Speech bubbles
Layer 6:  Threat overlays (red pulse, skull markers)
Layer 7:  Combat dim overlay (when in combat)
Layer 8:  Combat highlight container (combatants reparent here during combat)
Layer 9:  Screen-level effects (full-screen flash, shake offset)
[Outside Pixi]
HUD chrome (React, absolute-positioned over canvas)
Modals
```

### 14.1 Why a single sortable container

PixiJS `sortableChildren` only sorts children **within a single Container**. If buildings live in one container and clansmen live in a sibling container above it, the clansmen container always renders on top of the buildings container — no per-Y interleaving across the boundary. A clansman walking behind a building would still render on top of the roof. Splitting by entity *type* breaks the entire 2.5D occlusion premise.

The fix: every Y-sortable entity lives in `worldDynamic`. Environment motion, buildings, building overlays (as children of their host), clansmen, bandits, carry indicators (as children of their host) — all in one sortable container, all sharing the same `zIndex = y` global Y-sort. That's what enables clansman-walks-behind-windmill to actually look right.

Static-only background (Layers 0 and 1) and screen-space layers (Layers 3-9) stay in their own containers because they don't participate in dynamic Y-sorting. Combat highlight (Layer 8) is a dedicated container because the combat-dim trick requires its sprites rendering above the dim overlay — those are *temporarily reparented* during combat and reparented back to `worldDynamic` afterward.

### 14.2 Attachment patterns

For composite entities, prefer **child-of-host** over separate Y-sorted containers:

- **Building overlays (smoke, glow, banners, window-glow):** children of the building's PIXI.Container. Their world position derives from parent. They render in the parent's Y-slot automatically — no separate sort needed.
- **Carry indicators / wheelbarrows:** children of the clansman's container. Positioned at `(0, -spriteHeight - 4)` relative to clansman. Same Y-slot as parent.
- **Bandit pulse glow during march:** filter on the bandit sprite, not a sibling sprite. Stays z-correct by definition.

When a sprite genuinely needs to render *between* host-Y and the host above it (e.g., "smoke trails behind a passing clansman"), use `zIndex = parent.y + 0.5` on a sibling sprite in `worldDynamic`. Reserve fractional zIndex for these intentional overrides; integer zIndex = y for everything else.

### 14.3 Combat reparenting protocol

When `combatPhase` enters `dim`:

1. For each combatant entity (bandits, defending clansmen, the targeted base): record their current parent (`worldDynamic`) and their current `zIndex`.
2. Reparent them to `combatHighlight` container (Layer 8). `combatHighlight` does NOT use `sortableChildren` — combatants render in insertion order, which during combat is fine because they orbit the same point.
3. The dim overlay (Layer 7) renders between `worldDynamic` and `combatHighlight`, dimming the world but not the combatants.

When `combatPhase` returns to `none` (post-resolution):

1. Reparent each combatant back to `worldDynamic`, restoring their original `zIndex` (now updated to current Y).
2. Fade dim overlay to alpha 0.

This is the only place reparenting is allowed. All other entities stay in `worldDynamic` permanently.

-----

## 15. Performance and engineering

### 15.1 Budget

- 8 clans × 5 clansmen = 40 sprites.
- 16 buildings + 8 banners + ambient = ~80 active sprites in detail view.
- Pixi v8 batches by texture — single atlas = single draw call.
- Speech bubbles: bitmap text. Pre-render to texture if string repeats; else CPU hit per render.

### 15.2 Pixi v8 patterns

- Container (not `ParticleContainer`) for clansmen — they need flips, scales.
- ParticleContainer for same-effect bursts where transforms are limited.
- cullable: true on every world-layer container. Pixi v8 has built-in culling.
- boundsArea set on static containers to skip recompute.
- Atlas everything. No individual PNG sprites.
- SCALE_MODES.NEAREST globally. Set on Assets.load.
- renderer.resolution = window.devicePixelRatio capped at 2.
- One global ticker. Don't mount per-component tickers in React.
- maxFPS fallback: detect frame drops with 1s rolling avg; if avg fps < 45, set ticker.maxFPS = 30 and disable env motion.

### 15.3 Object pools

- 32 effect containers, recycled.
- 16 speech bubble containers (more than max simultaneous).
- 1 selection ring (hidden when not in use).

### 15.4 Asset loading

- Eager: both atlases on app start. ~500KB total. Show parchment-themed loading screen.
- Lazy: monument level art (10 frames), demo cinematic frames load on first need.
- Manifest: assets/manifest.json lists all atlases + JSONs. Assets.init({ manifest }) then Assets.loadBundle('strategic') and Assets.loadBundle('detail').

### 15.5 Plan B degradation order

As fps drops, in order:

1. ticker.maxFPS = 30.
2. Disable env motion (smoke, wheat) — biggest win.
3. Cap simultaneous speech bubbles at 3 (queue overflow).
4. Drop animation rates 6→4 fps.
5. Disable selection ring rotation (just static).
6. Disable day/night filter.
7. Last resort: skip strategic atlas, force detail at all zooms.

-----

## 16. React + Pixi integration

### 16.1 Single Pixi root

One `<canvas>` element mounted by one React component. Component's job:

1. Create Pixi Application on mount.
2. Pass to bridge.ts module.
3. Tear down on unmount.

React never imports from pixi/. Pixi never imports from React.

### 16.2 Bridge pattern

```
// bridge.ts — only point of contact
let app: PIXI.Application | null = null
let store: WorldStore | null = null
let unsubscribe: (() => void) | null = null

export const bridge = {
  attach(pixiApp: PIXI.Application, worldStore: WorldStore) {
    app = pixiApp
    store = worldStore
    unsubscribe = store.subscribe(state => updateScene(state))
  },
  detach() {
    unsubscribe?.()
    app?.destroy()
    app = null
    store = null
  },
  emit(event: PixiEvent) {
    // forward selection/tap events to store
    store?.handlePixiEvent(event)
  },
}
```

### 16.3 State subscription

- Zustand subscribeWithSelector. Pixi subscribes to specific slices.
- Diff-aware updates: when state changes, compute what *actually* changed (new clansmen, removed clansmen, status changes) and update only those Pixi nodes. Don't rebuild scene.
- Keep Map<entityId, PixiSprite> in bridge module.
  - For each entity in new state: if exists, update transform/state; else create.
  - For each entity in map but not in new state: destroy.

### 16.4 Events out of Pixi

Pixi handles tap/select/long-press via eventMode = 'static' + cursor = 'pointer':

```
sprite.eventMode = 'static'
sprite.cursor = 'pointer'
sprite.on('pointertap', () => bridge.emit({ type: 'select', entityId }))
```

Bridge forwards to store. React HUD re-renders from store change.

-----

## 17. Asset pipeline (Aseprite handoff)

- Export: PNG + JSON-Hash, 1× scale, trim enabled, 1px padding.
- Frame names: clansman_walk_ne_0, clansman_walk_ne_1, etc.
- Frame counts and fps live in hand-maintained animations.json separate from Aseprite export. Adding a frame doesn't require code change.
- pnpm validate-atlas checks every entry in animations.json resolves to actual frames in atlas. CI fails on mismatch.
- Vite watches assets/atlas/, hot-reloads in dev.

-----

## 18. The "premium feel" checklist

When playing the game, these should all be true. If any are false, the game feels cheap.

- [ ] Buildings breathe (1px sway, 0.25Hz, phase-offset).
- [ ] Wheat/waves have phase offsets — no marching band.
- [ ] Sprite frames advance at 4–8fps but pan is smooth at 60fps.
- [ ] Clansmen face direction of travel (4-way 45°).
- [ ] State transitions cross-fade or inherit, never snap.
- [ ] Speech bubbles pop in (overshoot ease) and fade out slowly.
- [ ] Counter changes roll, with delta floaters above.
- [ ] Tap-to-zoom is 400ms, not instant.
- [ ] Selection ring rotates and pulses.
- [ ] Bandit threats create dread (pulse, countdown, scary banner).
- [ ] Combat plays out cinematically across the full tick (advance → circle → whirlwind → flash → resolution).
- [ ] Defeating a bandit is satisfying (knockback + flash + coins + cheer).
- [ ] Trade beats happen (swirl, glow on Unicorn Town).
- [ ] Building upgrades are punctuated (burst, bounce, dust).
- [ ] Monument tier-up is cinematic (camera move, sparkles).
- [ ] Day/night cycle is visible without being jarring.
- [ ] Window glows turn on at night.
- [ ] Whispers attribute correctly (first clansman in mission).
- [ ] At 30fps fallback, env motion disables but core animation still feels alive.

-----

## 19. Open decisions / future work

Not blocking implementation but worth tracking:

- Star twinkle layer (§10.7): probably ship if time permits.
- Bandit night glow (§10.7): low risk cosmetic, ship if time.
- HUD cycle indicator (§10.7): ship for player legibility.
- Direction frame counts: currently 4 per direction. If author bandwidth allows, 6 frames per direction would smooth the walk significantly.
- Cinematic season-end camera (§11.6): polish item, ship last.
- Sound hooks: out of scope for hackathon, but each effect should have an optional soundId field in the effect catalog so adding audio later is one config map.

-----

## 20. Locked decisions (recap)

- Three independent clocks: game tick, animation frame, render frame.
- Position is a pure function of state + time, never stored.
- Two-resolution atlas system, cross-fade at zoom 1.5×.
- 4-way 45° walk: NE and SE authored, NW and SW mirrored.
- Idle always faces SE (or last walked direction on transition from walk).
- Sprite frames at 4–8 fps; never higher than 12 except during combat acceleration.
- Phase offsets on every repeating env element. No marching bands.
- Building breathe (1px y-offset, 0.25Hz) on every building.
- Cross-fade or inherit on state transitions; never snap.
- Direction flips are instant (no fade).
- Combat is the hero moment, choreographed across the full tick.
- Day/night via single ColorMatrixFilter, 30 ticks per cycle.
- Window glow at night is highest-ROI embellishment.
- Speech bubbles attributed to first clansman in mission array.
- Particle pool of 32, recycled.
- React/Pixi integration via single bridge.ts; no cross-imports.
