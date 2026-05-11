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
session id: 019ddb56-daf1-70e3-8be6-4abb552a260f
--------
user
Read prompt+diff from stdin. Use parallel tool calls + subagents. Output review in requested format.

<stdin>
Senior staff engineer FINAL pre-merge review for PR #200 (Phase 7 — OTC Transfer Surface) at head 0c20b46.

Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.

## Your task
Cohesive-phase review. Look for:
1. CROSS-CUTTING bugs (proposal lifecycle integrity, atomic accept-all-or-nothing, state machine consistency)
2. Spec compliance (§7.1-7.5)
3. Security — re-entrancy in transfers, replay protection, signature/owner validation
4. Data flow — gold/vault/blueprint accounting integrity (Phase 5 + 8.3 dependencies)
5. Dead-clan restrict (10.5 hook works correctly)
6. Test coverage on the atomic-failure paths

USE PARALLEL TOOL CALLS.

## Output

# Phase Super-Swarm Review — PR #200 (head 0c20b46)
## SUMMARY (verdict + top concerns)
## HIGH severity findings
## MEDIUM severity findings
## LOW severity findings
## Cross-cutting observations

DIFF FOLLOWS.
---
diff --git a/packages/contracts/abi/IClanWorld.json b/packages/contracts/abi/IClanWorld.json
index 2ca0d2c..73f54b3 100644
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
         {
-          "name": "to",
-          "type": "address",
-          "internalType": "address"
+          "name": "ironAmt",
+          "type": "uint256",
+          "internalType": "uint256"
+        },
+        {
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
@@ -2764,13 +3470,160 @@
           "internalType": "uint32"
         },
         {
-          "name": "amount",
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
+        },
+        {
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
@@ -3284,34 +4274,34 @@
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
         },
         {
-          "name": "atTick",
-          "type": "uint64",
+          "name": "tick",
+          "type": "uint32",
           "indexed": false,
-          "internalType": "uint64"
+          "internalType": "uint32"
         }
       ],
       "anonymous": false
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
index 945490b..1705e71 100644
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
 
@@ -62,9 +67,14 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     mapping(uint8 => mapping(uint32 => uint256)) private _defenderCountByRegionClan; // region => clanId => clansmen count
     mapping(uint32 => uint8) private _clansmanDefendingRegion; // clansmanId => defended home region
     mapping(uint64 => bytes32) private _tickSeeds; // tick => seed
+    mapping(uint256 => OtcProposal) private _otcGoldProposals;
+    mapping(uint256 => VaultTransferProposal) private _otcVaultTransferProposals;
+    mapping(uint256 => BlueprintTransferProposal) private _otcBlueprintTransferProposals;
+    mapping(uint256 => BundledTransferProposal) private _otcBundledTransferProposals;
 
     uint32 private _nextClanId;
     uint32 private _nextClansmanId;
+    uint256 private _nextOtcProposalId;
     uint32[] private _allClanIds;
 
     // per-clan clansman list: clanId => clansmanId[]
@@ -74,7 +84,10 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     // CONSTANTS — Wheat harvest rate (not in IClanWorld constants)
     // =========================================================================
 
+    uint64 private constant DEPOSIT_DURATION_TICKS = 1;
     uint256 private constant WHEAT_HARVEST_RATE = 20e18;
+    uint256 public constant INITIAL_RESOURCE_POOL_SEED = 100_000e18;
+    uint256 public constant INITIAL_GOLD_POOL_SEED = 50_000e18;
     /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
     uint256 public constant MAX_MARKET_ACTIONS_PER_TICK = 32;
 
@@ -91,13 +104,13 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
@@ -429,7 +442,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
     }
 
-    /// @dev Resolve one tick of action for a clansman that is in ACTING state.
+    /// @dev Resolve an action for a clansman that is in ACTING state.
     function _resolveAction(
         Clan storage clan,
         Clansman storage cs,
@@ -452,7 +465,9 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         } else if (action == ActionType.HarvestWheat) {
             _gatherWheat(clan, cs, m, clanId, tick, starving);
         } else if (action == ActionType.DepositResources) {
-            _doDeposit(clan, cs, m, clanId, tick);
+            // Deposit event ABI intentionally stores the current tick as uint32.
+            // forge-lint: disable-next-line(unsafe-typecast)
+            _doDeposit(clan, cs, m, clanId, uint32(tick));
         } else if (action == ActionType.Wait) {
             // NOOP — worker stays ACTING (continuous), no transition needed
             // Wait mission is effectively persistent until interrupted
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
@@ -665,7 +679,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         // else continuous
     }
 
-    function _doDeposit(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint64 tick)
+    function _doDeposit(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint32 tick)
         internal
     {
         // Must be at homebase region
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
@@ -1732,10 +1756,349 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
+        require(_clans[toClanId].clanId != 0, "ClanWorld: target clan not found");
+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(expiryTick <= type(uint64).max, "ClanWorld: expiry overflow");
+        if (fromClan.goldBalance < amount) revert("ERR_NOT_ENOUGH_GOLD");
+
+        proposalId = _nextOtcProposalId++;
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
+        Clan storage fromClan = _clans[proposal.from];
+        Clan storage toClan = _clans[proposal.to];
+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(toClan.owner == msg.sender, "ClanWorld: not clan owner");
+        if (fromClan.goldBalance < proposal.amount) revert("ERR_NOT_ENOUGH_GOLD");
+
+        fromClan.goldBalance -= proposal.amount;
+        toClan.goldBalance += proposal.amount;
+        proposal.accepted = true;
+
+        emit GoldTransferAccepted(proposalId, proposal.from, proposal.to, proposal.amount, _world.currentTick);
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
+        proposal.cancelled = true;
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
+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
+        require(_clans[toClanId].clanId != 0, "ClanWorld: target clan not found");
+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        if (!_hasVaultResources(fromClan, woodAmt, wheatAmt, fishAmt, ironAmt)) {
+            revert("ERR_NOT_ENOUGH_RESOURCES");
+        }
+
+        proposalId = _nextOtcProposalId++;
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
+        Clan storage fromClan = _clans[proposal.from];
+        Clan storage toClan = _clans[proposal.to];
+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(toClan.owner == msg.sender, "ClanWorld: not clan owner");
+        if (!_hasVaultResources(fromClan, proposal.wood, proposal.wheat, proposal.fish, proposal.iron)) {
+            revert("ERR_NOT_ENOUGH_RESOURCES");
+        }
+
+        fromClan.vaultWood -= proposal.wood;
+        fromClan.vaultWheat -= proposal.wheat;
+        fromClan.vaultFish -= proposal.fish;
+        fromClan.vaultIron -= proposal.iron;
+        toClan.vaultWood += proposal.wood;
+        toClan.vaultWheat += proposal.wheat;
+        toClan.vaultFish += proposal.fish;
+        toClan.vaultIron += proposal.iron;
+        proposal.accepted = true;
+
+        emit VaultTransferAccepted(
+            proposalId,
+            proposal.from,
+            proposal.to,
+            proposal.wood,
+            proposal.wheat,
+            proposal.fish,
+            proposal.iron,
+            _world.currentTick
+        );
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
+        proposal.cancelled = true;
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
+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
+        require(_clans[toClanId].clanId != 0, "ClanWorld: target clan not found");
+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        if (fromClan.blueprintBalance < amount) revert("ERR_NOT_ENOUGH_BLUEPRINT");
+
+        proposalId = _nextOtcProposalId++;
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
+        Clan storage fromClan = _clans[proposal.from];
+        Clan storage toClan = _clans[proposal.to];
+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(toClan.owner == msg.sender, "ClanWorld: not clan owner");
+        if (fromClan.blueprintBalance < proposal.amount) revert("ERR_NOT_ENOUGH_BLUEPRINT");
+
+        fromClan.blueprintBalance -= proposal.amount;
+        toClan.blueprintBalance += proposal.amount;
+        proposal.accepted = true;
+
+        emit BlueprintTransferAccepted(proposalId, proposal.from, proposal.to, proposal.amount, _world.currentTick);
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
+        proposal.cancelled = true;
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
+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
+        require(_clans[toClanId].clanId != 0, "ClanWorld: target clan not found");
+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(!_isEmptyBundledTransfer(gold, wood, wheat, fish, iron, blueprint), "ClanWorld: empty bundled transfer");
+        _requireBundledTransferBalance(fromClan, gold, wood, wheat, fish, iron, blueprint);
+
+        proposalId = _nextOtcProposalId++;
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
+        Clan storage fromClan = _clans[proposal.from];
+        Clan storage toClan = _clans[proposal.to];
+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
+        require(toClan.owner == msg.sender, "ClanWorld: not clan owner");
+        _requireBundledTransferBalance(
+            fromClan, proposal.gold, proposal.wood, proposal.wheat, proposal.fish, proposal.iron, proposal.blueprint
+        );
+
+        fromClan.goldBalance -= proposal.gold;
+        fromClan.vaultWood -= proposal.wood;
+        fromClan.vaultWheat -= proposal.wheat;
+        fromClan.vaultFish -= proposal.fish;
+        fromClan.vaultIron -= proposal.iron;
+        fromClan.blueprintBalance -= proposal.blueprint;
+        toClan.goldBalance += proposal.gold;
+        toClan.vaultWood += proposal.wood;
+        toClan.vaultWheat += proposal.wheat;
+        toClan.vaultFish += proposal.fish;
+        toClan.vaultIron += proposal.iron;
+        toClan.blueprintBalance += proposal.blueprint;
+        proposal.accepted = true;
+
+        emit BundledTransferAccepted(
+            proposalId,
+            proposal.from,
+            proposal.to,
+            proposal.gold,
+            proposal.wood,
+            proposal.wheat,
+            proposal.fish,
+            proposal.iron,
+            proposal.blueprint,
+            _world.currentTick
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
+        proposal.cancelled = true;
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
     function transferGold(uint32, uint32, uint256) external pure override {
         revert("OTC transfers not implemented");
     }
@@ -1768,6 +2131,30 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
@@ -1801,10 +2188,13 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
@@ -1852,6 +2242,37 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
index 2b80fbe..674e538 100644
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
@@ -188,8 +190,8 @@ struct WorldState {
     uint64 seasonStartTick;
     uint64 seasonEndTick;
     bool seasonFinalized;
-    uint64 currentSeasonNumber;   // 1-indexed; incremented each time seasonEndTick is crossed
-    uint64 nextHeartbeatAtTick;   // estimated tick that will be opened by the next heartbeat (for off-chain UI)
+    uint64 currentSeasonNumber; // 1-indexed; incremented each time seasonEndTick is crossed
+    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)
 
     uint64 nextHeartbeatAtTs;
     uint64 nextBanditSpawnEligibleTick;
@@ -328,6 +330,50 @@ struct ScheduledMarketAction {
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
@@ -533,11 +579,11 @@ interface IClanWorldEvents {
     event ResourcesDeposited(
         uint32 indexed clanId,
         uint32 indexed clansmanId,
-        uint256 wood,
-        uint256 iron,
-        uint256 wheat,
-        uint256 fish,
-        uint64 atTick
+        uint256 woodDelta,
+        uint256 ironDelta,
+        uint256 wheatDelta,
+        uint256 fishDelta,
+        uint32 tick
     );
 
     // ----- building -----
@@ -576,6 +622,8 @@ interface IClanWorldEvents {
         uint256 maxGoldIn
     );
     event MarketActionFailed(uint32 indexed clanId, uint32 indexed clansmanId, ActionType action, StatusCode reason);
+    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
+    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);
 
     // ----- bandits -----
     event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
@@ -614,6 +662,82 @@ interface IClanWorldEvents {
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
@@ -674,6 +798,52 @@ interface IClanWorld is IClanWorldEvents {
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
@@ -699,6 +869,12 @@ interface IClanWorld is IClanWorldEvents {
 
     function getTreasuryState() external view returns (TreasuryState memory);
 
+    function getResourceToken(uint8 resourceType) external view returns (address);
+
+    function getPool(uint8 resourceType) external view returns (address);
+
+    function getPrice(uint8 resourceType, uint256 amountIn) external view returns (uint256 amountOut);
+
     function getClan(uint32 clanId) external view returns (Clan memory);
 
     function getClansman(uint32 clansmanId) external view returns (Clansman memory);
@@ -720,6 +896,17 @@ interface IClanWorld is IClanWorldEvents {
 
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
index 0000000..a32b944
--- /dev/null
+++ b/packages/contracts/test/BlueprintTransferOtc.t.sol
@@ -0,0 +1,151 @@
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
+        assertTrue(world.getOtcBlueprintTransferProposal(proposalId).accepted, "proposal accepted");
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
+        assertTrue(world.getOtcBlueprintTransferProposal(proposalId).cancelled, "proposal cancelled");
+        vm.expectRevert("ClanWorld: proposal cancelled");
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
index 0000000..9621f57
--- /dev/null
+++ b/packages/contracts/test/BundledTransferOtc.t.sol
@@ -0,0 +1,228 @@
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
+        assertTrue(world.getOtcBundledTransferProposal(proposalId).accepted, "proposal accepted");
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
+        assertTrue(world.getOtcBundledTransferProposal(proposalId).cancelled, "proposal cancelled");
+        vm.expectRevert("ClanWorld: proposal cancelled");
+        vm.prank(elderB);
+        world.acceptBundledTransfer(proposalId);
+    }
+
+    function test_proposeBundledTransfer_revertsWhenEmpty() public {
+        (uint32 clanA, uint32 clanB,) = _mintThreeClans();
+
+        vm.expectRevert("ClanWorld: empty bundled transfer");
+        _propose(clanA, clanB, 0, 0, 0, 0, 0, 0, 10);
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
index 92781d3..f19e312 100644
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
+        bytes32 depositedTopic = keccak256("ResourcesDeposited(uint32,uint32,uint256,uint256,uint256,uint256,uint32)");
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
index 0000000..e2eb5db
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
+        assertTrue(world.getOtcGoldProposal(goldProposal).cancelled, "gold cancelled");
+        assertTrue(world.getOtcVaultTransferProposal(vaultProposal).cancelled, "vault cancelled");
+        assertTrue(world.getOtcBlueprintTransferProposal(blueprintProposal).cancelled, "blueprint cancelled");
+        assertTrue(world.getOtcBundledTransferProposal(bundledProposal).cancelled, "bundled cancelled");
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
index 0000000..a4cca26
--- /dev/null
+++ b/packages/contracts/test/GoldTransferOtc.t.sol
@@ -0,0 +1,170 @@
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
+        assertTrue(world.getOtcGoldProposal(proposalId).accepted, "proposal accepted");
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
+        assertTrue(world.getOtcGoldProposal(proposalId).cancelled, "proposal cancelled");
+        vm.expectRevert("ClanWorld: proposal cancelled");
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
index 0000000..3711711
--- /dev/null
+++ b/packages/contracts/test/VaultTransferOtc.t.sol
@@ -0,0 +1,189 @@
+// SPDX-License-Identifier: MIT
+pragma solidity ^0.8.34;
+
+import {Test} from "forge-std/Test.sol";
+import {ClanWorld} from "../src/ClanWorld.sol";
+import {ClanWorldConstants, Clan, ClanState, VaultTransferProposal} from "../src/IClanWorld.sol";
+
+contract VaultTransferHarness is ClanWorld {
+    function setVault(uint32 clanId, uint256 wood, uint256 wheat, uint256 fish, uint256 iron) external {
+        _clans[clanId].vaultWood = wood;
+        _clans[clanId].vaultWheat = wheat;
+        _clans[clanId].vaultFish = fish;
+        _clans[clanId].vaultIron = iron;
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
+        assertTrue(world.getOtcVaultTransferProposal(proposalId).accepted, "proposal accepted");
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
+        assertTrue(world.getOtcVaultTransferProposal(proposalId).cancelled, "proposal cancelled");
+        vm.expectRevert("ClanWorld: proposal cancelled");
+        vm.prank(elderB);
+        world.acceptVaultTransfer(proposalId);
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
+    function _advanceTick() internal {
+        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
+        world.heartbeat();
+    }
+}
</stdin>
warning: Codex could not find bubblewrap on PATH. Install bubblewrap with your OS package manager. See the sandbox prerequisites: https://developers.openai.com/codex/concepts/sandboxing#prerequisites. Codex will use the vendored bubblewrap in the meantime.
codex
Reviewing the Phase 7 diff as a cohesive pre-merge pass. I’m going to split this into parallel reads: one track on OTC lifecycle/security/accounting in the contract, and one on spec/tests/dead-clan coverage so we can converge on cross-cutting findings faster.
  → Gather contract/spec context for Phase 7 OTC transfer surface and related invariants
  • Parallelize focused review across OTC logic and test/spec coverage
  • Synthesize findings into requested review format with severity ordering
exec
/bin/bash -lc "pwd && rg --files packages/contracts | rg 'AGENTS\\.md|IClanWorld\\.sol|ClanWorld\\.sol|MinimalERC20\\.sol|StubPool\\.sol|.*Otc.*\\.t\\.sol|DeadClanOtc\\.t\\.sol|SeedPools\\.t\\.sol|ResourceBoundaryTokens\\.t\\.sol|ClanWorld\\.t\\.sol|HeartbeatOrdering\\.t\\.sol|Reentrancy\\.t\\.sol'" in /home/claude/code/clan-world
 succeeded in 0ms:
/home/claude/code/omnipass-world/clan-world
packages/contracts/AGENTS.md
packages/contracts/src/ClanWorld.sol
packages/contracts/src/StubPool.sol
packages/contracts/src/IClanWorld.sol
packages/contracts/src/MinimalERC20.sol
packages/contracts/test/SeedPools.t.sol
packages/contracts/test/HeartbeatOrdering.t.sol
packages/contracts/test/Reentrancy.t.sol
packages/contracts/test/ResourceBoundaryTokens.t.sol
packages/contracts/test/ClanWorld.t.sol

exec
/bin/bash -lc "rg -n \"7\\.1|7\\.2|7\\.3|7\\.4|7\\.5|OTC|atomic|dead-clan|dead clan|blueprint transfer|vault transfer|gold transfer|bundled transfer\" docs packages/contracts -g '"'!packages/contracts/abi/**'"'" in /home/claude/code/clan-world
 succeeded in 0ms:
docs/reviews/pr194-r2-codereview-codex-5-5.md:2480:+    ///         5. Increment tick and publish the next tick seed atomically.
docs/reviews/pr194-r2-codereview-codex-5-5.md:2520:-        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
docs/reviews/pr194-r2-codereview-codex-5-5.md:4779:     "convex": "1.17.4",
docs/reviews/pr194-r2-codereview-codex-5-5.md:5201:2026-04-29T17:33:17.264004Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
docs/reviews/pr194-r2-codereview-codex-5-5.md:5203:2026-04-29T17:33:17.270665Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
docs/reviews/pr194-r2-codereview-codex-5-5.md:5205:2026-04-29T17:33:17.328460Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
docs/reviews/pr194-r2-codereview-codex-5-5.md:6192:  2304	    ///         5. Increment tick and publish the next tick seed atomically.
docs/reviews/pr194-r2-codereview-codex-5-5.md:6441:   643	            // Persistent mission. Registration happens atomically at order submission.
docs/reviews/pr194-r2-codereview-codex-5-5.md:7468:    "convex": "1.17.4",
docs/reviews/pr194-r2-codereview-codex-5-5.md:8491:   354	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr194-r2-codereview-codex-5-5.md:8497:   360	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr194-r2-codereview-gemini-3-1-pro.md:114:  Symbol(gaxios-gaxios-error): '6.7.1'
docs/reviews/pr194-r2-codereview-gemini-3-1-pro.md:229:  Symbol(gaxios-gaxios-error): '6.7.1'
docs/reviews/pr194-r2-codereview-gemini-3-1-pro.md:344:  Symbol(gaxios-gaxios-error): '6.7.1'
docs/reviews/pr194-r2-codereview-gemini-3-1-pro.md:459:  Symbol(gaxios-gaxios-error): '6.7.1'
docs/reviews/pr194-r2-codereview-gemini-3-1-pro.md:574:  Symbol(gaxios-gaxios-error): '6.7.1'
docs/reviews/pr194-r2-codereview-gemini-3-1-pro.md:689:  Symbol(gaxios-gaxios-error): '6.7.1'
docs/reviews/pr194-r2-codereview-gemini-3-1-pro.md:804:  Symbol(gaxios-gaxios-error): '6.7.1'
docs/reviews/pr194-r2-codereview-gemini-3-1-pro.md:919:  Symbol(gaxios-gaxios-error): '6.7.1'
docs/reviews/pr194-r2-codereview-gemini-3-1-pro.md:1034:  Symbol(gaxios-gaxios-error): '6.7.1'
docs/reviews/pr200-codereview-codex-5-5.md:17:Senior staff engineer FINAL pre-merge review for PR #200 (Phase 7 — OTC Transfer Surface) at head 0c20b46.
docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr200-codereview-codex-5-5.md:23:1. CROSS-CUTTING bugs (proposal lifecycle integrity, atomic accept-all-or-nothing, state machine consistency)
docs/reviews/pr200-codereview-codex-5-5.md:24:2. Spec compliance (§7.1-7.5)
docs/reviews/pr200-codereview-codex-5-5.md:28:6. Test coverage on the atomic-failure paths
docs/reviews/pr200-codereview-codex-5-5.md:1871:     // OTC TRANSFERS
docs/reviews/pr200-codereview-codex-5-5.md:2103:+        require(!_isEmptyBundledTransfer(gold, wood, wheat, fish, iron, blueprint), "ClanWorld: empty bundled transfer");
docs/reviews/pr200-codereview-codex-5-5.md:2209:         revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-5.md:2323:     // OTC transfers
docs/reviews/pr200-codereview-codex-5-5.md:2557:     // ----- OTC transfers -----
docs/reviews/pr200-codereview-codex-5-5.md:2638:     // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/pr200-codereview-codex-5-5.md:3281:+        vm.expectRevert("ClanWorld: empty bundled transfer");
docs/reviews/pr200-codereview-codex-5-5.md:5079:packages/contracts/src/ClanWorld.sol:309:        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr200-codereview-codex-5-5.md:5081:packages/contracts/src/ClanWorld.sol:315:                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr200-codereview-codex-5-5.md:5137:packages/contracts/test/ClanWorld.t.sol:1699:    // 3.E3: ERR_CLANSMAN_DEAD — dead clansman is rejected
docs/reviews/pr200-codereview-codex-5-5.md:5140:packages/contracts/test/ClanWorld.t.sol:1727:        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CLANSMAN_DEAD), "3.E3: dead clansman must be rejected");
docs/reviews/pr200-codereview-codex-5-5.md:5144:packages/contracts/test/ClanWorld.t.sol:2173:    // Path 6: dead clansman mid-mission → mission invalidated
docs/reviews/r2/pr194-codereview-codex-5-5.md:2423:+    ///         5. Increment tick and publish the next tick seed atomically.
docs/reviews/r2/pr194-codereview-codex-5-5.md:2463:-        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
docs/reviews/r2/pr194-codereview-codex-5-5.md:6804:   352	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/r2/pr194-codereview-codex-5-5.md:6810:   358	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/r2/pr194-codereview-codex-5-5.md:7972:  2225	    ///         5. Increment tick and publish the next tick seed atomically.
docs/reviews/r2/pr194-codereview-codex-5-5.md:8424:  3097	    // OTC TRANSFERS
docs/reviews/r2/pr194-codereview-codex-5-5.md:8428:  3101	        revert("OTC transfers not implemented");
docs/reviews/r2/pr194-codereview-codex-5-5.md:8432:  3105	        revert("OTC transfers not implemented");
docs/reviews/r2/pr194-codereview-codex-5-5.md:8436:  3109	        revert("OTC transfers not implemented");
docs/reviews/r2/pr194-codereview-codex-5-5.md:8444:  3117	        revert("OTC transfers not implemented");
docs/reviews/r2/pr194-codereview-codex-5-5.md:10013:   691	    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/r2/pr194-codereview-codex-5-5.md:10611:`packages/contracts/src/ClanWorld.sol:551` — target-death cleanup misses en-route defenders. `_releaseDefendersForDeadTarget` requires `_clansmanDefendingRegion[clansmanId] == baseRegion`, but traveling defenders are not registered until arrival, so their `DefendBase` mission can survive a target death and later register against a dead clan. Clear all active `DefendBase` missions with `mission.targetClanId == deadClanId`, registered or not.
docs/reviews/r2/pr194-codereview-codex-5-5.md:10653:`packages/contracts/src/ClanWorld.sol:551` — target-death cleanup misses en-route defenders. `_releaseDefendersForDeadTarget` requires `_clansmanDefendingRegion[clansmanId] == baseRegion`, but traveling defenders are not registered until arrival, so their `DefendBase` mission can survive a target death and later register against a dead clan. Clear all active `DefendBase` missions with `mission.targetClanId == deadClanId`, registered or not.
docs/reviews/pr198-codereview-codex-5-4.md:1648:     // OTC TRANSFERS
docs/reviews/pr198-codereview-codex-5-4.md:4684:   463	            // Persistent mission. Registration happens atomically at order submission.
docs/reviews/pr198-codereview-codex-5-4.md:5713:  2115	    // OTC TRANSFERS
docs/reviews/pr198-codereview-codex-5-4.md:5717:  2119	        revert("OTC transfers not implemented");
docs/reviews/pr198-codereview-codex-5-4.md:5721:  2123	        revert("OTC transfers not implemented");
docs/reviews/pr198-codereview-codex-5-4.md:5725:  2127	        revert("OTC transfers not implemented");
docs/reviews/pr198-codereview-codex-5-4.md:5733:  2135	        revert("OTC transfers not implemented");
docs/reviews/pr198-codereview-codex-5-4.md:6737:   242	    // test_heartbeat_atomicTickSeedPublish
docs/reviews/pr198-codereview-codex-5-4.md:6742:   247	    function test_heartbeat_atomicTickSeedPublish() public {
docs/reviews/pr198-codereview-codex-5-4.md:7390:docs/reviews/r2/pr194-codereview-opus-4-7.md:19:`ClanWorld.sol:~2210-2230` documents the heartbeat as 5 steps ("1. Settle… 2. Execute markets… 3. Eager-settle… 4. Advance bandit timers and resolve closed-tick bandit/world events… 5. Increment tick and publish the next tick seed atomically"), but the body has 8 inline-numbered steps (`Step 1`…`Step 8`), bandit spawn evaluation is its own step, and `_resolveWorldEvents` runs at step 7 not step 4. Stale natspec → reviewer/operator confusion. Trivial fix.
docs/reviews/pr198-codereview-codex-5-4.md:7972:docs/reviews/pr137-review-claude-opus.md:57:| 6 | `src/ClanWorld.sol` | 1378–1394 | **OTC transfer functions still `revert("... Phase 2")`.** The string "Phase 2" is misleading now that Phase 2 market economy is implemented. Should clarify these are OTC-specific stubs. Agent 7. | **SHOULD FIX** |
docs/reviews/pr198-codereview-codex-5-4.md:7977:docs/reviews/pr137-review-claude-opus.md:153:The core AMM math is **correct and economically sound** — constant-product formulas, rounding, K-invariant all verified. The try/catch external self-call pattern for atomic market execution is **well-designed**. Test coverage is **adequate for hackathon standards**. Gold and resource conservation **hold**.
docs/reviews/pr198-codereview-codex-5-4.md:8800:   904	        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
docs/reviews/pr198-codereview-codex-5-4.md:9328:- OTC clan-to-clan resource transfers
docs/reviews/pr198-codereview-codex-5-4.md:9338:- OTC transfers of abstract assets
docs/reviews/pr198-codereview-codex-5-4.md:9397:### 3.8 OTC model
docs/reviews/pr198-codereview-codex-5-4.md:9398:OTC transfers are:
docs/reviews/pr198-codereview-codex-5-4.md:9450:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1201:- dead clans are eliminated and remaining vault resources are burned
docs/reviews/pr198-codereview-codex-5-4.md:10444:   309	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr198-codereview-codex-5-4.md:10450:   315	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr198-codereview-codex-5-4.md:10927:2026-04-29T18:06:47.200156Z ERROR codex_core::session: failed to record rollout items: thread 019dda66-8104-7693-bd3e-96fac497ee98 not found
docs/reviews/pr198-codereview-codex-5-4.md:10928:2026-04-29T18:06:47.200615Z ERROR codex_core::session: failed to record rollout items: thread 019dda65-bc9d-74b1-898f-44824da0a158 not found
docs/reviews/pr198-codereview-codex-5-4.md:10929:2026-04-29T18:06:47.200168Z ERROR codex_core::session: failed to record rollout items: thread 019dda66-8093-77c1-ba98-e55cc34b8ffa not found
docs/reviews/r2/pr194-codereview-codex-5-4.md:2423:+    ///         5. Increment tick and publish the next tick seed atomically.
docs/reviews/r2/pr194-codereview-codex-5-4.md:2463:-        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
docs/reviews/r2/pr194-codereview-codex-5-4.md:6086:    // OTC TRANSFERS — Phase 2 stubs
docs/reviews/r2/pr194-codereview-codex-5-4.md:6090:        revert("ClanWorld: OTC transfers available in Phase 2");
docs/reviews/r2/pr194-codereview-codex-5-4.md:6094:        revert("ClanWorld: OTC transfers available in Phase 2");
docs/reviews/r2/pr194-codereview-codex-5-4.md:6098:        revert("ClanWorld: OTC transfers available in Phase 2");
docs/reviews/r2/pr194-codereview-codex-5-4.md:6106:        revert("ClanWorld: OTC transfers available in Phase 2");
docs/reviews/r2/pr194-codereview-codex-5-4.md:6856:    // ----- OTC transfers -----
docs/reviews/r2/pr194-codereview-codex-5-4.md:6923:    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/r2/pr194-codereview-codex-5-4.md:7376:   352	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/r2/pr194-codereview-codex-5-4.md:7382:   358	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/r2/pr194-codereview-codex-5-4.md:7658:   629	            // Persistent mission. Registration happens atomically at order submission.
docs/reviews/r2/pr194-codereview-codex-5-4.md:9264:  2225	    ///         5. Increment tick and publish the next tick seed atomically.
docs/reviews/r2/pr194-codereview-codex-5-4.md:10141:  3097	    // OTC TRANSFERS
docs/reviews/r2/pr194-codereview-codex-5-4.md:10145:  3101	        revert("OTC transfers not implemented");
docs/reviews/r2/pr194-codereview-codex-5-4.md:10149:  3105	        revert("OTC transfers not implemented");
docs/reviews/r2/pr194-codereview-codex-5-4.md:10153:  3109	        revert("OTC transfers not implemented");
docs/reviews/r2/pr194-codereview-codex-5-4.md:10161:  3117	        revert("OTC transfers not implemented");
docs/reviews/r2/pr194-codereview-codex-5-4.md:11014:   633	    // ----- OTC transfers -----
docs/reviews/r2/pr194-codereview-codex-5-4.md:11072:   691	    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/r2/pr194-codereview-codex-5-4.md:13787:   333	    // test_heartbeat_atomicTickSeedPublish
docs/reviews/r2/pr194-codereview-codex-5-4.md:13792:   338	    function test_heartbeat_atomicTickSeedPublish() public {
docs/reviews/r2/pr194-synthesis.md:60:5. **Defender registry invariants are clean.** All three registries (`_defendingClansByRegion`, `_defenderCountByRegionClan`, `_clansmanDefendingRegion`) update atomically. New cleanup paths properly hit `_clearDefender`. No leaks.
docs/reviews/r2/pr194-codereview-opus-4-7.md:19:`ClanWorld.sol:~2210-2230` documents the heartbeat as 5 steps ("1. Settle… 2. Execute markets… 3. Eager-settle… 4. Advance bandit timers and resolve closed-tick bandit/world events… 5. Increment tick and publish the next tick seed atomically"), but the body has 8 inline-numbered steps (`Step 1`…`Step 8`), bandit spawn evaluation is its own step, and `_resolveWorldEvents` runs at step 7 not step 4. Stale natspec → reviewer/operator confusion. Trivial fix.
docs/reviews/r2/pr194-codereview-opus-4-7.md:71:6. **Defender registry invariants are clean** (Agent 3 confirmed). All three registries (`_defendingClansByRegion`, `_defenderCountByRegionClan`, `_clansmanDefendingRegion`) are updated atomically through `_registerDefender` and `_clearDefender`. New paths (`_markClansmanDead`, `_markClanDead`, `_releaseDefendersForDeadTarget`) all hit `_clearDefender` correctly. No leak surface found.
docs/reviews/r2/pr194-codereview-gemini-3-1-pro.md:114:  Symbol(gaxios-gaxios-error): '6.7.1'
docs/reviews/r2/pr194-codereview-gemini-3-1-pro.md:229:  Symbol(gaxios-gaxios-error): '6.7.1'
docs/reviews/r2/pr194-codereview-gemini-3-1-pro.md:344:  Symbol(gaxios-gaxios-error): '6.7.1'
docs/reviews/r2/pr194-codereview-gemini-3-1-pro.md:459:  Symbol(gaxios-gaxios-error): '6.7.1'
docs/reviews/pr200-codereview-codex-5-4.md:17:Senior staff engineer FINAL pre-merge review for PR #200 (Phase 7 — OTC Transfer Surface) at head 0c20b46.
docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr200-codereview-codex-5-4.md:23:1. CROSS-CUTTING bugs (proposal lifecycle integrity, atomic accept-all-or-nothing, state machine consistency)
docs/reviews/pr200-codereview-codex-5-4.md:24:2. Spec compliance (§7.1-7.5)
docs/reviews/pr200-codereview-codex-5-4.md:28:6. Test coverage on the atomic-failure paths
docs/reviews/pr200-codereview-codex-5-4.md:1871:     // OTC TRANSFERS
docs/reviews/pr200-codereview-codex-5-4.md:2103:+        require(!_isEmptyBundledTransfer(gold, wood, wheat, fish, iron, blueprint), "ClanWorld: empty bundled transfer");
docs/reviews/pr200-codereview-codex-5-4.md:2209:         revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:2323:     // OTC transfers
docs/reviews/pr200-codereview-codex-5-4.md:2557:     // ----- OTC transfers -----
docs/reviews/pr200-codereview-codex-5-4.md:2638:     // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/pr200-codereview-codex-5-4.md:3281:+        vm.expectRevert("ClanWorld: empty bundled transfer");
docs/reviews/pr200-codereview-codex-5-4.md:4636:Reviewing the Phase 7 diff as a cohesive pre-merge pass. I’m going to split this into parallel reads: one track on OTC lifecycle/security/accounting in the contract, and one on spec/tests/dead-clan coverage so we can converge on cross-cutting findings faster.
docs/reviews/pr200-codereview-codex-5-4.md:4637:  → Gather contract/spec context for Phase 7 OTC transfer surface and related invariants
docs/reviews/pr200-codereview-codex-5-4.md:4638:  • Parallelize focused review across OTC logic and test/spec coverage
docs/planning/phase-3-test-spec.md:5:**Spec references:** v4.2 §2.4, §7.7, §7.9-7.11, §8.2, §11.1, §15; dispatch brief `plans/drafts/20260426-phase3-dispatch-brief.md`
docs/planning/phase-3-test-spec.md:291:**Assert:** Array is empty after resolution. (v4.2 §7.11 — entries cleared after distribution.)
docs/planning/clanworld_v4_3_schema_patch.md:298:## M. Dead Clan OTC Restriction (explicit restatement)
docs/planning/clanworld_v4_3_schema_patch.md:301:All OTC transfer entrypoints must require sender clan state == `ACTIVE`:
docs/planning/clanworld_v4_3_schema_patch.md:354:- dead clans blocked from OTC transfers
docs/reviews/pr198-codereview-codex-5-5.md:1648:     // OTC TRANSFERS
docs/reviews/pr198-codereview-codex-5-5.md:4541:   633	    // ----- OTC transfers -----
docs/reviews/pr198-codereview-codex-5-5.md:4599:   691	    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/pr198-codereview-codex-5-5.md:5721:docs/planning/clanworld_v4_spec.md:1130:- purse gold remains bound to the dead clan and is unusable for the remainder of the season
docs/reviews/pr198-codereview-codex-5-5.md:6003:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1130:- purse gold remains bound to the dead clan and is unusable for the remainder of the season
docs/reviews/pr198-codereview-codex-5-5.md:7107:  2115	    // OTC TRANSFERS
docs/reviews/pr198-codereview-codex-5-5.md:7111:  2119	        revert("OTC transfers not implemented");
docs/reviews/pr198-codereview-codex-5-5.md:7115:  2123	        revert("OTC transfers not implemented");
docs/reviews/pr198-codereview-codex-5-5.md:7119:  2127	        revert("OTC transfers not implemented");
docs/reviews/pr198-codereview-codex-5-5.md:7127:  2135	        revert("OTC transfers not implemented");
docs/reviews/pr198-codereview-codex-5-5.md:7261:   309	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr198-codereview-codex-5-5.md:7267:   315	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr198-codereview-codex-5-5.md:7415:   463	            // Persistent mission. Registration happens atomically at order submission.
docs/reviews/pr198-codereview-codex-5-5.md:9265:   904	        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
docs/planning/V1/07 clanworld_v4_5_alignment_addendum.md:303:### 7.1 §1 Stream Contracts updates
docs/planning/V1/07 clanworld_v4_5_alignment_addendum.md:320:### 7.2 §2 Open Questions updates
docs/planning/V1/07 clanworld_v4_5_alignment_addendum.md:330:### 7.3 §3 Dependency Graph updates
docs/planning/V1/07 clanworld_v4_5_alignment_addendum.md:352:### 7.4 §7 Risk Register updates
docs/reviews/pr137-review-claude-opus.md:57:| 6 | `src/ClanWorld.sol` | 1378–1394 | **OTC transfer functions still `revert("... Phase 2")`.** The string "Phase 2" is misleading now that Phase 2 market economy is implemented. Should clarify these are OTC-specific stubs. Agent 7. | **SHOULD FIX** |
docs/reviews/pr137-review-claude-opus.md:134:5. **#5, #6 Stale comments** — Quick fix: update NatSpec and OTC revert strings
docs/reviews/pr137-review-claude-opus.md:153:The core AMM math is **correct and economically sound** — constant-product formulas, rounding, K-invariant all verified. The try/catch external self-call pattern for atomic market execution is **well-designed**. Test coverage is **adequate for hackathon standards**. Gold and resource conservation **hold**.
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:546:- Source text comes from `agentLogs` Convex query. The agent runner sets `triggeringClansmanId` on each log; if set, the bubble appears over that clansman. If unset (e.g., for `do_nothing` or `send_whisper` decisions), the bubble appears over the clan flag at the homebase. See agent runner spec §7.1 for the attribution rules.
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:569:### 7.1 Tick metadata
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:581:### 7.2 Required queries
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:612:### 7.3 Subscription patterns
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:626:### 7.4 EventLogEntry shape
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:857:- OTC asset transfer UI (whispers chat is sufficient)
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:307:### 7.1 Tool definitions
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:394:### 7.2 Why this surface and not more
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:398:### 7.3 Tool call execution
docs/reviews/pr194-r2-codereview-codex-5-4.md:2480:+    ///         5. Increment tick and publish the next tick seed atomically.
docs/reviews/pr194-r2-codereview-codex-5-4.md:2520:-        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
docs/reviews/pr194-r2-codereview-codex-5-4.md:4779:     "convex": "1.17.4",
docs/reviews/pr194-r2-codereview-codex-5-4.md:5449:    // ----- OTC transfers -----
docs/reviews/pr194-r2-codereview-codex-5-4.md:7523:   643	            // Persistent mission. Registration happens atomically at order submission.
docs/reviews/pr194-r2-codereview-codex-5-4.md:7655:   354	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr194-r2-codereview-codex-5-4.md:7661:   360	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr194-r2-codereview-codex-5-4.md:7924:    "convex": "1.17.4",
docs/reviews/pr194-r2-codereview-codex-5-4.md:8138:  2304	    ///         5. Increment tick and publish the next tick seed atomically.
docs/reviews/pr153-review-claude-opus.md:46:| 9 | `contracts/src/ClanWorld.sol` | 1417–1437 | **OTC revert strings say "Phase 2".** Misleading since Phase 2 economy is now live; OTC is a separate unimplemented feature. PR #137 #6 — NOT FIXED. | Docs |
docs/reviews/pr153-review-claude-opus.md:158:| PR #137 #6 (SHOULD FIX) | OTC revert strings | **Not fixed** |
docs/reviews/pr153-review-claude-opus.md:201:- #9: Change OTC revert strings to "OTC transfers not implemented"
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:610:| 6 | Cross-chain ownership transfer ergonomics | Demo polish | Step 7 | Script as bundled tx, not atomic |
docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:169:{ tick: 186, type: ‘OTC’, clan: ‘ALPHA’, counter: ‘GAMMA’, resource: ‘WHEAT’, amount: 8, gold: 5.6 },
docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:172:{ tick: 180, type: ‘OTC’, clan: ‘ALPHA’, counter: ‘BETA’, resource: ‘GOLD’, amount: 5 },
docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:175:{ tick: 174, type: ‘OTC’, clan: ‘DELTA’, counter: ‘GAMMA’, resource: ‘BLUEPRINT’, amount: 1 },
docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:179:{ tick: 163, type: ‘OTC’, clan: ‘GAMMA’, counter: ‘DELTA’, resource: ‘WHEAT’, amount: 6 },
docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:187:{ tick: 174, severity: ‘HIGH’, text: ‘BLUEPRINT FRAGMENT moved DELTA → GAMMA via OTC’ },
docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:1030:}}>{t.type === ‘AMM’ ? ‘⌬ UNISWAP’ : ‘✦ OTC’}</span>
docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:1589:        <LabelValue label="OTC TRANSFERS" value="34" color={T.gamma} />
docs/planning/V1/05 0G/clanworld_clan_memory_spec.md:153:Best-effort sequential batch write. **No atomicity guarantee** — 0G KV does not support transactional batches at the protocol level, so partial state is possible if a failure occurs mid-batch.
docs/planning/V1/05 0G/clanworld_clan_memory_spec.md:490:If both processes ever wrote to the same key, last-write-wins applies (no atomicity, no merge). In practice they never do — the conventions prevent it.
docs/planning/V1/05 0G/clanworld_clan_memory_spec.md:603:- ✅ `writeMany` is best-effort sequential, not atomic; returns success/failure breakdown
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:207:- OTC clan-to-clan resource transfers
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:217:- OTC transfers of abstract assets
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:225:- OTC transfers
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:327:### 7.1 WorldState
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:350:### 7.2 TreasuryState
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:372:### 7.3 Clan
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:403:### 7.4 WheatPlot
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:421:### 7.5 Clansman
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:523:### 7.10 Active defender registries
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:537:### 7.11 Defense contribution storage
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:680:OTC transfers may draw only from:
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:685:OTC transfers may **not** draw from worker carry balances.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:931:- a dead clansman cannot receive orders
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:932:- a dead clan cannot receive new workers or orders
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:939:- OTC transfers cannot draw from worker carry balances
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:997:- explicit OTC transfer functions
docs/planning/clanworld_numbered_implementation_plan.md:127:- dead clan restrictions on OTC
docs/planning/clanworld_numbered_implementation_plan.md:244:- worker carry -> vault transfer
docs/planning/clanworld_numbered_implementation_plan.md:311:## Phase 7 — OTC transfer surface
docs/planning/clanworld_numbered_implementation_plan.md:313:### 7.1 Implement gold transfer
docs/planning/clanworld_numbered_implementation_plan.md:316:### 7.2 Implement vault resource transfer
docs/planning/clanworld_numbered_implementation_plan.md:319:### 7.3 Implement blueprint transfer
docs/planning/clanworld_numbered_implementation_plan.md:322:### 7.4 Implement bundled transfer convenience
docs/planning/clanworld_numbered_implementation_plan.md:326:### 7.5 Restrict dead clans
docs/planning/clanworld_numbered_implementation_plan.md:438:- preserve dead-clan restrictions
docs/planning/clanworld_numbered_implementation_plan.md:492:- OTC transfer calls
docs/planning/clanworld_numbered_implementation_plan.md:531:- OTC transfer surface
docs/planning/clanworld_numbered_implementation_plan.md:563:11. OTC  
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:298:## M. Dead Clan OTC Restriction (explicit restatement)
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:301:All OTC transfer entrypoints must require sender clan state == `ACTIVE`:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:354:- dead clans blocked from OTC transfers
docs/planning/clanworld_v1_implementation_profile.md:28:- trustless OTC escrow
docs/planning/clanworld_v1_implementation_profile.md:114:### 3.8 OTC model
docs/planning/clanworld_v1_implementation_profile.md:115:OTC transfers are:
docs/planning/clanworld_v1_implementation_profile.md:129:- dead clan cannot send OTC transfers
docs/planning/clanworld_v1_implementation_profile.md:238:- advanced OTC convenience batching
docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:634:    // ----- OTC transfers -----
docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:696:    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/planning/clanworld_v4_5_alignment_addendum.md:303:### 7.1 §1 Stream Contracts updates
docs/planning/clanworld_v4_5_alignment_addendum.md:320:### 7.2 §2 Open Questions updates
docs/planning/clanworld_v4_5_alignment_addendum.md:330:### 7.3 §3 Dependency Graph updates
docs/planning/clanworld_v4_5_alignment_addendum.md:352:### 7.4 §7 Risk Register updates
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:348:OTC negotiation happens off-chain / via AXL, and actual asset transfer happens directly at token/account level, not by worker courier simulation.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:825:## 7.1 Winter cadence
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:828:## 7.2 Winter duration
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:831:## 7.3 Winter upkeep
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:837:## 7.4 Winter farming rule
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:848:## 7.5 Winter gathering outside farming
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1050:# 11. OTC Trust Model
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1053:OTC deals, mercenary promises, alliances, threats, betrayals, and cooperative pacts are intentionally **non-atomic** in v1.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1130:- purse gold remains bound to the dead clan and is unusable for the remainder of the season
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1131:- Blueprint Fragment balance remains bound to the dead clan and is unusable for the remainder of the season
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1163:- a dead clansman cannot receive orders
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1200:- OTC trust is social, not escrowed
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1201:- dead clans are eliminated and remaining vault resources are burned
docs/planning/clanworld_v4_spec.md:348:OTC negotiation happens off-chain / via AXL, and actual asset transfer happens directly at token/account level, not by worker courier simulation.
docs/planning/clanworld_v4_spec.md:825:## 7.1 Winter cadence
docs/planning/clanworld_v4_spec.md:828:## 7.2 Winter duration
docs/planning/clanworld_v4_spec.md:831:## 7.3 Winter upkeep
docs/planning/clanworld_v4_spec.md:837:## 7.4 Winter farming rule
docs/planning/clanworld_v4_spec.md:848:## 7.5 Winter gathering outside farming
docs/planning/clanworld_v4_spec.md:1050:# 11. OTC Trust Model
docs/planning/clanworld_v4_spec.md:1053:OTC deals, mercenary promises, alliances, threats, betrayals, and cooperative pacts are intentionally **non-atomic** in v1.
docs/planning/clanworld_v4_spec.md:1130:- purse gold remains bound to the dead clan and is unusable for the remainder of the season
docs/planning/clanworld_v4_spec.md:1131:- Blueprint Fragment balance remains bound to the dead clan and is unusable for the remainder of the season
docs/planning/clanworld_v4_spec.md:1163:- a dead clansman cannot receive orders
docs/planning/clanworld_v4_spec.md:1200:- OTC trust is social, not escrowed
docs/planning/clanworld_v4_spec.md:1201:- dead clans are eliminated and remaining vault resources are burned
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:28:- trustless OTC escrow
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:114:### 3.8 OTC model
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:115:OTC transfers are:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:129:- dead clan cannot send OTC transfers
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:238:- advanced OTC convenience batching
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:207:- OTC clan-to-clan resource transfers
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:217:- OTC transfers of abstract assets
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:225:- OTC transfers
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:327:### 7.1 WorldState
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:350:### 7.2 TreasuryState
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:372:### 7.3 Clan
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:403:### 7.4 WheatPlot
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:421:### 7.5 Clansman
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:523:### 7.10 Active defender registries
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:537:### 7.11 Defense contribution storage
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:680:OTC transfers may draw only from:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:685:OTC transfers may **not** draw from worker carry balances.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:931:- a dead clansman cannot receive orders
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:932:- a dead clan cannot receive new workers or orders
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:939:- OTC transfers cannot draw from worker carry balances
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:997:- explicit OTC transfer functions
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:127:- dead clan restrictions on OTC
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:244:- worker carry -> vault transfer
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:311:## Phase 7 — OTC transfer surface
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:313:### 7.1 Implement gold transfer
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:316:### 7.2 Implement vault resource transfer
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:319:### 7.3 Implement blueprint transfer
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:322:### 7.4 Implement bundled transfer convenience
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:326:### 7.5 Restrict dead clans
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:438:- preserve dead-clan restrictions
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:492:- OTC transfer calls
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:531:- OTC transfer surface
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:563:11. OTC  
packages/contracts/src/ClanWorld.sol:309:        // Path 6: dead clansman — invalidate active mission if any
packages/contracts/src/ClanWorld.sol:315:                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
packages/contracts/src/ClanWorld.sol:463:            // Persistent mission. Registration happens atomically at order submission.
packages/contracts/src/ClanWorld.sol:904:        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
packages/contracts/src/ClanWorld.sol:2115:    // OTC TRANSFERS
packages/contracts/src/ClanWorld.sol:2119:        revert("OTC transfers not implemented");
packages/contracts/src/ClanWorld.sol:2123:        revert("OTC transfers not implemented");
packages/contracts/src/ClanWorld.sol:2127:        revert("OTC transfers not implemented");
packages/contracts/src/ClanWorld.sol:2135:        revert("OTC transfers not implemented");
packages/contracts/src/IClanWorld.sol:629:    // ----- OTC transfers -----
packages/contracts/src/IClanWorld.sol:687:    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
packages/contracts/src/ClanWorldStub.sol:117:    // OTC transfers
packages/contracts/test/HeartbeatOrdering.t.sol:242:    // test_heartbeat_atomicTickSeedPublish
packages/contracts/test/HeartbeatOrdering.t.sol:247:    function test_heartbeat_atomicTickSeedPublish() public {
packages/contracts/test/ClanWorld.t.sol:1699:    // 3.E3: ERR_CLANSMAN_DEAD — dead clansman is rejected
packages/contracts/test/ClanWorld.t.sol:1727:        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CLANSMAN_DEAD), "3.E3: dead clansman must be rejected");
packages/contracts/test/ClanWorld.t.sol:2173:    // Path 6: dead clansman mid-mission → mission invalidated

2026-04-29T22:23:37.340998Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
2026-04-29T22:23:37.341228Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
collab: SpawnAgent
codex
I’ve got the diff prompt and I’m pulling in the surrounding contract/spec context now. The local worktree isn’t on the PR head, so I’m treating the stdin diff as the source of truth and using the repo only to understand unchanged behavior around the touched seams.
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
/bin/bash -lc "sed -n '311,335p' docs/planning/clanworld_numbered_implementation_plan.md" in /home/claude/code/clan-world
 succeeded in 0ms:
## Phase 7 — OTC transfer surface

### 7.1 Implement gold transfer
- clan purse -> clan purse

### 7.2 Implement vault resource transfer
- clan vault -> clan vault

### 7.3 Implement blueprint transfer
- clan blueprint -> clan blueprint

### 7.4 Implement bundled transfer convenience
Optional:
- one call for mixed payment

### 7.5 Restrict dead clans
Dead clans cannot initiate transfers.

**Definition of done**
- mercenary / alliance payments have a real contract surface

**Cut line:** bundle transfer can be cut first.

---


exec
/bin/bash -lc "sed -n '660,710p' docs/planning/clanworld_v4_2_state_schema_interface_spec.md" in /home/claude/code/clan-world
 succeeded in 0ms:
### 10.3 OTC transfer surface

Internal accounting requires explicit transfer functions.

```solidity
function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
function transferBundle(
    uint32 fromClanId,
    uint32 toClanId,
    uint256 gold,
    uint256 blueprint,
    uint256 wood,
    uint256 iron,
    uint256 wheat,
    uint256 fish
) external;
```

OTC transfers may draw only from:
- clan vault resources
- clan gold purse
- clan blueprint balance

OTC transfers may **not** draw from worker carry balances.

### 10.4 ClanOrder

```solidity
struct ClanOrder {
    uint32 clansmanId;
    uint8 gotoRegion;
    ActionType action;

    uint32 targetClanId;
    address marketToken;
    uint256 marketAmount;
    uint256 maxGoldIn;
}
```

### 10.5 OrderResult

```solidity
struct OrderResult {
    uint32 clansmanId;
    StatusCode status;
    uint64 cooldownEndsAtTs;
    uint64 missionNonce;
}

collab: SpawnAgent
collab: SpawnAgent
codex
The spec slice is loaded: Phase 7 only promised the transfer surface plus dead-clan blocking on initiation, while the deeper state-schema doc still describes direct transfer entrypoints. I’m now checking whether the proposal-based implementation preserves those guarantees and whether the new token/pool changes accidentally widened the blast radius.
exec
/bin/bash -lc "sed -n '280,340p' docs/planning/V1/01\\ Blockchain\\ Game\\ Spec/clanworld_v4_3_schema_patch.md" in /home/claude/code/clan-world
 succeeded in 0ms:

### L.1 Canonical recommendation
If both:
- `starvationStartsAtTick`
- `starvingCached`

exist, remove `starvingCached` in favor of deriving starvation from canonical state.

### L.2 Preferred model
Use:
- settlement checkpoint data
- `starvationStartsAtTick`
- current tick context

rather than redundant cached boolean state when avoidable.

---

## M. Dead Clan OTC Restriction (explicit restatement)

### M.1 Dead sender restriction
All OTC transfer entrypoints must require sender clan state == `ACTIVE`:

- `transferGold`
- `transferVaultResource`
- `transferBlueprint`
- `transferBundle`

Dead clans may not initiate outbound value transfer.

---

## N. Implementation Notes (Non-blocking)

### N.1 Heartbeat gas exposure
Heartbeat remains permissionless in v1.

Worst-case heartbeat cost includes:
- scheduled market execution
- eager settlement of touched clans
- world event resolution
- winter transition checks
- season end checks

This is acceptable for the intended v1 realm size (~4–12 clans). Larger scales require sharding/batching and are out of scope for v1.

### N.2 `quoteTravel(src, src)`
For completeness:
- `quoteTravel(src, src)` returns `travelTicks = 0`
- path is effectively `[src]`
- actual same-region mission execution bypasses travel state per Section A above

### N.3 Path packing constraint
Any packed path encoding such as `bytes8` is valid only while maximum route length remains within that representation's capacity. Future map expansion must revisit this constraint.

---

## O. Locked summary

This patch locks the following:


exec
/bin/bash -lc "rg -n \"mintClan\\(|goldBalance|blueprintBalance|clanState = ClanState.DEAD|clanState == ClanState.DEAD|_poolForResourceType|seedPools\\(|transferGold\\(|transferVaultResource\\(|transferBlueprint\\(|transferBundle\\(|settleClansman\\(|DepositResources|CLANSMAN_CARRY_CAP|WOOD_YIELD_PER_TICK|ResourceMinted|ResourceBurned\" packages/contracts/src packages/contracts/test" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/test/SeedPools.t.sol:51:        world.seedPools(_seedConfig());
packages/contracts/test/MissionTiming.t.sol:37:    function _mintClan() internal returns (uint32 clanId) {
packages/contracts/test/MissionTiming.t.sol:39:        (clanId,) = world.mintClan(elder);
packages/contracts/test/MissionTiming.t.sol:66:        uint32 clanId = _mintClan();
packages/contracts/test/MissionTiming.t.sol:92:        uint32 clanId = _mintClan();
packages/contracts/test/MissionTiming.t.sol:128:        assertEq(world.getActionDuration(ActionType.DepositResources), 1, "deposit");
packages/contracts/test/MissionTiming.t.sol:151:        uint32 clanId = _mintClan();
packages/contracts/test/Gathering.t.sol:43:    function _mintClan() internal returns (uint32 clanId) {
packages/contracts/test/Gathering.t.sol:45:        (clanId,) = world.mintClan(elder);
packages/contracts/test/Gathering.t.sol:81:        uint32 clanId = _mintClan();
packages/contracts/test/Gathering.t.sol:86:        uint256 expected = ClanWorldConstants.WOOD_YIELD_PER_TICK * world.getActionDuration(ActionType.ChopWood);
packages/contracts/test/Gathering.t.sol:91:        uint256 baseYield = ClanWorldConstants.WOOD_YIELD_PER_TICK * world.getActionDuration(ActionType.ChopWood);
packages/contracts/test/Gathering.t.sol:99:            uint32 clanId = _mintClan();
packages/contracts/test/Gathering.t.sol:115:        uint32 clanId = _mintClan();
packages/contracts/test/Gathering.t.sol:125:        uint32 clanId = _mintClan();
packages/contracts/test/HeartbeatOrdering.t.sol:74:    function _mintClan() internal returns (uint32 clanId) {
packages/contracts/test/HeartbeatOrdering.t.sol:76:        (clanId,) = world.mintClan(elder);
packages/contracts/test/HeartbeatOrdering.t.sol:178:        world.seedPools(cfg);
packages/contracts/test/HeartbeatOrdering.t.sol:197:        uint32 clanId = _mintClan();
packages/contracts/test/HeartbeatOrdering.t.sol:209:            _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
packages/contracts/test/HeartbeatOrdering.t.sol:227:        uint256 goldBefore = world.getClan(clanId).goldBalance;
packages/contracts/test/HeartbeatOrdering.t.sol:235:        uint256 goldAfter = world.getClan(clanId).goldBalance;
packages/contracts/test/HeartbeatOrdering.t.sol:327:        uint32 clanId = _mintClan();
packages/contracts/test/HeartbeatOrdering.t.sol:338:            _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
packages/contracts/test/HeartbeatOrdering.t.sol:349:        uint256 goldBefore = world.getClan(clanId).goldBalance;
packages/contracts/test/HeartbeatOrdering.t.sol:356:        assertGt(world.getClan(clanId).goldBalance, goldBefore, "sell executed: gold increased");
packages/contracts/test/Reentrancy.t.sol:118:        uint32 clanId = _mintClan();
packages/contracts/test/Reentrancy.t.sol:121:        uint256 goldBefore = world.getClan(clanId).goldBalance;
packages/contracts/test/Reentrancy.t.sol:135:        assertGt(world.getClan(clanId).goldBalance, goldBefore, "heartbeat should finish original market action");
packages/contracts/test/Reentrancy.t.sol:189:        world.seedPools(cfg);
packages/contracts/test/Reentrancy.t.sol:197:    function _mintClan() internal returns (uint32 clanId) {
packages/contracts/test/Reentrancy.t.sol:199:        (clanId,) = world.mintClan(elder);
packages/contracts/test/ResourceBoundaryTokens.t.sol:11:    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
packages/contracts/test/ResourceBoundaryTokens.t.sol:12:    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);
packages/contracts/test/ResourceBoundaryTokens.t.sol:68:        emit ResourceMinted(uint8(ResourceType.Wood), holder, 10e18);
packages/contracts/test/ResourceBoundaryTokens.t.sol:75:        emit ResourceBurned(uint8(ResourceType.Wood), holder, 4e18);
packages/contracts/test/ClanWorld.t.sol:144:        world.seedPools(cfg);
packages/contracts/test/ClanWorld.t.sol:158:    function _mintClan() internal returns (uint32 clanId) {
packages/contracts/test/ClanWorld.t.sol:160:        (clanId,) = world.mintClan(elder);
packages/contracts/test/ClanWorld.t.sol:270:        (uint32 clanId, uint256 iftTokenId) = world.mintClan(elder);
packages/contracts/test/ClanWorld.t.sol:281:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:293:        assertGt(clan.goldBalance, 0, "should have starting gold");
packages/contracts/test/ClanWorld.t.sol:301:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:320:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:360:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:378:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:415:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:452:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:478:        _submitOrder(clanId, cs0, homeRegion, ActionType.DepositResources);
packages/contracts/test/ClanWorld.t.sol:482:        for (uint256 i = 0; i < uint256(travelBack) + world.getActionDuration(ActionType.DepositResources) + 1; i++) {
packages/contracts/test/ClanWorld.t.sol:506:        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
packages/contracts/test/ClanWorld.t.sol:512:            mission.executesAtTick + harness.getActionDuration(ActionType.DepositResources),
packages/contracts/test/ClanWorld.t.sol:527:        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
packages/contracts/test/ClanWorld.t.sol:553:        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
packages/contracts/test/ClanWorld.t.sol:577:        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, nonHomeRegion, ActionType.DepositResources);
packages/contracts/test/ClanWorld.t.sol:588:        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
packages/contracts/test/ClanWorld.t.sol:624:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:661:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:770:        clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:801:        assertGt(afterClan.goldBalance, beforeClan.goldBalance, "gold should be credited immediately");
packages/contracts/test/ClanWorld.t.sol:814:        uint256 goldBefore = world.getClan(clanId).goldBalance;
packages/contracts/test/ClanWorld.t.sol:827:        assertEq(world.getClan(clanId).goldBalance, goldBefore, "scheduled fallback should not trade immediately");
packages/contracts/test/ClanWorld.t.sol:888:        assertLt(afterClan.goldBalance, beforeClan.goldBalance, "gold should be debited immediately");
packages/contracts/test/ClanWorld.t.sol:918:        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
packages/contracts/test/ClanWorld.t.sol:947:        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
packages/contracts/test/ClanWorld.t.sol:976:        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
packages/contracts/test/ClanWorld.t.sol:998:        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
packages/contracts/test/ClanWorld.t.sol:1005:    // Test 11: sell_creditsGold — after scheduled sell, clan.goldBalance > starter gold
packages/contracts/test/ClanWorld.t.sol:1010:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1014:        uint256 goldBefore = world.getClan(clanId).goldBalance;
packages/contracts/test/ClanWorld.t.sol:1047:        uint256 goldAfter = world.getClan(clanId).goldBalance;
packages/contracts/test/ClanWorld.t.sol:1057:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1064:        uint256 goldBefore = world.getClan(clanId).goldBalance;
packages/contracts/test/ClanWorld.t.sol:1082:        uint256 goldAfter = world.getClan(clanId).goldBalance;
packages/contracts/test/ClanWorld.t.sol:1094:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1128:        assertEq(world.getClan(clanId).goldBalance, 3e18, "gold should be unchanged after failed buy");
packages/contracts/test/ClanWorld.t.sol:1136:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1161:        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
packages/contracts/test/ClanWorld.t.sol:1171:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1185:        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "rejected buy should not debit gold");
packages/contracts/test/ClanWorld.t.sol:1193:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1219:        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
packages/contracts/test/ClanWorld.t.sol:1231:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1254:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1278:        uint256 goldBefore = world.getClan(clanId).goldBalance;
packages/contracts/test/ClanWorld.t.sol:1283:        assertEq(world.getClan(clanId).goldBalance, goldBefore, "stale sell must not execute");
packages/contracts/test/ClanWorld.t.sol:1288:        assertGt(world.getClan(clanId).goldBalance, goldBefore, "replacement sell should execute");
packages/contracts/test/ClanWorld.t.sol:1299:            uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1352:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1402:        assertGt(world.getClan(clanId).goldBalance, 3e18, "clan should gain gold from sells");
packages/contracts/test/ClanWorld.t.sol:1412:        uint32 clanId1 = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1414:        (uint32 clanId2,) = world.mintClan(elder2);
packages/contracts/test/ClanWorld.t.sol:1486:        assertGt(world.getClan(clanId1).goldBalance, 3e18, "clan1 should have more gold after sell");
packages/contracts/test/ClanWorld.t.sol:1490:        assertLt(world.getClan(clanId2).goldBalance, 3e18, "clan2 should have less gold after buy");
packages/contracts/test/ClanWorld.t.sol:1496:        uint32 clanId1 = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1498:        (uint32 clanId2,) = world.mintClan(elder2);
packages/contracts/test/ClanWorld.t.sol:1524:        uint256 sellerGoldBefore = world.getClan(clanId2).goldBalance;
packages/contracts/test/ClanWorld.t.sol:1532:        assertEq(failingAfter.goldBalance, failingBefore.goldBalance, "failed buy should not debit gold");
packages/contracts/test/ClanWorld.t.sol:1533:        assertGt(world.getClan(clanId2).goldBalance, sellerGoldBefore, "other clan sell should execute");
packages/contracts/test/ClanWorld.t.sol:1542:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1562:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1604:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1652:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1673:        uint32 clanId1 = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1676:        (uint32 clanId2,) = world.mintClan(elder2);
packages/contracts/test/ClanWorld.t.sol:1706:        (uint32 clanId,) = harness.mintClan(elder);
packages/contracts/test/ClanWorld.t.sol:1735:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1754:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1759:        // Send clansman to homebase to DepositResources (empty carry -> completes when the arrival tick closes).
packages/contracts/test/ClanWorld.t.sol:1761:        OrderResult[] memory r = _submitOrder(clanId, csId, homeRegion, ActionType.DepositResources);
packages/contracts/test/ClanWorld.t.sol:1801:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1830:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1885:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1912:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:1946:        (clanId,) = harness.mintClan(elder);
packages/contracts/test/ClanWorld.t.sol:1984:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:2002:        world.settleClansman(csId);
packages/contracts/test/ClanWorld.t.sol:2018:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:2037:        world.settleClansman(csId);
packages/contracts/test/ClanWorld.t.sol:2051:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:2078:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:2139:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:2258:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:2278:        world.settleClansman(csId);
packages/contracts/test/ClanWorld.t.sol:2292:        uint32 clanId = _mintClan();
packages/contracts/test/ClanWorld.t.sol:2298:        world.settleClansman(csId);
packages/contracts/test/ClanWorld.t.sol:2311:        uint32 clanId = _mintClan();
packages/contracts/test/DefendBase.t.sol:26:    function _mintClan() internal returns (uint32 clanId) {
packages/contracts/test/DefendBase.t.sol:28:        (clanId,) = world.mintClan(elder);
packages/contracts/test/DefendBase.t.sol:97:        uint32 clanId = _mintClan();
packages/contracts/test/DefendBase.t.sol:133:        uint32 clanId = _mintClan();
packages/contracts/test/DefendBase.t.sol:174:        uint32 clanId = _mintClan();
packages/contracts/test/DefendBase.t.sol:185:        uint32 clanId = _mintClan();
packages/contracts/test/DefendBase.t.sol:203:        uint32 clanId = _mintClan();
packages/contracts/test/DefendBase.t.sol:219:        uint32 clanId = _mintClan();
packages/contracts/test/DefendBase.t.sol:234:        uint32 firstClanId = _mintClan();
packages/contracts/test/DefendBase.t.sol:238:            seventhClanId = _mintClan();
packages/contracts/test/DefendBase.t.sol:252:        uint32 firstClanId = _mintClan();
packages/contracts/test/DefendBase.t.sol:256:            seventhClanId = _mintClan();
packages/contracts/test/DefendBase.t.sol:274:        uint32 clanId = _mintClan();
packages/contracts/src/ClanWorldStub.sol:88:    function settleClansman(uint32) external override {}
packages/contracts/src/ClanWorldStub.sol:96:    function mintClan(address) external override returns (uint32, uint256) {
packages/contracts/src/ClanWorldStub.sol:114:    function seedPools(PoolSeedConfig calldata) external override {}
packages/contracts/src/ClanWorldStub.sol:120:    function transferGold(uint32, uint32, uint256) external override {}
packages/contracts/src/ClanWorldStub.sol:122:    function transferVaultResource(uint32, uint32, ResourceType, uint256) external override {}
packages/contracts/src/ClanWorldStub.sol:124:    function transferBlueprint(uint32, uint32, uint256) external override {}
packages/contracts/src/ClanWorldStub.sol:126:    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256) external override {}
packages/contracts/src/ClanWorldStub.sol:174:            goldBalance: 0,
packages/contracts/src/ClanWorldStub.sol:175:            blueprintBalance: 0,
packages/contracts/src/MinimalERC20.sol:23:    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
packages/contracts/src/MinimalERC20.sol:24:    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);
packages/contracts/src/MinimalERC20.sol:74:        emit ResourceMinted(resourceType, to, amount);
packages/contracts/src/MinimalERC20.sol:81:        emit ResourceBurned(resourceType, from, amount);
packages/contracts/src/IClanWorld.sol:50:    uint256 internal constant WOOD_YIELD_PER_TICK = 1e18;
packages/contracts/src/IClanWorld.sol:51:    uint256 internal constant WOOD_BASE_YIELD = WOOD_YIELD_PER_TICK;
packages/contracts/src/IClanWorld.sol:52:    uint256 internal constant WOOD_CRIT_BONUS = WOOD_YIELD_PER_TICK;
packages/contracts/src/IClanWorld.sol:141:    DepositResources,
packages/contracts/src/IClanWorld.sol:250:    uint256 goldBalance;
packages/contracts/src/IClanWorld.sol:251:    uint256 blueprintBalance;
packages/contracts/src/IClanWorld.sol:590:    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
packages/contracts/src/IClanWorld.sol:591:    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);
packages/contracts/src/IClanWorld.sol:658:    function settleClansman(uint32 csId) external;
packages/contracts/src/IClanWorld.sol:668:    function mintClan(address to) external returns (uint32 clanId, uint256 iftTokenId);
packages/contracts/src/IClanWorld.sol:684:    function seedPools(PoolSeedConfig calldata cfg) external;
packages/contracts/src/IClanWorld.sol:690:    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
packages/contracts/src/IClanWorld.sol:692:    function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
packages/contracts/src/IClanWorld.sol:694:    function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
packages/contracts/src/IClanWorld.sol:696:    function transferBundle(
packages/contracts/src/ClanWorld.sol:455:        } else if (action == ActionType.DepositResources) {
packages/contracts/src/ClanWorld.sol:494:        uint256 yield = ClanWorldConstants.WOOD_YIELD_PER_TICK * uint256(getActionDuration(ActionType.ChopWood));
packages/contracts/src/ClanWorld.sol:547:            clan.goldBalance += goldBonus;
packages/contracts/src/ClanWorld.sol:842:                || clan.blueprintBalance < blueprintCost
packages/contracts/src/ClanWorld.sol:848:        clan.blueprintBalance -= blueprintCost;
packages/contracts/src/ClanWorld.sol:919:            if (clan.clanState == ClanState.DEAD) continue;
packages/contracts/src/ClanWorld.sol:988:    function settleClansman(uint32 csId) external override nonReentrant {
packages/contracts/src/ClanWorld.sol:1004:    function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
packages/contracts/src/ClanWorld.sol:1035:        clan.goldBalance = 3e18; // starter gold per v4 spec §12.5
packages/contracts/src/ClanWorld.sol:1036:        clan.blueprintBalance = 0;
packages/contracts/src/ClanWorld.sol:1670:            clan.goldBalance += goldOut;
packages/contracts/src/ClanWorld.sol:1760:        if (clan.goldBalance < goldIn) {
packages/contracts/src/ClanWorld.sol:1772:            clan.goldBalance -= actualGoldIn;
packages/contracts/src/ClanWorld.sol:1837:        clan.goldBalance += goldOut;
packages/contracts/src/ClanWorld.sol:1921:        if (clan.goldBalance < goldIn) {
packages/contracts/src/ClanWorld.sol:1946:        clan.goldBalance -= actualGoldIn;
packages/contracts/src/ClanWorld.sol:1971:        // DepositResources: must go to homebase
packages/contracts/src/ClanWorld.sol:1972:        if (action == ActionType.DepositResources) {
packages/contracts/src/ClanWorld.sol:2083:    function seedPools(PoolSeedConfig calldata cfg) external override nonReentrant {
packages/contracts/src/ClanWorld.sol:2118:    function transferGold(uint32, uint32, uint256) external pure override {
packages/contracts/src/ClanWorld.sol:2122:    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
packages/contracts/src/ClanWorld.sol:2126:    function transferBlueprint(uint32, uint32, uint256) external pure override {
packages/contracts/src/ClanWorld.sol:2130:    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
packages/contracts/src/ClanWorld.sol:2159:        return _poolForResourceType(resourceType);
packages/contracts/src/ClanWorld.sol:2163:        amountOut = StubPool(_poolForResourceType(resourceType)).getAmountOutForExactIn(amountIn);
packages/contracts/src/ClanWorld.sol:2166:    function _poolForResourceType(uint8 resourceType) internal view returns (address) {
packages/contracts/src/ClanWorld.sol:2207:        if (action == ActionType.DepositResources) {

exec
/bin/bash -lc "sed -n '1120,1140p' docs/planning/clanworld_v4_spec.md" in /home/claude/code/clan-world
 succeeded in 0ms:
While a clan is dead:
- the Elder may no longer assign missions
- the clan may not trade, build, defend, or gather
- the clan is removed from active play for the rest of the season
- the iNFT remains owned, but the clan is unusable until any future season-reset or resurrection system that may be defined later

When a clan dies in v1:
- all remaining vault resources are **burned**
- no ruin cache is created
- no salvage action exists
- purse gold remains bound to the dead clan and is unusable for the remainder of the season
- Blueprint Fragment balance remains bound to the dead clan and is unusable for the remainder of the season

---

# 13. Interfaces, Events, and Future Technical Spec Boundaries

## 13.1 Not fully specified here
This mechanics spec intentionally does **not** fully pin down:
- Solidity struct packing
- exact storage layout

exec
/bin/bash -lc "sed -n '1048,1060p' docs/planning/clanworld_v4_spec.md" in /home/claude/code/clan-world
 succeeded in 0ms:
---

# 11. OTC Trust Model

## 11.1 Social trust, not protocol escrow
OTC deals, mercenary promises, alliances, threats, betrayals, and cooperative pacts are intentionally **non-atomic** in v1.

There is no protocol-level escrow primitive for these interactions in v1.

## 11.2 Intentional gameplay consequence
The following are valid and expected behaviors:
- lying
- bluffing

exec
/bin/bash -lc "rg -n \"transferGold\\(|transferVaultResource\\(|transferBlueprint\\(|transferBundle\\(|proposeGoldTransfer\\(|acceptGoldTransfer\\(|proposeVaultTransfer\\(|proposeBlueprintTransfer\\(|proposeBundledTransfer\\(\" -g '"'!packages/contracts/abi/**'"' ." in /home/claude/code/clan-world
 succeeded in 0ms:
./docs/reviews/r2/pr194-codereview-codex-5-5.md:8427:  3100	    function transferGold(uint32, uint32, uint256) external pure override {
./docs/reviews/r2/pr194-codereview-codex-5-5.md:8431:  3104	    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
./docs/reviews/r2/pr194-codereview-codex-5-5.md:8435:  3108	    function transferBlueprint(uint32, uint32, uint256) external pure override {
./docs/reviews/r2/pr194-codereview-codex-5-5.md:8439:  3112	    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
./docs/reviews/r2/pr194-codereview-codex-5-5.md:10016:   694	    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/reviews/r2/pr194-codereview-codex-5-5.md:10018:   696	    function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
./docs/reviews/r2/pr194-codereview-codex-5-5.md:10020:   698	    function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/reviews/r2/pr194-codereview-codex-5-5.md:10022:   700	    function transferBundle(
./docs/reviews/pr200-codereview-codex-5-5.md:1874:+    function proposeGoldTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint256 expiryTick)
./docs/reviews/pr200-codereview-codex-5-5.md:1902:+    function acceptGoldTransfer(uint256 proposalId) external override nonReentrant {
./docs/reviews/pr200-codereview-codex-5-5.md:1936:+    function proposeVaultTransfer(
./docs/reviews/pr200-codereview-codex-5-5.md:2030:+    function proposeBlueprintTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint64 expiryTick)
./docs/reviews/pr200-codereview-codex-5-5.md:2086:+    function proposeBundledTransfer(
./docs/reviews/pr200-codereview-codex-5-5.md:2208:     function transferGold(uint32, uint32, uint256) external pure override {
./docs/reviews/pr200-codereview-codex-5-5.md:2326:+    function proposeGoldTransfer(uint32, uint32, uint256, uint256) external pure override returns (uint256) {
./docs/reviews/pr200-codereview-codex-5-5.md:2330:+    function acceptGoldTransfer(uint256) external override {}
./docs/reviews/pr200-codereview-codex-5-5.md:2334:+    function proposeVaultTransfer(uint32, uint32, uint256, uint256, uint256, uint256, uint64)
./docs/reviews/pr200-codereview-codex-5-5.md:2347:+    function proposeBlueprintTransfer(uint32, uint32, uint256, uint64) external pure override returns (uint256) {
./docs/reviews/pr200-codereview-codex-5-5.md:2355:+    function proposeBundledTransfer(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256, uint64)
./docs/reviews/pr200-codereview-codex-5-5.md:2368:     function transferGold(uint32, uint32, uint256) external override {}
./docs/reviews/pr200-codereview-codex-5-5.md:2370:     function transferVaultResource(uint32, uint32, ResourceType, uint256) external override {}
./docs/reviews/pr200-codereview-codex-5-5.md:2641:+    function proposeGoldTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint256 expiryTick)
./docs/reviews/pr200-codereview-codex-5-5.md:2645:+    function acceptGoldTransfer(uint256 proposalId) external;
./docs/reviews/pr200-codereview-codex-5-5.md:2649:+    function proposeVaultTransfer(
./docs/reviews/pr200-codereview-codex-5-5.md:2663:+    function proposeBlueprintTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint64 expiryTick)
./docs/reviews/pr200-codereview-codex-5-5.md:2671:+    function proposeBundledTransfer(
./docs/reviews/pr200-codereview-codex-5-5.md:2687:     function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/reviews/pr200-codereview-codex-5-5.md:2689:     function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
./docs/reviews/pr200-codereview-codex-5-5.md:3110:+        proposalId = world.proposeBlueprintTransfer(fromClanId, toClanId, amount, expiryTick);
./docs/reviews/pr200-codereview-codex-5-5.md:3325:+            world.proposeBundledTransfer(fromClanId, toClanId, gold, wood, wheat, fish, iron, blueprint, expiryTick);
./docs/reviews/pr200-codereview-codex-5-5.md:3613:+        world.acceptGoldTransfer(goldProposal);
./docs/reviews/pr200-codereview-codex-5-5.md:3658:+        world.acceptGoldTransfer(goldProposal);
./docs/reviews/pr200-codereview-codex-5-5.md:3682:+        world.acceptGoldTransfer(goldProposal);
./docs/reviews/pr200-codereview-codex-5-5.md:3727:+        world.acceptGoldTransfer(proposalId);
./docs/reviews/pr200-codereview-codex-5-5.md:3751:+        proposalId = world.proposeGoldTransfer(fromClanId, toClanId, amount, 10);
./docs/reviews/pr200-codereview-codex-5-5.md:3759:+        proposalId = world.proposeVaultTransfer(fromClanId, toClanId, amount, amount, amount, amount, 10);
./docs/reviews/pr200-codereview-codex-5-5.md:3767:+        proposalId = world.proposeBlueprintTransfer(fromClanId, toClanId, amount, 10);
./docs/reviews/pr200-codereview-codex-5-5.md:3776:+            world.proposeBundledTransfer(fromClanId, toClanId, amount, amount, amount, amount, amount, amount, 10);
./docs/reviews/pr200-codereview-codex-5-5.md:3966:+        uint256 proposalId = world.proposeGoldTransfer(clanA, clanB, amount, expiryTick);
./docs/reviews/pr200-codereview-codex-5-5.md:3989:+        world.acceptGoldTransfer(proposalId);
./docs/reviews/pr200-codereview-codex-5-5.md:4002:+        world.acceptGoldTransfer(proposalOne);
./docs/reviews/pr200-codereview-codex-5-5.md:4006:+        world.acceptGoldTransfer(proposalTwo);
./docs/reviews/pr200-codereview-codex-5-5.md:4016:+        world.acceptGoldTransfer(proposalId);
./docs/reviews/pr200-codereview-codex-5-5.md:4024:+        world.proposeGoldTransfer(clanA, clanB, 1e18, 10);
./docs/reviews/pr200-codereview-codex-5-5.md:4029:+        world.acceptGoldTransfer(proposalId);
./docs/reviews/pr200-codereview-codex-5-5.md:4044:+        world.acceptGoldTransfer(proposalId);
./docs/reviews/pr200-codereview-codex-5-5.md:4063:+        world.acceptGoldTransfer(proposalId);
./docs/reviews/pr200-codereview-codex-5-5.md:4087:+        proposalId = world.proposeGoldTransfer(fromClanId, toClanId, amount, expiryTick);
./docs/reviews/pr200-codereview-codex-5-5.md:4625:+        proposalId = world.proposeVaultTransfer(fromClanId, toClanId, wood, wheat, fish, iron, expiryTick);
./docs/reviews/pr200-codereview-codex-5-5.md:5030:packages/contracts/src/ClanWorld.sol:2118:    function transferGold(uint32, uint32, uint256) external pure override {
./docs/reviews/pr200-codereview-codex-5-5.md:5031:packages/contracts/src/IClanWorld.sol:690:    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/reviews/pr200-codereview-codex-5-5.md:5032:packages/contracts/src/ClanWorldStub.sol:120:    function transferGold(uint32, uint32, uint256) external override {}
./docs/reviews/pr200-codereview-codex-5-5.md:5225:    function transferGold(uint32, uint32, uint256) external pure override {
./docs/reviews/pr200-codereview-codex-5-5.md:5229:    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
./docs/reviews/pr200-codereview-codex-5-5.md:5233:    function transferBlueprint(uint32, uint32, uint256) external pure override {
./docs/reviews/pr200-codereview-codex-5-5.md:5237:    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
./docs/reviews/pr200-codereview-codex-5-5.md:6166:function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/reviews/pr200-codereview-codex-5-5.md:6167:function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
./docs/reviews/pr200-codereview-codex-5-5.md:6168:function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/reviews/pr200-codereview-codex-5-5.md:6169:function transferBundle(
./docs/reviews/r2/pr194-codereview-codex-5-4.md:6089:    function transferGold(uint32, uint32, uint256) external pure override {
./docs/reviews/r2/pr194-codereview-codex-5-4.md:6093:    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
./docs/reviews/r2/pr194-codereview-codex-5-4.md:6097:    function transferBlueprint(uint32, uint32, uint256) external pure override {
./docs/reviews/r2/pr194-codereview-codex-5-4.md:6101:    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
./docs/reviews/r2/pr194-codereview-codex-5-4.md:6926:    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/reviews/r2/pr194-codereview-codex-5-4.md:6928:    function transferVaultResource(
./docs/reviews/r2/pr194-codereview-codex-5-4.md:6935:    function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/reviews/r2/pr194-codereview-codex-5-4.md:6937:    function transferBundle(
./docs/reviews/r2/pr194-codereview-codex-5-4.md:10144:  3100	    function transferGold(uint32, uint32, uint256) external pure override {
./docs/reviews/r2/pr194-codereview-codex-5-4.md:10148:  3104	    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
./docs/reviews/r2/pr194-codereview-codex-5-4.md:10152:  3108	    function transferBlueprint(uint32, uint32, uint256) external pure override {
./docs/reviews/r2/pr194-codereview-codex-5-4.md:10156:  3112	    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
./docs/reviews/r2/pr194-codereview-codex-5-4.md:11075:   694	    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/reviews/r2/pr194-codereview-codex-5-4.md:11077:   696	    function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
./docs/reviews/r2/pr194-codereview-codex-5-4.md:11079:   698	    function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/reviews/r2/pr194-codereview-codex-5-4.md:11081:   700	    function transferBundle(
./docs/reviews/pr198-codereview-codex-5-4.md:5716:  2118	    function transferGold(uint32, uint32, uint256) external pure override {
./docs/reviews/pr198-codereview-codex-5-4.md:5720:  2122	    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
./docs/reviews/pr198-codereview-codex-5-4.md:5724:  2126	    function transferBlueprint(uint32, uint32, uint256) external pure override {
./docs/reviews/pr198-codereview-codex-5-4.md:5728:  2130	    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
./docs/reviews/pr200-codereview-codex-5-4.md:1874:+    function proposeGoldTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint256 expiryTick)
./docs/reviews/pr200-codereview-codex-5-4.md:1902:+    function acceptGoldTransfer(uint256 proposalId) external override nonReentrant {
./docs/reviews/pr200-codereview-codex-5-4.md:1936:+    function proposeVaultTransfer(
./docs/reviews/pr200-codereview-codex-5-4.md:2030:+    function proposeBlueprintTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint64 expiryTick)
./docs/reviews/pr200-codereview-codex-5-4.md:2086:+    function proposeBundledTransfer(
./docs/reviews/pr200-codereview-codex-5-4.md:2208:     function transferGold(uint32, uint32, uint256) external pure override {
./docs/reviews/pr200-codereview-codex-5-4.md:2326:+    function proposeGoldTransfer(uint32, uint32, uint256, uint256) external pure override returns (uint256) {
./docs/reviews/pr200-codereview-codex-5-4.md:2330:+    function acceptGoldTransfer(uint256) external override {}
./docs/reviews/pr200-codereview-codex-5-4.md:2334:+    function proposeVaultTransfer(uint32, uint32, uint256, uint256, uint256, uint256, uint64)
./docs/reviews/pr200-codereview-codex-5-4.md:2347:+    function proposeBlueprintTransfer(uint32, uint32, uint256, uint64) external pure override returns (uint256) {
./docs/reviews/pr200-codereview-codex-5-4.md:2355:+    function proposeBundledTransfer(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256, uint64)
./docs/reviews/pr200-codereview-codex-5-4.md:2368:     function transferGold(uint32, uint32, uint256) external override {}
./docs/reviews/pr200-codereview-codex-5-4.md:2370:     function transferVaultResource(uint32, uint32, ResourceType, uint256) external override {}
./docs/reviews/pr200-codereview-codex-5-4.md:2641:+    function proposeGoldTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint256 expiryTick)
./docs/reviews/pr200-codereview-codex-5-4.md:2645:+    function acceptGoldTransfer(uint256 proposalId) external;
./docs/reviews/pr200-codereview-codex-5-4.md:2649:+    function proposeVaultTransfer(
./docs/reviews/pr200-codereview-codex-5-4.md:2663:+    function proposeBlueprintTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint64 expiryTick)
./docs/reviews/pr200-codereview-codex-5-4.md:2671:+    function proposeBundledTransfer(
./docs/reviews/pr200-codereview-codex-5-4.md:2687:     function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/reviews/pr200-codereview-codex-5-4.md:2689:     function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
./docs/reviews/pr200-codereview-codex-5-4.md:3110:+        proposalId = world.proposeBlueprintTransfer(fromClanId, toClanId, amount, expiryTick);
./docs/reviews/pr200-codereview-codex-5-4.md:3325:+            world.proposeBundledTransfer(fromClanId, toClanId, gold, wood, wheat, fish, iron, blueprint, expiryTick);
./docs/reviews/pr200-codereview-codex-5-4.md:3613:+        world.acceptGoldTransfer(goldProposal);
./docs/reviews/pr200-codereview-codex-5-4.md:3658:+        world.acceptGoldTransfer(goldProposal);
./docs/reviews/pr200-codereview-codex-5-4.md:3682:+        world.acceptGoldTransfer(goldProposal);
./docs/reviews/pr200-codereview-codex-5-4.md:3727:+        world.acceptGoldTransfer(proposalId);
./docs/reviews/pr200-codereview-codex-5-4.md:3751:+        proposalId = world.proposeGoldTransfer(fromClanId, toClanId, amount, 10);
./docs/reviews/pr200-codereview-codex-5-4.md:3759:+        proposalId = world.proposeVaultTransfer(fromClanId, toClanId, amount, amount, amount, amount, 10);
./docs/reviews/pr200-codereview-codex-5-4.md:3767:+        proposalId = world.proposeBlueprintTransfer(fromClanId, toClanId, amount, 10);
./docs/reviews/pr200-codereview-codex-5-4.md:3776:+            world.proposeBundledTransfer(fromClanId, toClanId, amount, amount, amount, amount, amount, amount, 10);
./docs/reviews/pr200-codereview-codex-5-4.md:3966:+        uint256 proposalId = world.proposeGoldTransfer(clanA, clanB, amount, expiryTick);
./docs/reviews/pr200-codereview-codex-5-4.md:3989:+        world.acceptGoldTransfer(proposalId);
./docs/reviews/pr200-codereview-codex-5-4.md:4002:+        world.acceptGoldTransfer(proposalOne);
./docs/reviews/pr200-codereview-codex-5-4.md:4006:+        world.acceptGoldTransfer(proposalTwo);
./docs/reviews/pr200-codereview-codex-5-4.md:4016:+        world.acceptGoldTransfer(proposalId);
./docs/reviews/pr200-codereview-codex-5-4.md:4024:+        world.proposeGoldTransfer(clanA, clanB, 1e18, 10);
./docs/reviews/pr200-codereview-codex-5-4.md:4029:+        world.acceptGoldTransfer(proposalId);
./docs/reviews/pr200-codereview-codex-5-4.md:4044:+        world.acceptGoldTransfer(proposalId);
./docs/reviews/pr200-codereview-codex-5-4.md:4063:+        world.acceptGoldTransfer(proposalId);
./docs/reviews/pr200-codereview-codex-5-4.md:4087:+        proposalId = world.proposeGoldTransfer(fromClanId, toClanId, amount, expiryTick);
./docs/reviews/pr200-codereview-codex-5-4.md:4625:+        proposalId = world.proposeVaultTransfer(fromClanId, toClanId, wood, wheat, fish, iron, expiryTick);
./docs/reviews/pr200-codereview-codex-5-4.md:5043:function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/reviews/pr200-codereview-codex-5-4.md:5044:function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
./docs/reviews/pr200-codereview-codex-5-4.md:5045:function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/reviews/pr200-codereview-codex-5-4.md:5046:function transferBundle(
./docs/reviews/pr200-codereview-codex-5-4.md:5305:packages/contracts/src/ClanWorldStub.sol:120:    function transferGold(uint32, uint32, uint256) external override {}
./docs/reviews/pr200-codereview-codex-5-4.md:5306:packages/contracts/src/ClanWorldStub.sol:122:    function transferVaultResource(uint32, uint32, ResourceType, uint256) external override {}
./docs/reviews/pr200-codereview-codex-5-4.md:5307:packages/contracts/src/ClanWorldStub.sol:124:    function transferBlueprint(uint32, uint32, uint256) external override {}
./docs/reviews/pr200-codereview-codex-5-4.md:5308:packages/contracts/src/ClanWorldStub.sol:126:    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256) external override {}
./docs/reviews/pr200-codereview-codex-5-4.md:5326:packages/contracts/src/IClanWorld.sol:690:    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/reviews/pr200-codereview-codex-5-4.md:5327:packages/contracts/src/IClanWorld.sol:692:    function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
./docs/reviews/pr200-codereview-codex-5-4.md:5328:packages/contracts/src/IClanWorld.sol:694:    function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/reviews/pr200-codereview-codex-5-4.md:5329:packages/contracts/src/IClanWorld.sol:696:    function transferBundle(
./docs/reviews/pr200-codereview-codex-5-4.md:5349:packages/contracts/src/ClanWorld.sol:2118:    function transferGold(uint32, uint32, uint256) external pure override {
./docs/reviews/pr200-codereview-codex-5-4.md:5350:packages/contracts/src/ClanWorld.sol:2122:    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
./docs/reviews/pr200-codereview-codex-5-4.md:5351:packages/contracts/src/ClanWorld.sol:2126:    function transferBlueprint(uint32, uint32, uint256) external pure override {
./docs/reviews/pr200-codereview-codex-5-4.md:5352:packages/contracts/src/ClanWorld.sol:2130:    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
./packages/contracts/src/ClanWorld.sol:2118:    function transferGold(uint32, uint32, uint256) external pure override {
./packages/contracts/src/ClanWorld.sol:2122:    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
./packages/contracts/src/ClanWorld.sol:2126:    function transferBlueprint(uint32, uint32, uint256) external pure override {
./packages/contracts/src/ClanWorld.sol:2130:    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
./packages/contracts/src/IClanWorld.sol:690:    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./packages/contracts/src/IClanWorld.sol:692:    function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
./packages/contracts/src/IClanWorld.sol:694:    function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./packages/contracts/src/IClanWorld.sol:696:    function transferBundle(
./docs/reviews/pr198-codereview-codex-5-5.md:4602:   694	    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/reviews/pr198-codereview-codex-5-5.md:4604:   696	    function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
./docs/reviews/pr198-codereview-codex-5-5.md:4606:   698	    function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/reviews/pr198-codereview-codex-5-5.md:4608:   700	    function transferBundle(
./docs/reviews/pr198-codereview-codex-5-5.md:7110:  2118	    function transferGold(uint32, uint32, uint256) external pure override {
./docs/reviews/pr198-codereview-codex-5-5.md:7114:  2122	    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
./docs/reviews/pr198-codereview-codex-5-5.md:7118:  2126	    function transferBlueprint(uint32, uint32, uint256) external pure override {
./docs/reviews/pr198-codereview-codex-5-5.md:7122:  2130	    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
./packages/contracts/src/ClanWorldStub.sol:120:    function transferGold(uint32, uint32, uint256) external override {}
./packages/contracts/src/ClanWorldStub.sol:122:    function transferVaultResource(uint32, uint32, ResourceType, uint256) external override {}
./packages/contracts/src/ClanWorldStub.sol:124:    function transferBlueprint(uint32, uint32, uint256) external override {}
./packages/contracts/src/ClanWorldStub.sol:126:    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256) external override {}
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:638:- `transferGold(...)`, `transferVaultResource(...)`, `transferBlueprint(...)`, `transferBundle(...)`
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:665:function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:666:function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:667:function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:668:function transferBundle(

exec
/bin/bash -lc "sed -n '980,1055p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
    function settleClan(uint32 clanId) external override nonReentrant {
        _settleClan(clanId);
    }

    /// @notice Lazily settle a single clansman's mission to current tick. Idempotent.
    ///         Internally settles the entire clan (including upkeep) to guarantee
    ///         correct ordering and prevent double-settlement. Callers may call this
    ///         or settleClan interchangeably; both are safe and idempotent.
    function settleClansman(uint32 csId) external override nonReentrant {
        Clansman storage cs = _clansmen[csId];
        if (cs.clansmanId == 0) return;
        _settleClan(cs.clanId);
    }

    /// @notice Finalize season. Phase 1 stub.
    function finalizeSeason() external override {
        // TODO Phase 3
    }

    // =========================================================================
    // CLAN LIFECYCLE
    // =========================================================================

    /// @notice Mint a new clan and spawn its homebase.
    function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
        require(to != address(0), "ClanWorld: zero address");
        require(_allClanIds.length < 12, "ClanWorld: max clans");
        clanId = _nextClanId++;
        iftTokenId = uint256(clanId); // Phase 1 placeholder; real iNFT is Phase 7

        // Assign homebase region: valid spawn regions are 1,2,4,5,6,7 (not 3=UnicornTown, not 8=DeepSea)
        uint8[6] memory spawnRegions = [
            ClanWorldConstants.REGION_FOREST,
            ClanWorldConstants.REGION_MOUNTAINS,
            ClanWorldConstants.REGION_WEST_FARMS,
            ClanWorldConstants.REGION_EAST_FARMS,
            ClanWorldConstants.REGION_WEST_DOCKS,
            ClanWorldConstants.REGION_EAST_DOCKS
        ];
        uint8 baseRegion = spawnRegions[(clanId - 1) % 6];

        // Create clan
        Clan storage clan = _clans[clanId];
        clan.clanId = clanId;
        clan.iftTokenId = iftTokenId;
        clan.owner = to;
        clan.clanState = ClanState.ACTIVE;
        clan.baseRegion = baseRegion;
        clan.baseLevel = 1;
        clan.wallLevel = 0;
        clan.monumentLevel = 0;
        clan.livingClansmen = 4;
        clan.lastSettledTick = _world.currentTick;
        clan.starvationStartsAtTick = 0;
        clan.coldDamage = 0;
        clan.goldBalance = 3e18; // starter gold per v4 spec §12.5
        clan.blueprintBalance = 0;
        // Starting vault per v4 spec §12.4
        clan.vaultWood = 20e18;
        clan.vaultIron = 0;
        clan.vaultWheat = 20e18;
        clan.vaultFish = 2e18;

        // Wheat plots
        _wheatPlots[clanId][0] = WheatPlot({
            state: WheatPlotState.Harvestable,
            region: ClanWorldConstants.REGION_WEST_FARMS,
            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
            regrowUntilTick: 0
        });
        _wheatPlots[clanId][1] = WheatPlot({
            state: WheatPlotState.Harvestable,
            region: ClanWorldConstants.REGION_EAST_FARMS,
            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
            regrowUntilTick: 0
        });

exec
/bin/bash -lc "sed -n '1600,1795p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
        if (token == _treasury.woodToken) {
            carried = cs.carryWood;
            cap = ClanWorldConstants.WOOD_CAP;
        } else if (token == _treasury.ironToken) {
            carried = cs.carryIron;
            cap = ClanWorldConstants.IRON_CAP;
        } else if (token == _treasury.wheatToken) {
            carried = cs.carryWheat;
            cap = ClanWorldConstants.WHEAT_CAP;
        } else if (token == _treasury.fishToken) {
            carried = cs.carryFish;
            cap = ClanWorldConstants.FISH_CAP;
        } else {
            return 0;
        }

        if (carried >= cap) return 0;
        return cap - carried;
    }

    function _executeImmediateMarket(uint32 clanId, ClanOrder calldata order, uint32 clansmanId)
        internal
        returns (StatusCode)
    {
        if (order.action == ActionType.MarketSell) {
            return _executeImmediateMarketSell(clanId, clansmanId, order.marketToken, order.marketAmount);
        }
        return _executeImmediateMarketBuy(clanId, clansmanId, order.marketToken, order.marketAmount, order.maxGoldIn);
    }

    function _executeImmediateMarketSell(uint32 clanId, uint32 clansmanId, address token, uint256 amount)
        internal
        returns (StatusCode)
    {
        if (!_treasury.poolsSeeded) {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketSell,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
                _world.currentTick
            );
        }
        address poolAddr = _poolFor(token);
        if (poolAddr == address(0)) {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketSell,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
                _world.currentTick
            );
        }

        Clan storage clan = _clans[clanId];
        if (!_hasVaultBalance(clan, token, amount)) {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketSell,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_MISSING_RESOURCES,
                _world.currentTick
            );
        }

        try StubPool(poolAddr).swapExactInForOut(amount, 1) returns (uint256 goldOut) {
            _deductFromVault(clan, token, amount);
            clan.goldBalance += goldOut;
            emit ImmediateMarketActionExecuted(
                clanId,
                clansmanId,
                ActionType.MarketSell,
                _marketResourceForToken(token),
                amount,
                ClanWorldConstants.RESOURCE_GOLD,
                goldOut,
                _world.currentTick
            );
            return StatusCode.OK;
        } catch {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketSell,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
                _world.currentTick
            );
        }
    }

    function _executeImmediateMarketBuy(
        uint32 clanId,
        uint32 clansmanId,
        address token,
        uint256 amountOut,
        uint256 maxGoldIn
    ) internal returns (StatusCode) {
        if (!_treasury.poolsSeeded) {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
                _world.currentTick
            );
        }
        address poolAddr = _poolFor(token);
        if (poolAddr == address(0)) {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
                _world.currentTick
            );
        }

        Clansman storage cs = _clansmen[clansmanId];
        if (amountOut > _remainingCarryForToken(cs, token)) {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_CARRY_FULL,
                _world.currentTick
            );
        }

        uint256 goldIn;
        try StubPool(poolAddr).getAmountInForExactOut(amountOut) returns (uint256 quotedGoldIn) {
            goldIn = quotedGoldIn;
        } catch {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
                _world.currentTick
            );
        }

        Clan storage clan = _clans[clanId];
        if (goldIn > maxGoldIn) {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_MAX_GOLD_IN_EXCEEDED,
                _world.currentTick
            );
        }
        if (clan.goldBalance < goldIn) {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_NOT_ENOUGH_GOLD,
                _world.currentTick
            );
        }

        try StubPool(poolAddr).swapExactOutForInWithMaxIn(amountOut, maxGoldIn) returns (uint256 actualGoldIn) {
            clan.goldBalance -= actualGoldIn;
            _addToVault(clan, token, amountOut);
            emit ImmediateMarketActionExecuted(
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                ClanWorldConstants.RESOURCE_GOLD,
                actualGoldIn,
                _marketResourceForToken(token),
                amountOut,
                _world.currentTick
            );
            return StatusCode.OK;
        } catch {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Immediate,
                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
                _world.currentTick
            );
        }
    }

exec
/bin/bash -lc "sed -n '1795,2215p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
    }

    /// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.
    function _executeMarketSell(uint64 closedTick, uint32 clanId, uint32 clansmanId, address token, uint256 amount)
        internal
        returns (StatusCode)
    {
        if (!_treasury.poolsSeeded) {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketSell,
                MarketExecutionMode.Scheduled,
                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
                closedTick
            );
        }
        address poolAddr = _poolFor(token);
        if (poolAddr == address(0)) {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketSell,
                MarketExecutionMode.Scheduled,
                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
                closedTick
            );
        }

        Clan storage clan = _clans[clanId];
        if (!_deductFromVault(clan, token, amount)) {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketSell,
                MarketExecutionMode.Scheduled,
                StatusCode.ERR_MISSING_RESOURCES,
                closedTick
            );
        }

        uint256 goldOut = StubPool(poolAddr).sellResource(amount);
        clan.goldBalance += goldOut;

        emit ScheduledMarketActionExecuted(
            clanId,
            clansmanId,
            ActionType.MarketSell,
            _marketResourceForToken(token),
            amount,
            ClanWorldConstants.RESOURCE_GOLD,
            goldOut,
            closedTick
        );
        return StatusCode.OK;
    }

    /// @dev Execute a scheduled market buy: deduct gold from purse, credit resource to vault.
    function _executeMarketBuy(
        uint64 closedTick,
        uint32 clanId,
        uint32 clansmanId,
        address token,
        uint256 amountOut,
        uint256 maxGoldIn
    ) internal returns (StatusCode) {
        if (!_treasury.poolsSeeded) {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Scheduled,
                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
                closedTick
            );
        }
        address poolAddr = _poolFor(token);
        if (poolAddr == address(0)) {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Scheduled,
                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
                closedTick
            );
        }

        Clansman storage cs = _clansmen[clansmanId];
        if (amountOut > _remainingCarryForToken(cs, token)) {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Scheduled,
                StatusCode.ERR_CARRY_FULL,
                closedTick
            );
        }

        uint256 goldIn;
        try StubPool(poolAddr).quoteBuy(amountOut) returns (uint256 quotedGoldIn) {
            goldIn = quotedGoldIn;
        } catch {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Scheduled,
                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
                closedTick
            );
        }

        Clan storage clan = _clans[clanId];

        if (goldIn > maxGoldIn) {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Scheduled,
                StatusCode.ERR_MAX_GOLD_IN_EXCEEDED,
                closedTick
            );
        }
        if (clan.goldBalance < goldIn) {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Scheduled,
                StatusCode.ERR_NOT_ENOUGH_GOLD,
                closedTick
            );
        }

        uint256 actualGoldIn;
        try StubPool(poolAddr).swapExactOutForInWithMaxIn(amountOut, maxGoldIn) returns (uint256 spentGold) {
            actualGoldIn = spentGold;
        } catch {
            return _handleMarketFailure(
                clanId,
                clansmanId,
                ActionType.MarketBuy,
                MarketExecutionMode.Scheduled,
                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
                closedTick
            );
        }

        clan.goldBalance -= actualGoldIn;
        _addToVault(clan, token, amountOut);

        emit ScheduledMarketActionExecuted(
            clanId,
            clansmanId,
            ActionType.MarketBuy,
            ClanWorldConstants.RESOURCE_GOLD,
            actualGoldIn,
            _marketResourceForToken(token),
            amountOut,
            closedTick
        );
        return StatusCode.OK;
    }

    function _validateAction(Clan storage clan, Clansman storage cs, ClanOrder calldata order, uint8 gotoRegion)
        internal
        view
        returns (StatusCode)
    {
        ActionType action = order.action;

        if (action == ActionType.None) return StatusCode.ERR_INVALID_ACTION;

        // DepositResources: must go to homebase
        if (action == ActionType.DepositResources) {
            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
        }

        // BuildWall / UpgradeBase / UpgradeMonument: must go to homebase
        if (action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument)
        {
            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
        }

        // ChopWood: must go to Forest
        if (action == ActionType.ChopWood) {
            if (gotoRegion != ClanWorldConstants.REGION_FOREST) return StatusCode.ERR_INVALID_REGION;
        }
        // MineIron: must go to Mountains
        if (action == ActionType.MineIron) {
            if (gotoRegion != ClanWorldConstants.REGION_MOUNTAINS) return StatusCode.ERR_INVALID_REGION;
        }
        // FishDocks: must go to WestDocks or EastDocks
        if (action == ActionType.FishDocks) {
            if (
                gotoRegion != ClanWorldConstants.REGION_WEST_DOCKS && gotoRegion != ClanWorldConstants.REGION_EAST_DOCKS
            ) {
                return StatusCode.ERR_INVALID_REGION;
            }
        }
        // FishDeepSea: must go to DeepSea
        if (action == ActionType.FishDeepSea) {
            if (gotoRegion != ClanWorldConstants.REGION_DEEP_SEA) return StatusCode.ERR_INVALID_REGION;
        }
        // HarvestWheat: must go to WestFarms or EastFarms
        if (action == ActionType.HarvestWheat) {
            if (
                gotoRegion != ClanWorldConstants.REGION_WEST_FARMS && gotoRegion != ClanWorldConstants.REGION_EAST_FARMS
            ) {
                return StatusCode.ERR_INVALID_REGION;
            }
        }

        if (action == ActionType.DefendBase) {
            return _validateDefendBaseOrder(clan, order, gotoRegion);
        }

        // MarketBuy/MarketSell: must target Unicorn Town
        if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
            if (_treasury.woodToken == address(0)) return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
            if (gotoRegion != ClanWorldConstants.REGION_UNICORN_TOWN) {
                return StatusCode.ERR_INVALID_REGION;
            }
            if (order.marketAmount == 0) return StatusCode.ERR_MARKET_ZERO_AMOUNT;
            // Validate token is a supported resource token (not gold itself)
            address tok = order.marketToken;
            if (tok == address(0) || tok == _treasury.goldToken) {
                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
            }
            if (
                tok != _treasury.woodToken && tok != _treasury.ironToken && tok != _treasury.wheatToken
                    && tok != _treasury.fishToken
            ) {
                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
            }
            // Over-capacity buys are rejected at submission; no partial fills or overflow refunds.
            if (action == ActionType.MarketBuy && order.marketAmount > _remainingCarryForToken(cs, tok)) {
                return StatusCode.ERR_CARRY_FULL;
            }
            // Immediate market orders execute during submitClanOrders when the
            // worker is waiting in Unicorn Town and off cooldown. Other valid
            // market orders enqueue when the scheduled mission resolves.
            if (cs.currentRegion == ClanWorldConstants.REGION_UNICORN_TOWN && cs.state == ClansmanState.WAITING) {
                // Already at Unicorn Town — immediate if off cooldown, scheduled otherwise.
            }
        }

        cs; // suppress unused warning
        return StatusCode.OK;
    }

    function _validateDefendBaseOrder(Clan storage clan, ClanOrder calldata order, uint8 gotoRegion)
        internal
        view
        returns (StatusCode)
    {
        if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
        if (order.targetClanId != 0 && order.targetClanId != clan.clanId) return StatusCode.ERR_INVALID_TARGET;
        return StatusCode.OK;
    }

    // =========================================================================
    // TREASURY / POOL SEEDING
    // =========================================================================

    /// @notice One-time treasury initialization: register token and pool addresses.
    ///         Must be called before seedPools. Callable only once.
    function initTreasury(address[6] calldata tokens, address[4] calldata pools) external override nonReentrant {
        require(!_treasury.poolsSeeded && _treasury.woodToken == address(0), "ClanWorld: treasury already init");
        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");

        _treasury.woodToken = tokens[0];
        _treasury.ironToken = tokens[1];
        _treasury.wheatToken = tokens[2];
        _treasury.fishToken = tokens[3];
        _treasury.goldToken = tokens[4];
        _treasury.blueprintToken = tokens[5];

        _treasury.woodGoldPool = pools[0];
        _treasury.wheatGoldPool = pools[1];
        _treasury.fishGoldPool = pools[2];
        _treasury.ironGoldPool = pools[3];
    }

    /// @notice Owner-only. Seeds the four Unicorn Town AMM pools.
    function seedPools(PoolSeedConfig calldata cfg) external override nonReentrant {
        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");
        require(!_treasury.poolsSeeded, "ClanWorld: pools already seeded");
        require(_treasury.woodToken != address(0), "ClanWorld: treasury not init");

        _transferSeed(_treasury.woodToken, _treasury.woodGoldPool, cfg.woodSeed);
        _transferSeed(_treasury.goldToken, _treasury.woodGoldPool, cfg.goldSeedForWood);
        _transferSeed(_treasury.wheatToken, _treasury.wheatGoldPool, cfg.wheatSeed);
        _transferSeed(_treasury.goldToken, _treasury.wheatGoldPool, cfg.goldSeedForWheat);
        _transferSeed(_treasury.fishToken, _treasury.fishGoldPool, cfg.fishSeed);
        _transferSeed(_treasury.goldToken, _treasury.fishGoldPool, cfg.goldSeedForFish);
        _transferSeed(_treasury.ironToken, _treasury.ironGoldPool, cfg.ironSeed);
        _transferSeed(_treasury.goldToken, _treasury.ironGoldPool, cfg.goldSeedForIron);

        StubPool(_treasury.woodGoldPool).seed(cfg.woodSeed, cfg.goldSeedForWood);
        StubPool(_treasury.wheatGoldPool).seed(cfg.wheatSeed, cfg.goldSeedForWheat);
        StubPool(_treasury.fishGoldPool).seed(cfg.fishSeed, cfg.goldSeedForFish);
        StubPool(_treasury.ironGoldPool).seed(cfg.ironSeed, cfg.goldSeedForIron);

        _treasury.poolsSeeded = true;

        emit PoolsSeeded(
            _treasury.woodGoldPool, _treasury.wheatGoldPool, _treasury.fishGoldPool, _treasury.ironGoldPool
        );
    }

    function _transferSeed(address token, address pool, uint256 amount) internal {
        require(token != address(0) && pool != address(0), "ClanWorld: treasury not init");
        require(MinimalERC20(token).transferFrom(msg.sender, pool, amount), "ClanWorld: seed transfer failed");
    }

    // =========================================================================
    // OTC TRANSFERS
    // =========================================================================

    function transferGold(uint32, uint32, uint256) external pure override {
        revert("OTC transfers not implemented");
    }

    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
        revert("OTC transfers not implemented");
    }

    function transferBlueprint(uint32, uint32, uint256) external pure override {
        revert("OTC transfers not implemented");
    }

    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
        external
        pure
        override
    {
        revert("OTC transfers not implemented");
    }

    // =========================================================================
    // RAW READ GETTERS
    // =========================================================================

    function getWorldState() external view override returns (WorldState memory) {
        return _world;
    }

    function getTreasuryState() external view override returns (TreasuryState memory) {
        return _treasury;
    }

    function getResourceToken(uint8 resourceType) external view override returns (address) {
        if (resourceType == uint8(ResourceType.Wood)) return _treasury.woodToken;
        if (resourceType == uint8(ResourceType.Iron)) return _treasury.ironToken;
        if (resourceType == uint8(ResourceType.Wheat)) return _treasury.wheatToken;
        if (resourceType == uint8(ResourceType.Fish)) return _treasury.fishToken;
        revert("ClanWorld: invalid resource");
    }

    function getPool(uint8 resourceType) external view override returns (address) {
        return _poolForResourceType(resourceType);
    }

    function getPrice(uint8 resourceType, uint256 amountIn) external view override returns (uint256 amountOut) {
        amountOut = StubPool(_poolForResourceType(resourceType)).getAmountOutForExactIn(amountIn);
    }

    function _poolForResourceType(uint8 resourceType) internal view returns (address) {
        if (resourceType == uint8(ResourceType.Wood)) return _treasury.woodGoldPool;
        if (resourceType == uint8(ResourceType.Iron)) return _treasury.ironGoldPool;
        if (resourceType == uint8(ResourceType.Wheat)) return _treasury.wheatGoldPool;
        if (resourceType == uint8(ResourceType.Fish)) return _treasury.fishGoldPool;
        revert("ClanWorld: invalid resource");
    }

    function getClan(uint32 clanId) external view override returns (Clan memory) {
        return _clans[clanId];
    }

    function getClansman(uint32 clansmanId) external view override returns (Clansman memory) {
        return _clansmen[clansmanId];
    }

    function getActiveMission(uint32 clansmanId) external view override returns (Mission memory) {
        return _missions[clansmanId];
    }

    function getMissionTiming(uint32 clanId, uint32 clansmanId)
        external
        view
        override
        returns (uint64 submitted, uint64 executes, uint64 settles)
    {
        Mission memory m = _missions[clansmanId];
        if (!m.active || _clansmen[clansmanId].clanId != clanId) {
            return (0, 0, 0);
        }
        return (m.submittedAtTick, m.executesAtTick, m.settlesAtTick);
    }

    function getActionDuration(ActionType action) public pure override returns (uint64) {
        if (
            action == ActionType.ChopWood || action == ActionType.MineIron || action == ActionType.FishDocks
                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
        ) {
            return 4;
        }

        if (action == ActionType.DepositResources) {
            return DEPOSIT_DURATION_TICKS;
        }

        if (
            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
                || action == ActionType.MarketBuy || action == ActionType.MarketSell
        ) {
            return 1;

exec
/bin/bash -lc "rg -n \"\\.mint\\(|\\.burn\\(|ResourceMinted\\(|ResourceBurned\\(\" packages/contracts/src" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/src/IClanWorld.sol:590:    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
packages/contracts/src/IClanWorld.sol:591:    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);
packages/contracts/src/MinimalERC20.sol:23:    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
packages/contracts/src/MinimalERC20.sol:24:    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);
packages/contracts/src/MinimalERC20.sol:74:        emit ResourceMinted(resourceType, to, amount);
packages/contracts/src/MinimalERC20.sol:81:        emit ResourceBurned(resourceType, from, amount);

exec
/bin/bash -lc "sed -n '620,700p' docs/planning/clanworld_v4_2_state_schema_interface_spec.md" in /home/claude/code/clan-world
 succeeded in 0ms:
function getBanditTargetPreview(uint32 banditId) external view returns (uint32 previewClanId);
function quoteTravel(uint8 src, uint8 dst) external view returns (uint8 travelTicks, bytes8 path);
function quoteLootValue(uint32 clanId) external view returns (uint256 lootValue);
```

### 9.3 Bandit target preview disclaimer
`getBanditTargetPreview()` is **non-binding**.

Bandit targeting is recomputed at attack resolution time using then-current eagerly settled state.

---

## 10. Write interfaces and access control

### 10.1 Access control rules

- `submitClanOrders(clanId, ...)`
  - only clan owner or approved operator
- `transferGold(...)`, `transferVaultResource(...)`, `transferBlueprint(...)`, `transferBundle(...)`
  - only sender clan owner or approved operator
- `mintClan(...)`
  - public payable if minting is enabled
- `seedPools(...)`
  - only owner/admin
- `finalizeSeason()`
  - permissionless after season end, because outcome is deterministic
- `heartbeat()`
  - permissionless, but rate-limited by `nextHeartbeatAtTs`

### 10.2 Core write functions

```solidity
function heartbeat() external;
function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders) external returns (OrderResult[] memory);
function settleClan(uint32 clanId) external;
function mintClan(address to) external payable returns (uint32 clanId, uint256 iftTokenId);
function seedPools(PoolSeedConfig calldata cfg) external;
function finalizeSeason() external;
```

### 10.3 OTC transfer surface

Internal accounting requires explicit transfer functions.

```solidity
function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
function transferBundle(
    uint32 fromClanId,
    uint32 toClanId,
    uint256 gold,
    uint256 blueprint,
    uint256 wood,
    uint256 iron,
    uint256 wheat,
    uint256 fish
) external;
```

OTC transfers may draw only from:
- clan vault resources
- clan gold purse
- clan blueprint balance

OTC transfers may **not** draw from worker carry balances.

### 10.4 ClanOrder

```solidity
struct ClanOrder {
    uint32 clansmanId;
    uint8 gotoRegion;
    ActionType action;

    uint32 targetClanId;
    address marketToken;
    uint256 marketAmount;
    uint256 maxGoldIn;
}
```

exec
/bin/bash -lc "sed -n '120,140p' docs/planning/clanworld_numbered_implementation_plan.md" in /home/claude/code/clan-world
 succeeded in 0ms:
- wheat plot starting state

### 2.4 Add access control rules
Implement:
- clan owner / operator checks for order submission
- admin for pool seeding
- finalize permissions
- dead clan restrictions on OTC

**Definition of done**
- minting a clan gives a fully initialized playable clan
- getters show coherent initial state

**Cut line:** if behind, prize pot routing can be stubbed or deferred.

---

## Phase 3 — mission assignment and lazy settlement core

### 3.1 Implement order submission
Implement:

exec
/bin/bash -lc "sed -n '236,250p' docs/planning/clanworld_numbered_implementation_plan.md" in /home/claude/code/clan-world
 succeeded in 0ms:

### 5.4 Implement wheat plot system
- harvestable state
- depletion
- regrow
- winter interaction hook

### 5.5 Implement deposit action
- worker carry -> vault transfer
- action tick semantics
- proper event emission

### 5.6 Implement starvation consequences
- next-tick starvation start
- 50% gathering penalty

codex
One cross-cutting seam is looking especially important now: balances and clan state in this engine are lazily settled. I’m checking whether the new OTC entrypoints settle the source/target clans before reading balances or dead/alive status, because if they don’t, proposal acceptance can run against stale gold/vault/death state.
exec
/bin/bash -lc "rg -n \"_settleClan\\(|submitClanOrders\\(|clan.lastSettledTick|starvationStartsAtTick|_resolveWorldEvents|_killClan|clanState = ClanState.DEAD|clanState "'!= ClanState.DEAD" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
 succeeded in 0ms:
354:    function _settleClan(uint32 clanId) internal {
359:        uint64 fromTick = clan.lastSettledTick;
393:        clan.lastSettledTick = curTick;
419:        if (starving && clan.starvationStartsAtTick == 0) {
420:            clan.starvationStartsAtTick = tick;
422:        } else if (!starving && clan.starvationStartsAtTick != 0) {
423:            clan.starvationStartsAtTick = 0;
430:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
902:        _resolveWorldEvents(closedTick);
938:    function _resolveWorldEvents(uint64 closedTick) internal {
981:        _settleClan(clanId);
991:        _settleClan(cs.clanId);
1032:        clan.lastSettledTick = _world.currentTick;
1033:        clan.starvationStartsAtTick = 0;
1081:    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
1113:        _settleClan(clanId);
2301:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
2406:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;

exec
/bin/bash -lc "sed -n '104,240p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
    // =========================================================================
    // TRAVEL DISTANCE MATRIX
    // =========================================================================

    // Precomputed BFS distances for 8 regions (1-indexed; index 0 unused).
    // Adjacency:
    //   Forest(1):       Mountains(2), WestFarms(4)
    //   Mountains(2):    Forest(1), UnicornTown(3)
    //   UnicornTown(3):  Mountains(2), WestFarms(4), EastFarms(5)
    //   WestFarms(4):    Forest(1), UnicornTown(3), WestDocks(6)
    //   EastFarms(5):    UnicornTown(3), EastDocks(7)
    //   WestDocks(6):    WestFarms(4), DeepSea(8)
    //   EastDocks(7):    EastFarms(5), DeepSea(8)
    //   DeepSea(8):      WestDocks(6), EastDocks(7)
    //
    // Distance table dist[src][dst] — 0-indexed internally (region - 1).
    // Distance of 0 = same region.
    //
    // Full BFS-computed 8x8 matrix:
    //   src\dst  1  2  3  4  5  6  7  8
    //      1     0  1  2  1  3  2  4  3
    //      2     1  0  1  2  2  3  3  4
    //      3     2  1  0  1  1  2  2  3
    //      4     1  2  1  0  2  1  3  2
    //      5     3  2  1  2  0  3  1  2
    //      6     2  3  2  1  3  0  2  1
    //      7     4  3  2  3  1  2  0  1
    //      8     3  4  3  2  2  1  1  0

    function _distMatrix(uint8 src, uint8 dst) private pure returns (uint8) {
        if (src == dst) return 0;
        // Encode as (src-1)*8 + (dst-1), values from BFS
        uint8[64] memory d = [
            // src=1: to 1,2,3,4,5,6,7,8
            0,
            1,
            2,
            1,
            3,
            2,
            4,
            3,
            // src=2: to 1,2,3,4,5,6,7,8
            1,
            0,
            1,
            2,
            2,
            3,
            3,
            4,
            // src=3: to 1,2,3,4,5,6,7,8
            2,
            1,
            0,
            1,
            1,
            2,
            2,
            3,
            // src=4: to 1,2,3,4,5,6,7,8
            1,
            2,
            1,
            0,
            2,
            1,
            3,
            2,
            // src=5: to 1,2,3,4,5,6,7,8
            3,
            2,
            1,
            2,
            0,
            3,
            1,
            2,
            // src=6: to 1,2,3,4,5,6,7,8
            2,
            3,
            2,
            1,
            3,
            0,
            2,
            1,
            // src=7: to 1,2,3,4,5,6,7,8
            4,
            3,
            2,
            3,
            1,
            2,
            0,
            1,
            // src=8: to 1,2,3,4,5,6,7,8
            3,
            4,
            3,
            2,
            2,
            1,
            1,
            0
        ];
        return d[uint8(src - 1) * 8 + uint8(dst - 1)];
    }

    // Build a canonical path from src to dst (BFS on the adjacency graph).
    // Returns packed bytes8: region IDs in order, zero-padded.
    function _buildPath(uint8 src, uint8 dst) private pure returns (bytes8) {
        if (src == dst) {
            return bytes8(uint64(src) << 56);
        }
        // Adjacency list (1-indexed, 0 = end sentinel)
        // adj[r] = neighbors of region r (up to 3)
        uint8[3][9] memory adj = [
            [0, 0, 0], // 0: unused
            [2, 4, 0], // 1: Forest
            [1, 3, 0], // 2: Mountains
            [2, 4, 5], // 3: UnicornTown
            [1, 3, 6], // 4: WestFarms
            [3, 7, 0], // 5: EastFarms
            [4, 8, 0], // 6: WestDocks
            [5, 8, 0], // 7: EastDocks
            [6, 7, 0] // 8: DeepSea
        ];

        // BFS with parent tracking (max 8 nodes)
        uint8[9] memory parent;
        bool[9] memory visited;
        uint8[9] memory queue;
        uint256 head;
        uint256 tail;

        for (uint256 i = 0; i < 9; i++) {

exec
/bin/bash -lc "sed -n '240,420p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
        for (uint256 i = 0; i < 9; i++) {
            parent[i] = 0;
            visited[i] = false;
        }

        visited[src] = true;
        queue[tail++] = src;

        while (head < tail) {
            uint8 curr = queue[head++];
            if (curr == dst) break;
            for (uint256 ni = 0; ni < 3; ni++) {
                uint8 nb = adj[curr][ni];
                if (nb == 0) break;
                if (!visited[nb]) {
                    visited[nb] = true;
                    parent[nb] = curr;
                    queue[tail++] = nb;
                }
            }
        }

        // Reconstruct path backwards
        uint8[8] memory path;
        uint256 pathLen;
        uint8 cur = dst;
        while (cur != src) {
            path[pathLen++] = cur;
            cur = parent[cur];
        }
        path[pathLen++] = src;

        // Reverse into result
        bytes8 packed;
        uint64 byteShift = 56;
        for (uint256 i = pathLen; i > 0; i--) {
            packed = packed | bytes8(uint64(path[i - 1]) << byteShift);
            if (byteShift >= 8) byteShift -= 8;
        }
        return packed;
    }

    function _travelTicks(uint8 fromRegion, uint8 toRegion) private pure returns (uint8) {
        if (fromRegion == 0 || toRegion == 0) return 0; // NOOP region
        if (fromRegion == toRegion) return 0;
        if (fromRegion > 8 || toRegion > 8) return 0;
        return _distMatrix(fromRegion, toRegion);
    }

    function _addTicksClamped(uint64 tick, uint64 delta) private pure returns (uint64) {
        if (type(uint64).max - tick < delta) return type(uint64).max;
        return tick + delta;
    }

    // =========================================================================
    // INTERNAL SETTLEMENT
    // =========================================================================

    /// @dev Settle a single clansman's mission for the tick range [fromTick, toTick).
    ///      Handles all 6 mission lifecycle paths. Called from _settleClan and settleClansman.
    function _settleMissionForClansman(
        Clan storage clan,
        Clansman storage cs,
        uint32 clanId,
        uint64 fromTick,
        uint64 toTick
    ) internal {
        Mission storage m = _missions[cs.clansmanId];

        // Path 6: dead clansman — invalidate active mission if any
        if (cs.state == ClansmanState.DEAD) {
            if (m.active) {
                if (m.action == ActionType.DefendBase) {
                    _clearDefender(cs.clansmanId);
                }
                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
            }
            return;
        }

        if (!m.active) return; // no active mission — nothing to settle

        bytes32 tickSeed;
        for (uint64 tick = fromTick; tick < toTick; tick++) {
            tickSeed = _tickSeeds[tick];

            // Path 1 → Path 2 transition: TRAVELING → ACTING at arrivalTick
            if (cs.state == ClansmanState.TRAVELING && tick >= m.arrivalTick) {
                cs.state = ClansmanState.ACTING;
                cs.currentRegion = m.targetRegion;
                emit WorkerArrived(clanId, cs.clansmanId, m.targetRegion, tick);

                if (m.action == ActionType.DefendBase) {
                    _registerDefender(m.targetRegion, clanId, cs.clansmanId);
                }
            }

            if (m.action == ActionType.DefendBase) continue; // persistent defender mission

            // Path 3: ACTING at/past settlesAtTick → resolve
            if (cs.state == ClansmanState.ACTING && tick >= m.settlesAtTick) {
                _resolveAction(clan, cs, m, clanId, tick, tickSeed);
                if (m.active && getActionDuration(m.action) > 0) {
                    _completeMission(cs, m);
                }
            }

            // If mission completed during this tick, stop iterating
            if (!m.active) break;
        }
    }

    /// @dev Lazy settlement of a clan forward to currentTick.
    ///      Mutates storage. Called before order submission and by public settleClan().
    function _settleClan(uint32 clanId) internal {
        Clan storage clan = _clans[clanId];
        if (clan.clanId == 0) return;

        uint64 curTick = _world.currentTick;
        uint64 fromTick = clan.lastSettledTick;
        if (fromTick >= curTick) return;

        // Cap ticks settled per call to prevent block gas limit issues
        uint64 maxSettleTicks = 200;
        if (curTick > fromTick + maxSettleTicks) {
            curTick = fromTick + maxSettleTicks;
        }

        uint32[] storage clansmanIds = _clanClansmanIds[clanId];

        // Settle tick by tick from fromTick to curTick - 1
        // (curTick is still open; we settle through the last closed tick)
        for (uint64 tick = fromTick; tick < curTick; tick++) {
            // 1. Apply upkeep for this tick
            _applyUpkeep(clan, tick);

            // 2. Wheat plot regrow check (lazy, per tick)
            for (uint256 pi = 0; pi < 2; pi++) {
                WheatPlot storage plot = _wheatPlots[clanId][pi];
                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
                    plot.state = WheatPlotState.Harvestable;
                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
                    plot.regrowUntilTick = 0;
                }
            }

            // 3. Advance each clansman (single-tick range: [tick, tick+1))
            for (uint256 i = 0; i < clansmanIds.length; i++) {
                Clansman storage cs = _clansmen[clansmanIds[i]];
                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
            }
        }

        clan.lastSettledTick = curTick;
        emit ClanSettled(clanId, curTick);
    }

    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
    function _applyUpkeep(Clan storage clan, uint64 tick) internal {
        if (clan.livingClansmen == 0) return;

        uint256 wheatNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.WHEAT_UPKEEP_PER_CLANSMAN;
        uint256 fishNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.FISH_UPKEEP_PER_CLANSMAN;

        bool hadEnoughWheat = clan.vaultWheat >= wheatNeeded;
        bool hadEnoughFish = clan.vaultFish >= fishNeeded;

        if (hadEnoughWheat) {
            clan.vaultWheat -= wheatNeeded;
        } else {
            clan.vaultWheat = 0;
        }
        if (hadEnoughFish) {
            clan.vaultFish -= fishNeeded;
        } else {
            clan.vaultFish = 0;
        }

        bool starving = !hadEnoughWheat || !hadEnoughFish;
        if (starving && clan.starvationStartsAtTick == 0) {
            clan.starvationStartsAtTick = tick;

exec
/bin/bash -lc "sed -n '420,520p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
            clan.starvationStartsAtTick = tick;
            emit ClanStarvationChanged(clan.clanId, true, tick);
        } else if (!starving && clan.starvationStartsAtTick != 0) {
            clan.starvationStartsAtTick = 0;
            emit ClanStarvationChanged(clan.clanId, false, tick);
        }
    }

    /// @dev Check if a clan is currently starving (lazy read).
    function _isStarving(Clan storage clan) internal view returns (bool) {
        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
    }

    /// @dev Resolve an action for a clansman that is in ACTING state.
    function _resolveAction(
        Clan storage clan,
        Clansman storage cs,
        Mission storage m,
        uint32 clanId,
        uint64 tick,
        bytes32 tickSeed
    ) internal {
        bool starving = _isStarving(clan);
        ActionType action = m.action;

        if (action == ActionType.ChopWood) {
            _gatherWood(clan, cs, m, clanId, tick, starving, tickSeed);
        } else if (action == ActionType.MineIron) {
            _gatherIron(clan, cs, m, clanId, tick, starving, tickSeed);
        } else if (action == ActionType.FishDocks) {
            _gatherFishDocks(clan, cs, m, clanId, tick, starving, tickSeed);
        } else if (action == ActionType.FishDeepSea) {
            _gatherFishDeepSea(clan, cs, m, clanId, tick, starving, tickSeed);
        } else if (action == ActionType.HarvestWheat) {
            _gatherWheat(clan, cs, m, clanId, tick, starving);
        } else if (action == ActionType.DepositResources) {
            // Deposit event ABI intentionally stores the current tick as uint32.
            // forge-lint: disable-next-line(unsafe-typecast)
            _doDeposit(clan, cs, m, clanId, uint32(tick));
        } else if (action == ActionType.Wait) {
            // NOOP — worker stays ACTING (continuous), no transition needed
            // Wait mission is effectively persistent until interrupted
        } else if (action == ActionType.DefendBase) {
            // Persistent mission. Registration happens atomically at order submission.
        } else if (
            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
        ) {
            // Phase 1 stub: check homebase, check resources; if ok, stub success
            _doBuilding(clan, cs, m, clanId, tick, action);
        } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
            _enqueueScheduledMarketAction(clanId, cs.clansmanId, m);
            _completeMission(cs, m);
        }
    }

    // -------------------------------------------------------------------------
    // Gathering helpers
    // -------------------------------------------------------------------------

    function _gatherWood(
        Clan storage, // clan (unused — no clan-level mutation in wood gather)
        Clansman storage cs,
        Mission storage m,
        uint32 clanId,
        uint64 tick,
        bool starving,
        bytes32 tickSeed
    ) internal {
        if (cs.carryWood >= ClanWorldConstants.WOOD_CARRY_CAP) {
            _completeMission(cs, m);
            return;
        }

        uint256 remaining = ClanWorldConstants.WOOD_CARRY_CAP - cs.carryWood;
        uint256 yield = ClanWorldConstants.WOOD_YIELD_PER_TICK * uint256(getActionDuration(ActionType.ChopWood));
        bytes32 woodRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
        if (uint256(woodRng) % 10000 < ClanWorldConstants.WOOD_CRIT_BPS) {
            yield *= 2;
        }

        if (starving) yield = yield / 2;
        if (yield > remaining) yield = remaining;
        cs.carryWood += yield;

        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.ChopWood, yield, 0, 0, 0, 0, tick);

        if (cs.carryWood >= ClanWorldConstants.WOOD_CARRY_CAP) {
            _completeMission(cs, m);
        }
    }

    function _gatherIron(
        Clan storage clan,
        Clansman storage cs,
        Mission storage m,
        uint32 clanId,
        uint64 tick,
        bool starving,
        bytes32 tickSeed
    ) internal {
        uint256 remaining = ClanWorldConstants.IRON_CAP - cs.carryIron;

exec
/bin/bash -lc 'rg -n "_killClan|ClanDied|livingClansmen == 0|vaultWood = 0|vaultWheat = 0|vaultFish = 0|vaultIron = 0|clanState = ClanState.DEAD" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
 succeeded in 0ms:
399:        if (clan.livingClansmen == 0) return;
410:            clan.vaultWheat = 0;
415:            clan.vaultFish = 0;
1039:        clan.vaultIron = 0;

exec
/bin/bash -lc "sed -n '900,980p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:

        // Step 4: Resolve world events (season boundary, winter transitions).
        _resolveWorldEvents(closedTick);

        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
        uint64 newTick = closedTick + 1;
        _world.currentTick = newTick;
        _world.nextHeartbeatAtTick = newTick + 1;

        emit TickAdvanced(closedTick, newTick, newSeed);
    }

    /// @dev Settle missions that complete exactly at `tick` (settlesAtTick == tick).
    ///      Called from heartbeat before market execution and tick increment.
    ///      Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
    function _settleCompletingMissions(uint64 tick) internal {
        for (uint256 i = 0; i < _allClanIds.length; i++) {
            uint32 clanId = _allClanIds[i];
            Clan storage clan = _clans[clanId];
            if (clan.clanState == ClanState.DEAD) continue;

            uint32[] storage csIds = _clanClansmanIds[clanId];
            for (uint256 j = 0; j < csIds.length; j++) {
                Clansman storage cs = _clansmen[csIds[j]];
                if (cs.state == ClansmanState.DEAD) continue;

                Mission storage m = _missions[cs.clansmanId];
                if (!m.active) continue;
                if (m.settlesAtTick != tick) continue; // not due this tick

                // Settle this mission using the single-tick range [tick, tick+1).
                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
            }
        }
    }

    /// @dev Resolve world events for the tick that was just closed.
    ///      Uses closedTick+1 as the equivalent of the old `newTick` for transition checks.
    function _resolveWorldEvents(uint64 closedTick) internal {
        uint64 newTick = closedTick + 1;

        // --- season boundary ---
        if (newTick >= _world.seasonEndTick) {
            _world.currentSeasonNumber += 1;
            _world.seasonStartTick = _world.seasonEndTick;
            _world.seasonEndTick = _world.seasonStartTick + ClanWorldConstants.SEASON_DURATION_TICKS;
            // reset winter timers for new season
            _world.winterActive = false;
            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
                - ClanWorldConstants.WINTER_DURATION_TICKS;
            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
        }

        // --- winter transitions (timer only; mechanics = Phase 10) ---
        if (
            !_world.winterActive && newTick >= _world.winterStartsAtTick
                && _world.winterStartsAtTick < _world.seasonEndTick
        ) {
            _world.winterActive = true;
            emit WinterStarted(newTick);
        }
        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
            _world.winterActive = false;
            emit WinterEnded(newTick);
            // schedule next winter cycle within this season
            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
                - ClanWorldConstants.WINTER_DURATION_TICKS;
            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
            if (nextWinterStart < _world.seasonEndTick) {
                _world.winterStartsAtTick = nextWinterStart;
                _world.winterEndsAtTick = nextWinterEnd;
            } else {
                // no more winters this season; sentinel = seasonEndTick so guard never fires
                _world.winterStartsAtTick = _world.seasonEndTick;
                _world.winterEndsAtTick = _world.seasonEndTick;
            }
        }
    }

    /// @notice Public settlement trigger — lazily settle a clan.
    function settleClan(uint32 clanId) external override nonReentrant {

exec
/bin/bash -lc "sed -n '520,620p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
        uint256 remaining = ClanWorldConstants.IRON_CAP - cs.carryIron;
        if (remaining == 0) {
            _completeMission(cs, m);
            return;
        }
        uint256 yield = ClanWorldConstants.IRON_BASE_YIELD;
        if (starving) yield = yield / 2;
        if (yield > remaining) yield = remaining;
        cs.carryIron += yield;

        // Gold bonus roll — scoped to reduce stack depth
        uint256 goldBonus = _rollIronGoldBonus(clan, cs.clansmanId, m.nonce, tick, tickSeed);

        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.MineIron, 0, yield, 0, 0, goldBonus, tick);

        if (cs.carryIron >= ClanWorldConstants.IRON_CAP) {
            _completeMission(cs, m);
        }
    }

    function _rollIronGoldBonus(Clan storage clan, uint32 clansmanId, uint64 nonce, uint64 tick, bytes32 tickSeed)
        internal
        returns (uint256 goldBonus)
    {
        bytes32 goldRng = keccak256(abi.encode("iron_gold_bonus", tickSeed, clansmanId, nonce, tick));
        if (uint256(goldRng) % 10000 < ClanWorldConstants.GOLD_FROM_IRON_BPS) {
            goldBonus = ClanWorldConstants.GOLD_FROM_IRON_AMOUNT;
            clan.goldBalance += goldBonus;
        }
    }

    function _gatherFishDocks(
        Clan storage,
        Clansman storage cs,
        Mission storage m,
        uint32 clanId,
        uint64 tick,
        bool starving,
        bytes32 tickSeed
    ) internal {
        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
        if (remaining == 0) {
            _completeMission(cs, m);
            return;
        }
        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
        uint256 fishRoll = uint256(fishRng) % 10000;
        uint256 yield = 0;
        if (fishRoll < ClanWorldConstants.FISH_DOCKS_BPS) {
            yield = 1e18;
        }
        if (starving) yield = yield / 2;
        if (yield > remaining) yield = remaining;
        if (yield > 0) {
            cs.carryFish += yield;
            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDocks, 0, 0, 0, yield, 0, tick);
        }
        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
            _completeMission(cs, m);
        }
    }

    function _gatherFishDeepSea(
        Clan storage,
        Clansman storage cs,
        Mission storage m,
        uint32 clanId,
        uint64 tick,
        bool starving,
        bytes32 tickSeed
    ) internal {
        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
        if (remaining == 0) {
            _completeMission(cs, m);
            return;
        }
        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
        uint256 fishRoll = uint256(fishRng) % 10000;
        uint256 yield = 0;
        if (fishRoll < ClanWorldConstants.FISH_DEEP_BPS) {
            yield = 1e18;
        }
        if (starving) yield = yield / 2;
        if (yield > remaining) yield = remaining;
        if (yield > 0) {
            cs.carryFish += yield;
            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDeepSea, 0, 0, 0, yield, 0, tick);
        }
        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
            _completeMission(cs, m);
        }
    }

    function _gatherWheat(
        Clan storage,
        /* clan — unused but kept positional for callsite parity */
        Clansman storage cs,
        Mission storage m,
        uint32 clanId,
        uint64 tick,
        bool starving

exec
/bin/bash -lc 'rg -n "operator|approved operator|approveOperator|setApprovalForAll|owner == msg.sender|not clan owner" packages/contracts/src/IClanWorld.sol packages/contracts/src/ClanWorld.sol docs/planning/clanworld_v4_2_state_schema_interface_spec.md' in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/src/ClanWorld.sol:1089:        require(clan.owner == msg.sender, "ClanWorld: not clan owner");
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:637:  - only clan owner or approved operator
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:639:  - only sender clan owner or approved operator

exec
/bin/bash -lc "rg -n \"only clan owner or approved operator|approved operator\" docs/planning -g '*.md'" in /home/claude/code/clan-world
 succeeded in 0ms:
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:637:  - only clan owner or approved operator
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:639:  - only sender clan owner or approved operator

exec
/bin/bash -lc "rg -n \"ResourcesDeposited|GoldTransferProposed|VaultTransferProposed|BlueprintTransferProposed|BundledTransferProposed|getOtcGoldProposal|getOtcVaultTransferProposal|getOtcBlueprintTransferProposal|getOtcBundledTransferProposal\" apps packages -g '"'!packages/contracts/**'"'" in /home/claude/code/clan-world
 exited 1 in 0ms:
exec
/bin/bash -lc "rg -n \"ResourcesDeposited\\(uint32,uint32,uint256,uint256,uint256,uint256,uint64\\)|ResourcesDeposited\\(\" ." in /home/claude/code/clan-world
 succeeded in 0ms:
./docs/reviews/pr194-r2-codereview-codex-5-5.md:918:+        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
./docs/reviews/pr194-r2-codereview-codex-5-5.md:2144:-        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
./docs/reviews/pr200-codereview-codex-5-5.md:1831:-        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
./docs/reviews/pr200-codereview-codex-5-5.md:1832:+        emit ResourcesDeposited(clanId, cs.clansmanId, woodDelta, ironDelta, wheatDelta, fishDelta, tick);
./docs/reviews/pr200-codereview-codex-5-5.md:2529:     event ResourcesDeposited(
./docs/reviews/pr200-codereview-codex-5-5.md:3446:+        bytes32 depositedTopic = keccak256("ResourcesDeposited(uint32,uint32,uint256,uint256,uint256,uint256,uint32)");
./docs/reviews/pr200-codereview-codex-5-5.md:3505:+        emit IClanWorldEvents.ResourcesDeposited(clanId, csId, 5e18, 2e18, 1e18, 3e18, 1);
./docs/reviews/r2/pr194-codereview-codex-5-5.md:926:+        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
./docs/reviews/r2/pr194-codereview-codex-5-5.md:2087:-        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
./docs/reviews/pr198-codereview-codex-5-4.md:865:-        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
./docs/reviews/pr198-codereview-codex-5-4.md:866:+        emit ResourcesDeposited(clanId, cs.clansmanId, woodDelta, ironDelta, wheatDelta, fishDelta, tick);
./docs/reviews/pr198-codereview-codex-5-4.md:1802:     event ResourcesDeposited(
./docs/reviews/pr198-codereview-codex-5-4.md:2240:+        bytes32 depositedTopic = keccak256("ResourcesDeposited(uint32,uint32,uint256,uint256,uint256,uint256,uint32)");
./docs/reviews/pr198-codereview-codex-5-4.md:2299:+        emit IClanWorldEvents.ResourcesDeposited(clanId, csId, 5e18, 2e18, 1e18, 3e18, 1);
./docs/reviews/pr198-codereview-codex-5-4.md:9251:   541	    event ResourcesDeposited(
./docs/reviews/r2/pr194-codereview-codex-5-4.md:926:+        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
./docs/reviews/r2/pr194-codereview-codex-5-4.md:2087:-        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
./docs/reviews/r2/pr194-codereview-codex-5-4.md:5419:        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
./docs/reviews/r2/pr194-codereview-codex-5-4.md:6764:    event ResourcesDeposited(
./docs/reviews/r2/pr194-codereview-codex-5-4.md:7895:   866	        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
./docs/reviews/pr200-codereview-codex-5-4.md:1831:-        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
./docs/reviews/pr200-codereview-codex-5-4.md:1832:+        emit ResourcesDeposited(clanId, cs.clansmanId, woodDelta, ironDelta, wheatDelta, fishDelta, tick);
./docs/reviews/pr200-codereview-codex-5-4.md:2529:     event ResourcesDeposited(
./docs/reviews/pr200-codereview-codex-5-4.md:3446:+        bytes32 depositedTopic = keccak256("ResourcesDeposited(uint32,uint32,uint256,uint256,uint256,uint256,uint32)");
./docs/reviews/pr200-codereview-codex-5-4.md:3505:+        emit IClanWorldEvents.ResourcesDeposited(clanId, csId, 5e18, 2e18, 1e18, 3e18, 1);
./docs/reviews/pr198-codereview-codex-5-5.md:865:-        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
./docs/reviews/pr198-codereview-codex-5-5.md:866:+        emit ResourcesDeposited(clanId, cs.clansmanId, woodDelta, ironDelta, wheatDelta, fishDelta, tick);
./docs/reviews/pr198-codereview-codex-5-5.md:1802:     event ResourcesDeposited(
./docs/reviews/pr198-codereview-codex-5-5.md:2240:+        bytes32 depositedTopic = keccak256("ResourcesDeposited(uint32,uint32,uint256,uint256,uint256,uint256,uint32)");
./docs/reviews/pr198-codereview-codex-5-5.md:2299:+        emit IClanWorldEvents.ResourcesDeposited(clanId, csId, 5e18, 2e18, 1e18, 3e18, 1);
./docs/reviews/pr198-codereview-codex-5-5.md:4440:   532	    event ResourcesDeposited(
./docs/reviews/pr198-codereview-codex-5-5.md:7840:   541	    event ResourcesDeposited(
./docs/reviews/pr194-r2-codereview-codex-5-4.md:918:+        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
./docs/reviews/pr194-r2-codereview-codex-5-4.md:2144:-        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:859:event ResourcesDeposited(uint32 indexed clanId, uint32 indexed clansmanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish);
./packages/contracts/test/ClanWorld.t.sol:534:        bytes32 depositedTopic = keccak256("ResourcesDeposited(uint32,uint32,uint256,uint256,uint256,uint256,uint32)");
./packages/contracts/test/ClanWorld.t.sol:593:        emit IClanWorldEvents.ResourcesDeposited(clanId, csId, 5e18, 2e18, 1e18, 3e18, 1);
./packages/contracts/src/IClanWorld.sol:541:    event ResourcesDeposited(
./packages/contracts/src/ClanWorld.sol:698:        emit ResourcesDeposited(clanId, cs.clansmanId, woodDelta, ironDelta, wheatDelta, fishDelta, tick);

exec
/bin/bash -lc "sed -n '120,180p' packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:

enum WheatPlotState {
    Harvestable,
    Regrowing,
    WinterLocked
}

enum ResourceType {
    Wood,
    Iron,
    Wheat,
    Fish
}

enum ActionType {
    None,
    ChopWood,
    MineIron,
    FishDocks,
    FishDeepSea,
    HarvestWheat,
    DepositResources,
    BuildWall,
    UpgradeBase,
    UpgradeMonument,
    DefendBase,
    MarketBuy,
    MarketSell,
    Wait
}

enum MarketExecutionMode {
    None,
    Immediate,
    Scheduled
}

enum StatusCode {
    OK,
    ERR_CLAN_DEAD,
    ERR_CLAN_NOT_OWNED,
    ERR_CLANSMAN_DEAD,
    ERR_INVALID_CLANSMAN,
    ERR_INVALID_REGION,
    ERR_INVALID_ACTION,
    ERR_INVALID_TARGET,
    ERR_COOLDOWN_ACTIVE,
    ERR_NOT_WAITING,
    ERR_NOT_IN_UNICORN_TOWN,
    ERR_NOT_AT_HOMEBASE,
    ERR_NOT_AT_TARGET_BASE,
    ERR_NOT_DEFENDABLE,
    ERR_MISSING_RESOURCES,
    ERR_EMPTY_CARGO,
    ERR_PLOT_NOT_READY,
    ERR_PLOT_EMPTY,
    ERR_MARKET_ZERO_AMOUNT,
    ERR_MARKET_UNSUPPORTED_TOKEN,
    ERR_IMMEDIATE_MARKET_NOT_ELIGIBLE,
    ERR_MARKET_BUY_OVER_CAPACITY,
    ERR_WORLD_TICK_MISMATCH,

exec
/bin/bash -lc "sed -n '1,80p' packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

/**
 * @title IClanWorld
 * @notice Canonical seam interface for the ClanWorld v4 hackathon build.
 *
 * Synthesized from:
 *   - clanworld_v4_spec.md (mechanics)
 *   - clanworld_v4_1_addendum.md (locked clarifications)
 *   - clanworld_v4_2_state_schema_interface_spec.md (state schema)
 *   - clanworld_v4_3_schema_patch.md (final lockdowns)
 *   - clanworld_v4_4_ui_indexer_getters.md (UI convenience aggregators)
 *
 * This is the contract surface every other workstream depends on.
 * Treat field names, event names, struct layouts, and enum order as STABLE.
 *
 * Implementation lives behind this interface. Mocks and indexer types are
 * derived from it. Do not change without coordinating with all streams.
 */

// =============================================================================
// CONSTANTS
// =============================================================================

library ClanWorldConstants {
    // World cadence
    uint64 internal constant HEARTBEAT_INTERVAL_SECONDS = 60;
    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
    uint64 internal constant WINTER_DURATION_TICKS = 10;
    uint64 internal constant SEASON_DURATION_TICKS = 360;

    // Bandit cadence
    uint64 internal constant BANDIT_COOLDOWN_TICKS = 10;
    uint64 internal constant BANDIT_CAMP_TICKS = 3;
    uint64 internal constant BANDIT_REST_TICKS = 2;
    uint8 internal constant BANDIT_MAX_ATTACK_ATTEMPTS = 6;

    // Clansman cadence
    uint64 internal constant CLANSMAN_COOLDOWN_SECONDS = 60;

    // Carry caps (per clansman)
    uint256 internal constant WOOD_CARRY_CAP = 10e18;
    uint256 internal constant WOOD_CAP = WOOD_CARRY_CAP;
    uint256 internal constant IRON_CAP = 5e18;
    uint256 internal constant WHEAT_CAP = 40e18;
    uint256 internal constant FISH_CAP = 8e18;

    // Gathering yields
    uint256 internal constant WOOD_YIELD_PER_TICK = 1e18;
    uint256 internal constant WOOD_BASE_YIELD = WOOD_YIELD_PER_TICK;
    uint256 internal constant WOOD_CRIT_BONUS = WOOD_YIELD_PER_TICK;
    uint16 internal constant WOOD_CRIT_BPS = 1000; // 10%

    uint256 internal constant IRON_BASE_YIELD = 5e17; // 0.5e18
    uint16 internal constant GOLD_FROM_IRON_BPS = 200; // 2%
    uint256 internal constant GOLD_FROM_IRON_AMOUNT = 1e18;

    uint16 internal constant FISH_DOCKS_BPS = 2500; // 25%
    uint16 internal constant FISH_DEEP_BPS = 7500; // 75%

    // Upkeep
    uint256 internal constant WHEAT_UPKEEP_PER_CLANSMAN = 1e18;
    uint256 internal constant FISH_UPKEEP_PER_CLANSMAN = 1e17; // 0.1
    uint256 internal constant WINTER_WOOD_BURN_PER_BASE = 1e18;
    uint16 internal constant WINTER_UPKEEP_MULTIPLIER_BPS = 20000; // 2x

    // Wheat plots
    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;

    // Bandit combat
    uint16 internal constant BANDIT_BASE_STEAL_BPS = 2000; // 20%
    uint16 internal constant BANDIT_DROP_TO_DEFENDERS_BPS = 5000; // 50%

    // Region IDs (1-indexed; 0 = NOOP / unset sentinel)
    uint8 internal constant REGION_NOOP = 0;
    uint8 internal constant REGION_FOREST = 1;
    uint8 internal constant REGION_MOUNTAINS = 2;
    uint8 internal constant REGION_UNICORN_TOWN = 3;

collab: Wait
codex
The biggest likely defect is now pretty clear: the new OTC entrypoints appear to bypass the engine’s lazy-settlement discipline, while every existing balance-sensitive write path settles first. I’m pulling exact surrounding snippets so I can tie that to concrete file references and separate it from softer spec/API concerns.
exec
/bin/bash -lc "nl -ba docs/reviews/pr200-codereview-codex-5-5.md | sed -n '1860,2215p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1860	         StubPool(_treasury.fishGoldPool).seed(cfg.fishSeed, cfg.goldSeedForFish);
  1861	@@ -1732,10 +1756,349 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
  1862	         );
  1863	     }
  1864	 
  1865	+    function _transferSeed(address token, address pool, uint256 amount) internal {
  1866	+        require(token != address(0) && pool != address(0), "ClanWorld: treasury not init");
  1867	+        require(MinimalERC20(token).transferFrom(msg.sender, pool, amount), "ClanWorld: seed transfer failed");
  1868	+    }
  1869	+
  1870	     // =========================================================================
  1871	     // OTC TRANSFERS
  1872	     // =========================================================================
  1873	 
  1874	+    function proposeGoldTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint256 expiryTick)
  1875	+        external
  1876	+        override
  1877	+        nonReentrant
  1878	+        returns (uint256 proposalId)
  1879	+    {
  1880	+        Clan storage fromClan = _clans[fromClanId];
  1881	+        require(fromClan.clanId != 0, "ClanWorld: clan not found");
  1882	+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1883	+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
  1884	+        require(_clans[toClanId].clanId != 0, "ClanWorld: target clan not found");
  1885	+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1886	+        require(expiryTick <= type(uint64).max, "ClanWorld: expiry overflow");
  1887	+        if (fromClan.goldBalance < amount) revert("ERR_NOT_ENOUGH_GOLD");
  1888	+
  1889	+        proposalId = _nextOtcProposalId++;
  1890	+        _otcGoldProposals[proposalId] = OtcProposal({
  1891	+            from: fromClanId,
  1892	+            to: toClanId,
  1893	+            amount: amount,
  1894	+            expiryTick: uint64(expiryTick),
  1895	+            accepted: false,
  1896	+            cancelled: false
  1897	+        });
  1898	+
  1899	+        emit GoldTransferProposed(proposalId, fromClanId, toClanId, amount, expiryTick);
  1900	+    }
  1901	+
  1902	+    function acceptGoldTransfer(uint256 proposalId) external override nonReentrant {
  1903	+        OtcProposal storage proposal = _otcGoldProposals[proposalId];
  1904	+        require(proposal.from != 0, "ClanWorld: proposal not found");
  1905	+        require(!proposal.accepted, "ClanWorld: proposal accepted");
  1906	+        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
  1907	+        require(_world.currentTick <= proposal.expiryTick, "ClanWorld: proposal expired");
  1908	+
  1909	+        Clan storage fromClan = _clans[proposal.from];
  1910	+        Clan storage toClan = _clans[proposal.to];
  1911	+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1912	+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1913	+        require(toClan.owner == msg.sender, "ClanWorld: not clan owner");
  1914	+        if (fromClan.goldBalance < proposal.amount) revert("ERR_NOT_ENOUGH_GOLD");
  1915	+
  1916	+        fromClan.goldBalance -= proposal.amount;
  1917	+        toClan.goldBalance += proposal.amount;
  1918	+        proposal.accepted = true;
  1919	+
  1920	+        emit GoldTransferAccepted(proposalId, proposal.from, proposal.to, proposal.amount, _world.currentTick);
  1921	+    }
  1922	+
  1923	+    function cancelGoldTransfer(uint256 proposalId) external override nonReentrant {
  1924	+        OtcProposal storage proposal = _otcGoldProposals[proposalId];
  1925	+        require(proposal.from != 0, "ClanWorld: proposal not found");
  1926	+        require(!proposal.accepted, "ClanWorld: proposal accepted");
  1927	+        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
  1928	+
  1929	+        Clan storage fromClan = _clans[proposal.from];
  1930	+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
  1931	+
  1932	+        proposal.cancelled = true;
  1933	+        emit GoldTransferCancelled(proposalId);
  1934	+    }
  1935	+
  1936	+    function proposeVaultTransfer(
  1937	+        uint32 fromClanId,
  1938	+        uint32 toClanId,
  1939	+        uint256 woodAmt,
  1940	+        uint256 wheatAmt,
  1941	+        uint256 fishAmt,
  1942	+        uint256 ironAmt,
  1943	+        uint64 expiryTick
  1944	+    ) external override nonReentrant returns (uint256 proposalId) {
  1945	+        Clan storage fromClan = _clans[fromClanId];
  1946	+        require(fromClan.clanId != 0, "ClanWorld: clan not found");
  1947	+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1948	+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
  1949	+        require(_clans[toClanId].clanId != 0, "ClanWorld: target clan not found");
  1950	+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1951	+        if (!_hasVaultResources(fromClan, woodAmt, wheatAmt, fishAmt, ironAmt)) {
  1952	+            revert("ERR_NOT_ENOUGH_RESOURCES");
  1953	+        }
  1954	+
  1955	+        proposalId = _nextOtcProposalId++;
  1956	+        _otcVaultTransferProposals[proposalId] = VaultTransferProposal({
  1957	+            from: fromClanId,
  1958	+            to: toClanId,
  1959	+            wood: woodAmt,
  1960	+            wheat: wheatAmt,
  1961	+            fish: fishAmt,
  1962	+            iron: ironAmt,
  1963	+            expiryTick: expiryTick,
  1964	+            accepted: false,
  1965	+            cancelled: false
  1966	+        });
  1967	+
  1968	+        emit VaultTransferProposed(proposalId, fromClanId, toClanId, woodAmt, wheatAmt, fishAmt, ironAmt, expiryTick);
  1969	+    }
  1970	+
  1971	+    function acceptVaultTransfer(uint256 proposalId) external override nonReentrant {
  1972	+        VaultTransferProposal storage proposal = _otcVaultTransferProposals[proposalId];
  1973	+        require(proposal.from != 0, "ClanWorld: proposal not found");
  1974	+        require(!proposal.accepted, "ClanWorld: proposal accepted");
  1975	+        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
  1976	+        require(_world.currentTick <= proposal.expiryTick, "ClanWorld: proposal expired");
  1977	+
  1978	+        Clan storage fromClan = _clans[proposal.from];
  1979	+        Clan storage toClan = _clans[proposal.to];
  1980	+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1981	+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  1982	+        require(toClan.owner == msg.sender, "ClanWorld: not clan owner");
  1983	+        if (!_hasVaultResources(fromClan, proposal.wood, proposal.wheat, proposal.fish, proposal.iron)) {
  1984	+            revert("ERR_NOT_ENOUGH_RESOURCES");
  1985	+        }
  1986	+
  1987	+        fromClan.vaultWood -= proposal.wood;
  1988	+        fromClan.vaultWheat -= proposal.wheat;
  1989	+        fromClan.vaultFish -= proposal.fish;
  1990	+        fromClan.vaultIron -= proposal.iron;
  1991	+        toClan.vaultWood += proposal.wood;
  1992	+        toClan.vaultWheat += proposal.wheat;
  1993	+        toClan.vaultFish += proposal.fish;
  1994	+        toClan.vaultIron += proposal.iron;
  1995	+        proposal.accepted = true;
  1996	+
  1997	+        emit VaultTransferAccepted(
  1998	+            proposalId,
  1999	+            proposal.from,
  2000	+            proposal.to,
  2001	+            proposal.wood,
  2002	+            proposal.wheat,
  2003	+            proposal.fish,
  2004	+            proposal.iron,
  2005	+            _world.currentTick
  2006	+        );
  2007	+    }
  2008	+
  2009	+    function cancelVaultTransfer(uint256 proposalId) external override nonReentrant {
  2010	+        VaultTransferProposal storage proposal = _otcVaultTransferProposals[proposalId];
  2011	+        require(proposal.from != 0, "ClanWorld: proposal not found");
  2012	+        require(!proposal.accepted, "ClanWorld: proposal accepted");
  2013	+        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
  2014	+
  2015	+        Clan storage fromClan = _clans[proposal.from];
  2016	+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
  2017	+
  2018	+        proposal.cancelled = true;
  2019	+        emit VaultTransferCancelled(proposalId);
  2020	+    }
  2021	+
  2022	+    function _hasVaultResources(Clan storage clan, uint256 wood, uint256 wheat, uint256 fish, uint256 iron)
  2023	+        private
  2024	+        view
  2025	+        returns (bool)
  2026	+    {
  2027	+        return clan.vaultWood >= wood && clan.vaultWheat >= wheat && clan.vaultFish >= fish && clan.vaultIron >= iron;
  2028	+    }
  2029	+
  2030	+    function proposeBlueprintTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint64 expiryTick)
  2031	+        external
  2032	+        override
  2033	+        nonReentrant
  2034	+        returns (uint256 proposalId)
  2035	+    {
  2036	+        Clan storage fromClan = _clans[fromClanId];
  2037	+        require(fromClan.clanId != 0, "ClanWorld: clan not found");
  2038	+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  2039	+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
  2040	+        require(_clans[toClanId].clanId != 0, "ClanWorld: target clan not found");
  2041	+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  2042	+        if (fromClan.blueprintBalance < amount) revert("ERR_NOT_ENOUGH_BLUEPRINT");
  2043	+
  2044	+        proposalId = _nextOtcProposalId++;
  2045	+        _otcBlueprintTransferProposals[proposalId] = BlueprintTransferProposal({
  2046	+            from: fromClanId, to: toClanId, amount: amount, expiryTick: expiryTick, accepted: false, cancelled: false
  2047	+        });
  2048	+
  2049	+        emit BlueprintTransferProposed(proposalId, fromClanId, toClanId, amount, expiryTick);
  2050	+    }
  2051	+
  2052	+    function acceptBlueprintTransfer(uint256 proposalId) external override nonReentrant {
  2053	+        BlueprintTransferProposal storage proposal = _otcBlueprintTransferProposals[proposalId];
  2054	+        require(proposal.from != 0, "ClanWorld: proposal not found");
  2055	+        require(!proposal.accepted, "ClanWorld: proposal accepted");
  2056	+        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
  2057	+        require(_world.currentTick <= proposal.expiryTick, "ClanWorld: proposal expired");
  2058	+
  2059	+        Clan storage fromClan = _clans[proposal.from];
  2060	+        Clan storage toClan = _clans[proposal.to];
  2061	+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  2062	+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  2063	+        require(toClan.owner == msg.sender, "ClanWorld: not clan owner");
  2064	+        if (fromClan.blueprintBalance < proposal.amount) revert("ERR_NOT_ENOUGH_BLUEPRINT");
  2065	+
  2066	+        fromClan.blueprintBalance -= proposal.amount;
  2067	+        toClan.blueprintBalance += proposal.amount;
  2068	+        proposal.accepted = true;
  2069	+
  2070	+        emit BlueprintTransferAccepted(proposalId, proposal.from, proposal.to, proposal.amount, _world.currentTick);
  2071	+    }
  2072	+
  2073	+    function cancelBlueprintTransfer(uint256 proposalId) external override nonReentrant {
  2074	+        BlueprintTransferProposal storage proposal = _otcBlueprintTransferProposals[proposalId];
  2075	+        require(proposal.from != 0, "ClanWorld: proposal not found");
  2076	+        require(!proposal.accepted, "ClanWorld: proposal accepted");
  2077	+        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
  2078	+
  2079	+        Clan storage fromClan = _clans[proposal.from];
  2080	+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
  2081	+
  2082	+        proposal.cancelled = true;
  2083	+        emit BlueprintTransferCancelled(proposalId);
  2084	+    }
  2085	+
  2086	+    function proposeBundledTransfer(
  2087	+        uint32 fromClanId,
  2088	+        uint32 toClanId,
  2089	+        uint256 gold,
  2090	+        uint256 wood,
  2091	+        uint256 wheat,
  2092	+        uint256 fish,
  2093	+        uint256 iron,
  2094	+        uint256 blueprint,
  2095	+        uint64 expiryTick
  2096	+    ) external override nonReentrant returns (uint256 proposalId) {
  2097	+        Clan storage fromClan = _clans[fromClanId];
  2098	+        require(fromClan.clanId != 0, "ClanWorld: clan not found");
  2099	+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  2100	+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
  2101	+        require(_clans[toClanId].clanId != 0, "ClanWorld: target clan not found");
  2102	+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  2103	+        require(!_isEmptyBundledTransfer(gold, wood, wheat, fish, iron, blueprint), "ClanWorld: empty bundled transfer");
  2104	+        _requireBundledTransferBalance(fromClan, gold, wood, wheat, fish, iron, blueprint);
  2105	+
  2106	+        proposalId = _nextOtcProposalId++;
  2107	+        _otcBundledTransferProposals[proposalId] = BundledTransferProposal({
  2108	+            from: fromClanId,
  2109	+            to: toClanId,
  2110	+            gold: gold,
  2111	+            wood: wood,
  2112	+            wheat: wheat,
  2113	+            fish: fish,
  2114	+            iron: iron,
  2115	+            blueprint: blueprint,
  2116	+            expiryTick: expiryTick,
  2117	+            accepted: false,
  2118	+            cancelled: false
  2119	+        });
  2120	+
  2121	+        emit BundledTransferProposed(
  2122	+            proposalId, fromClanId, toClanId, gold, wood, wheat, fish, iron, blueprint, expiryTick
  2123	+        );
  2124	+    }
  2125	+
  2126	+    function acceptBundledTransfer(uint256 proposalId) external override nonReentrant {
  2127	+        BundledTransferProposal storage proposal = _otcBundledTransferProposals[proposalId];
  2128	+        require(proposal.from != 0, "ClanWorld: proposal not found");
  2129	+        require(!proposal.accepted, "ClanWorld: proposal accepted");
  2130	+        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
  2131	+        require(_world.currentTick <= proposal.expiryTick, "ClanWorld: proposal expired");
  2132	+
  2133	+        Clan storage fromClan = _clans[proposal.from];
  2134	+        Clan storage toClan = _clans[proposal.to];
  2135	+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  2136	+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
  2137	+        require(toClan.owner == msg.sender, "ClanWorld: not clan owner");
  2138	+        _requireBundledTransferBalance(
  2139	+            fromClan, proposal.gold, proposal.wood, proposal.wheat, proposal.fish, proposal.iron, proposal.blueprint
  2140	+        );
  2141	+
  2142	+        fromClan.goldBalance -= proposal.gold;
  2143	+        fromClan.vaultWood -= proposal.wood;
  2144	+        fromClan.vaultWheat -= proposal.wheat;
  2145	+        fromClan.vaultFish -= proposal.fish;
  2146	+        fromClan.vaultIron -= proposal.iron;
  2147	+        fromClan.blueprintBalance -= proposal.blueprint;
  2148	+        toClan.goldBalance += proposal.gold;
  2149	+        toClan.vaultWood += proposal.wood;
  2150	+        toClan.vaultWheat += proposal.wheat;
  2151	+        toClan.vaultFish += proposal.fish;
  2152	+        toClan.vaultIron += proposal.iron;
  2153	+        toClan.blueprintBalance += proposal.blueprint;
  2154	+        proposal.accepted = true;
  2155	+
  2156	+        emit BundledTransferAccepted(
  2157	+            proposalId,
  2158	+            proposal.from,
  2159	+            proposal.to,
  2160	+            proposal.gold,
  2161	+            proposal.wood,
  2162	+            proposal.wheat,
  2163	+            proposal.fish,
  2164	+            proposal.iron,
  2165	+            proposal.blueprint,
  2166	+            _world.currentTick
  2167	+        );
  2168	+    }
  2169	+
  2170	+    function cancelBundledTransfer(uint256 proposalId) external override nonReentrant {
  2171	+        BundledTransferProposal storage proposal = _otcBundledTransferProposals[proposalId];
  2172	+        require(proposal.from != 0, "ClanWorld: proposal not found");
  2173	+        require(!proposal.accepted, "ClanWorld: proposal accepted");
  2174	+        require(!proposal.cancelled, "ClanWorld: proposal cancelled");
  2175	+
  2176	+        Clan storage fromClan = _clans[proposal.from];
  2177	+        require(fromClan.owner == msg.sender, "ClanWorld: not clan owner");
  2178	+
  2179	+        proposal.cancelled = true;
  2180	+        emit BundledTransferCancelled(proposalId);
  2181	+    }
  2182	+
  2183	+    function _isEmptyBundledTransfer(
  2184	+        uint256 gold,
  2185	+        uint256 wood,
  2186	+        uint256 wheat,
  2187	+        uint256 fish,
  2188	+        uint256 iron,
  2189	+        uint256 blueprint
  2190	+    ) private pure returns (bool) {
  2191	+        return gold == 0 && wood == 0 && wheat == 0 && fish == 0 && iron == 0 && blueprint == 0;
  2192	+    }
  2193	+
  2194	+    function _requireBundledTransferBalance(
  2195	+        Clan storage clan,
  2196	+        uint256 gold,
  2197	+        uint256 wood,
  2198	+        uint256 wheat,
  2199	+        uint256 fish,
  2200	+        uint256 iron,
  2201	+        uint256 blueprint
  2202	+    ) private view {
  2203	+        if (clan.goldBalance < gold) revert("ERR_NOT_ENOUGH_GOLD");
  2204	+        if (!_hasVaultResources(clan, wood, wheat, fish, iron)) revert("ERR_NOT_ENOUGH_RESOURCES");
  2205	+        if (clan.blueprintBalance < blueprint) revert("ERR_NOT_ENOUGH_BLUEPRINT");
  2206	+    }
  2207	+
  2208	     function transferGold(uint32, uint32, uint256) external pure override {
  2209	         revert("OTC transfers not implemented");
  2210	     }
  2211	@@ -1768,6 +2131,30 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
  2212	         return _treasury;
  2213	     }
  2214	 
  2215	+    function getResourceToken(uint8 resourceType) external view override returns (address) {

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '1080,1125p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1080	    /// @notice Submit one or more orders for a clan's clansmen. Per-order failures don't revert.
  1081	    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
  1082	        external
  1083	        override
  1084	        nonReentrant
  1085	        returns (OrderResult[] memory results)
  1086	    {
  1087	        Clan storage clan = _clans[clanId];
  1088	        require(clan.clanId != 0, "ClanWorld: clan not found");
  1089	        require(clan.owner == msg.sender, "ClanWorld: not clan owner");
  1090	        require(clan.clanState == ClanState.ACTIVE, "ClanWorld: clan dead");
  1091	
  1092	        // Guard: if clan is more than 200 ticks behind, caller must call settleClan() first
  1093	        // (_settleClan caps at 200 ticks per call; submitting into a partially-settled clan corrupts invariants)
  1094	        // ERR_MUST_SETTLE_FIRST not in StatusCode enum — using ERR_INVALID_ACTION as the closest proxy
  1095	        {
  1096	            uint64 lastSettled = _clans[clanId].lastSettledTick;
  1097	            if (_world.currentTick > lastSettled + 200) {
  1098	                results = new OrderResult[](orders.length);
  1099	                for (uint256 i = 0; i < orders.length; i++) {
  1100	                    results[i] = OrderResult({
  1101	                        clansmanId: orders[i].clansmanId,
  1102	                        status: StatusCode.ERR_INVALID_ACTION,
  1103	                        cooldownEndsAtTs: 0,
  1104	                        missionNonce: 0,
  1105	                        marketMode: MarketExecutionMode.None
  1106	                    });
  1107	                }
  1108	                return results;
  1109	            }
  1110	        }
  1111	
  1112	        // Lazy settle before processing orders
  1113	        _settleClan(clanId);
  1114	
  1115	        results = new OrderResult[](orders.length);
  1116	
  1117	        for (uint256 i = 0; i < orders.length; i++) {
  1118	            results[i] = _processOrder(clanId, clan, orders[i]);
  1119	        }
  1120	
  1121	        return results;
  1122	    }
  1123	
  1124	    struct OrderCtx {
  1125	        uint8 gotoRegion;

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '354,395p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   354	    function _settleClan(uint32 clanId) internal {
   355	        Clan storage clan = _clans[clanId];
   356	        if (clan.clanId == 0) return;
   357	
   358	        uint64 curTick = _world.currentTick;
   359	        uint64 fromTick = clan.lastSettledTick;
   360	        if (fromTick >= curTick) return;
   361	
   362	        // Cap ticks settled per call to prevent block gas limit issues
   363	        uint64 maxSettleTicks = 200;
   364	        if (curTick > fromTick + maxSettleTicks) {
   365	            curTick = fromTick + maxSettleTicks;
   366	        }
   367	
   368	        uint32[] storage clansmanIds = _clanClansmanIds[clanId];
   369	
   370	        // Settle tick by tick from fromTick to curTick - 1
   371	        // (curTick is still open; we settle through the last closed tick)
   372	        for (uint64 tick = fromTick; tick < curTick; tick++) {
   373	            // 1. Apply upkeep for this tick
   374	            _applyUpkeep(clan, tick);
   375	
   376	            // 2. Wheat plot regrow check (lazy, per tick)
   377	            for (uint256 pi = 0; pi < 2; pi++) {
   378	                WheatPlot storage plot = _wheatPlots[clanId][pi];
   379	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
   380	                    plot.state = WheatPlotState.Harvestable;
   381	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
   382	                    plot.regrowUntilTick = 0;
   383	                }
   384	            }
   385	
   386	            // 3. Advance each clansman (single-tick range: [tick, tick+1))
   387	            for (uint256 i = 0; i < clansmanIds.length; i++) {
   388	                Clansman storage cs = _clansmen[clansmanIds[i]];
   389	                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
   390	            }
   391	        }
   392	
   393	        clan.lastSettledTick = curTick;
   394	        emit ClanSettled(clanId, curTick);
   395	    }

collab: Wait
exec
/bin/bash -lc "nl -ba docs/planning/clanworld_v4_2_state_schema_interface_spec.md | sed -n '636,690p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   636	- `submitClanOrders(clanId, ...)`
   637	  - only clan owner or approved operator
   638	- `transferGold(...)`, `transferVaultResource(...)`, `transferBlueprint(...)`, `transferBundle(...)`
   639	  - only sender clan owner or approved operator
   640	- `mintClan(...)`
   641	  - public payable if minting is enabled
   642	- `seedPools(...)`
   643	  - only owner/admin
   644	- `finalizeSeason()`
   645	  - permissionless after season end, because outcome is deterministic
   646	- `heartbeat()`
   647	  - permissionless, but rate-limited by `nextHeartbeatAtTs`
   648	
   649	### 10.2 Core write functions
   650	
   651	```solidity
   652	function heartbeat() external;
   653	function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders) external returns (OrderResult[] memory);
   654	function settleClan(uint32 clanId) external;
   655	function mintClan(address to) external payable returns (uint32 clanId, uint256 iftTokenId);
   656	function seedPools(PoolSeedConfig calldata cfg) external;
   657	function finalizeSeason() external;
   658	```
   659	
   660	### 10.3 OTC transfer surface
   661	
   662	Internal accounting requires explicit transfer functions.
   663	
   664	```solidity
   665	function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
   666	function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
   667	function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
   668	function transferBundle(
   669	    uint32 fromClanId,
   670	    uint32 toClanId,
   671	    uint256 gold,
   672	    uint256 blueprint,
   673	    uint256 wood,
   674	    uint256 iron,
   675	    uint256 wheat,
   676	    uint256 fish
   677	) external;
   678	```
   679	
   680	OTC transfers may draw only from:
   681	- clan vault resources
   682	- clan gold purse
   683	- clan blueprint balance
   684	
   685	OTC transfers may **not** draw from worker carry balances.
   686	
   687	### 10.4 ClanOrder
   688	
   689	```solidity
   690	struct ClanOrder {

exec
/bin/bash -lc "nl -ba docs/planning/V1/01\\ Blockchain\\ Game\\ Spec/clanworld_v4_3_schema_patch.md | sed -n '298,310p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   298	## M. Dead Clan OTC Restriction (explicit restatement)
   299	
   300	### M.1 Dead sender restriction
   301	All OTC transfer entrypoints must require sender clan state == `ACTIVE`:
   302	
   303	- `transferGold`
   304	- `transferVaultResource`
   305	- `transferBlueprint`
   306	- `transferBundle`
   307	
   308	Dead clans may not initiate outbound value transfer.
   309	
   310	---

exec
/bin/bash -lc "nl -ba docs/planning/clanworld_numbered_implementation_plan.md | sed -n '311,330p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   311	## Phase 7 — OTC transfer surface
   312	
   313	### 7.1 Implement gold transfer
   314	- clan purse -> clan purse
   315	
   316	### 7.2 Implement vault resource transfer
   317	- clan vault -> clan vault
   318	
   319	### 7.3 Implement blueprint transfer
   320	- clan blueprint -> clan blueprint
   321	
   322	### 7.4 Implement bundled transfer convenience
   323	Optional:
   324	- one call for mixed payment
   325	
   326	### 7.5 Restrict dead clans
   327	Dead clans cannot initiate transfers.
   328	
   329	**Definition of done**
   330	- mercenary / alliance payments have a real contract surface

collab: Wait
codex
The spec references are pinned now. I’m just waiting on the two parallel reviewers to see whether they found anything beyond the lazy-settlement hole and the still-reverting canonical transfer surface, then I’ll collapse everything into the final severity-ordered review.
collab: Wait
collab: Wait
collab: Wait
exec
/bin/bash -lc "nl -ba docs/planning/clanworld_v4_spec.md | sed -n '408,420p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   408	
   409	Carried resources do **not** count until deposited.
   410	
   411	## 4.6 Deposit rule
   412	A clansman must be at their own homebase and perform `deposit_resources` for carried resources to enter the clan vault.
   413	
   414	## 4.7 Gathering yields
   415	
   416	### Wood (Forest)
   417	- base yield = `2e18` wood per action tick
   418	- critical chance = `20%`
   419	- critical bonus = `+1e18` wood
   420	- total per tick = `2e18` or `3e18`

exec
/bin/bash -lc "nl -ba docs/planning/clanworld_v4_spec.md | sed -n '592,606p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   592	
   593	## 5.10 Failed market actions
   594	If a scheduled or immediate market action fails for reasons other than slippage, such as malformed params or unavailable balances:
   595	- no partial swap occurs
   596	- clansman retains their inventory or balances
   597	- clansman becomes or remains `WAITING` in Unicorn Town
   598	
   599	## 5.11 Initial pool seeding
   600	At deploy time, a treasury or deployer-controlled bootstrap address mints and seeds the initial resource and gold liquidity.
   601	
   602	### Working initial seed ratios
   603	- Wood / Gold: `1000 wood : 500 gold`
   604	- Wheat / Gold: `1000 wheat : 700 gold`
   605	- Fish / Gold: `500 fish : 600 gold`
   606	- Iron / Gold: `250 iron : 800 gold`

exec
/bin/bash -lc "nl -ba packages/contracts/test/GoldTransferOtc.t.sol | sed -n '1,170p'" in /home/claude/code/clan-world
 succeeded in 0ms:
nl: packages/contracts/test/GoldTransferOtc.t.sol: No such file or directory

exec
/bin/bash -lc "nl -ba packages/contracts/test/VaultTransferOtc.t.sol | sed -n '1,190p'" in /home/claude/code/clan-world
 succeeded in 0ms:
nl: packages/contracts/test/VaultTransferOtc.t.sol: No such file or directory

exec
/bin/bash -lc "nl -ba packages/contracts/test/BlueprintTransferOtc.t.sol | sed -n '1,170p'" in /home/claude/code/clan-world
 succeeded in 0ms:
nl: packages/contracts/test/BlueprintTransferOtc.t.sol: No such file or directory

exec
/bin/bash -lc "nl -ba packages/contracts/test/BundledTransferOtc.t.sol | sed -n '1,235p'" in /home/claude/code/clan-world
 succeeded in 0ms:
nl: packages/contracts/test/BundledTransferOtc.t.sol: No such file or directory

exec
/bin/bash -lc "nl -ba packages/contracts/src/IClanWorld.sol | sed -n '620,700p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   620	
   621	    // ----- winter cold damage -----
   622	    event ColdDamageApplied(uint32 indexed clanId, uint16 oldDamage, uint16 newDamage, uint64 atTick);
   623	    event ClansmanDiedFromCold(uint32 indexed clanId, uint64 atTick);
   624	
   625	    // ----- OTC transfers -----
   626	    event GoldTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
   627	    event VaultResourceTransferred(
   628	        uint32 indexed fromClanId, uint32 indexed toClanId, ResourceType resource, uint256 amount, uint64 atTick
   629	    );
   630	    event BlueprintTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
   631	
   632	    // ----- treasury / pools -----
   633	    event PoolsSeeded(address woodGoldPool, address wheatGoldPool, address fishGoldPool, address ironGoldPool);
   634	}
   635	
   636	// =============================================================================
   637	// MAIN INTERFACE
   638	// =============================================================================
   639	
   640	interface IClanWorld is IClanWorldEvents {
   641	    // -------------------------------------------------------------------------
   642	    // World progression
   643	    // -------------------------------------------------------------------------
   644	
   645	    /// @notice Permissionless heartbeat. Closes the current tick, resolves
   646	    ///         scheduled market actions and world events, advances the tick.
   647	    ///         Rate-limited by WorldState.nextHeartbeatAtTs.
   648	    function heartbeat() external;
   649	
   650	    /// @notice Lazily settle a clan forward to current tick. Idempotent.
   651	    function settleClan(uint32 clanId) external;
   652	
   653	    /// @notice Lazily settle a single clansman's mission to current tick. Idempotent.
   654	    function settleClansman(uint32 csId) external;
   655	
   656	    /// @notice Finalize the current season. Permissionless after seasonEndTick.
   657	    function finalizeSeason() external;
   658	
   659	    // -------------------------------------------------------------------------
   660	    // Clan lifecycle
   661	    // -------------------------------------------------------------------------
   662	
   663	    /// @notice Mint a new clan iNFT and spawn its homebase in a valid region.
   664	    function mintClan(address to) external returns (uint32 clanId, uint256 iftTokenId);
   665	
   666	    /// @notice Submit one or more orders for a single clan's clansmen.
   667	    ///         Per-order failures do not revert the tx.
   668	    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders) external returns (OrderResult[] memory);
   669	
   670	    // -------------------------------------------------------------------------
   671	    // Treasury / pool seeding
   672	    // -------------------------------------------------------------------------
   673	
   674	    /// @notice Owner-only. Registers token and pool addresses once before seeding.
   675	    ///         tokens order: wood, iron, wheat, fish, gold, blueprint.
   676	    ///         pools order: wood, wheat, fish, iron.
   677	    function initTreasury(address[6] calldata tokens, address[4] calldata pools) external;
   678	
   679	    /// @notice Owner-only. Seeds the four Unicorn Town pools at deploy time.
   680	    function seedPools(PoolSeedConfig calldata cfg) external;
   681	
   682	    // -------------------------------------------------------------------------
   683	    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
   684	    // -------------------------------------------------------------------------
   685	
   686	    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
   687	
   688	    function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
   689	
   690	    function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
   691	
   692	    function transferBundle(
   693	        uint32 fromClanId,
   694	        uint32 toClanId,
   695	        uint256 gold,
   696	        uint256 blueprint,
   697	        uint256 wood,
   698	        uint256 iron,
   699	        uint256 wheat,
   700	        uint256 fish

exec
/bin/bash -lc "nl -ba docs/reviews/pr200-codereview-codex-5-5.md | sed -n '2520,2695p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  2520	+    uint64 expiryTick;
  2521	+    bool accepted;
  2522	+    bool cancelled;
  2523	+}
  2524	+
  2525	 struct DefenseContribution {
  2526	     uint32 clansmanId;
  2527	     uint32 clanId;
  2528	@@ -533,11 +579,11 @@ interface IClanWorldEvents {
  2529	     event ResourcesDeposited(
  2530	         uint32 indexed clanId,
  2531	         uint32 indexed clansmanId,
  2532	-        uint256 wood,
  2533	-        uint256 iron,
  2534	-        uint256 wheat,
  2535	-        uint256 fish,
  2536	-        uint64 atTick
  2537	+        uint256 woodDelta,
  2538	+        uint256 ironDelta,
  2539	+        uint256 wheatDelta,
  2540	+        uint256 fishDelta,
  2541	+        uint32 tick
  2542	     );
  2543	 
  2544	     // ----- building -----
  2545	@@ -576,6 +622,8 @@ interface IClanWorldEvents {
  2546	         uint256 maxGoldIn
  2547	     );
  2548	     event MarketActionFailed(uint32 indexed clanId, uint32 indexed clansmanId, ActionType action, StatusCode reason);
  2549	+    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
  2550	+    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);
  2551	 
  2552	     // ----- bandits -----
  2553	     event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
  2554	@@ -614,6 +662,82 @@ interface IClanWorldEvents {
  2555	     event ClansmanDiedFromCold(uint32 indexed clanId, uint64 atTick);
  2556	 
  2557	     // ----- OTC transfers -----
  2558	+    event GoldTransferProposed(
  2559	+        uint256 indexed proposalId,
  2560	+        uint32 indexed fromClanId,
  2561	+        uint32 indexed toClanId,
  2562	+        uint256 amount,
  2563	+        uint256 expiryTick
  2564	+    );
  2565	+    event GoldTransferAccepted(
  2566	+        uint256 indexed proposalId,
  2567	+        uint32 indexed fromClanId,
  2568	+        uint32 indexed toClanId,
  2569	+        uint256 amount,
  2570	+        uint64 settledAtTick
  2571	+    );
  2572	+    event GoldTransferCancelled(uint256 indexed proposalId);
  2573	+    event VaultTransferProposed(
  2574	+        uint256 indexed proposalId,
  2575	+        uint32 indexed fromClanId,
  2576	+        uint32 indexed toClanId,
  2577	+        uint256 wood,
  2578	+        uint256 wheat,
  2579	+        uint256 fish,
  2580	+        uint256 iron,
  2581	+        uint64 expiryTick
  2582	+    );
  2583	+    event VaultTransferAccepted(
  2584	+        uint256 indexed proposalId,
  2585	+        uint32 indexed fromClanId,
  2586	+        uint32 indexed toClanId,
  2587	+        uint256 wood,
  2588	+        uint256 wheat,
  2589	+        uint256 fish,
  2590	+        uint256 iron,
  2591	+        uint64 settledAtTick
  2592	+    );
  2593	+    event VaultTransferCancelled(uint256 indexed proposalId);
  2594	+    event BlueprintTransferProposed(
  2595	+        uint256 indexed proposalId,
  2596	+        uint32 indexed fromClanId,
  2597	+        uint32 indexed toClanId,
  2598	+        uint256 amount,
  2599	+        uint64 expiryTick
  2600	+    );
  2601	+    event BlueprintTransferAccepted(
  2602	+        uint256 indexed proposalId,
  2603	+        uint32 indexed fromClanId,
  2604	+        uint32 indexed toClanId,
  2605	+        uint256 amount,
  2606	+        uint64 settledAtTick
  2607	+    );
  2608	+    event BlueprintTransferCancelled(uint256 indexed proposalId);
  2609	+    event BundledTransferProposed(
  2610	+        uint256 indexed proposalId,
  2611	+        uint32 indexed fromClanId,
  2612	+        uint32 indexed toClanId,
  2613	+        uint256 gold,
  2614	+        uint256 wood,
  2615	+        uint256 wheat,
  2616	+        uint256 fish,
  2617	+        uint256 iron,
  2618	+        uint256 blueprint,
  2619	+        uint64 expiryTick
  2620	+    );
  2621	+    event BundledTransferAccepted(
  2622	+        uint256 indexed proposalId,
  2623	+        uint32 indexed fromClanId,
  2624	+        uint32 indexed toClanId,
  2625	+        uint256 gold,
  2626	+        uint256 wood,
  2627	+        uint256 wheat,
  2628	+        uint256 fish,
  2629	+        uint256 iron,
  2630	+        uint256 blueprint,
  2631	+        uint64 settledAtTick
  2632	+    );
  2633	+    event BundledTransferCancelled(uint256 indexed proposalId);
  2634	     event GoldTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
  2635	     event VaultResourceTransferred(
  2636	         uint32 indexed fromClanId, uint32 indexed toClanId, ResourceType resource, uint256 amount, uint64 atTick
  2637	@@ -674,6 +798,52 @@ interface IClanWorld is IClanWorldEvents {
  2638	     // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
  2639	     // -------------------------------------------------------------------------
  2640	 
  2641	+    function proposeGoldTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint256 expiryTick)
  2642	+        external
  2643	+        returns (uint256 proposalId);
  2644	+
  2645	+    function acceptGoldTransfer(uint256 proposalId) external;
  2646	+
  2647	+    function cancelGoldTransfer(uint256 proposalId) external;
  2648	+
  2649	+    function proposeVaultTransfer(
  2650	+        uint32 fromClanId,
  2651	+        uint32 toClanId,
  2652	+        uint256 woodAmt,
  2653	+        uint256 wheatAmt,
  2654	+        uint256 fishAmt,
  2655	+        uint256 ironAmt,
  2656	+        uint64 expiryTick
  2657	+    ) external returns (uint256 proposalId);
  2658	+
  2659	+    function acceptVaultTransfer(uint256 proposalId) external;
  2660	+
  2661	+    function cancelVaultTransfer(uint256 proposalId) external;
  2662	+
  2663	+    function proposeBlueprintTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint64 expiryTick)
  2664	+        external
  2665	+        returns (uint256 proposalId);
  2666	+
  2667	+    function acceptBlueprintTransfer(uint256 proposalId) external;
  2668	+
  2669	+    function cancelBlueprintTransfer(uint256 proposalId) external;
  2670	+
  2671	+    function proposeBundledTransfer(
  2672	+        uint32 fromClanId,
  2673	+        uint32 toClanId,
  2674	+        uint256 gold,
  2675	+        uint256 wood,
  2676	+        uint256 wheat,
  2677	+        uint256 fish,
  2678	+        uint256 iron,
  2679	+        uint256 blueprint,
  2680	+        uint64 expiryTick
  2681	+    ) external returns (uint256 proposalId);
  2682	+
  2683	+    function acceptBundledTransfer(uint256 proposalId) external;
  2684	+
  2685	+    function cancelBundledTransfer(uint256 proposalId) external;
  2686	+
  2687	     function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
  2688	 
  2689	     function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
  2690	@@ -699,6 +869,12 @@ interface IClanWorld is IClanWorldEvents {
  2691	 
  2692	     function getTreasuryState() external view returns (TreasuryState memory);
  2693	 
  2694	+    function getResourceToken(uint8 resourceType) external view returns (address);
  2695	+

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorldStub.sol | sed -n '110,130p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   110	        returns (OrderResult[] memory results)
   111	    {
   112	        results = new OrderResult[](orders.length);
   113	    }
   114	
   115	    // -------------------------------------------------------------------------
   116	    // Treasury
   117	    // -------------------------------------------------------------------------
   118	
   119	    function initTreasury(address[6] calldata, address[4] calldata) external override {}
   120	
   121	    function seedPools(PoolSeedConfig calldata) external override {}
   122	
   123	    // -------------------------------------------------------------------------
   124	    // OTC transfers
   125	    // -------------------------------------------------------------------------
   126	
   127	    function transferGold(uint32, uint32, uint256) external override {}
   128	
   129	    function transferVaultResource(uint32, uint32, ResourceType, uint256) external override {}
   130	

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '2114,2134p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  2114	                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
  2115	                    count++;
  2116	                }
  2117	            }
  2118	        }
  2119	
  2120	        clansmanIds = new uint32[](count);
  2121	        uint256 out = 0;
  2122	        for (uint256 i = 0; i < defendingClans.length; i++) {
  2123	            uint32[] storage clanClansmen = _clanClansmanIds[defendingClans[i]];
  2124	            for (uint256 j = 0; j < clanClansmen.length; j++) {
  2125	                uint32 clansmanId = clanClansmen[j];
  2126	                Mission storage mission = _missions[clansmanId];
  2127	                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
  2128	                    clansmanIds[out++] = clansmanId;
  2129	                }
  2130	            }
  2131	        }
  2132	    }
  2133	
  2134	    function getDefendingClans(uint8 region) external view override returns (uint32[] memory) {

  ✓ Gather contract/spec context for Phase 7 OTC transfer surface and related invariants
  ✓ Parallelize focused review across OTC logic and test/spec coverage
  → Synthesize findings into requested review format with severity ordering
codex
# Phase Super-Swarm Review — PR #200 (head 0c20b46)
## SUMMARY (verdict + top concerns)
Not green for merge.

Top blockers are:
- the new OTC write paths operate on lazily-settled clan state without calling `_settleClan`, so they can transfer stale gold/vault/blueprint balances and bypass dead-clan restrictions until someone settles the clan;
- the documented/canonical OTC seam is still the old `transfer*` API, but those entrypoints still revert in the real contract and no-op in the stub, while the new tests only cover the new `propose*`/`accept*` API.

## HIGH severity findings
- [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1874), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1902), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1971), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:2052), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:2126), repo context [packages/contracts/src/ClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:354) and [packages/contracts/src/ClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:1092): every OTC propose/accept path reads `clanState` and balances directly, but this engine’s gold/vault/death state is lazy-settled and other balance-sensitive writes explicitly settle first. A clan that is behind on upkeep/mission resolution can propose or accept using stale balances, and a clan that should already be dead can still trade until someone settles it. The missing `>200 tick` guard is part of the same problem: `submitClanOrders` refuses partially-settled writes, OTC does not.
- [packages/contracts/src/IClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:683), [docs/planning/clanworld_v4_2_state_schema_interface_spec.md](/home/claude/code/clan-world/docs/planning/clanworld_v4_2_state_schema_interface_spec.md:660), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:2208), [packages/contracts/src/ClanWorldStub.sol](/home/claude/code/clan-world/packages/contracts/src/ClanWorldStub.sol:123): Phase 7 is supposed to ship a real OTC contract surface, but the canonical seam still documents `transferGold`/`transferVaultResource`/`transferBlueprint`/`transferBundle`, the real contract still reverts those calls, and the stub still silently succeeds. The PR adds a second OTC API instead of making the documented one real, so downstream callers can stay green in stub mode and still fail on chain.

## MEDIUM severity findings
- [packages/contracts/src/IClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:625), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1902), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1971), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:2052): accepted OTC transfers emit only the new `*TransferAccepted` events. The canonical completion events `GoldTransferred`, `VaultResourceTransferred`, and `BlueprintTransferred` remain declared but are now unreachable because the legacy `transfer*` entrypoints still revert. Any indexer/UI keyed off the stable transfer-completion events will miss actual completed transfers.
- [docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md](/home/claude/code/clan-world/docs/planning/V1/01%20Blockchain%20Game%20Spec/clanworld_v4_3_schema_patch.md:298), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1884), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1911): the written dead-clan rule is “dead sender cannot initiate outbound transfer,” but the implementation also blocks dead recipients and invalidates previously-proposed deals if either side is dead at accept time. That may be intentional, but it is stricter than the locked spec and the tests codify the stricter behavior without a spec update.
- [docs/planning/clanworld_v4_spec.md](/home/claude/code/clan-world/docs/planning/clanworld_v4_spec.md:599), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1720), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1756): this PR also changes initial pool seeding from the spec’s per-resource ratios to one uniform resource seed and one uniform gold seed across all pools. That is a material economy change riding along with the OTC surface, and the new tests lock in the new ratios rather than flagging the spec drift.

## LOW severity findings
- [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1874), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1936), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:2030), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:2086): gold/vault/blueprint proposals allow zero-value offers, and none of the OTC proposal types forbid `fromClanId == toClanId`. That creates no-op proposals that can still be accepted and emit success events, which weakens lifecycle/accounting cleanliness for off-chain consumers.
- [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:3966), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:4176), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:4427), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:4488): the new OTC tests cover happy path, expiry, cancel, and post-proposal balance shrink, but they do not cover the most dangerous failure mode here: accepting/proposing while either clan is stale and should first be settled. They also do not exercise the still-documented legacy `transfer*` seam, so the API break can ship unnoticed.

## Cross-cutting observations
- The accept paths themselves are transaction-atomic: I did not find a partial-write / reentrancy bug inside the OTC mutations. The real integrity problem is state freshness, not all-or-nothing execution.
- Dead-clan restriction is only as good as settlement. With the current implementation, “dead clans cannot initiate transfers” is not actually enforced for clans whose death/upkeep effects are still sitting behind lazy settlement.
- This PR bundles unrelated economy changes with the OTC surface. Even if those changes are acceptable, they widen the blast radius and make the OTC review materially harder than it needed to be.
2026-04-29T22:31:56.948775Z ERROR codex_core::session: failed to record rollout items: thread 019ddb57-81d3-7853-90ef-20b6316c7492 not found
2026-04-29T22:31:56.949502Z ERROR codex_core::session: failed to record rollout items: thread 019ddb56-daf1-70e3-8be6-4abb552a260f not found
2026-04-29T22:31:56.949691Z ERROR codex_core::session: failed to record rollout items: thread 019ddb57-81a1-76c2-b5b5-8bed8ddd0112 not found
tokens used
230,006
# Phase Super-Swarm Review — PR #200 (head 0c20b46)
## SUMMARY (verdict + top concerns)
Not green for merge.

Top blockers are:
- the new OTC write paths operate on lazily-settled clan state without calling `_settleClan`, so they can transfer stale gold/vault/blueprint balances and bypass dead-clan restrictions until someone settles the clan;
- the documented/canonical OTC seam is still the old `transfer*` API, but those entrypoints still revert in the real contract and no-op in the stub, while the new tests only cover the new `propose*`/`accept*` API.

## HIGH severity findings
- [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1874), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1902), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1971), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:2052), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:2126), repo context [packages/contracts/src/ClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:354) and [packages/contracts/src/ClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:1092): every OTC propose/accept path reads `clanState` and balances directly, but this engine’s gold/vault/death state is lazy-settled and other balance-sensitive writes explicitly settle first. A clan that is behind on upkeep/mission resolution can propose or accept using stale balances, and a clan that should already be dead can still trade until someone settles it. The missing `>200 tick` guard is part of the same problem: `submitClanOrders` refuses partially-settled writes, OTC does not.
- [packages/contracts/src/IClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:683), [docs/planning/clanworld_v4_2_state_schema_interface_spec.md](/home/claude/code/clan-world/docs/planning/clanworld_v4_2_state_schema_interface_spec.md:660), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:2208), [packages/contracts/src/ClanWorldStub.sol](/home/claude/code/clan-world/packages/contracts/src/ClanWorldStub.sol:123): Phase 7 is supposed to ship a real OTC contract surface, but the canonical seam still documents `transferGold`/`transferVaultResource`/`transferBlueprint`/`transferBundle`, the real contract still reverts those calls, and the stub still silently succeeds. The PR adds a second OTC API instead of making the documented one real, so downstream callers can stay green in stub mode and still fail on chain.

## MEDIUM severity findings
- [packages/contracts/src/IClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:625), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1902), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1971), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:2052): accepted OTC transfers emit only the new `*TransferAccepted` events. The canonical completion events `GoldTransferred`, `VaultResourceTransferred`, and `BlueprintTransferred` remain declared but are now unreachable because the legacy `transfer*` entrypoints still revert. Any indexer/UI keyed off the stable transfer-completion events will miss actual completed transfers.
- [docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md](/home/claude/code/clan-world/docs/planning/V1/01%20Blockchain%20Game%20Spec/clanworld_v4_3_schema_patch.md:298), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1884), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1911): the written dead-clan rule is “dead sender cannot initiate outbound transfer,” but the implementation also blocks dead recipients and invalidates previously-proposed deals if either side is dead at accept time. That may be intentional, but it is stricter than the locked spec and the tests codify the stricter behavior without a spec update.
- [docs/planning/clanworld_v4_spec.md](/home/claude/code/clan-world/docs/planning/clanworld_v4_spec.md:599), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1720), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1756): this PR also changes initial pool seeding from the spec’s per-resource ratios to one uniform resource seed and one uniform gold seed across all pools. That is a material economy change riding along with the OTC surface, and the new tests lock in the new ratios rather than flagging the spec drift.

## LOW severity findings
- [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1874), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:1936), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:2030), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:2086): gold/vault/blueprint proposals allow zero-value offers, and none of the OTC proposal types forbid `fromClanId == toClanId`. That creates no-op proposals that can still be accepted and emit success events, which weakens lifecycle/accounting cleanliness for off-chain consumers.
- [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:3966), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:4176), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:4427), [docs/reviews/pr200-codereview-codex-5-5.md](/home/claude/code/clan-world/docs/reviews/pr200-codereview-codex-5-5.md:4488): the new OTC tests cover happy path, expiry, cancel, and post-proposal balance shrink, but they do not cover the most dangerous failure mode here: accepting/proposing while either clan is stale and should first be settled. They also do not exercise the still-documented legacy `transfer*` seam, so the API break can ship unnoticed.

## Cross-cutting observations
- The accept paths themselves are transaction-atomic: I did not find a partial-write / reentrancy bug inside the OTC mutations. The real integrity problem is state freshness, not all-or-nothing execution.
- Dead-clan restriction is only as good as settlement. With the current implementation, “dead clans cannot initiate transfers” is not actually enforced for clans whose death/upkeep effects are still sitting behind lazy settlement.
- This PR bundles unrelated economy changes with the OTC surface. Even if those changes are acceptable, they widen the blast radius and make the OTC review materially harder than it needed to be.
