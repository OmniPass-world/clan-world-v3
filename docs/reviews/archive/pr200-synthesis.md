# Phase Super-Swarm Synthesis — PR #200 (head 0c20b46)

**Models run:** Codex 5.4 ⚠️ (review content unverified — file dominated by prompt+diff echo) | Codex 5.5 ✓ | Opus 4.7 ⚠️ (truncated; emitted only an amendment to a previous review)
**Phase:** dev-phase-7-otc — OTC Transfer Surface
**Diff size:** 4590 lines

## Summary

**Verdict: NEEDS_FIXES — 1 cross-model HIGH (Codex 5.5) + multiple MEDs from Opus 4.7's amendment.**

Codex 5.5 flagged a real architectural HIGH: **OTC proposals don't bind to the proposer's owner address or include an ownership/iNFT nonce.** When Phase 7 0G iNFT (S2 hero) ships transfer of clan ownership, an OTC proposal made under owner A can be accepted by the receiver after ownership transfers to owner B — draining B's clan based on A's pre-transfer authorization.

Opus 4.7 emitted only an "amendment" file (referencing "the rest of my review" not in the file). The amendment surfaces a key insight: **§7.1-7.5 spec doesn't actually exist** — Phase 7 was implemented against a 5-line checklist + the dead-stub v4.2 sync signatures. Propose/accept/cancel lifecycle, expiryTick semantics, replay flags, self-transfer/zero-amount handling are all implementer choices without written spec backing.

Codex 5.4 file is 372KB but appears to be prompt-echo-only (per memory `feedback_codex_54_review_output_check.md`). Treating as ✗ failed.

## MUST FIX

| File:line | Models | Severity | Finding |
|---|---|---|---|
| H1: `ClanWorld.sol` OTC `propose*Transfer` / `accept*Transfer` | 5.5 = 1/3 | HIGH | **Stale OTC proposals can drain assets after clan ownership transfer.** Proposal stores `from`, `to`, amounts, expiry — but NOT proposer address or ownership nonce. `accept*Transfer` validates only the current target owner. Clan ownership change after proposal creation = old proposal can debit new owner's clan. **Fix:** store `proposerAddress` AND `proposerOwnerNonce` at propose-time; reject accept if current owner doesn't match (or nonce changed). Affects gold/vault/blueprint/bundled transfers. **Critical for Phase 7 0G iNFT hero phase** (which is where ownership transfer is the demo punchline). |

## SHOULD FIX (from Opus 4.7 amendment)

| Severity | Finding |
|---|---|
| M1 | `expiryTick` should be `uint64` (not `uint256`/`uint32`) for chain-tick consistency |
| M2 | Canonical v4.2 direct transfer functions (`transferGold`, `transferVaultResource`, etc.) still exist as stubs that revert — delete dead code |
| M5 | Self-transfer (clan A → clan A) + zero-amount transfers should revert at propose-time, not silently succeed |
| M9 | **No canonical §7.1-7.5 spec exists.** Write a §7 spec patch (or ADR) documenting propose/accept/cancel lifecycle, expiryTick inclusivity, replay flags. Otherwise spec stays out of sync with shipped code. |

## DEFER / Document

(unable to extract more from truncated reviews)

## Per-model verdicts

- **Codex 5.4:** ✗ failed to emit review — file is prompt-echo only. Re-dispatch if Liam wants completion.
- **Codex 5.5:** REQUEST CHANGES — 1 HIGH (clan-ownership stale proposals) + secondary concerns (canonical transfer funcs stub-revert + ABI/event changes affecting indexers).
- **Opus 4.7:** Partial — amendment only. References M1, M2, M5, M9 from a fuller review not captured in stdout.

## Decision

Per Liam's pattern (Path A/B/C on Phase 6 super-swarm), this synthesis should ask Liam:

**Path A (fix-round):** add proposerAddress + ownership nonce to OTC proposals + 4 MEDs from Opus. ~30-60 min codex fix.

**Path B (defer):** file H1 + MEDs as Phase 7B follow-up issues. Mark Phase 7 ready for UAT now.

**Path C (re-run super-swarm with retry):** re-dispatch Codex 5.4 + Opus 4.7 to get cleaner review pass first. Then fix.

**Recommend Path B** — Phase 7 is small, the HIGH is theoretical (Phase 7 0G hero phase which adds iNFT transfer is FUTURE work, not yet shipped). MEDs are backlog material. Liam already directed "no more cycles" for Phase 9; same pattern likely fits here.
