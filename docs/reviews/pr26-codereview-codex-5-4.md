# Vault PR Super-Swarm Review — PR #26 (head ab099c8)

## SUMMARY
NEEDS_FIXES. The frontend fallback behavior is mostly aligned with the demo requirement, and the `by_clan_tick` index does exist, but the new vault projection has material event omissions and a public seed mutation that can forge production feed entries. I would not merge this before fixing the missing event coverage and locking down or removing the write-side seeding surface.

## HIGH severity findings

- [apps/server/convex/vault.ts:231](/home/claude/code/cwv2-worktrees/vault/apps/server/convex/vault.ts:231) exposes `seedVaultMovement` as a public mutation that inserts arbitrary rows into `chainEvents`, including arbitrary `eventName`, `args`, `tick`, and `clanId`. Unlike the comms seed mutations, this poisons the same table the app treats as decoded chain truth. Any caller with Convex access can forge vault history in prod. Suggested fix: make this `internalMutation`, gate it behind an explicit demo/dev env flag, or remove it and keep stubbing entirely client-side.

## MEDIUM severity findings

- [apps/server/convex/vault.ts:156](/home/claude/code/cwv2-worktrees/vault/apps/server/convex/vault.ts:156) omits `BlueprintTransferred`, even though the contract emits it and the Vault tab shows blueprint balances. Result: OTC/bundle blueprint moves never appear in the movement log for sender or recipient. Suggested fix: handle `BlueprintTransferred` in the broadcast path exactly like `GoldTransferred`, with `resource: "blueprint"` and spend/gain direction from `fromClanId` / `toClanId`.

- [apps/server/convex/vault.ts:171](/home/claude/code/cwv2-worktrees/vault/apps/server/convex/vault.ts:171) omits `ImmediateMarketActionExecuted` and `ScheduledMarketActionExecuted`, which are movement-relevant ABI events for vault gold/resource balances. Market buys/sells will update the snapshot grid but leave the movement feed silent, making the tab internally inconsistent. Suggested fix: project both events into one gold delta plus one resource delta using `resourceIn/resourceOut` and `amountIn/amountOut`, with gold resource id `4` per [packages/contracts/src/IClanWorld.sol:98](/home/claude/code/cwv2-worktrees/vault/packages/contracts/src/IClanWorld.sol:98).

- [apps/server/convex/vault.ts:187](/home/claude/code/cwv2-worktrees/vault/apps/server/convex/vault.ts:187) says it uses `by_event_block`, but the implementation does a global `chainEvents` recency scan with no event-name index. That is both a comment/code mismatch and a correctness risk: sender-side transfers can fall out of the window if enough unrelated events are inserted after them. Suggested fix: query each broadcast event type via `withIndex("by_event_block", ...)` and merge those results, or add dedicated secondary indexes for transfer-style lookups.

- [apps/web/src/components/cockpit/tabs/VaultTab.tsx:75](/home/claude/code/cwv2-worktrees/vault/apps/web/src/components/cockpit/tabs/VaultTab.tsx:75) has no test coverage for the demo-critical fallback contract. The existing cockpit E2E only checks the vault shell renders labels; it does not assert stub-vs-live behavior, empty-array fallback, or `data-source`. Given the “backend offline must demo cleanly” requirement, this needs at least one happy-path UI test. Suggested fix: add a Playwright or component test that forces `getVaultMovements` to `undefined`, `[]`, and non-empty data and asserts rendered rows plus `data-source`.

## LOW severity findings

- [apps/web/src/components/cockpit/tabs/VaultTab.tsx:80](/home/claude/code/cwv2-worktrees/vault/apps/web/src/components/cockpit/tabs/VaultTab.tsx:80) uses `as 'gain' | 'spend'` on server data. It is probably safe today because the Convex function returns that literal union, but the cast hides drift if the backend changes. Suggested fix: rely on the generated type directly.

- [apps/server/convex/vault.ts:199](/home/claude/code/cwv2-worktrees/vault/apps/server/convex/vault.ts:199) and [apps/server/convex/vault.ts:206](/home/claude/code/cwv2-worktrees/vault/apps/server/convex/vault.ts:206) cast `e.args` to `Record<string, unknown>`. This is consistent with the indexer’s `bigintSafe` shape, but it keeps the projection logic stringly typed. Suggested fix: define narrow event-arg helpers for the handled events if this file grows.

## Cross-cutting observations

The frontend fallback shape is good: `undefined` and `[]` both land on `STUB_MOVEMENTS`, and the root `data-source` goes `stub` unless both the snapshot clan and movement feed are live. That matches the demo goal.

The main backend issue is drift from the contract seam. `vault.ts` is re-encoding event/resource semantics locally instead of importing one canonical mapper, while `indexer.ts` already hardcodes overlapping conventions. This is exactly the kind of fast-moving hackathon surface where one shared contract-facing utility would pay for itself quickly.
```
