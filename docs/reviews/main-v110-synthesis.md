# v1.1.0 Post-Tag Audit Synthesis — main `1487cd7`

**Date:** 2026-05-02 02:00 ET
**Reviewers:** codex 5-4 ✅ NEEDS_HOTFIX, codex 5-5 ✅ NEEDS_HOTFIX, Claude feature-dev:code-reviewer ✅ NEEDS_V12_WORK, gemini 3.1 Pro ❌ (Ripgrep error), opus 4.6 ❌ (silent fail), opus 4.7 ❌ (silent fail)

**Verdict:** **NEEDS v1.1.1 HOTFIX** — 2/3 reviewers converged on the cockpit auth HIGH (codex 5-4 + 5-5; Claude didn't surface it). Claude added 4 distinct MEDIUMs codex missed including a real `stepStatus` ternary bug. ClanWorld engine itself is CLEAN — all 3 reviewers agree (boundary-freeze coverage complete, starvation/freeze semantics aligned, webhook auth directionally correct).

---

## MUST FIX — v1.1.1 hotfix

### MUST-1.1.1.1 — Bridge cockpit API exposes secret-backed commands without independent auth

**Confirmed by:** codex 5-4 + codex 5-5 (convergent)

**Files:**
- `gold-bridge-monorepo/scripts/20-cockpit-api.mjs:31-107, 189-197, 228-230, 1435-1455, 1504`
- `gold-bridge-monorepo/.env.template:87-90`

**Bug:** The cockpit is a localhost control plane for privileged operations, but:

1. **Default CORS allowlist includes `https://bridge-dev.clan-world.com`** (a remote origin), not just localhost.
2. **No independent auth beyond CORS.** Any XSS, compromised deployment, or malicious JS served from the allowed remote origin can drive the operator's localhost API.
3. **Privileged endpoints exposed:** `recover-base-execute`, `timelock-schedule`, `timelock-execute`, `ntt:*` series, `liquidity:recover-base`. These run with the operator's local `.env` and private keys.
4. **`/api/actions` and `/api/actions/:id/preview` reveal confirmation strings** needed by `/run` — a remote attacker reading those + crafting a `/run` request gets full privileged execution.
5. **Env var name typo:** template documents `COCKPIT_CORS_ORIGIN`, server reads `COCKPIT_CORS_ORIGINS`. Operators following the template believe they've changed the allowlist; in fact their override is silently ignored, leaving the remote default in place.

**Effect:** A funded bridge-operator running cockpit with default config has a remotely-driveable privileged surface. The remote origin doesn't even need to be malicious — a compromised `bridge-dev.clan-world.com` deployment, a stored XSS, or a local-network attack on DNS for that domain all yield the same outcome.

**Suggested fix (synthesizing both reviewers):**
1. **Default-allow only localhost origins.** Remove `bridge-dev.clan-world.com` from code defaults.
2. **Honor the documented env var name** (or rename the template — pick one, document explicitly, print active allowlist on startup).
3. **Require an operator secret/nonce** on every mutating + reconcile endpoint. Use `COCKPIT_API_TOKEN` (high-entropy) or signed nonce. CORS alone is not auth.
4. **Stop exposing runnable confirmation values from public read endpoints.**

---

## SHOULD FIX — bundle into v1.1.1 if cheap, else v1.2

### SHOULD-1 — `stepStatus` ternary bug — both branches return `'ready'`

**Confirmed by:** Claude

**File:** `gold-bridge-monorepo/scripts/20-cockpit-api.mjs:1088-1091`

**Bug:**
```javascript
function stepStatus(done, blockers = []) {
  if (done) return 'done';
  return blockers.length ? 'ready' : 'ready';  // both branches return 'ready'
}
```

Both ternary branches return `'ready'`. Passing blockers has no effect. Steps that should show `'blocked'` show `'ready'` in the cockpit guide sidebar, misleading operators about execution prerequisites. Downstream pass partially rescues via `dependsOn` chains, but direct `stepStatus(false, ['dep'])` callers fail.

**Fix:** `return blockers.length ? 'blocked' : 'ready';`

This is a small standalone fix. **Bundle into v1.1.1.**

### SHOULD-2 — Wallet intent reconciliation trusts browser-supplied data

**Confirmed by:** codex 5-4

**File:** `gold-bridge-monorepo/scripts/20-cockpit-api.mjs:368-399`

**Bug:** For `deploy-base-gold-proxy`, the server does not verify that `contractAddress` actually came from `txHash`, that the tx succeeded, that it's on the expected chain, or that helper bytecode matches `UpgradeableGoldDeployer`. It just reads three getters from whatever the browser passed and persists token/timelock/admin addresses into `.env`.

**Effect:** Benign failure → poisoned local deployment state aiming later upgrade/minter/recovery actions at wrong contracts. Same threat model as MUST-1.1.1.1 → state poisoning trivial.

**Fix:** Server-side fetch the receipt; verify chain + success + contract creation address; verify helper bytecode or expected immutable layout before writing env updates.

### SHOULD-3 — `payload.engineAddress` validation only fires for strings

**Confirmed by:** codex 5-5

**File:** `apps/server/convex/heartbeat.ts:77`

**Bug:** Validation only runs when `payload.engineAddress` is a string. Missing, null, numeric, or object values silently accepted. Receipt `to` + log-address filters still prevent wrong-engine ingestion (so not direct spoofing), but weakens the auth/diagnostic invariant.

**Fix:** Require valid 20-byte hex address equal to `CLAN_WORLD_CONTRACT_ADDRESS`; reject other shapes.

### SHOULD-4 — `recoverFromAllowedSource` lacks EOA-only enforcement

**Confirmed by:** Claude

**File:** `gold-bridge-monorepo/packages/contracts/src/GoldBridgeToken.sol:110-126`

**Bug:** `recoverFromAllowedSource` calls `_transfer(source, recipient, amount)` which works on any address. The README warns "User wallets should not be allowlisted" but there is no on-chain enforcement. If timelock governance mistakenly allowlists a user's Smart Account or Safe wallet, the owner can forcibly move their GOLD with no recourse. Protected by 24h `TIMELOCK_DELAY_SECONDS` (sufficient for testnet) but needs explicit governance policy doc + ideally on-chain check before mainnet.

**Fix for v1.2:** Add `require(source.code.length > 0, "GoldBridgeToken: recovery source must be contract");` to `setRecoveryAllowed`. Document governance policy. Consider whether recovery hook needed post-NTT-integration (`disableRecoveryForever` is the preferred endgame).

### SHOULD-5 — `BridgePanel` hidden when cockpit API offline

**Confirmed by:** Claude

**File:** `gold-bridge-monorepo/apps/web/src/components/CockpitDashboard.tsx:95-107`

**Bug:** `BridgePanel` reads from pre-generated `goldDeployment.ts` and doesn't need cockpit API, but all tabs gated behind `{state && ...}` where `state` is the cockpit API response. Frontend running independently → tabs blank, Wormhole Connect inaccessible.

**Fix:** Move `{tab === 'bridge' && <BridgePanel />}` outside the `{state && ...}` guard.

---

## DEFER — v1.2 work

### V1.2-1 — Decimal reconciliation between bridge GOLD (9) and game GOLD (18)

**Confirmed by:** codex 5-5

**Files:** `gold-bridge-monorepo/packages/contracts/src/GoldBridgeToken.sol:27`, `packages/contracts/src/ClanWorld.sol`

Bridge GOLD = 9 decimals; game GOLD = 18 decimals. Not a v1.1.0 bug because bridge is not yet integrated into game flows. v1.2 needs explicit conversion boundary, dust rejection, round-trip tests. Codex 5-5 verified Wormhole NTT EVM docs match the bridge's `burn(uint256)` + `mint(address,uint256)` interface.

### V1.2-2 — `numberFromPayload` NaN handling

**File:** `apps/server/convex/heartbeat.ts:31`

`numberFromPayload` can convert non-empty nonnumeric string into NaN. Authenticated path, low impact. Drop non-finite or 400.

### V1.2-3 — No-op heartbeat keeper churn

**File:** `packages/contracts/src/ClanWorld.sol:2989`

During season-finalization limbo, `heartbeat()` returns before updating `nextHeartbeatAtTs`. Correct for avoiding replay, but keepers can keep submitting successful no-op txs once old timestamp is reached. Optional: emit no-op event or throttle if gas churn gets noisy.

---

## SKIP

None convergent. The 3 LOWs above are all explicitly flagged as deferred/not-blocking by the reviewers.

---

## Cross-cutting — what's CLEAN (both reviewers agree)

✅ **`_requireNoPendingSeasonFinalization` coverage complete** for all IClanWorld external mutators: settle, mint, submit orders, ownership transfer, OTC transfers all guard before settlement or balance mutation. `initTreasury` and `seedPools` are owner-only setup actions and don't mutate clan ranking inputs — coverage decision is consistent.

✅ **Heartbeat boundary freeze placed correctly** — before settlement, market execution, bandit progression, winter transitions, and tick increment. Round-2's "freeze too late" replay class did NOT reintroduce.

✅ **Starvation `tick+1` onset + strict `< tick` winter-kill semantics** consistent across both storage settlement (`_settleClanThroughTick`) and simulation paths (`_isStarvingAtTick`).

✅ **Convex real-indexer receipt checks** directionally correct: waits for receipt, rejects reverted tx, compares `receipt.to`, filters logs by engine address before parsing, uses receipt block for snapshot refresh. Remaining gap is payload-shape strictness, not log spoofing.

✅ **Wormhole NTT EVM token interface alignment** — bridge's `burn(uint256)` + `mint(address,uint256)` matches Wormhole spec.

---

## V1.1.1 hotfix dispatch shape

**Branch:** `fix/v111-bridge-cockpit-auth` based on `main` HEAD `1487cd7`.

**Scope:** Single codex Pattern B-bulk dispatch addressing MUST-1.1.1.1 (cockpit API auth + env var typo + remove remote default origin + stop exposing confirmation strings publicly). Optional: bundle SHOULD-1 (wallet intent verification) into the same PR if Liam wants belt-and-suspenders.

**Validation:**
- Cockpit cannot be driven from a remote origin without `COCKPIT_API_TOKEN` set to a non-empty value
- Mutating endpoints reject requests without valid token/nonce
- `/api/actions/preview` doesn't reveal confirmation strings used by `/run`
- Env var name documented + read by server matches; startup logs active allowlist
- Manual smoke test: localhost-only operation works; remote origin without token denied

**Time:** ~30-45 min for the cockpit hardening. Add ~15 min if bundling SHOULD-1.

---

## Round-1 reviewer attribution

| Reviewer | File | Verdict | HIGH count | Notes |
|---|---|---|---|---|
| codex 5-4 | `/tmp/main-v110-review-codex-5-4.log` | NEEDS_HOTFIX | 1 | Cockpit auth + env var typo (with fix details) + SHOULD-1 |
| codex 5-5 | `/tmp/main-v110-review-codex-5-5.log` | NEEDS_HOTFIX | 1 | Same cockpit finding + SHOULD-3 + V1.2-1/2/3 |
| Claude feature-dev:code-reviewer | (read-only sandbox; review verbatim in agent reply) | NEEDS_V12_WORK | 0 | **Missed cockpit auth HIGH** but added 4 distinct MEDIUMs (stepStatus ternary, recoverFromAllowedSource, BridgePanel gating, timelock delay test gap) + confirmed clean cross-cutting + explicit decimal conversion warning for v1.2 |
| gemini 3.1 Pro | `docs/reviews/main-v110-codereview-gemini-3-1-pro.md` | (failed early — Ripgrep not available) | — | — |
| opus 4.6 | `docs/reviews/main-v110-codereview-opus-4-6.md` | (silent fail, 0 bytes) | — | Headless `claude -p` known issue |
| opus 4.7 | `docs/reviews/main-v110-codereview-opus-4-7.md` | (silent fail, 0 bytes) | — | Same |

**Convergence:** 2/3 reviewers (both codex) identified the same MUST finding with identical file paths, line numbers, and threat model. Claude missed the cockpit auth issue but found 4 distinct MEDIUMs the codex reviewers missed (most notably the `stepStatus` ternary bug — both branches return `'ready'`, real fix needed). The cross-model gap on the cockpit auth is interesting: codex models read the cockpit API code in security-review mode; Claude focused more on contract surface analysis.

Strong signal — v1.1.1 hotfix is justified. Recommended hotfix scope: MUST-1.1.1.1 (cockpit API auth + env var typo) + SHOULD-1 (stepStatus ternary, trivial fix). SHOULDs 2-5 deferred to v1.2.

🤖 Synthesized by Claude Opus 4.7 from codex review logs in /tmp/main-v110-review-codex-*.log
