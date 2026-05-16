# ClanWorld Dockerize Migration v1 — Rehearsal Transcript

> **Instructions for operator:** Fill in each section below as you execute the rehearsal pass using `docker-compose.rehearsal.yml`. Paste terminal output into the code blocks. Sign off at the bottom when complete. This signed transcript is required before scheduling the real cutover.

---

## Metadata

| Field | Value |
|---|---|
| Date | |
| Operator | |
| VPS hostname | |
| Repo commit | |
| `CONVEX_BACKEND_TAG` | `dd00d1f30042cedab25b8cc76a23c40cc2abbd90` |
| `CONVEX_DASHBOARD_TAG` | `dd00d1f30042cedab25b8cc76a23c40cc2abbd90` |
| `CONVEX_CLI_PINNED_VERSION` | `1.17.4` |

---

## Step 1.a — Stand up rehearsal stack

**Command run:**
```
docker compose -f docker-compose.rehearsal.yml up -d
```

**Output:**
```

```

**`docker compose -f docker-compose.rehearsal.yml ps` output:**
```

```

**Result:** [ ] PASS  [ ] FAIL

---

## Step 1.b — Export hosted Convex + schema fingerprint

**Command run:**
```
npx convex@1.17.4 export --path /tmp/clanworld-rehearsal-<timestamp>.zip --include-file-storage
npx convex@1.17.4 schema --json | sha256sum > /tmp/hosted-schema.sha256
```

**Export size:**
```

```

**Hosted schema fingerprint (`cat /tmp/hosted-schema.sha256`):**
```

```

**Result:** [ ] PASS  [ ] FAIL

---

## Step 1.c — Deploy schema to rehearsal + fingerprint comparison

**Command run:**
```
CONVEX_SELF_HOSTED_URL=http://localhost:38050 \
  npx convex@1.17.4 deploy --admin-key "$(cat agents/secrets/convex-admin.key)"
CONVEX_SELF_HOSTED_URL=http://localhost:38050 \
  npx convex@1.17.4 schema --json | sha256sum > /tmp/rehearsal-schema.sha256
diff /tmp/hosted-schema.sha256 /tmp/rehearsal-schema.sha256
```

**Deploy output:**
```

```

**`diff` output (MUST be empty):**
```

```

**Result:** [ ] PASS (diff empty)  [ ] FAIL (diff non-empty — STOP, investigate)

---

## Step 1.d — Import hosted data into rehearsal

**Command run:**
```
npx convex@1.17.4 import --replace-all \
  --instance-url http://localhost:38050 \
  /tmp/clanworld-rehearsal-*.zip
```

**Import output:**
```

```

**Post-import fingerprint check (`diff /tmp/hosted-schema.sha256 /tmp/rehearsal-post-import.sha256`):**
```

```

**Result:** [ ] PASS  [ ] FAIL

---

## Step 1.e — Rehearsal teardown

**Command run:**
```
docker compose -f docker-compose.rehearsal.yml down -v
```

**Output:**
```

```

**Result:** [ ] PASS  [ ] FAIL

---

## Issues encountered

_List any errors, unexpected output, or steps that required deviation from the runbook. Include the step number, the error, and how it was resolved._

| Step | Issue | Resolution |
|---|---|---|
| | | |

---

## Sign-off

I have executed the full rehearsal pass. All validation gates passed. The self-hosted stack accepted the hosted data export cleanly with matching schema fingerprints.

**This stack is ready for real cutover scheduling.**

| Field | Value |
|---|---|
| Signed by | |
| Date | |
| Next action | Schedule real cutover with Liam |
