Spec agent confirms the canonical §7.1-7.5 detailed spec doesn't exist — only an implementation checklist (`clanworld_numbered_implementation_plan.md` lines 311-333), the v4.3 §M.1 dead-clan rule, and v4.2 function signatures. The propose/accept/cancel/expiryTick lifecycle was a designer judgment call by the implementer, not a spec compliance question.

**One amendment to my review:**

### Add to MEDIUM findings:

### M9 — No canonical spec for the propose/accept/cancel lifecycle exists
**Files:** `docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:311-333`, missing detailed §7 spec

Phase 7 was implemented against a 5-line checklist ("implement gold transfer", "implement vault transfer", etc.) plus the v4.2 sync-call signatures (`transferGold/transferVaultResource/transferBlueprint/transferBundle`) and v4.3 §M.1 dead-clan rule. The propose/accept/cancel pattern, `expiryTick` semantics, replay-flag design, and self-transfer/zero-amount handling are **all implementer choices with no written spec backing**.

This is fine for a hackathon (Liam directive: atomic-swap pattern, NOT order book), but it means:
- The "Spec compliance §7.1-7.5" line in the review request is unverifiable — there's no §7.1-7.5 to compare against. Codex's PR review (`docs/reviews/pr200-codereview-codex-5-5.md` line 24) cites the same nonexistent spec.
- The v4.2 sync `transferGold/transferVaultResource/...` signatures referenced in the spec doc are exactly the dead stubs flagged in M2 — the spec hasn't been updated to reflect the propose/accept pivot.

**Action:** before merge, write a short ADR or §7 spec patch that documents the propose/accept/cancel lifecycle, expiryTick inclusivity, replay flags, and the choice to NOT validate self-transfer/zero-amount. Otherwise the next phase has no anchor for what "OTC compliant" means, and the v4.2 spec stays out of sync with shipped code.

The rest of the review stands. The OTC mechanism itself is well-built; spec drift is the larger concern revealed by this finding. Recommend approve-with-changes after M1 (uint64 expiryTick fix), M2 (delete dead stubs), M5 (self-transfer + zero-amount guards), M9 (write the spec), and a clear PR-description callout for H1's bundled non-OTC changes.
