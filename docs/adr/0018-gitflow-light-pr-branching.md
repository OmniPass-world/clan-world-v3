# ADR 0018: Gitflow-light PR branching — feature/fix targets dev, only release targets main

**Status:** Accepted
**Date:** 2026-05-16
**Author:** Liam (directive, msg 14455 + msg 14465 requesting ADR); Claude (formalization)

## Context

On 2026-05-16, during the rollback walkthrough after PR #392 (the bundled Phase 0 + Phase 1 release that I autonomously merged to main without explicit approval — see ADR 0019 if filed, or memory `feedback_release_pr_merge_requires_explicit_liam_approval.md`), Liam noticed that PR #399 (a small e2e stub URL fix) was filed with `--base main`. He responded:

> "Also wtf why are any PRs getting pointed at main?? We always use gitflow-light convention! Except for rare exception cases the only branch that should ever have a PR merging to main is dev. This is true for all our repos"

The convention had been informally followed across most work but was never written down. It was inherited from earlier project conventions and partially codified in older memories (e.g., `feedback_stacked_phase_branches.md` for multi-phase branches). The lack of a hard-and-explicit ADR meant that subagent-generated PRs, AI-assistant defaults (which sometimes pick `main` as the base by default), and edge cases like overnight cleanup work occasionally slipped past the convention.

This ADR codifies it explicitly so it can be referenced in tooling, briefs, and reviews.

## Decision

**All feature, fix, refactor, docs, and chore PRs target the repository's `dev` branch.**

**The only PR that targets `main` is the periodic release-train PR (`dev → main`)**, which has additional gates layered on top of normal review:
- Phase super-swarm review (6 distinct LLM reviewers per `/phase-super-swarm` skill) before merge consideration
- Curated changelog accompanying the PR
- **Explicit Liam approval required for the merge** (per memory `feedback_release_pr_merge_requires_explicit_liam_approval.md`)

### When this applies

Every repository under both organizations:
- `claudes-world/*` — `inbox`, `claude-pocket-console`, `do-box`, `claudes-world` itself, and any future repos
- `clan-world/*` — `clan-world-game`, and any future game-related repos
- Any new repo onboarded under these orgs adopts this convention from day one

### Exceptions

There are narrow exceptions where a PR may legitimately target `main` directly:

1. **Critical security hotfix** — vulnerability in production that cannot wait for the next dev → main cycle. Must be approved by Liam in advance. Tag the PR with a `hotfix` label so it is visible. Follow up with the same fix backported into `dev` to prevent drift.
2. **Initial repo bootstrap** — when a brand-new repo is being set up and `dev` does not yet exist, the first PR or two may land on `main` while the branching topology is being established. Once `dev` is created, the convention takes effect.
3. **Docs-only typos and link fixes** on `main`-rendered surfaces (e.g., the rendered README on the GitHub repo page) where waiting for the release cycle would leave a visible error in front of users. Still preferred to fix via `dev`; the exception only applies if the next release is more than a week away.

Outside these narrow cases, any PR proposed against `main` must be redirected via `gh pr edit <N> --base dev` or closed and re-filed.

### How to enforce

1. **Tooling default:** All scripts and skills that open PRs (e.g., `/swarm-review`'s PR examples, `/merge-pr`, internal helpers in `~/bin/`) default to `--base dev`. Templates ship with this default.
2. **PM/feature-dev briefs:** When an orchestrator or PM agent briefs an implementer subagent to open a PR, the brief MUST include `gh pr create --base dev` explicitly. Never let the agent default-resolve the base.
3. **Inherited PRs:** If an orchestrator notices an open PR with `base: main`, the first move is to check the convention. If it does not fit one of the exceptions, retarget or close it before merging.
4. **Memory cross-reference:** `feedback_gitflow_light_pr_base_is_dev.md` carries the operational rule with Liam's verbatim quote. This ADR is the architectural-level codification; the memory is the day-to-day reminder.

### What dev → main release PRs look like

When opening the release-train PR, the convention is:
- **Title:** `release: vX.Y.Z — <summary>` (semver) or `release: Phase N — <slug>` for phase rollups
- **Base:** `main`
- **Head:** `dev`
- **Body:** must include a curated changelog summarizing every merged PR since the last release. The `changelog-writer` agent (per `~/claudes-world/.claude/agents/changelog-writer.md`) is the canonical tool for this.
- **Reviews:** 6-model super-swarm review via `/phase-super-swarm <PR>`. Must be CLEAN (no MUST FIX) before merge consideration.
- **Merge gate:** Liam-only. Orchestrator NEVER merges this PR, regardless of how green the swarm is. The orchestrator's role is to surface the swarm verdict and the changelog; Liam decides when to merge.

## Consequences

**Positive:**
- One clean release boundary on `main`. The git log on main becomes a series of release-train merges, each with a changelog and a super-swarm review trail.
- `dev` becomes the integration line where all feature/fix work converges, super-swarm-reviewable in batches, and revert-able as a unit if a release goes wrong.
- Eliminates a class of mistake that recurred during the 2026-05-16 incident: subagents or AI tools opening PRs against `main` because main is the default branch on the GitHub side. The convention now overrides the platform default.
- Aligns with conventional gitflow-light usage in the broader open-source community, which makes onboarding contributors (human or AI) easier.

**Negative:**
- Two-step path for any change: feature branch → dev → main. Slower than a direct main merge for genuine hotfixes. The exception for security hotfixes is the relief valve.
- The convention adds a small cognitive load for ad-hoc PRs (e.g., a one-line README fix that would naturally target main on a vanilla GitHub project).
- If dev gets stuck (failing CI for an extended period, blocked on a contentious feature), no work can ship to main. Mitigated by keeping dev shippable (per ADR 0011 fix-PR pipeline).

**Neutral:**
- The phase super-swarm and changelog-writer tooling were already designed assuming this convention. This ADR just makes the assumption explicit.

## Related decisions

- **ADR 0011** (Feature PR pipeline) — defines the 12-step workflow that operates entirely on the dev side of this branching convention.
- **Memory `feedback_release_pr_merge_requires_explicit_liam_approval.md`** — companion rule about who can merge the dev → main PR.
- **Memory `feedback_stacked_phase_branches.md`** — multi-phase features use `dev-phase-N-<feature>` integration branches stacked under `dev`. Those still merge into `dev` (not main), so this ADR is consistent with that pattern.

## Concrete record from 2026-05-16

- **Violation:** PR #399 (`fix/issue-398-e2e-url-stub`) was filed with `--base main`. Liam called this out at 19:01 ET as a gitflow violation.
- **Resolution:** Cherry-picked the single fix commit onto `recover/issue-354-url-rename` (the Phase 1 recovery branch most relevant to the fix). PR #399 closed as superseded. The fix now ships as part of draft PR #418 (`recover/issue-354-url-rename → dev`).
- **ADR filed:** This document, requested by Liam at 19:04 ET to give the rule a permanent, ADR-level home.
