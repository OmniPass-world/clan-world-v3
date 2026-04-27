# ClanWorld — Canonical Spec Reference

This document defines the authoritative read-order for the ClanWorld v4 specification corpus.
When two docs conflict, the higher-ranked doc wins.

---

## Precedence table (rank 1 = highest authority)

| Rank | Document | Location |
|------|----------|----------|
| 1 | `IClanWorld.sol` | `packages/contracts/src/IClanWorld.sol` |
| 2 | `clanworld_v4_2_state_schema_interface_spec.md` | `docs/planning/` |
| 3 | `clanworld_v4_3_schema_patch.md` | `docs/planning/` |
| 4 | `clanworld_v4_4_ui_indexer_getters.md` | `docs/planning/` |
| 5 | `clanworld_v1_implementation_profile.md` | `docs/planning/` |
| 6 | `clanworld_v4_spec.md` | `docs/planning/` |
| 7 | `clanworld_v4_1_addendum.md` | `docs/planning/` (§A7 patched — see below) |
| 8 | Sponsor integration specs | `docs/planning/V1/03`, `V1/04`, `V1/05 0G/` |
| 9 | Landing/lore copy | `apps/landing/` (non-authoritative) |

---

## Known conflicts and resolutions

### Market-buy: exact-input vs exact-output

**The conflict:** `clanworld_v4_1_addendum.md §A7` originally stated "All v1 market actions are Exact Input actions only" and described `market_buy` as spending an exact amount of gold. This contradicts `IClanWorld.sol`, `clanworld_v4_2_state_schema_interface_spec.md §8.3–8.4`, and `clanworld_v4_3_schema_patch.md`.

**The resolution (rank 2 and rank 1 win):**
- `market_sell` — exact input: `marketAmount` = exact resource amount to sell; gold out is AMM-determined.
- `market_buy` — exact output: `marketAmount` = exact resource amount to receive; `maxGoldIn` = maximum gold willing to spend (slippage guard). Buy fails if required gold exceeds `maxGoldIn` at execution time.

§A7 in `clanworld_v4_1_addendum.md` has been patched (2026-04-26) to reflect this. See §A7 directly for the corrected text.

### Landing/lore copy

`apps/landing/src/pages/LorePage.tsx` contains gameplay constants (gather durations, cooldown descriptions) written for narrative clarity. These are not authoritative. Where they diverge from rank 1–7 docs, rank 1–7 wins.

---

## How to use this document

1. When implementing a mechanic, find the highest-ranked doc that addresses it.
2. If a lower-ranked doc seems to say something different, check whether the higher-ranked doc explicitly overrides it. If so, follow the higher-ranked doc and note the conflict here.
3. Conflicts not listed above should be escalated to the spec owner before implementation proceeds.
4. `IClanWorld.sol` (rank 1) is the tiebreaker for anything touching the contract interface. If the interface is unambiguous, follow it.
