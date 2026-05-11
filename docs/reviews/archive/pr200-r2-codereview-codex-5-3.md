Reading additional input from stdin...
OpenAI Codex v0.125.0 (research preview)
--------
workdir: /home/claude/code/clan-world
model: gpt-5.3-codex
provider: openai
approval: never
sandbox: read-only
reasoning effort: high
reasoning summaries: none
session id: 019ddc0a-4ef0-7e90-8c28-71f80df60889
--------
user
Read prompt+diff from stdin. Use parallel tool calls + subagents. Output full review in requested format.

<stdin>
You are a senior staff engineer doing the FINAL pre-merge code review for a multi-issue phase release PR.

PR: Phase 7 — OTC Transfer Surface (post fix-round merge)
Head SHA: 2bec876

## Your task

This is a COHESIVE PHASE — sub-issues + fix-rounds already merged. Review the integrated whole. Look for:

1. CROSS-CUTTING bugs that surface at integration seams (race conditions, state machine inconsistencies, broken invariants across components, regressions introduced by fix-rounds themselves)
2. ARCHITECTURAL drift — does the phase deliver its stated goal?
3. SECURITY surface — auth, input validation, prompt injection, TOCTOU, resource leaks
4. DATA-flow correctness — schema consistency, idempotency, error paths
5. Integration risks — newly-added code's effect on existing code paths
6. Missing test coverage on integration seams
7. Specifically check that fix-round fixes didn't introduce NEW regressions

USE PARALLEL TOOL CALLS / SUB-AGENTS aggressively. Read changed files. Trace state machines. Don't skim.

## Output format

# Phase Super-Swarm Review — PR #200 (head 2bec876)

## SUMMARY
2-4 sentences: verdict (CLEAN | NEEDS_FIXES), top concerns, merge recommendation.

## HIGH severity findings
(real bugs with file:line + paragraph + suggested fix)

## MEDIUM severity findings
(should-fix; design quality, edge cases, ops)

## LOW severity findings
(defer-OK; nits, polish, follow-ups)

## Cross-cutting observations

If clean, say "CLEAN — no findings" under each section.

DIFF FOLLOWS BELOW.
---
---
diff --git a/packages/contracts/abi/IClanWorld.json b/packages/contracts/abi/IClanWorld.json
index 2ca0d2c..c248b55 100644
--- a/packages/contracts/abi/IClanWorld.json
+++ b/packages/contracts/abi/IClanWorld.json
@@ -1,5 +1,109 @@
 {
   "abi": [
+    {
+      "type": "function",
+      "name": "acceptBlueprintTransfer",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "outputs": [],
+      "stateMutability": "nonpayable"
+    },
+    {
+      "type": "function",
+      "name": "acceptBundledTransfer",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "outputs": [],
+      "stateMutability": "nonpayable"
+    },
+    {
+      "type": "function",
+      "name": "acceptGoldTransfer",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "outputs": [],
+      "stateMutability": "nonpayable"
+    },
+    {
+      "type": "function",
+      "name": "acceptVaultTransfer",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "outputs": [],
+      "stateMutability": "nonpayable"
+    },
+    {
+      "type": "function",
+      "name": "cancelBlueprintTransfer",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "outputs": [],
+      "stateMutability": "nonpayable"
+    },
+    {
+      "type": "function",
+      "name": "cancelBundledTransfer",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "outputs": [],
+      "stateMutability": "nonpayable"
+    },
+    {
+      "type": "function",
+      "name": "cancelGoldTransfer",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "outputs": [],
+      "stateMutability": "nonpayable"
+    },
+    {
+      "type": "function",
+      "name": "cancelVaultTransfer",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "outputs": [],
+      "stateMutability": "nonpayable"
+    },
     {
       "type": "function",
       "name": "finalizeSeason",
@@ -7,6 +111,25 @@
       "outputs": [],
       "stateMutability": "nonpayable"
     },
+    {
+      "type": "function",
+      "name": "getActionDuration",
+      "inputs": [
+        {
+          "name": "action",
+          "type": "uint8",
+          "internalType": "enum ActionType"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "",
+          "type": "uint64",
+          "internalType": "uint64"
+        }
+      ],
+      "stateMutability": "pure"
+    },
     {
       "type": "function",
       "name": "getActiveBanditView",
@@ -232,83 +355,6 @@
       ],
       "stateMutability": "view"
     },
-    {
-      "type": "function",
-      "name": "getMissionTiming",
-      "inputs": [
-        {
-          "name": "clanId",
-          "type": "uint32",
-          "internalType": "uint32"
-        },
-        {
-          "name": "clansmanId",
-          "type": "uint32",
-          "internalType": "uint32"
-        }
-      ],
-      "outputs": [
-        {
-          "name": "submitted",
-          "type": "uint64",
-          "internalType": "uint64"
-        },
-        {
-          "name": "executes",
-          "type": "uint64",
-          "internalType": "uint64"
-        },
-        {
-          "name": "settles",
-          "type": "uint64",
-          "internalType": "uint64"
-        }
-      ],
-      "stateMutability": "view"
-    },
-    {
-      "type": "function",
-      "name": "getActionDuration",
-      "inputs": [
-        {
-          "name": "action",
-          "type": "uint8",
-          "internalType": "enum ActionType"
-        }
-      ],
-      "outputs": [
-        {
-          "name": "",
-          "type": "uint64",
-          "internalType": "uint64"
-        }
-      ],
-      "stateMutability": "pure"
-    },
-    {
-      "type": "function",
-      "name": "getTravelTicks",
-      "inputs": [
-        {
-          "name": "fromRegion",
-          "type": "uint8",
-          "internalType": "uint8"
-        },
-        {
-          "name": "toRegion",
-          "type": "uint8",
-          "internalType": "uint8"
-        }
-      ],
-      "outputs": [
-        {
-          "name": "",
-          "type": "uint64",
-          "internalType": "uint64"
-        }
-      ],
-      "stateMutability": "pure"
-    },
     {
       "type": "function",
       "name": "getBanditTargetPreview",
@@ -1641,44 +1687,83 @@
     },
     {
       "type": "function",
-      "name": "getRegionPopulation",
+      "name": "getMissionTiming",
       "inputs": [
         {
-          "name": "region",
-          "type": "uint8",
-          "internalType": "uint8"
+          "name": "clanId",
+          "type": "uint32",
+          "internalType": "uint32"
+        },
+        {
+          "name": "clansmanId",
+          "type": "uint32",
+          "internalType": "uint32"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "submitted",
+          "type": "uint64",
+          "internalType": "uint64"
+        },
+        {
+          "name": "executes",
+          "type": "uint64",
+          "internalType": "uint64"
+        },
+        {
+          "name": "settles",
+          "type": "uint64",
+          "internalType": "uint64"
+        }
+      ],
+      "stateMutability": "view"
+    },
+    {
+      "type": "function",
+      "name": "getOtcBlueprintTransferProposal",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "internalType": "uint256"
         }
       ],
       "outputs": [
         {
           "name": "",
-          "type": "tuple[]",
-          "internalType": "struct RegionOccupant[]",
+          "type": "tuple",
+          "internalType": "struct BlueprintTransferProposal",
           "components": [
             {
-              "name": "clansmanId",
+              "name": "from",
               "type": "uint32",
               "internalType": "uint32"
             },
             {
-              "name": "clanId",
+              "name": "to",
               "type": "uint32",
               "internalType": "uint32"
             },
             {
-              "name": "state",
-              "type": "uint8",
-              "internalType": "enum ClansmanState"
-            },
-            {
-              "name": "currentAction",
-              "type": "uint8",
-              "internalType": "enum ActionType"
+              "name": "amount",
+              "type": "uint256",
+              "internalType": "uint256"
             },
             {
-              "name": "missionNonce",
+              "name": "expiryTick",
               "type": "uint64",
               "internalType": "uint64"
+            },
+            {
+              "name": "accepted",
+              "type": "bool",
+              "internalType": "bool"
+            },
+            {
+              "name": "cancelled",
+              "type": "bool",
+              "internalType": "bool"
             }
           ]
         }
@@ -1687,19 +1772,320 @@
     },
     {
       "type": "function",
-      "name": "getScheduledMarketActionsForTick",
+      "name": "getOtcBundledTransferProposal",
       "inputs": [
         {
-          "name": "tick",
-          "type": "uint64",
-          "internalType": "uint64"
+          "name": "proposalId",
+          "type": "uint256",
+          "internalType": "uint256"
         }
       ],
       "outputs": [
         {
           "name": "",
-          "type": "tuple[]",
-          "internalType": "struct ScheduledMarketAction[]",
+          "type": "tuple",
+          "internalType": "struct BundledTransferProposal",
+          "components": [
+            {
+              "name": "from",
+              "type": "uint32",
+              "internalType": "uint32"
+            },
+            {
+              "name": "to",
+              "type": "uint32",
+              "internalType": "uint32"
+            },
+            {
+              "name": "gold",
+              "type": "uint256",
+              "internalType": "uint256"
+            },
+            {
+              "name": "wood",
+              "type": "uint256",
+              "internalType": "uint256"
+            },
+            {
+              "name": "wheat",
+              "type": "uint256",
+              "internalType": "uint256"
+            },
+            {
+              "name": "fish",
+              "type": "uint256",
+              "internalType": "uint256"
+            },
+            {
+              "name": "iron",
+              "type": "uint256",
+              "internalType": "uint256"
+            },
+            {
+              "name": "blueprint",
+              "type": "uint256",
+              "internalType": "uint256"
+            },
+            {
+              "name": "expiryTick",
+              "type": "uint64",
+              "internalType": "uint64"
+            },
+            {
+              "name": "accepted",
+              "type": "bool",
+              "internalType": "bool"
+            },
+            {
+              "name": "cancelled",
+              "type": "bool",
+              "internalType": "bool"
+            }
+          ]
+        }
+      ],
+      "stateMutability": "view"
+    },
+    {
+      "type": "function",
+      "name": "getOtcGoldProposal",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "",
+          "type": "tuple",
+          "internalType": "struct OtcProposal",
+          "components": [
+            {
+              "name": "from",
+              "type": "uint32",
+              "internalType": "uint32"
+            },
+            {
+              "name": "to",
+              "type": "uint32",
+              "internalType": "uint32"
+            },
+            {
+              "name": "amount",
+              "type": "uint256",
+              "internalType": "uint256"
+            },
+            {
+              "name": "expiryTick",
+              "type": "uint64",
+              "internalType": "uint64"
+            },
+            {
+              "name": "accepted",
+              "type": "bool",
+              "internalType": "bool"
+            },
+            {
+              "name": "cancelled",
+              "type": "bool",
+              "internalType": "bool"
+            }
+          ]
+        }
+      ],
+      "stateMutability": "view"
+    },
+    {
+      "type": "function",
+      "name": "getOtcVaultTransferProposal",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "",
+          "type": "tuple",
+          "internalType": "struct VaultTransferProposal",
+          "components": [
+            {
+              "name": "from",
+              "type": "uint32",
+              "internalType": "uint32"
+            },
+            {
+              "name": "to",
+              "type": "uint32",
+              "internalType": "uint32"
+            },
+            {
+              "name": "wood",
+              "type": "uint256",
+              "internalType": "uint256"
+            },
+            {
+              "name": "wheat",
+              "type": "uint256",
+              "internalType": "uint256"
+            },
+            {
+              "name": "fish",
+              "type": "uint256",
+              "internalType": "uint256"
+            },
+            {
+              "name": "iron",
+              "type": "uint256",
+              "internalType": "uint256"
+            },
+            {
+              "name": "expiryTick",
+              "type": "uint64",
+              "internalType": "uint64"
+            },
+            {
+              "name": "accepted",
+              "type": "bool",
+              "internalType": "bool"
+            },
+            {
+              "name": "cancelled",
+              "type": "bool",
+              "internalType": "bool"
+            }
+          ]
+        }
+      ],
+      "stateMutability": "view"
+    },
+    {
+      "type": "function",
+      "name": "getPool",
+      "inputs": [
+        {
+          "name": "resourceType",
+          "type": "uint8",
+          "internalType": "uint8"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "",
+          "type": "address",
+          "internalType": "address"
+        }
+      ],
+      "stateMutability": "view"
+    },
+    {
+      "type": "function",
+      "name": "getPrice",
+      "inputs": [
+        {
+          "name": "resourceType",
+          "type": "uint8",
+          "internalType": "uint8"
+        },
+        {
+          "name": "amountIn",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "amountOut",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "stateMutability": "view"
+    },
+    {
+      "type": "function",
+      "name": "getRegionPopulation",
+      "inputs": [
+        {
+          "name": "region",
+          "type": "uint8",
+          "internalType": "uint8"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "",
+          "type": "tuple[]",
+          "internalType": "struct RegionOccupant[]",
+          "components": [
+            {
+              "name": "clansmanId",
+              "type": "uint32",
+              "internalType": "uint32"
+            },
+            {
+              "name": "clanId",
+              "type": "uint32",
+              "internalType": "uint32"
+            },
+            {
+              "name": "state",
+              "type": "uint8",
+              "internalType": "enum ClansmanState"
+            },
+            {
+              "name": "currentAction",
+              "type": "uint8",
+              "internalType": "enum ActionType"
+            },
+            {
+              "name": "missionNonce",
+              "type": "uint64",
+              "internalType": "uint64"
+            }
+          ]
+        }
+      ],
+      "stateMutability": "view"
+    },
+    {
+      "type": "function",
+      "name": "getResourceToken",
+      "inputs": [
+        {
+          "name": "resourceType",
+          "type": "uint8",
+          "internalType": "uint8"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "",
+          "type": "address",
+          "internalType": "address"
+        }
+      ],
+      "stateMutability": "view"
+    },
+    {
+      "type": "function",
+      "name": "getScheduledMarketActionsForTick",
+      "inputs": [
+        {
+          "name": "tick",
+          "type": "uint64",
+          "internalType": "uint64"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "",
+          "type": "tuple[]",
+          "internalType": "struct ScheduledMarketAction[]",
           "components": [
             {
               "name": "executeAtTick",
@@ -1751,6 +2137,30 @@
       ],
       "stateMutability": "view"
     },
+    {
+      "type": "function",
+      "name": "getTravelTicks",
+      "inputs": [
+        {
+          "name": "fromRegion",
+          "type": "uint8",
+          "internalType": "uint8"
+        },
+        {
+          "name": "toRegion",
+          "type": "uint8",
+          "internalType": "uint8"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "",
+          "type": "uint64",
+          "internalType": "uint64"
+        }
+      ],
+      "stateMutability": "pure"
+    },
     {
       "type": "function",
       "name": "getTreasuryState",
@@ -1929,6 +2339,16 @@
               "type": "bool",
               "internalType": "bool"
             },
+            {
+              "name": "currentSeasonNumber",
+              "type": "uint64",
+              "internalType": "uint64"
+            },
+            {
+              "name": "nextHeartbeatAtTick",
+              "type": "uint64",
+              "internalType": "uint64"
+            },
             {
               "name": "winterActive",
               "type": "bool",
@@ -2036,6 +2456,16 @@
               "type": "bool",
               "internalType": "bool"
             },
+            {
+              "name": "currentSeasonNumber",
+              "type": "uint64",
+              "internalType": "uint64"
+            },
+            {
+              "name": "nextHeartbeatAtTick",
+              "type": "uint64",
+              "internalType": "uint64"
+            },
             {
               "name": "nextHeartbeatAtTs",
               "type": "uint64",
@@ -2103,32 +2533,208 @@
           "internalType": "address[6]"
         },
         {
-          "name": "pools",
-          "type": "address[4]",
-          "internalType": "address[4]"
-        }
-      ],
-      "outputs": [],
-      "stateMutability": "nonpayable"
-    },
-    {
-      "type": "function",
-      "name": "mintClan",
-      "inputs": [
+          "name": "pools",
+          "type": "address[4]",
+          "internalType": "address[4]"
+        }
+      ],
+      "outputs": [],
+      "stateMutability": "nonpayable"
+    },
+    {
+      "type": "function",
+      "name": "mintClan",
+      "inputs": [
+        {
+          "name": "to",
+          "type": "address",
+          "internalType": "address"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "clanId",
+          "type": "uint32",
+          "internalType": "uint32"
+        },
+        {
+          "name": "iftTokenId",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "stateMutability": "nonpayable"
+    },
+    {
+      "type": "function",
+      "name": "proposeBlueprintTransfer",
+      "inputs": [
+        {
+          "name": "fromClanId",
+          "type": "uint32",
+          "internalType": "uint32"
+        },
+        {
+          "name": "toClanId",
+          "type": "uint32",
+          "internalType": "uint32"
+        },
+        {
+          "name": "amount",
+          "type": "uint256",
+          "internalType": "uint256"
+        },
+        {
+          "name": "expiryTick",
+          "type": "uint64",
+          "internalType": "uint64"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "stateMutability": "nonpayable"
+    },
+    {
+      "type": "function",
+      "name": "proposeBundledTransfer",
+      "inputs": [
+        {
+          "name": "fromClanId",
+          "type": "uint32",
+          "internalType": "uint32"
+        },
+        {
+          "name": "toClanId",
+          "type": "uint32",
+          "internalType": "uint32"
+        },
+        {
+          "name": "gold",
+          "type": "uint256",
+          "internalType": "uint256"
+        },
+        {
+          "name": "wood",
+          "type": "uint256",
+          "internalType": "uint256"
+        },
+        {
+          "name": "wheat",
+          "type": "uint256",
+          "internalType": "uint256"
+        },
+        {
+          "name": "fish",
+          "type": "uint256",
+          "internalType": "uint256"
+        },
+        {
+          "name": "iron",
+          "type": "uint256",
+          "internalType": "uint256"
+        },
+        {
+          "name": "blueprint",
+          "type": "uint256",
+          "internalType": "uint256"
+        },
+        {
+          "name": "expiryTick",
+          "type": "uint64",
+          "internalType": "uint64"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "stateMutability": "nonpayable"
+    },
+    {
+      "type": "function",
+      "name": "proposeGoldTransfer",
+      "inputs": [
+        {
+          "name": "fromClanId",
+          "type": "uint32",
+          "internalType": "uint32"
+        },
+        {
+          "name": "toClanId",
+          "type": "uint32",
+          "internalType": "uint32"
+        },
+        {
+          "name": "amount",
+          "type": "uint256",
+          "internalType": "uint256"
+        },
+        {
+          "name": "expiryTick",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "stateMutability": "nonpayable"
+    },
+    {
+      "type": "function",
+      "name": "proposeVaultTransfer",
+      "inputs": [
+        {
+          "name": "fromClanId",
+          "type": "uint32",
+          "internalType": "uint32"
+        },
+        {
+          "name": "toClanId",
+          "type": "uint32",
+          "internalType": "uint32"
+        },
+        {
+          "name": "woodAmt",
+          "type": "uint256",
+          "internalType": "uint256"
+        },
+        {
+          "name": "wheatAmt",
+          "type": "uint256",
+          "internalType": "uint256"
+        },
+        {
+          "name": "fishAmt",
+          "type": "uint256",
+          "internalType": "uint256"
+        },
+        {
+          "name": "ironAmt",
+          "type": "uint256",
+          "internalType": "uint256"
+        },
         {
-          "name": "to",
-          "type": "address",
-          "internalType": "address"
+          "name": "expiryTick",
+          "type": "uint64",
+          "internalType": "uint64"
         }
       ],
       "outputs": [
         {
-          "name": "clanId",
-          "type": "uint32",
-          "internalType": "uint32"
-        },
-        {
-          "name": "iftTokenId",
+          "name": "proposalId",
           "type": "uint256",
           "internalType": "uint256"
         }
@@ -2270,6 +2876,19 @@
       "outputs": [],
       "stateMutability": "nonpayable"
     },
+    {
+      "type": "function",
+      "name": "settleClansman",
+      "inputs": [
+        {
+          "name": "csId",
+          "type": "uint32",
+          "internalType": "uint32"
+        }
+      ],
+      "outputs": [],
+      "stateMutability": "nonpayable"
+    },
     {
       "type": "function",
       "name": "submitClanOrders",
@@ -2747,6 +3366,93 @@
       ],
       "anonymous": false
     },
+    {
+      "type": "event",
+      "name": "BlueprintTransferAccepted",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "indexed": true,
+          "internalType": "uint256"
+        },
+        {
+          "name": "fromClanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "toClanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "amount",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "settledAtTick",
+          "type": "uint64",
+          "indexed": false,
+          "internalType": "uint64"
+        }
+      ],
+      "anonymous": false
+    },
+    {
+      "type": "event",
+      "name": "BlueprintTransferCancelled",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "indexed": true,
+          "internalType": "uint256"
+        }
+      ],
+      "anonymous": false
+    },
+    {
+      "type": "event",
+      "name": "BlueprintTransferProposed",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "indexed": true,
+          "internalType": "uint256"
+        },
+        {
+          "name": "fromClanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "toClanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "amount",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "expiryTick",
+          "type": "uint64",
+          "indexed": false,
+          "internalType": "uint64"
+        }
+      ],
+      "anonymous": false
+    },
     {
       "type": "event",
       "name": "BlueprintTransferred",
@@ -2758,19 +3464,166 @@
           "internalType": "uint32"
         },
         {
-          "name": "toClanId",
-          "type": "uint32",
-          "indexed": true,
-          "internalType": "uint32"
+          "name": "toClanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "amount",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "atTick",
+          "type": "uint64",
+          "indexed": false,
+          "internalType": "uint64"
+        }
+      ],
+      "anonymous": false
+    },
+    {
+      "type": "event",
+      "name": "BundledTransferAccepted",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "indexed": true,
+          "internalType": "uint256"
+        },
+        {
+          "name": "fromClanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "toClanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "gold",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "wood",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "wheat",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "fish",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "iron",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "blueprint",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "settledAtTick",
+          "type": "uint64",
+          "indexed": false,
+          "internalType": "uint64"
+        }
+      ],
+      "anonymous": false
+    },
+    {
+      "type": "event",
+      "name": "BundledTransferCancelled",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "indexed": true,
+          "internalType": "uint256"
+        }
+      ],
+      "anonymous": false
+    },
+    {
+      "type": "event",
+      "name": "BundledTransferProposed",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "indexed": true,
+          "internalType": "uint256"
+        },
+        {
+          "name": "fromClanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "toClanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "gold",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "wood",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "wheat",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "fish",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "iron",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
         },
         {
-          "name": "amount",
+          "name": "blueprint",
           "type": "uint256",
           "indexed": false,
           "internalType": "uint256"
         },
         {
-          "name": "atTick",
+          "name": "expiryTick",
           "type": "uint64",
           "indexed": false,
           "internalType": "uint64"
@@ -2928,6 +3781,93 @@
       ],
       "anonymous": false
     },
+    {
+      "type": "event",
+      "name": "GoldTransferAccepted",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "indexed": true,
+          "internalType": "uint256"
+        },
+        {
+          "name": "fromClanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "toClanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "amount",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "settledAtTick",
+          "type": "uint64",
+          "indexed": false,
+          "internalType": "uint64"
+        }
+      ],
+      "anonymous": false
+    },
+    {
+      "type": "event",
+      "name": "GoldTransferCancelled",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "indexed": true,
+          "internalType": "uint256"
+        }
+      ],
+      "anonymous": false
+    },
+    {
+      "type": "event",
+      "name": "GoldTransferProposed",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "indexed": true,
+          "internalType": "uint256"
+        },
+        {
+          "name": "fromClanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "toClanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "amount",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "expiryTick",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        }
+      ],
+      "anonymous": false
+    },
     {
       "type": "event",
       "name": "GoldTransferred",
@@ -3267,6 +4207,56 @@
       ],
       "anonymous": false
     },
+    {
+      "type": "event",
+      "name": "ResourceBurned",
+      "inputs": [
+        {
+          "name": "resourceType",
+          "type": "uint8",
+          "indexed": true,
+          "internalType": "uint8"
+        },
+        {
+          "name": "from",
+          "type": "address",
+          "indexed": true,
+          "internalType": "address"
+        },
+        {
+          "name": "amount",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        }
+      ],
+      "anonymous": false
+    },
+    {
+      "type": "event",
+      "name": "ResourceMinted",
+      "inputs": [
+        {
+          "name": "resourceType",
+          "type": "uint8",
+          "indexed": true,
+          "internalType": "uint8"
+        },
+        {
+          "name": "to",
+          "type": "address",
+          "indexed": true,
+          "internalType": "address"
+        },
+        {
+          "name": "amount",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        }
+      ],
+      "anonymous": false
+    },
     {
       "type": "event",
       "name": "ResourcesDeposited",
@@ -3284,25 +4274,25 @@
           "internalType": "uint32"
         },
         {
-          "name": "wood",
+          "name": "woodDelta",
           "type": "uint256",
           "indexed": false,
           "internalType": "uint256"
         },
         {
-          "name": "iron",
+          "name": "ironDelta",
           "type": "uint256",
           "indexed": false,
           "internalType": "uint256"
         },
         {
-          "name": "wheat",
+          "name": "wheatDelta",
           "type": "uint256",
           "indexed": false,
           "internalType": "uint256"
         },
         {
-          "name": "fish",
+          "name": "fishDelta",
           "type": "uint256",
           "indexed": false,
           "internalType": "uint256"
@@ -3568,6 +4558,129 @@
       ],
       "anonymous": false
     },
+    {
+      "type": "event",
+      "name": "VaultTransferAccepted",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "indexed": true,
+          "internalType": "uint256"
+        },
+        {
+          "name": "fromClanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "toClanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "wood",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "wheat",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "fish",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "iron",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "settledAtTick",
+          "type": "uint64",
+          "indexed": false,
+          "internalType": "uint64"
+        }
+      ],
+      "anonymous": false
+    },
+    {
+      "type": "event",
+      "name": "VaultTransferCancelled",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "indexed": true,
+          "internalType": "uint256"
+        }
+      ],
+      "anonymous": false
+    },
+    {
+      "type": "event",
+      "name": "VaultTransferProposed",
+      "inputs": [
+        {
+          "name": "proposalId",
+          "type": "uint256",
+          "indexed": true,
+          "internalType": "uint256"
+        },
+        {
+          "name": "fromClanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "toClanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "wood",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "wheat",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "fish",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "iron",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "expiryTick",
+          "type": "uint64",
+          "indexed": false,
+          "internalType": "uint64"
+        }
+      ],
+      "anonymous": false
+    },
     {
       "type": "event",
       "name": "WallLevelChanged",
diff --git a/packages/contracts/script/Deploy.s.sol b/packages/contracts/script/Deploy.s.sol
index 0219458..e5cd0ca 100644
--- a/packages/contracts/script/Deploy.s.sol
+++ b/packages/contracts/script/Deploy.s.sol
@@ -5,20 +5,21 @@ import {Script, console} from "forge-std/Script.sol";
 import {MinimalERC20} from "../src/MinimalERC20.sol";
 import {StubPool} from "../src/StubPool.sol";
 import {ClanWorld} from "../src/ClanWorld.sol";
-import {PoolSeedConfig} from "../src/IClanWorld.sol";
+import {PoolSeedConfig, ResourceType} from "../src/IClanWorld.sol";
 
 contract Deploy is Script {
     function run() external {
         uint256 deployerPrivateKey = vm.envUint("DEPLOYER_PRIVATE_KEY");
+        address treasury = vm.addr(deployerPrivateKey);
 
         vm.startBroadcast(deployerPrivateKey);
 
-        // 1. Deploy 6 resource tokens (still needed for treasury/pool references)
-        MinimalERC20 wood = new MinimalERC20("ClanWorld Wood", "WOOD");
-        MinimalERC20 iron = new MinimalERC20("ClanWorld Iron", "IRON");
-        MinimalERC20 wheat = new MinimalERC20("ClanWorld Wheat", "WHEAT");
-        MinimalERC20 fish = new MinimalERC20("ClanWorld Fish", "FISH");
-        MinimalERC20 gold = new MinimalERC20("ClanWorld Gold", "GOLD");
+        // 1. Deploy boundary tokens (gold existed in Phase 2 and is reused here).
+        MinimalERC20 wood = new MinimalERC20("Wood", "WOOD");
+        MinimalERC20 iron = new MinimalERC20("Iron", "IRON");
+        MinimalERC20 wheat = new MinimalERC20("Wheat", "WHEAT");
+        MinimalERC20 fish = new MinimalERC20("Fish", "FISH");
+        MinimalERC20 gold = new MinimalERC20("Gold", "GOLD");
         MinimalERC20 blueprint = new MinimalERC20("ClanWorld Blueprint", "BPRT");
 
         console.log("wood:     ", address(wood));
@@ -28,11 +29,16 @@ contract Deploy is Script {
         console.log("gold:     ", address(gold));
         console.log("blueprint:", address(blueprint));
 
-        // 3. Deploy ClanWorld first (needed as engine arg for pools)
+        // 2. Deploy ClanWorld first (needed as engine arg for pools).
         ClanWorld game = new ClanWorld();
         console.log("CLAN_WORLD_CONTRACT_ADDRESS:", address(game));
 
-        // 2. Deploy 4 AMM pools (Phase 2: real constant-product pools)
+        wood.configureBoundary(uint8(ResourceType.Wood), address(game));
+        iron.configureBoundary(uint8(ResourceType.Iron), address(game));
+        wheat.configureBoundary(uint8(ResourceType.Wheat), address(game));
+        fish.configureBoundary(uint8(ResourceType.Fish), address(game));
+
+        // 3. Deploy 4 AMM pools (Phase 6.2: constant-product pools).
         StubPool woodGold = new StubPool(address(wood), address(gold), address(game));
         StubPool wheatGold = new StubPool(address(wheat), address(gold), address(game));
         StubPool fishGold = new StubPool(address(fish), address(gold), address(game));
@@ -49,8 +55,22 @@ contract Deploy is Script {
 
         game.initTreasury(tokens, pools);
 
-        uint256 resSeed = 1000e18;
-        uint256 goldSeed = 1000e18;
+        uint256 resSeed = game.INITIAL_RESOURCE_POOL_SEED();
+        uint256 goldSeed = game.INITIAL_GOLD_POOL_SEED();
+        uint256 totalGoldSeed = goldSeed * 4;
+
+        wood.seedTreasury(treasury, resSeed);
+        wheat.seedTreasury(treasury, resSeed);
+        fish.seedTreasury(treasury, resSeed);
+        iron.seedTreasury(treasury, resSeed);
+        gold.seedTreasury(treasury, totalGoldSeed);
+
+        wood.approve(address(game), resSeed);
+        wheat.approve(address(game), resSeed);
+        fish.approve(address(game), resSeed);
+        iron.approve(address(game), resSeed);
+        gold.approve(address(game), totalGoldSeed);
+
         game.seedPools(
             PoolSeedConfig({
                 woodSeed: resSeed,
diff --git a/packages/contracts/src/ClanWorld.sol b/packages/contracts/src/ClanWorld.sol
index 945490b..cf30204 100644
--- a/packages/contracts/src/ClanWorld.sol
+++ b/packages/contracts/src/ClanWorld.sol
@@ -21,6 +21,10 @@ import {
     Mission,
     BanditTroop,
     ScheduledMarketAction,
+    OtcProposal,
+    VaultTransferProposal,
+    BlueprintTransferProposal,
+    BundledTransferProposal,
     DefenseContribution,
     PackedRoute,
     DerivedClanState,
@@ -37,6 +41,7 @@ import {
     ActiveBanditView,
     RegionOccupant
 } from "./IClanWorld.sol";
+import {MinimalERC20} from "./MinimalERC20.sol";
 import {StubPool} from "./StubPool.sol";
 import {ReentrancyGuard} from "./util/ReentrancyGuard.sol";
 
@@ -62,9 +67,15 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     mapping(uint8 => mapping(uint32 => uint256)) private _defenderCountByRegionClan; // region => clanId => clansmen count
     mapping(uint32 => uint8) private _clansmanDefendingRegion; // clansmanId => defended home region
     mapping(uint64 => bytes32) private _tickSeeds; // tick => seed
+    mapping(uint256 => OtcProposal) private _otcGoldProposals;
+    mapping(uint256 => VaultTransferProposal) private _otcVaultTransferProposals;
+    mapping(uint256 => BlueprintTransferProposal) private _otcBlueprintTransferProposals;
+    mapping(uint256 => BundledTransferProposal) private _otcBundledTransferProposals;
+    mapping(uint32 => uint256) private _openOtcProposalsByClan;
 
     uint32 private _nextClanId;
     uint32 private _nextClansmanId;
+    uint256 private _nextOtcProposalId;
     uint32[] private _allClanIds;
 
     // per-clan clansman list: clanId => clansmanId[]
@@ -74,9 +85,13 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     // CONSTANTS — Wheat harvest rate (not in IClanWorld constants)
     // =========================================================================
 
+    uint64 private constant DEPOSIT_DURATION_TICKS = 1;
     uint256 private constant WHEAT_HARVEST_RATE = 20e18;
+    uint256 public constant INITIAL_RESOURCE_POOL_SEED = 100_000e18;
+    uint256 public constant INITIAL_GOLD_POOL_SEED = 50_000e18;
     /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
     uint256 public constant MAX_MARKET_ACTIONS_PER_TICK = 32;
+    uint256 public constant MAX_OPEN_OTC_PROPOSALS_PER_CLAN = 8;
 
     // =========================================================================
     // CONSTRUCTOR
@@ -91,13 +106,13 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         _world.nextHeartbeatAtTick = 1; // first heartbeat will open tick 1
         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
         // i.e. ticks [100, 110)
-        _world.winterStartsAtTick =
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
         _world.winterActive = false;
         _treasury.treasuryOwner = msg.sender;
         _nextClanId = 1;
         _nextClansmanId = 1;
+        _nextOtcProposalId = 1;
     }
 
     // =========================================================================
@@ -429,7 +444,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
     }
 
-    /// @dev Resolve one tick of action for a clansman that is in ACTING state.
+    /// @dev Resolve an action for a clansman that is in ACTING state.
     function _resolveAction(
         Clan storage clan,
         Clansman storage cs,
@@ -484,28 +499,27 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         bool starving,
         bytes32 tickSeed
     ) internal {
-        uint256 remaining = ClanWorldConstants.WOOD_CAP - cs.carryWood;
-        if (remaining == 0) {
+        if (cs.carryWood >= ClanWorldConstants.CLANSMAN_CARRY_CAP) {
             _completeMission(cs, m);
             return;
         }
-        uint256 yield = ClanWorldConstants.WOOD_BASE_YIELD;
-        // Crit roll: domain-separated RNG
-        bytes32 critRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
-        uint256 critRoll = uint256(critRng) % 10000;
-        if (critRoll < ClanWorldConstants.WOOD_CRIT_BPS) {
-            yield += ClanWorldConstants.WOOD_CRIT_BONUS;
+
+        uint256 remaining = ClanWorldConstants.CLANSMAN_CARRY_CAP - cs.carryWood;
+        uint256 yield = ClanWorldConstants.WOOD_YIELD_PER_TICK * uint256(getActionDuration(ActionType.ChopWood));
+        bytes32 woodRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
+        if (uint256(woodRng) % 10000 < ClanWorldConstants.WOOD_CRIT_BPS) {
+            yield *= 2;
         }
+
         if (starving) yield = yield / 2;
         if (yield > remaining) yield = remaining;
         cs.carryWood += yield;
 
         emit ResourcesGathered(clanId, cs.clansmanId, ActionType.ChopWood, yield, 0, 0, 0, 0, tick);
 
-        if (cs.carryWood >= ClanWorldConstants.WOOD_CAP) {
+        if (cs.carryWood >= ClanWorldConstants.CLANSMAN_CARRY_CAP) {
             _completeMission(cs, m);
         }
-        // else continuous — worker stays ACTING
     }
 
     function _gatherIron(
@@ -675,26 +689,27 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         }
         bool hasAnything = cs.carryWood > 0 || cs.carryIron > 0 || cs.carryWheat > 0 || cs.carryFish > 0;
         if (!hasAnything) {
+            // Empty deposits are silent no-ops; no zero-delta event for indexers to process.
             _completeMission(cs, m);
             return;
         }
 
-        uint256 w = cs.carryWood;
-        uint256 ir = cs.carryIron;
-        uint256 wh = cs.carryWheat;
-        uint256 fi = cs.carryFish;
+        uint256 woodDelta = cs.carryWood;
+        uint256 ironDelta = cs.carryIron;
+        uint256 wheatDelta = cs.carryWheat;
+        uint256 fishDelta = cs.carryFish;
 
-        clan.vaultWood += w;
-        clan.vaultIron += ir;
-        clan.vaultWheat += wh;
-        clan.vaultFish += fi;
+        clan.vaultWood += woodDelta;
+        clan.vaultIron += ironDelta;
+        clan.vaultWheat += wheatDelta;
+        clan.vaultFish += fishDelta;
 
         cs.carryWood = 0;
         cs.carryIron = 0;
         cs.carryWheat = 0;
         cs.carryFish = 0;
 
-        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
+        emit ResourcesDeposited(clanId, cs.clansmanId, woodDelta, ironDelta, wheatDelta, fishDelta, tick);
         _completeMission(cs, m);
     }
 
@@ -1610,7 +1625,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
 
         // DepositResources: must go to homebase
         if (action == ActionType.DepositResources) {
-            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
+            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
         }
 
         // BuildWall / UpgradeBase / UpgradeMonument: must go to homebase
@@ -1720,6 +1735,15 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         require(!_treasury.poolsSeeded, "ClanWorld: pools already seeded");
         require(_treasury.woodToken != address(0), "ClanWorld: treasury not init");
 
+        _transferSeed(_treasury.woodToken, _treasury.woodGoldPool, cfg.woodSeed);
+        _transferSeed(_treasury.goldToken, _treasury.woodGoldPool, cfg.goldSeedForWood);
+        _transferSeed(_treasury.wheatToken, _treasury.wheatGoldPool, cfg.wheatSeed);
+        _transferSeed(_treasury.goldToken, _treasury.wheatGoldPool, cfg.goldSeedForWheat);
+        _transferSeed(_treasury.fishToken, _treasury.fishGoldPool, cfg.fishSeed);
+        _transferSeed(_treasury.goldToken, _treasury.fishGoldPool, cfg.goldSeedForFish);
+        _transferSeed(_treasury.ironToken, _treasury.ironGoldPool, cfg.ironSeed);
+        _transferSeed(_treasury.goldToken, _treasury.ironGoldPool, cfg.goldSeedForIron);
+
         StubPool(_treasury.woodGoldPool).seed(cfg.woodSeed, cfg.goldSeedForWood);
         StubPool(_treasury.wheatGoldPool).seed(cfg.wheatSeed, cfg.goldSeedForWheat);
         StubPool(_treasury.fishGoldPool).seed(cfg.fishSeed, cfg.goldSeedForFish);
@@ -1732,10 +1756,389 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         );
     }
 
+    function _transferSeed(address token, address pool, uint256 amount) internal {
+        require(token != address(0) && pool != address(0), "ClanWorld: treasury not init");
+        require(MinimalERC20(token).transferFrom(msg.sender, pool, amount), "ClanWorld: seed transfer failed");
+    }
+
     // =========================================================================
     // OTC TRANSFERS
     // =========================================================================
 
+    function proposeGoldTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint256 expiryTick)
+        external
+        override
+        nonReentrant
+        returns (uint256 proposalId)
+    {
+        Clan storage fromClan = _clans[fromClanId];
+        require(fromClan.clanId != 0, "ClanWorld: clan not found");
+        require(fromClanId != toClanId, "ERR_SELF_TRANSFER");
+        require(amount > 0, "ERR_ZERO_AMOUNT");
+        require(_openOtcProposalsByClan[fromClanId] < MAX_OPEN_OTC_PROPOSALS_PER_CLAN, "ERR_OTC_CAP");
+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
+        require(_clans[toClanId].clanId != 0, "ClanWorld: target clan not found");
+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(expiryTick <= type(uint64).max, "ClanWorld: expiry overflow");
+
+        proposalId = _nextOtcProposalId++;
+        _openOtcProposalsByClan[fromClanId]++;
+        _otcGoldProposals[proposalId] = OtcProposal({
+            from: fromClanId,
+            to: toClanId,
+            amount: amount,
+            expiryTick: uint64(expiryTick),
+            accepted: false,
+            cancelled: false
+        });
+
+        emit GoldTransferProposed(proposalId, fromClanId, toClanId, amount, expiryTick);
+    }
+
+    function acceptGoldTransfer(uint256 proposalId) external override nonReentrant {
+        OtcProposal storage proposal = _otcGoldProposals[proposalId];
+        require(proposal.from != 0, "ClanWorld: proposal not found");
+        require(!proposal.accepted, "ClanWorld: proposal accepted");
+        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
+        require(_world.currentTick <= proposal.expiryTick, "ClanWorld: proposal expired");
+
+        uint32 fromClanId = proposal.from;
+        uint32 toClanId = proposal.to;
+        uint256 amount = proposal.amount;
+        _settleClan(fromClanId);
+        _settleClan(toClanId);
+
+        Clan storage fromClan = _clans[proposal.from];
+        Clan storage toClan = _clans[proposal.to];
+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(toClan.owner == msg.sender, "ClanWorld: not clan owner");
+        if (fromClan.goldBalance < amount) revert("ERR_NOT_ENOUGH_GOLD");
+
+        fromClan.goldBalance -= amount;
+        toClan.goldBalance += amount;
+        _closeOtcProposal(fromClanId);
+        delete _otcGoldProposals[proposalId];
+
+        emit GoldTransferAccepted(proposalId, fromClanId, toClanId, amount, _world.currentTick);
+    }
+
+    function cancelGoldTransfer(uint256 proposalId) external override nonReentrant {
+        OtcProposal storage proposal = _otcGoldProposals[proposalId];
+        require(proposal.from != 0, "ClanWorld: proposal not found");
+        require(!proposal.accepted, "ClanWorld: proposal accepted");
+        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
+
+        Clan storage fromClan = _clans[proposal.from];
+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
+
+        uint32 fromClanId = proposal.from;
+        _closeOtcProposal(fromClanId);
+        delete _otcGoldProposals[proposalId];
+        emit GoldTransferCancelled(proposalId);
+    }
+
+    function proposeVaultTransfer(
+        uint32 fromClanId,
+        uint32 toClanId,
+        uint256 woodAmt,
+        uint256 wheatAmt,
+        uint256 fishAmt,
+        uint256 ironAmt,
+        uint64 expiryTick
+    ) external override nonReentrant returns (uint256 proposalId) {
+        Clan storage fromClan = _clans[fromClanId];
+        require(fromClan.clanId != 0, "ClanWorld: clan not found");
+        require(fromClanId != toClanId, "ERR_SELF_TRANSFER");
+        require(woodAmt > 0 || wheatAmt > 0 || fishAmt > 0 || ironAmt > 0, "ERR_ZERO_AMOUNT");
+        require(_openOtcProposalsByClan[fromClanId] < MAX_OPEN_OTC_PROPOSALS_PER_CLAN, "ERR_OTC_CAP");
+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
+        require(_clans[toClanId].clanId != 0, "ClanWorld: target clan not found");
+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+
+        proposalId = _nextOtcProposalId++;
+        _openOtcProposalsByClan[fromClanId]++;
+        _otcVaultTransferProposals[proposalId] = VaultTransferProposal({
+            from: fromClanId,
+            to: toClanId,
+            wood: woodAmt,
+            wheat: wheatAmt,
+            fish: fishAmt,
+            iron: ironAmt,
+            expiryTick: expiryTick,
+            accepted: false,
+            cancelled: false
+        });
+
+        emit VaultTransferProposed(proposalId, fromClanId, toClanId, woodAmt, wheatAmt, fishAmt, ironAmt, expiryTick);
+    }
+
+    function acceptVaultTransfer(uint256 proposalId) external override nonReentrant {
+        VaultTransferProposal storage proposal = _otcVaultTransferProposals[proposalId];
+        require(proposal.from != 0, "ClanWorld: proposal not found");
+        require(!proposal.accepted, "ClanWorld: proposal accepted");
+        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
+        require(_world.currentTick <= proposal.expiryTick, "ClanWorld: proposal expired");
+
+        uint32 fromClanId = proposal.from;
+        uint32 toClanId = proposal.to;
+        uint256 wood = proposal.wood;
+        uint256 wheat = proposal.wheat;
+        uint256 fish = proposal.fish;
+        uint256 iron = proposal.iron;
+        _settleClan(fromClanId);
+        _settleClan(toClanId);
+
+        Clan storage fromClan = _clans[fromClanId];
+        Clan storage toClan = _clans[toClanId];
+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(toClan.owner == msg.sender, "ClanWorld: not clan owner");
+        if (!_hasVaultResources(fromClan, wood, wheat, fish, iron)) {
+            revert("ERR_NOT_ENOUGH_RESOURCES");
+        }
+
+        fromClan.vaultWood -= wood;
+        fromClan.vaultWheat -= wheat;
+        fromClan.vaultFish -= fish;
+        fromClan.vaultIron -= iron;
+        toClan.vaultWood += wood;
+        toClan.vaultWheat += wheat;
+        toClan.vaultFish += fish;
+        toClan.vaultIron += iron;
+        _closeOtcProposal(fromClanId);
+        delete _otcVaultTransferProposals[proposalId];
+
+        emit VaultTransferAccepted(proposalId, fromClanId, toClanId, wood, wheat, fish, iron, _world.currentTick);
+    }
+
+    function cancelVaultTransfer(uint256 proposalId) external override nonReentrant {
+        VaultTransferProposal storage proposal = _otcVaultTransferProposals[proposalId];
+        require(proposal.from != 0, "ClanWorld: proposal not found");
+        require(!proposal.accepted, "ClanWorld: proposal accepted");
+        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
+
+        Clan storage fromClan = _clans[proposal.from];
+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
+
+        uint32 fromClanId = proposal.from;
+        _closeOtcProposal(fromClanId);
+        delete _otcVaultTransferProposals[proposalId];
+        emit VaultTransferCancelled(proposalId);
+    }
+
+    function _hasVaultResources(Clan storage clan, uint256 wood, uint256 wheat, uint256 fish, uint256 iron)
+        private
+        view
+        returns (bool)
+    {
+        return clan.vaultWood >= wood && clan.vaultWheat >= wheat && clan.vaultFish >= fish && clan.vaultIron >= iron;
+    }
+
+    function proposeBlueprintTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint64 expiryTick)
+        external
+        override
+        nonReentrant
+        returns (uint256 proposalId)
+    {
+        Clan storage fromClan = _clans[fromClanId];
+        require(fromClan.clanId != 0, "ClanWorld: clan not found");
+        require(fromClanId != toClanId, "ERR_SELF_TRANSFER");
+        require(amount > 0, "ERR_ZERO_AMOUNT");
+        require(_openOtcProposalsByClan[fromClanId] < MAX_OPEN_OTC_PROPOSALS_PER_CLAN, "ERR_OTC_CAP");
+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
+        require(_clans[toClanId].clanId != 0, "ClanWorld: target clan not found");
+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+
+        proposalId = _nextOtcProposalId++;
+        _openOtcProposalsByClan[fromClanId]++;
+        _otcBlueprintTransferProposals[proposalId] = BlueprintTransferProposal({
+            from: fromClanId, to: toClanId, amount: amount, expiryTick: expiryTick, accepted: false, cancelled: false
+        });
+
+        emit BlueprintTransferProposed(proposalId, fromClanId, toClanId, amount, expiryTick);
+    }
+
+    function acceptBlueprintTransfer(uint256 proposalId) external override nonReentrant {
+        BlueprintTransferProposal storage proposal = _otcBlueprintTransferProposals[proposalId];
+        require(proposal.from != 0, "ClanWorld: proposal not found");
+        require(!proposal.accepted, "ClanWorld: proposal accepted");
+        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
+        require(_world.currentTick <= proposal.expiryTick, "ClanWorld: proposal expired");
+
+        uint32 fromClanId = proposal.from;
+        uint32 toClanId = proposal.to;
+        uint256 amount = proposal.amount;
+        _settleClan(fromClanId);
+        _settleClan(toClanId);
+
+        Clan storage fromClan = _clans[fromClanId];
+        Clan storage toClan = _clans[toClanId];
+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(toClan.owner == msg.sender, "ClanWorld: not clan owner");
+        if (fromClan.blueprintBalance < amount) revert("ERR_NOT_ENOUGH_BLUEPRINT");
+
+        fromClan.blueprintBalance -= amount;
+        toClan.blueprintBalance += amount;
+        _closeOtcProposal(fromClanId);
+        delete _otcBlueprintTransferProposals[proposalId];
+
+        emit BlueprintTransferAccepted(proposalId, fromClanId, toClanId, amount, _world.currentTick);
+    }
+
+    function cancelBlueprintTransfer(uint256 proposalId) external override nonReentrant {
+        BlueprintTransferProposal storage proposal = _otcBlueprintTransferProposals[proposalId];
+        require(proposal.from != 0, "ClanWorld: proposal not found");
+        require(!proposal.accepted, "ClanWorld: proposal accepted");
+        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
+
+        Clan storage fromClan = _clans[proposal.from];
+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
+
+        uint32 fromClanId = proposal.from;
+        _closeOtcProposal(fromClanId);
+        delete _otcBlueprintTransferProposals[proposalId];
+        emit BlueprintTransferCancelled(proposalId);
+    }
+
+    function proposeBundledTransfer(
+        uint32 fromClanId,
+        uint32 toClanId,
+        uint256 gold,
+        uint256 wood,
+        uint256 wheat,
+        uint256 fish,
+        uint256 iron,
+        uint256 blueprint,
+        uint64 expiryTick
+    ) external override nonReentrant returns (uint256 proposalId) {
+        Clan storage fromClan = _clans[fromClanId];
+        require(fromClan.clanId != 0, "ClanWorld: clan not found");
+        require(fromClanId != toClanId, "ERR_SELF_TRANSFER");
+        require(!_isEmptyBundledTransfer(gold, wood, wheat, fish, iron, blueprint), "ERR_ZERO_AMOUNT");
+        require(_openOtcProposalsByClan[fromClanId] < MAX_OPEN_OTC_PROPOSALS_PER_CLAN, "ERR_OTC_CAP");
+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
+        require(_clans[toClanId].clanId != 0, "ClanWorld: target clan not found");
+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+
+        proposalId = _nextOtcProposalId++;
+        _openOtcProposalsByClan[fromClanId]++;
+        _otcBundledTransferProposals[proposalId] = BundledTransferProposal({
+            from: fromClanId,
+            to: toClanId,
+            gold: gold,
+            wood: wood,
+            wheat: wheat,
+            fish: fish,
+            iron: iron,
+            blueprint: blueprint,
+            expiryTick: expiryTick,
+            accepted: false,
+            cancelled: false
+        });
+
+        emit BundledTransferProposed(
+            proposalId, fromClanId, toClanId, gold, wood, wheat, fish, iron, blueprint, expiryTick
+        );
+    }
+
+    function acceptBundledTransfer(uint256 proposalId) external override nonReentrant {
+        BundledTransferProposal storage proposal = _otcBundledTransferProposals[proposalId];
+        require(proposal.from != 0, "ClanWorld: proposal not found");
+        require(!proposal.accepted, "ClanWorld: proposal accepted");
+        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
+        require(_world.currentTick <= proposal.expiryTick, "ClanWorld: proposal expired");
+
+        uint32 fromClanId = proposal.from;
+        uint32 toClanId = proposal.to;
+        uint256 gold = proposal.gold;
+        uint256 wood = proposal.wood;
+        uint256 wheat = proposal.wheat;
+        uint256 fish = proposal.fish;
+        uint256 iron = proposal.iron;
+        uint256 blueprint = proposal.blueprint;
+        _settleClan(fromClanId);
+        _settleClan(toClanId);
+
+        Clan storage fromClan = _clans[fromClanId];
+        Clan storage toClan = _clans[toClanId];
+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(toClan.owner == msg.sender, "ClanWorld: not clan owner");
+        _requireBundledTransferBalance(fromClan, gold, wood, wheat, fish, iron, blueprint);
+
+        fromClan.goldBalance -= gold;
+        fromClan.vaultWood -= wood;
+        fromClan.vaultWheat -= wheat;
+        fromClan.vaultFish -= fish;
+        fromClan.vaultIron -= iron;
+        fromClan.blueprintBalance -= blueprint;
+        toClan.goldBalance += gold;
+        toClan.vaultWood += wood;
+        toClan.vaultWheat += wheat;
+        toClan.vaultFish += fish;
+        toClan.vaultIron += iron;
+        toClan.blueprintBalance += blueprint;
+        _closeOtcProposal(fromClanId);
+        delete _otcBundledTransferProposals[proposalId];
+
+        emit BundledTransferAccepted(
+            proposalId, fromClanId, toClanId, gold, wood, wheat, fish, iron, blueprint, _world.currentTick
+        );
+    }
+
+    function cancelBundledTransfer(uint256 proposalId) external override nonReentrant {
+        BundledTransferProposal storage proposal = _otcBundledTransferProposals[proposalId];
+        require(proposal.from != 0, "ClanWorld: proposal not found");
+        require(!proposal.accepted, "ClanWorld: proposal accepted");
+        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
+
+        Clan storage fromClan = _clans[proposal.from];
+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
+
+        uint32 fromClanId = proposal.from;
+        _closeOtcProposal(fromClanId);
+        delete _otcBundledTransferProposals[proposalId];
+        emit BundledTransferCancelled(proposalId);
+    }
+
+    function _isEmptyBundledTransfer(
+        uint256 gold,
+        uint256 wood,
+        uint256 wheat,
+        uint256 fish,
+        uint256 iron,
+        uint256 blueprint
+    ) private pure returns (bool) {
+        return gold == 0 && wood == 0 && wheat == 0 && fish == 0 && iron == 0 && blueprint == 0;
+    }
+
+    function _requireBundledTransferBalance(
+        Clan storage clan,
+        uint256 gold,
+        uint256 wood,
+        uint256 wheat,
+        uint256 fish,
+        uint256 iron,
+        uint256 blueprint
+    ) private view {
+        if (clan.goldBalance < gold) revert("ERR_NOT_ENOUGH_GOLD");
+        if (!_hasVaultResources(clan, wood, wheat, fish, iron)) revert("ERR_NOT_ENOUGH_RESOURCES");
+        if (clan.blueprintBalance < blueprint) revert("ERR_NOT_ENOUGH_BLUEPRINT");
+    }
+
+    function _closeOtcProposal(uint32 fromClanId) private {
+        uint256 openCount = _openOtcProposalsByClan[fromClanId];
+        if (openCount > 0) {
+            _openOtcProposalsByClan[fromClanId] = openCount - 1;
+        }
+    }
+
     function transferGold(uint32, uint32, uint256) external pure override {
         revert("OTC transfers not implemented");
     }
@@ -1768,6 +2171,30 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         return _treasury;
     }
 
+    function getResourceToken(uint8 resourceType) external view override returns (address) {
+        if (resourceType == uint8(ResourceType.Wood)) return _treasury.woodToken;
+        if (resourceType == uint8(ResourceType.Iron)) return _treasury.ironToken;
+        if (resourceType == uint8(ResourceType.Wheat)) return _treasury.wheatToken;
+        if (resourceType == uint8(ResourceType.Fish)) return _treasury.fishToken;
+        revert("ClanWorld: invalid resource");
+    }
+
+    function getPool(uint8 resourceType) external view override returns (address) {
+        return _poolForResourceType(resourceType);
+    }
+
+    function getPrice(uint8 resourceType, uint256 amountIn) external view override returns (uint256 amountOut) {
+        amountOut = StubPool(_poolForResourceType(resourceType)).getAmountOutForExactIn(amountIn);
+    }
+
+    function _poolForResourceType(uint8 resourceType) internal view returns (address) {
+        if (resourceType == uint8(ResourceType.Wood)) return _treasury.woodGoldPool;
+        if (resourceType == uint8(ResourceType.Iron)) return _treasury.ironGoldPool;
+        if (resourceType == uint8(ResourceType.Wheat)) return _treasury.wheatGoldPool;
+        if (resourceType == uint8(ResourceType.Fish)) return _treasury.fishGoldPool;
+        revert("ClanWorld: invalid resource");
+    }
+
     function getClan(uint32 clanId) external view override returns (Clan memory) {
         return _clans[clanId];
     }
@@ -1801,10 +2228,13 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             return 4;
         }
 
+        if (action == ActionType.DepositResources) {
+            return DEPOSIT_DURATION_TICKS;
+        }
+
         if (
-            action == ActionType.DepositResources || action == ActionType.BuildWall || action == ActionType.UpgradeBase
-                || action == ActionType.UpgradeMonument || action == ActionType.MarketBuy
-                || action == ActionType.MarketSell
+            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
+                || action == ActionType.MarketBuy || action == ActionType.MarketSell
         ) {
             return 1;
         }
@@ -1852,6 +2282,37 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         return _scheduledMarketActions[tick];
     }
 
+    function getOtcGoldProposal(uint256 proposalId) external view override returns (OtcProposal memory) {
+        return _otcGoldProposals[proposalId];
+    }
+
+    function getOtcVaultTransferProposal(uint256 proposalId)
+        external
+        view
+        override
+        returns (VaultTransferProposal memory)
+    {
+        return _otcVaultTransferProposals[proposalId];
+    }
+
+    function getOtcBlueprintTransferProposal(uint256 proposalId)
+        external
+        view
+        override
+        returns (BlueprintTransferProposal memory)
+    {
+        return _otcBlueprintTransferProposals[proposalId];
+    }
+
+    function getOtcBundledTransferProposal(uint256 proposalId)
+        external
+        view
+        override
+        returns (BundledTransferProposal memory)
+    {
+        return _otcBundledTransferProposals[proposalId];
+    }
+
     function getActiveDefenders(uint32 targetClanId) external view override returns (uint32[] memory clansmanIds) {
         uint32[] storage defendingClans = _defendingClansByRegion[_clans[targetClanId].baseRegion];
         uint256 count = 0;
diff --git a/packages/contracts/src/ClanWorldStub.sol b/packages/contracts/src/ClanWorldStub.sol
index 36ce29b..426272e 100644
--- a/packages/contracts/src/ClanWorldStub.sol
+++ b/packages/contracts/src/ClanWorldStub.sol
@@ -21,6 +21,10 @@ import {
     Mission,
     BanditTroop,
     ScheduledMarketAction,
+    OtcProposal,
+    VaultTransferProposal,
+    BlueprintTransferProposal,
+    BundledTransferProposal,
     DefenseContribution,
     PackedRoute,
     DerivedClanState,
@@ -51,8 +55,7 @@ contract ClanWorldStub is IClanWorld {
         _world.seasonEndTick = ClanWorldConstants.SEASON_DURATION_TICKS;
         _world.currentSeasonNumber = 1;
         _world.nextHeartbeatAtTick = _world.currentTick + 1;
-        _world.winterStartsAtTick =
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
         _world.winterActive = false;
 
@@ -117,6 +120,48 @@ contract ClanWorldStub is IClanWorld {
     // OTC transfers
     // -------------------------------------------------------------------------
 
+    function proposeGoldTransfer(uint32, uint32, uint256, uint256) external pure override returns (uint256) {
+        return 1;
+    }
+
+    function acceptGoldTransfer(uint256) external override {}
+
+    function cancelGoldTransfer(uint256) external override {}
+
+    function proposeVaultTransfer(uint32, uint32, uint256, uint256, uint256, uint256, uint64)
+        external
+        pure
+        override
+        returns (uint256)
+    {
+        return 1;
+    }
+
+    function acceptVaultTransfer(uint256) external override {}
+
+    function cancelVaultTransfer(uint256) external override {}
+
+    function proposeBlueprintTransfer(uint32, uint32, uint256, uint64) external pure override returns (uint256) {
+        return 1;
+    }
+
+    function acceptBlueprintTransfer(uint256) external override {}
+
+    function cancelBlueprintTransfer(uint256) external override {}
+
+    function proposeBundledTransfer(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256, uint64)
+        external
+        pure
+        override
+        returns (uint256)
+    {
+        return 1;
+    }
+
+    function acceptBundledTransfer(uint256) external override {}
+
+    function cancelBundledTransfer(uint256) external override {}
+
     function transferGold(uint32, uint32, uint256) external override {}
 
     function transferVaultResource(uint32, uint32, ResourceType, uint256) external override {}
@@ -137,6 +182,26 @@ contract ClanWorldStub is IClanWorld {
         return _treasury;
     }
 
+    function getResourceToken(uint8 resourceType) external view override returns (address) {
+        if (resourceType == uint8(ResourceType.Wood)) return _treasury.woodToken;
+        if (resourceType == uint8(ResourceType.Iron)) return _treasury.ironToken;
+        if (resourceType == uint8(ResourceType.Wheat)) return _treasury.wheatToken;
+        if (resourceType == uint8(ResourceType.Fish)) return _treasury.fishToken;
+        revert("ClanWorldStub: invalid resource");
+    }
+
+    function getPool(uint8 resourceType) external view override returns (address) {
+        if (resourceType == uint8(ResourceType.Wood)) return _treasury.woodGoldPool;
+        if (resourceType == uint8(ResourceType.Iron)) return _treasury.ironGoldPool;
+        if (resourceType == uint8(ResourceType.Wheat)) return _treasury.wheatGoldPool;
+        if (resourceType == uint8(ResourceType.Fish)) return _treasury.fishGoldPool;
+        revert("ClanWorldStub: invalid resource");
+    }
+
+    function getPrice(uint8, uint256) external pure override returns (uint256) {
+        return 0;
+    }
+
     function getClan(uint32) external pure override returns (Clan memory) {
         return Clan({
             clanId: 0,
@@ -241,6 +306,41 @@ contract ClanWorldStub is IClanWorld {
         return new ScheduledMarketAction[](0);
     }
 
+    function getOtcGoldProposal(uint256) external pure override returns (OtcProposal memory) {
+        return OtcProposal({from: 0, to: 0, amount: 0, expiryTick: 0, accepted: false, cancelled: false});
+    }
+
+    function getOtcVaultTransferProposal(uint256) external pure override returns (VaultTransferProposal memory) {
+        return VaultTransferProposal({
+            from: 0, to: 0, wood: 0, wheat: 0, fish: 0, iron: 0, expiryTick: 0, accepted: false, cancelled: false
+        });
+    }
+
+    function getOtcBlueprintTransferProposal(uint256)
+        external
+        pure
+        override
+        returns (BlueprintTransferProposal memory)
+    {
+        return BlueprintTransferProposal({from: 0, to: 0, amount: 0, expiryTick: 0, accepted: false, cancelled: false});
+    }
+
+    function getOtcBundledTransferProposal(uint256) external pure override returns (BundledTransferProposal memory) {
+        return BundledTransferProposal({
+            from: 0,
+            to: 0,
+            gold: 0,
+            wood: 0,
+            wheat: 0,
+            fish: 0,
+            iron: 0,
+            blueprint: 0,
+            expiryTick: 0,
+            accepted: false,
+            cancelled: false
+        });
+    }
+
     function getActiveDefenders(uint32) external pure override returns (uint32[] memory) {
         return new uint32[](0);
     }
diff --git a/packages/contracts/src/IClanWorld.sol b/packages/contracts/src/IClanWorld.sol
index 2b80fbe..d1894a4 100644
--- a/packages/contracts/src/IClanWorld.sol
+++ b/packages/contracts/src/IClanWorld.sol
@@ -40,15 +40,17 @@ library ClanWorldConstants {
     uint64 internal constant CLANSMAN_COOLDOWN_SECONDS = 60;
 
     // Carry caps (per clansman)
-    uint256 internal constant WOOD_CAP = 15e18;
+    uint256 internal constant CLANSMAN_CARRY_CAP = 10e18;
+    uint256 internal constant WOOD_CAP = CLANSMAN_CARRY_CAP;
     uint256 internal constant IRON_CAP = 5e18;
     uint256 internal constant WHEAT_CAP = 40e18;
     uint256 internal constant FISH_CAP = 8e18;
 
     // Gathering yields
-    uint256 internal constant WOOD_BASE_YIELD = 2e18;
-    uint256 internal constant WOOD_CRIT_BONUS = 1e18;
-    uint16 internal constant WOOD_CRIT_BPS = 2000; // 20%
+    uint256 internal constant WOOD_YIELD_PER_TICK = 1e18;
+    uint256 internal constant WOOD_BASE_YIELD = WOOD_YIELD_PER_TICK;
+    uint256 internal constant WOOD_CRIT_BONUS = WOOD_YIELD_PER_TICK;
+    uint16 internal constant WOOD_CRIT_BPS = 1000; // 10%
 
     uint256 internal constant IRON_BASE_YIELD = 5e17; // 0.5e18
     uint16 internal constant GOLD_FROM_IRON_BPS = 200; // 2%
@@ -176,7 +178,10 @@ enum StatusCode {
     ERR_NO_ACTIVE_BANDIT,
     ERR_SEASON_ENDED,
     ERR_NOT_ENOUGH_GOLD,
-    ERR_CARRY_FULL
+    ERR_CARRY_FULL,
+    ERR_ZERO_AMOUNT,
+    ERR_SELF_TRANSFER,
+    ERR_OTC_CAP
 }
 
 // =============================================================================
@@ -188,8 +193,8 @@ struct WorldState {
     uint64 seasonStartTick;
     uint64 seasonEndTick;
     bool seasonFinalized;
-    uint64 currentSeasonNumber;   // 1-indexed; incremented each time seasonEndTick is crossed
-    uint64 nextHeartbeatAtTick;   // estimated tick that will be opened by the next heartbeat (for off-chain UI)
+    uint64 currentSeasonNumber; // 1-indexed; incremented each time seasonEndTick is crossed
+    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)
 
     uint64 nextHeartbeatAtTs;
     uint64 nextBanditSpawnEligibleTick;
@@ -328,6 +333,50 @@ struct ScheduledMarketAction {
     uint256 maxGoldIn; // buy only, 0 otherwise
 }
 
+struct OtcProposal {
+    uint32 from;
+    uint32 to;
+    uint256 amount;
+    uint64 expiryTick;
+    bool accepted;
+    bool cancelled;
+}
+
+struct VaultTransferProposal {
+    uint32 from;
+    uint32 to;
+    uint256 wood;
+    uint256 wheat;
+    uint256 fish;
+    uint256 iron;
+    uint64 expiryTick;
+    bool accepted;
+    bool cancelled;
+}
+
+struct BlueprintTransferProposal {
+    uint32 from;
+    uint32 to;
+    uint256 amount;
+    uint64 expiryTick;
+    bool accepted;
+    bool cancelled;
+}
+
+struct BundledTransferProposal {
+    uint32 from;
+    uint32 to;
+    uint256 gold;
+    uint256 wood;
+    uint256 wheat;
+    uint256 fish;
+    uint256 iron;
+    uint256 blueprint;
+    uint64 expiryTick;
+    bool accepted;
+    bool cancelled;
+}
+
 struct DefenseContribution {
     uint32 clansmanId;
     uint32 clanId;
@@ -533,10 +582,10 @@ interface IClanWorldEvents {
     event ResourcesDeposited(
         uint32 indexed clanId,
         uint32 indexed clansmanId,
-        uint256 wood,
-        uint256 iron,
-        uint256 wheat,
-        uint256 fish,
+        uint256 woodDelta,
+        uint256 ironDelta,
+        uint256 wheatDelta,
+        uint256 fishDelta,
         uint64 atTick
     );
 
@@ -576,6 +625,8 @@ interface IClanWorldEvents {
         uint256 maxGoldIn
     );
     event MarketActionFailed(uint32 indexed clanId, uint32 indexed clansmanId, ActionType action, StatusCode reason);
+    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
+    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);
 
     // ----- bandits -----
     event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
@@ -614,6 +665,82 @@ interface IClanWorldEvents {
     event ClansmanDiedFromCold(uint32 indexed clanId, uint64 atTick);
 
     // ----- OTC transfers -----
+    event GoldTransferProposed(
+        uint256 indexed proposalId,
+        uint32 indexed fromClanId,
+        uint32 indexed toClanId,
+        uint256 amount,
+        uint256 expiryTick
+    );
+    event GoldTransferAccepted(
+        uint256 indexed proposalId,
+        uint32 indexed fromClanId,
+        uint32 indexed toClanId,
+        uint256 amount,
+        uint64 settledAtTick
+    );
+    event GoldTransferCancelled(uint256 indexed proposalId);
+    event VaultTransferProposed(
+        uint256 indexed proposalId,
+        uint32 indexed fromClanId,
+        uint32 indexed toClanId,
+        uint256 wood,
+        uint256 wheat,
+        uint256 fish,
+        uint256 iron,
+        uint64 expiryTick
+    );
+    event VaultTransferAccepted(
+        uint256 indexed proposalId,
+        uint32 indexed fromClanId,
+        uint32 indexed toClanId,
+        uint256 wood,
+        uint256 wheat,
+        uint256 fish,
+        uint256 iron,
+        uint64 settledAtTick
+    );
+    event VaultTransferCancelled(uint256 indexed proposalId);
+    event BlueprintTransferProposed(
+        uint256 indexed proposalId,
+        uint32 indexed fromClanId,
+        uint32 indexed toClanId,
+        uint256 amount,
+        uint64 expiryTick
+    );
+    event BlueprintTransferAccepted(
+        uint256 indexed proposalId,
+        uint32 indexed fromClanId,
+        uint32 indexed toClanId,
+        uint256 amount,
+        uint64 settledAtTick
+    );
+    event BlueprintTransferCancelled(uint256 indexed proposalId);
+    event BundledTransferProposed(
+        uint256 indexed proposalId,
+        uint32 indexed fromClanId,
+        uint32 indexed toClanId,
+        uint256 gold,
+        uint256 wood,
+        uint256 wheat,
+        uint256 fish,
+        uint256 iron,
+        uint256 blueprint,
+        uint64 expiryTick
+    );
+    event BundledTransferAccepted(
+        uint256 indexed proposalId,
+        uint32 indexed fromClanId,
+        uint32 indexed toClanId,
+        uint256 gold,
+        uint256 wood,
+        uint256 wheat,
+        uint256 fish,
+        uint256 iron,
+        uint256 blueprint,
+        uint64 settledAtTick
+    );
+    event BundledTransferCancelled(uint256 indexed proposalId);
     event GoldTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
     event VaultResourceTransferred(
         uint32 indexed fromClanId, uint32 indexed toClanId, ResourceType resource, uint256 amount, uint64 atTick
@@ -674,6 +801,52 @@ interface IClanWorld is IClanWorldEvents {
     // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
     // -------------------------------------------------------------------------
 
+    function proposeGoldTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint256 expiryTick)
+        external
+        returns (uint256 proposalId);
+
+    function acceptGoldTransfer(uint256 proposalId) external;
+
+    function cancelGoldTransfer(uint256 proposalId) external;
+
+    function proposeVaultTransfer(
+        uint32 fromClanId,
+        uint32 toClanId,
+        uint256 woodAmt,
+        uint256 wheatAmt,
+        uint256 fishAmt,
+        uint256 ironAmt,
+        uint64 expiryTick
+    ) external returns (uint256 proposalId);
+
+    function acceptVaultTransfer(uint256 proposalId) external;
+
+    function cancelVaultTransfer(uint256 proposalId) external;
+
+    function proposeBlueprintTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint64 expiryTick)
+        external
+        returns (uint256 proposalId);
+
+    function acceptBlueprintTransfer(uint256 proposalId) external;
+
+    function cancelBlueprintTransfer(uint256 proposalId) external;
+
+    function proposeBundledTransfer(
+        uint32 fromClanId,
+        uint32 toClanId,
+        uint256 gold,
+        uint256 wood,
+        uint256 wheat,
+        uint256 fish,
+        uint256 iron,
+        uint256 blueprint,
+        uint64 expiryTick
+    ) external returns (uint256 proposalId);
+
+    function acceptBundledTransfer(uint256 proposalId) external;
+
+    function cancelBundledTransfer(uint256 proposalId) external;
+
     function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
 
     function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
@@ -699,6 +872,12 @@ interface IClanWorld is IClanWorldEvents {
 
     function getTreasuryState() external view returns (TreasuryState memory);
 
+    function getResourceToken(uint8 resourceType) external view returns (address);
+
+    function getPool(uint8 resourceType) external view returns (address);
+
+    function getPrice(uint8 resourceType, uint256 amountIn) external view returns (uint256 amountOut);
+
     function getClan(uint32 clanId) external view returns (Clan memory);
 
     function getClansman(uint32 clansmanId) external view returns (Clansman memory);
@@ -720,6 +899,17 @@ interface IClanWorld is IClanWorldEvents {
 
     function getScheduledMarketActionsForTick(uint64 tick) external view returns (ScheduledMarketAction[] memory);
 
+    function getOtcGoldProposal(uint256 proposalId) external view returns (OtcProposal memory);
+
+    function getOtcVaultTransferProposal(uint256 proposalId) external view returns (VaultTransferProposal memory);
+
+    function getOtcBlueprintTransferProposal(uint256 proposalId)
+        external
+        view
+        returns (BlueprintTransferProposal memory);
+
+    function getOtcBundledTransferProposal(uint256 proposalId) external view returns (BundledTransferProposal memory);
+
     function getActiveDefenders(uint32 targetClanId) external view returns (uint32[] memory clansmanIds);
 
     function getDefendingClans(uint8 region) external view returns (uint32[] memory clanIds);
diff --git a/packages/contracts/src/MinimalERC20.sol b/packages/contracts/src/MinimalERC20.sol
index b9ac0b8..fd704ce 100644
--- a/packages/contracts/src/MinimalERC20.sol
+++ b/packages/contracts/src/MinimalERC20.sol
@@ -1,39 +1,87 @@
 // SPDX-License-Identifier: MIT
 pragma solidity ^0.8.34;
 
-/// @notice Minimal ERC20 with owner-only mint. No external deps.
+/// @notice Minimal ERC20 boundary token. No external deps.
 contract MinimalERC20 {
     string public name;
     string public symbol;
     uint8 public constant decimals = 18;
 
     uint256 public totalSupply;
-    address public immutable OWNER;
+    address public immutable DEPLOYER;
+    address public engine;
+    uint8 public resourceType;
+    bool public boundaryConfigured;
+    bool public treasurySeeded;
 
     mapping(address => uint256) public balanceOf;
     mapping(address => mapping(address => uint256)) public allowance;
 
     event Transfer(address indexed from, address indexed to, uint256 value);
     event Approval(address indexed owner_, address indexed spender, uint256 value);
+    event BoundaryConfigured(uint8 indexed resourceType, address indexed engine);
+    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
+    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);
 
     constructor(string memory name_, string memory symbol_) {
         name = name_;
         symbol = symbol_;
-        OWNER = msg.sender;
+        DEPLOYER = msg.sender;
+        resourceType = type(uint8).max;
     }
 
     /// @dev Wrapped per forge-lint unwrapped-modifier-logic — keeps modifier
     /// body slim so the require isn't duplicated at every call site.
-    function _onlyOwner() internal view {
-        require(msg.sender == OWNER, "not owner");
+    function _onlyDeployer() internal view {
+        require(msg.sender == DEPLOYER, "MinimalERC20: not deployer");
     }
 
-    modifier onlyOwner() {
-        _onlyOwner();
+    function _onlyEngine() internal view {
+        require(boundaryConfigured && msg.sender == engine, "MinimalERC20: not engine");
+    }
+
+    modifier onlyDeployer() {
+        _onlyDeployer();
+        _;
+    }
+
+    modifier onlyEngine() {
+        _onlyEngine();
         _;
     }
 
-    function mint(address to, uint256 amount) external onlyOwner {
+    function configureBoundary(uint8 resourceType_, address engine_) external onlyDeployer {
+        require(!boundaryConfigured, "MinimalERC20: boundary configured");
+        require(engine_ != address(0), "MinimalERC20: zero engine");
+
+        resourceType = resourceType_;
+        engine = engine_;
+        boundaryConfigured = true;
+
+        emit BoundaryConfigured(resourceType_, engine_);
+    }
+
+    function seedTreasury(address treasury, uint256 amount) external onlyDeployer {
+        require(!treasurySeeded, "MinimalERC20: treasury seeded");
+        require(treasury != address(0), "MinimalERC20: zero treasury");
+
+        treasurySeeded = true;
+        _mint(treasury, amount);
+    }
+
+    function mint(address to, uint256 amount) external onlyEngine {
+        _mint(to, amount);
+        emit ResourceMinted(resourceType, to, amount);
+    }
+
+    function burn(address from, uint256 amount) external onlyEngine {
+        balanceOf[from] -= amount;
+        totalSupply -= amount;
+        emit Transfer(from, address(0), amount);
+        emit ResourceBurned(resourceType, from, amount);
+    }
+
+    function _mint(address to, uint256 amount) internal {
         totalSupply += amount;
         balanceOf[to] += amount;
         emit Transfer(address(0), to, amount);
diff --git a/packages/contracts/src/StubPool.sol b/packages/contracts/src/StubPool.sol
index 15fc86d..d35b4f4 100644
--- a/packages/contracts/src/StubPool.sol
+++ b/packages/contracts/src/StubPool.sol
@@ -1,18 +1,21 @@
 // SPDX-License-Identifier: MIT
 pragma solidity ^0.8.34;
 
+interface IERC20Balance {
+    function balanceOf(address account) external view returns (uint256);
+}
+
 /// @notice Constant-product AMM pool for one resource/gold pair.
-///         Reserves are tracked internally; ClanWorld calls the math
-///         functions to compute exchange rates, then mints/credits balances
-///         directly via the internal accounting model.
-///         No real ERC20 transfers occur here — the pool is the math oracle.
+///         TOKEN_A is the resource token and TOKEN_B is gold. ClanWorld owns
+///         the swap surface because clans use the engine's internal accounting,
+///         while the treasury seeds real ERC20 balances into the pool once.
 contract StubPool {
-    address public immutable TOKEN_A;  // resource token
-    address public immutable TOKEN_B;  // gold token
-    address public immutable ENGINE;   // ClanWorld address
+    address public immutable TOKEN_A; // resource token
+    address public immutable TOKEN_B; // gold token
+    address public immutable ENGINE; // ClanWorld address
 
-    uint256 public reserveA;          // resource reserve
-    uint256 public reserveB;          // gold reserve
+    uint256 public reserveA; // resource reserve
+    uint256 public reserveB; // gold reserve
 
     bool private _seeded;
 
@@ -32,45 +35,80 @@ contract StubPool {
     function seed(uint256 amountA, uint256 amountB) external onlyEngine {
         require(!_seeded, "StubPool: already seeded");
         require(amountA > 0 && amountB > 0, "StubPool: zero seed");
+        require(IERC20Balance(TOKEN_A).balanceOf(address(this)) >= amountA, "StubPool: missing token A");
+        require(IERC20Balance(TOKEN_B).balanceOf(address(this)) >= amountB, "StubPool: missing token B");
         reserveA = amountA;
         reserveB = amountB;
         _seeded = true;
     }
 
-    /// @notice Exact-input sell: clan sells amountIn of resource, gets goldOut.
-    ///         Constant-product: goldOut = reserveB * amountIn / (reserveA + amountIn).
-    ///         Updates reserves. Called by ClanWorld; no token transfers.
-    function sellResource(uint256 amountIn) external onlyEngine returns (uint256 goldOut) {
+    function getAmountOutForExactIn(uint256 amountIn) public view returns (uint256 amountOut) {
         require(amountIn > 0, "StubPool: zero amount");
         require(reserveA > 0 && reserveB > 0, "StubPool: not seeded");
-        goldOut = (reserveB * amountIn) / (reserveA + amountIn);
-        require(goldOut > 0, "StubPool: zero output");
-        reserveA += amountIn;
-        reserveB -= goldOut;
+        amountOut = (reserveB * amountIn) / (reserveA + amountIn);
     }
 
-    /// @notice Quote a buy without updating reserves (view).
-    ///         goldIn = ceil(reserveB * amountOut / (reserveA - amountOut)).
-    function quoteBuy(uint256 amountOut) external view returns (uint256 goldIn) {
+    function getAmountInForExactOut(uint256 amountOut) public view returns (uint256 amountIn) {
         require(amountOut > 0, "StubPool: zero amount");
         require(amountOut < reserveA, "StubPool: insufficient resource reserve");
-        uint256 num = reserveB * amountOut;
-        uint256 denom_ = reserveA - amountOut;
-        goldIn = num / denom_ + (num % denom_ == 0 ? 0 : 1);
+        require(reserveB > 0, "StubPool: not seeded");
+
+        uint256 numerator = reserveB * amountOut;
+        uint256 denominator = reserveA - amountOut;
+        amountIn = numerator / denominator + (numerator % denominator == 0 ? 0 : 1);
+    }
+
+    /// @notice Exact-input sell: resource in, gold out.
+    function swapExactInForOut(uint256 amountIn, uint256 minOut) external onlyEngine returns (uint256 amountOut) {
+        return _swapExactInForOut(amountIn, minOut);
+    }
+
+    function _swapExactInForOut(uint256 amountIn, uint256 minOut) internal returns (uint256 amountOut) {
+        uint256 priorK = reserveA * reserveB;
+        amountOut = getAmountOutForExactIn(amountIn);
+        require(amountOut >= minOut, "StubPool: insufficient output");
+
+        reserveA += amountIn;
+        reserveB -= amountOut;
+
+        require(reserveA * reserveB >= priorK, "StubPool: k invariant");
+    }
+
+    /// @notice Exact-output buy: gold in, exact resource out.
+    function swapExactOutForInWithMaxIn(uint256 amountOut, uint256 maxIn)
+        external
+        onlyEngine
+        returns (uint256 amountIn)
+    {
+        return _swapExactOutForInWithMaxIn(amountOut, maxIn);
+    }
+
+    function _swapExactOutForInWithMaxIn(uint256 amountOut, uint256 maxIn) internal returns (uint256 amountIn) {
+        uint256 priorK = reserveA * reserveB;
+        amountIn = getAmountInForExactOut(amountOut);
+        require(amountIn <= maxIn, "StubPool: max input exceeded");
+
+        reserveA -= amountOut;
+        reserveB += amountIn;
+
+        require(reserveA * reserveB >= priorK, "StubPool: k invariant");
+    }
+
+    /// @notice Exact-input sell: clan sells amountIn of resource, gets goldOut.
+    ///         Legacy alias for Phase 6.1 scheduled-market code.
+    function sellResource(uint256 amountIn) external onlyEngine returns (uint256 goldOut) {
+        goldOut = _swapExactInForOut(amountIn, 1);
+    }
+
+    /// @notice Legacy quote alias for exact-output resource buys.
+    function quoteBuy(uint256 amountOut) external view returns (uint256 goldIn) {
+        goldIn = getAmountInForExactOut(amountOut);
     }
 
     /// @notice Exact-output buy: clan buys amountOut of resource, pays goldIn.
-    ///         Returns actual goldIn charged (ceiling arithmetic).
-    ///         Updates reserves. Called by ClanWorld; no token transfers.
+    ///         Legacy alias for Phase 6.1 scheduled-market code.
     function buyResource(uint256 amountOut) external onlyEngine returns (uint256 goldIn) {
-        require(amountOut > 0, "StubPool: zero amount");
-        require(amountOut < reserveA, "StubPool: insufficient resource reserve");
-        require(reserveB > 0, "StubPool: not seeded");
-        uint256 num = reserveB * amountOut;
-        uint256 denom_ = reserveA - amountOut;
-        goldIn = num / denom_ + (num % denom_ == 0 ? 0 : 1);
-        reserveA -= amountOut;
-        reserveB += goldIn;
+        goldIn = _swapExactOutForInWithMaxIn(amountOut, type(uint256).max);
     }
 
     function getReserves() external view returns (uint256, uint256) {
diff --git a/packages/contracts/test/BlueprintTransferOtc.t.sol b/packages/contracts/test/BlueprintTransferOtc.t.sol
new file mode 100644
index 0000000..268724e
--- /dev/null
+++ b/packages/contracts/test/BlueprintTransferOtc.t.sol
@@ -0,0 +1,165 @@
+// SPDX-License-Identifier: MIT
+pragma solidity ^0.8.34;
+
+import {Test} from "forge-std/Test.sol";
+import {ClanWorld} from "../src/ClanWorld.sol";
+import {BlueprintTransferProposal, ClanWorldConstants, Clan, ClanState} from "../src/IClanWorld.sol";
+
+contract BlueprintTransferHarness is ClanWorld {
+    function setBlueprintBalance(uint32 clanId, uint256 amount) external {
+        _clans[clanId].blueprintBalance = amount;
+    }
+}
+
+contract BlueprintTransferOtcTest is Test {
+    event BlueprintTransferProposed(
+        uint256 indexed proposalId,
+        uint32 indexed fromClanId,
+        uint32 indexed toClanId,
+        uint256 amount,
+        uint64 expiryTick
+    );
+    event BlueprintTransferAccepted(
+        uint256 indexed proposalId,
+        uint32 indexed fromClanId,
+        uint32 indexed toClanId,
+        uint256 amount,
+        uint64 settledAtTick
+    );
+    event BlueprintTransferCancelled(uint256 indexed proposalId);
+
+    BlueprintTransferHarness world;
+    address elderA = address(0xA1);
+    address elderB = address(0xA2);
+    address elderC = address(0xA3);
+
+    function setUp() public {
+        world = new BlueprintTransferHarness();
+    }
+
+    function test_proposeAndAcceptBlueprintTransfer_transfersAtomically() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        world.setBlueprintBalance(clanA, 10e18);
+        world.setBlueprintBalance(clanB, 2e18);
+        uint256 amount = 3e18;
+
+        vm.expectEmit(true, true, true, true, address(world));
+        emit BlueprintTransferProposed(1, clanA, clanB, amount, 10);
+        uint256 proposalId = _propose(clanA, clanB, amount, 10);
+
+        BlueprintTransferProposal memory proposal = world.getOtcBlueprintTransferProposal(proposalId);
+        assertEq(proposal.from, clanA, "proposal from");
+        assertEq(proposal.to, clanB, "proposal to");
+        assertEq(proposal.amount, amount, "proposal amount");
+        assertEq(proposal.expiryTick, 10, "proposal expiry");
+        assertFalse(proposal.accepted, "proposal not accepted");
+        assertFalse(proposal.cancelled, "proposal not cancelled");
+
+        vm.expectEmit(true, true, true, true, address(world));
+        emit BlueprintTransferAccepted(proposalId, clanA, clanB, amount, world.getWorldState().currentTick);
+        vm.prank(elderB);
+        world.acceptBlueprintTransfer(proposalId);
+
+        assertEq(world.getClan(clanA).blueprintBalance, 7e18, "from debited");
+        assertEq(world.getClan(clanB).blueprintBalance, 5e18, "to credited");
+        assertEq(world.getOtcBlueprintTransferProposal(proposalId).from, 0, "proposal deleted");
+    }
+
+    function test_acceptBlueprintTransfer_revertsWhenBalanceChangedAfterProposal() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        world.setBlueprintBalance(clanA, 3e18);
+        world.setBlueprintBalance(clanB, 1e18);
+        uint256 proposalId = _propose(clanA, clanB, 3e18, 10);
+        world.setBlueprintBalance(clanA, 2e18);
+
+        vm.expectRevert("ERR_NOT_ENOUGH_BLUEPRINT");
+        vm.prank(elderB);
+        world.acceptBlueprintTransfer(proposalId);
+
+        Clan memory fromAfter = world.getClan(clanA);
+        Clan memory toAfter = world.getClan(clanB);
+        assertEq(fromAfter.blueprintBalance, 2e18, "from unchanged");
+        assertEq(toAfter.blueprintBalance, 1e18, "to unchanged");
+    }
+
+    function test_acceptBlueprintTransfer_revertsWhenExpired() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        world.setBlueprintBalance(clanA, 3e18);
+        uint256 proposalId = _propose(clanA, clanB, 1e18, world.getWorldState().currentTick);
+        _advanceTick();
+
+        vm.expectRevert("ClanWorld: proposal expired");
+        vm.prank(elderB);
+        world.acceptBlueprintTransfer(proposalId);
+    }
+
+    function test_cancelBlueprintTransfer_byProposerBlocksAccept() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        world.setBlueprintBalance(clanA, 3e18);
+        uint256 proposalId = _propose(clanA, clanB, 1e18, 10);
+
+        vm.expectEmit(true, false, false, true, address(world));
+        emit BlueprintTransferCancelled(proposalId);
+        vm.prank(elderA);
+        world.cancelBlueprintTransfer(proposalId);
+
+        assertEq(world.getOtcBlueprintTransferProposal(proposalId).from, 0, "proposal deleted");
+        vm.expectRevert("ClanWorld: proposal not found");
+        vm.prank(elderB);
+        world.acceptBlueprintTransfer(proposalId);
+    }
+
+    function test_blueprintTransfer_twoClanNoInterference() public {
+        (uint32 clanA, uint32 clanB, uint32 clanC) = _mintThreeClans();
+        world.setBlueprintBalance(clanA, 10e18);
+        world.setBlueprintBalance(clanB, 4e18);
+        world.setBlueprintBalance(clanC, 1e18);
+
+        uint256 proposalId = _propose(clanA, clanC, 6e18, 10);
+        vm.prank(elderC);
+        world.acceptBlueprintTransfer(proposalId);
+
+        assertEq(world.getClan(clanB).blueprintBalance, 4e18, "unrelated clan unchanged");
+        assertEq(world.getClan(clanC).blueprintBalance, 7e18, "target clan credited");
+    }
+
+    function test_proposeBlueprintTransfer_revertsWhenZeroAmount() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+
+        vm.expectRevert("ERR_ZERO_AMOUNT");
+        _propose(clanA, clanB, 0, 10);
+    }
+
+    function test_proposeBlueprintTransfer_revertsWhenSelfTransfer() public {
+        (uint32 clanA,,) = _mintThreeClans();
+
+        vm.expectRevert("ERR_SELF_TRANSFER");
+        _propose(clanA, clanA, 1e18, 10);
+    }
+
+    function _mintThreeClans() internal returns (uint32 clanA, uint32 clanB, uint32 clanC) {
+        vm.prank(elderA);
+        (clanA,) = world.mintClan(elderA);
+        vm.prank(elderB);
+        (clanB,) = world.mintClan(elderB);
+        vm.prank(elderC);
+        (clanC,) = world.mintClan(elderC);
+
+        assertEq(uint8(world.getClan(clanA).clanState), uint8(ClanState.ACTIVE), "clan A alive");
+        assertEq(uint8(world.getClan(clanB).clanState), uint8(ClanState.ACTIVE), "clan B alive");
+        assertEq(uint8(world.getClan(clanC).clanState), uint8(ClanState.ACTIVE), "clan C alive");
+    }
+
+    function _propose(uint32 fromClanId, uint32 toClanId, uint256 amount, uint64 expiryTick)
+        internal
+        returns (uint256 proposalId)
+    {
+        vm.prank(elderA);
+        proposalId = world.proposeBlueprintTransfer(fromClanId, toClanId, amount, expiryTick);
+    }
+
+    function _advanceTick() internal {
+        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
+        world.heartbeat();
+    }
+}
diff --git a/packages/contracts/test/BundledTransferOtc.t.sol b/packages/contracts/test/BundledTransferOtc.t.sol
new file mode 100644
index 0000000..7a93de3
--- /dev/null
+++ b/packages/contracts/test/BundledTransferOtc.t.sol
@@ -0,0 +1,235 @@
+// SPDX-License-Identifier: MIT
+pragma solidity ^0.8.34;
+
+import {Test} from "forge-std/Test.sol";
+import {ClanWorld} from "../src/ClanWorld.sol";
+import {BundledTransferProposal, ClanWorldConstants, Clan, ClanState} from "../src/IClanWorld.sol";
+
+contract BundledTransferHarness is ClanWorld {
+    function setBalances(
+        uint32 clanId,
+        uint256 gold,
+        uint256 wood,
+        uint256 wheat,
+        uint256 fish,
+        uint256 iron,
+        uint256 blueprint
+    ) external {
+        _clans[clanId].goldBalance = gold;
+        _clans[clanId].vaultWood = wood;
+        _clans[clanId].vaultWheat = wheat;
+        _clans[clanId].vaultFish = fish;
+        _clans[clanId].vaultIron = iron;
+        _clans[clanId].blueprintBalance = blueprint;
+    }
+}
+
+contract BundledTransferOtcTest is Test {
+    event BundledTransferProposed(
+        uint256 indexed proposalId,
+        uint32 indexed fromClanId,
+        uint32 indexed toClanId,
+        uint256 gold,
+        uint256 wood,
+        uint256 wheat,
+        uint256 fish,
+        uint256 iron,
+        uint256 blueprint,
+        uint64 expiryTick
+    );
+    event BundledTransferAccepted(
+        uint256 indexed proposalId,
+        uint32 indexed fromClanId,
+        uint32 indexed toClanId,
+        uint256 gold,
+        uint256 wood,
+        uint256 wheat,
+        uint256 fish,
+        uint256 iron,
+        uint256 blueprint,
+        uint64 settledAtTick
+    );
+    event BundledTransferCancelled(uint256 indexed proposalId);
+
+    BundledTransferHarness world;
+    address elderA = address(0xA1);
+    address elderB = address(0xA2);
+    address elderC = address(0xA3);
+
+    function setUp() public {
+        world = new BundledTransferHarness();
+    }
+
+    function test_proposeAndAcceptBundledTransfer_transfersAllComponentsAtomically() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        world.setBalances(clanA, 100e18, 80e18, 70e18, 60e18, 50e18, 40e18);
+        world.setBalances(clanB, 10e18, 11e18, 12e18, 13e18, 14e18, 15e18);
+
+        vm.expectEmit(true, true, true, true, address(world));
+        emit BundledTransferProposed(1, clanA, clanB, 9e18, 8e18, 7e18, 6e18, 5e18, 4e18, 10);
+        uint256 proposalId = _propose(clanA, clanB, 9e18, 8e18, 7e18, 6e18, 5e18, 4e18, 10);
+
+        BundledTransferProposal memory proposal = world.getOtcBundledTransferProposal(proposalId);
+        assertEq(proposal.from, clanA, "proposal from");
+        assertEq(proposal.to, clanB, "proposal to");
+        assertEq(proposal.gold, 9e18, "proposal gold");
+        assertEq(proposal.wood, 8e18, "proposal wood");
+        assertEq(proposal.wheat, 7e18, "proposal wheat");
+        assertEq(proposal.fish, 6e18, "proposal fish");
+        assertEq(proposal.iron, 5e18, "proposal iron");
+        assertEq(proposal.blueprint, 4e18, "proposal blueprint");
+        assertEq(proposal.expiryTick, 10, "proposal expiry");
+        assertFalse(proposal.accepted, "proposal not accepted");
+        assertFalse(proposal.cancelled, "proposal not cancelled");
+
+        vm.expectEmit(true, true, true, true, address(world));
+        emit BundledTransferAccepted(
+            proposalId, clanA, clanB, 9e18, 8e18, 7e18, 6e18, 5e18, 4e18, world.getWorldState().currentTick
+        );
+        vm.prank(elderB);
+        world.acceptBundledTransfer(proposalId);
+
+        _assertBalances(clanA, 91e18, 72e18, 63e18, 54e18, 45e18, 36e18, "from debited");
+        _assertBalances(clanB, 19e18, 19e18, 19e18, 19e18, 19e18, 19e18, "to credited");
+        assertEq(world.getOtcBundledTransferProposal(proposalId).from, 0, "proposal deleted");
+    }
+
+    function test_acceptBundledTransfer_revertsAndLeavesAllComponentsWhenGoldInsufficient() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        world.setBalances(clanA, 10e18, 80e18, 70e18, 60e18, 50e18, 40e18);
+        world.setBalances(clanB, 1e18, 2e18, 3e18, 4e18, 5e18, 6e18);
+        uint256 proposalId = _propose(clanA, clanB, 10e18, 8e18, 7e18, 6e18, 5e18, 4e18, 10);
+        world.setBalances(clanA, 9e18, 80e18, 70e18, 60e18, 50e18, 40e18);
+
+        vm.expectRevert("ERR_NOT_ENOUGH_GOLD");
+        vm.prank(elderB);
+        world.acceptBundledTransfer(proposalId);
+
+        _assertBalances(clanA, 9e18, 80e18, 70e18, 60e18, 50e18, 40e18, "from unchanged");
+        _assertBalances(clanB, 1e18, 2e18, 3e18, 4e18, 5e18, 6e18, "to unchanged");
+    }
+
+    function test_acceptBundledTransfer_revertsAndLeavesAllComponentsWhenOneResourceInsufficient() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        world.setBalances(clanA, 100e18, 80e18, 70e18, 60e18, 5e18, 40e18);
+        world.setBalances(clanB, 1e18, 2e18, 3e18, 4e18, 5e18, 6e18);
+        uint256 proposalId = _propose(clanA, clanB, 10e18, 8e18, 7e18, 6e18, 5e18, 4e18, 10);
+        world.setBalances(clanA, 100e18, 80e18, 70e18, 60e18, 4e18, 40e18);
+
+        vm.expectRevert("ERR_NOT_ENOUGH_RESOURCES");
+        vm.prank(elderB);
+        world.acceptBundledTransfer(proposalId);
+
+        _assertBalances(clanA, 100e18, 80e18, 70e18, 60e18, 4e18, 40e18, "from unchanged");
+        _assertBalances(clanB, 1e18, 2e18, 3e18, 4e18, 5e18, 6e18, "to unchanged");
+    }
+
+    function test_acceptBundledTransfer_revertsWhenExpired() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        world.setBalances(clanA, 100e18, 80e18, 70e18, 60e18, 50e18, 40e18);
+        uint256 proposalId =
+            _propose(clanA, clanB, 1e18, 1e18, 1e18, 1e18, 1e18, 1e18, world.getWorldState().currentTick);
+        _advanceTick();
+
+        vm.expectRevert("ClanWorld: proposal expired");
+        vm.prank(elderB);
+        world.acceptBundledTransfer(proposalId);
+    }
+
+    function test_cancelBundledTransfer_byProposerBlocksAccept() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        world.setBalances(clanA, 100e18, 80e18, 70e18, 60e18, 50e18, 40e18);
+        uint256 proposalId = _propose(clanA, clanB, 1e18, 1e18, 1e18, 1e18, 1e18, 1e18, 10);
+
+        vm.expectEmit(true, false, false, true, address(world));
+        emit BundledTransferCancelled(proposalId);
+        vm.prank(elderA);
+        world.cancelBundledTransfer(proposalId);
+
+        assertEq(world.getOtcBundledTransferProposal(proposalId).from, 0, "proposal deleted");
+        vm.expectRevert("ClanWorld: proposal not found");
+        vm.prank(elderB);
+        world.acceptBundledTransfer(proposalId);
+    }
+
+    function test_proposeBundledTransfer_revertsWhenEmpty() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+
+        vm.expectRevert("ERR_ZERO_AMOUNT");
+        _propose(clanA, clanB, 0, 0, 0, 0, 0, 0, 10);
+    }
+
+    function test_proposeBundledTransfer_revertsWhenSelfTransfer() public {
+        (uint32 clanA,,) = _mintThreeClans();
+
+        vm.expectRevert("ERR_SELF_TRANSFER");
+        _propose(clanA, clanA, 1e18, 0, 0, 0, 0, 0, 10);
+    }
+
+    function test_bundledTransfer_twoClanNoInterference() public {
+        (uint32 clanA, uint32 clanB, uint32 clanC) = _mintThreeClans();
+        world.setBalances(clanA, 100e18, 80e18, 70e18, 60e18, 50e18, 40e18);
+        world.setBalances(clanB, 1e18, 2e18, 3e18, 4e18, 5e18, 6e18);
+        world.setBalances(clanC, 10e18, 20e18, 30e18, 40e18, 50e18, 60e18);
+
+        uint256 proposalId = _propose(clanA, clanC, 9e18, 8e18, 7e18, 6e18, 5e18, 4e18, 10);
+        vm.prank(elderC);
+        world.acceptBundledTransfer(proposalId);
+
+        _assertBalances(clanB, 1e18, 2e18, 3e18, 4e18, 5e18, 6e18, "unrelated unchanged");
+        _assertBalances(clanC, 19e18, 28e18, 37e18, 46e18, 55e18, 64e18, "target credited");
+    }
+
+    function _mintThreeClans() internal returns (uint32 clanA, uint32 clanB, uint32 clanC) {
+        vm.prank(elderA);
+        (clanA,) = world.mintClan(elderA);
+        vm.prank(elderB);
+        (clanB,) = world.mintClan(elderB);
+        vm.prank(elderC);
+        (clanC,) = world.mintClan(elderC);
+
+        assertEq(uint8(world.getClan(clanA).clanState), uint8(ClanState.ACTIVE), "clan A alive");
+        assertEq(uint8(world.getClan(clanB).clanState), uint8(ClanState.ACTIVE), "clan B alive");
+        assertEq(uint8(world.getClan(clanC).clanState), uint8(ClanState.ACTIVE), "clan C alive");
+    }
+
+    function _propose(
+        uint32 fromClanId,
+        uint32 toClanId,
+        uint256 gold,
+        uint256 wood,
+        uint256 wheat,
+        uint256 fish,
+        uint256 iron,
+        uint256 blueprint,
+        uint64 expiryTick
+    ) internal returns (uint256 proposalId) {
+        vm.prank(elderA);
+        proposalId =
+            world.proposeBundledTransfer(fromClanId, toClanId, gold, wood, wheat, fish, iron, blueprint, expiryTick);
+    }
+
+    function _assertBalances(
+        uint32 clanId,
+        uint256 gold,
+        uint256 wood,
+        uint256 wheat,
+        uint256 fish,
+        uint256 iron,
+        uint256 blueprint,
+        string memory label
+    ) internal view {
+        Clan memory clan = world.getClan(clanId);
+        assertEq(clan.goldBalance, gold, label);
+        assertEq(clan.vaultWood, wood, label);
+        assertEq(clan.vaultWheat, wheat, label);
+        assertEq(clan.vaultFish, fish, label);
+        assertEq(clan.vaultIron, iron, label);
+        assertEq(clan.blueprintBalance, blueprint, label);
+    }
+
+    function _advanceTick() internal {
+        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
+        world.heartbeat();
+    }
+}
diff --git a/packages/contracts/test/ClanWorld.t.sol b/packages/contracts/test/ClanWorld.t.sol
index 92781d3..4b107ee 100644
--- a/packages/contracts/test/ClanWorld.t.sol
+++ b/packages/contracts/test/ClanWorld.t.sol
@@ -48,6 +48,13 @@ contract ClanWorldTestHarness is ClanWorld {
     function killClansman(uint32 csId) external {
         _clansmen[csId].state = ClansmanState.DEAD;
     }
+
+    function setCarry(uint32 csId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish) external {
+        _clansmen[csId].carryWood = wood;
+        _clansmen[csId].carryIron = iron;
+        _clansmen[csId].carryWheat = wheat;
+        _clansmen[csId].carryFish = fish;
+    }
 }
 
 contract ClanWorldTest is Test {
@@ -98,9 +105,23 @@ contract ClanWorldTest is Test {
 
         world.initTreasury(tokens, pools);
 
-        // Seed: 1000 wood + 1000 gold per pool (spot price 1 gold / 1 wood)
-        uint256 resSeed = 1000e18;
-        uint256 goldSeed = 1000e18;
+        // Seed: 100,000 resource + 50,000 gold per pool (spot price 0.5 gold / resource).
+        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
+        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
+        uint256 totalGoldSeed = goldSeed * 4;
+
+        woodToken.seedTreasury(address(this), resSeed);
+        wheatToken.seedTreasury(address(this), resSeed);
+        fishToken.seedTreasury(address(this), resSeed);
+        ironToken.seedTreasury(address(this), resSeed);
+        goldToken.seedTreasury(address(this), totalGoldSeed);
+
+        woodToken.approve(address(world), resSeed);
+        wheatToken.approve(address(world), resSeed);
+        fishToken.approve(address(world), resSeed);
+        ironToken.approve(address(world), resSeed);
+        goldToken.approve(address(world), totalGoldSeed);
+
         PoolSeedConfig memory cfg = PoolSeedConfig({
             woodSeed: resSeed,
             wheatSeed: resSeed,
@@ -447,7 +468,7 @@ contract ClanWorldTest is Test {
         vm.warp(block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS + 1);
         _submitOrder(clanId, cs0, homeRegion, ActionType.DepositResources);
 
-        // Advance travel back to homebase + deposit duration.
+        // Advance through travel back to homebase and the deposit's 1-tick transfer.
         (uint8 travelBack,) = world.quoteTravel(targetRegion, homeRegion);
         for (uint256 i = 0; i < uint256(travelBack) + world.getActionDuration(ActionType.DepositResources) + 1; i++) {
             _advanceTick();
@@ -465,6 +486,105 @@ contract ClanWorldTest is Test {
         cs1;
     }
 
+    function test_depositResources_woodCarryMovesToVaultAndClears() public {
+        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
+        uint8 homeRegion = harness.getClan(clanId).baseRegion;
+        uint256 woodDelta = 5e18;
+
+        harness.setCarry(csId, woodDelta, 0, 0, 0);
+        uint256 vaultBefore = harness.getClan(clanId).vaultWood;
+
+        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "deposit order should be accepted");
+
+        Mission memory mission = harness.getActiveMission(csId);
+        assertEq(
+            mission.settlesAtTick,
+            mission.executesAtTick + harness.getActionDuration(ActionType.DepositResources),
+            "deposit settles after transfer duration"
+        );
+
+        _advanceTickHarness(harness);
+        _advanceTickHarness(harness);
+
+        assertEq(harness.getClan(clanId).vaultWood, vaultBefore + woodDelta, "vault wood receives carried wood");
+        assertEq(harness.getClansman(csId).carryWood, 0, "carry wood is cleared");
+    }
+
+    function test_depositResources_emptyCarryNoopsWithoutEvent() public {
+        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
+        uint8 homeRegion = harness.getClan(clanId).baseRegion;
+
+        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "empty deposit order should be accepted");
+
+        vm.recordLogs();
+        _advanceTickHarness(harness);
+        _advanceTickHarness(harness);
+        Vm.Log[] memory logs = vm.getRecordedLogs();
+        bytes32 depositedTopic = keccak256("ResourcesDeposited(uint32,uint32,uint256,uint256,uint256,uint256,uint64)");
+
+        for (uint256 i = 0; i < logs.length; i++) {
+            assertTrue(logs[i].topics[0] != depositedTopic, "empty deposit emits no ResourcesDeposited event");
+        }
+
+        assertFalse(harness.getActiveMission(csId).active, "empty deposit still completes");
+    }
+
+    function test_depositResources_multipleTypesMoveTogether() public {
+        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
+        uint8 homeRegion = harness.getClan(clanId).baseRegion;
+        uint256 woodDelta = 4e18;
+        uint256 ironDelta = 2e18;
+        uint256 fishDelta = 3e18;
+
+        harness.setCarry(csId, woodDelta, ironDelta, 0, fishDelta);
+        Clan memory beforeClan = harness.getClan(clanId);
+
+        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "deposit order should be accepted");
+
+        _advanceTickHarness(harness);
+        _advanceTickHarness(harness);
+
+        Clan memory afterClan = harness.getClan(clanId);
+        Clansman memory afterCs = harness.getClansman(csId);
+
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood + woodDelta, "wood transferred");
+        assertEq(afterClan.vaultIron, beforeClan.vaultIron + ironDelta, "iron transferred");
+        assertEq(afterClan.vaultFish, beforeClan.vaultFish + fishDelta, "fish transferred");
+        assertEq(afterCs.carryWood, 0, "wood carry cleared");
+        assertEq(afterCs.carryIron, 0, "iron carry cleared");
+        assertEq(afterCs.carryFish, 0, "fish carry cleared");
+    }
+
+    function test_depositResources_requiresHomeRegion() public {
+        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
+        uint8 homeRegion = harness.getClan(clanId).baseRegion;
+        uint8 nonHomeRegion = homeRegion == ClanWorldConstants.REGION_FOREST
+            ? ClanWorldConstants.REGION_MOUNTAINS
+            : ClanWorldConstants.REGION_FOREST;
+
+        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, nonHomeRegion, ActionType.DepositResources);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_INVALID_REGION), "deposit must target home region");
+    }
+
+    function test_depositResources_eventHasCorrectDeltas() public {
+        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
+        uint8 homeRegion = harness.getClan(clanId).baseRegion;
+
+        harness.setCarry(csId, 5e18, 2e18, 1e18, 3e18);
+
+        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "deposit order should be accepted");
+
+        _advanceTickHarness(harness);
+        vm.expectEmit(true, true, false, true);
+        emit IClanWorldEvents.ResourcesDeposited(clanId, csId, 5e18, 2e18, 1e18, 3e18, 1);
+        _advanceTickHarness(harness);
+    }
+
     function test_quoteTravel_outOfBounds_returnsZero() public {
         (uint8 ticks, bytes8 path) = world.quoteTravel(9, 1);
         assertEq(ticks, 0, "out-of-range src should return 0 ticks");
@@ -1293,15 +1413,14 @@ contract ClanWorldTest is Test {
         uint32 csId = v.clansmen[0].clansman.clansman.clansmanId;
         uint8 homeRegion = v.clan.clan.baseRegion;
 
-        // Send clansman to homebase to DepositResources (empty carry → completes in 1 tick).
-        // This is the cleanest single-tick completion: _doDeposit with empty carry calls _completeMission immediately.
+        // Send clansman to homebase to DepositResources (empty carry -> completes when the arrival tick closes).
+        // _doDeposit with empty carry calls _completeMission immediately during settlement.
         OrderResult[] memory r = _submitOrder(clanId, csId, homeRegion, ActionType.DepositResources);
         assertEq(uint8(r[0].status), uint8(StatusCode.OK), "3.E5: order must succeed");
 
         // Wait for submit-time cooldown to expire (warp only; no ticks yet)
         vm.warp(block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS + 1);
 
-        // Advance until the one-tick deposit duration has closed.
         _advanceTick();
         _advanceTick();
 
@@ -1814,7 +1933,11 @@ contract ClanWorldTest is Test {
         // Call settleClansman on-demand — must be idempotent (no crash, no double-credit).
         uint256 ironBefore = world.getClansman(csId).carryIron;
         world.settleClansman(csId);
-        assertEq(world.getClansman(csId).carryIron, ironBefore, "onDemand: settleClansman is idempotent after heartbeat settle");
+        assertEq(
+            world.getClansman(csId).carryIron,
+            ironBefore,
+            "onDemand: settleClansman is idempotent after heartbeat settle"
+        );
     }
 
     // -------------------------------------------------------------------------
@@ -1921,8 +2044,7 @@ contract ClanWorldTest is Test {
         assertEq(ws.seasonStartTick, 0);
         assertEq(ws.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS);
         assertEq(
-            ws.winterStartsAtTick,
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
         );
         assertFalse(ws.winterActive);
     }
@@ -1931,8 +2053,7 @@ contract ClanWorldTest is Test {
         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
         // Advance 99 heartbeats so currentTick=99, then one more heartbeat fires WinterStarted at tick 100.
-        uint64 winterStart =
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
         for (uint64 i = 0; i < winterStart - 1; i++) {
             vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
             world.heartbeat();
diff --git a/packages/contracts/test/DeadClanOtc.t.sol b/packages/contracts/test/DeadClanOtc.t.sol
new file mode 100644
index 0000000..70d1b43
--- /dev/null
+++ b/packages/contracts/test/DeadClanOtc.t.sol
@@ -0,0 +1,210 @@
+// SPDX-License-Identifier: MIT
+pragma solidity ^0.8.34;
+
+import {Test} from "forge-std/Test.sol";
+import {ClanWorld} from "../src/ClanWorld.sol";
+import {Clan, ClanState} from "../src/IClanWorld.sol";
+
+contract DeadClanOtcHarness is ClanWorld {
+    function setClanState(uint32 clanId, ClanState state) external {
+        _clans[clanId].clanState = state;
+    }
+
+    function setVault(uint32 clanId, uint256 wood, uint256 wheat, uint256 fish, uint256 iron) external {
+        _clans[clanId].vaultWood = wood;
+        _clans[clanId].vaultWheat = wheat;
+        _clans[clanId].vaultFish = fish;
+        _clans[clanId].vaultIron = iron;
+    }
+
+    function setBlueprintBalance(uint32 clanId, uint256 amount) external {
+        _clans[clanId].blueprintBalance = amount;
+    }
+}
+
+contract DeadClanOtcTest is Test {
+    DeadClanOtcHarness world;
+    address elderA = address(0xA1);
+    address elderB = address(0xA2);
+    address elderC = address(0xA3);
+
+    function setUp() public {
+        world = new DeadClanOtcHarness();
+    }
+
+    function test_aliveClansCanProposeAndAcceptAllOtcTypes() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        _fundOtcBalances(clanA);
+
+        uint256 goldProposal = _proposeGold(elderA, clanA, clanB, 1e18);
+        uint256 vaultProposal = _proposeVault(elderA, clanA, clanB, 1e18);
+        uint256 blueprintProposal = _proposeBlueprint(elderA, clanA, clanB, 1e18);
+        uint256 bundledProposal = _proposeBundled(elderA, clanA, clanB, 1e18);
+
+        vm.prank(elderB);
+        world.acceptGoldTransfer(goldProposal);
+        vm.prank(elderB);
+        world.acceptVaultTransfer(vaultProposal);
+        vm.prank(elderB);
+        world.acceptBlueprintTransfer(blueprintProposal);
+        vm.prank(elderB);
+        world.acceptBundledTransfer(bundledProposal);
+
+        Clan memory fromAfter = world.getClan(clanA);
+        Clan memory toAfter = world.getClan(clanB);
+        assertEq(fromAfter.goldBalance, 1e18, "from gold debited");
+        assertEq(toAfter.goldBalance, 5e18, "to gold credited");
+        assertEq(fromAfter.vaultWood, 8e18, "from wood debited");
+        assertEq(toAfter.vaultWood, 22e18, "to wood credited");
+        assertEq(fromAfter.blueprintBalance, 0, "from blueprint debited");
+        assertEq(toAfter.blueprintBalance, 2e18, "to blueprint credited");
+    }
+
+    function test_deadProposerCannotProposeAnyOtcType() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        _fundOtcBalances(clanA);
+        world.setClanState(clanA, ClanState.DEAD);
+
+        vm.expectRevert("ERR_CLAN_DEAD");
+        _proposeGold(elderA, clanA, clanB, 1e18);
+        vm.expectRevert("ERR_CLAN_DEAD");
+        _proposeVault(elderA, clanA, clanB, 1e18);
+        vm.expectRevert("ERR_CLAN_DEAD");
+        _proposeBlueprint(elderA, clanA, clanB, 1e18);
+        vm.expectRevert("ERR_CLAN_DEAD");
+        _proposeBundled(elderA, clanA, clanB, 1e18);
+    }
+
+    function test_deadProposerAfterProposeCannotBeAccepted() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        _fundOtcBalances(clanA);
+        uint256 goldProposal = _proposeGold(elderA, clanA, clanB, 1e18);
+        uint256 vaultProposal = _proposeVault(elderA, clanA, clanB, 1e18);
+        uint256 blueprintProposal = _proposeBlueprint(elderA, clanA, clanB, 1e18);
+        uint256 bundledProposal = _proposeBundled(elderA, clanA, clanB, 1e18);
+
+        world.setClanState(clanA, ClanState.DEAD);
+
+        vm.expectRevert("ERR_CLAN_DEAD");
+        vm.prank(elderB);
+        world.acceptGoldTransfer(goldProposal);
+        vm.expectRevert("ERR_CLAN_DEAD");
+        vm.prank(elderB);
+        world.acceptVaultTransfer(vaultProposal);
+        vm.expectRevert("ERR_CLAN_DEAD");
+        vm.prank(elderB);
+        world.acceptBlueprintTransfer(blueprintProposal);
+        vm.expectRevert("ERR_CLAN_DEAD");
+        vm.prank(elderB);
+        world.acceptBundledTransfer(bundledProposal);
+    }
+
+    function test_deadTargetCannotAcceptAnyOtcType() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        _fundOtcBalances(clanA);
+        uint256 goldProposal = _proposeGold(elderA, clanA, clanB, 1e18);
+        uint256 vaultProposal = _proposeVault(elderA, clanA, clanB, 1e18);
+        uint256 blueprintProposal = _proposeBlueprint(elderA, clanA, clanB, 1e18);
+        uint256 bundledProposal = _proposeBundled(elderA, clanA, clanB, 1e18);
+
+        world.setClanState(clanB, ClanState.DEAD);
+
+        vm.expectRevert("ERR_CLAN_DEAD");
+        vm.prank(elderB);
+        world.acceptGoldTransfer(goldProposal);
+        vm.expectRevert("ERR_CLAN_DEAD");
+        vm.prank(elderB);
+        world.acceptVaultTransfer(vaultProposal);
+        vm.expectRevert("ERR_CLAN_DEAD");
+        vm.prank(elderB);
+        world.acceptBlueprintTransfer(blueprintProposal);
+        vm.expectRevert("ERR_CLAN_DEAD");
+        vm.prank(elderB);
+        world.acceptBundledTransfer(bundledProposal);
+    }
+
+    function test_deadProposerCanCancelExistingProposals() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        _fundOtcBalances(clanA);
+        uint256 goldProposal = _proposeGold(elderA, clanA, clanB, 1e18);
+        uint256 vaultProposal = _proposeVault(elderA, clanA, clanB, 1e18);
+        uint256 blueprintProposal = _proposeBlueprint(elderA, clanA, clanB, 1e18);
+        uint256 bundledProposal = _proposeBundled(elderA, clanA, clanB, 1e18);
+
+        world.setClanState(clanA, ClanState.DEAD);
+
+        vm.prank(elderA);
+        world.cancelGoldTransfer(goldProposal);
+        vm.prank(elderA);
+        world.cancelVaultTransfer(vaultProposal);
+        vm.prank(elderA);
+        world.cancelBlueprintTransfer(blueprintProposal);
+        vm.prank(elderA);
+        world.cancelBundledTransfer(bundledProposal);
+
+        assertEq(world.getOtcGoldProposal(goldProposal).from, 0, "gold deleted");
+        assertEq(world.getOtcVaultTransferProposal(vaultProposal).from, 0, "vault deleted");
+        assertEq(world.getOtcBlueprintTransferProposal(blueprintProposal).from, 0, "blueprint deleted");
+        assertEq(world.getOtcBundledTransferProposal(bundledProposal).from, 0, "bundled deleted");
+    }
+
+    function test_unrelatedClanDeathDoesNotBlockOtherClanOtc() public {
+        (uint32 clanA, uint32 clanB, uint32 clanC) = _mintThreeClans();
+        world.setClanState(clanA, ClanState.DEAD);
+
+        uint256 clanCBefore = world.getClan(clanC).goldBalance;
+        uint256 proposalId = _proposeGold(elderB, clanB, clanC, 1e18);
+
+        vm.prank(elderC);
+        world.acceptGoldTransfer(proposalId);
+
+        assertEq(world.getClan(clanC).goldBalance, clanCBefore + 1e18, "alive clans trade");
+    }
+
+    function _mintThreeClans() internal returns (uint32 clanA, uint32 clanB, uint32 clanC) {
+        vm.prank(elderA);
+        (clanA,) = world.mintClan(elderA);
+        vm.prank(elderB);
+        (clanB,) = world.mintClan(elderB);
+        vm.prank(elderC);
+        (clanC,) = world.mintClan(elderC);
+    }
+
+    function _fundOtcBalances(uint32 clanId) internal {
+        world.setVault(clanId, 10e18, 10e18, 10e18, 10e18);
+        world.setBlueprintBalance(clanId, 2e18);
+    }
+
+    function _proposeGold(address elder, uint32 fromClanId, uint32 toClanId, uint256 amount)
+        internal
+        returns (uint256 proposalId)
+    {
+        vm.prank(elder);
+        proposalId = world.proposeGoldTransfer(fromClanId, toClanId, amount, 10);
+    }
+
+    function _proposeVault(address elder, uint32 fromClanId, uint32 toClanId, uint256 amount)
+        internal
+        returns (uint256 proposalId)
+    {
+        vm.prank(elder);
+        proposalId = world.proposeVaultTransfer(fromClanId, toClanId, amount, amount, amount, amount, 10);
+    }
+
+    function _proposeBlueprint(address elder, uint32 fromClanId, uint32 toClanId, uint256 amount)
+        internal
+        returns (uint256 proposalId)
+    {
+        vm.prank(elder);
+        proposalId = world.proposeBlueprintTransfer(fromClanId, toClanId, amount, 10);
+    }
+
+    function _proposeBundled(address elder, uint32 fromClanId, uint32 toClanId, uint256 amount)
+        internal
+        returns (uint256 proposalId)
+    {
+        vm.prank(elder);
+        proposalId =
+            world.proposeBundledTransfer(fromClanId, toClanId, amount, amount, amount, amount, amount, amount, 10);
+    }
+}
diff --git a/packages/contracts/test/Gathering.t.sol b/packages/contracts/test/Gathering.t.sol
new file mode 100644
index 0000000..4f64396
--- /dev/null
+++ b/packages/contracts/test/Gathering.t.sol
@@ -0,0 +1,134 @@
+// SPDX-License-Identifier: MIT
+pragma solidity ^0.8.34;
+
+import {Test} from "forge-std/Test.sol";
+import {ClanWorld} from "../src/ClanWorld.sol";
+import {
+    ClanWorldConstants,
+    ActionType,
+    ClansmanState,
+    StatusCode,
+    Clansman,
+    Mission,
+    ClanFullView,
+    ClanOrder,
+    OrderResult
+} from "../src/IClanWorld.sol";
+
+contract GatheringHarness is ClanWorld {
+    function setCarryWood(uint32 csId, uint256 wood) external {
+        _clansmen[csId].carryWood = wood;
+    }
+}
+
+contract GatheringTest is Test {
+    GatheringHarness world;
+    address elder = address(0xA1);
+
+    function setUp() public {
+        world = new GatheringHarness();
+    }
+
+    function _advanceTick() internal {
+        vm.warp(world.getWorldState().nextHeartbeatAtTs);
+        world.heartbeat();
+    }
+
+    function _advanceUntilCurrentTick(uint64 targetTick) internal {
+        while (world.getWorldState().currentTick < targetTick) {
+            _advanceTick();
+        }
+    }
+
+    function _mintClan() internal returns (uint32 clanId) {
+        vm.prank(elder);
+        (clanId,) = world.mintClan(elder);
+    }
+
+    function _firstCs(uint32 clanId) internal view returns (uint32) {
+        ClanFullView memory view_ = world.getClanFullView(clanId);
+        return view_.clansmen[0].clansman.clansman.clansmanId;
+    }
+
+    function _submitChopWood(uint32 clanId, uint32 csId) internal returns (OrderResult[] memory) {
+        ClanOrder[] memory orders = new ClanOrder[](1);
+        orders[0] = ClanOrder({
+            clansmanId: csId,
+            gotoRegion: ClanWorldConstants.REGION_FOREST,
+            action: ActionType.ChopWood,
+            targetClanId: 0,
+            marketToken: address(0),
+            marketAmount: 0,
+            maxGoldIn: 0
+        });
+
+        vm.prank(elder);
+        return world.submitClanOrders(clanId, orders);
+    }
+
+    function _settleChopWood(uint32 clanId, uint32 csId) internal returns (Clansman memory) {
+        OrderResult[] memory result = _submitChopWood(clanId, csId);
+        assertEq(uint8(result[0].status), uint8(StatusCode.OK), "chop wood accepted");
+
+        Mission memory mission = world.getActiveMission(csId);
+        _advanceUntilCurrentTick(mission.settlesAtTick + 1);
+        world.settleClan(clanId);
+        return world.getClansman(csId);
+    }
+
+    function test_chopWoodAtForestYieldsBaseTimesActionDuration() public {
+        vm.prevrandao(bytes32(uint256(2)));
+        uint32 clanId = _mintClan();
+        uint32 csId = _firstCs(clanId);
+
+        Clansman memory cs = _settleChopWood(clanId, csId);
+
+        uint256 expected = ClanWorldConstants.WOOD_YIELD_PER_TICK * world.getActionDuration(ActionType.ChopWood);
+        assertEq(cs.carryWood, expected, "base wood yield");
+    }
+
+    function test_chopWoodCritDistributionAcrossSeeds() public {
+        uint256 baseYield = ClanWorldConstants.WOOD_YIELD_PER_TICK * world.getActionDuration(ActionType.ChopWood);
+        uint256 critCount = 0;
+        world = new GatheringHarness();
+        uint256 cleanState = vm.snapshotState();
+
+        for (uint256 i = 0; i < 100; i++) {
+            assertTrue(vm.revertToState(cleanState), "reset gathering world");
+            vm.prevrandao(bytes32(uint256(i + 10_000)));
+            uint32 clanId = _mintClan();
+            uint32 csId = _firstCs(clanId);
+
+            Clansman memory cs = _settleChopWood(clanId, csId);
+            if (cs.carryWood == baseYield * 2) {
+                critCount++;
+            } else {
+                assertEq(cs.carryWood, baseYield, "non-crit yield");
+            }
+        }
+
+        assertGe(critCount, 3, "crit count too low");
+        assertLe(critCount, 20, "crit count too high");
+    }
+
+    function test_chopWoodClampsToCarryCap() public {
+        uint32 clanId = _mintClan();
+        uint32 csId = _firstCs(clanId);
+        world.setCarryWood(csId, ClanWorldConstants.CLANSMAN_CARRY_CAP - 1e18);
+
+        Clansman memory cs = _settleChopWood(clanId, csId);
+
+        assertEq(cs.carryWood, ClanWorldConstants.CLANSMAN_CARRY_CAP, "wood carry cap");
+    }
+
+    function test_chopWoodAppliesCooldownPostSettle() public {
+        uint32 clanId = _mintClan();
+        uint32 csId = _firstCs(clanId);
+
+        Clansman memory cs = _settleChopWood(clanId, csId);
+
+        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "mission completed");
+        assertFalse(world.getActiveMission(csId).active, "mission inactive");
+        assertGt(cs.cooldownEndsAtTs, block.timestamp, "cooldown starts on settlement");
+    }
+}
diff --git a/packages/contracts/test/GoldTransferOtc.t.sol b/packages/contracts/test/GoldTransferOtc.t.sol
new file mode 100644
index 0000000..643682e
--- /dev/null
+++ b/packages/contracts/test/GoldTransferOtc.t.sol
@@ -0,0 +1,214 @@
+// SPDX-License-Identifier: MIT
+pragma solidity ^0.8.34;
+
+import {Test} from "forge-std/Test.sol";
+import {ClanWorld} from "../src/ClanWorld.sol";
+import {ClanWorldConstants, ClanState, OtcProposal} from "../src/IClanWorld.sol";
+
+contract GoldTransferOtcTest is Test {
+    event GoldTransferProposed(
+        uint256 indexed proposalId,
+        uint32 indexed fromClanId,
+        uint32 indexed toClanId,
+        uint256 amount,
+        uint256 expiryTick
+    );
+    event GoldTransferAccepted(
+        uint256 indexed proposalId,
+        uint32 indexed fromClanId,
+        uint32 indexed toClanId,
+        uint256 amount,
+        uint64 settledAtTick
+    );
+    event GoldTransferCancelled(uint256 indexed proposalId);
+
+    ClanWorld world;
+    address elderA = address(0xA1);
+    address elderB = address(0xA2);
+    address elderC = address(0xA3);
+
+    function setUp() public {
+        world = new ClanWorld();
+    }
+
+    function test_proposeGoldTransfer_storesProposalAndEmits() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        uint256 amount = 1e18;
+        uint256 expiryTick = 10;
+
+        vm.expectEmit(true, true, true, true, address(world));
+        emit GoldTransferProposed(1, clanA, clanB, amount, expiryTick);
+        vm.prank(elderA);
+        uint256 proposalId = world.proposeGoldTransfer(clanA, clanB, amount, expiryTick);
+
+        assertEq(proposalId, 1, "first proposal id");
+        OtcProposal memory proposal = world.getOtcGoldProposal(proposalId);
+        assertEq(proposal.from, clanA, "proposal from");
+        assertEq(proposal.to, clanB, "proposal to");
+        assertEq(proposal.amount, amount, "proposal amount");
+        assertEq(proposal.expiryTick, uint64(expiryTick), "proposal expiry");
+        assertFalse(proposal.accepted, "proposal not accepted");
+        assertFalse(proposal.cancelled, "proposal not cancelled");
+    }
+
+    function test_acceptGoldTransfer_transfersAtomicallyAndEmits() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        uint256 amount = 1e18;
+        uint256 fromBefore = world.getClan(clanA).goldBalance;
+        uint256 toBefore = world.getClan(clanB).goldBalance;
+
+        uint256 proposalId = _propose(clanA, clanB, amount, 10);
+
+        vm.expectEmit(true, true, true, true, address(world));
+        emit GoldTransferAccepted(proposalId, clanA, clanB, amount, world.getWorldState().currentTick);
+        vm.prank(elderB);
+        world.acceptGoldTransfer(proposalId);
+
+        assertEq(world.getClan(clanA).goldBalance, fromBefore - amount, "from debited");
+        assertEq(world.getClan(clanB).goldBalance, toBefore + amount, "to credited");
+        assertEq(world.getOtcGoldProposal(proposalId).from, 0, "proposal deleted");
+    }
+
+    function test_acceptGoldTransfer_revertsWhenBalanceChangedAfterProposal() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        uint256 proposalOne = _propose(clanA, clanB, 3e18, 10);
+        uint256 proposalTwo = _propose(clanA, clanB, 3e18, 10);
+
+        vm.prank(elderB);
+        world.acceptGoldTransfer(proposalOne);
+
+        vm.expectRevert("ERR_NOT_ENOUGH_GOLD");
+        vm.prank(elderB);
+        world.acceptGoldTransfer(proposalTwo);
+    }
+
+    function test_acceptGoldTransfer_revertsWhenExpired() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        uint256 proposalId = _propose(clanA, clanB, 1e18, world.getWorldState().currentTick);
+        _advanceTick();
+
+        vm.expectRevert("ClanWorld: proposal expired");
+        vm.prank(elderB);
+        world.acceptGoldTransfer(proposalId);
+    }
+
+    function test_goldTransfer_wrongCallersRevert() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+
+        vm.expectRevert("ClanWorld: not clan owner");
+        vm.prank(elderB);
+        world.proposeGoldTransfer(clanA, clanB, 1e18, 10);
+
+        uint256 proposalId = _propose(clanA, clanB, 1e18, 10);
+        vm.expectRevert("ClanWorld: not clan owner");
+        vm.prank(elderA);
+        world.acceptGoldTransfer(proposalId);
+    }
+
+    function test_cancelGoldTransfer_byProposerBlocksAccept() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        uint256 proposalId = _propose(clanA, clanB, 1e18, 10);
+
+        vm.expectEmit(true, false, false, true, address(world));
+        emit GoldTransferCancelled(proposalId);
+        vm.prank(elderA);
+        world.cancelGoldTransfer(proposalId);
+
+        assertEq(world.getOtcGoldProposal(proposalId).from, 0, "proposal deleted");
+        vm.expectRevert("ClanWorld: proposal not found");
+        vm.prank(elderB);
+        world.acceptGoldTransfer(proposalId);
+    }
+
+    function test_cancelGoldTransfer_acceptorCannotCancel() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        uint256 proposalId = _propose(clanA, clanB, 1e18, 10);
+
+        vm.expectRevert("ClanWorld: not clan owner");
+        vm.prank(elderB);
+        world.cancelGoldTransfer(proposalId);
+    }
+
+    function test_goldTransfer_twoClanNoInterference() public {
+        (uint32 clanA, uint32 clanB, uint32 clanC) = _mintThreeClans();
+        uint256 clanBBefore = world.getClan(clanB).goldBalance;
+        uint256 clanCBefore = world.getClan(clanC).goldBalance;
+
+        uint256 proposalId = _propose(clanA, clanC, 1e18, 10);
+        vm.prank(elderC);
+        world.acceptGoldTransfer(proposalId);
+
+        assertEq(world.getClan(clanB).goldBalance, clanBBefore, "unrelated clan unchanged");
+        assertEq(world.getClan(clanC).goldBalance, clanCBefore + 1e18, "target clan credited");
+    }
+
+    function test_proposeGoldTransfer_revertsWhenZeroAmount() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+
+        vm.expectRevert("ERR_ZERO_AMOUNT");
+        _propose(clanA, clanB, 0, 10);
+    }
+
+    function test_proposeGoldTransfer_revertsWhenSelfTransfer() public {
+        (uint32 clanA,,) = _mintThreeClans();
+
+        vm.expectRevert("ERR_SELF_TRANSFER");
+        _propose(clanA, clanA, 1e18, 10);
+    }
+
+    function test_goldTransfer_openProposalCapDecrementsAfterAccept() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+
+        uint256 firstProposalId;
+        uint256 secondProposalId;
+        for (uint256 i = 0; i < world.MAX_OPEN_OTC_PROPOSALS_PER_CLAN(); i++) {
+            uint256 proposalId = _propose(clanA, clanB, 1, 10);
+            if (i == 0) firstProposalId = proposalId;
+            if (i == 1) secondProposalId = proposalId;
+        }
+
+        vm.expectRevert("ERR_OTC_CAP");
+        _propose(clanA, clanB, 1, 10);
+
+        vm.prank(elderB);
+        world.acceptGoldTransfer(firstProposalId);
+
+        uint256 newProposalId = _propose(clanA, clanB, 1, 10);
+        assertGt(newProposalId, firstProposalId, "cap slot reopened after accept");
+
+        vm.expectRevert("ERR_OTC_CAP");
+        _propose(clanA, clanB, 1, 10);
+
+        vm.prank(elderA);
+        world.cancelGoldTransfer(secondProposalId);
+
+        uint256 postCancelProposalId = _propose(clanA, clanB, 1, 10);
+        assertGt(postCancelProposalId, newProposalId, "cap slot reopened after cancel");
+    }
+
+    function _mintThreeClans() internal returns (uint32 clanA, uint32 clanB, uint32 clanC) {
+        vm.prank(elderA);
+        (clanA,) = world.mintClan(elderA);
+        vm.prank(elderB);
+        (clanB,) = world.mintClan(elderB);
+        vm.prank(elderC);
+        (clanC,) = world.mintClan(elderC);
+
+        assertEq(uint8(world.getClan(clanA).clanState), uint8(ClanState.ACTIVE), "clan A alive");
+        assertEq(uint8(world.getClan(clanB).clanState), uint8(ClanState.ACTIVE), "clan B alive");
+        assertEq(uint8(world.getClan(clanC).clanState), uint8(ClanState.ACTIVE), "clan C alive");
+    }
+
+    function _propose(uint32 fromClanId, uint32 toClanId, uint256 amount, uint256 expiryTick)
+        internal
+        returns (uint256 proposalId)
+    {
+        vm.prank(elderA);
+        proposalId = world.proposeGoldTransfer(fromClanId, toClanId, amount, expiryTick);
+    }
+
+    function _advanceTick() internal {
+        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
+        world.heartbeat();
+    }
+}
diff --git a/packages/contracts/test/HeartbeatOrdering.t.sol b/packages/contracts/test/HeartbeatOrdering.t.sol
index 9d012e8..cea31e1 100644
--- a/packages/contracts/test/HeartbeatOrdering.t.sol
+++ b/packages/contracts/test/HeartbeatOrdering.t.sol
@@ -149,8 +149,22 @@ contract HeartbeatOrderingTest is Test {
         address[4] memory pools = [address(woodPool), address(wheatPool), address(fishPool), address(ironPool)];
         world.initTreasury(tokens, pools);
 
-        uint256 resSeed = 1000e18;
-        uint256 goldSeed = 1000e18;
+        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
+        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
+        uint256 totalGoldSeed = goldSeed * 4;
+
+        woodToken.seedTreasury(address(this), resSeed);
+        wheatToken.seedTreasury(address(this), resSeed);
+        fishToken.seedTreasury(address(this), resSeed);
+        ironToken.seedTreasury(address(this), resSeed);
+        goldToken.seedTreasury(address(this), totalGoldSeed);
+
+        woodToken.approve(address(world), resSeed);
+        wheatToken.approve(address(world), resSeed);
+        fishToken.approve(address(world), resSeed);
+        ironToken.approve(address(world), resSeed);
+        goldToken.approve(address(world), totalGoldSeed);
+
         PoolSeedConfig memory cfg = PoolSeedConfig({
             woodSeed: resSeed,
             wheatSeed: resSeed,
@@ -191,7 +205,8 @@ contract HeartbeatOrderingTest is Test {
         world.setClansmanRegion(csId0, ClanWorldConstants.REGION_MOUNTAINS);
 
         // cs0: submit Deposit. arrivalTick = t0+1, settlesAtTick = t0+2.
-        OrderResult[] memory r0 = _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
+        OrderResult[] memory r0 =
+            _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
         assertEq(uint8(r0[0].status), uint8(StatusCode.OK), "deposit order must succeed");
         Mission memory depositMission = world.getActiveMission(csId0);
         assertEq(depositMission.settlesAtTick, t0 + 2, "deposit settlesAtTick must be t0+2");
@@ -318,7 +333,8 @@ contract HeartbeatOrderingTest is Test {
         // Deposit: arrivalTick = t0+1, settlesAtTick = t0+2.
         world.setClansmanRegion(csId0, ClanWorldConstants.REGION_MOUNTAINS);
         world.setCarryWood(csId0, 10e18);
-        OrderResult[] memory r0 = _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
+        OrderResult[] memory r0 =
+            _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
         assertEq(uint8(r0[0].status), uint8(StatusCode.OK), "deposit must succeed");
         assertEq(world.getActiveMission(csId0).settlesAtTick, t0 + 2, "deposit settlesAtTick must be t0+2");
 
diff --git a/packages/contracts/test/Reentrancy.t.sol b/packages/contracts/test/Reentrancy.t.sol
index 30632fe..a5fc962 100644
--- a/packages/contracts/test/Reentrancy.t.sol
+++ b/packages/contracts/test/Reentrancy.t.sol
@@ -160,8 +160,22 @@ contract ReentrancyTest is Test {
         address[4] memory pools = [address(woodPool), address(wheatPool), address(fishPool), address(ironPool)];
         world.initTreasury(tokens, pools);
 
-        uint256 resSeed = 1000e18;
-        uint256 goldSeed = 1000e18;
+        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
+        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
+        uint256 totalGoldSeed = goldSeed * 4;
+
+        woodToken.seedTreasury(address(this), resSeed);
+        wheatToken.seedTreasury(address(this), resSeed);
+        fishToken.seedTreasury(address(this), resSeed);
+        ironToken.seedTreasury(address(this), resSeed);
+        goldToken.seedTreasury(address(this), totalGoldSeed);
+
+        woodToken.approve(address(world), resSeed);
+        wheatToken.approve(address(world), resSeed);
+        fishToken.approve(address(world), resSeed);
+        ironToken.approve(address(world), resSeed);
+        goldToken.approve(address(world), totalGoldSeed);
+
         PoolSeedConfig memory cfg = PoolSeedConfig({
             woodSeed: resSeed,
             wheatSeed: resSeed,
diff --git a/packages/contracts/test/ResourceBoundaryTokens.t.sol b/packages/contracts/test/ResourceBoundaryTokens.t.sol
new file mode 100644
index 0000000..864ee1f
--- /dev/null
+++ b/packages/contracts/test/ResourceBoundaryTokens.t.sol
@@ -0,0 +1,105 @@
+// SPDX-License-Identifier: MIT
+pragma solidity ^0.8.34;
+
+import {Test} from "forge-std/Test.sol";
+import {ClanWorld} from "../src/ClanWorld.sol";
+import {MinimalERC20} from "../src/MinimalERC20.sol";
+import {StubPool} from "../src/StubPool.sol";
+import {ResourceType} from "../src/IClanWorld.sol";
+
+contract ResourceBoundaryTokensTest is Test {
+    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
+    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);
+
+    ClanWorld world;
+    MinimalERC20 woodToken;
+    MinimalERC20 ironToken;
+    MinimalERC20 wheatToken;
+    MinimalERC20 fishToken;
+    MinimalERC20 goldToken;
+    MinimalERC20 blueprintToken;
+
+    function setUp() public {
+        world = new ClanWorld();
+        woodToken = new MinimalERC20("Wood", "WOOD");
+        ironToken = new MinimalERC20("Iron", "IRON");
+        wheatToken = new MinimalERC20("Wheat", "WHEAT");
+        fishToken = new MinimalERC20("Fish", "FISH");
+        goldToken = new MinimalERC20("Gold", "GOLD");
+        blueprintToken = new MinimalERC20("BPRT", "BPRT");
+
+        woodToken.configureBoundary(uint8(ResourceType.Wood), address(world));
+        ironToken.configureBoundary(uint8(ResourceType.Iron), address(world));
+        wheatToken.configureBoundary(uint8(ResourceType.Wheat), address(world));
+        fishToken.configureBoundary(uint8(ResourceType.Fish), address(world));
+    }
+
+    function test_getResourceTokenReturnsConfiguredBoundaryTokens() public {
+        StubPool woodPool = new StubPool(address(woodToken), address(goldToken), address(world));
+        StubPool wheatPool = new StubPool(address(wheatToken), address(goldToken), address(world));
+        StubPool fishPool = new StubPool(address(fishToken), address(goldToken), address(world));
+        StubPool ironPool = new StubPool(address(ironToken), address(goldToken), address(world));
+
+        address[6] memory tokens = [
+            address(woodToken),
+            address(ironToken),
+            address(wheatToken),
+            address(fishToken),
+            address(goldToken),
+            address(blueprintToken)
+        ];
+        address[4] memory pools = [address(woodPool), address(wheatPool), address(fishPool), address(ironPool)];
+
+        world.initTreasury(tokens, pools);
+
+        assertEq(world.getResourceToken(uint8(ResourceType.Wood)), address(woodToken), "wood");
+        assertEq(world.getResourceToken(uint8(ResourceType.Iron)), address(ironToken), "iron");
+        assertEq(world.getResourceToken(uint8(ResourceType.Wheat)), address(wheatToken), "wheat");
+        assertEq(world.getResourceToken(uint8(ResourceType.Fish)), address(fishToken), "fish");
+    }
+
+    function test_onlyEngineCanMintAndBurnBoundaryTokens() public {
+        address holder = address(0xCAFE);
+
+        vm.expectRevert(bytes("MinimalERC20: not engine"));
+        woodToken.mint(holder, 10e18);
+
+        vm.expectEmit(true, true, false, true, address(woodToken));
+        emit ResourceMinted(uint8(ResourceType.Wood), holder, 10e18);
+        vm.prank(address(world));
+        woodToken.mint(holder, 10e18);
+
+        assertEq(woodToken.balanceOf(holder), 10e18, "minted balance");
+
+        vm.expectEmit(true, true, false, true, address(woodToken));
+        emit ResourceBurned(uint8(ResourceType.Wood), holder, 4e18);
+        vm.prank(address(world));
+        woodToken.burn(holder, 4e18);
+
+        assertEq(woodToken.balanceOf(holder), 6e18, "burned balance");
+    }
+
+    function test_seedTreasuryMintsStartingSupply() public {
+        address treasury = address(0xBEEF);
+
+        woodToken.seedTreasury(treasury, 1000e18);
+
+        assertEq(woodToken.balanceOf(treasury), 1000e18, "treasury balance");
+        assertEq(woodToken.totalSupply(), 1000e18, "total supply");
+    }
+
+    function test_seedTreasuryCanOnlyRunOnce() public {
+        address treasury = address(0xBEEF);
+
+        woodToken.seedTreasury(treasury, 1000e18);
+
+        vm.expectRevert(bytes("MinimalERC20: treasury seeded"));
+        woodToken.seedTreasury(treasury, 1e18);
+    }
+
+    function test_onlyDeployerCanSeedTreasury() public {
+        vm.prank(address(0xBAD));
+        vm.expectRevert(bytes("MinimalERC20: not deployer"));
+        woodToken.seedTreasury(address(0xBEEF), 1000e18);
+    }
+}
diff --git a/packages/contracts/test/SeedPools.t.sol b/packages/contracts/test/SeedPools.t.sol
new file mode 100644
index 0000000..0252a89
--- /dev/null
+++ b/packages/contracts/test/SeedPools.t.sol
@@ -0,0 +1,148 @@
+// SPDX-License-Identifier: MIT
+pragma solidity ^0.8.34;
+
+import {Test} from "forge-std/Test.sol";
+import {ClanWorld} from "../src/ClanWorld.sol";
+import {MinimalERC20} from "../src/MinimalERC20.sol";
+import {StubPool} from "../src/StubPool.sol";
+import {PoolSeedConfig, ResourceType} from "../src/IClanWorld.sol";
+
+contract SeedPoolsTest is Test {
+    ClanWorld world;
+    MinimalERC20 woodToken;
+    MinimalERC20 ironToken;
+    MinimalERC20 wheatToken;
+    MinimalERC20 fishToken;
+    MinimalERC20 goldToken;
+    MinimalERC20 blueprintToken;
+    StubPool woodPool;
+    StubPool ironPool;
+    StubPool wheatPool;
+    StubPool fishPool;
+
+    function setUp() public {
+        world = new ClanWorld();
+
+        woodToken = new MinimalERC20("Wood", "WOOD");
+        ironToken = new MinimalERC20("Iron", "IRON");
+        wheatToken = new MinimalERC20("Wheat", "WHEAT");
+        fishToken = new MinimalERC20("Fish", "FISH");
+        goldToken = new MinimalERC20("Gold", "GOLD");
+        blueprintToken = new MinimalERC20("BPRT", "BPRT");
+
+        address engine = address(world);
+        woodPool = new StubPool(address(woodToken), address(goldToken), engine);
+        wheatPool = new StubPool(address(wheatToken), address(goldToken), engine);
+        fishPool = new StubPool(address(fishToken), address(goldToken), engine);
+        ironPool = new StubPool(address(ironToken), address(goldToken), engine);
+
+        address[6] memory tokens = [
+            address(woodToken),
+            address(ironToken),
+            address(wheatToken),
+            address(fishToken),
+            address(goldToken),
+            address(blueprintToken)
+        ];
+        address[4] memory pools = [address(woodPool), address(wheatPool), address(fishPool), address(ironPool)];
+        world.initTreasury(tokens, pools);
+
+        _seedTreasuryAndApprove();
+        world.seedPools(_seedConfig());
+    }
+
+    function test_deploysFourPoolsAndGetterReturnsExpectedPools() public {
+        assertEq(world.getPool(uint8(ResourceType.Wood)), address(woodPool), "wood pool");
+        assertEq(world.getPool(uint8(ResourceType.Wheat)), address(wheatPool), "wheat pool");
+        assertEq(world.getPool(uint8(ResourceType.Fish)), address(fishPool), "fish pool");
+        assertEq(world.getPool(uint8(ResourceType.Iron)), address(ironPool), "iron pool");
+    }
+
+    function test_treasurySeedingTransfersExpectedPoolBalances() public {
+        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
+        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
+
+        _assertPoolSeeded(woodToken, woodPool, resSeed, goldSeed, "wood");
+        _assertPoolSeeded(wheatToken, wheatPool, resSeed, goldSeed, "wheat");
+        _assertPoolSeeded(fishToken, fishPool, resSeed, goldSeed, "fish");
+        _assertPoolSeeded(ironToken, ironPool, resSeed, goldSeed, "iron");
+        assertEq(goldToken.balanceOf(address(this)), 0, "treasury gold fully seeded");
+    }
+
+    function test_kInvariantHoldsAfterEachSwap() public {
+        uint256 kBefore = _k(woodPool);
+
+        vm.prank(address(world));
+        woodPool.swapExactInForOut(10e18, 1);
+        uint256 kAfterSell = _k(woodPool);
+        assertGe(kAfterSell, kBefore, "k after exact-in sell");
+
+        vm.prank(address(world));
+        woodPool.swapExactOutForInWithMaxIn(3e18, 10e18);
+        uint256 kAfterBuy = _k(woodPool);
+        assertGe(kAfterBuy, kAfterSell, "k after exact-out buy");
+    }
+
+    function test_pricePreviewMatchesActualSwap() public {
+        uint256 amountIn = 25e18;
+        uint256 preview = world.getPrice(uint8(ResourceType.Wood), amountIn);
+
+        vm.prank(address(world));
+        uint256 actual = woodPool.swapExactInForOut(amountIn, 0);
+
+        assertEq(actual, preview, "preview should match exact-in swap");
+    }
+
+    function _seedTreasuryAndApprove() internal {
+        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
+        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
+        uint256 totalGoldSeed = goldSeed * 4;
+
+        woodToken.seedTreasury(address(this), resSeed);
+        wheatToken.seedTreasury(address(this), resSeed);
+        fishToken.seedTreasury(address(this), resSeed);
+        ironToken.seedTreasury(address(this), resSeed);
+        goldToken.seedTreasury(address(this), totalGoldSeed);
+
+        woodToken.approve(address(world), resSeed);
+        wheatToken.approve(address(world), resSeed);
+        fishToken.approve(address(world), resSeed);
+        ironToken.approve(address(world), resSeed);
+        goldToken.approve(address(world), totalGoldSeed);
+    }
+
+    function _seedConfig() internal view returns (PoolSeedConfig memory) {
+        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
+        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
+        return PoolSeedConfig({
+            woodSeed: resSeed,
+            wheatSeed: resSeed,
+            fishSeed: resSeed,
+            ironSeed: resSeed,
+            goldSeedForWood: goldSeed,
+            goldSeedForWheat: goldSeed,
+            goldSeedForFish: goldSeed,
+            goldSeedForIron: goldSeed
+        });
+    }
+
+    function _assertPoolSeeded(
+        MinimalERC20 resourceToken,
+        StubPool pool,
+        uint256 expectedResource,
+        uint256 expectedGold,
+        string memory label
+    ) internal view {
+        (uint256 resourceReserve, uint256 goldReserve) = pool.getReserves();
+
+        assertEq(resourceReserve, expectedResource, string.concat(label, " resource reserve"));
+        assertEq(goldReserve, expectedGold, string.concat(label, " gold reserve"));
+        assertEq(resourceToken.balanceOf(address(pool)), expectedResource, string.concat(label, " resource balance"));
+        assertEq(goldToken.balanceOf(address(pool)), expectedGold, string.concat(label, " gold balance"));
+    }
+
+    function _k(StubPool pool) internal view returns (uint256) {
+        (uint256 resourceReserve, uint256 goldReserve) = pool.getReserves();
+        return resourceReserve * goldReserve;
+    }
+}
diff --git a/packages/contracts/test/VaultTransferOtc.t.sol b/packages/contracts/test/VaultTransferOtc.t.sol
new file mode 100644
index 0000000..5622654
--- /dev/null
+++ b/packages/contracts/test/VaultTransferOtc.t.sol
@@ -0,0 +1,282 @@
+// SPDX-License-Identifier: MIT
+pragma solidity ^0.8.34;
+
+import {Test} from "forge-std/Test.sol";
+import {ClanWorld} from "../src/ClanWorld.sol";
+import {
+    ActionType,
+    ClanWorldConstants,
+    Clan,
+    ClanFullView,
+    ClanOrder,
+    ClanState,
+    OrderResult,
+    StatusCode,
+    VaultTransferProposal
+} from "../src/IClanWorld.sol";
+
+contract VaultTransferHarness is ClanWorld {
+    function setVault(uint32 clanId, uint256 wood, uint256 wheat, uint256 fish, uint256 iron) external {
+        _clans[clanId].vaultWood = wood;
+        _clans[clanId].vaultWheat = wheat;
+        _clans[clanId].vaultFish = fish;
+        _clans[clanId].vaultIron = iron;
+    }
+
+    function setCarry(uint32 clansmanId, uint256 wood, uint256 wheat, uint256 fish, uint256 iron) external {
+        _clansmen[clansmanId].carryWood = wood;
+        _clansmen[clansmanId].carryWheat = wheat;
+        _clansmen[clansmanId].carryFish = fish;
+        _clansmen[clansmanId].carryIron = iron;
+    }
+}
+
+contract VaultTransferOtcTest is Test {
+    event VaultTransferProposed(
+        uint256 indexed proposalId,
+        uint32 indexed fromClanId,
+        uint32 indexed toClanId,
+        uint256 wood,
+        uint256 wheat,
+        uint256 fish,
+        uint256 iron,
+        uint64 expiryTick
+    );
+    event VaultTransferAccepted(
+        uint256 indexed proposalId,
+        uint32 indexed fromClanId,
+        uint32 indexed toClanId,
+        uint256 wood,
+        uint256 wheat,
+        uint256 fish,
+        uint256 iron,
+        uint64 settledAtTick
+    );
+    event VaultTransferCancelled(uint256 indexed proposalId);
+
+    VaultTransferHarness world;
+    address elderA = address(0xA1);
+    address elderB = address(0xA2);
+    address elderC = address(0xA3);
+
+    function setUp() public {
+        world = new VaultTransferHarness();
+    }
+
+    function test_proposeAndAcceptVaultTransfer_transfersAllResourcesAtomically() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        world.setVault(clanA, 100e18, 80e18, 30e18, 20e18);
+        world.setVault(clanB, 10e18, 11e18, 12e18, 13e18);
+
+        vm.expectEmit(true, true, true, true, address(world));
+        emit VaultTransferProposed(1, clanA, clanB, 15e18, 16e18, 2e18, 3e18, 10);
+        uint256 proposalId = _propose(clanA, clanB, 15e18, 16e18, 2e18, 3e18, 10);
+
+        VaultTransferProposal memory proposal = world.getOtcVaultTransferProposal(proposalId);
+        assertEq(proposal.from, clanA, "proposal from");
+        assertEq(proposal.to, clanB, "proposal to");
+        assertEq(proposal.wood, 15e18, "proposal wood");
+        assertEq(proposal.wheat, 16e18, "proposal wheat");
+        assertEq(proposal.fish, 2e18, "proposal fish");
+        assertEq(proposal.iron, 3e18, "proposal iron");
+        assertFalse(proposal.accepted, "proposal not accepted");
+
+        vm.expectEmit(true, true, true, true, address(world));
+        emit VaultTransferAccepted(
+            proposalId, clanA, clanB, 15e18, 16e18, 2e18, 3e18, world.getWorldState().currentTick
+        );
+        vm.prank(elderB);
+        world.acceptVaultTransfer(proposalId);
+
+        Clan memory fromAfter = world.getClan(clanA);
+        Clan memory toAfter = world.getClan(clanB);
+        assertEq(fromAfter.vaultWood, 85e18, "from wood debited");
+        assertEq(fromAfter.vaultWheat, 64e18, "from wheat debited");
+        assertEq(fromAfter.vaultFish, 28e18, "from fish debited");
+        assertEq(fromAfter.vaultIron, 17e18, "from iron debited");
+        assertEq(toAfter.vaultWood, 25e18, "to wood credited");
+        assertEq(toAfter.vaultWheat, 27e18, "to wheat credited");
+        assertEq(toAfter.vaultFish, 14e18, "to fish credited");
+        assertEq(toAfter.vaultIron, 16e18, "to iron credited");
+        assertEq(world.getOtcVaultTransferProposal(proposalId).from, 0, "proposal deleted");
+    }
+
+    function test_acceptVaultTransfer_revertsAndLeavesAllResourcesWhenOneResourceInsufficient() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        world.setVault(clanA, 100e18, 100e18, 100e18, 5e18);
+        world.setVault(clanB, 10e18, 10e18, 10e18, 10e18);
+        uint256 proposalId = _propose(clanA, clanB, 20e18, 20e18, 20e18, 5e18, 10);
+        world.setVault(clanA, 100e18, 100e18, 100e18, 4e18);
+
+        vm.expectRevert("ERR_NOT_ENOUGH_RESOURCES");
+        vm.prank(elderB);
+        world.acceptVaultTransfer(proposalId);
+
+        Clan memory fromAfter = world.getClan(clanA);
+        Clan memory toAfter = world.getClan(clanB);
+        assertEq(fromAfter.vaultWood, 100e18, "from wood unchanged");
+        assertEq(fromAfter.vaultWheat, 100e18, "from wheat unchanged");
+        assertEq(fromAfter.vaultFish, 100e18, "from fish unchanged");
+        assertEq(fromAfter.vaultIron, 4e18, "from iron unchanged");
+        assertEq(toAfter.vaultWood, 10e18, "to wood unchanged");
+        assertEq(toAfter.vaultWheat, 10e18, "to wheat unchanged");
+        assertEq(toAfter.vaultFish, 10e18, "to fish unchanged");
+        assertEq(toAfter.vaultIron, 10e18, "to iron unchanged");
+    }
+
+    function test_acceptVaultTransfer_revertsWhenExpired() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        world.setVault(clanA, 100e18, 100e18, 100e18, 100e18);
+        uint256 proposalId = _propose(clanA, clanB, 1e18, 1e18, 1e18, 1e18, world.getWorldState().currentTick);
+        _advanceTick();
+
+        vm.expectRevert("ClanWorld: proposal expired");
+        vm.prank(elderB);
+        world.acceptVaultTransfer(proposalId);
+    }
+
+    function test_cancelVaultTransfer_byProposerBlocksAccept() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        world.setVault(clanA, 100e18, 100e18, 100e18, 100e18);
+        uint256 proposalId = _propose(clanA, clanB, 1e18, 1e18, 1e18, 1e18, 10);
+
+        vm.expectEmit(true, false, false, true, address(world));
+        emit VaultTransferCancelled(proposalId);
+        vm.prank(elderA);
+        world.cancelVaultTransfer(proposalId);
+
+        assertEq(world.getOtcVaultTransferProposal(proposalId).from, 0, "proposal deleted");
+        vm.expectRevert("ClanWorld: proposal not found");
+        vm.prank(elderB);
+        world.acceptVaultTransfer(proposalId);
+    }
+
+    function test_acceptVaultTransfer_settlesPendingUpkeepBeforeDebit() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        world.setVault(clanA, 0, 1e18, 0, 0);
+        world.setVault(clanB, 0, 0, 0, 0);
+        uint256 proposalId = _propose(clanA, clanB, 0, 1e18, 0, 0, 10);
+
+        _advanceTick();
+
+        vm.expectRevert("ERR_NOT_ENOUGH_RESOURCES");
+        vm.prank(elderB);
+        world.acceptVaultTransfer(proposalId);
+
+        assertEq(world.getClan(clanB).vaultWheat, 0, "target not credited on failed accept");
+    }
+
+    function test_proposeVaultTransfer_revertsWhenAllZero() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+
+        vm.expectRevert("ERR_ZERO_AMOUNT");
+        _propose(clanA, clanB, 0, 0, 0, 0, 10);
+    }
+
+    function test_proposeVaultTransfer_revertsWhenSelfTransfer() public {
+        (uint32 clanA,,) = _mintThreeClans();
+
+        vm.expectRevert("ERR_SELF_TRANSFER");
+        _propose(clanA, clanA, 1e18, 0, 0, 0, 10);
+    }
+
+    function test_acceptVaultTransfer_settlesPendingDepositBeforeDebit() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+        uint32 csId = _firstClansman(clanA);
+        uint8 homeRegion = world.getClan(clanA).baseRegion;
+
+        world.setVault(clanA, 0, 100e18, 100e18, 100e18);
+        world.setVault(clanB, 0, 0, 0, 0);
+        world.setCarry(csId, 5e18, 0, 0, 0);
+
+        OrderResult[] memory results = _submitDeposit(clanA, csId, homeRegion);
+        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "deposit accepted");
+
+        uint256 proposalId = _propose(clanA, clanB, 5e18, 0, 0, 0, 10);
+        _advanceTick();
+        _advanceTick();
+
+        vm.prank(elderB);
+        world.acceptVaultTransfer(proposalId);
+
+        assertEq(world.getClan(clanA).vaultWood, 0, "pending deposit settled then debited");
+        assertEq(world.getClan(clanB).vaultWood, 5e18, "target credited from settled deposit");
+    }
+
+    function test_vaultTransfer_twoClanNoInterference() public {
+        (uint32 clanA, uint32 clanB, uint32 clanC) = _mintThreeClans();
+        world.setVault(clanA, 100e18, 100e18, 100e18, 100e18);
+        world.setVault(clanB, 7e18, 8e18, 9e18, 10e18);
+        world.setVault(clanC, 1e18, 2e18, 3e18, 4e18);
+
+        uint256 proposalId = _propose(clanA, clanC, 11e18, 12e18, 13e18, 14e18, 10);
+        vm.prank(elderC);
+        world.acceptVaultTransfer(proposalId);
+
+        Clan memory clanBAfter = world.getClan(clanB);
+        Clan memory clanCAfter = world.getClan(clanC);
+        assertEq(clanBAfter.vaultWood, 7e18, "unrelated wood unchanged");
+        assertEq(clanBAfter.vaultWheat, 8e18, "unrelated wheat unchanged");
+        assertEq(clanBAfter.vaultFish, 9e18, "unrelated fish unchanged");
+        assertEq(clanBAfter.vaultIron, 10e18, "unrelated iron unchanged");
+        assertEq(clanCAfter.vaultWood, 12e18, "target wood credited");
+        assertEq(clanCAfter.vaultWheat, 14e18, "target wheat credited");
+        assertEq(clanCAfter.vaultFish, 16e18, "target fish credited");
+        assertEq(clanCAfter.vaultIron, 18e18, "target iron credited");
+    }
+
+    function _mintThreeClans() internal returns (uint32 clanA, uint32 clanB, uint32 clanC) {
+        vm.prank(elderA);
+        (clanA,) = world.mintClan(elderA);
+        vm.prank(elderB);
+        (clanB,) = world.mintClan(elderB);
+        vm.prank(elderC);
+        (clanC,) = world.mintClan(elderC);
+
+        assertEq(uint8(world.getClan(clanA).clanState), uint8(ClanState.ACTIVE), "clan A alive");
+        assertEq(uint8(world.getClan(clanB).clanState), uint8(ClanState.ACTIVE), "clan B alive");
+        assertEq(uint8(world.getClan(clanC).clanState), uint8(ClanState.ACTIVE), "clan C alive");
+    }
+
+    function _propose(
+        uint32 fromClanId,
+        uint32 toClanId,
+        uint256 wood,
+        uint256 wheat,
+        uint256 fish,
+        uint256 iron,
+        uint64 expiryTick
+    ) internal returns (uint256 proposalId) {
+        vm.prank(elderA);
+        proposalId = world.proposeVaultTransfer(fromClanId, toClanId, wood, wheat, fish, iron, expiryTick);
+    }
+
+    function _firstClansman(uint32 clanId) internal view returns (uint32) {
+        ClanFullView memory view_ = world.getClanFullView(clanId);
+        return view_.clansmen[0].clansman.clansman.clansmanId;
+    }
+
+    function _submitDeposit(uint32 clanId, uint32 clansmanId, uint8 homeRegion)
+        internal
+        returns (OrderResult[] memory)
+    {
+        ClanOrder[] memory orders = new ClanOrder[](1);
+        orders[0] = ClanOrder({
+            clansmanId: clansmanId,
+            gotoRegion: homeRegion,
+            action: ActionType.DepositResources,
+            targetClanId: 0,
+            marketToken: address(0),
+            marketAmount: 0,
+            maxGoldIn: 0
+        });
+
+        vm.prank(elderA);
+        return world.submitClanOrders(clanId, orders);
+    }
+
+    function _advanceTick() internal {
+        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
+        world.heartbeat();
+    }
+}
</stdin>
warning: Codex could not find bubblewrap on PATH. Install bubblewrap with your OS package manager. See the sandbox prerequisites: https://developers.openai.com/codex/concepts/sandboxing#prerequisites. Codex will use the vendored bubblewrap in the meantime.
codex
Reviewing this as a full integrated phase audit, not a spot-check. I’ll parallelize analysis across OTC state machine, token/pool boundary logic, and tests, then synthesize cross-cutting findings with concrete file/line references.
web search: 
web search: 
codex
I’m splitting this into focused sub-reviews now: one on OTC lifecycle invariants, one on treasury/pool/token boundary behavior, and one on test seam coverage/regressions. I’ll merge them into a final severity-ranked report.
collab: SpawnAgent
collab: SpawnAgent
collab: SpawnAgent
codex
While those run, I’m doing my own pass on the same seams so we can cross-check and avoid false positives, especially around type consistency and state-machine transitions.
exec
/bin/bash -lc 'git rev-parse --verify 2bec876 && git status --short' in /home/claude/code/clan-world
exec
/bin/bash -lc 'rg -n "proposeGoldTransfer|acceptGoldTransfer|cancelGoldTransfer|proposeVaultTransfer|acceptVaultTransfer|cancelVaultTransfer|proposeBlueprintTransfer|acceptBlueprintTransfer|cancelBlueprintTransfer|proposeBundledTransfer|acceptBundledTransfer|cancelBundledTransfer|_closeOtcProposal|MAX_OPEN_OTC_PROPOSALS_PER_CLAN" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
exec
/bin/bash -lc "rg -n \"event GoldTransferProposed|event VaultTransferProposed|event BlueprintTransferProposed|event BundledTransferProposed|struct OtcProposal|struct VaultTransferProposal|struct BlueprintTransferProposal|struct BundledTransferProposal|function proposeGoldTransfer\\(|function proposeVaultTransfer\\(|function proposeBlueprintTransfer\\(|function proposeBundledTransfer\\(\" packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
 exited 1 in 0ms:
 exited 1 in 110ms:
 succeeded in 181ms:
?? docs/planning/gold-bridge-monorepo.zip
?? docs/reviews/pr153-review-claude-opus.md
?? docs/reviews/pr153-review-codex-5-3.md
?? docs/reviews/pr194-r2-codereview-codex-5-4.md
?? docs/reviews/pr194-r2-codereview-codex-5-5.md
?? docs/reviews/pr194-r2-codereview-gemini-3-1-pro.md
?? docs/reviews/pr194-r2-codereview-opus-4-7.md
?? docs/reviews/pr194-r2-synthesis.md
?? docs/reviews/pr198-r2-codereview-codex-5-4.md
?? docs/reviews/pr198-r2-codereview-codex-5-5.md
?? docs/reviews/pr198-r2-codereview-opus-4-7.md
?? docs/reviews/pr198-r2-synthesis.md
?? docs/reviews/pr199-codereview-codex-5-4.md
?? docs/reviews/pr199-codereview-codex-5-5.md
?? docs/reviews/pr199-codereview-opus-4-7.md
?? docs/reviews/pr199-r3-codereview-codex-5-4.md
?? docs/reviews/pr199-r3-codereview-codex-5-5.md
?? docs/reviews/pr199-r3-codereview-gemini-3-1-pro.md
?? docs/reviews/pr199-r3-codereview-opus-4-6.md
?? docs/reviews/pr199-r3-codereview-opus-4-7.md
?? docs/reviews/pr199-r3-synthesis.md
?? docs/reviews/pr199-synthesis.md
?? docs/reviews/pr200-codereview-codex-5-4.md
?? docs/reviews/pr200-codereview-codex-5-5.md
?? docs/reviews/pr200-codereview-opus-4-7.md
?? docs/reviews/pr200-r2-codereview-codex-5-3.md
?? docs/reviews/pr200-r2-codereview-codex-5-4.md
?? docs/reviews/pr200-r2-codereview-codex-5-5.md
?? docs/reviews/pr200-r2-codereview-gemini-3-1-pro.md
?? docs/reviews/pr200-r2-codereview-opus-4-6.md
?? docs/reviews/pr200-r2-codereview-opus-4-7.md
?? docs/reviews/pr200-synthesis.md
?? docs/reviews/pr250-r2-codereview-codex-5-5.md
?? docs/reviews/pr250-r2-codereview-opus-4-7.md
?? docs/reviews/pr250-r2-synthesis-update.md
?? docs/reviews/pr250-r2-synthesis.md
?? docs/reviews/pr250-r3-codereview-codex-5-3.md
?? docs/reviews/pr250-r3-codereview-codex-5-4.md
?? docs/reviews/pr250-r3-codereview-codex-5-5.md
?? docs/reviews/pr250-r3-codereview-gemini-3-1-pro.md
?? docs/reviews/pr250-r3-codereview-opus-4-6.md
?? docs/reviews/pr250-r3-codereview-opus-4-7.md
?? docs/reviews/r2-phase10/
?? docs/reviews/r2-phase6/
?? docs/reviews/r2/

codex
I confirmed the target commit and saw a dirty worktree with unrelated untracked review docs, which I’m ignoring. Two search commands failed due shell quoting, so I’m rerunning them with safer patterns to continue line-precise tracing.
exec
/bin/bash -lc 'rg -n "proposeGoldTransfer|acceptGoldTransfer|cancelGoldTransfer|proposeVaultTransfer|acceptVaultTransfer|cancelVaultTransfer|proposeBlueprintTransfer|acceptBlueprintTransfer|cancelBlueprintTransfer|proposeBundledTransfer|acceptBundledTransfer|cancelBundledTransfer|_closeOtcProposal|MAX_OPEN_OTC_PROPOSALS_PER_CLAN" /home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
 exited 1 in 0ms:
exec
/bin/bash -lc "rg -n \"event GoldTransferProposed|event VaultTransferProposed|event BlueprintTransferProposed|event BundledTransferProposed|struct OtcProposal|struct VaultTransferProposal|struct BlueprintTransferProposal|struct BundledTransferProposal|function proposeGoldTransfer\\(|function proposeVaultTransfer\\(|function proposeBlueprintTransfer\\(|function proposeBundledTransfer\\(\" /home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
 exited 1 in 0ms:
exec
/bin/bash -lc "nl -ba /home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol | sed -n '1710,2135p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1710	
  1711	        _treasury.woodGoldPool = pools[0];
  1712	        _treasury.wheatGoldPool = pools[1];
  1713	        _treasury.fishGoldPool = pools[2];
  1714	        _treasury.ironGoldPool = pools[3];
  1715	    }
  1716	
  1717	    /// @notice Owner-only. Seeds the four Unicorn Town AMM pools.
  1718	    function seedPools(PoolSeedConfig calldata cfg) external override nonReentrant {
  1719	        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");
  1720	        require(!_treasury.poolsSeeded, "ClanWorld: pools already seeded");
  1721	        require(_treasury.woodToken != address(0), "ClanWorld: treasury not init");
  1722	
  1723	        StubPool(_treasury.woodGoldPool).seed(cfg.woodSeed, cfg.goldSeedForWood);
  1724	        StubPool(_treasury.wheatGoldPool).seed(cfg.wheatSeed, cfg.goldSeedForWheat);
  1725	        StubPool(_treasury.fishGoldPool).seed(cfg.fishSeed, cfg.goldSeedForFish);
  1726	        StubPool(_treasury.ironGoldPool).seed(cfg.ironSeed, cfg.goldSeedForIron);
  1727	
  1728	        _treasury.poolsSeeded = true;
  1729	
  1730	        emit PoolsSeeded(
  1731	            _treasury.woodGoldPool, _treasury.wheatGoldPool, _treasury.fishGoldPool, _treasury.ironGoldPool
  1732	        );
  1733	    }
  1734	
  1735	    // =========================================================================
  1736	    // OTC TRANSFERS
  1737	    // =========================================================================
  1738	
  1739	    function transferGold(uint32, uint32, uint256) external pure override {
  1740	        revert("OTC transfers not implemented");
  1741	    }
  1742	
  1743	    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
  1744	        revert("OTC transfers not implemented");
  1745	    }
  1746	
  1747	    function transferBlueprint(uint32, uint32, uint256) external pure override {
  1748	        revert("OTC transfers not implemented");
  1749	    }
  1750	
  1751	    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
  1752	        external
  1753	        pure
  1754	        override
  1755	    {
  1756	        revert("OTC transfers not implemented");
  1757	    }
  1758	
  1759	    // =========================================================================
  1760	    // RAW READ GETTERS
  1761	    // =========================================================================
  1762	
  1763	    function getWorldState() external view override returns (WorldState memory) {
  1764	        return _world;
  1765	    }
  1766	
  1767	    function getTreasuryState() external view override returns (TreasuryState memory) {
  1768	        return _treasury;
  1769	    }
  1770	
  1771	    function getClan(uint32 clanId) external view override returns (Clan memory) {
  1772	        return _clans[clanId];
  1773	    }
  1774	
  1775	    function getClansman(uint32 clansmanId) external view override returns (Clansman memory) {
  1776	        return _clansmen[clansmanId];
  1777	    }
  1778	
  1779	    function getActiveMission(uint32 clansmanId) external view override returns (Mission memory) {
  1780	        return _missions[clansmanId];
  1781	    }
  1782	
  1783	    function getMissionTiming(uint32 clanId, uint32 clansmanId)
  1784	        external
  1785	        view
  1786	        override
  1787	        returns (uint64 submitted, uint64 executes, uint64 settles)
  1788	    {
  1789	        Mission memory m = _missions[clansmanId];
  1790	        if (!m.active || _clansmen[clansmanId].clanId != clanId) {
  1791	            return (0, 0, 0);
  1792	        }
  1793	        return (m.submittedAtTick, m.executesAtTick, m.settlesAtTick);
  1794	    }
  1795	
  1796	    function getActionDuration(ActionType action) public pure override returns (uint64) {
  1797	        if (
  1798	            action == ActionType.ChopWood || action == ActionType.MineIron || action == ActionType.FishDocks
  1799	                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
  1800	        ) {
  1801	            return 4;
  1802	        }
  1803	
  1804	        if (
  1805	            action == ActionType.DepositResources || action == ActionType.BuildWall || action == ActionType.UpgradeBase
  1806	                || action == ActionType.UpgradeMonument || action == ActionType.MarketBuy
  1807	                || action == ActionType.MarketSell
  1808	        ) {
  1809	            return 1;
  1810	        }
  1811	
  1812	        return 0;
  1813	    }
  1814	
  1815	    function getTravelTicks(uint8 fromRegion, uint8 toRegion) external pure override returns (uint64) {
  1816	        return uint64(_travelTicks(fromRegion, toRegion));
  1817	    }
  1818	
  1819	    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
  1820	        return BanditTroop({
  1821	            banditId: 0,
  1822	            state: BanditState.NONE,
  1823	            currentRegion: 0,
  1824	            attackAttemptsMade: 0,
  1825	            stateEnteredTick: 0,
  1826	            nextActionTick: 0,
  1827	            tier: 0,
  1828	            attackPower: 0,
  1829	            carryWood: 0,
  1830	            carryIron: 0,
  1831	            carryWheat: 0,
  1832	            carryFish: 0
  1833	        });
  1834	    }
  1835	
  1836	    function getWheatPlots(uint32 clanId)
  1837	        external
  1838	        view
  1839	        override
  1840	        returns (WheatPlot memory west, WheatPlot memory east)
  1841	    {
  1842	        west = _wheatPlots[clanId][0];
  1843	        east = _wheatPlots[clanId][1];
  1844	    }
  1845	
  1846	    function getScheduledMarketActionsForTick(uint64 tick)
  1847	        external
  1848	        view
  1849	        override
  1850	        returns (ScheduledMarketAction[] memory)
  1851	    {
  1852	        return _scheduledMarketActions[tick];
  1853	    }
  1854	
  1855	    function getActiveDefenders(uint32 targetClanId) external view override returns (uint32[] memory clansmanIds) {
  1856	        uint32[] storage defendingClans = _defendingClansByRegion[_clans[targetClanId].baseRegion];
  1857	        uint256 count = 0;
  1858	
  1859	        for (uint256 i = 0; i < defendingClans.length; i++) {
  1860	            uint32[] storage clanClansmen = _clanClansmanIds[defendingClans[i]];
  1861	            for (uint256 j = 0; j < clanClansmen.length; j++) {
  1862	                Mission storage mission = _missions[clanClansmen[j]];
  1863	                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
  1864	                    count++;
  1865	                }
  1866	            }
  1867	        }
  1868	
  1869	        clansmanIds = new uint32[](count);
  1870	        uint256 out = 0;
  1871	        for (uint256 i = 0; i < defendingClans.length; i++) {
  1872	            uint32[] storage clanClansmen = _clanClansmanIds[defendingClans[i]];
  1873	            for (uint256 j = 0; j < clanClansmen.length; j++) {
  1874	                uint32 clansmanId = clanClansmen[j];
  1875	                Mission storage mission = _missions[clansmanId];
  1876	                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
  1877	                    clansmanIds[out++] = clansmanId;
  1878	                }
  1879	            }
  1880	        }
  1881	    }
  1882	
  1883	    function getDefendingClans(uint8 region) external view override returns (uint32[] memory) {
  1884	        return _defendingClansByRegion[region];
  1885	    }
  1886	
  1887	    // =========================================================================
  1888	    // DERIVED READ GETTERS (read-only, no storage mutation)
  1889	    // =========================================================================
  1890	
  1891	    /// @dev Returns last-settled state; starvation check uses currentTick (live).
  1892	    ///      Carry amounts lag until next settleClan().
  1893	    function getDerivedClanState(uint32 clanId) external view override returns (DerivedClanState memory) {
  1894	        Clan memory clan = _clans[clanId];
  1895	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
  1896	        uint256 lootVal = _lootValueRaw(clan);
  1897	        return
  1898	            DerivedClanState({clan: clan, isStarving: starving, lootValue: lootVal, derivedAtTick: _world.currentTick});
  1899	    }
  1900	
  1901	    function getDerivedClansmanState(uint32 clansmanId) external view override returns (DerivedClansmanState memory) {
  1902	        Clansman memory cs = _clansmen[clansmanId];
  1903	        Mission memory m = _missions[clansmanId];
  1904	        uint8 effectiveRegion = cs.currentRegion;
  1905	        if (cs.state == ClansmanState.TRAVELING && m.active) {
  1906	            // Simplified: if past arrivalTick, they're at target; else at start
  1907	            if (_world.currentTick >= m.arrivalTick) {
  1908	                effectiveRegion = m.targetRegion;
  1909	            } else {
  1910	                effectiveRegion = m.startRegion;
  1911	            }
  1912	        }
  1913	        return DerivedClansmanState({
  1914	            clansman: cs, activeMission: m, effectiveRegion: effectiveRegion, derivedAtTick: _world.currentTick
  1915	        });
  1916	    }
  1917	
  1918	    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
  1919	        return 0; // Phase 3
  1920	    }
  1921	
  1922	    function quoteTravel(uint8 srcRegion, uint8 dstRegion)
  1923	        external
  1924	        pure
  1925	        override
  1926	        returns (uint8 travelTicks, bytes8 path)
  1927	    {
  1928	        if (srcRegion > 8 || dstRegion > 8) {
  1929	            return (0, bytes8(0));
  1930	        }
  1931	        if (srcRegion == dstRegion || srcRegion == 0 || dstRegion == 0) {
  1932	            travelTicks = 0;
  1933	            path = bytes8(uint64(srcRegion) << 56);
  1934	            return (travelTicks, path);
  1935	        }
  1936	        travelTicks = _travelTicks(srcRegion, dstRegion);
  1937	        path = _buildPath(srcRegion, dstRegion);
  1938	    }
  1939	
  1940	    function quoteLootValueRaw(uint32 clanId) external view override returns (uint256) {
  1941	        return _lootValueRaw(_clans[clanId]);
  1942	    }
  1943	
  1944	    function quoteLootValueSettled(uint32 clanId) external view override returns (uint256) {
  1945	        // Phase 1: return raw value (no simulation)
  1946	        return _lootValueRaw(_clans[clanId]);
  1947	    }
  1948	
  1949	    /// @dev Compute loot value per v4 spec §6.9: wood=1, wheat=1, fish=2, iron=4 points.
  1950	    function _lootValueRaw(Clan memory clan) internal pure returns (uint256) {
  1951	        return clan.vaultWood + clan.vaultWheat + clan.vaultFish * 2 + clan.vaultIron * 4;
  1952	    }
  1953	
  1954	    // =========================================================================
  1955	    // UI INDEXER AGGREGATOR GETTERS
  1956	    // =========================================================================
  1957	
  1958	    /// @dev Leaderboard loot values reflect vault contents only (last-settled state).
  1959	    ///      Carry amounts not included. Full view-only settlement deferred.
  1960	    ///      View function — no gas cost for off-chain indexer/UI reads.
  1961	    ///      Iterates all clans. Canonical game cap: ≤12 clans, ≤4 clansmen each = ≤48 iterations.
  1962	    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
  1963	        LeaderboardEntry[] memory lb = new LeaderboardEntry[](_allClanIds.length);
  1964	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  1965	            uint32 cid = _allClanIds[i];
  1966	            Clan storage clan = _clans[cid];
  1967	            lb[i] = LeaderboardEntry({
  1968	                clanId: cid,
  1969	                owner: clan.owner,
  1970	                monumentLevel: clan.monumentLevel,
  1971	                baseLevel: clan.baseLevel,
  1972	                wallLevel: clan.wallLevel,
  1973	                livingClansmen: clan.livingClansmen,
  1974	                state: clan.clanState,
  1975	                lootValue: _lootValueRaw(clan)
  1976	            });
  1977	        }
  1978	
  1979	        return WorldSnapshot({
  1980	            currentTick: _world.currentTick,
  1981	            seasonStartTick: _world.seasonStartTick,
  1982	            seasonEndTick: _world.seasonEndTick,
  1983	            seasonFinalized: _world.seasonFinalized,
  1984	            currentSeasonNumber: _world.currentSeasonNumber,
  1985	            nextHeartbeatAtTick: _world.nextHeartbeatAtTick,
  1986	            winterActive: _world.winterActive,
  1987	            winterStartsAtTick: _world.winterStartsAtTick,
  1988	            winterEndsAtTick: _world.winterEndsAtTick,
  1989	            activeBanditId: _world.activeBanditId,
  1990	            currentTickSeed: _world.currentTickSeed,
  1991	            leaderboard: lb
  1992	        });
  1993	    }
  1994	
  1995	    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
  1996	    ///      current; carry amounts and wheat progress lag until next settleClan() call.
  1997	    ///      Full view-only settlement simulation is deferred (tracked issue).
  1998	    function getClanFullView(uint32 clanId) external view override returns (ClanFullView memory) {
  1999	        Clan storage clan = _clans[clanId];
  2000	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
  2001	        uint256 lootVal = _lootValueRaw(clan);
  2002	
  2003	        DerivedClanState memory derivedClan =
  2004	            DerivedClanState({clan: clan, isStarving: starving, lootValue: lootVal, derivedAtTick: _world.currentTick});
  2005	
  2006	        uint32[] storage csIds = _clanClansmanIds[clanId];
  2007	        ClansmanFullView[] memory clansmen = new ClansmanFullView[](csIds.length);
  2008	        for (uint256 i = 0; i < csIds.length; i++) {
  2009	            uint32 csId = csIds[i];
  2010	            Clansman memory cs = _clansmen[csId];
  2011	            Mission memory m = _missions[csId];
  2012	            uint8 effRegion = cs.currentRegion;
  2013	            if (cs.state == ClansmanState.TRAVELING && m.active) {
  2014	                effRegion = _world.currentTick >= m.arrivalTick ? m.targetRegion : m.startRegion;
  2015	            }
  2016	            DerivedClansmanState memory dcs = DerivedClansmanState({
  2017	                clansman: cs, activeMission: m, effectiveRegion: effRegion, derivedAtTick: _world.currentTick
  2018	            });
  2019	            clansmen[i] = ClansmanFullView({clansman: dcs, activeMission: m});
  2020	        }
  2021	
  2022	        // Find if any of this clan's clansmen is defending a home region.
  2023	        uint32 thisClanDefendingBaseId = 0;
  2024	        for (uint256 i = 0; i < csIds.length; i++) {
  2025	            uint8 region = _clansmanDefendingRegion[csIds[i]];
  2026	            if (region != 0) {
  2027	                thisClanDefendingBaseId = region;
  2028	                break;
  2029	            }
  2030	        }
  2031	
  2032	        return ClanFullView({
  2033	            clan: derivedClan,
  2034	            clansmen: clansmen,
  2035	            westPlot: _wheatPlots[clanId][0],
  2036	            eastPlot: _wheatPlots[clanId][1],
  2037	            incomingDefenderIds: _defendingClansByRegion[clan.baseRegion],
  2038	            thisClanDefendingBaseId: thisClanDefendingBaseId
  2039	        });
  2040	    }
  2041	
  2042	    function getMarketState() external view override returns (MarketState memory) {
  2043	        return MarketState({
  2044	            wood: _poolReserves(_treasury.woodToken, _treasury.woodGoldPool),
  2045	            wheat: _poolReserves(_treasury.wheatToken, _treasury.wheatGoldPool),
  2046	            fish: _poolReserves(_treasury.fishToken, _treasury.fishGoldPool),
  2047	            iron: _poolReserves(_treasury.ironToken, _treasury.ironGoldPool),
  2048	            currentTick: _world.currentTick,
  2049	            currentTickQueue: _scheduledMarketActions[_world.currentTick],
  2050	            nextTickQueue: _scheduledMarketActions[_world.currentTick + 1]
  2051	        });
  2052	    }
  2053	
  2054	    function _poolReserves(address resourceToken, address poolAddr) internal view returns (PoolReserves memory pr) {
  2055	        pr.resourceToken = resourceToken;
  2056	        if (poolAddr == address(0) || resourceToken == address(0)) {
  2057	            return pr;
  2058	        }
  2059	        (uint256 rA, uint256 rB) = StubPool(poolAddr).getReserves();
  2060	        pr.resourceReserve = rA;
  2061	        pr.goldReserve = rB;
  2062	        pr.spotPriceGoldPerResource = rA > 0 ? (rB * 1e18) / rA : 0;
  2063	    }
  2064	
  2065	    function getActiveBanditView() external pure override returns (ActiveBanditView memory) {
  2066	        return ActiveBanditView({
  2067	            exists: false,
  2068	            banditId: 0,
  2069	            state: BanditState.NONE,
  2070	            currentRegion: 0,
  2071	            attackAttemptsMade: 0,
  2072	            maxAttemptsRemaining: 0,
  2073	            stateEnteredTick: 0,
  2074	            nextActionTick: 0,
  2075	            tier: 0,
  2076	            attackPower: 0,
  2077	            carryWood: 0,
  2078	            carryIron: 0,
  2079	            carryWheat: 0,
  2080	            carryFish: 0,
  2081	            projectedTargetClanId: 0,
  2082	            projectedTargetLootValue: 0
  2083	        });
  2084	    }
  2085	
  2086	    /// @dev View function — no gas cost for off-chain indexer/UI reads.
  2087	    ///      Iterates all clans. Canonical game cap: ≤12 clans, ≤4 clansmen each = ≤48 iterations.
  2088	    function getRegionPopulation(uint8 region) external view override returns (RegionOccupant[] memory) {
  2089	        // Count matching occupants first
  2090	        uint256 count = 0;
  2091	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  2092	            uint32 cid = _allClanIds[i];
  2093	            uint32[] storage csIds = _clanClansmanIds[cid];
  2094	            for (uint256 j = 0; j < csIds.length; j++) {
  2095	                Clansman storage cs = _clansmen[csIds[j]];
  2096	                if (cs.state != ClansmanState.DEAD && cs.currentRegion == region) {
  2097	                    count++;
  2098	                }
  2099	            }
  2100	        }
  2101	
  2102	        RegionOccupant[] memory occupants = new RegionOccupant[](count);
  2103	        uint256 idx = 0;
  2104	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  2105	            uint32 cid = _allClanIds[i];
  2106	            uint32[] storage csIds = _clanClansmanIds[cid];
  2107	            for (uint256 j = 0; j < csIds.length; j++) {
  2108	                uint32 csId = csIds[j];
  2109	                Clansman storage cs = _clansmen[csId];
  2110	                if (cs.state != ClansmanState.DEAD && cs.currentRegion == region) {
  2111	                    Mission storage m = _missions[csId];
  2112	                    occupants[idx++] = RegionOccupant({
  2113	                        clansmanId: csId,
  2114	                        clanId: cid,
  2115	                        state: cs.state,
  2116	                        currentAction: m.active ? m.action : ActionType.None,
  2117	                        missionNonce: cs.lastMissionNonce
  2118	                    });
  2119	                }
  2120	            }
  2121	        }
  2122	        return occupants;
  2123	    }
  2124	}

codex
I found the local working tree file differs from the provided PR diff context, so I’m pivoting to inspect the exact `2bec876` blob contents directly via `git show` to keep line references accurate to the reviewed head.
exec
/bin/bash -lc "git show --name-only --pretty=format: 2bec876 | sed '/"'^$/d'"'" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/abi/IClanWorld.json
packages/contracts/src/ClanWorld.sol
packages/contracts/src/IClanWorld.sol
packages/contracts/test/BlueprintTransferOtc.t.sol
packages/contracts/test/BundledTransferOtc.t.sol
packages/contracts/test/ClanWorld.t.sol
packages/contracts/test/DeadClanOtc.t.sol
packages/contracts/test/GoldTransferOtc.t.sol
packages/contracts/test/VaultTransferOtc.t.sol

exec
/bin/bash -lc "git show 2bec876:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1650,2350p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1650	                return StatusCode.ERR_INVALID_REGION;
  1651	            }
  1652	        }
  1653	        // FishDeepSea: must go to DeepSea
  1654	        if (action == ActionType.FishDeepSea) {
  1655	            if (gotoRegion != ClanWorldConstants.REGION_DEEP_SEA) return StatusCode.ERR_INVALID_REGION;
  1656	        }
  1657	        // HarvestWheat: must go to WestFarms or EastFarms
  1658	        if (action == ActionType.HarvestWheat) {
  1659	            if (
  1660	                gotoRegion != ClanWorldConstants.REGION_WEST_FARMS && gotoRegion != ClanWorldConstants.REGION_EAST_FARMS
  1661	            ) {
  1662	                return StatusCode.ERR_INVALID_REGION;
  1663	            }
  1664	        }
  1665	
  1666	        if (action == ActionType.DefendBase) {
  1667	            return _validateDefendBaseOrder(clan, order, gotoRegion);
  1668	        }
  1669	
  1670	        // MarketBuy/MarketSell: must target Unicorn Town
  1671	        if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
  1672	            if (_treasury.woodToken == address(0)) return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
  1673	            if (gotoRegion != ClanWorldConstants.REGION_UNICORN_TOWN) {
  1674	                return StatusCode.ERR_INVALID_REGION;
  1675	            }
  1676	            if (order.marketAmount == 0) return StatusCode.ERR_MARKET_ZERO_AMOUNT;
  1677	            // Validate token is a supported resource token (not gold itself)
  1678	            address tok = order.marketToken;
  1679	            if (tok == address(0) || tok == _treasury.goldToken) {
  1680	                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
  1681	            }
  1682	            if (
  1683	                tok != _treasury.woodToken && tok != _treasury.ironToken && tok != _treasury.wheatToken
  1684	                    && tok != _treasury.fishToken
  1685	            ) {
  1686	                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
  1687	            }
  1688	            // Market orders are always enqueued for the arrivalTick FIFO queue.
  1689	            // _resolveAction records mission completion but does not execute any swap.
  1690	            if (cs.currentRegion == ClanWorldConstants.REGION_UNICORN_TOWN && cs.state == ClansmanState.WAITING) {
  1691	                // Already at Unicorn Town — arrivalTick == currentTick, queued for next heartbeat.
  1692	            }
  1693	        }
  1694	
  1695	        cs; // suppress unused warning
  1696	        return StatusCode.OK;
  1697	    }
  1698	
  1699	    function _validateDefendBaseOrder(Clan storage clan, ClanOrder calldata order, uint8 gotoRegion)
  1700	        internal
  1701	        view
  1702	        returns (StatusCode)
  1703	    {
  1704	        if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
  1705	        if (order.targetClanId != 0 && order.targetClanId != clan.clanId) return StatusCode.ERR_INVALID_TARGET;
  1706	        return StatusCode.OK;
  1707	    }
  1708	
  1709	    // =========================================================================
  1710	    // TREASURY / POOL SEEDING
  1711	    // =========================================================================
  1712	
  1713	    /// @notice One-time treasury initialization: register token and pool addresses.
  1714	    ///         Must be called before seedPools. Callable only once.
  1715	    function initTreasury(address[6] calldata tokens, address[4] calldata pools) external override nonReentrant {
  1716	        require(!_treasury.poolsSeeded && _treasury.woodToken == address(0), "ClanWorld: treasury already init");
  1717	        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");
  1718	
  1719	        _treasury.woodToken = tokens[0];
  1720	        _treasury.ironToken = tokens[1];
  1721	        _treasury.wheatToken = tokens[2];
  1722	        _treasury.fishToken = tokens[3];
  1723	        _treasury.goldToken = tokens[4];
  1724	        _treasury.blueprintToken = tokens[5];
  1725	
  1726	        _treasury.woodGoldPool = pools[0];
  1727	        _treasury.wheatGoldPool = pools[1];
  1728	        _treasury.fishGoldPool = pools[2];
  1729	        _treasury.ironGoldPool = pools[3];
  1730	    }
  1731	
  1732	    /// @notice Owner-only. Seeds the four Unicorn Town AMM pools.
  1733	    function seedPools(PoolSeedConfig calldata cfg) external override nonReentrant {
  1734	        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");
  1735	        require(!_treasury.poolsSeeded, "ClanWorld: pools already seeded");
  1736	        require(_treasury.woodToken != address(0), "ClanWorld: treasury not init");
  1737	
  1738	        _transferSeed(_treasury.woodToken, _treasury.woodGoldPool, cfg.woodSeed);
  1739	        _transferSeed(_treasury.goldToken, _treasury.woodGoldPool, cfg.goldSeedForWood);
  1740	        _transferSeed(_treasury.wheatToken, _treasury.wheatGoldPool, cfg.wheatSeed);
  1741	        _transferSeed(_treasury.goldToken, _treasury.wheatGoldPool, cfg.goldSeedForWheat);
  1742	        _transferSeed(_treasury.fishToken, _treasury.fishGoldPool, cfg.fishSeed);
  1743	        _transferSeed(_treasury.goldToken, _treasury.fishGoldPool, cfg.goldSeedForFish);
  1744	        _transferSeed(_treasury.ironToken, _treasury.ironGoldPool, cfg.ironSeed);
  1745	        _transferSeed(_treasury.goldToken, _treasury.ironGoldPool, cfg.goldSeedForIron);
  1746	
  1747	        StubPool(_treasury.woodGoldPool).seed(cfg.woodSeed, cfg.goldSeedForWood);
  1748	        StubPool(_treasury.wheatGoldPool).seed(cfg.wheatSeed, cfg.goldSeedForWheat);
  1749	        StubPool(_treasury.fishGoldPool).seed(cfg.fishSeed, cfg.goldSeedForFish);
  1750	        StubPool(_treasury.ironGoldPool).seed(cfg.ironSeed, cfg.goldSeedForIron);
  1751	
  1752	        _treasury.poolsSeeded = true;
  1753	
  1754	        emit PoolsSeeded(
  1755	            _treasury.woodGoldPool, _treasury.wheatGoldPool, _treasury.fishGoldPool, _treasury.ironGoldPool
  1756	        );
  1757	    }
  1758	
  1759	    function _transferSeed(address token, address pool, uint256 amount) internal {
  1760	        require(token != address(0) && pool != address(0), "ClanWorld: treasury not init");
  1761	        require(MinimalERC20(token).transferFrom(msg.sender, pool, amount), "ClanWorld: seed transfer failed");
  1762	    }
  1763	
  1764	    // =========================================================================
  1765	    // OTC TRANSFERS
  1766	    // =========================================================================
  1767	
  1768	    function proposeGoldTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint256 expiryTick)
  1769	        external
  1770	        override
  1771	        nonReentrant
  1772	        returns (uint256 proposalId)
  1773	    {
  1774	        Clan storage fromClan = _clans[fromClanId];
  1775	        require(fromClan.clanId != 0, "ClanWorld: clan not found");
  1776	        require(fromClanId != toClanId, "ERR_SELF_TRANSFER");
  1777	        require(amount > 0, "ERR_ZERO_AMOUNT");
  1778	        require(_openOtcProposalsByClan[fromClanId] < MAX_OPEN_OTC_PROPOSALS_PER_CLAN, "ERR_OTC_CAP");
  1779	        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1780	        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
  1781	        require(_clans[toClanId].clanId != 0, "ClanWorld: target clan not found");
  1782	        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1783	        require(expiryTick <= type(uint64).max, "ClanWorld: expiry overflow");
  1784	
  1785	        proposalId = _nextOtcProposalId++;
  1786	        _openOtcProposalsByClan[fromClanId]++;
  1787	        _otcGoldProposals[proposalId] = OtcProposal({
  1788	            from: fromClanId,
  1789	            to: toClanId,
  1790	            amount: amount,
  1791	            expiryTick: uint64(expiryTick),
  1792	            accepted: false,
  1793	            cancelled: false
  1794	        });
  1795	
  1796	        emit GoldTransferProposed(proposalId, fromClanId, toClanId, amount, expiryTick);
  1797	    }
  1798	
  1799	    function acceptGoldTransfer(uint256 proposalId) external override nonReentrant {
  1800	        OtcProposal storage proposal = _otcGoldProposals[proposalId];
  1801	        require(proposal.from != 0, "ClanWorld: proposal not found");
  1802	        require(!proposal.accepted, "ClanWorld: proposal accepted");
  1803	        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
  1804	        require(_world.currentTick <= proposal.expiryTick, "ClanWorld: proposal expired");
  1805	
  1806	        uint32 fromClanId = proposal.from;
  1807	        uint32 toClanId = proposal.to;
  1808	        uint256 amount = proposal.amount;
  1809	        _settleClan(fromClanId);
  1810	        _settleClan(toClanId);
  1811	
  1812	        Clan storage fromClan = _clans[proposal.from];
  1813	        Clan storage toClan = _clans[proposal.to];
  1814	        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1815	        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1816	        require(toClan.owner == msg.sender, "ClanWorld: not clan owner");
  1817	        if (fromClan.goldBalance < amount) revert("ERR_NOT_ENOUGH_GOLD");
  1818	
  1819	        fromClan.goldBalance -= amount;
  1820	        toClan.goldBalance += amount;
  1821	        _closeOtcProposal(fromClanId);
  1822	        delete _otcGoldProposals[proposalId];
  1823	
  1824	        emit GoldTransferAccepted(proposalId, fromClanId, toClanId, amount, _world.currentTick);
  1825	    }
  1826	
  1827	    function cancelGoldTransfer(uint256 proposalId) external override nonReentrant {
  1828	        OtcProposal storage proposal = _otcGoldProposals[proposalId];
  1829	        require(proposal.from != 0, "ClanWorld: proposal not found");
  1830	        require(!proposal.accepted, "ClanWorld: proposal accepted");
  1831	        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
  1832	
  1833	        Clan storage fromClan = _clans[proposal.from];
  1834	        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
  1835	
  1836	        uint32 fromClanId = proposal.from;
  1837	        _closeOtcProposal(fromClanId);
  1838	        delete _otcGoldProposals[proposalId];
  1839	        emit GoldTransferCancelled(proposalId);
  1840	    }
  1841	
  1842	    function proposeVaultTransfer(
  1843	        uint32 fromClanId,
  1844	        uint32 toClanId,
  1845	        uint256 woodAmt,
  1846	        uint256 wheatAmt,
  1847	        uint256 fishAmt,
  1848	        uint256 ironAmt,
  1849	        uint64 expiryTick
  1850	    ) external override nonReentrant returns (uint256 proposalId) {
  1851	        Clan storage fromClan = _clans[fromClanId];
  1852	        require(fromClan.clanId != 0, "ClanWorld: clan not found");
  1853	        require(fromClanId != toClanId, "ERR_SELF_TRANSFER");
  1854	        require(woodAmt > 0 || wheatAmt > 0 || fishAmt > 0 || ironAmt > 0, "ERR_ZERO_AMOUNT");
  1855	        require(_openOtcProposalsByClan[fromClanId] < MAX_OPEN_OTC_PROPOSALS_PER_CLAN, "ERR_OTC_CAP");
  1856	        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1857	        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
  1858	        require(_clans[toClanId].clanId != 0, "ClanWorld: target clan not found");
  1859	        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1860	
  1861	        proposalId = _nextOtcProposalId++;
  1862	        _openOtcProposalsByClan[fromClanId]++;
  1863	        _otcVaultTransferProposals[proposalId] = VaultTransferProposal({
  1864	            from: fromClanId,
  1865	            to: toClanId,
  1866	            wood: woodAmt,
  1867	            wheat: wheatAmt,
  1868	            fish: fishAmt,
  1869	            iron: ironAmt,
  1870	            expiryTick: expiryTick,
  1871	            accepted: false,
  1872	            cancelled: false
  1873	        });
  1874	
  1875	        emit VaultTransferProposed(proposalId, fromClanId, toClanId, woodAmt, wheatAmt, fishAmt, ironAmt, expiryTick);
  1876	    }
  1877	
  1878	    function acceptVaultTransfer(uint256 proposalId) external override nonReentrant {
  1879	        VaultTransferProposal storage proposal = _otcVaultTransferProposals[proposalId];
  1880	        require(proposal.from != 0, "ClanWorld: proposal not found");
  1881	        require(!proposal.accepted, "ClanWorld: proposal accepted");
  1882	        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
  1883	        require(_world.currentTick <= proposal.expiryTick, "ClanWorld: proposal expired");
  1884	
  1885	        uint32 fromClanId = proposal.from;
  1886	        uint32 toClanId = proposal.to;
  1887	        uint256 wood = proposal.wood;
  1888	        uint256 wheat = proposal.wheat;
  1889	        uint256 fish = proposal.fish;
  1890	        uint256 iron = proposal.iron;
  1891	        _settleClan(fromClanId);
  1892	        _settleClan(toClanId);
  1893	
  1894	        Clan storage fromClan = _clans[fromClanId];
  1895	        Clan storage toClan = _clans[toClanId];
  1896	        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1897	        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1898	        require(toClan.owner == msg.sender, "ClanWorld: not clan owner");
  1899	        if (!_hasVaultResources(fromClan, wood, wheat, fish, iron)) {
  1900	            revert("ERR_NOT_ENOUGH_RESOURCES");
  1901	        }
  1902	
  1903	        fromClan.vaultWood -= wood;
  1904	        fromClan.vaultWheat -= wheat;
  1905	        fromClan.vaultFish -= fish;
  1906	        fromClan.vaultIron -= iron;
  1907	        toClan.vaultWood += wood;
  1908	        toClan.vaultWheat += wheat;
  1909	        toClan.vaultFish += fish;
  1910	        toClan.vaultIron += iron;
  1911	        _closeOtcProposal(fromClanId);
  1912	        delete _otcVaultTransferProposals[proposalId];
  1913	
  1914	        emit VaultTransferAccepted(proposalId, fromClanId, toClanId, wood, wheat, fish, iron, _world.currentTick);
  1915	    }
  1916	
  1917	    function cancelVaultTransfer(uint256 proposalId) external override nonReentrant {
  1918	        VaultTransferProposal storage proposal = _otcVaultTransferProposals[proposalId];
  1919	        require(proposal.from != 0, "ClanWorld: proposal not found");
  1920	        require(!proposal.accepted, "ClanWorld: proposal accepted");
  1921	        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
  1922	
  1923	        Clan storage fromClan = _clans[proposal.from];
  1924	        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
  1925	
  1926	        uint32 fromClanId = proposal.from;
  1927	        _closeOtcProposal(fromClanId);
  1928	        delete _otcVaultTransferProposals[proposalId];
  1929	        emit VaultTransferCancelled(proposalId);
  1930	    }
  1931	
  1932	    function _hasVaultResources(Clan storage clan, uint256 wood, uint256 wheat, uint256 fish, uint256 iron)
  1933	        private
  1934	        view
  1935	        returns (bool)
  1936	    {
  1937	        return clan.vaultWood >= wood && clan.vaultWheat >= wheat && clan.vaultFish >= fish && clan.vaultIron >= iron;
  1938	    }
  1939	
  1940	    function proposeBlueprintTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint64 expiryTick)
  1941	        external
  1942	        override
  1943	        nonReentrant
  1944	        returns (uint256 proposalId)
  1945	    {
  1946	        Clan storage fromClan = _clans[fromClanId];
  1947	        require(fromClan.clanId != 0, "ClanWorld: clan not found");
  1948	        require(fromClanId != toClanId, "ERR_SELF_TRANSFER");
  1949	        require(amount > 0, "ERR_ZERO_AMOUNT");
  1950	        require(_openOtcProposalsByClan[fromClanId] < MAX_OPEN_OTC_PROPOSALS_PER_CLAN, "ERR_OTC_CAP");
  1951	        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1952	        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
  1953	        require(_clans[toClanId].clanId != 0, "ClanWorld: target clan not found");
  1954	        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1955	
  1956	        proposalId = _nextOtcProposalId++;
  1957	        _openOtcProposalsByClan[fromClanId]++;
  1958	        _otcBlueprintTransferProposals[proposalId] = BlueprintTransferProposal({
  1959	            from: fromClanId, to: toClanId, amount: amount, expiryTick: expiryTick, accepted: false, cancelled: false
  1960	        });
  1961	
  1962	        emit BlueprintTransferProposed(proposalId, fromClanId, toClanId, amount, expiryTick);
  1963	    }
  1964	
  1965	    function acceptBlueprintTransfer(uint256 proposalId) external override nonReentrant {
  1966	        BlueprintTransferProposal storage proposal = _otcBlueprintTransferProposals[proposalId];
  1967	        require(proposal.from != 0, "ClanWorld: proposal not found");
  1968	        require(!proposal.accepted, "ClanWorld: proposal accepted");
  1969	        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
  1970	        require(_world.currentTick <= proposal.expiryTick, "ClanWorld: proposal expired");
  1971	
  1972	        uint32 fromClanId = proposal.from;
  1973	        uint32 toClanId = proposal.to;
  1974	        uint256 amount = proposal.amount;
  1975	        _settleClan(fromClanId);
  1976	        _settleClan(toClanId);
  1977	
  1978	        Clan storage fromClan = _clans[fromClanId];
  1979	        Clan storage toClan = _clans[toClanId];
  1980	        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1981	        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1982	        require(toClan.owner == msg.sender, "ClanWorld: not clan owner");
  1983	        if (fromClan.blueprintBalance < amount) revert("ERR_NOT_ENOUGH_BLUEPRINT");
  1984	
  1985	        fromClan.blueprintBalance -= amount;
  1986	        toClan.blueprintBalance += amount;
  1987	        _closeOtcProposal(fromClanId);
  1988	        delete _otcBlueprintTransferProposals[proposalId];
  1989	
  1990	        emit BlueprintTransferAccepted(proposalId, fromClanId, toClanId, amount, _world.currentTick);
  1991	    }
  1992	
  1993	    function cancelBlueprintTransfer(uint256 proposalId) external override nonReentrant {
  1994	        BlueprintTransferProposal storage proposal = _otcBlueprintTransferProposals[proposalId];
  1995	        require(proposal.from != 0, "ClanWorld: proposal not found");
  1996	        require(!proposal.accepted, "ClanWorld: proposal accepted");
  1997	        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
  1998	
  1999	        Clan storage fromClan = _clans[proposal.from];
  2000	        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
  2001	
  2002	        uint32 fromClanId = proposal.from;
  2003	        _closeOtcProposal(fromClanId);
  2004	        delete _otcBlueprintTransferProposals[proposalId];
  2005	        emit BlueprintTransferCancelled(proposalId);
  2006	    }
  2007	
  2008	    function proposeBundledTransfer(
  2009	        uint32 fromClanId,
  2010	        uint32 toClanId,
  2011	        uint256 gold,
  2012	        uint256 wood,
  2013	        uint256 wheat,
  2014	        uint256 fish,
  2015	        uint256 iron,
  2016	        uint256 blueprint,
  2017	        uint64 expiryTick
  2018	    ) external override nonReentrant returns (uint256 proposalId) {
  2019	        Clan storage fromClan = _clans[fromClanId];
  2020	        require(fromClan.clanId != 0, "ClanWorld: clan not found");
  2021	        require(fromClanId != toClanId, "ERR_SELF_TRANSFER");
  2022	        require(!_isEmptyBundledTransfer(gold, wood, wheat, fish, iron, blueprint), "ERR_ZERO_AMOUNT");
  2023	        require(_openOtcProposalsByClan[fromClanId] < MAX_OPEN_OTC_PROPOSALS_PER_CLAN, "ERR_OTC_CAP");
  2024	        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  2025	        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
  2026	        require(_clans[toClanId].clanId != 0, "ClanWorld: target clan not found");
  2027	        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  2028	
  2029	        proposalId = _nextOtcProposalId++;
  2030	        _openOtcProposalsByClan[fromClanId]++;
  2031	        _otcBundledTransferProposals[proposalId] = BundledTransferProposal({
  2032	            from: fromClanId,
  2033	            to: toClanId,
  2034	            gold: gold,
  2035	            wood: wood,
  2036	            wheat: wheat,
  2037	            fish: fish,
  2038	            iron: iron,
  2039	            blueprint: blueprint,
  2040	            expiryTick: expiryTick,
  2041	            accepted: false,
  2042	            cancelled: false
  2043	        });
  2044	
  2045	        emit BundledTransferProposed(
  2046	            proposalId, fromClanId, toClanId, gold, wood, wheat, fish, iron, blueprint, expiryTick
  2047	        );
  2048	    }
  2049	
  2050	    function acceptBundledTransfer(uint256 proposalId) external override nonReentrant {
  2051	        BundledTransferProposal storage proposal = _otcBundledTransferProposals[proposalId];
  2052	        require(proposal.from != 0, "ClanWorld: proposal not found");
  2053	        require(!proposal.accepted, "ClanWorld: proposal accepted");
  2054	        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
  2055	        require(_world.currentTick <= proposal.expiryTick, "ClanWorld: proposal expired");
  2056	
  2057	        uint32 fromClanId = proposal.from;
  2058	        uint32 toClanId = proposal.to;
  2059	        uint256 gold = proposal.gold;
  2060	        uint256 wood = proposal.wood;
  2061	        uint256 wheat = proposal.wheat;
  2062	        uint256 fish = proposal.fish;
  2063	        uint256 iron = proposal.iron;
  2064	        uint256 blueprint = proposal.blueprint;
  2065	        _settleClan(fromClanId);
  2066	        _settleClan(toClanId);
  2067	
  2068	        Clan storage fromClan = _clans[fromClanId];
  2069	        Clan storage toClan = _clans[toClanId];
  2070	        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  2071	        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  2072	        require(toClan.owner == msg.sender, "ClanWorld: not clan owner");
  2073	        _requireBundledTransferBalance(fromClan, gold, wood, wheat, fish, iron, blueprint);
  2074	
  2075	        fromClan.goldBalance -= gold;
  2076	        fromClan.vaultWood -= wood;
  2077	        fromClan.vaultWheat -= wheat;
  2078	        fromClan.vaultFish -= fish;
  2079	        fromClan.vaultIron -= iron;
  2080	        fromClan.blueprintBalance -= blueprint;
  2081	        toClan.goldBalance += gold;
  2082	        toClan.vaultWood += wood;
  2083	        toClan.vaultWheat += wheat;
  2084	        toClan.vaultFish += fish;
  2085	        toClan.vaultIron += iron;
  2086	        toClan.blueprintBalance += blueprint;
  2087	        _closeOtcProposal(fromClanId);
  2088	        delete _otcBundledTransferProposals[proposalId];
  2089	
  2090	        emit BundledTransferAccepted(
  2091	            proposalId, fromClanId, toClanId, gold, wood, wheat, fish, iron, blueprint, _world.currentTick
  2092	        );
  2093	    }
  2094	
  2095	    function cancelBundledTransfer(uint256 proposalId) external override nonReentrant {
  2096	        BundledTransferProposal storage proposal = _otcBundledTransferProposals[proposalId];
  2097	        require(proposal.from != 0, "ClanWorld: proposal not found");
  2098	        require(!proposal.accepted, "ClanWorld: proposal accepted");
  2099	        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
  2100	
  2101	        Clan storage fromClan = _clans[proposal.from];
  2102	        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
  2103	
  2104	        uint32 fromClanId = proposal.from;
  2105	        _closeOtcProposal(fromClanId);
  2106	        delete _otcBundledTransferProposals[proposalId];
  2107	        emit BundledTransferCancelled(proposalId);
  2108	    }
  2109	
  2110	    function _isEmptyBundledTransfer(
  2111	        uint256 gold,
  2112	        uint256 wood,
  2113	        uint256 wheat,
  2114	        uint256 fish,
  2115	        uint256 iron,
  2116	        uint256 blueprint
  2117	    ) private pure returns (bool) {
  2118	        return gold == 0 && wood == 0 && wheat == 0 && fish == 0 && iron == 0 && blueprint == 0;
  2119	    }
  2120	
  2121	    function _requireBundledTransferBalance(
  2122	        Clan storage clan,
  2123	        uint256 gold,
  2124	        uint256 wood,
  2125	        uint256 wheat,
  2126	        uint256 fish,
  2127	        uint256 iron,
  2128	        uint256 blueprint
  2129	    ) private view {
  2130	        if (clan.goldBalance < gold) revert("ERR_NOT_ENOUGH_GOLD");
  2131	        if (!_hasVaultResources(clan, wood, wheat, fish, iron)) revert("ERR_NOT_ENOUGH_RESOURCES");
  2132	        if (clan.blueprintBalance < blueprint) revert("ERR_NOT_ENOUGH_BLUEPRINT");
  2133	    }
  2134	
  2135	    function _closeOtcProposal(uint32 fromClanId) private {
  2136	        uint256 openCount = _openOtcProposalsByClan[fromClanId];
  2137	        if (openCount > 0) {
  2138	            _openOtcProposalsByClan[fromClanId] = openCount - 1;
  2139	        }
  2140	    }
  2141	
  2142	    function transferGold(uint32, uint32, uint256) external pure override {
  2143	        revert("OTC transfers not implemented");
  2144	    }
  2145	
  2146	    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
  2147	        revert("OTC transfers not implemented");
  2148	    }
  2149	
  2150	    function transferBlueprint(uint32, uint32, uint256) external pure override {
  2151	        revert("OTC transfers not implemented");
  2152	    }
  2153	
  2154	    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
  2155	        external
  2156	        pure
  2157	        override
  2158	    {
  2159	        revert("OTC transfers not implemented");
  2160	    }
  2161	
  2162	    // =========================================================================
  2163	    // RAW READ GETTERS
  2164	    // =========================================================================
  2165	
  2166	    function getWorldState() external view override returns (WorldState memory) {
  2167	        return _world;
  2168	    }
  2169	
  2170	    function getTreasuryState() external view override returns (TreasuryState memory) {
  2171	        return _treasury;
  2172	    }
  2173	
  2174	    function getResourceToken(uint8 resourceType) external view override returns (address) {
  2175	        if (resourceType == uint8(ResourceType.Wood)) return _treasury.woodToken;
  2176	        if (resourceType == uint8(ResourceType.Iron)) return _treasury.ironToken;
  2177	        if (resourceType == uint8(ResourceType.Wheat)) return _treasury.wheatToken;
  2178	        if (resourceType == uint8(ResourceType.Fish)) return _treasury.fishToken;
  2179	        revert("ClanWorld: invalid resource");
  2180	    }
  2181	
  2182	    function getPool(uint8 resourceType) external view override returns (address) {
  2183	        return _poolForResourceType(resourceType);
  2184	    }
  2185	
  2186	    function getPrice(uint8 resourceType, uint256 amountIn) external view override returns (uint256 amountOut) {
  2187	        amountOut = StubPool(_poolForResourceType(resourceType)).getAmountOutForExactIn(amountIn);
  2188	    }
  2189	
  2190	    function _poolForResourceType(uint8 resourceType) internal view returns (address) {
  2191	        if (resourceType == uint8(ResourceType.Wood)) return _treasury.woodGoldPool;
  2192	        if (resourceType == uint8(ResourceType.Iron)) return _treasury.ironGoldPool;
  2193	        if (resourceType == uint8(ResourceType.Wheat)) return _treasury.wheatGoldPool;
  2194	        if (resourceType == uint8(ResourceType.Fish)) return _treasury.fishGoldPool;
  2195	        revert("ClanWorld: invalid resource");
  2196	    }
  2197	
  2198	    function getClan(uint32 clanId) external view override returns (Clan memory) {
  2199	        return _clans[clanId];
  2200	    }
  2201	
  2202	    function getClansman(uint32 clansmanId) external view override returns (Clansman memory) {
  2203	        return _clansmen[clansmanId];
  2204	    }
  2205	
  2206	    function getActiveMission(uint32 clansmanId) external view override returns (Mission memory) {
  2207	        return _missions[clansmanId];
  2208	    }
  2209	
  2210	    function getMissionTiming(uint32 clanId, uint32 clansmanId)
  2211	        external
  2212	        view
  2213	        override
  2214	        returns (uint64 submitted, uint64 executes, uint64 settles)
  2215	    {
  2216	        Mission memory m = _missions[clansmanId];
  2217	        if (!m.active || _clansmen[clansmanId].clanId != clanId) {
  2218	            return (0, 0, 0);
  2219	        }
  2220	        return (m.submittedAtTick, m.executesAtTick, m.settlesAtTick);
  2221	    }
  2222	
  2223	    function getActionDuration(ActionType action) public pure override returns (uint64) {
  2224	        if (
  2225	            action == ActionType.ChopWood || action == ActionType.MineIron || action == ActionType.FishDocks
  2226	                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
  2227	        ) {
  2228	            return 4;
  2229	        }
  2230	
  2231	        if (action == ActionType.DepositResources) {
  2232	            return DEPOSIT_DURATION_TICKS;
  2233	        }
  2234	
  2235	        if (
  2236	            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
  2237	                || action == ActionType.MarketBuy || action == ActionType.MarketSell
  2238	        ) {
  2239	            return 1;
  2240	        }
  2241	
  2242	        return 0;
  2243	    }
  2244	
  2245	    function getTravelTicks(uint8 fromRegion, uint8 toRegion) external pure override returns (uint64) {
  2246	        return uint64(_travelTicks(fromRegion, toRegion));
  2247	    }
  2248	
  2249	    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
  2250	        return BanditTroop({
  2251	            banditId: 0,
  2252	            state: BanditState.NONE,
  2253	            currentRegion: 0,
  2254	            attackAttemptsMade: 0,
  2255	            stateEnteredTick: 0,
  2256	            nextActionTick: 0,
  2257	            tier: 0,
  2258	            attackPower: 0,
  2259	            carryWood: 0,
  2260	            carryIron: 0,
  2261	            carryWheat: 0,
  2262	            carryFish: 0
  2263	        });
  2264	    }
  2265	
  2266	    function getWheatPlots(uint32 clanId)
  2267	        external
  2268	        view
  2269	        override
  2270	        returns (WheatPlot memory west, WheatPlot memory east)
  2271	    {
  2272	        west = _wheatPlots[clanId][0];
  2273	        east = _wheatPlots[clanId][1];
  2274	    }
  2275	
  2276	    function getScheduledMarketActionsForTick(uint64 tick)
  2277	        external
  2278	        view
  2279	        override
  2280	        returns (ScheduledMarketAction[] memory)
  2281	    {
  2282	        return _scheduledMarketActions[tick];
  2283	    }
  2284	
  2285	    function getOtcGoldProposal(uint256 proposalId) external view override returns (OtcProposal memory) {
  2286	        return _otcGoldProposals[proposalId];
  2287	    }
  2288	
  2289	    function getOtcVaultTransferProposal(uint256 proposalId)
  2290	        external
  2291	        view
  2292	        override
  2293	        returns (VaultTransferProposal memory)
  2294	    {
  2295	        return _otcVaultTransferProposals[proposalId];
  2296	    }
  2297	
  2298	    function getOtcBlueprintTransferProposal(uint256 proposalId)
  2299	        external
  2300	        view
  2301	        override
  2302	        returns (BlueprintTransferProposal memory)
  2303	    {
  2304	        return _otcBlueprintTransferProposals[proposalId];
  2305	    }
  2306	
  2307	    function getOtcBundledTransferProposal(uint256 proposalId)
  2308	        external
  2309	        view
  2310	        override
  2311	        returns (BundledTransferProposal memory)
  2312	    {
  2313	        return _otcBundledTransferProposals[proposalId];
  2314	    }
  2315	
  2316	    function getActiveDefenders(uint32 targetClanId) external view override returns (uint32[] memory clansmanIds) {
  2317	        uint32[] storage defendingClans = _defendingClansByRegion[_clans[targetClanId].baseRegion];
  2318	        uint256 count = 0;
  2319	
  2320	        for (uint256 i = 0; i < defendingClans.length; i++) {
  2321	            uint32[] storage clanClansmen = _clanClansmanIds[defendingClans[i]];
  2322	            for (uint256 j = 0; j < clanClansmen.length; j++) {
  2323	                Mission storage mission = _missions[clanClansmen[j]];
  2324	                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
  2325	                    count++;
  2326	                }
  2327	            }
  2328	        }
  2329	
  2330	        clansmanIds = new uint32[](count);
  2331	        uint256 out = 0;
  2332	        for (uint256 i = 0; i < defendingClans.length; i++) {
  2333	            uint32[] storage clanClansmen = _clanClansmanIds[defendingClans[i]];
  2334	            for (uint256 j = 0; j < clanClansmen.length; j++) {
  2335	                uint32 clansmanId = clanClansmen[j];
  2336	                Mission storage mission = _missions[clansmanId];
  2337	                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
  2338	                    clansmanIds[out++] = clansmanId;
  2339	                }
  2340	            }
  2341	        }
  2342	    }
  2343	
  2344	    function getDefendingClans(uint8 region) external view override returns (uint32[] memory) {
  2345	        return _defendingClansByRegion[region];
  2346	    }
  2347	
  2348	    // =========================================================================
  2349	    // DERIVED READ GETTERS (read-only, no storage mutation)
  2350	    // =========================================================================

exec
/bin/bash -lc "git show 2bec876:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '1,260p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	/**
     5	 * @title IClanWorld
     6	 * @notice Canonical seam interface for the ClanWorld v4 hackathon build.
     7	 *
     8	 * Synthesized from:
     9	 *   - clanworld_v4_spec.md (mechanics)
    10	 *   - clanworld_v4_1_addendum.md (locked clarifications)
    11	 *   - clanworld_v4_2_state_schema_interface_spec.md (state schema)
    12	 *   - clanworld_v4_3_schema_patch.md (final lockdowns)
    13	 *   - clanworld_v4_4_ui_indexer_getters.md (UI convenience aggregators)
    14	 *
    15	 * This is the contract surface every other workstream depends on.
    16	 * Treat field names, event names, struct layouts, and enum order as STABLE.
    17	 *
    18	 * Implementation lives behind this interface. Mocks and indexer types are
    19	 * derived from it. Do not change without coordinating with all streams.
    20	 */
    21	
    22	// =============================================================================
    23	// CONSTANTS
    24	// =============================================================================
    25	
    26	library ClanWorldConstants {
    27	    // World cadence
    28	    uint64 internal constant HEARTBEAT_INTERVAL_SECONDS = 60;
    29	    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
    30	    uint64 internal constant WINTER_DURATION_TICKS = 10;
    31	    uint64 internal constant SEASON_DURATION_TICKS = 360;
    32	
    33	    // Bandit cadence
    34	    uint64 internal constant BANDIT_COOLDOWN_TICKS = 10;
    35	    uint64 internal constant BANDIT_CAMP_TICKS = 3;
    36	    uint64 internal constant BANDIT_REST_TICKS = 2;
    37	    uint8 internal constant BANDIT_MAX_ATTACK_ATTEMPTS = 6;
    38	
    39	    // Clansman cadence
    40	    uint64 internal constant CLANSMAN_COOLDOWN_SECONDS = 60;
    41	
    42	    // Carry caps (per clansman)
    43	    uint256 internal constant CLANSMAN_CARRY_CAP = 10e18;
    44	    uint256 internal constant WOOD_CAP = CLANSMAN_CARRY_CAP;
    45	    uint256 internal constant IRON_CAP = 5e18;
    46	    uint256 internal constant WHEAT_CAP = 40e18;
    47	    uint256 internal constant FISH_CAP = 8e18;
    48	
    49	    // Gathering yields
    50	    uint256 internal constant WOOD_YIELD_PER_TICK = 1e18;
    51	    uint256 internal constant WOOD_BASE_YIELD = WOOD_YIELD_PER_TICK;
    52	    uint256 internal constant WOOD_CRIT_BONUS = WOOD_YIELD_PER_TICK;
    53	    uint16 internal constant WOOD_CRIT_BPS = 1000; // 10%
    54	
    55	    uint256 internal constant IRON_BASE_YIELD = 5e17; // 0.5e18
    56	    uint16 internal constant GOLD_FROM_IRON_BPS = 200; // 2%
    57	    uint256 internal constant GOLD_FROM_IRON_AMOUNT = 1e18;
    58	
    59	    uint16 internal constant FISH_DOCKS_BPS = 2500; // 25%
    60	    uint16 internal constant FISH_DEEP_BPS = 7500; // 75%
    61	
    62	    // Upkeep
    63	    uint256 internal constant WHEAT_UPKEEP_PER_CLANSMAN = 1e18;
    64	    uint256 internal constant FISH_UPKEEP_PER_CLANSMAN = 1e17; // 0.1
    65	    uint256 internal constant WINTER_WOOD_BURN_PER_BASE = 1e18;
    66	    uint16 internal constant WINTER_UPKEEP_MULTIPLIER_BPS = 20000; // 2x
    67	
    68	    // Wheat plots
    69	    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
    70	    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
    71	
    72	    // Bandit combat
    73	    uint16 internal constant BANDIT_BASE_STEAL_BPS = 2000; // 20%
    74	    uint16 internal constant BANDIT_DROP_TO_DEFENDERS_BPS = 5000; // 50%
    75	
    76	    // Region IDs (1-indexed; 0 = NOOP / unset sentinel)
    77	    uint8 internal constant REGION_NOOP = 0;
    78	    uint8 internal constant REGION_FOREST = 1;
    79	    uint8 internal constant REGION_MOUNTAINS = 2;
    80	    uint8 internal constant REGION_UNICORN_TOWN = 3;
    81	    uint8 internal constant REGION_WEST_FARMS = 4;
    82	    uint8 internal constant REGION_EAST_FARMS = 5;
    83	    uint8 internal constant REGION_WEST_DOCKS = 6;
    84	    uint8 internal constant REGION_EAST_DOCKS = 7;
    85	    uint8 internal constant REGION_DEEP_SEA = 8;
    86	
    87	    // Sentinels
    88	    uint32 internal constant CLAN_ID_NULL = 0; // valid clan IDs start at 1
    89	    uint32 internal constant BANDIT_ID_NULL = 0;
    90	}
    91	
    92	// =============================================================================
    93	// ENUMS
    94	// =============================================================================
    95	
    96	enum ClanState {
    97	    ACTIVE,
    98	    DEAD
    99	}
   100	
   101	enum ClansmanState {
   102	    WAITING,
   103	    TRAVELING,
   104	    ACTING,
   105	    DEAD
   106	}
   107	
   108	enum BanditState {
   109	    NONE,
   110	    CAMPING,
   111	    RESTING,
   112	    ATTACKING,
   113	    DEFEATED,
   114	    ESCAPED
   115	}
   116	
   117	enum WheatPlotState {
   118	    Harvestable,
   119	    Regrowing,
   120	    WinterLocked
   121	}
   122	
   123	enum ResourceType {
   124	    Wood,
   125	    Iron,
   126	    Wheat,
   127	    Fish
   128	}
   129	
   130	enum ActionType {
   131	    None,
   132	    ChopWood,
   133	    MineIron,
   134	    FishDocks,
   135	    FishDeepSea,
   136	    HarvestWheat,
   137	    DepositResources,
   138	    BuildWall,
   139	    UpgradeBase,
   140	    UpgradeMonument,
   141	    DefendBase,
   142	    MarketBuy,
   143	    MarketSell,
   144	    Wait
   145	}
   146	
   147	enum MarketExecutionMode {
   148	    None,
   149	    Immediate,
   150	    Scheduled
   151	}
   152	
   153	enum StatusCode {
   154	    OK,
   155	    ERR_CLAN_DEAD,
   156	    ERR_CLAN_NOT_OWNED,
   157	    ERR_CLANSMAN_DEAD,
   158	    ERR_INVALID_CLANSMAN,
   159	    ERR_INVALID_REGION,
   160	    ERR_INVALID_ACTION,
   161	    ERR_INVALID_TARGET,
   162	    ERR_COOLDOWN_ACTIVE,
   163	    ERR_NOT_WAITING,
   164	    ERR_NOT_IN_UNICORN_TOWN,
   165	    ERR_NOT_AT_HOMEBASE,
   166	    ERR_NOT_AT_TARGET_BASE,
   167	    ERR_NOT_DEFENDABLE,
   168	    ERR_MISSING_RESOURCES,
   169	    ERR_EMPTY_CARGO,
   170	    ERR_PLOT_NOT_READY,
   171	    ERR_PLOT_EMPTY,
   172	    ERR_MARKET_ZERO_AMOUNT,
   173	    ERR_MARKET_UNSUPPORTED_TOKEN,
   174	    ERR_IMMEDIATE_MARKET_NOT_ELIGIBLE,
   175	    ERR_MARKET_BUY_OVER_CAPACITY,
   176	    ERR_MARKET_BUY_MAX_GOLD_EXCEEDED,
   177	    ERR_WORLD_TICK_MISMATCH,
   178	    ERR_NO_ACTIVE_BANDIT,
   179	    ERR_SEASON_ENDED,
   180	    ERR_NOT_ENOUGH_GOLD,
   181	    ERR_CARRY_FULL,
   182	    ERR_ZERO_AMOUNT,
   183	    ERR_SELF_TRANSFER,
   184	    ERR_OTC_CAP
   185	}
   186	
   187	// =============================================================================
   188	// CORE STATE STRUCTS (raw storage shape)
   189	// =============================================================================
   190	
   191	struct WorldState {
   192	    uint64 currentTick;
   193	    uint64 seasonStartTick;
   194	    uint64 seasonEndTick;
   195	    bool seasonFinalized;
   196	    uint64 currentSeasonNumber; // 1-indexed; incremented each time seasonEndTick is crossed
   197	    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)
   198	
   199	    uint64 nextHeartbeatAtTs;
   200	    uint64 nextBanditSpawnEligibleTick;
   201	    uint16 currentBanditSpawnChanceBps;
   202	    bytes32 currentTickSeed;
   203	
   204	    uint32 activeBanditId; // 0 if none
   205	    bool winterActive;
   206	    uint64 winterStartsAtTick;
   207	    uint64 winterEndsAtTick; // 0 if not active
   208	
   209	    uint64 nextCommitSequence; // global FIFO sequence for scheduled market actions
   210	}
   211	
   212	struct TreasuryState {
   213	    address treasuryOwner;
   214	    uint256 prizePotGold;
   215	
   216	    bool poolsSeeded;
   217	
   218	    address woodToken;
   219	    address wheatToken;
   220	    address fishToken;
   221	    address ironToken;
   222	    address goldToken;
   223	    address blueprintToken;
   224	
   225	    address woodGoldPool;
   226	    address wheatGoldPool;
   227	    address fishGoldPool;
   228	    address ironGoldPool;
   229	}
   230	
   231	struct Clan {
   232	    uint32 clanId;
   233	    uint256 iftTokenId;
   234	    address owner;
   235	    ClanState clanState;
   236	
   237	    uint8 baseRegion;
   238	    uint8 baseLevel;
   239	    uint8 wallLevel;
   240	    uint8 monumentLevel;
   241	    uint8 livingClansmen;
   242	
   243	    uint64 lastSettledTick;
   244	    uint64 starvationStartsAtTick; // 0 = none
   245	
   246	    uint16 coldDamage; // resets to 0 at winter end
   247	
   248	    uint256 goldBalance;
   249	    uint256 blueprintBalance;
   250	
   251	    uint256 vaultWood;
   252	    uint256 vaultIron;
   253	    uint256 vaultWheat;
   254	    uint256 vaultFish;
   255	}
   256	
   257	struct WheatPlot {
   258	    WheatPlotState state;
   259	    uint8 region; // West Farms or East Farms
   260	    uint256 remainingWheat;

exec
/bin/bash -lc 'git show 2bec876:packages/contracts/src/MinimalERC20.sol | nl -ba' in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	/// @notice Minimal ERC20 boundary token. No external deps.
     5	contract MinimalERC20 {
     6	    string public name;
     7	    string public symbol;
     8	    uint8 public constant decimals = 18;
     9	
    10	    uint256 public totalSupply;
    11	    address public immutable DEPLOYER;
    12	    address public engine;
    13	    uint8 public resourceType;
    14	    bool public boundaryConfigured;
    15	    bool public treasurySeeded;
    16	
    17	    mapping(address => uint256) public balanceOf;
    18	    mapping(address => mapping(address => uint256)) public allowance;
    19	
    20	    event Transfer(address indexed from, address indexed to, uint256 value);
    21	    event Approval(address indexed owner_, address indexed spender, uint256 value);
    22	    event BoundaryConfigured(uint8 indexed resourceType, address indexed engine);
    23	    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
    24	    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);
    25	
    26	    constructor(string memory name_, string memory symbol_) {
    27	        name = name_;
    28	        symbol = symbol_;
    29	        DEPLOYER = msg.sender;
    30	        resourceType = type(uint8).max;
    31	    }
    32	
    33	    /// @dev Wrapped per forge-lint unwrapped-modifier-logic — keeps modifier
    34	    /// body slim so the require isn't duplicated at every call site.
    35	    function _onlyDeployer() internal view {
    36	        require(msg.sender == DEPLOYER, "MinimalERC20: not deployer");
    37	    }
    38	
    39	    function _onlyEngine() internal view {
    40	        require(boundaryConfigured && msg.sender == engine, "MinimalERC20: not engine");
    41	    }
    42	
    43	    modifier onlyDeployer() {
    44	        _onlyDeployer();
    45	        _;
    46	    }
    47	
    48	    modifier onlyEngine() {
    49	        _onlyEngine();
    50	        _;
    51	    }
    52	
    53	    function configureBoundary(uint8 resourceType_, address engine_) external onlyDeployer {
    54	        require(!boundaryConfigured, "MinimalERC20: boundary configured");
    55	        require(engine_ != address(0), "MinimalERC20: zero engine");
    56	
    57	        resourceType = resourceType_;
    58	        engine = engine_;
    59	        boundaryConfigured = true;
    60	
    61	        emit BoundaryConfigured(resourceType_, engine_);
    62	    }
    63	
    64	    function seedTreasury(address treasury, uint256 amount) external onlyDeployer {
    65	        require(!treasurySeeded, "MinimalERC20: treasury seeded");
    66	        require(treasury != address(0), "MinimalERC20: zero treasury");
    67	
    68	        treasurySeeded = true;
    69	        _mint(treasury, amount);
    70	    }
    71	
    72	    function mint(address to, uint256 amount) external onlyEngine {
    73	        _mint(to, amount);
    74	        emit ResourceMinted(resourceType, to, amount);
    75	    }
    76	
    77	    function burn(address from, uint256 amount) external onlyEngine {
    78	        balanceOf[from] -= amount;
    79	        totalSupply -= amount;
    80	        emit Transfer(from, address(0), amount);
    81	        emit ResourceBurned(resourceType, from, amount);
    82	    }
    83	
    84	    function _mint(address to, uint256 amount) internal {
    85	        totalSupply += amount;
    86	        balanceOf[to] += amount;
    87	        emit Transfer(address(0), to, amount);
    88	    }
    89	
    90	    function transfer(address to, uint256 amount) external returns (bool) {
    91	        balanceOf[msg.sender] -= amount;
    92	        balanceOf[to] += amount;
    93	        emit Transfer(msg.sender, to, amount);
    94	        return true;
    95	    }
    96	
    97	    function transferFrom(address from, address to, uint256 amount) external returns (bool) {
    98	        allowance[from][msg.sender] -= amount;
    99	        balanceOf[from] -= amount;
   100	        balanceOf[to] += amount;
   101	        emit Transfer(from, to, amount);
   102	        return true;
   103	    }
   104	
   105	    function approve(address spender, uint256 amount) external returns (bool) {
   106	        allowance[msg.sender][spender] = amount;
   107	        emit Approval(msg.sender, spender, amount);
   108	        return true;
   109	    }
   110	}

exec
/bin/bash -lc 'git show 2bec876:packages/contracts/src/StubPool.sol | nl -ba' in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	interface IERC20Balance {
     5	    function balanceOf(address account) external view returns (uint256);
     6	}
     7	
     8	/// @notice Constant-product AMM pool for one resource/gold pair.
     9	///         TOKEN_A is the resource token and TOKEN_B is gold. ClanWorld owns
    10	///         the swap surface because clans use the engine's internal accounting,
    11	///         while the treasury seeds real ERC20 balances into the pool once.
    12	contract StubPool {
    13	    address public immutable TOKEN_A; // resource token
    14	    address public immutable TOKEN_B; // gold token
    15	    address public immutable ENGINE; // ClanWorld address
    16	
    17	    uint256 public reserveA; // resource reserve
    18	    uint256 public reserveB; // gold reserve
    19	
    20	    bool private _seeded;
    21	
    22	    modifier onlyEngine() {
    23	        require(msg.sender == ENGINE, "StubPool: only engine");
    24	        _;
    25	    }
    26	
    27	    constructor(address tokenA_, address tokenB_, address engine_) {
    28	        TOKEN_A = tokenA_;
    29	        TOKEN_B = tokenB_;
    30	        ENGINE = engine_;
    31	    }
    32	
    33	    /// @notice Called by ClanWorld at seedPools time to set initial reserves.
    34	    ///         Can only be called once.
    35	    function seed(uint256 amountA, uint256 amountB) external onlyEngine {
    36	        require(!_seeded, "StubPool: already seeded");
    37	        require(amountA > 0 && amountB > 0, "StubPool: zero seed");
    38	        require(IERC20Balance(TOKEN_A).balanceOf(address(this)) >= amountA, "StubPool: missing token A");
    39	        require(IERC20Balance(TOKEN_B).balanceOf(address(this)) >= amountB, "StubPool: missing token B");
    40	        reserveA = amountA;
    41	        reserveB = amountB;
    42	        _seeded = true;
    43	    }
    44	
    45	    function getAmountOutForExactIn(uint256 amountIn) public view returns (uint256 amountOut) {
    46	        require(amountIn > 0, "StubPool: zero amount");
    47	        require(reserveA > 0 && reserveB > 0, "StubPool: not seeded");
    48	        amountOut = (reserveB * amountIn) / (reserveA + amountIn);
    49	    }
    50	
    51	    function getAmountInForExactOut(uint256 amountOut) public view returns (uint256 amountIn) {
    52	        require(amountOut > 0, "StubPool: zero amount");
    53	        require(amountOut < reserveA, "StubPool: insufficient resource reserve");
    54	        require(reserveB > 0, "StubPool: not seeded");
    55	
    56	        uint256 numerator = reserveB * amountOut;
    57	        uint256 denominator = reserveA - amountOut;
    58	        amountIn = numerator / denominator + (numerator % denominator == 0 ? 0 : 1);
    59	    }
    60	
    61	    /// @notice Exact-input sell: resource in, gold out.
    62	    function swapExactInForOut(uint256 amountIn, uint256 minOut) external onlyEngine returns (uint256 amountOut) {
    63	        return _swapExactInForOut(amountIn, minOut);
    64	    }
    65	
    66	    function _swapExactInForOut(uint256 amountIn, uint256 minOut) internal returns (uint256 amountOut) {
    67	        uint256 priorK = reserveA * reserveB;
    68	        amountOut = getAmountOutForExactIn(amountIn);
    69	        require(amountOut >= minOut, "StubPool: insufficient output");
    70	
    71	        reserveA += amountIn;
    72	        reserveB -= amountOut;
    73	
    74	        require(reserveA * reserveB >= priorK, "StubPool: k invariant");
    75	    }
    76	
    77	    /// @notice Exact-output buy: gold in, exact resource out.
    78	    function swapExactOutForInWithMaxIn(uint256 amountOut, uint256 maxIn)
    79	        external
    80	        onlyEngine
    81	        returns (uint256 amountIn)
    82	    {
    83	        return _swapExactOutForInWithMaxIn(amountOut, maxIn);
    84	    }
    85	
    86	    function _swapExactOutForInWithMaxIn(uint256 amountOut, uint256 maxIn) internal returns (uint256 amountIn) {
    87	        uint256 priorK = reserveA * reserveB;
    88	        amountIn = getAmountInForExactOut(amountOut);
    89	        require(amountIn <= maxIn, "StubPool: max input exceeded");
    90	
    91	        reserveA -= amountOut;
    92	        reserveB += amountIn;
    93	
    94	        require(reserveA * reserveB >= priorK, "StubPool: k invariant");
    95	    }
    96	
    97	    /// @notice Exact-input sell: clan sells amountIn of resource, gets goldOut.
    98	    ///         Legacy alias for Phase 6.1 scheduled-market code.
    99	    function sellResource(uint256 amountIn) external onlyEngine returns (uint256 goldOut) {
   100	        goldOut = _swapExactInForOut(amountIn, 1);
   101	    }
   102	
   103	    /// @notice Legacy quote alias for exact-output resource buys.
   104	    function quoteBuy(uint256 amountOut) external view returns (uint256 goldIn) {
   105	        goldIn = getAmountInForExactOut(amountOut);
   106	    }
   107	
   108	    /// @notice Exact-output buy: clan buys amountOut of resource, pays goldIn.
   109	    ///         Legacy alias for Phase 6.1 scheduled-market code.
   110	    function buyResource(uint256 amountOut) external onlyEngine returns (uint256 goldIn) {
   111	        goldIn = _swapExactOutForInWithMaxIn(amountOut, type(uint256).max);
   112	    }
   113	
   114	    function getReserves() external view returns (uint256, uint256) {
   115	        return (reserveA, reserveB);
   116	    }
   117	}

exec
/bin/bash -lc 'git show 2bec876:packages/contracts/script/Deploy.s.sol | nl -ba' in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.24;
     3	
     4	import {Script, console} from "forge-std/Script.sol";
     5	import {MinimalERC20} from "../src/MinimalERC20.sol";
     6	import {StubPool} from "../src/StubPool.sol";
     7	import {ClanWorld} from "../src/ClanWorld.sol";
     8	import {PoolSeedConfig, ResourceType} from "../src/IClanWorld.sol";
     9	
    10	contract Deploy is Script {
    11	    function run() external {
    12	        uint256 deployerPrivateKey = vm.envUint("DEPLOYER_PRIVATE_KEY");
    13	        address treasury = vm.addr(deployerPrivateKey);
    14	
    15	        vm.startBroadcast(deployerPrivateKey);
    16	
    17	        // 1. Deploy boundary tokens (gold existed in Phase 2 and is reused here).
    18	        MinimalERC20 wood = new MinimalERC20("Wood", "WOOD");
    19	        MinimalERC20 iron = new MinimalERC20("Iron", "IRON");
    20	        MinimalERC20 wheat = new MinimalERC20("Wheat", "WHEAT");
    21	        MinimalERC20 fish = new MinimalERC20("Fish", "FISH");
    22	        MinimalERC20 gold = new MinimalERC20("Gold", "GOLD");
    23	        MinimalERC20 blueprint = new MinimalERC20("ClanWorld Blueprint", "BPRT");
    24	
    25	        console.log("wood:     ", address(wood));
    26	        console.log("iron:     ", address(iron));
    27	        console.log("wheat:    ", address(wheat));
    28	        console.log("fish:     ", address(fish));
    29	        console.log("gold:     ", address(gold));
    30	        console.log("blueprint:", address(blueprint));
    31	
    32	        // 2. Deploy ClanWorld first (needed as engine arg for pools).
    33	        ClanWorld game = new ClanWorld();
    34	        console.log("CLAN_WORLD_CONTRACT_ADDRESS:", address(game));
    35	
    36	        wood.configureBoundary(uint8(ResourceType.Wood), address(game));
    37	        iron.configureBoundary(uint8(ResourceType.Iron), address(game));
    38	        wheat.configureBoundary(uint8(ResourceType.Wheat), address(game));
    39	        fish.configureBoundary(uint8(ResourceType.Fish), address(game));
    40	
    41	        // 3. Deploy 4 AMM pools (Phase 6.2: constant-product pools).
    42	        StubPool woodGold = new StubPool(address(wood), address(gold), address(game));
    43	        StubPool wheatGold = new StubPool(address(wheat), address(gold), address(game));
    44	        StubPool fishGold = new StubPool(address(fish), address(gold), address(game));
    45	        StubPool ironGold = new StubPool(address(iron), address(gold), address(game));
    46	
    47	        console.log("woodGoldPool: ", address(woodGold));
    48	        console.log("wheatGoldPool:", address(wheatGold));
    49	        console.log("fishGoldPool: ", address(fishGold));
    50	        console.log("ironGoldPool: ", address(ironGold));
    51	
    52	        address[6] memory tokens =
    53	            [address(wood), address(iron), address(wheat), address(fish), address(gold), address(blueprint)];
    54	        address[4] memory pools = [address(woodGold), address(wheatGold), address(fishGold), address(ironGold)];
    55	
    56	        game.initTreasury(tokens, pools);
    57	
    58	        uint256 resSeed = game.INITIAL_RESOURCE_POOL_SEED();
    59	        uint256 goldSeed = game.INITIAL_GOLD_POOL_SEED();
    60	        uint256 totalGoldSeed = goldSeed * 4;
    61	
    62	        wood.seedTreasury(treasury, resSeed);
    63	        wheat.seedTreasury(treasury, resSeed);
    64	        fish.seedTreasury(treasury, resSeed);
    65	        iron.seedTreasury(treasury, resSeed);
    66	        gold.seedTreasury(treasury, totalGoldSeed);
    67	
    68	        wood.approve(address(game), resSeed);
    69	        wheat.approve(address(game), resSeed);
    70	        fish.approve(address(game), resSeed);
    71	        iron.approve(address(game), resSeed);
    72	        gold.approve(address(game), totalGoldSeed);
    73	
    74	        game.seedPools(
    75	            PoolSeedConfig({
    76	                woodSeed: resSeed,
    77	                wheatSeed: resSeed,
    78	                fishSeed: resSeed,
    79	                ironSeed: resSeed,
    80	                goldSeedForWood: goldSeed,
    81	                goldSeedForWheat: goldSeed,
    82	                goldSeedForFish: goldSeed,
    83	                goldSeedForIron: goldSeed
    84	            })
    85	        );
    86	
    87	        vm.stopBroadcast();
    88	    }
    89	}

codex
Next I’m drilling into the non-OTC deltas (gathering/deposit behavior and status-code shifts) because those are prime spots for fix-round regressions that unit tests can miss.
exec
/bin/bash -lc "git show 2bec876:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1,160p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	import {
     5	    IClanWorld,
     6	    IClanWorldEvents,
     7	    ClanWorldConstants,
     8	    ClanState,
     9	    ClansmanState,
    10	    BanditState,
    11	    WheatPlotState,
    12	    ResourceType,
    13	    ActionType,
    14	    MarketExecutionMode,
    15	    StatusCode,
    16	    WorldState,
    17	    TreasuryState,
    18	    Clan,
    19	    WheatPlot,
    20	    Clansman,
    21	    Mission,
    22	    BanditTroop,
    23	    ScheduledMarketAction,
    24	    OtcProposal,
    25	    VaultTransferProposal,
    26	    BlueprintTransferProposal,
    27	    BundledTransferProposal,
    28	    DefenseContribution,
    29	    PackedRoute,
    30	    DerivedClanState,
    31	    DerivedClansmanState,
    32	    ClanOrder,
    33	    OrderResult,
    34	    PoolSeedConfig,
    35	    LeaderboardEntry,
    36	    WorldSnapshot,
    37	    ClansmanFullView,
    38	    ClanFullView,
    39	    PoolReserves,
    40	    MarketState,
    41	    ActiveBanditView,
    42	    RegionOccupant
    43	} from "./IClanWorld.sol";
    44	import {MinimalERC20} from "./MinimalERC20.sol";
    45	import {StubPool} from "./StubPool.sol";
    46	import {ReentrancyGuard} from "./util/ReentrancyGuard.sol";
    47	
    48	/// @title ClanWorld
    49	/// @notice Phase 1+2 real engine implementation of IClanWorld v4.
    50	///         Implements: world clock, clan lifecycle, lazy settlement, resource gathering,
    51	///         deposit, wheat harvest, travel, NOOP bypass, order validation, and market execution.
    52	///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
    53	contract ClanWorld is IClanWorld, ReentrancyGuard {
    54	    // =========================================================================
    55	    // STORAGE
    56	    // =========================================================================
    57	
    58	    WorldState private _world;
    59	    TreasuryState private _treasury;
    60	
    61	    mapping(uint32 => Clan) internal _clans;
    62	    mapping(uint32 => Clansman) internal _clansmen;
    63	    mapping(uint32 => Mission) private _missions; // keyed by clansmanId
    64	    mapping(uint32 => WheatPlot[2]) private _wheatPlots; // [0]=west [1]=east
    65	    mapping(uint64 => ScheduledMarketAction[]) private _scheduledMarketActions; // keyed by tick
    66	    mapping(uint8 => uint32[]) private _defendingClansByRegion; // home region => unique defending clanIds
    67	    mapping(uint8 => mapping(uint32 => uint256)) private _defenderCountByRegionClan; // region => clanId => clansmen count
    68	    mapping(uint32 => uint8) private _clansmanDefendingRegion; // clansmanId => defended home region
    69	    mapping(uint64 => bytes32) private _tickSeeds; // tick => seed
    70	    mapping(uint256 => OtcProposal) private _otcGoldProposals;
    71	    mapping(uint256 => VaultTransferProposal) private _otcVaultTransferProposals;
    72	    mapping(uint256 => BlueprintTransferProposal) private _otcBlueprintTransferProposals;
    73	    mapping(uint256 => BundledTransferProposal) private _otcBundledTransferProposals;
    74	    mapping(uint32 => uint256) private _openOtcProposalsByClan;
    75	
    76	    uint32 private _nextClanId;
    77	    uint32 private _nextClansmanId;
    78	    uint256 private _nextOtcProposalId;
    79	    uint32[] private _allClanIds;
    80	
    81	    // per-clan clansman list: clanId => clansmanId[]
    82	    mapping(uint32 => uint32[]) private _clanClansmanIds;
    83	
    84	    // =========================================================================
    85	    // CONSTANTS — Wheat harvest rate (not in IClanWorld constants)
    86	    // =========================================================================
    87	
    88	    uint64 private constant DEPOSIT_DURATION_TICKS = 1;
    89	    uint256 private constant WHEAT_HARVEST_RATE = 20e18;
    90	    uint256 public constant INITIAL_RESOURCE_POOL_SEED = 100_000e18;
    91	    uint256 public constant INITIAL_GOLD_POOL_SEED = 50_000e18;
    92	    /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
    93	    uint256 public constant MAX_MARKET_ACTIONS_PER_TICK = 32;
    94	    uint256 public constant MAX_OPEN_OTC_PROPOSALS_PER_CLAN = 8;
    95	
    96	    // =========================================================================
    97	    // CONSTRUCTOR
    98	    // =========================================================================
    99	
   100	    constructor() {
   101	        _world.currentTick = 0;
   102	        _world.nextHeartbeatAtTs = uint64(block.timestamp);
   103	        _world.seasonStartTick = 0;
   104	        _world.seasonEndTick = ClanWorldConstants.SEASON_DURATION_TICKS;
   105	        _world.currentSeasonNumber = 1;
   106	        _world.nextHeartbeatAtTick = 1; // first heartbeat will open tick 1
   107	        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
   108	        // i.e. ticks [100, 110)
   109	        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
   110	        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
   111	        _world.winterActive = false;
   112	        _treasury.treasuryOwner = msg.sender;
   113	        _nextClanId = 1;
   114	        _nextClansmanId = 1;
   115	        _nextOtcProposalId = 1;
   116	    }
   117	
   118	    // =========================================================================
   119	    // TRAVEL DISTANCE MATRIX
   120	    // =========================================================================
   121	
   122	    // Precomputed BFS distances for 8 regions (1-indexed; index 0 unused).
   123	    // Adjacency:
   124	    //   Forest(1):       Mountains(2), WestFarms(4)
   125	    //   Mountains(2):    Forest(1), UnicornTown(3)
   126	    //   UnicornTown(3):  Mountains(2), WestFarms(4), EastFarms(5)
   127	    //   WestFarms(4):    Forest(1), UnicornTown(3), WestDocks(6)
   128	    //   EastFarms(5):    UnicornTown(3), EastDocks(7)
   129	    //   WestDocks(6):    WestFarms(4), DeepSea(8)
   130	    //   EastDocks(7):    EastFarms(5), DeepSea(8)
   131	    //   DeepSea(8):      WestDocks(6), EastDocks(7)
   132	    //
   133	    // Distance table dist[src][dst] — 0-indexed internally (region - 1).
   134	    // Distance of 0 = same region.
   135	    //
   136	    // Full BFS-computed 8x8 matrix:
   137	    //   src\dst  1  2  3  4  5  6  7  8
   138	    //      1     0  1  2  1  3  2  4  3
   139	    //      2     1  0  1  2  2  3  3  4
   140	    //      3     2  1  0  1  1  2  2  3
   141	    //      4     1  2  1  0  2  1  3  2
   142	    //      5     3  2  1  2  0  3  1  2
   143	    //      6     2  3  2  1  3  0  2  1
   144	    //      7     4  3  2  3  1  2  0  1
   145	    //      8     3  4  3  2  2  1  1  0
   146	
   147	    function _distMatrix(uint8 src, uint8 dst) private pure returns (uint8) {
   148	        if (src == dst) return 0;
   149	        // Encode as (src-1)*8 + (dst-1), values from BFS
   150	        uint8[64] memory d = [
   151	            // src=1: to 1,2,3,4,5,6,7,8
   152	            0,
   153	            1,
   154	            2,
   155	            1,
   156	            3,
   157	            2,
   158	            4,
   159	            3,
   160	            // src=2: to 1,2,3,4,5,6,7,8

exec
/bin/bash -lc "git show 2bec876:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '460,760p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   460	            _gatherWood(clan, cs, m, clanId, tick, starving, tickSeed);
   461	        } else if (action == ActionType.MineIron) {
   462	            _gatherIron(clan, cs, m, clanId, tick, starving, tickSeed);
   463	        } else if (action == ActionType.FishDocks) {
   464	            _gatherFishDocks(clan, cs, m, clanId, tick, starving, tickSeed);
   465	        } else if (action == ActionType.FishDeepSea) {
   466	            _gatherFishDeepSea(clan, cs, m, clanId, tick, starving, tickSeed);
   467	        } else if (action == ActionType.HarvestWheat) {
   468	            _gatherWheat(clan, cs, m, clanId, tick, starving);
   469	        } else if (action == ActionType.DepositResources) {
   470	            _doDeposit(clan, cs, m, clanId, tick);
   471	        } else if (action == ActionType.Wait) {
   472	            // NOOP — worker stays ACTING (continuous), no transition needed
   473	            // Wait mission is effectively persistent until interrupted
   474	        } else if (action == ActionType.DefendBase) {
   475	            // Persistent mission. Registration happens atomically at order submission.
   476	        } else if (
   477	            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
   478	        ) {
   479	            // Phase 1 stub: check homebase, check resources; if ok, stub success
   480	            _doBuilding(clan, cs, m, clanId, tick, action);
   481	        } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
   482	            // Scheduled market actions: already enqueued at submitClanOrders time.
   483	            // Settlement resolves this action slot — just complete the mission.
   484	            // (Actual execution happened or will happen at heartbeat.)
   485	            _completeMission(cs, m);
   486	        }
   487	    }
   488	
   489	    // -------------------------------------------------------------------------
   490	    // Gathering helpers
   491	    // -------------------------------------------------------------------------
   492	
   493	    function _gatherWood(
   494	        Clan storage, // clan (unused — no clan-level mutation in wood gather)
   495	        Clansman storage cs,
   496	        Mission storage m,
   497	        uint32 clanId,
   498	        uint64 tick,
   499	        bool starving,
   500	        bytes32 tickSeed
   501	    ) internal {
   502	        if (cs.carryWood >= ClanWorldConstants.CLANSMAN_CARRY_CAP) {
   503	            _completeMission(cs, m);
   504	            return;
   505	        }
   506	
   507	        uint256 remaining = ClanWorldConstants.CLANSMAN_CARRY_CAP - cs.carryWood;
   508	        uint256 yield = ClanWorldConstants.WOOD_YIELD_PER_TICK * uint256(getActionDuration(ActionType.ChopWood));
   509	        bytes32 woodRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
   510	        if (uint256(woodRng) % 10000 < ClanWorldConstants.WOOD_CRIT_BPS) {
   511	            yield *= 2;
   512	        }
   513	
   514	        if (starving) yield = yield / 2;
   515	        if (yield > remaining) yield = remaining;
   516	        cs.carryWood += yield;
   517	
   518	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.ChopWood, yield, 0, 0, 0, 0, tick);
   519	
   520	        if (cs.carryWood >= ClanWorldConstants.CLANSMAN_CARRY_CAP) {
   521	            _completeMission(cs, m);
   522	        }
   523	    }
   524	
   525	    function _gatherIron(
   526	        Clan storage clan,
   527	        Clansman storage cs,
   528	        Mission storage m,
   529	        uint32 clanId,
   530	        uint64 tick,
   531	        bool starving,
   532	        bytes32 tickSeed
   533	    ) internal {
   534	        uint256 remaining = ClanWorldConstants.IRON_CAP - cs.carryIron;
   535	        if (remaining == 0) {
   536	            _completeMission(cs, m);
   537	            return;
   538	        }
   539	        uint256 yield = ClanWorldConstants.IRON_BASE_YIELD;
   540	        if (starving) yield = yield / 2;
   541	        if (yield > remaining) yield = remaining;
   542	        cs.carryIron += yield;
   543	
   544	        // Gold bonus roll — scoped to reduce stack depth
   545	        uint256 goldBonus = _rollIronGoldBonus(clan, cs.clansmanId, m.nonce, tick, tickSeed);
   546	
   547	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.MineIron, 0, yield, 0, 0, goldBonus, tick);
   548	
   549	        if (cs.carryIron >= ClanWorldConstants.IRON_CAP) {
   550	            _completeMission(cs, m);
   551	        }
   552	    }
   553	
   554	    function _rollIronGoldBonus(Clan storage clan, uint32 clansmanId, uint64 nonce, uint64 tick, bytes32 tickSeed)
   555	        internal
   556	        returns (uint256 goldBonus)
   557	    {
   558	        bytes32 goldRng = keccak256(abi.encode("iron_gold_bonus", tickSeed, clansmanId, nonce, tick));
   559	        if (uint256(goldRng) % 10000 < ClanWorldConstants.GOLD_FROM_IRON_BPS) {
   560	            goldBonus = ClanWorldConstants.GOLD_FROM_IRON_AMOUNT;
   561	            clan.goldBalance += goldBonus;
   562	        }
   563	    }
   564	
   565	    function _gatherFishDocks(
   566	        Clan storage,
   567	        Clansman storage cs,
   568	        Mission storage m,
   569	        uint32 clanId,
   570	        uint64 tick,
   571	        bool starving,
   572	        bytes32 tickSeed
   573	    ) internal {
   574	        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
   575	        if (remaining == 0) {
   576	            _completeMission(cs, m);
   577	            return;
   578	        }
   579	        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
   580	        uint256 fishRoll = uint256(fishRng) % 10000;
   581	        uint256 yield = 0;
   582	        if (fishRoll < ClanWorldConstants.FISH_DOCKS_BPS) {
   583	            yield = 1e18;
   584	        }
   585	        if (starving) yield = yield / 2;
   586	        if (yield > remaining) yield = remaining;
   587	        if (yield > 0) {
   588	            cs.carryFish += yield;
   589	            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDocks, 0, 0, 0, yield, 0, tick);
   590	        }
   591	        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
   592	            _completeMission(cs, m);
   593	        }
   594	    }
   595	
   596	    function _gatherFishDeepSea(
   597	        Clan storage,
   598	        Clansman storage cs,
   599	        Mission storage m,
   600	        uint32 clanId,
   601	        uint64 tick,
   602	        bool starving,
   603	        bytes32 tickSeed
   604	    ) internal {
   605	        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
   606	        if (remaining == 0) {
   607	            _completeMission(cs, m);
   608	            return;
   609	        }
   610	        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
   611	        uint256 fishRoll = uint256(fishRng) % 10000;
   612	        uint256 yield = 0;
   613	        if (fishRoll < ClanWorldConstants.FISH_DEEP_BPS) {
   614	            yield = 1e18;
   615	        }
   616	        if (starving) yield = yield / 2;
   617	        if (yield > remaining) yield = remaining;
   618	        if (yield > 0) {
   619	            cs.carryFish += yield;
   620	            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDeepSea, 0, 0, 0, yield, 0, tick);
   621	        }
   622	        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
   623	            _completeMission(cs, m);
   624	        }
   625	    }
   626	
   627	    function _gatherWheat(
   628	        Clan storage,
   629	        /* clan — unused but kept positional for callsite parity */
   630	        Clansman storage cs,
   631	        Mission storage m,
   632	        uint32 clanId,
   633	        uint64 tick,
   634	        bool starving
   635	    ) internal {
   636	        uint256 remaining = ClanWorldConstants.WHEAT_CAP - cs.carryWheat;
   637	        if (remaining == 0) {
   638	            _completeMission(cs, m);
   639	            return;
   640	        }
   641	        // Determine plot index from region
   642	        uint8 region = m.targetRegion;
   643	        uint256 plotIdx;
   644	        if (region == ClanWorldConstants.REGION_WEST_FARMS) {
   645	            plotIdx = 0;
   646	        } else if (region == ClanWorldConstants.REGION_EAST_FARMS) {
   647	            plotIdx = 1;
   648	        } else {
   649	            // Wrong region — complete (no harvest)
   650	            _completeMission(cs, m);
   651	            return;
   652	        }
   653	
   654	        WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
   655	        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
   656	            // Plot not ready — worker waits
   657	            _completeMission(cs, m);
   658	            return;
   659	        }
   660	
   661	        uint256 yield = WHEAT_HARVEST_RATE;
   662	        if (starving) yield = yield / 2;
   663	        if (yield > remaining) yield = remaining;
   664	        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
   665	
   666	        cs.carryWheat += yield;
   667	        plot.remainingWheat -= yield;
   668	
   669	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
   670	
   671	        if (plot.remainingWheat == 0) {
   672	            plot.state = WheatPlotState.Regrowing;
   673	            plot.regrowUntilTick = tick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
   674	        }
   675	
   676	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
   677	            _completeMission(cs, m);
   678	        }
   679	        // else continuous
   680	    }
   681	
   682	    function _doDeposit(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint64 tick)
   683	        internal
   684	    {
   685	        // Must be at homebase region
   686	        if (cs.currentRegion != clan.baseRegion) {
   687	            _completeMission(cs, m);
   688	            return;
   689	        }
   690	        bool hasAnything = cs.carryWood > 0 || cs.carryIron > 0 || cs.carryWheat > 0 || cs.carryFish > 0;
   691	        if (!hasAnything) {
   692	            // Empty deposits are silent no-ops; no zero-delta event for indexers to process.
   693	            _completeMission(cs, m);
   694	            return;
   695	        }
   696	
   697	        uint256 woodDelta = cs.carryWood;
   698	        uint256 ironDelta = cs.carryIron;
   699	        uint256 wheatDelta = cs.carryWheat;
   700	        uint256 fishDelta = cs.carryFish;
   701	
   702	        clan.vaultWood += woodDelta;
   703	        clan.vaultIron += ironDelta;
   704	        clan.vaultWheat += wheatDelta;
   705	        clan.vaultFish += fishDelta;
   706	
   707	        cs.carryWood = 0;
   708	        cs.carryIron = 0;
   709	        cs.carryWheat = 0;
   710	        cs.carryFish = 0;
   711	
   712	        emit ResourcesDeposited(clanId, cs.clansmanId, woodDelta, ironDelta, wheatDelta, fishDelta, tick);
   713	        _completeMission(cs, m);
   714	    }
   715	
   716	    function _doBuilding(
   717	        Clan storage clan,
   718	        Clansman storage cs,
   719	        Mission storage m,
   720	        uint32 clanId,
   721	        uint64 tick,
   722	        ActionType action
   723	    ) internal {
   724	        // Must be at homebase
   725	        if (cs.currentRegion != clan.baseRegion) {
   726	            _completeMission(cs, m);
   727	            return;
   728	        }
   729	
   730	        bool success = false;
   731	        if (action == ActionType.BuildWall) {
   732	            success = _tryBuildWall(clan, clanId, tick);
   733	        } else if (action == ActionType.UpgradeBase) {
   734	            success = _tryUpgradeBase(clan, clanId, tick);
   735	        } else if (action == ActionType.UpgradeMonument) {
   736	            success = _tryUpgradeMonument(clan, clanId, tick);
   737	        }
   738	
   739	        if (!success) {
   740	            // Resources missing — worker transitions to WAITING
   741	        }
   742	        _completeMission(cs, m);
   743	    }
   744	
   745	    function _tryBuildWall(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
   746	        uint8 nextLevel = clan.wallLevel + 1;
   747	        if (nextLevel > 5) return false;
   748	
   749	        uint256 woodCost;
   750	        uint256 ironCost;
   751	
   752	        if (nextLevel == 1) {
   753	            woodCost = 20e18;
   754	            ironCost = 0;
   755	        } else if (nextLevel == 2) {
   756	            woodCost = 35e18;
   757	            ironCost = 0;
   758	        } else if (nextLevel == 3) {
   759	            woodCost = 30e18;
   760	            ironCost = 5e18;

exec
/bin/bash -lc "git show 2bec876:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1560,1665p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1560	            return;
  1561	        }
  1562	
  1563	        uint256 goldOut = StubPool(poolAddr).sellResource(amount);
  1564	        clan.goldBalance += goldOut;
  1565	
  1566	        emit ScheduledMarketActionExecuted(
  1567	            closedTick, commitSequence, clanId, clansmanId, token, _treasury.goldToken, amount, goldOut
  1568	        );
  1569	    }
  1570	
  1571	    /// @dev Execute a scheduled market buy: deduct gold from purse, credit resource to vault.
  1572	    function _executeMarketBuy(
  1573	        uint64 closedTick,
  1574	        uint32 clanId,
  1575	        uint32 clansmanId,
  1576	        address token,
  1577	        uint256 amountOut,
  1578	        uint256 maxGoldIn,
  1579	        uint64 commitSequence
  1580	    ) internal {
  1581	        if (!_treasury.poolsSeeded) {
  1582	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
  1583	            return;
  1584	        }
  1585	        address poolAddr = _poolFor(token);
  1586	        if (poolAddr == address(0)) {
  1587	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
  1588	            return;
  1589	        }
  1590	
  1591	        // Quote gold cost without updating reserves
  1592	        uint256 goldIn = StubPool(poolAddr).quoteBuy(amountOut);
  1593	
  1594	        Clan storage clan = _clans[clanId];
  1595	
  1596	        if (goldIn > maxGoldIn) {
  1597	            emit MarketActionFailed(
  1598	                clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_BUY_MAX_GOLD_EXCEEDED
  1599	            );
  1600	            return;
  1601	        }
  1602	        if (clan.goldBalance < goldIn) {
  1603	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_NOT_ENOUGH_GOLD);
  1604	            return;
  1605	        }
  1606	
  1607	        // Execute — use return value to guard against any future pool divergence
  1608	        uint256 actualGoldIn = StubPool(poolAddr).buyResource(amountOut);
  1609	        clan.goldBalance -= actualGoldIn;
  1610	        _addToVault(clan, token, amountOut);
  1611	
  1612	        emit ScheduledMarketActionExecuted(
  1613	            closedTick, commitSequence, clanId, clansmanId, _treasury.goldToken, token, actualGoldIn, amountOut
  1614	        );
  1615	    }
  1616	
  1617	    function _validateAction(Clan storage clan, Clansman storage cs, ClanOrder calldata order, uint8 gotoRegion)
  1618	        internal
  1619	        view
  1620	        returns (StatusCode)
  1621	    {
  1622	        ActionType action = order.action;
  1623	
  1624	        if (action == ActionType.None) return StatusCode.ERR_INVALID_ACTION;
  1625	
  1626	        // DepositResources: must go to homebase
  1627	        if (action == ActionType.DepositResources) {
  1628	            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
  1629	        }
  1630	
  1631	        // BuildWall / UpgradeBase / UpgradeMonument: must go to homebase
  1632	        if (action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument)
  1633	        {
  1634	            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
  1635	        }
  1636	
  1637	        // ChopWood: must go to Forest
  1638	        if (action == ActionType.ChopWood) {
  1639	            if (gotoRegion != ClanWorldConstants.REGION_FOREST) return StatusCode.ERR_INVALID_REGION;
  1640	        }
  1641	        // MineIron: must go to Mountains
  1642	        if (action == ActionType.MineIron) {
  1643	            if (gotoRegion != ClanWorldConstants.REGION_MOUNTAINS) return StatusCode.ERR_INVALID_REGION;
  1644	        }
  1645	        // FishDocks: must go to WestDocks or EastDocks
  1646	        if (action == ActionType.FishDocks) {
  1647	            if (
  1648	                gotoRegion != ClanWorldConstants.REGION_WEST_DOCKS && gotoRegion != ClanWorldConstants.REGION_EAST_DOCKS
  1649	            ) {
  1650	                return StatusCode.ERR_INVALID_REGION;
  1651	            }
  1652	        }
  1653	        // FishDeepSea: must go to DeepSea
  1654	        if (action == ActionType.FishDeepSea) {
  1655	            if (gotoRegion != ClanWorldConstants.REGION_DEEP_SEA) return StatusCode.ERR_INVALID_REGION;
  1656	        }
  1657	        // HarvestWheat: must go to WestFarms or EastFarms
  1658	        if (action == ActionType.HarvestWheat) {
  1659	            if (
  1660	                gotoRegion != ClanWorldConstants.REGION_WEST_FARMS && gotoRegion != ClanWorldConstants.REGION_EAST_FARMS
  1661	            ) {
  1662	                return StatusCode.ERR_INVALID_REGION;
  1663	            }
  1664	        }
  1665	

exec
/bin/bash -lc "git show 2bec876:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '560,760p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   560	        uint8 targetRegion,
   561	        uint64 startTick,
   562	        uint64 arrivalTick
   563	    );
   564	    event MissionInterrupted(
   565	        uint32 indexed clanId, uint32 indexed clansmanId, uint64 oldMissionNonce, uint64 newMissionNonce
   566	    );
   567	    event MissionCompleted(uint32 indexed clanId, uint32 indexed clansmanId, uint64 missionNonce, ActionType action);
   568	    event WorkerArrived(uint32 indexed clanId, uint32 indexed clansmanId, uint8 region, uint64 tick);
   569	
   570	    // ----- gathering / vault movement -----
   571	    event ResourcesGathered(
   572	        uint32 indexed clanId,
   573	        uint32 indexed clansmanId,
   574	        ActionType action,
   575	        uint256 woodGained,
   576	        uint256 ironGained,
   577	        uint256 wheatGained,
   578	        uint256 fishGained,
   579	        uint256 goldBonus,
   580	        uint64 atTick
   581	    );
   582	    event ResourcesDeposited(
   583	        uint32 indexed clanId,
   584	        uint32 indexed clansmanId,
   585	        uint256 woodDelta,
   586	        uint256 ironDelta,
   587	        uint256 wheatDelta,
   588	        uint256 fishDelta,
   589	        uint64 atTick
   590	    );
   591	
   592	    // ----- building -----
   593	    event WallLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
   594	    event BaseLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
   595	    event MonumentLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
   596	
   597	    // ----- market -----
   598	    event ImmediateMarketActionExecuted(
   599	        uint32 indexed clanId,
   600	        uint32 indexed clansmanId,
   601	        address tokenIn,
   602	        address tokenOut,
   603	        uint256 amountIn,
   604	        uint256 amountOut,
   605	        uint64 atTick
   606	    );
   607	    event ScheduledMarketActionExecuted(
   608	        uint64 indexed executeAtTick,
   609	        uint64 indexed commitSequence,
   610	        uint32 indexed clanId,
   611	        uint32 clansmanId,
   612	        address tokenIn,
   613	        address tokenOut,
   614	        uint256 amountIn,
   615	        uint256 amountOut
   616	    );
   617	    event ScheduledMarketActionCommitted(
   618	        uint64 indexed executeAtTick,
   619	        uint64 indexed commitSequence,
   620	        uint32 indexed clanId,
   621	        uint32 clansmanId,
   622	        ActionType action,
   623	        address marketToken,
   624	        uint256 marketAmount,
   625	        uint256 maxGoldIn
   626	    );
   627	    event MarketActionFailed(uint32 indexed clanId, uint32 indexed clansmanId, ActionType action, StatusCode reason);
   628	    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
   629	    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);
   630	
   631	    // ----- bandits -----
   632	    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
   633	    event BanditStateChanged(
   634	        uint32 indexed banditId, BanditState oldState, BanditState newState, uint8 region, uint64 atTick
   635	    );
   636	    event BanditMoved(uint32 indexed banditId, uint8 fromRegion, uint8 toRegion, uint64 atTick);
   637	    event BanditAttackResolved(
   638	        uint32 indexed banditId,
   639	        uint32 indexed targetClanId,
   640	        bool defended,
   641	        uint16 attackPower,
   642	        uint16 totalDefense,
   643	        uint16 wallLevelAfter,
   644	        uint256 stolenWood,
   645	        uint256 stolenIron,
   646	        uint256 stolenWheat,
   647	        uint256 stolenFish,
   648	        uint64 atTick
   649	    );
   650	    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
   651	    event BanditEscaped(uint32 indexed banditId, uint64 atTick);
   652	    event BlueprintAwarded(uint32 indexed clanId, uint32 indexed banditId, uint256 amount);
   653	    event LootDistributedToDefender(
   654	        uint32 indexed banditId,
   655	        uint32 indexed clanId,
   656	        uint32 indexed clansmanId,
   657	        uint256 wood,
   658	        uint256 iron,
   659	        uint256 wheat,
   660	        uint256 fish
   661	    );
   662	
   663	    // ----- winter cold damage -----
   664	    event ColdDamageApplied(uint32 indexed clanId, uint16 oldDamage, uint16 newDamage, uint64 atTick);
   665	    event ClansmanDiedFromCold(uint32 indexed clanId, uint64 atTick);
   666	
   667	    // ----- OTC transfers -----
   668	    event GoldTransferProposed(
   669	        uint256 indexed proposalId,
   670	        uint32 indexed fromClanId,
   671	        uint32 indexed toClanId,
   672	        uint256 amount,
   673	        uint256 expiryTick
   674	    );
   675	    event GoldTransferAccepted(
   676	        uint256 indexed proposalId,
   677	        uint32 indexed fromClanId,
   678	        uint32 indexed toClanId,
   679	        uint256 amount,
   680	        uint64 settledAtTick
   681	    );
   682	    event GoldTransferCancelled(uint256 indexed proposalId);
   683	    event VaultTransferProposed(
   684	        uint256 indexed proposalId,
   685	        uint32 indexed fromClanId,
   686	        uint32 indexed toClanId,
   687	        uint256 wood,
   688	        uint256 wheat,
   689	        uint256 fish,
   690	        uint256 iron,
   691	        uint64 expiryTick
   692	    );
   693	    event VaultTransferAccepted(
   694	        uint256 indexed proposalId,
   695	        uint32 indexed fromClanId,
   696	        uint32 indexed toClanId,
   697	        uint256 wood,
   698	        uint256 wheat,
   699	        uint256 fish,
   700	        uint256 iron,
   701	        uint64 settledAtTick
   702	    );
   703	    event VaultTransferCancelled(uint256 indexed proposalId);
   704	    event BlueprintTransferProposed(
   705	        uint256 indexed proposalId,
   706	        uint32 indexed fromClanId,
   707	        uint32 indexed toClanId,
   708	        uint256 amount,
   709	        uint64 expiryTick
   710	    );
   711	    event BlueprintTransferAccepted(
   712	        uint256 indexed proposalId,
   713	        uint32 indexed fromClanId,
   714	        uint32 indexed toClanId,
   715	        uint256 amount,
   716	        uint64 settledAtTick
   717	    );
   718	    event BlueprintTransferCancelled(uint256 indexed proposalId);
   719	    event BundledTransferProposed(
   720	        uint256 indexed proposalId,
   721	        uint32 indexed fromClanId,
   722	        uint32 indexed toClanId,
   723	        uint256 gold,
   724	        uint256 wood,
   725	        uint256 wheat,
   726	        uint256 fish,
   727	        uint256 iron,
   728	        uint256 blueprint,
   729	        uint64 expiryTick
   730	    );
   731	    event BundledTransferAccepted(
   732	        uint256 indexed proposalId,
   733	        uint32 indexed fromClanId,
   734	        uint32 indexed toClanId,
   735	        uint256 gold,
   736	        uint256 wood,
   737	        uint256 wheat,
   738	        uint256 fish,
   739	        uint256 iron,
   740	        uint256 blueprint,
   741	        uint64 settledAtTick
   742	    );
   743	    event BundledTransferCancelled(uint256 indexed proposalId);
   744	    event GoldTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
   745	    event VaultResourceTransferred(
   746	        uint32 indexed fromClanId, uint32 indexed toClanId, ResourceType resource, uint256 amount, uint64 atTick
   747	    );
   748	    event BlueprintTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
   749	
   750	    // ----- treasury / pools -----
   751	    event PoolsSeeded(address woodGoldPool, address wheatGoldPool, address fishGoldPool, address ironGoldPool);
   752	}
   753	
   754	// =============================================================================
   755	// MAIN INTERFACE
   756	// =============================================================================
   757	
   758	interface IClanWorld is IClanWorldEvents {
   759	    // -------------------------------------------------------------------------
   760	    // World progression

exec
/bin/bash -lc "git show 2bec876:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '780,940p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   780	
   781	    /// @notice Mint a new clan iNFT and spawn its homebase in a valid region.
   782	    function mintClan(address to) external returns (uint32 clanId, uint256 iftTokenId);
   783	
   784	    /// @notice Submit one or more orders for a single clan's clansmen.
   785	    ///         Per-order failures do not revert the tx.
   786	    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders) external returns (OrderResult[] memory);
   787	
   788	    // -------------------------------------------------------------------------
   789	    // Treasury / pool seeding
   790	    // -------------------------------------------------------------------------
   791	
   792	    /// @notice Owner-only. Registers token and pool addresses once before seeding.
   793	    ///         tokens order: wood, iron, wheat, fish, gold, blueprint.
   794	    ///         pools order: wood, wheat, fish, iron.
   795	    function initTreasury(address[6] calldata tokens, address[4] calldata pools) external;
   796	
   797	    /// @notice Owner-only. Seeds the four Unicorn Town pools at deploy time.
   798	    function seedPools(PoolSeedConfig calldata cfg) external;
   799	
   800	    // -------------------------------------------------------------------------
   801	    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
   802	    // -------------------------------------------------------------------------
   803	
   804	    function proposeGoldTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint256 expiryTick)
   805	        external
   806	        returns (uint256 proposalId);
   807	
   808	    function acceptGoldTransfer(uint256 proposalId) external;
   809	
   810	    function cancelGoldTransfer(uint256 proposalId) external;
   811	
   812	    function proposeVaultTransfer(
   813	        uint32 fromClanId,
   814	        uint32 toClanId,
   815	        uint256 woodAmt,
   816	        uint256 wheatAmt,
   817	        uint256 fishAmt,
   818	        uint256 ironAmt,
   819	        uint64 expiryTick
   820	    ) external returns (uint256 proposalId);
   821	
   822	    function acceptVaultTransfer(uint256 proposalId) external;
   823	
   824	    function cancelVaultTransfer(uint256 proposalId) external;
   825	
   826	    function proposeBlueprintTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint64 expiryTick)
   827	        external
   828	        returns (uint256 proposalId);
   829	
   830	    function acceptBlueprintTransfer(uint256 proposalId) external;
   831	
   832	    function cancelBlueprintTransfer(uint256 proposalId) external;
   833	
   834	    function proposeBundledTransfer(
   835	        uint32 fromClanId,
   836	        uint32 toClanId,
   837	        uint256 gold,
   838	        uint256 wood,
   839	        uint256 wheat,
   840	        uint256 fish,
   841	        uint256 iron,
   842	        uint256 blueprint,
   843	        uint64 expiryTick
   844	    ) external returns (uint256 proposalId);
   845	
   846	    function acceptBundledTransfer(uint256 proposalId) external;
   847	
   848	    function cancelBundledTransfer(uint256 proposalId) external;
   849	
   850	    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
   851	
   852	    function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
   853	
   854	    function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
   855	
   856	    function transferBundle(
   857	        uint32 fromClanId,
   858	        uint32 toClanId,
   859	        uint256 gold,
   860	        uint256 blueprint,
   861	        uint256 wood,
   862	        uint256 iron,
   863	        uint256 wheat,
   864	        uint256 fish
   865	    ) external;
   866	
   867	    // -------------------------------------------------------------------------
   868	    // Raw read getters (committed storage, no settlement simulation)
   869	    // -------------------------------------------------------------------------
   870	
   871	    function getWorldState() external view returns (WorldState memory);
   872	
   873	    function getTreasuryState() external view returns (TreasuryState memory);
   874	
   875	    function getResourceToken(uint8 resourceType) external view returns (address);
   876	
   877	    function getPool(uint8 resourceType) external view returns (address);
   878	
   879	    function getPrice(uint8 resourceType, uint256 amountIn) external view returns (uint256 amountOut);
   880	
   881	    function getClan(uint32 clanId) external view returns (Clan memory);
   882	
   883	    function getClansman(uint32 clansmanId) external view returns (Clansman memory);
   884	
   885	    function getActiveMission(uint32 clansmanId) external view returns (Mission memory);
   886	
   887	    function getMissionTiming(uint32 clanId, uint32 clansmanId)
   888	        external
   889	        view
   890	        returns (uint64 submitted, uint64 executes, uint64 settles);
   891	
   892	    function getActionDuration(ActionType action) external pure returns (uint64);
   893	
   894	    function getTravelTicks(uint8 fromRegion, uint8 toRegion) external pure returns (uint64);
   895	
   896	    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
   897	
   898	    function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east);
   899	
   900	    function getScheduledMarketActionsForTick(uint64 tick) external view returns (ScheduledMarketAction[] memory);
   901	
   902	    function getOtcGoldProposal(uint256 proposalId) external view returns (OtcProposal memory);
   903	
   904	    function getOtcVaultTransferProposal(uint256 proposalId) external view returns (VaultTransferProposal memory);
   905	
   906	    function getOtcBlueprintTransferProposal(uint256 proposalId)
   907	        external
   908	        view
   909	        returns (BlueprintTransferProposal memory);
   910	
   911	    function getOtcBundledTransferProposal(uint256 proposalId) external view returns (BundledTransferProposal memory);
   912	
   913	    function getActiveDefenders(uint32 targetClanId) external view returns (uint32[] memory clansmanIds);
   914	
   915	    function getDefendingClans(uint8 region) external view returns (uint32[] memory clanIds);
   916	
   917	    // -------------------------------------------------------------------------
   918	    // Derived read getters (read-only simulation forward to current tick)
   919	    //
   920	    // Per v4.3 §J ("Derived Getter Non-Mutation Rule"), these MUST NOT mutate
   921	    // any storage, including settlement checkpoints, cached flags, or queues.
   922	    // -------------------------------------------------------------------------
   923	
   924	    function getDerivedClanState(uint32 clanId) external view returns (DerivedClanState memory);
   925	
   926	    function getDerivedClansmanState(uint32 clansmanId) external view returns (DerivedClansmanState memory);
   927	
   928	    /// @notice Non-binding preview. Bandit targeting is recomputed at attack
   929	    ///         resolution time using then-current eagerly settled state.
   930	    function getBanditTargetPreview(uint32 banditId) external view returns (uint32 previewClanId);
   931	
   932	    function quoteTravel(uint8 srcRegion, uint8 dstRegion) external view returns (uint8 travelTicks, bytes8 path);
   933	
   934	    function quoteLootValueRaw(uint32 clanId) external view returns (uint256 lootValue);
   935	
   936	    function quoteLootValueSettled(uint32 clanId) external view returns (uint256 lootValue);
   937	
   938	    // -------------------------------------------------------------------------
   939	    // UI indexer aggregator getters (v4.4 additions)
   940	    //

exec
/bin/bash -lc 'git show 2bec876:packages/contracts/abi/IClanWorld.json | rg -n "proposeGoldTransfer|GoldTransferProposed|expiryTick|proposeVaultTransfer|proposeBlueprintTransfer|proposeBundledTransfer"' in /home/claude/code/clan-world
 succeeded in 0ms:
1754:              "name": "expiryTick",
1830:              "name": "expiryTick",
1881:              "name": "expiryTick",
1947:              "name": "expiryTick",
2570:      "name": "proposeBlueprintTransfer",
2588:          "name": "expiryTick",
2604:      "name": "proposeBundledTransfer",
2647:          "name": "expiryTick",
2663:      "name": "proposeGoldTransfer",
2681:          "name": "expiryTick",
2697:      "name": "proposeVaultTransfer",
2730:          "name": "expiryTick",
3448:          "name": "expiryTick",
3626:          "name": "expiryTick",
3836:      "name": "GoldTransferProposed",
3863:          "name": "expiryTick",
4676:          "name": "expiryTick",

codex
I’ve confirmed the interface surface and now I’m checking the generated ABI for any parameter width drift that could silently break off-chain clients after merge.
exec
/bin/bash -lc "git show 2bec876:packages/contracts/abi/IClanWorld.json | nl -ba | sed -n '2655,2695p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  2655	          "type": "uint256",
  2656	          "internalType": "uint256"
  2657	        }
  2658	      ],
  2659	      "stateMutability": "nonpayable"
  2660	    },
  2661	    {
  2662	      "type": "function",
  2663	      "name": "proposeGoldTransfer",
  2664	      "inputs": [
  2665	        {
  2666	          "name": "fromClanId",
  2667	          "type": "uint32",
  2668	          "internalType": "uint32"
  2669	        },
  2670	        {
  2671	          "name": "toClanId",
  2672	          "type": "uint32",
  2673	          "internalType": "uint32"
  2674	        },
  2675	        {
  2676	          "name": "amount",
  2677	          "type": "uint256",
  2678	          "internalType": "uint256"
  2679	        },
  2680	        {
  2681	          "name": "expiryTick",
  2682	          "type": "uint256",
  2683	          "internalType": "uint256"
  2684	        }
  2685	      ],
  2686	      "outputs": [
  2687	        {
  2688	          "name": "proposalId",
  2689	          "type": "uint256",
  2690	          "internalType": "uint256"
  2691	        }
  2692	      ],
  2693	      "stateMutability": "nonpayable"
  2694	    },
  2695	    {

web search: 
web search: 
exec
/bin/bash -lc "git show 2bec876:packages/contracts/src/ClanWorld.sol | rg -n \"\\.mint\\(|\\.burn\\(|ResourceMinted|ResourceBurned|goldToken|blueprintToken\"" in /home/claude/code/clan-world
 succeeded in 0ms:
1567:            closedTick, commitSequence, clanId, clansmanId, token, _treasury.goldToken, amount, goldOut
1613:            closedTick, commitSequence, clanId, clansmanId, _treasury.goldToken, token, actualGoldIn, amountOut
1679:            if (tok == address(0) || tok == _treasury.goldToken) {
1723:        _treasury.goldToken = tokens[4];
1724:        _treasury.blueprintToken = tokens[5];
1739:        _transferSeed(_treasury.goldToken, _treasury.woodGoldPool, cfg.goldSeedForWood);
1741:        _transferSeed(_treasury.goldToken, _treasury.wheatGoldPool, cfg.goldSeedForWheat);
1743:        _transferSeed(_treasury.goldToken, _treasury.fishGoldPool, cfg.goldSeedForFish);
1745:        _transferSeed(_treasury.goldToken, _treasury.ironGoldPool, cfg.goldSeedForIron);

web search: 
web search: 
exec
/bin/bash -lc 'git show 2bec876:packages/contracts/src/ClanWorld.sol | rg -n "ERR_CARRY_FULL|CLANSMAN_CARRY_CAP|WOOD_CAP|IRON_CAP|WHEAT_CAP|FISH_CAP"' in /home/claude/code/clan-world
 succeeded in 0ms:
502:        if (cs.carryWood >= ClanWorldConstants.CLANSMAN_CARRY_CAP) {
507:        uint256 remaining = ClanWorldConstants.CLANSMAN_CARRY_CAP - cs.carryWood;
520:        if (cs.carryWood >= ClanWorldConstants.CLANSMAN_CARRY_CAP) {
534:        uint256 remaining = ClanWorldConstants.IRON_CAP - cs.carryIron;
549:        if (cs.carryIron >= ClanWorldConstants.IRON_CAP) {
574:        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
591:        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
605:        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
622:        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
636:        uint256 remaining = ClanWorldConstants.WHEAT_CAP - cs.carryWheat;
676:        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {

web search: 
web search: 
web search: 
web search: 
web search: 
web search: 
exec
/bin/bash -lc 'git show 2bec876:packages/contracts/src/ClanWorld.sol | rg -n "proposal.accepted|proposal.cancelled|accepted =|cancelled ="' in /home/claude/code/clan-world
 succeeded in 0ms:
1802:        require(!proposal.accepted, "ClanWorld: proposal accepted");
1803:        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
1830:        require(!proposal.accepted, "ClanWorld: proposal accepted");
1831:        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
1881:        require(!proposal.accepted, "ClanWorld: proposal accepted");
1882:        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
1920:        require(!proposal.accepted, "ClanWorld: proposal accepted");
1921:        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
1968:        require(!proposal.accepted, "ClanWorld: proposal accepted");
1969:        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
1996:        require(!proposal.accepted, "ClanWorld: proposal accepted");
1997:        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
2053:        require(!proposal.accepted, "ClanWorld: proposal accepted");
2054:        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
2098:        require(!proposal.accepted, "ClanWorld: proposal accepted");
2099:        require(!proposal.cancelled, "ClanWorld: proposal cancelled");

exec
/bin/bash -lc "git show 2bec876:packages/contracts/src/ClanWorld.sol | rg -n \"function settleClansman|function settleClan\\(\"" in /home/claude/code/clan-world
 succeeded in 0ms:
994:    function settleClan(uint32 clanId) external override nonReentrant {
1002:    function settleClansman(uint32 csId) external override nonReentrant {

codex
I’m now waiting on the parallel sub-reviews and will reconcile their findings with mine before finalizing severity and merge recommendation.
collab: Wait
collab: Wait
collab: Wait
codex
# Phase Super-Swarm Review — PR #200 (head 2bec876)

## SUMMARY
NEEDS_FIXES. The phase delivers the OTC proposal/accept/cancel flow, but there are integration risks at treasury initialization, config validation, and proposal lifecycle constraints that should be addressed before merge. I did not find a critical exploit path, but there are medium-severity correctness/operability issues plus architectural drift on legacy OTC interface methods that still hard-revert. Recommendation: fix medium findings first, then merge.

## HIGH severity findings
CLEAN — no findings.

## MEDIUM severity findings
1. `packages/contracts/src/ClanWorld.sol:1716`  
`initTreasury()` is intended as one-time init, but the sentinel is `_treasury.woodToken == address(0)`. If first init passes `tokens[0] = address(0)`, contract remains “uninitialized” by guard and can be re-initialized with different addresses later. This breaks the one-time invariant and can silently rewire treasury boundaries.  
Suggested fix: add explicit `require(tokens[i] != address(0))` for all token/pool inputs and a dedicated immutable/boolean `treasuryInitialized` flag.

2. `packages/contracts/src/ClanWorld.sol:1719`, `packages/contracts/src/ClanWorld.sol:1726`, `packages/contracts/src/ClanWorld.sol:1738`, `packages/contracts/src/StubPool.sol:27`  
No validation that configured pools match expected token pairing/engine (`TOKEN_A` resource token, `TOKEN_B` gold token, `ENGINE == address(this)`). Misordered/wrong pool addresses can produce wrong pricing routes or runtime reverts later (market/read paths), with no early fail-fast.  
Suggested fix: add interface getters on `StubPool` (or use existing publics) and validate pool/token/engine wiring during `initTreasury`/`seedPools`.

3. `packages/contracts/src/ClanWorld.sol:1768`, `packages/contracts/src/ClanWorld.sol:1842`, `packages/contracts/src/ClanWorld.sol:1940`, `packages/contracts/src/ClanWorld.sol:2008`  
Proposal creation allows already-expired proposals (no `expiryTick >= currentTick` check). These proposals can never be accepted (`...:1804`, `...:1883`, `...:1970`, `...:2055`) but still consume cap slots until manual cancel, creating avoidable OTC liveness degradation.  
Suggested fix: require `expiryTick > _world.currentTick` (or `>=`, per product rule) across all propose functions.

4. `packages/contracts/src/IClanWorld.sol:850`, `packages/contracts/src/ClanWorld.sol:2142`  
Architectural drift: interface still exposes direct OTC transfer methods (`transferGold`, `transferVaultResource`, `transferBlueprint`, `transferBundle`) but implementation hard-reverts as “not implemented.” This is a misleading public surface and integration footgun in a phase explicitly shipping OTC transfer features.  
Suggested fix: either implement these methods via proposal flow or deprecate/remove them from canonical interface/ABI.

## LOW severity findings
1. `packages/contracts/src/IClanWorld.sol:668`, `packages/contracts/src/IClanWorld.sol:804`, `packages/contracts/src/IClanWorld.sol:683`, `packages/contracts/src/IClanWorld.sol:812`  
Type consistency drift: gold proposal expiry is `uint256` in interface/event, while other OTC expiry fields are `uint64` and storage normalizes to `uint64`. This inconsistency increases client/schema friction.  
Suggested fix: standardize expiry width across all OTC APIs/events/ABI.

2. `packages/contracts/src/IClanWorld.sol:341`, `packages/contracts/src/ClanWorld.sol:1822`  
Proposal structs include `accepted`/`cancelled`, but lifecycle deletes proposals instead of transitioning flags; getters cannot expose terminal status.  
Suggested fix: either persist with status transitions, or remove status flags and define “deleted == closed” formally.

3. `packages/contracts/test/GoldTransferOtc.t.sol:161`  
Open-proposal cap behavior is tested for gold only, not vault/blueprint/bundled. This leaves a cross-type regression seam unguarded.  
Suggested fix: add one cap test per OTC type (or shared helper matrix) and include expired-proposal creation guard tests.

## Cross-cutting observations
1. Phase goal is mostly met for OTC proposal-based transfers, but canonical API clarity is not yet aligned (legacy direct-transfer methods still revert).  
2. Treasury/pool boundary hardening is the biggest integration risk area: init-time validation is too permissive and currently relies on “correct operator behavior.”  
3. Test additions are strong on happy-path OTC atomicity and dead-clan behavior, but key misconfiguration and lifecycle-edge seams remain under-tested.
2026-04-30T01:44:55.453608Z ERROR codex_core::session: failed to record rollout items: thread 019ddc0a-4ef0-7e90-8c28-71f80df60889 not found
2026-04-30T01:44:55.454041Z ERROR codex_core::session: failed to record rollout items: thread 019ddc0a-88d9-7ad3-881e-5b12dcb60f00 not found
2026-04-30T01:44:55.454365Z ERROR codex_core::session: failed to record rollout items: thread 019ddc0a-8960-7123-9c4b-5967094659ab not found
2026-04-30T01:44:55.453798Z ERROR codex_core::session: failed to record rollout items: thread 019ddc0a-89f1-74a2-abc0-999589f81eaa not found
tokens used
157,696
# Phase Super-Swarm Review — PR #200 (head 2bec876)

## SUMMARY
NEEDS_FIXES. The phase delivers the OTC proposal/accept/cancel flow, but there are integration risks at treasury initialization, config validation, and proposal lifecycle constraints that should be addressed before merge. I did not find a critical exploit path, but there are medium-severity correctness/operability issues plus architectural drift on legacy OTC interface methods that still hard-revert. Recommendation: fix medium findings first, then merge.

## HIGH severity findings
CLEAN — no findings.

## MEDIUM severity findings
1. `packages/contracts/src/ClanWorld.sol:1716`  
`initTreasury()` is intended as one-time init, but the sentinel is `_treasury.woodToken == address(0)`. If first init passes `tokens[0] = address(0)`, contract remains “uninitialized” by guard and can be re-initialized with different addresses later. This breaks the one-time invariant and can silently rewire treasury boundaries.  
Suggested fix: add explicit `require(tokens[i] != address(0))` for all token/pool inputs and a dedicated immutable/boolean `treasuryInitialized` flag.

2. `packages/contracts/src/ClanWorld.sol:1719`, `packages/contracts/src/ClanWorld.sol:1726`, `packages/contracts/src/ClanWorld.sol:1738`, `packages/contracts/src/StubPool.sol:27`  
No validation that configured pools match expected token pairing/engine (`TOKEN_A` resource token, `TOKEN_B` gold token, `ENGINE == address(this)`). Misordered/wrong pool addresses can produce wrong pricing routes or runtime reverts later (market/read paths), with no early fail-fast.  
Suggested fix: add interface getters on `StubPool` (or use existing publics) and validate pool/token/engine wiring during `initTreasury`/`seedPools`.

3. `packages/contracts/src/ClanWorld.sol:1768`, `packages/contracts/src/ClanWorld.sol:1842`, `packages/contracts/src/ClanWorld.sol:1940`, `packages/contracts/src/ClanWorld.sol:2008`  
Proposal creation allows already-expired proposals (no `expiryTick >= currentTick` check). These proposals can never be accepted (`...:1804`, `...:1883`, `...:1970`, `...:2055`) but still consume cap slots until manual cancel, creating avoidable OTC liveness degradation.  
Suggested fix: require `expiryTick > _world.currentTick` (or `>=`, per product rule) across all propose functions.

4. `packages/contracts/src/IClanWorld.sol:850`, `packages/contracts/src/ClanWorld.sol:2142`  
Architectural drift: interface still exposes direct OTC transfer methods (`transferGold`, `transferVaultResource`, `transferBlueprint`, `transferBundle`) but implementation hard-reverts as “not implemented.” This is a misleading public surface and integration footgun in a phase explicitly shipping OTC transfer features.  
Suggested fix: either implement these methods via proposal flow or deprecate/remove them from canonical interface/ABI.

## LOW severity findings
1. `packages/contracts/src/IClanWorld.sol:668`, `packages/contracts/src/IClanWorld.sol:804`, `packages/contracts/src/IClanWorld.sol:683`, `packages/contracts/src/IClanWorld.sol:812`  
Type consistency drift: gold proposal expiry is `uint256` in interface/event, while other OTC expiry fields are `uint64` and storage normalizes to `uint64`. This inconsistency increases client/schema friction.  
Suggested fix: standardize expiry width across all OTC APIs/events/ABI.

2. `packages/contracts/src/IClanWorld.sol:341`, `packages/contracts/src/ClanWorld.sol:1822`  
Proposal structs include `accepted`/`cancelled`, but lifecycle deletes proposals instead of transitioning flags; getters cannot expose terminal status.  
Suggested fix: either persist with status transitions, or remove status flags and define “deleted == closed” formally.

3. `packages/contracts/test/GoldTransferOtc.t.sol:161`  
Open-proposal cap behavior is tested for gold only, not vault/blueprint/bundled. This leaves a cross-type regression seam unguarded.  
Suggested fix: add one cap test per OTC type (or shared helper matrix) and include expired-proposal creation guard tests.

## Cross-cutting observations
1. Phase goal is mostly met for OTC proposal-based transfers, but canonical API clarity is not yet aligned (legacy direct-transfer methods still revert).  
2. Treasury/pool boundary hardening is the biggest integration risk area: init-time validation is too permissive and currently relies on “correct operator behavior.”  
3. Test additions are strong on happy-path OTC atomicity and dead-clan behavior, but key misconfiguration and lifecycle-edge seams remain under-tested.
EXIT=0
