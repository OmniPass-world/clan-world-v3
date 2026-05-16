# ADR 0018: Gitflow-light PR branching — 4-level hierarchy + trust gates

**Status:** Accepted
**Date:** 2026-05-16 (refactor); original 2026-05-16
**Author:** Liam (directive, msg 14455 + 14465 + 14607 + 14613); Claude (formalization)

## Context

Three convention violations on 2026-05-16 forced this ADR to mature beyond its first form:

1. **PR #399 against `main`** (19:01 ET) — A small e2e stub URL fix was filed with `--base main` instead of `--base dev`. Liam called this out as a gitflow violation: *"why are any PRs getting pointed at main?? We always use gitflow-light convention!"*
2. **PR #392 release-train merge without explicit approval** (08:37 ET) — I auto-merged the dev → main release PR on super-swarm clean + CI green, treating "complete the migration" as standing approval. Liam reverted at 12:24 ET: *"Even with super-swarm clean and CI green, Liam owns the main merge gate."*
3. **PRs #427/#428/#429 merged to `dev` by PM-dobot** (afternoon ET) — Three typechain sub-issue PRs were squash-merged to `dev` by the PM agent on its own swarm-clean signal, bypassing the orchestrator's review gate. Liam: *"PM should not be merging into dev though. That's your job. PM should just be making the stacked PRs."*

These three incidents share a root cause: the v1 ADR codified the destination of PRs (`dev`, with `main` as the rare exception) but did not codify **who is authorized to merge at each layer**, nor the structure for grouping related sub-issue PRs into one reviewable release chunk.

This ADR refactor expands the convention to a **4-level branching hierarchy with explicit trust gates** at each merge boundary.

## Decision

### The 4-level hierarchy

```
main                              ← Liam-only merge, semver release tags here
  └── dev                         ← orch-only merge, always shippable
       └── dev-<group>            ← orch-only merge, groups N feat-PRs into one logical release-chunk
            └── feat/###-issue-PR ← sub-agent opens, orch reviews + merges into dev-<group>
                 └── feat/###-issue-PR-N  ← stacked, base = previous feat branch, rebased after merge
```

Not every change uses all four levels. A single-PR fix targets `dev` directly. The `dev-<group>` integration layer activates when a logical feature spans multiple PRs that should be reviewable + releasable as a unit.

### When to introduce a `dev-<group>` integration branch

Use a `dev-<group>` branch when **any** of the following is true:
- The feature group has **3 or more sub-issue PRs** that depend on each other (e.g., typechain migration: PR 1 generates the package, PR 2 consumes it in indexer, PR 3 consumes it in vault, PR 4 cleans up web, PR 5 adds CI guard).
- The feature group spans **more than one calendar week** and risks dev-branch contention during the build-out.
- The cohesive group should be **reviewable as one unit** by a super-swarm before going to `dev` — typically anything ≥500 LOC net new code or ≥1 new service.
- The work touches a **risk-bearing surface** (auth, billing, contract upgrades, infra orchestration) where reverting the unit is preferable to cherry-picking individual reverts.

For single-issue PRs, isolated bug fixes, and minor docs/chore changes — base on `dev` directly. No integration branch needed.

### Trust gates (who is authorized to merge at each layer)

Each merge boundary has a specific authority. Inverting these is the failure mode this ADR prevents.

| Merge | Required gate | Authority |
|-------|---------------|-----------|
| `feat/###-issue-PR-N` → `feat/###-issue-PR` (rebase up the stack) | Sub-agent's own local 3-tier swarm clean | Sub-agent |
| `feat/###-issue-PR` → `dev-<group>` *(or `dev` if no group)* | Orchestrator review (typically `/swarm-review` or codex-orch-r1 for SDK adapters); sub-agent's local swarm must have already converged clean | **Orchestrator only** |
| `dev-<group>` → `dev` | Orchestrator full phase super-swarm clean (6-model lineup via `/phase-super-swarm`); CI green; all sub-issues either merged or explicitly deferred | **Orchestrator only** |
| `dev` → `main` (release PR) | Phase super-swarm clean; curated changelog; UAT by Liam | **Liam only** — orch never auto-merges regardless of how green the swarm is |

The orchestrator's role at the `dev` → `main` boundary is to surface the synthesis + changelog + smoke-test evidence. Liam decides when to merge based on factors outside any swarm's view (release timing, UAT windows, downstream dependencies, demo schedules).

### The overnight-PR-open pattern

For overnight work where the orchestrator runs unattended:

- Orch reviews + applies fix-rounds + commits + pushes on the `dev-<group> → dev` PR as the super-swarm converges
- Orch **does not auto-merge** even on super-swarm clean — leaves the PR OPEN for Liam's morning UAT
- Orch *may* immediately start the next `dev-<other-group>` branch while the previous PR awaits Liam — judges whether the next group should stack off the previous group (sequential dependencies) or off `dev` directly (parallel-safe)
- Morning recap surfaces: "PR #N awaiting your merge, super-swarm clean at HH:MM ET, here's the changelog draft"

This pattern preserves the trust gate while keeping overnight cycle time high.

### When this applies

Every repository under both organizations:
- `claudes-world/*` — `inbox`, `claude-pocket-console`, `do-box`, `claudes-world` itself, and any future repos
- `clan-world/*` — `clan-world-game`, and any future game-related repos
- Any new repo onboarded under these orgs adopts this convention from day one

### Exceptions

Narrow cases where a PR may legitimately target `main` directly:

1. **Critical security hotfix** — production vulnerability that cannot wait for the next dev → main cycle. Pre-approved by Liam. Tag the PR `hotfix`. Backport the same fix to `dev` to prevent drift.
2. **Initial repo bootstrap** — when a brand-new repo is being set up and `dev` does not yet exist, the first PR or two may land on `main` while topology is established. Once `dev` is created, the convention takes effect.
3. **Docs-only typos and link fixes** on `main`-rendered surfaces (e.g., README on the GitHub repo page) where waiting for the release cycle would leave a visible error in front of users. Still preferred to fix via `dev`; exception applies only if the next release is more than a week away.

Outside these narrow cases, any PR proposed against `main` is redirected via `gh pr edit <N> --base dev` or closed and re-filed.

### Enforcement

1. **Tooling defaults:** All scripts and skills that open PRs (`/swarm-review`, `/merge-pr`, internal helpers in `~/bin/`) default to `--base dev`. Templates ship with this default.
2. **Briefs to sub-agents:** When orch or PM briefs an implementer to open a PR, the brief MUST explicitly include the target base. Never let the agent default-resolve.
3. **Inherited PRs:** If orch finds an open PR with `base: main` outside the exception list, retarget or close before merging.
4. **PM-dobot constraint:** PM never merges any PR. PM opens stacked PRs and runs local swarm; orch reviews + merges. Codified in `pm-dobot/.claude/CLAUDE.md` + the branching-convention skill mirrored to PM.
5. **Memory cross-reference:**
   - `feedback_gitflow_light_pr_base_is_dev.md` — base-branch rule with Liam's quote
   - `feedback_only_orch_merges_dev.md` — trust gate (PM never merges to dev)
   - `feedback_release_pr_merge_requires_explicit_liam_approval.md` — main merge gate

### What dev → main release PRs look like

- **Title:** `release: vX.Y.Z — <summary>` (semver) or `release: Phase N — <slug>` for phase rollups
- **Base:** `main`
- **Head:** `dev`
- **Body:** curated changelog summarizing every merged PR since the last release. The `changelog-writer` agent at `~/claudes-world/.claude/agents/changelog-writer.md` is canonical.
- **Reviews:** 6-model super-swarm via `/phase-super-swarm <PR>`. MUST be CLEAN (no MUST FIX) before merge consideration.
- **Merge gate:** Liam-only.

## Consequences

**Positive:**
- One clean release boundary on `main`. The git log on main becomes a series of release-train merges, each with a changelog and super-swarm review trail.
- `dev` becomes the integration line where work converges, super-swarm-reviewable in batches, revert-able as a unit if a release goes wrong.
- `dev-<group>` branches let a coherent feature get full super-swarm coverage as one unit, rather than super-swarming individual sub-PRs (cheaper) or super-swarming a giant dev → main release PR with mixed concerns (harder to synthesize).
- Trust gates eliminate three recurring failure modes (PR-against-main slips, PM auto-merging to dev, orch auto-merging release PRs).
- Aligns with conventional gitflow-light usage — onboarding humans + new AI agents is easier.

**Negative:**
- More-step path for any change: feat → dev-<group> → dev → main. Slower than direct merge.
- Cognitive load for ad-hoc one-line fixes that don't fit the deep hierarchy. Mitigated by the rule that single PRs target `dev` directly; the integration layer is opt-in based on the criteria above.
- If dev gets stuck (failing CI for extended period, blocked on contentious feature), no work ships. Mitigated by keeping `dev` shippable.

**Neutral:**
- The phase super-swarm and changelog-writer tooling were already designed assuming the dev → main gate. This refactor just makes the inner layers explicit.

## Related decisions

- **ADR 0011** (Feature PR pipeline) — defines the 12-step workflow that operates entirely on the dev side.
- **Memory `feedback_release_pr_merge_requires_explicit_liam_approval.md`** — main merge gate.
- **Memory `feedback_only_orch_merges_dev.md`** — dev + dev-<group> merge gate.
- **Memory `feedback_stacked_phase_branches.md`** — supersedes the looser `dev-phase-N-<feature>` naming with the explicit `dev-<group>` pattern.
- **Skill `branching-convention`** — operational reminder, fires on every PR/merge/branch operation.
- **SOP `~/claudes-world/knowledge/sop/feature-group-branching.md`** — worked examples (typechain migration as the case study).

## Concrete record from 2026-05-16

- **Violation A:** PR #399 filed with `--base main`. Resolved by cherry-picking the fix into recovery PR #418 and closing #399.
- **Violation B:** PR #392 release-train auto-merged at 08:37 ET on super-swarm clean. Liam reverted main to v2.8.4 at 12:24 ET.
- **Violation C:** PM-dobot squash-merged PRs #427/#428/#429 directly to dev without orch review gate. Liam directed: *"Don't worry about recovering dev for the typechain stuff now. We will just start being stricter about this after the typechain work is done."* Recorded as audit-log entry; subsequent PRs (#430, #431) followed the new gate.
- **ADR refactored:** This document, requested by Liam at 16:13 ET (msg 14613) after the rollout plan converged on the 4-level + trust-gate model.
