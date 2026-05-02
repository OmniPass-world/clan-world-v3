# packages/contracts — AGENTS.md

Solidity contracts for ClanWorld. Foundry-managed. The canonical chain seam is
`src/IClanWorld.sol`; every other workspace talks to chain through the
`IChainClient` adapter and ABIs from this package.

## Current Shape

- `src/IClanWorld.sol` is the public ABI seam.
- `src/ClanWorld.sol` is the current monolithic engine and is over EIP-170.
- `foundry.toml` pins Solidity `0.8.34`, optimizer on, `via_ir = true`.
- Deploy target is Base Sepolia for the current wave.

## Local Commands

```bash
forge build
forge build --sizes
forge test
forge script script/Deploy.s.sol --rpc-url $RPC_URL_PRIMARY --broadcast
```

## Contract Rules

- Use `rg`/`rg --files` first when searching.
- Do not change `IClanWorld.sol` or generated ABI files casually. ABI changes
  require explicit user approval and downstream adapter/test updates.
- Run `forge build --sizes` for any change that can affect deployed bytecode.
- Keep tests focused on happy paths and critical failures per repo hackathon
  rules; do not add exhaustive regression suites unless needed for safety.
- Never commit real secrets or funded keys. `.env*` files stay local unless they
  are templates.

## Diamond / Facet Rules

ClanWorld is moving toward an EIP-2535 Diamond because the monolith is far above
the 24,576 byte EIP-170 runtime limit. Follow these rules for any diamond work:

- Use one shared `AppStorage` accessed through `LibStorage.appStorage()`.
- Facets MUST NOT declare contract-level state variables. All persistent game
  state goes through `AppStorage`; facet-level state silently corrupts diamond
  storage under `delegatecall`.
- `AppStorage` and nested storage structs are append-only after deployment. Do
  not reorder fields or insert fields in the middle.
- Prefer more small, domain-focused facets over near-limit mega-facets.
- Target each facet under `16 KB` runtime. Treat `20 KB` as a split-before-merge
  warning line.
- Avoid chatty cross-facet orchestration. Most calls should enter one facet,
  mutate shared storage, and return.
- Any external selector that exists only for internal diamond orchestration must
  be guarded with `onlyDiamond` or a stricter equivalent.
- State-writing user entrypoints use the shared `nonReentrant` guard, unless an
  orchestrator exception is explicitly documented and tested.
- Selector coverage tests must prove every intended `IClanWorld` selector routes
  through the diamond.
- Storage layout snapshot checks are mandatory for `LibStorage` changes.

## Facet vs Library

- Use a **facet** for external ABI functions, large stateful domains, future
  growth areas, or logic that needs diamond routing.
- Use an **internal library** for small pure/shared helpers that are cheap to
  inline and should not be exposed as selectors.
- Use an **external library** only when a shared helper is too large to inline
  and does not naturally belong as a facet.

Recommended initial slices:

- Facets: `WorldClockFacet`, `SeasonFacet`, `ClanLifecycleFacet`,
  `OrdersFacet`, `SettlementFacet`, `UpkeepFacet`, `GatheringFacet`,
  `DepositWithdrawFacet`, `UpgradesFacet`, `MarketFacet`, `TreasuryFacet`,
  `DirectTransfersFacet`, `BanditStateFacet`, `BanditTargetingFacet`,
  `BanditCombatFacet`, `RawWorldViewsFacet`, `RawTreasuryViewsFacet`,
  `RawClanViewsFacet`, `RawBanditViewsFacet`, `DerivedViewsFacet`,
  `MarketViewsFacet`, `BanditViewsFacet`, `RegionViewsFacet`,
  `SnapshotViewsFacet`, `QuoteViewsFacet`.
- Libraries: `LibStorage`, `LibTravel`, `LibResourceAccounting`, `LibRules`.

High-risk facets for size creep: `SettlementFacet`, `UpgradesFacet`,
`BanditCombatFacet`, `DerivedViewsFacet`, and `OrdersFacet`.

## Integration Boundary

This package is one side of `IChainClient`. If ABI, event, or deploy address
behavior changes, update `packages/shared` adapters/generated ABI and any server,
runner, or frontend consumers in the same PR.

See `../../docs/guides/stream-contracts.md` and
`../../docs/architecture/diamond-pattern.md` for deeper workflow and design
context.
