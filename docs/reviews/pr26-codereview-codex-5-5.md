# Vault PR Super-Swarm Review — PR #26 (head ab099c8)

## SUMMARY
NEEDS_FIXES. The demo fallback behavior mostly works, but the live vault feed is incomplete: blueprint transfers and market executions are missing, so real spends/gains disappear. Also fix the public seed mutation risk and add focused projection tests.

## HIGH severity findings
`apps/server/convex/vault.ts:134` / `packages/contracts/src/IClanWorld.sol:715` — `BlueprintTransferred` is not handled. Direct OTC blueprint transfers mutate clan blueprint balances like `GoldTransferred`, but sender/recipient never see spend/gain in the movement log. Add it to the cross-clan projector using `"blueprint"`.

`apps/server/convex/vault.ts:77` / `packages/contracts/src/IClanWorld.sol:611` / `packages/contracts/src/IClanWorld.sol:621` — market execution events are omitted. Buys spend gold and gain a vault resource; sells spend a vault resource and gain gold. Project `ImmediateMarketActionExecuted` and `ScheduledMarketActionExecuted`, treating resource id `4` as gold.

## MEDIUM severity findings
`apps/server/convex/vault.ts:39` / `packages/contracts/src/IClanWorld.sol:140` — `resourceName()` hardcodes enum order locally. It matches current `ResourceType` today, but conflicts with the indexer’s market-state order convention. Generate/share this mapping to avoid silent drift.

`apps/server/convex/vault.ts:189` — broadcast scan uses latest N `chainEvents`, not latest N relevant vault events. Unrelated events can push recent transfers/loot outside the 80-row default. Use `by_event_block` per event, sender/recipient indexes, or a denormalized movement table.

`apps/server/convex/vault.ts:231` — `seedVaultMovement` is public and writes arbitrary `eventName`/`args` into canonical `chainEvents`. That can poison prod feed data. Make it internal, gate by dev/mock env, or seed a separate mock table.

`apps/server/convex/vault.ts:212` — de-dupe key can collapse distinct same-tick identical events. Include `txHash|logIndex` or remove de-dupe after query shape is fixed.

## LOW severity findings
`apps/web/src/components/cockpit/tabs/VaultTab.tsx:80` — `m.type as 'gain' | 'spend'` masks server return mistakes. Prefer generated return typing or explicit narrowing. No `@ts-ignore` found.

`apps/web/src/components/cockpit/tabs/VaultTab.tsx:89` — tab-wide `data-source` becomes `"stub"` for mixed states. Fallback does work for `undefined` and `[]`, but separate resource/movement data-source attrs would test both halves more precisely.

## Cross-cutting observations
`by_clan_tick` is defined in `apps/server/convex/schema.ts:84`; the indexed attributed lookup is valid. Empty `chainEvents` is safe: query returns `[]`, UI falls back to stubs.

No focused tests cover `getVaultMovements` or the Vault tab live/stub branch. Existing cockpit E2E only checks shell labels, so it would miss missing blueprint/market projections, empty fallback, and `data-source` semantics. Add minimal projection tests for deposit/withdraw, gold/resource/blueprint transfers, market buy/sell, and empty result.
```
