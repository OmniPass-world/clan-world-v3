# Clan World — Mobile Shell

React Native (Expo) port of the Clan World mobile shell, scaffolded for the Solana hackathon and ultimately the Solana dApp Store. Targets Solana Seeker.

Visual design ported from `Clan World Shell.html` (Claude Design handoff bundle). Backend integrations are intentionally **not wired yet** — the goal of this scaffold is to confirm the UI runs on Seeker; integrations come next.

## Stack

- Expo SDK 52, React Native 0.76
- TypeScript (strict)
- `expo-linear-gradient`, `react-native-svg`
- `@expo-google-fonts/*` (Cinzel, Cormorant Garamond, EB Garamond, JetBrains Mono)
- `@solana-mobile/mobile-wallet-adapter-protocol-web3js` (installed; **not yet wired** — the MWA sheet is currently a UI mock)

## Run it

```bash
cd /Users/mikail/Desktop/clan-world-mobile
pnpm install
pnpm prebuild        # generate android/ ios/ from app.json
pnpm android         # build & install on a connected device / Seeker
```

Or for the JS-only dev cycle once the dev client is installed:

```bash
pnpm start
```

## What's wired vs stubbed

**Wired (UI only):**
- Splash → MWA approval (mock) → Hearth
- Hearth, My Hall, Bazaar, Treasury (root tabs)
- INFT detail (Dossier / Strategy / Memory / History)
- Strategy editor with sliders, presets, and MWA confirmation
- Steering console with optimistic chat
- Sigil Forge — 4-step ceremony with forging animation
- Cockpit (terminal mock), Bridge loader, Whispers, Codex
- Hire modal + chained MWA sheet

**Stubbed — wire when integrations begin:**
- Wallet connection (currently approves automatically via the mock sheet)
- INFT data (currently `src/data.ts` constants — replace with backend queries)
- Cockpit WebView (currently a static terminal-style mock)
- Buy GOLD / on-ramp flow

## Layout

```
App.tsx                       state-based router (push/pop stack + tab + modal layers)
src/
  data.ts                     mock data + types
  theme/                      colors, font families, typography helpers
  components/                 primitives (Diamond, Parchment, Stone, TopBar, TabBar, Slider, …)
  screens/                    one file per screen
```

## Design notes

- Browser-only effects skipped: SVG noise filters, CSS `mix-blend-mode`. Parchment is approximated with layered linear gradients on a tinted base.
- Letter-spacing is in pixels (RN limitation), not em — values were tuned by eye against the prototype.
- Animations: Forge "burn-edge / seal-stamp" is approximated with shadow-radius pulses; Bridge has a looping `Animated.Value` opacity pulse. The full keyframe choreography from the design can be revisited if needed.
- The dev-only **Tweaks panel** from the prototype is intentionally omitted.
