# Maestro flows

Mobile UI smoke tests in YAML. Run any flow with:

    maestro test .maestro/<flow>.yaml

## Install Maestro

    curl -Ls "https://get.maestro.mobile.dev" | bash
    # adds ~/.maestro/bin to PATH

## Required device setup

1. Plug Seeker / emulator over USB with `adb` enabled.
2. Install the latest debug APK on the device.
3. (For wallet flows) install fakewallet:
   `adb install fakewallet.apk` — Solana Mobile's test wallet.

## Flows

- `wallet-connect-smoke.yaml` — full smoke: Connect → MWA handoff → Hearth → tab through Hall/Bazaar/Codex.
