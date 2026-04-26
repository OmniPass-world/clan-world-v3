# clan-world

Hackathon monorepo for the clan-world project.

## Setup

This is a [pnpm](https://pnpm.io/) + [Turborepo](https://turbo.build/repo) workspace.

```bash
# install pnpm if missing (Node 24+ recommended; .nvmrc pins the verified version)
corepack enable && corepack prepare pnpm@10.28.1 --activate

# install all workspace deps
pnpm install
```

### Scripts

All scripts run via Turborepo and fan out across workspaces:

| Script              | What it does                                                              |
| ------------------- | ------------------------------------------------------------------------- |
| `pnpm build`        | Build every package/app                                                   |
| `pnpm dev`          | Run every workspace's `dev` task (watchers)                               |
| `pnpm lint`         | Lint every workspace                                                      |
| `pnpm test`         | Run tests across workspaces                                               |
| `pnpm typecheck`    | TypeScript typecheck across workspaces                                    |
| `pnpm clean`        | Run each workspace's clean + nuke `node_modules` and `.turbo` at the root |

### Layout

```
apps/        # end-user apps (web, server, CLI, etc.) — drop new ones in here
packages/    # shared libraries (contracts, ui kits, sdks, etc.)
docs/        # ADRs and planning docs
```

### Runtimes

Both **pnpm/Node** and **bun** are available on this host. Each package decides which runtime it wants:

- Default is Node + pnpm — works for everything.
- Packages can opt into bun (faster install / test / scripts) by adding a `bun` engine field and using `bun run` in their own scripts.
- Workspace install is always done via `pnpm install` at the root so the lockfile stays canonical.
