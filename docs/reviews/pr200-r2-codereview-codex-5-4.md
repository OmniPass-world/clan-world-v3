Reading additional input from stdin...
OpenAI Codex v0.125.0 (research preview)
--------
workdir: /home/claude/code/clan-world
model: gpt-5.4
provider: openai
approval: never
sandbox: read-only
reasoning effort: high
reasoning summaries: none
session id: 019ddc0a-55a7-7272-9909-6f85f5f69107
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
Reviewing the Phase 7 integrated surface as a full pre-merge pass. I’m starting by pulling the local contracts guidance plus the touched Solidity/test files in parallel, and I’ll split focused sub-reviews across subagents so we can trace the OTC state machine, pool/token boundary changes, and the deposit/gathering regressions without skimming.
exec
/bin/bash -lc 'git rev-parse --short HEAD' in /home/claude/code/clan-world
 succeeded in 0ms:
6cf6f39

exec
/bin/bash -lc "sed -n '1,220p' packages/contracts/AGENTS.md" in /home/claude/code/clan-world
 succeeded in 0ms:
# packages/contracts — AGENTS.md

Solidity contracts for ClanWorld. Foundry-managed. The canonical seam interface is `src/IClanWorld.sol` — every other workspace talks to chain through it (via the `IChainClient` adapter).

## What this package does

- Defines the `IClanWorld` interface (24KB, ABI-stable).
- (Wave 1+) Implements the engine: tick advancement, mission resolution, heartbeat throttle, treasury accounting.
- (Wave 2+) Includes deploy script for World Chain Sepolia (S1) and Base Sepolia (S2).
- (S2 only) Adds `ClanIdentity` (ERC-7857 iNFT wrapper) and `ClanMemory` (0G Storage KV wrapper) libraries.

## Wave 0 status

Only `src/IClanWorld.sol` exists. No engine impl, no deploy scripts, no tests. Wave 1 ships the engine + deploy.

## Key files

- `src/IClanWorld.sol` — canonical seam (DO NOT MODIFY without ADR; see `../../docs/adr/ADR001-IClanWorld-as-web2-web3-integration-seam.md`).
- `foundry.toml` — Foundry config; reads `RPC_URL_PRIMARY` from env.
- `package.json` — minimal; this is a Foundry project, not a TS package. The npm scripts shell out to `forge`.

## Local conventions

- **No TypeScript in this package.** Type generation for consumers happens via `wagmi typegen` or hand-written ABI types in `@clan-world/shared`.
- **`IClanWorld.sol` is sacred.** Changes require: ADR amendment, full team sign-off, all 6 spec docs reviewed for ripple effects.
- **Solc version pinned** in `foundry.toml` (`0.8.24`). Don't bump without a CI test run.
- **Two deploy targets:** `worldchain-sepolia` (S1) and `base-sepolia` (S2). Both use the same contract bytecode.
- **Test on a local anvil first.** Don't burn testnet faucet funds debugging silly bugs.

## How it interacts with adapters

This package IS one side of `IChainClient` — `RealChainClient` (in `@clan-world/shared/adapters`) calls into the deployed contract via viem/ethers using ABIs derived from this package.

## Running

```bash
forge build          # build contracts
forge test           # run tests (Wave 1+)
forge script script/Deploy.s.sol --rpc-url $RPC_URL_PRIMARY --broadcast
```

See `../../docs/guides/stream-contracts.md` for the full deploy workflow including wallet management.

exec
/bin/bash -lc "rg --files packages/contracts/src packages/contracts/test packages/contracts/script | sed -n '1,200p'" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/test/RNG.t.sol
packages/contracts/test/ClanWorldStub.t.sol
packages/contracts/test/MissionTiming.t.sol
packages/contracts/test/HeartbeatOrdering.t.sol
packages/contracts/test/Reentrancy.t.sol
packages/contracts/test/ClanWorld.t.sol
packages/contracts/test/DefendBase.t.sol
packages/contracts/src/ClanWorld.sol
packages/contracts/src/util/ReentrancyGuard.sol
packages/contracts/src/StubPool.sol
packages/contracts/src/IClanWorld.sol
packages/contracts/src/MinimalERC20.sol
packages/contracts/script/Heartbeat.s.sol
packages/contracts/script/Deploy.s.sol
packages/contracts/src/ClanWorldStub.sol
packages/contracts/src/lib/RNG.sol

exec
/bin/bash -lc 'git status --short' in /home/claude/code/clan-world
 succeeded in 0ms:
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
?? docs/reviews/r2-phase10/
?? docs/reviews/r2-phase6/
?? docs/reviews/r2/

codex
I’ve confirmed the contracts package guidance and the workspace isn’t checked out at `2bec876`, so I’m reviewing the exact PR state from git objects rather than assuming the working tree matches. Next I’m splitting the pass into three tracks: OTC proposal lifecycle, token/pool boundary + seeding, and mission/deposit/gathering behavior plus test seam coverage.
Parallel review plan for Phase 7 integrated contracts PR.
  → Load exact PR files from head 2bec876 and gather surrounding context for touched contracts/tests
  • Delegate focused sub-reviews for OTC lifecycle and token/pool boundary changes
  • Trace mission/deposit/gathering changes and cross-check invariants/tests locally
  • Synthesize findings into requested review format with severity ordering
2026-04-30T01:39:48.885661Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
2026-04-30T01:39:48.891889Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
2026-04-30T01:39:48.929112Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
codex
The first delegation attempt bounced because full-history forking can’t be combined with an explicit agent role here. I’m retrying with scoped explorer prompts only, then I’ll do the mission-state trace locally while those focused passes run.
collab: SpawnAgent
collab: SpawnAgent
collab: SpawnAgent
exec
/bin/bash -lc 'git show --stat --oneline 2bec876 -- packages/contracts' in /home/claude/code/clan-world
 succeeded in 0ms:
2bec876 fix(contracts): Phase 7 OTC cloud-review fixes — settle-before-debit + zero-amount + storage cleanup + uint64 atTick (#290)
 packages/contracts/abi/IClanWorld.json             |   6 +-
 packages/contracts/src/ClanWorld.sol               | 196 +++++++++++++--------
 packages/contracts/src/IClanWorld.sol              |   7 +-
 packages/contracts/test/BlueprintTransferOtc.t.sol |  20 ++-
 packages/contracts/test/BundledTransferOtc.t.sol   |  15 +-
 packages/contracts/test/ClanWorld.t.sol            |   2 +-
 packages/contracts/test/DeadClanOtc.t.sol          |   8 +-
 packages/contracts/test/GoldTransferOtc.t.sol      |  50 +++++-
 packages/contracts/test/VaultTransferOtc.t.sol     | 101 ++++++++++-
 9 files changed, 303 insertions(+), 102 deletions(-)

exec
/bin/bash -lc "git show 2bec876:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1660,2140p'" in /home/claude/code/clan-world
 succeeded in 2ms:
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

exec
/bin/bash -lc "git show 2bec876:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '430,760p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   430	        }
   431	
   432	        bool starving = !hadEnoughWheat || !hadEnoughFish;
   433	        if (starving && clan.starvationStartsAtTick == 0) {
   434	            clan.starvationStartsAtTick = tick;
   435	            emit ClanStarvationChanged(clan.clanId, true, tick);
   436	        } else if (!starving && clan.starvationStartsAtTick != 0) {
   437	            clan.starvationStartsAtTick = 0;
   438	            emit ClanStarvationChanged(clan.clanId, false, tick);
   439	        }
   440	    }
   441	
   442	    /// @dev Check if a clan is currently starving (lazy read).
   443	    function _isStarving(Clan storage clan) internal view returns (bool) {
   444	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
   445	    }
   446	
   447	    /// @dev Resolve an action for a clansman that is in ACTING state.
   448	    function _resolveAction(
   449	        Clan storage clan,
   450	        Clansman storage cs,
   451	        Mission storage m,
   452	        uint32 clanId,
   453	        uint64 tick,
   454	        bytes32 tickSeed
   455	    ) internal {
   456	        bool starving = _isStarving(clan);
   457	        ActionType action = m.action;
   458	
   459	        if (action == ActionType.ChopWood) {
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
/bin/bash -lc "git show 2bec876:packages/contracts/src/ClanWorld.sol | rg -n \"function (settleClan|settleClansman|heartbeat|submitClanOrders|getActionDuration|mintClan)|_settleClan\\(|_settleClansman\\(\"" in /home/claude/code/clan-world
 succeeded in 0ms:
368:    function _settleClan(uint32 clanId) internal {
892:    function heartbeat() external override nonReentrant {
994:    function settleClan(uint32 clanId) external override nonReentrant {
995:        _settleClan(clanId);
1002:    function settleClansman(uint32 csId) external override nonReentrant {
1005:        _settleClan(cs.clanId);
1018:    function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
1095:    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
1126:        _settleClan(clanId);
1809:        _settleClan(fromClanId);
1810:        _settleClan(toClanId);
1891:        _settleClan(fromClanId);
1892:        _settleClan(toClanId);
1975:        _settleClan(fromClanId);
1976:        _settleClan(toClanId);
2065:        _settleClan(fromClanId);
2066:        _settleClan(toClanId);
2223:    function getActionDuration(ActionType action) public pure override returns (uint64) {

exec
/bin/bash -lc "git show 2bec876:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '230,430p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   230	        if (src == dst) {
   231	            return bytes8(uint64(src) << 56);
   232	        }
   233	        // Adjacency list (1-indexed, 0 = end sentinel)
   234	        // adj[r] = neighbors of region r (up to 3)
   235	        uint8[3][9] memory adj = [
   236	            [0, 0, 0], // 0: unused
   237	            [2, 4, 0], // 1: Forest
   238	            [1, 3, 0], // 2: Mountains
   239	            [2, 4, 5], // 3: UnicornTown
   240	            [1, 3, 6], // 4: WestFarms
   241	            [3, 7, 0], // 5: EastFarms
   242	            [4, 8, 0], // 6: WestDocks
   243	            [5, 8, 0], // 7: EastDocks
   244	            [6, 7, 0] // 8: DeepSea
   245	        ];
   246	
   247	        // BFS with parent tracking (max 8 nodes)
   248	        uint8[9] memory parent;
   249	        bool[9] memory visited;
   250	        uint8[9] memory queue;
   251	        uint256 head;
   252	        uint256 tail;
   253	
   254	        for (uint256 i = 0; i < 9; i++) {
   255	            parent[i] = 0;
   256	            visited[i] = false;
   257	        }
   258	
   259	        visited[src] = true;
   260	        queue[tail++] = src;
   261	
   262	        while (head < tail) {
   263	            uint8 curr = queue[head++];
   264	            if (curr == dst) break;
   265	            for (uint256 ni = 0; ni < 3; ni++) {
   266	                uint8 nb = adj[curr][ni];
   267	                if (nb == 0) break;
   268	                if (!visited[nb]) {
   269	                    visited[nb] = true;
   270	                    parent[nb] = curr;
   271	                    queue[tail++] = nb;
   272	                }
   273	            }
   274	        }
   275	
   276	        // Reconstruct path backwards
   277	        uint8[8] memory path;
   278	        uint256 pathLen;
   279	        uint8 cur = dst;
   280	        while (cur != src) {
   281	            path[pathLen++] = cur;
   282	            cur = parent[cur];
   283	        }
   284	        path[pathLen++] = src;
   285	
   286	        // Reverse into result
   287	        bytes8 packed;
   288	        uint64 byteShift = 56;
   289	        for (uint256 i = pathLen; i > 0; i--) {
   290	            packed = packed | bytes8(uint64(path[i - 1]) << byteShift);
   291	            if (byteShift >= 8) byteShift -= 8;
   292	        }
   293	        return packed;
   294	    }
   295	
   296	    function _travelTicks(uint8 fromRegion, uint8 toRegion) private pure returns (uint8) {
   297	        if (fromRegion == 0 || toRegion == 0) return 0; // NOOP region
   298	        if (fromRegion == toRegion) return 0;
   299	        if (fromRegion > 8 || toRegion > 8) return 0;
   300	        return _distMatrix(fromRegion, toRegion);
   301	    }
   302	
   303	    function _addTicksClamped(uint64 tick, uint64 delta) private pure returns (uint64) {
   304	        if (type(uint64).max - tick < delta) return type(uint64).max;
   305	        return tick + delta;
   306	    }
   307	
   308	    // =========================================================================
   309	    // INTERNAL SETTLEMENT
   310	    // =========================================================================
   311	
   312	    /// @dev Settle a single clansman's mission for the tick range [fromTick, toTick).
   313	    ///      Handles all 6 mission lifecycle paths. Called from _settleClan and settleClansman.
   314	    function _settleMissionForClansman(
   315	        Clan storage clan,
   316	        Clansman storage cs,
   317	        uint32 clanId,
   318	        uint64 fromTick,
   319	        uint64 toTick
   320	    ) internal {
   321	        Mission storage m = _missions[cs.clansmanId];
   322	
   323	        // Path 6: dead clansman — invalidate active mission if any
   324	        if (cs.state == ClansmanState.DEAD) {
   325	            if (m.active) {
   326	                if (m.action == ActionType.DefendBase) {
   327	                    _clearDefender(cs.clansmanId);
   328	                }
   329	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
   330	            }
   331	            return;
   332	        }
   333	
   334	        if (!m.active) return; // no active mission — nothing to settle
   335	
   336	        bytes32 tickSeed;
   337	        for (uint64 tick = fromTick; tick < toTick; tick++) {
   338	            tickSeed = _tickSeeds[tick];
   339	
   340	            // Path 1 → Path 2 transition: TRAVELING → ACTING at arrivalTick
   341	            if (cs.state == ClansmanState.TRAVELING && tick >= m.arrivalTick) {
   342	                cs.state = ClansmanState.ACTING;
   343	                cs.currentRegion = m.targetRegion;
   344	                emit WorkerArrived(clanId, cs.clansmanId, m.targetRegion, tick);
   345	
   346	                if (m.action == ActionType.DefendBase) {
   347	                    _registerDefender(m.targetRegion, clanId, cs.clansmanId);
   348	                }
   349	            }
   350	
   351	            if (m.action == ActionType.DefendBase) continue; // persistent defender mission
   352	
   353	            // Path 3: ACTING at/past settlesAtTick → resolve
   354	            if (cs.state == ClansmanState.ACTING && tick >= m.settlesAtTick) {
   355	                _resolveAction(clan, cs, m, clanId, tick, tickSeed);
   356	                if (m.active && getActionDuration(m.action) > 0) {
   357	                    _completeMission(cs, m);
   358	                }
   359	            }
   360	
   361	            // If mission completed during this tick, stop iterating
   362	            if (!m.active) break;
   363	        }
   364	    }
   365	
   366	    /// @dev Lazy settlement of a clan forward to currentTick.
   367	    ///      Mutates storage. Called before order submission and by public settleClan().
   368	    function _settleClan(uint32 clanId) internal {
   369	        Clan storage clan = _clans[clanId];
   370	        if (clan.clanId == 0) return;
   371	
   372	        uint64 curTick = _world.currentTick;
   373	        uint64 fromTick = clan.lastSettledTick;
   374	        if (fromTick >= curTick) return;
   375	
   376	        // Cap ticks settled per call to prevent block gas limit issues
   377	        uint64 maxSettleTicks = 200;
   378	        if (curTick > fromTick + maxSettleTicks) {
   379	            curTick = fromTick + maxSettleTicks;
   380	        }
   381	
   382	        uint32[] storage clansmanIds = _clanClansmanIds[clanId];
   383	
   384	        // Settle tick by tick from fromTick to curTick - 1
   385	        // (curTick is still open; we settle through the last closed tick)
   386	        for (uint64 tick = fromTick; tick < curTick; tick++) {
   387	            // 1. Apply upkeep for this tick
   388	            _applyUpkeep(clan, tick);
   389	
   390	            // 2. Wheat plot regrow check (lazy, per tick)
   391	            for (uint256 pi = 0; pi < 2; pi++) {
   392	                WheatPlot storage plot = _wheatPlots[clanId][pi];
   393	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
   394	                    plot.state = WheatPlotState.Harvestable;
   395	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
   396	                    plot.regrowUntilTick = 0;
   397	                }
   398	            }
   399	
   400	            // 3. Advance each clansman (single-tick range: [tick, tick+1))
   401	            for (uint256 i = 0; i < clansmanIds.length; i++) {
   402	                Clansman storage cs = _clansmen[clansmanIds[i]];
   403	                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
   404	            }
   405	        }
   406	
   407	        clan.lastSettledTick = curTick;
   408	        emit ClanSettled(clanId, curTick);
   409	    }
   410	
   411	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
   412	    function _applyUpkeep(Clan storage clan, uint64 tick) internal {
   413	        if (clan.livingClansmen == 0) return;
   414	
   415	        uint256 wheatNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.WHEAT_UPKEEP_PER_CLANSMAN;
   416	        uint256 fishNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.FISH_UPKEEP_PER_CLANSMAN;
   417	
   418	        bool hadEnoughWheat = clan.vaultWheat >= wheatNeeded;
   419	        bool hadEnoughFish = clan.vaultFish >= fishNeeded;
   420	
   421	        if (hadEnoughWheat) {
   422	            clan.vaultWheat -= wheatNeeded;
   423	        } else {
   424	            clan.vaultWheat = 0;
   425	        }
   426	        if (hadEnoughFish) {
   427	            clan.vaultFish -= fishNeeded;
   428	        } else {
   429	            clan.vaultFish = 0;
   430	        }

exec
/bin/bash -lc "git show 2bec876:packages/contracts/src/MinimalERC20.sol | nl -ba | sed -n '1,220p'" in /home/claude/code/clan-world
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
/bin/bash -lc "git show 2bec876:packages/contracts/src/StubPool.sol | nl -ba | sed -n '1,240p'" in /home/claude/code/clan-world
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

codex
I’ve got the exact Phase 7 Solidity at `2bec876` loaded now. I’m tracing two seams in parallel from there: whether OTC settles correctly against the lazy tick engine, and whether the new ERC20/pool boundary is actually wired into the existing market path versus only into deployment/seeding.
exec
/bin/bash -lc "git show 2bec876:packages/contracts/src/ClanWorld.sol | rg -n \"sellResource|buyResource|swapExact|getPrice|getPool|getResourceToken|mint\\(|burn\\(|blueprintToken|goldToken|marketToken|MarketBuy|MarketSell\"" in /home/claude/code/clan-world
 succeeded in 0ms:
481:        } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
1252:        if (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell) {
1299:        m.marketMode = (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
1303:        m.marketToken = order.marketToken;
1322:            marketToken: order.marketToken,
1333:            order.marketToken,
1405:            if (sma.action == ActionType.MarketSell) {
1406:                try this._executeMarketSellExternal(
1407:                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.commitSequence
1414:            } else if (sma.action == ActionType.MarketBuy) {
1415:                try this._executeMarketBuyExternal(
1419:                    sma.marketToken,
1457:    /// @dev External wrapper for _executeMarketSell — enables try/catch from heartbeat loop.
1458:    function _executeMarketSellExternal(
1467:        _executeMarketSell(closedTick, clanId, clansmanId, token, amount, commitSequence);
1470:    /// @dev External wrapper for _executeMarketBuy — enables try/catch from heartbeat loop.
1471:    function _executeMarketBuyExternal(
1481:        _executeMarketBuy(closedTick, clanId, clansmanId, token, amountOut, maxGoldIn, commitSequence);
1539:    function _executeMarketSell(
1548:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
1553:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
1559:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MISSING_RESOURCES);
1563:        uint256 goldOut = StubPool(poolAddr).sellResource(amount);
1567:            closedTick, commitSequence, clanId, clansmanId, token, _treasury.goldToken, amount, goldOut
1572:    function _executeMarketBuy(
1582:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
1587:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
1598:                clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_BUY_MAX_GOLD_EXCEEDED
1603:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_NOT_ENOUGH_GOLD);
1608:        uint256 actualGoldIn = StubPool(poolAddr).buyResource(amountOut);
1613:            closedTick, commitSequence, clanId, clansmanId, _treasury.goldToken, token, actualGoldIn, amountOut
1670:        // MarketBuy/MarketSell: must target Unicorn Town
1671:        if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
1678:            address tok = order.marketToken;
1679:            if (tok == address(0) || tok == _treasury.goldToken) {
1723:        _treasury.goldToken = tokens[4];
1724:        _treasury.blueprintToken = tokens[5];
1739:        _transferSeed(_treasury.goldToken, _treasury.woodGoldPool, cfg.goldSeedForWood);
1741:        _transferSeed(_treasury.goldToken, _treasury.wheatGoldPool, cfg.goldSeedForWheat);
1743:        _transferSeed(_treasury.goldToken, _treasury.fishGoldPool, cfg.goldSeedForFish);
1745:        _transferSeed(_treasury.goldToken, _treasury.ironGoldPool, cfg.goldSeedForIron);
2174:    function getResourceToken(uint8 resourceType) external view override returns (address) {
2182:    function getPool(uint8 resourceType) external view override returns (address) {
2186:    function getPrice(uint8 resourceType, uint256 amountIn) external view override returns (uint256 amountOut) {
2237:                || action == ActionType.MarketBuy || action == ActionType.MarketSell

exec
/bin/bash -lc "git show 2bec876:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1088,1288p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1088	        _allClanIds.push(clanId);
  1089	
  1090	        emit ClanSpawned(clanId, to, iftTokenId, baseRegion, _world.currentTick);
  1091	        return (clanId, iftTokenId);
  1092	    }
  1093	
  1094	    /// @notice Submit one or more orders for a clan's clansmen. Per-order failures don't revert.
  1095	    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
  1096	        external
  1097	        override
  1098	        nonReentrant
  1099	        returns (OrderResult[] memory results)
  1100	    {
  1101	        Clan storage clan = _clans[clanId];
  1102	        require(clan.clanId != 0, "ClanWorld: clan not found");
  1103	        require(clan.owner == msg.sender, "ClanWorld: not clan owner");
  1104	        require(clan.clanState == ClanState.ACTIVE, "ClanWorld: clan dead");
  1105	
  1106	        // Guard: if clan is more than 200 ticks behind, caller must call settleClan() first
  1107	        // (_settleClan caps at 200 ticks per call; submitting into a partially-settled clan corrupts invariants)
  1108	        // ERR_MUST_SETTLE_FIRST not in StatusCode enum — using ERR_INVALID_ACTION as the closest proxy
  1109	        {
  1110	            uint64 lastSettled = _clans[clanId].lastSettledTick;
  1111	            if (_world.currentTick > lastSettled + 200) {
  1112	                results = new OrderResult[](orders.length);
  1113	                for (uint256 i = 0; i < orders.length; i++) {
  1114	                    results[i] = OrderResult({
  1115	                        clansmanId: orders[i].clansmanId,
  1116	                        status: StatusCode.ERR_INVALID_ACTION,
  1117	                        cooldownEndsAtTs: 0,
  1118	                        missionNonce: 0
  1119	                    });
  1120	                }
  1121	                return results;
  1122	            }
  1123	        }
  1124	
  1125	        // Lazy settle before processing orders
  1126	        _settleClan(clanId);
  1127	
  1128	        results = new OrderResult[](orders.length);
  1129	
  1130	        for (uint256 i = 0; i < orders.length; i++) {
  1131	            results[i] = _processOrder(clanId, clan, orders[i]);
  1132	        }
  1133	
  1134	        return results;
  1135	    }
  1136	
  1137	    struct OrderCtx {
  1138	        uint8 gotoRegion;
  1139	        uint8 fromRegion;
  1140	        bool isNoop;
  1141	        uint8 travelTicks;
  1142	        uint64 arrivalTick;
  1143	        uint64 newNonce;
  1144	        uint32 targetClanId;
  1145	        bool wasActive;
  1146	        uint64 oldNonce;
  1147	    }
  1148	
  1149	    function _processOrder(uint32 clanId, Clan storage clan, ClanOrder calldata order)
  1150	        internal
  1151	        returns (OrderResult memory result)
  1152	    {
  1153	        result.clansmanId = order.clansmanId;
  1154	
  1155	        // Validate clansman
  1156	        Clansman storage cs = _clansmen[order.clansmanId];
  1157	        if (cs.clansmanId == 0 || cs.clanId != clanId) {
  1158	            result.status = StatusCode.ERR_INVALID_CLANSMAN;
  1159	            return result;
  1160	        }
  1161	        if (cs.state == ClansmanState.DEAD) {
  1162	            result.status = StatusCode.ERR_CLANSMAN_DEAD;
  1163	            return result;
  1164	        }
  1165	
  1166	        if (order.action == ActionType.DefendBase) {
  1167	            StatusCode defendErr = _validateDefendBaseOrder(clan, order, order.gotoRegion);
  1168	            if (defendErr != StatusCode.OK) {
  1169	                result.status = defendErr;
  1170	                return result;
  1171	            }
  1172	
  1173	            uint32 defendTargetClanId = order.targetClanId == 0 ? clanId : order.targetClanId;
  1174	            Mission storage currentM = _missions[order.clansmanId];
  1175	            if (
  1176	                currentM.active && currentM.action == ActionType.DefendBase && currentM.targetRegion == order.gotoRegion
  1177	                    && currentM.targetClanId == defendTargetClanId
  1178	            ) {
  1179	                result.status = StatusCode.OK;
  1180	                result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  1181	                result.missionNonce = currentM.nonce;
  1182	                return result;
  1183	            }
  1184	        }
  1185	
  1186	        // Cooldown check
  1187	        if (block.timestamp < cs.cooldownEndsAtTs) {
  1188	            result.status = StatusCode.ERR_COOLDOWN_ACTIVE;
  1189	            result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  1190	            result.missionNonce = cs.lastMissionNonce;
  1191	            return result;
  1192	        }
  1193	
  1194	        OrderCtx memory ctx;
  1195	        ctx.fromRegion = cs.currentRegion;
  1196	        ctx.gotoRegion = order.gotoRegion;
  1197	        ctx.targetClanId =
  1198	            order.action == ActionType.DefendBase && order.targetClanId == 0 ? clanId : order.targetClanId;
  1199	
  1200	        // NOOP bypass: treat 0 as "stay here"; DefendBase requires explicit home region.
  1201	        ctx.isNoop = order.action != ActionType.DefendBase
  1202	            && (ctx.gotoRegion == ClanWorldConstants.REGION_NOOP || ctx.gotoRegion == ctx.fromRegion);
  1203	        if (ctx.isNoop) {
  1204	            ctx.gotoRegion = ctx.fromRegion;
  1205	        }
  1206	
  1207	        // Validate target region (1-8 or 0=noop)
  1208	        if (ctx.gotoRegion > 8) {
  1209	            result.status = StatusCode.ERR_INVALID_REGION;
  1210	            return result;
  1211	        }
  1212	
  1213	        // Validate action
  1214	        StatusCode actionErr = _validateAction(clan, cs, order, ctx.gotoRegion);
  1215	        if (actionErr != StatusCode.OK) {
  1216	            result.status = actionErr;
  1217	            return result;
  1218	        }
  1219	
  1220	        // Capture existing mission state
  1221	        Mission storage existingM = _missions[order.clansmanId];
  1222	        ctx.wasActive = existingM.active;
  1223	        ctx.oldNonce = existingM.nonce;
  1224	
  1225	        // Compute travel from the clansman's current region to the order target.
  1226	        ctx.travelTicks = _travelTicks(ctx.fromRegion, ctx.gotoRegion);
  1227	        ctx.arrivalTick = _addTicksClamped(_world.currentTick, uint64(ctx.travelTicks));
  1228	
  1229	        // New nonce
  1230	        ctx.newNonce = cs.lastMissionNonce + 1;
  1231	        cs.lastMissionNonce = ctx.newNonce;
  1232	
  1233	        // Install mission via helper to keep stack shallow
  1234	        _installMission(existingM, order, cs, ctx);
  1235	
  1236	        // Update clansman state
  1237	        if (ctx.travelTicks > 0) {
  1238	            cs.state = ClansmanState.TRAVELING;
  1239	        } else {
  1240	            // NOOP / same-region: no traveling state per v4.3 §A
  1241	            cs.state = ClansmanState.ACTING;
  1242	            cs.currentRegion = ctx.gotoRegion;
  1243	        }
  1244	
  1245	        // Start cooldown
  1246	        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
  1247	
  1248	        _clearDefender(cs.clansmanId);
  1249	
  1250	        // v4.2 §8 L758: "executes at heartbeat closing tick T" where T = arrivalTick.
  1251	        // executeAtTick = arrivalTick (not arrivalTick+1).
  1252	        if (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell) {
  1253	            _enqueueScheduledMarketAction(clanId, order, cs.clansmanId, ctx.arrivalTick, ctx.newNonce);
  1254	        }
  1255	
  1256	        if (order.action == ActionType.DefendBase && ctx.travelTicks == 0) {
  1257	            _registerDefender(ctx.gotoRegion, clanId, cs.clansmanId);
  1258	        }
  1259	
  1260	        if (ctx.wasActive) {
  1261	            emit MissionInterrupted(clanId, order.clansmanId, ctx.oldNonce, ctx.newNonce);
  1262	        }
  1263	
  1264	        emit MissionAssigned(
  1265	            clanId,
  1266	            order.clansmanId,
  1267	            ctx.newNonce,
  1268	            order.action,
  1269	            ctx.fromRegion,
  1270	            ctx.gotoRegion,
  1271	            _world.currentTick,
  1272	            ctx.arrivalTick
  1273	        );
  1274	
  1275	        result.status = StatusCode.OK;
  1276	        result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  1277	        result.missionNonce = ctx.newNonce;
  1278	        return result;
  1279	    }
  1280	
  1281	    function _installMission(Mission storage m, ClanOrder calldata order, Clansman storage cs, OrderCtx memory ctx)
  1282	        internal
  1283	    {
  1284	        m.active = true;
  1285	        m.nonce = ctx.newNonce;
  1286	        m.clansmanId = cs.clansmanId;
  1287	        m.submittedAtTick = _world.currentTick;
  1288	        m.executesAtTick = ctx.arrivalTick;

exec
/bin/bash -lc "git show 2bec876:packages/contracts/test/GoldTransferOtc.t.sol | nl -ba | sed -n '1,260p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	import {Test} from "forge-std/Test.sol";
     5	import {ClanWorld} from "../src/ClanWorld.sol";
     6	import {ClanWorldConstants, ClanState, OtcProposal} from "../src/IClanWorld.sol";
     7	
     8	contract GoldTransferOtcTest is Test {
     9	    event GoldTransferProposed(
    10	        uint256 indexed proposalId,
    11	        uint32 indexed fromClanId,
    12	        uint32 indexed toClanId,
    13	        uint256 amount,
    14	        uint256 expiryTick
    15	    );
    16	    event GoldTransferAccepted(
    17	        uint256 indexed proposalId,
    18	        uint32 indexed fromClanId,
    19	        uint32 indexed toClanId,
    20	        uint256 amount,
    21	        uint64 settledAtTick
    22	    );
    23	    event GoldTransferCancelled(uint256 indexed proposalId);
    24	
    25	    ClanWorld world;
    26	    address elderA = address(0xA1);
    27	    address elderB = address(0xA2);
    28	    address elderC = address(0xA3);
    29	
    30	    function setUp() public {
    31	        world = new ClanWorld();
    32	    }
    33	
    34	    function test_proposeGoldTransfer_storesProposalAndEmits() public {
    35	        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
    36	        uint256 amount = 1e18;
    37	        uint256 expiryTick = 10;
    38	
    39	        vm.expectEmit(true, true, true, true, address(world));
    40	        emit GoldTransferProposed(1, clanA, clanB, amount, expiryTick);
    41	        vm.prank(elderA);
    42	        uint256 proposalId = world.proposeGoldTransfer(clanA, clanB, amount, expiryTick);
    43	
    44	        assertEq(proposalId, 1, "first proposal id");
    45	        OtcProposal memory proposal = world.getOtcGoldProposal(proposalId);
    46	        assertEq(proposal.from, clanA, "proposal from");
    47	        assertEq(proposal.to, clanB, "proposal to");
    48	        assertEq(proposal.amount, amount, "proposal amount");
    49	        assertEq(proposal.expiryTick, uint64(expiryTick), "proposal expiry");
    50	        assertFalse(proposal.accepted, "proposal not accepted");
    51	        assertFalse(proposal.cancelled, "proposal not cancelled");
    52	    }
    53	
    54	    function test_acceptGoldTransfer_transfersAtomicallyAndEmits() public {
    55	        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
    56	        uint256 amount = 1e18;
    57	        uint256 fromBefore = world.getClan(clanA).goldBalance;
    58	        uint256 toBefore = world.getClan(clanB).goldBalance;
    59	
    60	        uint256 proposalId = _propose(clanA, clanB, amount, 10);
    61	
    62	        vm.expectEmit(true, true, true, true, address(world));
    63	        emit GoldTransferAccepted(proposalId, clanA, clanB, amount, world.getWorldState().currentTick);
    64	        vm.prank(elderB);
    65	        world.acceptGoldTransfer(proposalId);
    66	
    67	        assertEq(world.getClan(clanA).goldBalance, fromBefore - amount, "from debited");
    68	        assertEq(world.getClan(clanB).goldBalance, toBefore + amount, "to credited");
    69	        assertEq(world.getOtcGoldProposal(proposalId).from, 0, "proposal deleted");
    70	    }
    71	
    72	    function test_acceptGoldTransfer_revertsWhenBalanceChangedAfterProposal() public {
    73	        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
    74	        uint256 proposalOne = _propose(clanA, clanB, 3e18, 10);
    75	        uint256 proposalTwo = _propose(clanA, clanB, 3e18, 10);
    76	
    77	        vm.prank(elderB);
    78	        world.acceptGoldTransfer(proposalOne);
    79	
    80	        vm.expectRevert("ERR_NOT_ENOUGH_GOLD");
    81	        vm.prank(elderB);
    82	        world.acceptGoldTransfer(proposalTwo);
    83	    }
    84	
    85	    function test_acceptGoldTransfer_revertsWhenExpired() public {
    86	        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
    87	        uint256 proposalId = _propose(clanA, clanB, 1e18, world.getWorldState().currentTick);
    88	        _advanceTick();
    89	
    90	        vm.expectRevert("ClanWorld: proposal expired");
    91	        vm.prank(elderB);
    92	        world.acceptGoldTransfer(proposalId);
    93	    }
    94	
    95	    function test_goldTransfer_wrongCallersRevert() public {
    96	        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
    97	
    98	        vm.expectRevert("ClanWorld: not clan owner");
    99	        vm.prank(elderB);
   100	        world.proposeGoldTransfer(clanA, clanB, 1e18, 10);
   101	
   102	        uint256 proposalId = _propose(clanA, clanB, 1e18, 10);
   103	        vm.expectRevert("ClanWorld: not clan owner");
   104	        vm.prank(elderA);
   105	        world.acceptGoldTransfer(proposalId);
   106	    }
   107	
   108	    function test_cancelGoldTransfer_byProposerBlocksAccept() public {
   109	        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
   110	        uint256 proposalId = _propose(clanA, clanB, 1e18, 10);
   111	
   112	        vm.expectEmit(true, false, false, true, address(world));
   113	        emit GoldTransferCancelled(proposalId);
   114	        vm.prank(elderA);
   115	        world.cancelGoldTransfer(proposalId);
   116	
   117	        assertEq(world.getOtcGoldProposal(proposalId).from, 0, "proposal deleted");
   118	        vm.expectRevert("ClanWorld: proposal not found");
   119	        vm.prank(elderB);
   120	        world.acceptGoldTransfer(proposalId);
   121	    }
   122	
   123	    function test_cancelGoldTransfer_acceptorCannotCancel() public {
   124	        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
   125	        uint256 proposalId = _propose(clanA, clanB, 1e18, 10);
   126	
   127	        vm.expectRevert("ClanWorld: not clan owner");
   128	        vm.prank(elderB);
   129	        world.cancelGoldTransfer(proposalId);
   130	    }
   131	
   132	    function test_goldTransfer_twoClanNoInterference() public {
   133	        (uint32 clanA, uint32 clanB, uint32 clanC) = _mintThreeClans();
   134	        uint256 clanBBefore = world.getClan(clanB).goldBalance;
   135	        uint256 clanCBefore = world.getClan(clanC).goldBalance;
   136	
   137	        uint256 proposalId = _propose(clanA, clanC, 1e18, 10);
   138	        vm.prank(elderC);
   139	        world.acceptGoldTransfer(proposalId);
   140	
   141	        assertEq(world.getClan(clanB).goldBalance, clanBBefore, "unrelated clan unchanged");
   142	        assertEq(world.getClan(clanC).goldBalance, clanCBefore + 1e18, "target clan credited");
   143	    }
   144	
   145	    function test_proposeGoldTransfer_revertsWhenZeroAmount() public {
   146	        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
   147	
   148	        vm.expectRevert("ERR_ZERO_AMOUNT");
   149	        _propose(clanA, clanB, 0, 10);
   150	    }
   151	
   152	    function test_proposeGoldTransfer_revertsWhenSelfTransfer() public {
   153	        (uint32 clanA,,) = _mintThreeClans();
   154	
   155	        vm.expectRevert("ERR_SELF_TRANSFER");
   156	        _propose(clanA, clanA, 1e18, 10);
   157	    }
   158	
   159	    function test_goldTransfer_openProposalCapDecrementsAfterAccept() public {
   160	        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
   161	
   162	        uint256 firstProposalId;
   163	        uint256 secondProposalId;
   164	        for (uint256 i = 0; i < world.MAX_OPEN_OTC_PROPOSALS_PER_CLAN(); i++) {
   165	            uint256 proposalId = _propose(clanA, clanB, 1, 10);
   166	            if (i == 0) firstProposalId = proposalId;
   167	            if (i == 1) secondProposalId = proposalId;
   168	        }
   169	
   170	        vm.expectRevert("ERR_OTC_CAP");
   171	        _propose(clanA, clanB, 1, 10);
   172	
   173	        vm.prank(elderB);
   174	        world.acceptGoldTransfer(firstProposalId);
   175	
   176	        uint256 newProposalId = _propose(clanA, clanB, 1, 10);
   177	        assertGt(newProposalId, firstProposalId, "cap slot reopened after accept");
   178	
   179	        vm.expectRevert("ERR_OTC_CAP");
   180	        _propose(clanA, clanB, 1, 10);
   181	
   182	        vm.prank(elderA);
   183	        world.cancelGoldTransfer(secondProposalId);
   184	
   185	        uint256 postCancelProposalId = _propose(clanA, clanB, 1, 10);
   186	        assertGt(postCancelProposalId, newProposalId, "cap slot reopened after cancel");
   187	    }
   188	
   189	    function _mintThreeClans() internal returns (uint32 clanA, uint32 clanB, uint32 clanC) {
   190	        vm.prank(elderA);
   191	        (clanA,) = world.mintClan(elderA);
   192	        vm.prank(elderB);
   193	        (clanB,) = world.mintClan(elderB);
   194	        vm.prank(elderC);
   195	        (clanC,) = world.mintClan(elderC);
   196	
   197	        assertEq(uint8(world.getClan(clanA).clanState), uint8(ClanState.ACTIVE), "clan A alive");
   198	        assertEq(uint8(world.getClan(clanB).clanState), uint8(ClanState.ACTIVE), "clan B alive");
   199	        assertEq(uint8(world.getClan(clanC).clanState), uint8(ClanState.ACTIVE), "clan C alive");
   200	    }
   201	
   202	    function _propose(uint32 fromClanId, uint32 toClanId, uint256 amount, uint256 expiryTick)
   203	        internal
   204	        returns (uint256 proposalId)
   205	    {
   206	        vm.prank(elderA);
   207	        proposalId = world.proposeGoldTransfer(fromClanId, toClanId, amount, expiryTick);
   208	    }
   209	
   210	    function _advanceTick() internal {
   211	        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
   212	        world.heartbeat();
   213	    }
   214	}

exec
/bin/bash -lc "git show 2bec876:packages/contracts/test/VaultTransferOtc.t.sol | nl -ba | sed -n '1,320p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	import {Test} from "forge-std/Test.sol";
     5	import {ClanWorld} from "../src/ClanWorld.sol";
     6	import {
     7	    ActionType,
     8	    ClanWorldConstants,
     9	    Clan,
    10	    ClanFullView,
    11	    ClanOrder,
    12	    ClanState,
    13	    OrderResult,
    14	    StatusCode,
    15	    VaultTransferProposal
    16	} from "../src/IClanWorld.sol";
    17	
    18	contract VaultTransferHarness is ClanWorld {
    19	    function setVault(uint32 clanId, uint256 wood, uint256 wheat, uint256 fish, uint256 iron) external {
    20	        _clans[clanId].vaultWood = wood;
    21	        _clans[clanId].vaultWheat = wheat;
    22	        _clans[clanId].vaultFish = fish;
    23	        _clans[clanId].vaultIron = iron;
    24	    }
    25	
    26	    function setCarry(uint32 clansmanId, uint256 wood, uint256 wheat, uint256 fish, uint256 iron) external {
    27	        _clansmen[clansmanId].carryWood = wood;
    28	        _clansmen[clansmanId].carryWheat = wheat;
    29	        _clansmen[clansmanId].carryFish = fish;
    30	        _clansmen[clansmanId].carryIron = iron;
    31	    }
    32	}
    33	
    34	contract VaultTransferOtcTest is Test {
    35	    event VaultTransferProposed(
    36	        uint256 indexed proposalId,
    37	        uint32 indexed fromClanId,
    38	        uint32 indexed toClanId,
    39	        uint256 wood,
    40	        uint256 wheat,
    41	        uint256 fish,
    42	        uint256 iron,
    43	        uint64 expiryTick
    44	    );
    45	    event VaultTransferAccepted(
    46	        uint256 indexed proposalId,
    47	        uint32 indexed fromClanId,
    48	        uint32 indexed toClanId,
    49	        uint256 wood,
    50	        uint256 wheat,
    51	        uint256 fish,
    52	        uint256 iron,
    53	        uint64 settledAtTick
    54	    );
    55	    event VaultTransferCancelled(uint256 indexed proposalId);
    56	
    57	    VaultTransferHarness world;
    58	    address elderA = address(0xA1);
    59	    address elderB = address(0xA2);
    60	    address elderC = address(0xA3);
    61	
    62	    function setUp() public {
    63	        world = new VaultTransferHarness();
    64	    }
    65	
    66	    function test_proposeAndAcceptVaultTransfer_transfersAllResourcesAtomically() public {
    67	        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
    68	        world.setVault(clanA, 100e18, 80e18, 30e18, 20e18);
    69	        world.setVault(clanB, 10e18, 11e18, 12e18, 13e18);
    70	
    71	        vm.expectEmit(true, true, true, true, address(world));
    72	        emit VaultTransferProposed(1, clanA, clanB, 15e18, 16e18, 2e18, 3e18, 10);
    73	        uint256 proposalId = _propose(clanA, clanB, 15e18, 16e18, 2e18, 3e18, 10);
    74	
    75	        VaultTransferProposal memory proposal = world.getOtcVaultTransferProposal(proposalId);
    76	        assertEq(proposal.from, clanA, "proposal from");
    77	        assertEq(proposal.to, clanB, "proposal to");
    78	        assertEq(proposal.wood, 15e18, "proposal wood");
    79	        assertEq(proposal.wheat, 16e18, "proposal wheat");
    80	        assertEq(proposal.fish, 2e18, "proposal fish");
    81	        assertEq(proposal.iron, 3e18, "proposal iron");
    82	        assertFalse(proposal.accepted, "proposal not accepted");
    83	
    84	        vm.expectEmit(true, true, true, true, address(world));
    85	        emit VaultTransferAccepted(
    86	            proposalId, clanA, clanB, 15e18, 16e18, 2e18, 3e18, world.getWorldState().currentTick
    87	        );
    88	        vm.prank(elderB);
    89	        world.acceptVaultTransfer(proposalId);
    90	
    91	        Clan memory fromAfter = world.getClan(clanA);
    92	        Clan memory toAfter = world.getClan(clanB);
    93	        assertEq(fromAfter.vaultWood, 85e18, "from wood debited");
    94	        assertEq(fromAfter.vaultWheat, 64e18, "from wheat debited");
    95	        assertEq(fromAfter.vaultFish, 28e18, "from fish debited");
    96	        assertEq(fromAfter.vaultIron, 17e18, "from iron debited");
    97	        assertEq(toAfter.vaultWood, 25e18, "to wood credited");
    98	        assertEq(toAfter.vaultWheat, 27e18, "to wheat credited");
    99	        assertEq(toAfter.vaultFish, 14e18, "to fish credited");
   100	        assertEq(toAfter.vaultIron, 16e18, "to iron credited");
   101	        assertEq(world.getOtcVaultTransferProposal(proposalId).from, 0, "proposal deleted");
   102	    }
   103	
   104	    function test_acceptVaultTransfer_revertsAndLeavesAllResourcesWhenOneResourceInsufficient() public {
   105	        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
   106	        world.setVault(clanA, 100e18, 100e18, 100e18, 5e18);
   107	        world.setVault(clanB, 10e18, 10e18, 10e18, 10e18);
   108	        uint256 proposalId = _propose(clanA, clanB, 20e18, 20e18, 20e18, 5e18, 10);
   109	        world.setVault(clanA, 100e18, 100e18, 100e18, 4e18);
   110	
   111	        vm.expectRevert("ERR_NOT_ENOUGH_RESOURCES");
   112	        vm.prank(elderB);
   113	        world.acceptVaultTransfer(proposalId);
   114	
   115	        Clan memory fromAfter = world.getClan(clanA);
   116	        Clan memory toAfter = world.getClan(clanB);
   117	        assertEq(fromAfter.vaultWood, 100e18, "from wood unchanged");
   118	        assertEq(fromAfter.vaultWheat, 100e18, "from wheat unchanged");
   119	        assertEq(fromAfter.vaultFish, 100e18, "from fish unchanged");
   120	        assertEq(fromAfter.vaultIron, 4e18, "from iron unchanged");
   121	        assertEq(toAfter.vaultWood, 10e18, "to wood unchanged");
   122	        assertEq(toAfter.vaultWheat, 10e18, "to wheat unchanged");
   123	        assertEq(toAfter.vaultFish, 10e18, "to fish unchanged");
   124	        assertEq(toAfter.vaultIron, 10e18, "to iron unchanged");
   125	    }
   126	
   127	    function test_acceptVaultTransfer_revertsWhenExpired() public {
   128	        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
   129	        world.setVault(clanA, 100e18, 100e18, 100e18, 100e18);
   130	        uint256 proposalId = _propose(clanA, clanB, 1e18, 1e18, 1e18, 1e18, world.getWorldState().currentTick);
   131	        _advanceTick();
   132	
   133	        vm.expectRevert("ClanWorld: proposal expired");
   134	        vm.prank(elderB);
   135	        world.acceptVaultTransfer(proposalId);
   136	    }
   137	
   138	    function test_cancelVaultTransfer_byProposerBlocksAccept() public {
   139	        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
   140	        world.setVault(clanA, 100e18, 100e18, 100e18, 100e18);
   141	        uint256 proposalId = _propose(clanA, clanB, 1e18, 1e18, 1e18, 1e18, 10);
   142	
   143	        vm.expectEmit(true, false, false, true, address(world));
   144	        emit VaultTransferCancelled(proposalId);
   145	        vm.prank(elderA);
   146	        world.cancelVaultTransfer(proposalId);
   147	
   148	        assertEq(world.getOtcVaultTransferProposal(proposalId).from, 0, "proposal deleted");
   149	        vm.expectRevert("ClanWorld: proposal not found");
   150	        vm.prank(elderB);
   151	        world.acceptVaultTransfer(proposalId);
   152	    }
   153	
   154	    function test_acceptVaultTransfer_settlesPendingUpkeepBeforeDebit() public {
   155	        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
   156	        world.setVault(clanA, 0, 1e18, 0, 0);
   157	        world.setVault(clanB, 0, 0, 0, 0);
   158	        uint256 proposalId = _propose(clanA, clanB, 0, 1e18, 0, 0, 10);
   159	
   160	        _advanceTick();
   161	
   162	        vm.expectRevert("ERR_NOT_ENOUGH_RESOURCES");
   163	        vm.prank(elderB);
   164	        world.acceptVaultTransfer(proposalId);
   165	
   166	        assertEq(world.getClan(clanB).vaultWheat, 0, "target not credited on failed accept");
   167	    }
   168	
   169	    function test_proposeVaultTransfer_revertsWhenAllZero() public {
   170	        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
   171	
   172	        vm.expectRevert("ERR_ZERO_AMOUNT");
   173	        _propose(clanA, clanB, 0, 0, 0, 0, 10);
   174	    }
   175	
   176	    function test_proposeVaultTransfer_revertsWhenSelfTransfer() public {
   177	        (uint32 clanA,,) = _mintThreeClans();
   178	
   179	        vm.expectRevert("ERR_SELF_TRANSFER");
   180	        _propose(clanA, clanA, 1e18, 0, 0, 0, 10);
   181	    }
   182	
   183	    function test_acceptVaultTransfer_settlesPendingDepositBeforeDebit() public {
   184	        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
   185	        uint32 csId = _firstClansman(clanA);
   186	        uint8 homeRegion = world.getClan(clanA).baseRegion;
   187	
   188	        world.setVault(clanA, 0, 100e18, 100e18, 100e18);
   189	        world.setVault(clanB, 0, 0, 0, 0);
   190	        world.setCarry(csId, 5e18, 0, 0, 0);
   191	
   192	        OrderResult[] memory results = _submitDeposit(clanA, csId, homeRegion);
   193	        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "deposit accepted");
   194	
   195	        uint256 proposalId = _propose(clanA, clanB, 5e18, 0, 0, 0, 10);
   196	        _advanceTick();
   197	        _advanceTick();
   198	
   199	        vm.prank(elderB);
   200	        world.acceptVaultTransfer(proposalId);
   201	
   202	        assertEq(world.getClan(clanA).vaultWood, 0, "pending deposit settled then debited");
   203	        assertEq(world.getClan(clanB).vaultWood, 5e18, "target credited from settled deposit");
   204	    }
   205	
   206	    function test_vaultTransfer_twoClanNoInterference() public {
   207	        (uint32 clanA, uint32 clanB, uint32 clanC) = _mintThreeClans();
   208	        world.setVault(clanA, 100e18, 100e18, 100e18, 100e18);
   209	        world.setVault(clanB, 7e18, 8e18, 9e18, 10e18);
   210	        world.setVault(clanC, 1e18, 2e18, 3e18, 4e18);
   211	
   212	        uint256 proposalId = _propose(clanA, clanC, 11e18, 12e18, 13e18, 14e18, 10);
   213	        vm.prank(elderC);
   214	        world.acceptVaultTransfer(proposalId);
   215	
   216	        Clan memory clanBAfter = world.getClan(clanB);
   217	        Clan memory clanCAfter = world.getClan(clanC);
   218	        assertEq(clanBAfter.vaultWood, 7e18, "unrelated wood unchanged");
   219	        assertEq(clanBAfter.vaultWheat, 8e18, "unrelated wheat unchanged");
   220	        assertEq(clanBAfter.vaultFish, 9e18, "unrelated fish unchanged");
   221	        assertEq(clanBAfter.vaultIron, 10e18, "unrelated iron unchanged");
   222	        assertEq(clanCAfter.vaultWood, 12e18, "target wood credited");
   223	        assertEq(clanCAfter.vaultWheat, 14e18, "target wheat credited");
   224	        assertEq(clanCAfter.vaultFish, 16e18, "target fish credited");
   225	        assertEq(clanCAfter.vaultIron, 18e18, "target iron credited");
   226	    }
   227	
   228	    function _mintThreeClans() internal returns (uint32 clanA, uint32 clanB, uint32 clanC) {
   229	        vm.prank(elderA);
   230	        (clanA,) = world.mintClan(elderA);
   231	        vm.prank(elderB);
   232	        (clanB,) = world.mintClan(elderB);
   233	        vm.prank(elderC);
   234	        (clanC,) = world.mintClan(elderC);
   235	
   236	        assertEq(uint8(world.getClan(clanA).clanState), uint8(ClanState.ACTIVE), "clan A alive");
   237	        assertEq(uint8(world.getClan(clanB).clanState), uint8(ClanState.ACTIVE), "clan B alive");
   238	        assertEq(uint8(world.getClan(clanC).clanState), uint8(ClanState.ACTIVE), "clan C alive");
   239	    }
   240	
   241	    function _propose(
   242	        uint32 fromClanId,
   243	        uint32 toClanId,
   244	        uint256 wood,
   245	        uint256 wheat,
   246	        uint256 fish,
   247	        uint256 iron,
   248	        uint64 expiryTick
   249	    ) internal returns (uint256 proposalId) {
   250	        vm.prank(elderA);
   251	        proposalId = world.proposeVaultTransfer(fromClanId, toClanId, wood, wheat, fish, iron, expiryTick);
   252	    }
   253	
   254	    function _firstClansman(uint32 clanId) internal view returns (uint32) {
   255	        ClanFullView memory view_ = world.getClanFullView(clanId);
   256	        return view_.clansmen[0].clansman.clansman.clansmanId;
   257	    }
   258	
   259	    function _submitDeposit(uint32 clanId, uint32 clansmanId, uint8 homeRegion)
   260	        internal
   261	        returns (OrderResult[] memory)
   262	    {
   263	        ClanOrder[] memory orders = new ClanOrder[](1);
   264	        orders[0] = ClanOrder({
   265	            clansmanId: clansmanId,
   266	            gotoRegion: homeRegion,
   267	            action: ActionType.DepositResources,
   268	            targetClanId: 0,
   269	            marketToken: address(0),
   270	            marketAmount: 0,
   271	            maxGoldIn: 0
   272	        });
   273	
   274	        vm.prank(elderA);
   275	        return world.submitClanOrders(clanId, orders);
   276	    }
   277	
   278	    function _advanceTick() internal {
   279	        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
   280	        world.heartbeat();
   281	    }
   282	}

exec
/bin/bash -lc "git show 2bec876:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1280,1660p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1280	
  1281	    function _installMission(Mission storage m, ClanOrder calldata order, Clansman storage cs, OrderCtx memory ctx)
  1282	        internal
  1283	    {
  1284	        m.active = true;
  1285	        m.nonce = ctx.newNonce;
  1286	        m.clansmanId = cs.clansmanId;
  1287	        m.submittedAtTick = _world.currentTick;
  1288	        m.executesAtTick = ctx.arrivalTick;
  1289	        m.settlesAtTick = order.action == ActionType.DefendBase
  1290	            ? type(uint64).max
  1291	            : _addTicksClamped(ctx.arrivalTick, getActionDuration(order.action));
  1292	        m.startRegion = ctx.fromRegion;
  1293	        m.targetRegion = ctx.gotoRegion;
  1294	        m.action = order.action;
  1295	        m.startTick = _world.currentTick;
  1296	        m.arrivalTick = ctx.arrivalTick;
  1297	        m.actionStartTick = ctx.arrivalTick;
  1298	        m.missionSeed = keccak256(abi.encode(_world.currentTickSeed, cs.clansmanId, ctx.newNonce));
  1299	        m.marketMode = (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
  1300	            ? MarketExecutionMode.Scheduled
  1301	            : MarketExecutionMode.None;
  1302	        m.targetClanId = ctx.targetClanId;
  1303	        m.marketToken = order.marketToken;
  1304	        m.marketAmount = order.marketAmount;
  1305	        m.maxGoldIn = order.maxGoldIn;
  1306	    }
  1307	
  1308	    function _enqueueScheduledMarketAction(
  1309	        uint32 clanId,
  1310	        ClanOrder calldata order,
  1311	        uint32 clansmanId,
  1312	        uint64 executeAtTick,
  1313	        uint64 missionNonce
  1314	    ) internal {
  1315	        ScheduledMarketAction memory sma = ScheduledMarketAction({
  1316	            executeAtTick: executeAtTick,
  1317	            commitSequence: _world.nextCommitSequence++,
  1318	            missionNonce: missionNonce,
  1319	            clanId: clanId,
  1320	            clansmanId: clansmanId,
  1321	            action: order.action,
  1322	            marketToken: order.marketToken,
  1323	            marketAmount: order.marketAmount,
  1324	            maxGoldIn: order.maxGoldIn
  1325	        });
  1326	        _scheduledMarketActions[executeAtTick].push(sma);
  1327	        emit ScheduledMarketActionCommitted(
  1328	            executeAtTick,
  1329	            sma.commitSequence,
  1330	            clanId,
  1331	            clansmanId,
  1332	            order.action,
  1333	            order.marketToken,
  1334	            order.marketAmount,
  1335	            order.maxGoldIn
  1336	        );
  1337	    }
  1338	
  1339	    function _registerDefender(uint8 region, uint32 clanId, uint32 clansmanId) internal {
  1340	        if (_clansmanDefendingRegion[clansmanId] == region) return;
  1341	        _clearDefender(clansmanId);
  1342	
  1343	        if (_defenderCountByRegionClan[region][clanId] == 0) {
  1344	            _defendingClansByRegion[region].push(clanId);
  1345	        }
  1346	        _defenderCountByRegionClan[region][clanId]++;
  1347	        _clansmanDefendingRegion[clansmanId] = region;
  1348	    }
  1349	
  1350	    function _clearDefender(uint32 clansmanId) internal {
  1351	        uint8 region = _clansmanDefendingRegion[clansmanId];
  1352	        if (region == 0) return;
  1353	
  1354	        uint32 clanId = _clansmen[clansmanId].clanId;
  1355	        uint256 count = _defenderCountByRegionClan[region][clanId];
  1356	        if (count > 1) {
  1357	            _defenderCountByRegionClan[region][clanId] = count - 1;
  1358	        } else {
  1359	            delete _defenderCountByRegionClan[region][clanId];
  1360	            uint32[] storage clans = _defendingClansByRegion[region];
  1361	            for (uint256 i = 0; i < clans.length; i++) {
  1362	                if (clans[i] == clanId) {
  1363	                    clans[i] = clans[clans.length - 1];
  1364	                    clans.pop();
  1365	                    break;
  1366	                }
  1367	            }
  1368	        }
  1369	
  1370	        delete _clansmanDefendingRegion[clansmanId];
  1371	    }
  1372	
  1373	    // =========================================================================
  1374	    // MARKET EXECUTION (Phase 2)
  1375	    // =========================================================================
  1376	
  1377	    /// @dev Execute up to MAX_MARKET_ACTIONS_PER_TICK scheduled market actions for the given tick.
  1378	    ///      Overflow is appended to the next tick to keep heartbeat gas bounded.
  1379	    function _executeScheduledMarketActions(uint64 tick) internal {
  1380	        ScheduledMarketAction[] storage actions = _scheduledMarketActions[tick];
  1381	        uint256 len = actions.length;
  1382	        if (len == 0) return;
  1383	
  1384	        uint256 processCount = len > MAX_MARKET_ACTIONS_PER_TICK ? MAX_MARKET_ACTIONS_PER_TICK : len;
  1385	
  1386	        for (uint256 i = 0; i < processCount; i++) {
  1387	            ScheduledMarketAction storage sma = actions[i];
  1388	
  1389	            // Validate clansman still belongs to the clan
  1390	            Clansman storage cs = _clansmen[sma.clansmanId];
  1391	            if (cs.clanId != sma.clanId || cs.state == ClansmanState.DEAD) {
  1392	                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_CLANSMAN);
  1393	                continue;
  1394	            }
  1395	
  1396	            // Guard: clansman was re-tasked if mission action no longer matches the queued type.
  1397	            // Note: _completeMission sets m.active=false during settlement (by design), so we
  1398	            // cannot use m.active as a validity signal here — check action type and nonce.
  1399	            Mission storage m = _missions[sma.clansmanId];
  1400	            if (m.action != sma.action || m.nonce != sma.missionNonce) {
  1401	                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
  1402	                continue;
  1403	            }
  1404	
  1405	            if (sma.action == ActionType.MarketSell) {
  1406	                try this._executeMarketSellExternal(
  1407	                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.commitSequence
  1408	                ) {
  1409	                // success
  1410	                }
  1411	                catch {
  1412	                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
  1413	                }
  1414	            } else if (sma.action == ActionType.MarketBuy) {
  1415	                try this._executeMarketBuyExternal(
  1416	                    tick,
  1417	                    sma.clanId,
  1418	                    sma.clansmanId,
  1419	                    sma.marketToken,
  1420	                    sma.marketAmount,
  1421	                    sma.maxGoldIn,
  1422	                    sma.commitSequence
  1423	                ) {
  1424	                // success
  1425	                }
  1426	                catch {
  1427	                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
  1428	                }
  1429	            }
  1430	        }
  1431	
  1432	        if (len > processCount) {
  1433	            ScheduledMarketAction[] storage nextActions = _scheduledMarketActions[tick + 1];
  1434	            for (uint256 i = processCount; i < len; i++) {
  1435	                nextActions.push(actions[i]);
  1436	            }
  1437	            // Invariant: each tick queue executes in global commitSequence order, including
  1438	            // older overflow actions merged into a tick that already has native actions.
  1439	            _sortScheduledMarketActionsByCommitSequence(nextActions);
  1440	        }
  1441	
  1442	        delete _scheduledMarketActions[tick];
  1443	    }
  1444	
  1445	    function _sortScheduledMarketActionsByCommitSequence(ScheduledMarketAction[] storage actions) internal {
  1446	        for (uint256 i = 1; i < actions.length; i++) {
  1447	            ScheduledMarketAction memory key = actions[i];
  1448	            uint256 j = i;
  1449	            while (j > 0 && actions[j - 1].commitSequence > key.commitSequence) {
  1450	                actions[j] = actions[j - 1];
  1451	                j--;
  1452	            }
  1453	            actions[j] = key;
  1454	        }
  1455	    }
  1456	
  1457	    /// @dev External wrapper for _executeMarketSell — enables try/catch from heartbeat loop.
  1458	    function _executeMarketSellExternal(
  1459	        uint64 closedTick,
  1460	        uint32 clanId,
  1461	        uint32 clansmanId,
  1462	        address token,
  1463	        uint256 amount,
  1464	        uint64 commitSequence
  1465	    ) external {
  1466	        require(msg.sender == address(this), "ClanWorld: internal only");
  1467	        _executeMarketSell(closedTick, clanId, clansmanId, token, amount, commitSequence);
  1468	    }
  1469	
  1470	    /// @dev External wrapper for _executeMarketBuy — enables try/catch from heartbeat loop.
  1471	    function _executeMarketBuyExternal(
  1472	        uint64 closedTick,
  1473	        uint32 clanId,
  1474	        uint32 clansmanId,
  1475	        address token,
  1476	        uint256 amountOut,
  1477	        uint256 maxGoldIn,
  1478	        uint64 commitSequence
  1479	    ) external {
  1480	        require(msg.sender == address(this), "ClanWorld: internal only");
  1481	        _executeMarketBuy(closedTick, clanId, clansmanId, token, amountOut, maxGoldIn, commitSequence);
  1482	    }
  1483	
  1484	    /// @dev Map a resource token address to its pool address.
  1485	    function _poolFor(address token) internal view returns (address pool) {
  1486	        if (token == _treasury.woodToken) return _treasury.woodGoldPool;
  1487	        if (token == _treasury.ironToken) return _treasury.ironGoldPool;
  1488	        if (token == _treasury.wheatToken) return _treasury.wheatGoldPool;
  1489	        if (token == _treasury.fishToken) return _treasury.fishGoldPool;
  1490	        return address(0);
  1491	    }
  1492	
  1493	    /// @dev Add an amount of a resource token to the clan vault.
  1494	    function _addToVault(Clan storage clan, address token, uint256 amount) internal {
  1495	        if (token == _treasury.woodToken) {
  1496	            clan.vaultWood += amount;
  1497	            return;
  1498	        }
  1499	        if (token == _treasury.ironToken) {
  1500	            clan.vaultIron += amount;
  1501	            return;
  1502	        }
  1503	        if (token == _treasury.wheatToken) {
  1504	            clan.vaultWheat += amount;
  1505	            return;
  1506	        }
  1507	        if (token == _treasury.fishToken) {
  1508	            clan.vaultFish += amount;
  1509	            return;
  1510	        }
  1511	    }
  1512	
  1513	    /// @dev Deduct an amount of a resource token from the clan vault. Returns false if insufficient.
  1514	    function _deductFromVault(Clan storage clan, address token, uint256 amount) internal returns (bool) {
  1515	        if (token == _treasury.woodToken) {
  1516	            if (clan.vaultWood < amount) return false;
  1517	            clan.vaultWood -= amount;
  1518	            return true;
  1519	        }
  1520	        if (token == _treasury.ironToken) {
  1521	            if (clan.vaultIron < amount) return false;
  1522	            clan.vaultIron -= amount;
  1523	            return true;
  1524	        }
  1525	        if (token == _treasury.wheatToken) {
  1526	            if (clan.vaultWheat < amount) return false;
  1527	            clan.vaultWheat -= amount;
  1528	            return true;
  1529	        }
  1530	        if (token == _treasury.fishToken) {
  1531	            if (clan.vaultFish < amount) return false;
  1532	            clan.vaultFish -= amount;
  1533	            return true;
  1534	        }
  1535	        return false;
  1536	    }
  1537	
  1538	    /// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.
  1539	    function _executeMarketSell(
  1540	        uint64 closedTick,
  1541	        uint32 clanId,
  1542	        uint32 clansmanId,
  1543	        address token,
  1544	        uint256 amount,
  1545	        uint64 commitSequence
  1546	    ) internal {
  1547	        if (!_treasury.poolsSeeded) {
  1548	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
  1549	            return;
  1550	        }
  1551	        address poolAddr = _poolFor(token);
  1552	        if (poolAddr == address(0)) {
  1553	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
  1554	            return;
  1555	        }
  1556	
  1557	        Clan storage clan = _clans[clanId];
  1558	        if (!_deductFromVault(clan, token, amount)) {
  1559	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MISSING_RESOURCES);
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

exec
/bin/bash -lc "git show 2bec876:packages/contracts/test/Gathering.t.sol | nl -ba | sed -n '1,220p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	import {Test} from "forge-std/Test.sol";
     5	import {ClanWorld} from "../src/ClanWorld.sol";
     6	import {
     7	    ClanWorldConstants,
     8	    ActionType,
     9	    ClansmanState,
    10	    StatusCode,
    11	    Clansman,
    12	    Mission,
    13	    ClanFullView,
    14	    ClanOrder,
    15	    OrderResult
    16	} from "../src/IClanWorld.sol";
    17	
    18	contract GatheringHarness is ClanWorld {
    19	    function setCarryWood(uint32 csId, uint256 wood) external {
    20	        _clansmen[csId].carryWood = wood;
    21	    }
    22	}
    23	
    24	contract GatheringTest is Test {
    25	    GatheringHarness world;
    26	    address elder = address(0xA1);
    27	
    28	    function setUp() public {
    29	        world = new GatheringHarness();
    30	    }
    31	
    32	    function _advanceTick() internal {
    33	        vm.warp(world.getWorldState().nextHeartbeatAtTs);
    34	        world.heartbeat();
    35	    }
    36	
    37	    function _advanceUntilCurrentTick(uint64 targetTick) internal {
    38	        while (world.getWorldState().currentTick < targetTick) {
    39	            _advanceTick();
    40	        }
    41	    }
    42	
    43	    function _mintClan() internal returns (uint32 clanId) {
    44	        vm.prank(elder);
    45	        (clanId,) = world.mintClan(elder);
    46	    }
    47	
    48	    function _firstCs(uint32 clanId) internal view returns (uint32) {
    49	        ClanFullView memory view_ = world.getClanFullView(clanId);
    50	        return view_.clansmen[0].clansman.clansman.clansmanId;
    51	    }
    52	
    53	    function _submitChopWood(uint32 clanId, uint32 csId) internal returns (OrderResult[] memory) {
    54	        ClanOrder[] memory orders = new ClanOrder[](1);
    55	        orders[0] = ClanOrder({
    56	            clansmanId: csId,
    57	            gotoRegion: ClanWorldConstants.REGION_FOREST,
    58	            action: ActionType.ChopWood,
    59	            targetClanId: 0,
    60	            marketToken: address(0),
    61	            marketAmount: 0,
    62	            maxGoldIn: 0
    63	        });
    64	
    65	        vm.prank(elder);
    66	        return world.submitClanOrders(clanId, orders);
    67	    }
    68	
    69	    function _settleChopWood(uint32 clanId, uint32 csId) internal returns (Clansman memory) {
    70	        OrderResult[] memory result = _submitChopWood(clanId, csId);
    71	        assertEq(uint8(result[0].status), uint8(StatusCode.OK), "chop wood accepted");
    72	
    73	        Mission memory mission = world.getActiveMission(csId);
    74	        _advanceUntilCurrentTick(mission.settlesAtTick + 1);
    75	        world.settleClan(clanId);
    76	        return world.getClansman(csId);
    77	    }
    78	
    79	    function test_chopWoodAtForestYieldsBaseTimesActionDuration() public {
    80	        vm.prevrandao(bytes32(uint256(2)));
    81	        uint32 clanId = _mintClan();
    82	        uint32 csId = _firstCs(clanId);
    83	
    84	        Clansman memory cs = _settleChopWood(clanId, csId);
    85	
    86	        uint256 expected = ClanWorldConstants.WOOD_YIELD_PER_TICK * world.getActionDuration(ActionType.ChopWood);
    87	        assertEq(cs.carryWood, expected, "base wood yield");
    88	    }
    89	
    90	    function test_chopWoodCritDistributionAcrossSeeds() public {
    91	        uint256 baseYield = ClanWorldConstants.WOOD_YIELD_PER_TICK * world.getActionDuration(ActionType.ChopWood);
    92	        uint256 critCount = 0;
    93	        world = new GatheringHarness();
    94	        uint256 cleanState = vm.snapshotState();
    95	
    96	        for (uint256 i = 0; i < 100; i++) {
    97	            assertTrue(vm.revertToState(cleanState), "reset gathering world");
    98	            vm.prevrandao(bytes32(uint256(i + 10_000)));
    99	            uint32 clanId = _mintClan();
   100	            uint32 csId = _firstCs(clanId);
   101	
   102	            Clansman memory cs = _settleChopWood(clanId, csId);
   103	            if (cs.carryWood == baseYield * 2) {
   104	                critCount++;
   105	            } else {
   106	                assertEq(cs.carryWood, baseYield, "non-crit yield");
   107	            }
   108	        }
   109	
   110	        assertGe(critCount, 3, "crit count too low");
   111	        assertLe(critCount, 20, "crit count too high");
   112	    }
   113	
   114	    function test_chopWoodClampsToCarryCap() public {
   115	        uint32 clanId = _mintClan();
   116	        uint32 csId = _firstCs(clanId);
   117	        world.setCarryWood(csId, ClanWorldConstants.CLANSMAN_CARRY_CAP - 1e18);
   118	
   119	        Clansman memory cs = _settleChopWood(clanId, csId);
   120	
   121	        assertEq(cs.carryWood, ClanWorldConstants.CLANSMAN_CARRY_CAP, "wood carry cap");
   122	    }
   123	
   124	    function test_chopWoodAppliesCooldownPostSettle() public {
   125	        uint32 clanId = _mintClan();
   126	        uint32 csId = _firstCs(clanId);
   127	
   128	        Clansman memory cs = _settleChopWood(clanId, csId);
   129	
   130	        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "mission completed");
   131	        assertFalse(world.getActiveMission(csId).active, "mission inactive");
   132	        assertGt(cs.cooldownEndsAtTs, block.timestamp, "cooldown starts on settlement");
   133	    }
   134	}

exec
/bin/bash -lc "git show 2bec876:packages/contracts/test/ClanWorld.t.sol | nl -ba | sed -n '430,620p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   430	        // Trigger settlement by calling settleClan
   431	        world.settleClan(clanId);
   432	
   433	        // Verify clansman has carry iron (traveled to Mountains and mined)
   434	        Clansman memory cs = world.getClansman(csId);
   435	        assertGt(cs.carryIron, 0, "clansman should have gathered iron after travel to Mountains");
   436	    }
   437	
   438	    // -------------------------------------------------------------------------
   439	    // Test 10: settlement — deposit adds to vault and clears carry
   440	    // -------------------------------------------------------------------------
   441	
   442	    function test_settlement_depositAddsToVault() public {
   443	        uint32 clanId = _mintClan();
   444	        ClanFullView memory view_ = world.getClanFullView(clanId);
   445	        uint8 homeRegion = view_.clan.clan.baseRegion;
   446	
   447	        // Use two clansmen: one mines iron, one stays home to deposit
   448	        uint32 cs0 = view_.clansmen[0].clansman.clansman.clansmanId;
   449	        uint32 cs1 = view_.clansmen[1].clansman.clansman.clansmanId;
   450	
   451	        // Send cs0 to Mountains to mine iron (requires travel from non-Mountains home)
   452	        uint8 targetRegion = ClanWorldConstants.REGION_MOUNTAINS;
   453	        (uint8 travelToMountains,) = world.quoteTravel(homeRegion, targetRegion);
   454	        _submitOrder(clanId, cs0, targetRegion, ActionType.MineIron);
   455	
   456	        // Advance through travel and the four-tick mining duration.
   457	        for (uint256 i = 0; i < uint256(travelToMountains) + world.getActionDuration(ActionType.MineIron) + 1; i++) {
   458	            _advanceTick();
   459	        }
   460	        world.settleClan(clanId);
   461	
   462	        uint256 carryBefore = world.getClansman(cs0).carryIron;
   463	        assertGt(carryBefore, 0, "cs0 should have carry iron after mining at Mountains");
   464	
   465	        uint256 vaultBefore = world.getClan(clanId).vaultIron;
   466	
   467	        // Wait for cs0 cooldown to expire, then send back to deposit
   468	        vm.warp(block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS + 1);
   469	        _submitOrder(clanId, cs0, homeRegion, ActionType.DepositResources);
   470	
   471	        // Advance through travel back to homebase and the deposit's 1-tick transfer.
   472	        (uint8 travelBack,) = world.quoteTravel(targetRegion, homeRegion);
   473	        for (uint256 i = 0; i < uint256(travelBack) + world.getActionDuration(ActionType.DepositResources) + 1; i++) {
   474	            _advanceTick();
   475	        }
   476	        world.settleClan(clanId);
   477	
   478	        // cs0 carry iron should be cleared and vault iron should have increased
   479	        Clansman memory cs0After = world.getClansman(cs0);
   480	        assertEq(cs0After.carryIron, 0, "carry iron should be cleared after deposit");
   481	
   482	        uint256 vaultAfter = world.getClan(clanId).vaultIron;
   483	        assertGt(vaultAfter, vaultBefore, "vault iron should increase after deposit");
   484	
   485	        // cs1 unused — suppress warning
   486	        cs1;
   487	    }
   488	
   489	    function test_depositResources_woodCarryMovesToVaultAndClears() public {
   490	        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
   491	        uint8 homeRegion = harness.getClan(clanId).baseRegion;
   492	        uint256 woodDelta = 5e18;
   493	
   494	        harness.setCarry(csId, woodDelta, 0, 0, 0);
   495	        uint256 vaultBefore = harness.getClan(clanId).vaultWood;
   496	
   497	        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
   498	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "deposit order should be accepted");
   499	
   500	        Mission memory mission = harness.getActiveMission(csId);
   501	        assertEq(
   502	            mission.settlesAtTick,
   503	            mission.executesAtTick + harness.getActionDuration(ActionType.DepositResources),
   504	            "deposit settles after transfer duration"
   505	        );
   506	
   507	        _advanceTickHarness(harness);
   508	        _advanceTickHarness(harness);
   509	
   510	        assertEq(harness.getClan(clanId).vaultWood, vaultBefore + woodDelta, "vault wood receives carried wood");
   511	        assertEq(harness.getClansman(csId).carryWood, 0, "carry wood is cleared");
   512	    }
   513	
   514	    function test_depositResources_emptyCarryNoopsWithoutEvent() public {
   515	        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
   516	        uint8 homeRegion = harness.getClan(clanId).baseRegion;
   517	
   518	        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
   519	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "empty deposit order should be accepted");
   520	
   521	        vm.recordLogs();
   522	        _advanceTickHarness(harness);
   523	        _advanceTickHarness(harness);
   524	        Vm.Log[] memory logs = vm.getRecordedLogs();
   525	        bytes32 depositedTopic = keccak256("ResourcesDeposited(uint32,uint32,uint256,uint256,uint256,uint256,uint64)");
   526	
   527	        for (uint256 i = 0; i < logs.length; i++) {
   528	            assertTrue(logs[i].topics[0] != depositedTopic, "empty deposit emits no ResourcesDeposited event");
   529	        }
   530	
   531	        assertFalse(harness.getActiveMission(csId).active, "empty deposit still completes");
   532	    }
   533	
   534	    function test_depositResources_multipleTypesMoveTogether() public {
   535	        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
   536	        uint8 homeRegion = harness.getClan(clanId).baseRegion;
   537	        uint256 woodDelta = 4e18;
   538	        uint256 ironDelta = 2e18;
   539	        uint256 fishDelta = 3e18;
   540	
   541	        harness.setCarry(csId, woodDelta, ironDelta, 0, fishDelta);
   542	        Clan memory beforeClan = harness.getClan(clanId);
   543	
   544	        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
   545	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "deposit order should be accepted");
   546	
   547	        _advanceTickHarness(harness);
   548	        _advanceTickHarness(harness);
   549	
   550	        Clan memory afterClan = harness.getClan(clanId);
   551	        Clansman memory afterCs = harness.getClansman(csId);
   552	
   553	        assertEq(afterClan.vaultWood, beforeClan.vaultWood + woodDelta, "wood transferred");
   554	        assertEq(afterClan.vaultIron, beforeClan.vaultIron + ironDelta, "iron transferred");
   555	        assertEq(afterClan.vaultFish, beforeClan.vaultFish + fishDelta, "fish transferred");
   556	        assertEq(afterCs.carryWood, 0, "wood carry cleared");
   557	        assertEq(afterCs.carryIron, 0, "iron carry cleared");
   558	        assertEq(afterCs.carryFish, 0, "fish carry cleared");
   559	    }
   560	
   561	    function test_depositResources_requiresHomeRegion() public {
   562	        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
   563	        uint8 homeRegion = harness.getClan(clanId).baseRegion;
   564	        uint8 nonHomeRegion = homeRegion == ClanWorldConstants.REGION_FOREST
   565	            ? ClanWorldConstants.REGION_MOUNTAINS
   566	            : ClanWorldConstants.REGION_FOREST;
   567	
   568	        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, nonHomeRegion, ActionType.DepositResources);
   569	
   570	        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_INVALID_REGION), "deposit must target home region");
   571	    }
   572	
   573	    function test_depositResources_eventHasCorrectDeltas() public {
   574	        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
   575	        uint8 homeRegion = harness.getClan(clanId).baseRegion;
   576	
   577	        harness.setCarry(csId, 5e18, 2e18, 1e18, 3e18);
   578	
   579	        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
   580	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "deposit order should be accepted");
   581	
   582	        _advanceTickHarness(harness);
   583	        vm.expectEmit(true, true, false, true);
   584	        emit IClanWorldEvents.ResourcesDeposited(clanId, csId, 5e18, 2e18, 1e18, 3e18, 1);
   585	        _advanceTickHarness(harness);
   586	    }
   587	
   588	    function test_quoteTravel_outOfBounds_returnsZero() public {
   589	        (uint8 ticks, bytes8 path) = world.quoteTravel(9, 1);
   590	        assertEq(ticks, 0, "out-of-range src should return 0 ticks");
   591	        assertEq(path, bytes8(0), "out-of-range src should return empty path");
   592	
   593	        (ticks, path) = world.quoteTravel(1, 9);
   594	        assertEq(ticks, 0, "out-of-range dst should return 0 ticks");
   595	        assertEq(path, bytes8(0), "out-of-range dst should return empty path");
   596	    }
   597	
   598	    // -------------------------------------------------------------------------
   599	    // Test A: quoteTravel(9,9) — both out-of-bounds same-region, must return (0, bytes8(0))
   600	    // -------------------------------------------------------------------------
   601	
   602	    function test_quoteTravel_outOfBounds_sameRegion() public {
   603	        // Previously (9,9) escaped the > 8 guard (same-region early return fired first),
   604	        // returning (0, bytes8(uint64(9) << 56)) instead of (0, bytes8(0)).
   605	        (uint8 ticks, bytes8 path) = world.quoteTravel(9, 9);
   606	        assertEq(ticks, 0, "quoteTravel(9,9): travelTicks must be 0");
   607	        assertEq(path, bytes8(0), "quoteTravel(9,9): path must be bytes8(0), not a packed 9-region sentinel");
   608	    }
   609	
   610	    // -------------------------------------------------------------------------
   611	    // Test B: submitClanOrders returns ERR_INVALID_ACTION when clan is >200 ticks behind
   612	    // -------------------------------------------------------------------------
   613	
   614	    function test_submitClanOrders_reverts_when_clan_too_far_behind() public {
   615	        uint32 clanId = _mintClan();
   616	        ClanFullView memory view_ = world.getClanFullView(clanId);
   617	        uint32 csId = view_.clansmen[0].clansman.clansman.clansmanId;
   618	
   619	        // Advance 201 ticks — clan is now 201 ticks behind its lastSettledTick
   620	        for (uint256 i = 0; i < 201; i++) {

exec
/bin/bash -lc "git show 2bec876:packages/contracts/test/HeartbeatOrdering.t.sol | nl -ba | sed -n '180,360p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   180	
   181	    // -------------------------------------------------------------------------
   182	    // test_heartbeat_settlementBeforeMarket
   183	    //
   184	    // Proves Step 1 (settle) fires before Step 2 (market execute) within the SAME
   185	    // heartbeat closing tick T:
   186	    //   - cs0 is placed at Mountains (region 2). Deposit to homebase Forest (region 1)
   187	    //     = 1 tick travel. arrivalTick = T0+1, settlesAtTick = T0+2.
   188	    //   - cs1 at Forest submits MarketSell to UT (region 3) = 2 ticks travel.
   189	    //     executeAtTick = T0+2. (Same tick as cs0 settles.)
   190	    //   - vault starts at 0; cs0 has carry wood.
   191	    //   - Heartbeat at T0+2: Step 1 deposits cs0 carry wood to vault, Step 2 sells
   192	    //     it. Gold increases only if Step 1 ran first (vault was 0 before Step 1).
   193	    //   - If ordering were reversed, sell would fail (vault still 0) and gold stays flat.
   194	    // -------------------------------------------------------------------------
   195	    function test_heartbeat_settlementBeforeMarket() public {
   196	        _setupMarket();
   197	        uint32 clanId = _mintClan();
   198	        uint32 csId0 = _firstCs(clanId);
   199	        uint32 csId1 = _secondCs(clanId);
   200	
   201	        uint64 t0 = world.getWorldState().currentTick;
   202	
   203	        // Place cs0 at Mountains (region 2). Homebase = Forest (region 1).
   204	        // Deposit from Mountains to Forest: travel = 1 tick.
   205	        world.setClansmanRegion(csId0, ClanWorldConstants.REGION_MOUNTAINS);
   206	
   207	        // cs0: submit Deposit. arrivalTick = t0+1, settlesAtTick = t0+2.
   208	        OrderResult[] memory r0 =
   209	            _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
   210	        assertEq(uint8(r0[0].status), uint8(StatusCode.OK), "deposit order must succeed");
   211	        Mission memory depositMission = world.getActiveMission(csId0);
   212	        assertEq(depositMission.settlesAtTick, t0 + 2, "deposit settlesAtTick must be t0+2");
   213	
   214	        // cs1: at Forest. Submit MarketSell to UT. Forest→UT = 2 ticks.
   215	        // executeAtTick = t0+2. Same tick as deposit settles.
   216	        OrderResult[] memory r1 = _submitMarketOrder(clanId, csId1, ActionType.MarketSell, address(woodToken), 5e18, 0);
   217	        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell order must enqueue");
   218	        Mission memory sellMission = world.getActiveMission(csId1);
   219	        assertEq(sellMission.arrivalTick, t0 + 2, "sell arrivalTick must be t0+2");
   220	
   221	        // Give cs0 carry wood. Zero vault so market sell only succeeds if step1 ran first.
   222	        world.setCarryWood(csId0, 10e18);
   223	        world.setVaultWood(clanId, 0);
   224	        assertEq(world.getClan(clanId).vaultWood, 0, "vault wood must be 0 before test tick");
   225	
   226	        uint256 goldBefore = world.getClan(clanId).goldBalance;
   227	
   228	        // Advance to tick t0+2. The heartbeat closing t0+2 runs:
   229	        //   Step 1: _settleCompletingMissions(t0+2) → deposit fires, cs0 carry 10e18 → vault
   230	        //   Step 2: _executeScheduledMarketActions(t0+2) → sell fires, 5e18 vault wood → gold
   231	        // If reversed: sell would fail (vault=0), gold unchanged.
   232	        _advanceToTick(t0 + 3);
   233	
   234	        uint256 goldAfter = world.getClan(clanId).goldBalance;
   235	        assertGt(goldAfter, goldBefore, "gold must increase: settlement ran before market sell");
   236	        assertEq(world.getClansman(csId0).carryWood, 0, "cs0 carry wood cleared by deposit");
   237	        assertFalse(world.getActiveMission(csId0).active, "deposit mission must be complete");
   238	    }
   239	
   240	    // -------------------------------------------------------------------------
   241	    // test_heartbeat_atomicTickSeedPublish
   242	    //
   243	    // Proves Step 5: after heartbeat, currentTick increments by 1, currentTickSeed
   244	    // changes, and the new seed is deterministic from block.prevrandao + prior seed.
   245	    // -------------------------------------------------------------------------
   246	    function test_heartbeat_atomicTickSeedPublish() public {
   247	        WorldState memory before_ = world.getWorldState();
   248	        uint64 tickBefore = before_.currentTick;
   249	        bytes32 seedBefore = before_.currentTickSeed;
   250	
   251	        bytes32 expectedSeed = keccak256(abi.encode(block.prevrandao, seedBefore, tickBefore));
   252	        world.heartbeat();
   253	
   254	        WorldState memory after_ = world.getWorldState();
   255	        assertEq(after_.currentTick, tickBefore + 1, "tick must increment by 1");
   256	        assertNotEq(after_.currentTickSeed, seedBefore, "seed must change after heartbeat");
   257	        assertEq(after_.currentTickSeed, expectedSeed, "seed must match keccak(prevrandao, oldSeed, closedTick)");
   258	
   259	        // Second heartbeat chains from the new seed
   260	        uint64 tick2 = after_.currentTick;
   261	        bytes32 seed2 = after_.currentTickSeed;
   262	        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
   263	        bytes32 expectedSeed2 = keccak256(abi.encode(block.prevrandao, seed2, tick2));
   264	        world.heartbeat();
   265	
   266	        assertEq(world.getWorldState().currentTick, tick2 + 1, "tick must increment again");
   267	        assertEq(world.getWorldState().currentTickSeed, expectedSeed2, "seed must chain from prior seed");
   268	    }
   269	
   270	    // -------------------------------------------------------------------------
   271	    // test_heartbeat_seasonTransition
   272	    //
   273	    // Proves Step 4 (world events) fires AFTER Steps 1-3:
   274	    //   - season boundary is crossed at SEASON_DURATION_TICKS
   275	    //   - currentSeasonNumber increments on the heartbeat that closes tick
   276	    //     SEASON_DURATION_TICKS-1 (newTick = SEASON_DURATION_TICKS >= seasonEndTick)
   277	    //   - no crash or revert when there are no pending missions or market actions
   278	    // -------------------------------------------------------------------------
   279	    function test_heartbeat_seasonTransition() public {
   280	        WorldState memory ws0 = world.getWorldState();
   281	        assertEq(ws0.currentSeasonNumber, 1, "season starts at 1");
   282	        assertEq(ws0.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS, "seasonEndTick starts at 360");
   283	
   284	        // Advance SEASON_DURATION_TICKS heartbeats to cross the boundary
   285	        for (uint256 i = 0; i < ClanWorldConstants.SEASON_DURATION_TICKS; i++) {
   286	            vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
   287	            world.heartbeat();
   288	        }
   289	
   290	        WorldState memory ws1 = world.getWorldState();
   291	        assertEq(ws1.currentSeasonNumber, 2, "season must have incremented to 2 after Steps 1-4");
   292	        assertEq(ws1.currentTick, ClanWorldConstants.SEASON_DURATION_TICKS, "tick must be at season boundary");
   293	        assertEq(ws1.seasonStartTick, ClanWorldConstants.SEASON_DURATION_TICKS, "new seasonStartTick");
   294	        assertEq(ws1.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS * 2, "new seasonEndTick");
   295	    }
   296	
   297	    // -------------------------------------------------------------------------
   298	    // test_heartbeat_noopTick
   299	    //
   300	    // Heartbeat with no pending missions and no queued market actions must:
   301	    //   - not revert
   302	    //   - increment currentTick exactly by 1
   303	    //   - change currentTickSeed
   304	    // -------------------------------------------------------------------------
   305	    function test_heartbeat_noopTick() public {
   306	        uint64 tickBefore = world.getWorldState().currentTick;
   307	        bytes32 seedBefore = world.getWorldState().currentTickSeed;
   308	
   309	        world.heartbeat();
   310	
   311	        assertEq(world.getWorldState().currentTick, tickBefore + 1, "tick must increment");
   312	        assertNotEq(world.getWorldState().currentTickSeed, seedBefore, "seed must change");
   313	    }
   314	
   315	    // -------------------------------------------------------------------------
   316	    // test_heartbeat_multipleStepsInOneTick
   317	    //
   318	    // Proves that when a mission settles at tick T AND a market action is queued at
   319	    // tick T, both fire in the same heartbeat without conflict.
   320	    //   - cs0 placed at Mountains, deposits to Forest homebase: settlesAtTick = T0+2
   321	    //   - cs1 sells wood to UT from Forest: executeAtTick = T0+2
   322	    //   Both succeed in the same heartbeat call at T0+2.
   323	    // -------------------------------------------------------------------------
   324	    function test_heartbeat_multipleStepsInOneTick() public {
   325	        _setupMarket();
   326	        uint32 clanId = _mintClan();
   327	        uint32 csId0 = _firstCs(clanId);
   328	        uint32 csId1 = _secondCs(clanId);
   329	
   330	        uint64 t0 = world.getWorldState().currentTick;
   331	
   332	        // cs0: placed at Mountains (1 tick from Forest homebase).
   333	        // Deposit: arrivalTick = t0+1, settlesAtTick = t0+2.
   334	        world.setClansmanRegion(csId0, ClanWorldConstants.REGION_MOUNTAINS);
   335	        world.setCarryWood(csId0, 10e18);
   336	        OrderResult[] memory r0 =
   337	            _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
   338	        assertEq(uint8(r0[0].status), uint8(StatusCode.OK), "deposit must succeed");
   339	        assertEq(world.getActiveMission(csId0).settlesAtTick, t0 + 2, "deposit settlesAtTick must be t0+2");
   340	
   341	        // cs1: at Forest, sells wood to UT. Forest→UT = 2 ticks. executeAtTick = t0+2.
   342	        // Vault has 20e18 starter wood — sell always has enough.
   343	        OrderResult[] memory r1 = _submitMarketOrder(clanId, csId1, ActionType.MarketSell, address(woodToken), 5e18, 0);
   344	        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell must enqueue");
   345	        assertEq(world.getActiveMission(csId1).arrivalTick, t0 + 2, "sell executeAtTick must be t0+2");
   346	
   347	        uint256 goldBefore = world.getClan(clanId).goldBalance;
   348	
   349	        // Advance to tick t0+3 (the heartbeat closing t0+2 runs step1+step2 together).
   350	        _advanceToTick(t0 + 3);
   351	
   352	        // Both cs0 deposit (settled at T0+2) and cs1 sell (at T0+2) must have fired.
   353	        assertEq(world.getClansman(csId0).carryWood, 0, "deposit settled: carry cleared");
   354	        assertGt(world.getClan(clanId).goldBalance, goldBefore, "sell executed: gold increased");
   355	        assertFalse(world.getActiveMission(csId0).active, "deposit mission must be complete");
   356	    }
   357	}

exec
/bin/bash -lc "git show 2bec876:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1000,1095p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1000	    ///         correct ordering and prevent double-settlement. Callers may call this
  1001	    ///         or settleClan interchangeably; both are safe and idempotent.
  1002	    function settleClansman(uint32 csId) external override nonReentrant {
  1003	        Clansman storage cs = _clansmen[csId];
  1004	        if (cs.clansmanId == 0) return;
  1005	        _settleClan(cs.clanId);
  1006	    }
  1007	
  1008	    /// @notice Finalize season. Phase 1 stub.
  1009	    function finalizeSeason() external override {
  1010	        // TODO Phase 3
  1011	    }
  1012	
  1013	    // =========================================================================
  1014	    // CLAN LIFECYCLE
  1015	    // =========================================================================
  1016	
  1017	    /// @notice Mint a new clan and spawn its homebase.
  1018	    function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
  1019	        require(to != address(0), "ClanWorld: zero address");
  1020	        require(_allClanIds.length < 12, "ClanWorld: max clans");
  1021	        clanId = _nextClanId++;
  1022	        iftTokenId = uint256(clanId); // Phase 1 placeholder; real iNFT is Phase 7
  1023	
  1024	        // Assign homebase region: valid spawn regions are 1,2,4,5,6,7 (not 3=UnicornTown, not 8=DeepSea)
  1025	        uint8[6] memory spawnRegions = [
  1026	            ClanWorldConstants.REGION_FOREST,
  1027	            ClanWorldConstants.REGION_MOUNTAINS,
  1028	            ClanWorldConstants.REGION_WEST_FARMS,
  1029	            ClanWorldConstants.REGION_EAST_FARMS,
  1030	            ClanWorldConstants.REGION_WEST_DOCKS,
  1031	            ClanWorldConstants.REGION_EAST_DOCKS
  1032	        ];
  1033	        uint8 baseRegion = spawnRegions[(clanId - 1) % 6];
  1034	
  1035	        // Create clan
  1036	        Clan storage clan = _clans[clanId];
  1037	        clan.clanId = clanId;
  1038	        clan.iftTokenId = iftTokenId;
  1039	        clan.owner = to;
  1040	        clan.clanState = ClanState.ACTIVE;
  1041	        clan.baseRegion = baseRegion;
  1042	        clan.baseLevel = 1;
  1043	        clan.wallLevel = 0;
  1044	        clan.monumentLevel = 0;
  1045	        clan.livingClansmen = 4;
  1046	        clan.lastSettledTick = _world.currentTick;
  1047	        clan.starvationStartsAtTick = 0;
  1048	        clan.coldDamage = 0;
  1049	        clan.goldBalance = 3e18; // starter gold per v4 spec §12.5
  1050	        clan.blueprintBalance = 0;
  1051	        // Starting vault per v4 spec §12.4
  1052	        clan.vaultWood = 20e18;
  1053	        clan.vaultIron = 0;
  1054	        clan.vaultWheat = 20e18;
  1055	        clan.vaultFish = 2e18;
  1056	
  1057	        // Wheat plots
  1058	        _wheatPlots[clanId][0] = WheatPlot({
  1059	            state: WheatPlotState.Harvestable,
  1060	            region: ClanWorldConstants.REGION_WEST_FARMS,
  1061	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
  1062	            regrowUntilTick: 0
  1063	        });
  1064	        _wheatPlots[clanId][1] = WheatPlot({
  1065	            state: WheatPlotState.Harvestable,
  1066	            region: ClanWorldConstants.REGION_EAST_FARMS,
  1067	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
  1068	            regrowUntilTick: 0
  1069	        });
  1070	
  1071	        // Create 4 clansmen
  1072	        for (uint256 i = 0; i < 4; i++) {
  1073	            uint32 csId = _nextClansmanId++;
  1074	            Clansman storage cs = _clansmen[csId];
  1075	            cs.clansmanId = csId;
  1076	            cs.clanId = clanId;
  1077	            cs.state = ClansmanState.WAITING;
  1078	            cs.currentRegion = baseRegion;
  1079	            cs.cooldownEndsAtTs = 0;
  1080	            cs.lastMissionNonce = 0;
  1081	            cs.carryWood = 0;
  1082	            cs.carryIron = 0;
  1083	            cs.carryWheat = 0;
  1084	            cs.carryFish = 0;
  1085	            _clanClansmanIds[clanId].push(csId);
  1086	        }
  1087	
  1088	        _allClanIds.push(clanId);
  1089	
  1090	        emit ClanSpawned(clanId, to, iftTokenId, baseRegion, _world.currentTick);
  1091	        return (clanId, iftTokenId);
  1092	    }
  1093	
  1094	    /// @notice Submit one or more orders for a clan's clansmen. Per-order failures don't revert.
  1095	    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)

exec
/bin/bash -lc "git show 2bec876:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '880,1025p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   880	    // =========================================================================
   881	
   882	    /// @notice Permissionless heartbeat. Closes the current tick, advances tick counter.
   883	    ///         Execution order per spec §4.2 (CEI-safe):
   884	    ///         CEI guard: nextHeartbeatAtTs written first to close reentrancy window.
   885	    ///         Seed:      closedTick seed derived and published before step 1 so
   886	    ///                    settlement RNG reads real entropy, not zero.
   887	    ///         1. Settle missions completing this tick.
   888	    ///         2. Execute scheduled market actions for closedTick (external calls).
   889	    ///         3. Eager-settle clans touched by world events (Phase 3 stub).
   890	    ///         4. Resolve world events (season boundary, winter transitions).
   891	    ///         5. Increment tick and publish (seed already written above).
   892	    function heartbeat() external override nonReentrant {
   893	        require(block.timestamp >= _world.nextHeartbeatAtTs, "ClanWorld: heartbeat rate limited");
   894	
   895	        uint64 closedTick = _world.currentTick;
   896	
   897	        // CEI: update rate-limit guard before any external calls
   898	        _world.nextHeartbeatAtTs = uint64(block.timestamp) + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS;
   899	
   900	        // Derive and publish seed for closedTick before step 1 (settlement reads it for RNG)
   901	        bytes32 newSeed = keccak256(abi.encode(block.prevrandao, _world.currentTickSeed, closedTick));
   902	        _tickSeeds[closedTick] = newSeed;
   903	        _world.currentTickSeed = newSeed;
   904	
   905	        // Step 1: Settle missions that complete this tick (settlesAtTick == closedTick).
   906	        // Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
   907	        _settleCompletingMissions(closedTick);
   908	
   909	        // Step 2: Execute scheduled market actions for closedTick (may make external calls).
   910	        _executeScheduledMarketActions(closedTick);
   911	
   912	        // Step 3: Eager-settle clans touched by world events (Phase 3 bandit — stub).
   913	        // TODO Phase 3: _settleClansNearBandit(closedTick);
   914	
   915	        // Step 4: Resolve world events (season boundary, winter transitions).
   916	        _resolveWorldEvents(closedTick);
   917	
   918	        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
   919	        uint64 newTick = closedTick + 1;
   920	        _world.currentTick = newTick;
   921	        _world.nextHeartbeatAtTick = newTick + 1;
   922	
   923	        emit TickAdvanced(closedTick, newTick, newSeed);
   924	    }
   925	
   926	    /// @dev Settle missions that complete exactly at `tick` (settlesAtTick == tick).
   927	    ///      Called from heartbeat before market execution and tick increment.
   928	    ///      Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
   929	    function _settleCompletingMissions(uint64 tick) internal {
   930	        for (uint256 i = 0; i < _allClanIds.length; i++) {
   931	            uint32 clanId = _allClanIds[i];
   932	            Clan storage clan = _clans[clanId];
   933	            if (clan.clanState == ClanState.DEAD) continue;
   934	
   935	            uint32[] storage csIds = _clanClansmanIds[clanId];
   936	            for (uint256 j = 0; j < csIds.length; j++) {
   937	                Clansman storage cs = _clansmen[csIds[j]];
   938	                if (cs.state == ClansmanState.DEAD) continue;
   939	
   940	                Mission storage m = _missions[cs.clansmanId];
   941	                if (!m.active) continue;
   942	                if (m.settlesAtTick != tick) continue; // not due this tick
   943	
   944	                // Settle this mission using the single-tick range [tick, tick+1).
   945	                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
   946	            }
   947	        }
   948	    }
   949	
   950	    /// @dev Resolve world events for the tick that was just closed.
   951	    ///      Uses closedTick+1 as the equivalent of the old `newTick` for transition checks.
   952	    function _resolveWorldEvents(uint64 closedTick) internal {
   953	        uint64 newTick = closedTick + 1;
   954	
   955	        // --- season boundary ---
   956	        if (newTick >= _world.seasonEndTick) {
   957	            _world.currentSeasonNumber += 1;
   958	            _world.seasonStartTick = _world.seasonEndTick;
   959	            _world.seasonEndTick = _world.seasonStartTick + ClanWorldConstants.SEASON_DURATION_TICKS;
   960	            // reset winter timers for new season
   961	            _world.winterActive = false;
   962	            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
   963	                - ClanWorldConstants.WINTER_DURATION_TICKS;
   964	            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
   965	        }
   966	
   967	        // --- winter transitions (timer only; mechanics = Phase 10) ---
   968	        if (
   969	            !_world.winterActive && newTick >= _world.winterStartsAtTick
   970	                && _world.winterStartsAtTick < _world.seasonEndTick
   971	        ) {
   972	            _world.winterActive = true;
   973	            emit WinterStarted(newTick);
   974	        }
   975	        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
   976	            _world.winterActive = false;
   977	            emit WinterEnded(newTick);
   978	            // schedule next winter cycle within this season
   979	            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
   980	                - ClanWorldConstants.WINTER_DURATION_TICKS;
   981	            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
   982	            if (nextWinterStart < _world.seasonEndTick) {
   983	                _world.winterStartsAtTick = nextWinterStart;
   984	                _world.winterEndsAtTick = nextWinterEnd;
   985	            } else {
   986	                // no more winters this season; sentinel = seasonEndTick so guard never fires
   987	                _world.winterStartsAtTick = _world.seasonEndTick;
   988	                _world.winterEndsAtTick = _world.seasonEndTick;
   989	            }
   990	        }
   991	    }
   992	
   993	    /// @notice Public settlement trigger — lazily settle a clan.
   994	    function settleClan(uint32 clanId) external override nonReentrant {
   995	        _settleClan(clanId);
   996	    }
   997	
   998	    /// @notice Lazily settle a single clansman's mission to current tick. Idempotent.
   999	    ///         Internally settles the entire clan (including upkeep) to guarantee
  1000	    ///         correct ordering and prevent double-settlement. Callers may call this
  1001	    ///         or settleClan interchangeably; both are safe and idempotent.
  1002	    function settleClansman(uint32 csId) external override nonReentrant {
  1003	        Clansman storage cs = _clansmen[csId];
  1004	        if (cs.clansmanId == 0) return;
  1005	        _settleClan(cs.clanId);
  1006	    }
  1007	
  1008	    /// @notice Finalize season. Phase 1 stub.
  1009	    function finalizeSeason() external override {
  1010	        // TODO Phase 3
  1011	    }
  1012	
  1013	    // =========================================================================
  1014	    // CLAN LIFECYCLE
  1015	    // =========================================================================
  1016	
  1017	    /// @notice Mint a new clan and spawn its homebase.
  1018	    function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
  1019	        require(to != address(0), "ClanWorld: zero address");
  1020	        require(_allClanIds.length < 12, "ClanWorld: max clans");
  1021	        clanId = _nextClanId++;
  1022	        iftTokenId = uint256(clanId); // Phase 1 placeholder; real iNFT is Phase 7
  1023	
  1024	        // Assign homebase region: valid spawn regions are 1,2,4,5,6,7 (not 3=UnicornTown, not 8=DeepSea)
  1025	        uint8[6] memory spawnRegions = [

exec
/bin/bash -lc "git show 2bec876:packages/contracts/script/Deploy.s.sol | nl -ba | sed -n '1,220p'" in /home/claude/code/clan-world
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

exec
/bin/bash -lc "git show 2bec876:packages/contracts/test/SeedPools.t.sol | nl -ba | sed -n '1,220p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	import {Test} from "forge-std/Test.sol";
     5	import {ClanWorld} from "../src/ClanWorld.sol";
     6	import {MinimalERC20} from "../src/MinimalERC20.sol";
     7	import {StubPool} from "../src/StubPool.sol";
     8	import {PoolSeedConfig, ResourceType} from "../src/IClanWorld.sol";
     9	
    10	contract SeedPoolsTest is Test {
    11	    ClanWorld world;
    12	    MinimalERC20 woodToken;
    13	    MinimalERC20 ironToken;
    14	    MinimalERC20 wheatToken;
    15	    MinimalERC20 fishToken;
    16	    MinimalERC20 goldToken;
    17	    MinimalERC20 blueprintToken;
    18	    StubPool woodPool;
    19	    StubPool ironPool;
    20	    StubPool wheatPool;
    21	    StubPool fishPool;
    22	
    23	    function setUp() public {
    24	        world = new ClanWorld();
    25	
    26	        woodToken = new MinimalERC20("Wood", "WOOD");
    27	        ironToken = new MinimalERC20("Iron", "IRON");
    28	        wheatToken = new MinimalERC20("Wheat", "WHEAT");
    29	        fishToken = new MinimalERC20("Fish", "FISH");
    30	        goldToken = new MinimalERC20("Gold", "GOLD");
    31	        blueprintToken = new MinimalERC20("BPRT", "BPRT");
    32	
    33	        address engine = address(world);
    34	        woodPool = new StubPool(address(woodToken), address(goldToken), engine);
    35	        wheatPool = new StubPool(address(wheatToken), address(goldToken), engine);
    36	        fishPool = new StubPool(address(fishToken), address(goldToken), engine);
    37	        ironPool = new StubPool(address(ironToken), address(goldToken), engine);
    38	
    39	        address[6] memory tokens = [
    40	            address(woodToken),
    41	            address(ironToken),
    42	            address(wheatToken),
    43	            address(fishToken),
    44	            address(goldToken),
    45	            address(blueprintToken)
    46	        ];
    47	        address[4] memory pools = [address(woodPool), address(wheatPool), address(fishPool), address(ironPool)];
    48	        world.initTreasury(tokens, pools);
    49	
    50	        _seedTreasuryAndApprove();
    51	        world.seedPools(_seedConfig());
    52	    }
    53	
    54	    function test_deploysFourPoolsAndGetterReturnsExpectedPools() public {
    55	        assertEq(world.getPool(uint8(ResourceType.Wood)), address(woodPool), "wood pool");
    56	        assertEq(world.getPool(uint8(ResourceType.Wheat)), address(wheatPool), "wheat pool");
    57	        assertEq(world.getPool(uint8(ResourceType.Fish)), address(fishPool), "fish pool");
    58	        assertEq(world.getPool(uint8(ResourceType.Iron)), address(ironPool), "iron pool");
    59	    }
    60	
    61	    function test_treasurySeedingTransfersExpectedPoolBalances() public {
    62	        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
    63	        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
    64	
    65	        _assertPoolSeeded(woodToken, woodPool, resSeed, goldSeed, "wood");
    66	        _assertPoolSeeded(wheatToken, wheatPool, resSeed, goldSeed, "wheat");
    67	        _assertPoolSeeded(fishToken, fishPool, resSeed, goldSeed, "fish");
    68	        _assertPoolSeeded(ironToken, ironPool, resSeed, goldSeed, "iron");
    69	        assertEq(goldToken.balanceOf(address(this)), 0, "treasury gold fully seeded");
    70	    }
    71	
    72	    function test_kInvariantHoldsAfterEachSwap() public {
    73	        uint256 kBefore = _k(woodPool);
    74	
    75	        vm.prank(address(world));
    76	        woodPool.swapExactInForOut(10e18, 1);
    77	        uint256 kAfterSell = _k(woodPool);
    78	        assertGe(kAfterSell, kBefore, "k after exact-in sell");
    79	
    80	        vm.prank(address(world));
    81	        woodPool.swapExactOutForInWithMaxIn(3e18, 10e18);
    82	        uint256 kAfterBuy = _k(woodPool);
    83	        assertGe(kAfterBuy, kAfterSell, "k after exact-out buy");
    84	    }
    85	
    86	    function test_pricePreviewMatchesActualSwap() public {
    87	        uint256 amountIn = 25e18;
    88	        uint256 preview = world.getPrice(uint8(ResourceType.Wood), amountIn);
    89	
    90	        vm.prank(address(world));
    91	        uint256 actual = woodPool.swapExactInForOut(amountIn, 0);
    92	
    93	        assertEq(actual, preview, "preview should match exact-in swap");
    94	    }
    95	
    96	    function _seedTreasuryAndApprove() internal {
    97	        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
    98	        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
    99	        uint256 totalGoldSeed = goldSeed * 4;
   100	
   101	        woodToken.seedTreasury(address(this), resSeed);
   102	        wheatToken.seedTreasury(address(this), resSeed);
   103	        fishToken.seedTreasury(address(this), resSeed);
   104	        ironToken.seedTreasury(address(this), resSeed);
   105	        goldToken.seedTreasury(address(this), totalGoldSeed);
   106	
   107	        woodToken.approve(address(world), resSeed);
   108	        wheatToken.approve(address(world), resSeed);
   109	        fishToken.approve(address(world), resSeed);
   110	        ironToken.approve(address(world), resSeed);
   111	        goldToken.approve(address(world), totalGoldSeed);
   112	    }
   113	
   114	    function _seedConfig() internal view returns (PoolSeedConfig memory) {
   115	        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
   116	        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
   117	        return PoolSeedConfig({
   118	            woodSeed: resSeed,
   119	            wheatSeed: resSeed,
   120	            fishSeed: resSeed,
   121	            ironSeed: resSeed,
   122	            goldSeedForWood: goldSeed,
   123	            goldSeedForWheat: goldSeed,
   124	            goldSeedForFish: goldSeed,
   125	            goldSeedForIron: goldSeed
   126	        });
   127	    }
   128	
   129	    function _assertPoolSeeded(
   130	        MinimalERC20 resourceToken,
   131	        StubPool pool,
   132	        uint256 expectedResource,
   133	        uint256 expectedGold,
   134	        string memory label
   135	    ) internal view {
   136	        (uint256 resourceReserve, uint256 goldReserve) = pool.getReserves();
   137	
   138	        assertEq(resourceReserve, expectedResource, string.concat(label, " resource reserve"));
   139	        assertEq(goldReserve, expectedGold, string.concat(label, " gold reserve"));
   140	        assertEq(resourceToken.balanceOf(address(pool)), expectedResource, string.concat(label, " resource balance"));
   141	        assertEq(goldToken.balanceOf(address(pool)), expectedGold, string.concat(label, " gold balance"));
   142	    }
   143	
   144	    function _k(StubPool pool) internal view returns (uint256) {
   145	        (uint256 resourceReserve, uint256 goldReserve) = pool.getReserves();
   146	        return resourceReserve * goldReserve;
   147	    }
   148	}

collab: Wait
codex
One focused pass is back with an event-surface regression on `ResourcesDeposited`. I’m still waiting on the OTC and boundary-token explorers while I tighten the remaining local findings around stale-clan settlement and ABI/event mismatches, since those are the seams most likely to bite after the fix-round.
collab: Wait
collab: Wait
Completed static super-swarm review of PR head 2bec876 with parallel file inspection and three focused subagent passes.
  ✓ Load exact PR files from head 2bec876 and gather surrounding context for touched contracts/tests
  ✓ Delegate focused sub-reviews for OTC lifecycle and token/pool boundary changes
  ✓ Trace mission/deposit/gathering changes and cross-check invariants/tests locally
  → Synthesize findings into requested review format with severity ordering
codex
# Phase Super-Swarm Review — PR #200 (head 2bec876)

## SUMMARY
NEEDS_FIXES. The biggest issue is that OTC accepts trust partially-settled clan state, so clans that are more than 200 ticks stale can trade against incorrect balances and liveness. I also found a few integration-surface regressions around proposal-cap accounting, treasury/pool wiring safety, and externally visible ABI/state seams. I would not merge this as-is.

## HIGH severity findings
- `packages/contracts/src/ClanWorld.sol:376-380, 929-946, 1809-1810, 1891-1892, 1975-1976, 2065-2066`  
  `_settleClan()` is capped at 200 ticks per call, but every `accept*Transfer()` path settles each clan only once and then immediately trusts the resulting balances and clan state. Heartbeat only settles missions due on the just-closed tick; it does not keep `lastSettledTick` current for upkeep/starvation. That means a clan idle for >200 ticks can accept OTC transfers against partially-settled state, especially for vault/bundled transfers where wheat/fish upkeep and pending deposits after tick 200 are ignored. Suggested fix: use the same “must settle first” guard as `submitClanOrders()`, or add a helper that fully settles/reverts before any OTC path that relies on balances or `ClanState`.

## MEDIUM severity findings
- `packages/contracts/src/ClanWorld.sol:1786, 1804, 1821, 1837, 1862, 1883, 1911, 1927, 1957, 1970, 1987, 2003, 2030, 2055, 2087, 2105`  
  Expired proposals still count toward `_openOtcProposalsByClan`. The counter is incremented on propose and decremented only on explicit accept/cancel, so once 8 proposals expire the clan is still hard-capped until the proposer manually cancels them one by one. That makes “open proposal” accounting materially wrong. Suggested fix: lazily reap expired proposals from the cap path, or add an explicit sweep/close flow and test cap reopening after expiry.

- `packages/contracts/src/ClanWorld.sol:1715-1761`, `packages/contracts/src/StubPool.sol:35-43`  
  `initTreasury()` accepts arbitrary pool addresses, and `seedPools()` transfers the full seed inventory before validating that each target is actually the expected pool contract. If a pool slot is an EOA or wrong contract, the token transfers can succeed and the subsequent `seed()` call can also no-op successfully, permanently stranding liquidity while marking the treasury seeded. Suggested fix: validate `code.length`, `ENGINE`, and token pairing before transferring any seed inventory.

- `packages/contracts/src/StubPool.sol:66-75, 86-95`, `packages/contracts/src/ClanWorld.sol:1563-1564, 1608-1610, 1738-1750`  
  The new boundary-token/pool seam is not actually kept in sync after seeding. Swaps only mutate `reserveA/reserveB` and internal clan balances; they never move `TOKEN_A`/`TOKEN_B`, so after the first trade the pool’s ERC20 balances diverge from its reported reserves. That makes the exported pool/token surface misleading for any external consumer. Suggested fix: either keep this as a pure math oracle and stop exposing it as a live ERC20 seam, or wire swap flows so token balances and reserves stay aligned.

- `packages/contracts/src/IClanWorld.sol:582-589`, `packages/contracts/src/ClanWorld.sol:682-713`, `packages/contracts/test/ClanWorld.t.sol:521-529`  
  `ResourcesDeposited` changed signature from the previous `...uint32 tick` shape to `...uint64 atTick`, which changes the event topic hash. The PR updates local tests, but any existing indexer/UI/automation filtering the old topic will silently stop seeing deposit events after upgrade. Suggested fix: preserve the old event signature, version the event name, or ship explicit migration coverage for downstream consumers.

## LOW severity findings
- `packages/contracts/src/StubPool.sol:45-49, 99-100`, `packages/contracts/src/ClanWorld.sol:1405-1413, 2186-2187`, `packages/contracts/test/SeedPools.t.sol:86-93`  
  `getPrice()` previews exact-in sells with zero output allowed, but the reachable sell path uses `sellResource(amount)` which hardcodes `minOut = 1`. Dust sells can therefore look quotable off-chain and then fail during heartbeat as a generic `ERR_INVALID_ACTION`. Suggested fix: make preview and execution use the same minimum-output rule, and add a regression test around zero-output sells.

## Cross-cutting observations
The OTC happy paths and the “settle before debit” fix-round coverage are much better than earlier rounds, but the tests still miss the most important integration seams: >200-tick stale clans, cap reuse after proposal expiry, bad treasury/pool wiring, and live reserve-vs-balance drift on the new token boundary.

This was a static review against git object `2bec876` with parallel file inspection and subagent passes. I did not run `forge test` here because the workspace is read-only.
2026-04-30T01:45:31.875392Z ERROR codex_core::session: failed to record rollout items: thread 019ddc0a-f5f2-7310-b20a-5f39d6730e86 not found
2026-04-30T01:45:31.875464Z ERROR codex_core::session: failed to record rollout items: thread 019ddc0a-55a7-7272-9909-6f85f5f69107 not found
2026-04-30T01:45:31.875507Z ERROR codex_core::session: failed to record rollout items: thread 019ddc0a-f6ee-7992-b1c8-a7c01fab905a not found
2026-04-30T01:45:31.875763Z ERROR codex_core::session: failed to record rollout items: thread 019ddc0a-f64f-7382-b490-20f4b632cff1 not found
tokens used
137,897
# Phase Super-Swarm Review — PR #200 (head 2bec876)

## SUMMARY
NEEDS_FIXES. The biggest issue is that OTC accepts trust partially-settled clan state, so clans that are more than 200 ticks stale can trade against incorrect balances and liveness. I also found a few integration-surface regressions around proposal-cap accounting, treasury/pool wiring safety, and externally visible ABI/state seams. I would not merge this as-is.

## HIGH severity findings
- `packages/contracts/src/ClanWorld.sol:376-380, 929-946, 1809-1810, 1891-1892, 1975-1976, 2065-2066`  
  `_settleClan()` is capped at 200 ticks per call, but every `accept*Transfer()` path settles each clan only once and then immediately trusts the resulting balances and clan state. Heartbeat only settles missions due on the just-closed tick; it does not keep `lastSettledTick` current for upkeep/starvation. That means a clan idle for >200 ticks can accept OTC transfers against partially-settled state, especially for vault/bundled transfers where wheat/fish upkeep and pending deposits after tick 200 are ignored. Suggested fix: use the same “must settle first” guard as `submitClanOrders()`, or add a helper that fully settles/reverts before any OTC path that relies on balances or `ClanState`.

## MEDIUM severity findings
- `packages/contracts/src/ClanWorld.sol:1786, 1804, 1821, 1837, 1862, 1883, 1911, 1927, 1957, 1970, 1987, 2003, 2030, 2055, 2087, 2105`  
  Expired proposals still count toward `_openOtcProposalsByClan`. The counter is incremented on propose and decremented only on explicit accept/cancel, so once 8 proposals expire the clan is still hard-capped until the proposer manually cancels them one by one. That makes “open proposal” accounting materially wrong. Suggested fix: lazily reap expired proposals from the cap path, or add an explicit sweep/close flow and test cap reopening after expiry.

- `packages/contracts/src/ClanWorld.sol:1715-1761`, `packages/contracts/src/StubPool.sol:35-43`  
  `initTreasury()` accepts arbitrary pool addresses, and `seedPools()` transfers the full seed inventory before validating that each target is actually the expected pool contract. If a pool slot is an EOA or wrong contract, the token transfers can succeed and the subsequent `seed()` call can also no-op successfully, permanently stranding liquidity while marking the treasury seeded. Suggested fix: validate `code.length`, `ENGINE`, and token pairing before transferring any seed inventory.

- `packages/contracts/src/StubPool.sol:66-75, 86-95`, `packages/contracts/src/ClanWorld.sol:1563-1564, 1608-1610, 1738-1750`  
  The new boundary-token/pool seam is not actually kept in sync after seeding. Swaps only mutate `reserveA/reserveB` and internal clan balances; they never move `TOKEN_A`/`TOKEN_B`, so after the first trade the pool’s ERC20 balances diverge from its reported reserves. That makes the exported pool/token surface misleading for any external consumer. Suggested fix: either keep this as a pure math oracle and stop exposing it as a live ERC20 seam, or wire swap flows so token balances and reserves stay aligned.

- `packages/contracts/src/IClanWorld.sol:582-589`, `packages/contracts/src/ClanWorld.sol:682-713`, `packages/contracts/test/ClanWorld.t.sol:521-529`  
  `ResourcesDeposited` changed signature from the previous `...uint32 tick` shape to `...uint64 atTick`, which changes the event topic hash. The PR updates local tests, but any existing indexer/UI/automation filtering the old topic will silently stop seeing deposit events after upgrade. Suggested fix: preserve the old event signature, version the event name, or ship explicit migration coverage for downstream consumers.

## LOW severity findings
- `packages/contracts/src/StubPool.sol:45-49, 99-100`, `packages/contracts/src/ClanWorld.sol:1405-1413, 2186-2187`, `packages/contracts/test/SeedPools.t.sol:86-93`  
  `getPrice()` previews exact-in sells with zero output allowed, but the reachable sell path uses `sellResource(amount)` which hardcodes `minOut = 1`. Dust sells can therefore look quotable off-chain and then fail during heartbeat as a generic `ERR_INVALID_ACTION`. Suggested fix: make preview and execution use the same minimum-output rule, and add a regression test around zero-output sells.

## Cross-cutting observations
The OTC happy paths and the “settle before debit” fix-round coverage are much better than earlier rounds, but the tests still miss the most important integration seams: >200-tick stale clans, cap reuse after proposal expiry, bad treasury/pool wiring, and live reserve-vs-balance drift on the new token boundary.

This was a static review against git object `2bec876` with parallel file inspection and subagent passes. I did not run `forge test` here because the workspace is read-only.
EXIT=0
