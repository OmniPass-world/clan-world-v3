# Bandit Animation Implementation Plan

**Status:** Ready for implementation
**Date:** 2026-05-10
**Scope:** Frontend animation impl for bandit attack cycle, replaces speculative §9 in `clanworld_v5_animation_spec.md`
**Branch:** stacked off `feat/bandit-3-tick-cycle` (post-PR148 fix-round commit `18123c9`)
**Implementer:** Claude general-purpose subagent + worktree isolation

---

## Why this supersedes v5 spec §9

Original §9 assumed combat animation could play across the full 60-second resolution tick because the chain "knew" the outcome at tick start. We now know that's wrong:

- Resource deposits/withdrawals settle end-of-tick, BEFORE bandit resolution
- A clansman returning gold to the vault on the resolution tick can shift which clan is the highest-resource target last-second
- Therefore frontend cannot pre-compute the winner; outcome is only available AFTER the heartbeat closes the resolution tick

**New approach:** lag the attack animation by one tick. Telegraph during the resolution tick, then animate full battle over the NEXT tick once outcome is known.

---

## State machine: chain → frontend mapping

### Chain timeline (3-tick cycle, post-PR #148)

```
T0          Camped (tickEnteredState=T0)            Camp visible
T1          Camped tick 1                            Camp + glow ramp
T2          Camped tick 2                            Camp + glow peak
T3          Camped tick 3 (resolution tick)          Camp morphs → 3 bandits standing
end T3      Heartbeat: Camped → Attacking → outcome  (chain transitions)
T4          (post-resolution tick)                   Full battle animation
```

### Frontend state derivation

For every `useQuery(api.getSnapshot.getSnapshot)` snapshot, frontend computes:

| Field | Source | Meaning |
|---|---|---|
| `bandit.region` | snapshot | Where to anchor sprite |
| `bandit.state` | snapshot (enum: None/Spawned/Camped/Attacking/Defeated) | Which animation phase |
| `bandit.stateEnteredTick` | snapshot | Tick when current state started |
| `bandit.nextActionTick` | snapshot | When attack resolves (= stateEnteredTick + 3 in Camped) |
| `currentTick` | snapshot.tick | Latest closed tick |
| `currentTickFloat` | derived: `tick + (now - tickEpoch.startedAt) / tickEpoch.durationMs` | Float position in current tick (0.0..1.0+) |

### Animation phase from currentTickFloat

```typescript
function computeBanditAnimPhase(bandit: Bandit, currentTickFloat: number): BanditAnimPhase {
  const ticksInState = currentTickFloat - bandit.stateEnteredTick;

  if (bandit.state === BanditState.Camped) {
    if (ticksInState < 2.83) return { phase: 'camp_idle', glowAlpha: ticksInState / 3 };
    // Last 10s of resolution tick (tick T3): morph camp → standing bandits
    return { phase: 'camp_telegraph', telegraphProgress: (ticksInState - 2.83) / 0.17 };
  }

  if (bandit.state === BanditState.Attacking || (justResolvedThisTick && knownOutcome)) {
    // Tick T4: full battle animation
    const battleT = ticksInState; // 0..1 over the tick
    return { phase: 'battle', battleT, outcome: knownOutcome };
  }

  // ... post-battle states (defeated already removed from snapshot, win path = camp in new region)
}
```

`justResolvedThisTick`: detect by comparing previous snapshot's bandit (stateEnteredTick=T0, nextActionTick=T0+3, currentTick=T3) against new snapshot (state changed OR bandit removed OR bandit in new region). The transition is implicit from snapshot diff.

`knownOutcome`: derived from snapshot diff:
- Old bandit existed, new bandit is null OR state==None → **DEFEATED** (bandits lost)
- Old bandit in region X, new bandit in region Y (Y != X) AND new state==Camped → **WON** (rampage move)
- Old bandit in region X, new bandit in region X with same stateEnteredTick → no resolution yet (should not happen if T4 is current)

---

## Animation timing (Liam-approved 2026-05-10)

### Tick T3 (resolution tick) — last 10 seconds only

- 50s: camp idle (red glow at peak)
- **last 10s**: camp sprite morphs into 3 standing bandit sprites, jiggling with anticipation

Camp doesn't move. Bandits appear in standing pose where camp was. Slight vertical jitter (1-2px sin) per bandit with phase offset so they don't move in sync.

### Tick T4 (post-resolution animation tick) — full 60s

#### Common opening (0–14.5s)
- **0–7s**: bandits walk from camp position toward target base (pixel-walk strip, 4-frame SE/SW depending on relative position)
- **7–14.5s**: bandits + clansmen circle the base, accelerating into whirlwind

#### Resolution flash (14.5–15.5s)
- **0.5s flash hold** (white screen flash) at peak speed
- **0.5s launch impulse** — losers thrown back ~50-80px from base center

#### Outcome A: bandits defeated (15.5–25.5s, 10s total)
- **15.5–17.5s** (2s): death animation. Each bandit independently:
  - Frame 1 (back-pose) hold ~0.5s, flicker 2x
  - Frame 2 (face-down) hold ~0.5s, flicker 2x
  - Frame 3 (tombstone) hold ~1s, flicker 2x
  - Settle on tombstone
- **17.5–25.5s** (8s): tombstones slowly fade alpha 1 → 0
- **25.5–60s**: region empty, no bandit visible. Background continues normally.

#### Outcome B: bandits win (15.5–60s, 44.5s total)
- **15.5–17s** (1.5s): clansmen thrown back. Bandits converge on base center. Base shake animation (3px x-jitter, 8 cycles).
- **17–18.5s** (1.5s): bandits cluster together adjacent to looted base. Pause "thinking" pose.
- **18.5–25.5s** (7s): bandits walk toward next camp region (or off the side of the map if 6 attempts reached → terminal escape)
- **25.5–60s** (34.5s): bandits show as glowing camp sprite in destination region, awaiting next attack. Camp glow ramps as in T0–T2 of the next cycle.

### Outcome C: no-target advance (NEW — depends on backend PR)

**Trigger:** snapshot at T4 shows `bandit.region != prev region` AND no battle happened (no clansmen movement, no flash). Detected by snapshot diff.

**Animation (T4 full 60s):**
- **0–7s**: bandits walk off the camp toward the edge of region in direction of next region (no base attack, no circling)
- **7–25s** (18s): bandits cross to next region (slow march)
- **25.5–60s**: bandits appear as glowing camp in new region

Simpler than win-path because there's no battle. Just a region traversal.

### Outcome D: terminal escape (no-target on 6th attempt)

**Trigger:** snapshot at T4 shows `bandit == null` AND previous snapshot showed `attackAttemptsMade == 5` (or just always trigger D if bandit became null without a battle context).

**Animation:** same as Outcome C but bandits walk off the side of the map and disappear. No camp re-appears.

---

## Sprite kit

| Asset | Path | Dimensions | Frames | FPS | Notes |
|---|---|---|---|---|---|
| Camp | `apps/web/public/sprites/bandit_camp.png` | 500×500 RGBA single | 1 | — | Liam-shipped 2026-05-10 |
| Walking NE | `apps/web/public/sprites/bandit_walking_ne.png` | TBD strip 4 frames | 4 | 8 | From `bandit-walking-up.png` Liam shipped |
| Walking NW | derived | — | 4 | 8 | Mirror NE horizontally at runtime via `sprite.scale.x = -1` |
| Walking SE | `apps/web/public/sprites/bandit_walking_se.png` | TBD strip 4 frames | 4 | 8 | From TOP ROW of `bandit-walking-down.png` Liam shipped (bottom row discarded) |
| Walking SW | derived | — | 4 | 8 | Mirror SE horizontally |
| Standing (telegraph) | derive from walking frame 1 | — | 1 | — | Use frame 1 of NE/SE walk + add 1-2px sin jitter |
| Death sequence | `apps/web/public/sprites/bandit_death.png` | 1080×360 strip 3 frames | 3 | — | Liam-shipped 2026-05-10. Custom 3-step playback (back → face → tombstone) |

**Asset prep tasks** (animation impl agent should do these in worktree before impl):

1. Slice `bandit-walking-up.png` (4 frames horizontal) → save as `bandit_walking_ne.png` if needed (or use directly if dimensions are clean)
2. Slice top row of `bandit-walking-down.png` → save as `bandit_walking_se.png`. Discard bottom row.
3. Verify `bandit_camp.png` already in repo (camp sprite is 500×500 RGBA single image, no slicing needed)
4. Verify `bandit_death.png` placement — it's the 3-pose strip Liam shared

For Pixi loading: use `Assets.load(path)` and `Spritesheet` API for the 4-frame walks. Camp + death are single sprites/frames.

---

## Render integration in WorldMap.tsx

### Reads (lines ~1019, 2018, 2710)

```typescript
const snapshot = useQuery(api.getSnapshot.getSnapshot);
const bandit = snapshot?.bandit;  // { id, region, state, stateEnteredTick, nextActionTick, ... } | null
const currentTickFloat = computeCurrentTickFloat(snapshot);  // existing helper
```

### Snapshot diff tracking (NEW)

Add a `prevBanditRef = useRef<Bandit | null>(null)` and update on each snapshot. Use diff to derive `lastResolutionOutcome`:

```typescript
useEffect(() => {
  const prev = prevBanditRef.current;
  const curr = snapshot?.bandit ?? null;

  if (prev && prev.state === BanditState.Camped && prev.nextActionTick <= snapshot.tick) {
    // Resolution tick has closed
    if (!curr) lastOutcomeRef.current = { type: 'defeated', tick: snapshot.tick };
    else if (curr.region !== prev.region) lastOutcomeRef.current = { type: 'won', from: prev.region, to: curr.region };
    else if (curr.region === prev.region && curr.stateEnteredTick > prev.stateEnteredTick) {
      // attempt counter incremented but bandit stayed: NO-OP for this state, retry was applied
      // (only relevant if we KEEP retry behavior, but if no-target-advance lands, this branch is dead)
    }
  }

  prevBanditRef.current = curr;
}, [snapshot]);
```

### Render dispatch

In the render frame loop (`drawnRef.current.banditSprite` etc), select sprite based on phase:

```typescript
const phase = computeBanditAnimPhase(bandit, lastOutcome, currentTickFloat);
switch (phase.kind) {
  case 'camp_idle':
    renderCampSprite(banditContainer, bandit.region, phase.glowAlpha);
    break;
  case 'camp_telegraph':
    renderStandingBandits(banditContainer, bandit.region, phase.jitter);
    break;
  case 'battle':
    renderBattlePhase(banditContainer, bandit, lastOutcome, phase.battleT);
    break;
  case 'no_battle_advance':
    renderRegionTraversal(banditContainer, fromRegion, toRegion, phase.traversalT);
    break;
  case 'terminal_escape':
    renderWalkOffMap(banditContainer, fromRegion, phase.escapeT);
    break;
}
```

### New container layer

Add `worldBanditCombat: Container` between `worldDynamic` and `combatHighlight` for bandit-specific overlays (knockback, death frames). Existing `combatHighlight` reparenting pattern (line ~2780) can be reused for the flash + bandit reparenting during the battle phase.

---

## Edge cases

### 1. App reload mid-battle-tick (T4 + 0.4)
Frontend reads snapshot, sees bandit either in new region (won) or null (defeated). Computes phase from `currentTickFloat - prevResolutionTick`. Skips opening phases, drops into mid-circle or post-flash depending on fraction.

If user reloads and `lastOutcomeRef` is empty (no diff history), do best-effort: if bandit is null, assume defeat outcome and play `death + fade`. If bandit moved regions, assume win + play `bandits-walking-to-new-camp` (skip circle/flash since we missed them).

### 2. App reload mid-resolution-tick (T3 + 0.6)
Snapshot still has `bandit.state == Camped`. Phase is `camp_telegraph` if `ticksInState > 2.83` else `camp_idle`. Sprite shows standing bandits in their telegraph pose. No outcome animation yet.

### 3. First spawn
Previous snapshot bandit was null. New snapshot has bandit in region X. Render: camp sprite fades in over 1-2s (alpha 0 → 1).

### 4. Multi-bandit (future)
Current contract has one active bandit at a time (`activeBanditId` singular). If contract later supports multiple concurrent bandits, animation logic needs to iterate over an array. Current impl assumes singleton.

### 5. Pause / world paused
If `world.worldPaused == true`, freeze animation state. Camp glow holds, telegraph holds, battle pauses. Resume from frozen position when unpaused.

### 6. Indexer lag
If heartbeat fires but `worldSnapshot` row not yet written, frontend sits on previous snapshot. Animation continues running on old data (e.g., still showing camp_idle past T3). When new snapshot lands, animation jumps to new phase (some visual snap is acceptable; degrades gracefully).

---

## Test plan

### Manual / playwright tests

1. **camp_idle ramp**: spawn bandit at T0, advance to T1.5, screenshot — confirm glow at ~50%
2. **camp_telegraph**: advance to T2.9, screenshot — confirm 3 standing bandits visible (not camp sprite)
3. **defeat outcome**: trigger defeat, advance through T4, screenshot at T4+0.5 (mid-circle), T4+15s (mid-death), T4+25s (post-fade) — confirm visual sequence
4. **win outcome**: trigger win (with rampage to next region), screenshot at T4+0.5, T4+18s (cluster), T4+25.5 (camp in new region)
5. **no-target advance**: trigger empty region, screenshot at T4+0 (walking off camp), T4+25.5 (camp in new region)
6. **terminal escape**: trigger 6th no-target, screenshot at T4+0 + T4+25 — bandit walks off map, no new camp

### Unit tests (frontend)

- `computeBanditAnimPhase()` pure function:
  - state=Camped, ticksInState=1.5 → `camp_idle, glowAlpha=0.5`
  - state=Camped, ticksInState=2.9 → `camp_telegraph, telegraphProgress=0.41`
  - state=Camped, ticksInState=3.5 → `camp_telegraph` clamped (until snapshot updates with resolution)
  - lastOutcome.defeated, ticksInState=0.6 → `battle, battleT=0.6` with outcome=defeated
  - lastOutcome.won, ticksInState=0.4 → `battle, battleT=0.4` with outcome=won

- snapshot-diff outcome derivation:
  - prev camped region X tick 100, curr null → outcome=defeated
  - prev camped region X tick 100, curr camped region Y tick 101 → outcome=won

---

## Out of scope

- New sprite art (await Liam if more sprites needed)
- Sound effects (separate workstream)
- Multi-bandit concurrent (single active bandit assumed)
- Animation parameters tuning (timings here are first-pass; tune in dev)
- The CONTRACT change for no-target-advance (backend PR plan separate at `/tmp/cwg-bandit-noretry-plan/PLAN.md` — animation handles BOTH old retry-in-place AND new advance-to-next-region behaviors via the snapshot-diff pattern, so we don't need to gate on backend ordering)

---

## Dispatch instructions for impl agent

```
Branch: feat/bandit-animation-impl
Base: feat/bandit-3-tick-cycle (HEAD 18123c9)
Worktree: ~/code/clan-world/worktrees/bandit-animation
Port: use port-for-usage skill to allocate dev port
Verify: pnpm --filter @clan-world/web typecheck + visual smoke test in browser
PR title: feat(web): bandit attack animation — telegraph + battle + outcome variants
PR body: cite this plan + Liam's animation timing approval msg 12339
```

Worktree isolation per memory `feedback_always_use_worktree_isolation`. Cleanup worktree after PR opens per memory `worktree-feature` skill.
