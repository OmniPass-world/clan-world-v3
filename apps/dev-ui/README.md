# @clan-world/dev-ui

Generic read/write SPA over the ClanWorld diamond. Connects a wallet (Base Sepolia by default), enumerates every function across `IClanWorld` + `IDiamondLoupe` + `IDiamondCut` + `OwnershipFacet`, and lets you call any of them. Useful for diamond admin, debugging cuts, and one-off contract pokes during development.

## Migrated from standalone repo on 2026-05-12

This app was originally `github.com:clan-world/dev-ui` (a standalone Vite + React SPA). It was absorbed into the monorepo at `apps/dev-ui/` on 2026-05-12 via `git subtree add`, preserving full git history. See PR-of-record on the absorb commit. The standalone repo will be archived/deleted by the repo owner once this absorption is verified.

### Vercel project

The standalone repo was wired to a Vercel project:

- Project ID: `prj_AVXVwTKNJ6sFJ7VFov4ZXzk1ZFjE`
- Org ID: `team_YZdNnTlbEqyXPmeEe0JmPZ93`
- Project name: `dev-ui`

After this PR merges, that Vercel project must be relinked to point at `apps/dev-ui/` inside this monorepo (or a new project created). The standalone repo's git integration should be disabled before its archival.

### ABI source of truth

ABIs are no longer vendored. They are imported as JSON modules from the workspace's canonical contracts package:

```ts
import IClanWorldArtifact from '@clan-world/contracts/abi/IClanWorld.json' with { type: 'json' };
```

The four ABIs consumed (`IClanWorld`, `IDiamondLoupe`, `IDiamondCut`, `OwnershipFacet`) live at `packages/contracts/abi/*.json` and are regenerated from the forge artifacts under `packages/contracts/out/` by `pnpm codegen`.

If you need to add another facet ABI, add the artifact path to `scripts/gen-contract-abi.mjs` and export it from `packages/contracts/package.json`.

## Local dev

From repo root:

```bash
pnpm install
pnpm --filter @clan-world/dev-ui dev      # vite dev server
pnpm --filter @clan-world/dev-ui build    # tsc + vite build
```

## Configuration

| Env var | Default | Purpose |
|---|---|---|
| `VITE_DEFAULT_DIAMOND` | `0x14392f8276c6234064395e74f0741e26f1613c1e` | Pre-populates the diamond address input on load |
| `VITE_WALLETCONNECT_PROJECT_ID` | _(unset — WC disabled)_ | WalletConnect Cloud project ID. When unset, the WalletConnect connector is skipped and only injected wallets (MetaMask/Rabby/etc.) are offered. Get one at <https://cloud.walletconnect.com>. WC project IDs aren't secrets, but use your own per-deployment to keep quota attribution clean. |

Both vars are also listed in the root `.env.template` (`# --- dev-ui ---` section).

## Tech stack

- Vite 8 + React 19 (note: newer than `apps/web` which is on React 18 / Vite 5 — these are pinned per-package by pnpm and do not conflict)
- wagmi 3 + viem 2 for wallet + RPC
- @tanstack/react-query for caching
