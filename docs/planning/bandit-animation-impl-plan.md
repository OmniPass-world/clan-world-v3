# Bandit Animation Implementation Plan

## Edge cases

- Cold-start during T4 resolution: known limitation for this round. If the page reloads after a bandit attack resolves but before the 25.5s outcome animation finishes, `prevBanditRef` starts empty and the frontend cannot reconstruct `lastBanditOutcomeRef` from the current snapshot alone. The visible result may skip the battle/death/walk-out animation and snap to the latest snapshot state.
- Future fix: persist a compact backend resolution record, for example `lastResolution: { tick, kind, fromRegion, toRegion, targetClanId }` on the world snapshot. Once Convex exposes that field, the frontend can seed `lastBanditOutcomeRef` on first render and replay the in-flight T4 animation after reload.
