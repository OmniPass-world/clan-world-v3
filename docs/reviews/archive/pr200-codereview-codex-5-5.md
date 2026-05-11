Reading additional input from stdin...
OpenAI Codex v0.125.0 (research preview)
--------
workdir: /home/claude/code/clan-world
model: gpt-5.5
provider: openai
approval: never
sandbox: read-only
reasoning effort: high
reasoning summaries: none
session id: 019ddb56-e2a9-7730-8a1b-d1e1175a4008
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
I’ll treat this as a final pre-merge code review and keep it findings-first. I’m going to cross-check the supplied diff against the live contract sources, then use a couple of focused explorer agents for independent passes on lifecycle/accounting and ABI/test coverage.
exec
/bin/bash -lc "sed -n '1720,2105p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "rg -n \"proposeGoldTransfer|acceptGoldTransfer|cancelGoldTransfer|proposeVaultTransfer|acceptVaultTransfer|proposeBlueprintTransfer|acceptBundledTransfer|_nextOtcProposalId|transferGold\\(\" packages/contracts/src packages/contracts/test" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/src/ClanWorld.sol:2118:    function transferGold(uint32, uint32, uint256) external pure override {
packages/contracts/src/IClanWorld.sol:690:    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
packages/contracts/src/ClanWorldStub.sol:120:    function transferGold(uint32, uint32, uint256) external override {}

exec
/bin/bash -lc "sed -n '760,920p' packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:

    function quoteTravel(uint8 srcRegion, uint8 dstRegion) external view returns (uint8 travelTicks, bytes8 path);

    function quoteLootValueRaw(uint32 clanId) external view returns (uint256 lootValue);

    function quoteLootValueSettled(uint32 clanId) external view returns (uint256 lootValue);

    // -------------------------------------------------------------------------
    // UI indexer aggregator getters (v4.4 additions)
    //
    // These are pure compositions of the derived getters above and exist only
    // to reduce indexer RPC traffic. They MUST follow the same non-mutation
    // rule as the underlying derived getters.
    // -------------------------------------------------------------------------

    /// @notice Single-call top-level world state plus per-clan leaderboard data.
    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
    function getWorldSnapshot() external view returns (WorldSnapshot memory);

    /// @notice Single-call complete clan rendering data: derived clan + all
    ///         clansmen with derived state and active missions + plot states +
    ///         defender bookkeeping. Drives the entire sprite layer.
    function getClanFullView(uint32 clanId) external view returns (ClanFullView memory);

    /// @notice Single-call market panel data: 4 pool reserves + spot prices +
    ///         scheduled queues for current and next tick. Drives market UI
    ///         and indexer's per-tick price-history sampling.
    function getMarketState() external view returns (MarketState memory);

    /// @notice Single-call bandit drama state, including projected target if
    ///         attack resolved this tick. Drives the bandit warning UI.
    function getActiveBanditView() external view returns (ActiveBanditView memory);

    /// @notice Optional. List clansmen currently in a region for tap-to-inspect
    ///         tooltips. Can be derived clientside; included for completeness.
    function getRegionPopulation(uint8 region) external view returns (RegionOccupant[] memory);
}

exec
/bin/bash -lc 'rg -n "DEAD|clanState|starvation|dead|ClanState.DEAD|propose.*Transfer|accept.*Transfer|zero|expiry" packages/contracts/test packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/src/ClanWorld.sol:214:    // Returns packed bytes8: region IDs in order, zero-padded.
packages/contracts/src/ClanWorld.sol:309:        // Path 6: dead clansman — invalidate active mission if any
packages/contracts/src/ClanWorld.sol:310:        if (cs.state == ClansmanState.DEAD) {
packages/contracts/src/ClanWorld.sol:315:                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
packages/contracts/src/ClanWorld.sol:397:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
packages/contracts/src/ClanWorld.sol:419:        if (starving && clan.starvationStartsAtTick == 0) {
packages/contracts/src/ClanWorld.sol:420:            clan.starvationStartsAtTick = tick;
packages/contracts/src/ClanWorld.sol:422:        } else if (!starving && clan.starvationStartsAtTick != 0) {
packages/contracts/src/ClanWorld.sol:423:            clan.starvationStartsAtTick = 0;
packages/contracts/src/ClanWorld.sol:430:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
packages/contracts/src/ClanWorld.sol:678:            // Empty deposits are silent no-ops; no zero-delta event for indexers to process.
packages/contracts/src/ClanWorld.sol:872:    ///                    settlement RNG reads real entropy, not zero.
packages/contracts/src/ClanWorld.sol:919:            if (clan.clanState == ClanState.DEAD) continue;
packages/contracts/src/ClanWorld.sol:924:                if (cs.state == ClansmanState.DEAD) continue;
packages/contracts/src/ClanWorld.sol:1005:        require(to != address(0), "ClanWorld: zero address");
packages/contracts/src/ClanWorld.sol:1026:        clan.clanState = ClanState.ACTIVE;
packages/contracts/src/ClanWorld.sol:1033:        clan.starvationStartsAtTick = 0;
packages/contracts/src/ClanWorld.sol:1090:        require(clan.clanState == ClanState.ACTIVE, "ClanWorld: clan dead");
packages/contracts/src/ClanWorld.sol:1148:        if (cs.state == ClansmanState.DEAD) {
packages/contracts/src/ClanWorld.sol:1149:            result.status = StatusCode.ERR_CLANSMAN_DEAD;
packages/contracts/src/ClanWorld.sol:1391:            if (cs.clanId != sma.clanId || cs.state == ClansmanState.DEAD) {
packages/contracts/src/ClanWorld.sol:1589:        if (cs.clansmanId != 0 && cs.state != ClansmanState.DEAD) {
packages/contracts/src/ClanWorld.sol:2297:    /// @dev Returns last-settled state; starvation check uses currentTick (live).
packages/contracts/src/ClanWorld.sol:2301:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
packages/contracts/src/ClanWorld.sol:2380:                state: clan.clanState,
packages/contracts/src/ClanWorld.sol:2401:    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
packages/contracts/src/ClanWorld.sol:2406:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
packages/contracts/src/ClanWorld.sol:2502:                if (cs.state != ClansmanState.DEAD && cs.currentRegion == region) {
packages/contracts/src/ClanWorld.sol:2516:                if (cs.state != ClansmanState.DEAD && cs.currentRegion == region) {
packages/contracts/test/RNG.t.sol:142:        uint256[] memory zeros = new uint256[](3);
packages/contracts/test/RNG.t.sol:143:        assertEq(RNG.rngWeightedPick(SEED, DOMAIN_A, 0, zeros), 0, "zero-total weights return 0");
packages/contracts/test/RNG.t.sol:148:            assertEq(RNG.rngWeightedPick(SEED, DOMAIN_A, nonce, single), 0, "single non-zero weight always wins");
packages/contracts/test/RNG.t.sol:164:        uint256[] memory zeroFront = new uint256[](4);
packages/contracts/test/RNG.t.sol:165:        zeroFront[0] = 0;
packages/contracts/test/RNG.t.sol:166:        zeroFront[1] = 0;
packages/contracts/test/RNG.t.sol:167:        zeroFront[2] = 7;
packages/contracts/test/RNG.t.sol:168:        zeroFront[3] = 3;
packages/contracts/test/RNG.t.sol:169:        uint256[4] memory zeroFrontCounts;
packages/contracts/test/RNG.t.sol:171:            zeroFrontCounts[RNG.rngWeightedPick(SEED, DOMAIN_A, nonce, zeroFront)]++;
packages/contracts/test/RNG.t.sol:173:        assertEq(zeroFrontCounts[0], 0, "zero-weight index 0 should not be picked");
packages/contracts/test/RNG.t.sol:174:        assertEq(zeroFrontCounts[1], 0, "zero-weight index 1 should not be picked");
packages/contracts/test/RNG.t.sol:175:        assertGe(zeroFrontCounts[2], 600, "weight 7 bucket should dominate");
packages/contracts/test/RNG.t.sol:176:        assertLe(zeroFrontCounts[2], 800, "weight 7 bucket should stay plausible");
packages/contracts/test/RNG.t.sol:177:        assertGe(zeroFrontCounts[3], 200, "weight 3 bucket should be represented");
packages/contracts/test/RNG.t.sol:178:        assertLe(zeroFrontCounts[3], 400, "weight 3 bucket should stay plausible");
packages/contracts/test/Reentrancy.t.sol:44:        require(amountA > 0 && amountB > 0, "MockReentrantPool: zero seed");
packages/contracts/test/Reentrancy.t.sol:53:        require(amountIn > 0, "MockReentrantPool: zero amount");
packages/contracts/test/Reentrancy.t.sol:56:        require(goldOut > 0, "MockReentrantPool: zero output");
packages/contracts/test/Reentrancy.t.sol:62:        require(amountOut > 0, "MockReentrantPool: zero amount");
packages/contracts/test/Reentrancy.t.sol:70:        require(amountOut > 0, "MockReentrantPool: zero amount");
packages/contracts/test/ClanWorld.t.sol:48:        _clansmen[csId].state = ClansmanState.DEAD;
packages/contracts/test/ClanWorld.t.sol:254:            assertTrue(state.currentTickSeed != bytes32(0), "seed should be non-zero");
packages/contracts/test/ClanWorld.t.sol:273:        assertTrue(iftTokenId != 0, "iftTokenId should be non-zero");
packages/contracts/test/ClanWorld.t.sol:286:        assertGt(clan.baseRegion, 0, "baseRegion should be non-zero");
packages/contracts/test/ClanWorld.t.sol:1097:        // maxGoldIn = 0 (will always be exceeded for any nonzero buy)
packages/contracts/test/ClanWorld.t.sol:1600:    // Issue #95: DefendBase re-tasking — zero-travel same-target retask must not drop defender
packages/contracts/test/ClanWorld.t.sol:1603:    function test_defendBase_zeroTravel_retask_noDropWindow() public {
packages/contracts/test/ClanWorld.t.sol:1609:        // First DefendBase: clansman defends its own clan (same region → zero travel).
packages/contracts/test/ClanWorld.t.sol:1632:        // Second DefendBase to same target: zero-travel re-task — the regression case.
packages/contracts/test/ClanWorld.t.sol:1699:    // 3.E3: ERR_CLANSMAN_DEAD — dead clansman is rejected
packages/contracts/test/ClanWorld.t.sol:1702:    function test_phase3E3_deadClansman_rejectsOrder() public {
packages/contracts/test/ClanWorld.t.sol:1712:        assertEq(uint8(harness.getClansman(csId).state), uint8(ClansmanState.DEAD), "3.E3: clansman must be DEAD");
packages/contracts/test/ClanWorld.t.sol:1727:        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CLANSMAN_DEAD), "3.E3: dead clansman must be rejected");
packages/contracts/test/ClanWorld.t.sol:1835:        // Submit DefendBase to own clan — same region → zero travel → instant ACTING
packages/contracts/test/ClanWorld.t.sol:2144:        // Submit DefendBase to own clan's base (zero travel)
packages/contracts/test/ClanWorld.t.sol:2172:    // test_lazySettle_path6_deadClansmanMidMission
packages/contracts/test/ClanWorld.t.sol:2173:    // Path 6: dead clansman mid-mission → mission invalidated
packages/contracts/test/ClanWorld.t.sol:2176:    function test_lazySettle_path6_deadClansmanMidMission() public {
packages/contracts/test/ClanWorld.t.sol:2192:        assertEq(uint8(harness.getClansman(csId).state), uint8(ClansmanState.DEAD), "path6: clansman must be DEAD");
packages/contracts/test/ClanWorld.t.sol:2202:        // Clansman must still be DEAD (no resurrection)
packages/contracts/test/ClanWorld.t.sol:2203:        assertEq(uint8(harness.getClansman(csId).state), uint8(ClansmanState.DEAD), "path6: clansman must remain DEAD");
packages/contracts/test/ClanWorld.t.sol:2207:    // test_lazySettle_path6_deadDefender_cleanedFromRegistry
packages/contracts/test/ClanWorld.t.sol:2208:    // Path 6 + DefendBase: dead defending clansman must be removed from registry
packages/contracts/test/ClanWorld.t.sol:2211:    function test_lazySettle_path6_deadDefender_cleanedFromRegistry() public {
packages/contracts/test/ClanWorld.t.sol:2215:        // Submit DefendBase to own base (zero travel → immediately registered)
packages/contracts/test/ClanWorld.t.sol:2237:            uint8(harness.getClansman(csId).state), uint8(ClansmanState.DEAD), "path6-defender: clansman must be DEAD"
packages/contracts/test/ClanWorld.t.sol:2246:        assertEq(defsAfter.length, 0, "path6-defender: dead defender must be removed from registry");
packages/contracts/test/DefendBase.t.sol:114:        assertEq(mission.targetClanId, clanId, "zero target normalizes to self");

codex
The live tree already shows one interesting mismatch: `rg` only found the legacy `transferGold` surface, while the pasted diff adds the OTC functions. I’m going to verify whether this workspace is on an older local checkout, and I’ll still review the supplied diff as authoritative for PR head.
exec
/bin/bash -lc 'git rev-parse --short HEAD' in /home/claude/code/clan-world
 succeeded in 0ms:
9f93593

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
?? docs/reviews/pr198-codereview-codex-5-4.md
?? docs/reviews/pr198-codereview-codex-5-5.md
?? docs/reviews/pr198-codereview-opus-4-7.md
?? docs/reviews/pr198-synthesis.md
?? docs/reviews/pr200-codereview-codex-5-4.md
?? docs/reviews/pr200-codereview-codex-5-5.md
?? docs/reviews/pr200-codereview-opus-4-7.md
?? docs/reviews/r2/

exec
/bin/bash -lc "sed -n '2080,2160p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc 'rg -n "OtcProposal|GoldTransferProposed|proposeGoldTransfer|BundledTransfer" packages/contracts/src packages/contracts/test packages/contracts/abi || true' in /home/claude/code/clan-world
 succeeded in 0ms:
codex
I’m on local `9f93593`, not the supplied `0c20b46`, so I’ll avoid treating the checkout as the source of truth for the new OTC code. I found prior PR200 review artifacts in `docs/reviews`, and I’m pulling those in as extra signal while the subagents do independent passes on the diff.
2026-04-29T22:23:51.882817Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
2026-04-29T22:23:51.889104Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
exec
/bin/bash -lc "sed -n '1,220p' docs/reviews/pr200-codereview-codex-5-4.md" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "sed -n '1,240p' docs/reviews/pr200-codereview-codex-5-5.md" in /home/claude/code/clan-world
 succeeded in 0ms:
Reading additional input from stdin...
OpenAI Codex v0.125.0 (research preview)
--------
workdir: /home/claude/code/clan-world
model: gpt-5.5
provider: openai
approval: never
sandbox: read-only
reasoning effort: high
reasoning summaries: none
session id: 019ddb56-e2a9-7730-8a1b-d1e1175a4008
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

exec
/bin/bash -lc "sed -n '1,240p' docs/reviews/pr200-codereview-opus-4-7.md" in /home/claude/code/clan-world
 succeeded in 0ms:
collab: SpawnAgent
collab: SpawnAgent
exec
/bin/bash -lc 'tail -n 180 docs/reviews/pr200-codereview-opus-4-7.md' in /home/claude/code/clan-world
 succeeded in 0ms:
exec
/bin/bash -lc 'tail -n 180 docs/reviews/pr200-codereview-codex-5-5.md' in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "sed -n '1,240p' docs/reviews/pr200-codereview-opus-4-7.md" in /home/claude/code/clan-world
 succeeded in 0ms:
collab: SpawnAgent
collab: SpawnAgent

exec
/bin/bash -lc 'rg -n "##|HIGH|MEDIUM|LOW|finding|Finding|severity|VERDICT|SUMMARY|BLOCK|APPROVE|REQUEST" docs/reviews/pr200-codereview-*.md' in /home/claude/code/clan-world
 succeeded in 0ms:
docs/reviews/pr200-codereview-codex-5-4.md:21:## Your task
docs/reviews/pr200-codereview-codex-5-4.md:32:## Output
docs/reviews/pr200-codereview-codex-5-4.md:35:## SUMMARY (verdict + top concerns)
docs/reviews/pr200-codereview-codex-5-4.md:36:## HIGH severity findings
docs/reviews/pr200-codereview-codex-5-4.md:37:## MEDIUM severity findings
docs/reviews/pr200-codereview-codex-5-4.md:38:## LOW severity findings
docs/reviews/pr200-codereview-codex-5-4.md:39:## Cross-cutting observations
docs/reviews/pr200-codereview-codex-5-4.md:41:DIFF FOLLOWS.
docs/reviews/pr200-codereview-codex-5-4.md:4636:Reviewing the Phase 7 diff as a cohesive pre-merge pass. I’m going to split this into parallel reads: one track on OTC lifecycle/security/accounting in the contract, and one on spec/tests/dead-clan coverage so we can converge on cross-cutting findings faster.
docs/reviews/pr200-codereview-codex-5-4.md:4639:  • Synthesize findings into requested review format with severity ordering
docs/reviews/pr200-codereview-codex-5-4.md:4723:docs/reviews/pr198-codereview-codex-5-4.md:9397:### 3.8 OTC model
docs/reviews/pr200-codereview-codex-5-4.md:4772:docs/reviews/pr200-codereview-codex-5-4.md:4636:Reviewing the Phase 7 diff as a cohesive pre-merge pass. I’m going to split this into parallel reads: one track on OTC lifecycle/security/accounting in the contract, and one on spec/tests/dead-clan coverage so we can converge on cross-cutting findings faster.
docs/reviews/pr200-codereview-codex-5-4.md:4777:docs/planning/clanworld_v4_3_schema_patch.md:298:## M. Dead Clan OTC Restriction (explicit restatement)
docs/reviews/pr200-codereview-codex-5-4.md:4794:docs/planning/V1/07 clanworld_v4_5_alignment_addendum.md:303:### 7.1 §1 Stream Contracts updates
docs/reviews/pr200-codereview-codex-5-4.md:4795:docs/planning/V1/07 clanworld_v4_5_alignment_addendum.md:320:### 7.2 §2 Open Questions updates
docs/reviews/pr200-codereview-codex-5-4.md:4796:docs/planning/V1/07 clanworld_v4_5_alignment_addendum.md:330:### 7.3 §3 Dependency Graph updates
docs/reviews/pr200-codereview-codex-5-4.md:4797:docs/planning/V1/07 clanworld_v4_5_alignment_addendum.md:352:### 7.4 §7 Risk Register updates
docs/reviews/pr200-codereview-codex-5-4.md:4802:docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:569:### 7.1 Tick metadata
docs/reviews/pr200-codereview-codex-5-4.md:4803:docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:581:### 7.2 Required queries
docs/reviews/pr200-codereview-codex-5-4.md:4804:docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:612:### 7.3 Subscription patterns
docs/reviews/pr200-codereview-codex-5-4.md:4805:docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:626:### 7.4 EventLogEntry shape
docs/reviews/pr200-codereview-codex-5-4.md:4807:docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:307:### 7.1 Tool definitions
docs/reviews/pr200-codereview-codex-5-4.md:4808:docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:394:### 7.2 Why this surface and not more
docs/reviews/pr200-codereview-codex-5-4.md:4809:docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:398:### 7.3 Tool call execution
docs/reviews/pr200-codereview-codex-5-4.md:4827:docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:187:{ tick: 174, severity: ‘HIGH’, text: ‘BLUEPRINT FRAGMENT moved DELTA → GAMMA via OTC’ },
docs/reviews/pr200-codereview-codex-5-4.md:4836:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:327:### 7.1 WorldState
docs/reviews/pr200-codereview-codex-5-4.md:4837:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:350:### 7.2 TreasuryState
docs/reviews/pr200-codereview-codex-5-4.md:4838:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:372:### 7.3 Clan
docs/reviews/pr200-codereview-codex-5-4.md:4839:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:403:### 7.4 WheatPlot
docs/reviews/pr200-codereview-codex-5-4.md:4840:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:421:### 7.5 Clansman
docs/reviews/pr200-codereview-codex-5-4.md:4841:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:523:### 7.10 Active defender registries
docs/reviews/pr200-codereview-codex-5-4.md:4842:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:537:### 7.11 Defense contribution storage
docs/reviews/pr200-codereview-codex-5-4.md:4843:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:4852:docs/planning/clanworld_numbered_implementation_plan.md:311:## Phase 7 — OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:4853:docs/planning/clanworld_numbered_implementation_plan.md:313:### 7.1 Implement gold transfer
docs/reviews/pr200-codereview-codex-5-4.md:4854:docs/planning/clanworld_numbered_implementation_plan.md:316:### 7.2 Implement vault resource transfer
docs/reviews/pr200-codereview-codex-5-4.md:4855:docs/planning/clanworld_numbered_implementation_plan.md:319:### 7.3 Implement blueprint transfer
docs/reviews/pr200-codereview-codex-5-4.md:4856:docs/planning/clanworld_numbered_implementation_plan.md:322:### 7.4 Implement bundled transfer convenience
docs/reviews/pr200-codereview-codex-5-4.md:4857:docs/planning/clanworld_numbered_implementation_plan.md:326:### 7.5 Restrict dead clans
docs/reviews/pr200-codereview-codex-5-4.md:4862:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:298:## M. Dead Clan OTC Restriction (explicit restatement)
docs/reviews/pr200-codereview-codex-5-4.md:4866:docs/planning/clanworld_v1_implementation_profile.md:114:### 3.8 OTC model
docs/reviews/pr200-codereview-codex-5-4.md:4872:docs/planning/clanworld_v4_5_alignment_addendum.md:303:### 7.1 §1 Stream Contracts updates
docs/reviews/pr200-codereview-codex-5-4.md:4873:docs/planning/clanworld_v4_5_alignment_addendum.md:320:### 7.2 §2 Open Questions updates
docs/reviews/pr200-codereview-codex-5-4.md:4874:docs/planning/clanworld_v4_5_alignment_addendum.md:330:### 7.3 §3 Dependency Graph updates
docs/reviews/pr200-codereview-codex-5-4.md:4875:docs/planning/clanworld_v4_5_alignment_addendum.md:352:### 7.4 §7 Risk Register updates
docs/reviews/pr200-codereview-codex-5-4.md:4877:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:825:## 7.1 Winter cadence
docs/reviews/pr200-codereview-codex-5-4.md:4878:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:828:## 7.2 Winter duration
docs/reviews/pr200-codereview-codex-5-4.md:4879:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:831:## 7.3 Winter upkeep
docs/reviews/pr200-codereview-codex-5-4.md:4880:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:837:## 7.4 Winter farming rule
docs/reviews/pr200-codereview-codex-5-4.md:4881:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:848:## 7.5 Winter gathering outside farming
docs/reviews/pr200-codereview-codex-5-4.md:4890:docs/planning/clanworld_v4_spec.md:825:## 7.1 Winter cadence
docs/reviews/pr200-codereview-codex-5-4.md:4891:docs/planning/clanworld_v4_spec.md:828:## 7.2 Winter duration
docs/reviews/pr200-codereview-codex-5-4.md:4892:docs/planning/clanworld_v4_spec.md:831:## 7.3 Winter upkeep
docs/reviews/pr200-codereview-codex-5-4.md:4893:docs/planning/clanworld_v4_spec.md:837:## 7.4 Winter farming rule
docs/reviews/pr200-codereview-codex-5-4.md:4894:docs/planning/clanworld_v4_spec.md:848:## 7.5 Winter gathering outside farming
docs/reviews/pr200-codereview-codex-5-4.md:4903:docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:114:### 3.8 OTC model
docs/reviews/pr200-codereview-codex-5-4.md:4910:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:327:### 7.1 WorldState
docs/reviews/pr200-codereview-codex-5-4.md:4911:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:350:### 7.2 TreasuryState
docs/reviews/pr200-codereview-codex-5-4.md:4912:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:372:### 7.3 Clan
docs/reviews/pr200-codereview-codex-5-4.md:4913:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:403:### 7.4 WheatPlot
docs/reviews/pr200-codereview-codex-5-4.md:4914:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:421:### 7.5 Clansman
docs/reviews/pr200-codereview-codex-5-4.md:4915:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:523:### 7.10 Active defender registries
docs/reviews/pr200-codereview-codex-5-4.md:4916:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:537:### 7.11 Defense contribution storage
docs/reviews/pr200-codereview-codex-5-4.md:4917:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:4926:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:311:## Phase 7 — OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:4927:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:313:### 7.1 Implement gold transfer
docs/reviews/pr200-codereview-codex-5-4.md:4928:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:316:### 7.2 Implement vault resource transfer
docs/reviews/pr200-codereview-codex-5-4.md:4929:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:319:### 7.3 Implement blueprint transfer
docs/reviews/pr200-codereview-codex-5-4.md:4930:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:322:### 7.4 Implement bundled transfer convenience
docs/reviews/pr200-codereview-codex-5-4.md:4931:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:326:### 7.5 Restrict dead clans
docs/reviews/pr200-codereview-codex-5-4.md:4967:## What this package does
docs/reviews/pr200-codereview-codex-5-4.md:4974:## Wave 0 status
docs/reviews/pr200-codereview-codex-5-4.md:4978:## Key files
docs/reviews/pr200-codereview-codex-5-4.md:4984:## Local conventions
docs/reviews/pr200-codereview-codex-5-4.md:4992:## How it interacts with adapters
docs/reviews/pr200-codereview-codex-5-4.md:4996:## Running
docs/reviews/pr200-codereview-codex-5-4.md:5009:## Phase 7 — OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:5011:### 7.1 Implement gold transfer
docs/reviews/pr200-codereview-codex-5-4.md:5014:### 7.2 Implement vault resource transfer
docs/reviews/pr200-codereview-codex-5-4.md:5017:### 7.3 Implement blueprint transfer
docs/reviews/pr200-codereview-codex-5-4.md:5020:### 7.4 Implement bundled transfer convenience
docs/reviews/pr200-codereview-codex-5-4.md:5024:### 7.5 Restrict dead clans
docs/reviews/pr200-codereview-codex-5-4.md:5038:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:5065:### 10.4 ClanOrder
docs/reviews/pr200-codereview-codex-5-4.md:5080:### 10.5 OrderResult
docs/reviews/pr200-codereview-codex-5-5.md:21:## Your task
docs/reviews/pr200-codereview-codex-5-5.md:32:## Output
docs/reviews/pr200-codereview-codex-5-5.md:35:## SUMMARY (verdict + top concerns)
docs/reviews/pr200-codereview-codex-5-5.md:36:## HIGH severity findings
docs/reviews/pr200-codereview-codex-5-5.md:37:## MEDIUM severity findings
docs/reviews/pr200-codereview-codex-5-5.md:38:## LOW severity findings
docs/reviews/pr200-codereview-codex-5-5.md:39:## Cross-cutting observations
docs/reviews/pr200-codereview-codex-5-5.md:41:DIFF FOLLOWS.
docs/reviews/pr200-codereview-codex-5-5.md:4636:I’ll treat this as a final pre-merge code review and keep it findings-first. I’m going to cross-check the supplied diff against the live contract sources, then use a couple of focused explorer agents for independent passes on lifecycle/accounting and ABI/test coverage.
docs/reviews/pr200-codereview-codex-5-5.md:5301:## Your task
docs/reviews/pr200-codereview-codex-5-5.md:5312:## Output
docs/reviews/pr200-codereview-codex-5-5.md:5315:## SUMMARY (verdict + top concerns)
docs/reviews/pr200-codereview-codex-5-5.md:5316:## HIGH severity findings
docs/reviews/pr200-codereview-codex-5-5.md:5317:## MEDIUM severity findings
docs/reviews/pr200-codereview-codex-5-5.md:5318:## LOW severity findings
docs/reviews/pr200-codereview-codex-5-5.md:5319:## Cross-cutting observations
docs/reviews/pr200-codereview-codex-5-5.md:5321:DIFF FOLLOWS.
docs/reviews/pr200-codereview-codex-5-5.md:5525:## Your task
docs/reviews/pr200-codereview-codex-5-5.md:5536:## Output
docs/reviews/pr200-codereview-codex-5-5.md:5539:## SUMMARY (verdict + top concerns)
docs/reviews/pr200-codereview-codex-5-5.md:5540:## HIGH severity findings
docs/reviews/pr200-codereview-codex-5-5.md:5541:## MEDIUM severity findings
docs/reviews/pr200-codereview-codex-5-5.md:5542:## LOW severity findings
docs/reviews/pr200-codereview-codex-5-5.md:5543:## Cross-cutting observations
docs/reviews/pr200-codereview-codex-5-5.md:5545:DIFF FOLLOWS.

exec
/bin/bash -lc 'tail -n 160 docs/reviews/pr200-codereview-codex-5-4.md' in /home/claude/code/clan-world
 succeeded in 0ms:
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
The prior artifacts are transcripts, not clean review outputs, so I’m using them cautiously. The implementation-plan spec is small but important: Phase 7 explicitly says dead clans cannot initiate transfers; the diff blocks dead accept as well, which may be fine as a stricter rule, but I’m checking for places where that stricter lifecycle leaves proposals stuck or ambiguous.
exec
/bin/bash -lc 'rg -n "# Phase Super|SUMMARY|HIGH severity|MEDIUM severity|LOW severity|Cross-cutting|Verdict|verdict|BLOCK|APPROVE|No findings|Finding" docs/reviews/pr200-codereview-codex-5-4.md docs/reviews/pr200-codereview-codex-5-5.md docs/reviews/pr200-codereview-opus-4-7.md' in /home/claude/code/clan-world
 succeeded in 0ms:
docs/reviews/pr200-codereview-codex-5-5.md:34:# Phase Super-Swarm Review — PR #200 (head 0c20b46)
docs/reviews/pr200-codereview-codex-5-5.md:35:## SUMMARY (verdict + top concerns)
docs/reviews/pr200-codereview-codex-5-5.md:36:## HIGH severity findings
docs/reviews/pr200-codereview-codex-5-5.md:37:## MEDIUM severity findings
docs/reviews/pr200-codereview-codex-5-5.md:38:## LOW severity findings
docs/reviews/pr200-codereview-codex-5-5.md:39:## Cross-cutting observations
docs/reviews/pr200-codereview-codex-5-5.md:5314:# Phase Super-Swarm Review — PR #200 (head 0c20b46)
docs/reviews/pr200-codereview-codex-5-5.md:5315:## SUMMARY (verdict + top concerns)
docs/reviews/pr200-codereview-codex-5-5.md:5316:## HIGH severity findings
docs/reviews/pr200-codereview-codex-5-5.md:5317:## MEDIUM severity findings
docs/reviews/pr200-codereview-codex-5-5.md:5318:## LOW severity findings
docs/reviews/pr200-codereview-codex-5-5.md:5319:## Cross-cutting observations
docs/reviews/pr200-codereview-codex-5-5.md:5538:# Phase Super-Swarm Review — PR #200 (head 0c20b46)
docs/reviews/pr200-codereview-codex-5-5.md:5539:## SUMMARY (verdict + top concerns)
docs/reviews/pr200-codereview-codex-5-5.md:5540:## HIGH severity findings
docs/reviews/pr200-codereview-codex-5-5.md:5541:## MEDIUM severity findings
docs/reviews/pr200-codereview-codex-5-5.md:5542:## LOW severity findings
docs/reviews/pr200-codereview-codex-5-5.md:5543:## Cross-cutting observations
docs/reviews/pr200-codereview-codex-5-5.md:5939:/bin/bash -lc 'rg -n "##|HIGH|MEDIUM|LOW|finding|Finding|severity|VERDICT|SUMMARY|BLOCK|APPROVE|REQUEST" docs/reviews/pr200-codereview-*.md' in /home/claude/code/clan-world
docs/reviews/pr200-codereview-codex-5-5.md:5943:docs/reviews/pr200-codereview-codex-5-4.md:35:## SUMMARY (verdict + top concerns)
docs/reviews/pr200-codereview-codex-5-5.md:5944:docs/reviews/pr200-codereview-codex-5-4.md:36:## HIGH severity findings
docs/reviews/pr200-codereview-codex-5-5.md:5945:docs/reviews/pr200-codereview-codex-5-4.md:37:## MEDIUM severity findings
docs/reviews/pr200-codereview-codex-5-5.md:5946:docs/reviews/pr200-codereview-codex-5-4.md:38:## LOW severity findings
docs/reviews/pr200-codereview-codex-5-5.md:5947:docs/reviews/pr200-codereview-codex-5-4.md:39:## Cross-cutting observations
docs/reviews/pr200-codereview-codex-5-5.md:6028:docs/reviews/pr200-codereview-codex-5-5.md:35:## SUMMARY (verdict + top concerns)
docs/reviews/pr200-codereview-codex-5-5.md:6029:docs/reviews/pr200-codereview-codex-5-5.md:36:## HIGH severity findings
docs/reviews/pr200-codereview-codex-5-5.md:6030:docs/reviews/pr200-codereview-codex-5-5.md:37:## MEDIUM severity findings
docs/reviews/pr200-codereview-codex-5-5.md:6031:docs/reviews/pr200-codereview-codex-5-5.md:38:## LOW severity findings
docs/reviews/pr200-codereview-codex-5-5.md:6032:docs/reviews/pr200-codereview-codex-5-5.md:39:## Cross-cutting observations
docs/reviews/pr200-codereview-codex-5-5.md:6037:docs/reviews/pr200-codereview-codex-5-5.md:5315:## SUMMARY (verdict + top concerns)
docs/reviews/pr200-codereview-codex-5-5.md:6038:docs/reviews/pr200-codereview-codex-5-5.md:5316:## HIGH severity findings
docs/reviews/pr200-codereview-codex-5-5.md:6039:docs/reviews/pr200-codereview-codex-5-5.md:5317:## MEDIUM severity findings
docs/reviews/pr200-codereview-codex-5-5.md:6040:docs/reviews/pr200-codereview-codex-5-5.md:5318:## LOW severity findings
docs/reviews/pr200-codereview-codex-5-5.md:6041:docs/reviews/pr200-codereview-codex-5-5.md:5319:## Cross-cutting observations
docs/reviews/pr200-codereview-codex-5-5.md:6045:docs/reviews/pr200-codereview-codex-5-5.md:5539:## SUMMARY (verdict + top concerns)
docs/reviews/pr200-codereview-codex-5-5.md:6046:docs/reviews/pr200-codereview-codex-5-5.md:5540:## HIGH severity findings
docs/reviews/pr200-codereview-codex-5-5.md:6047:docs/reviews/pr200-codereview-codex-5-5.md:5541:## MEDIUM severity findings
docs/reviews/pr200-codereview-codex-5-5.md:6048:docs/reviews/pr200-codereview-codex-5-5.md:5542:## LOW severity findings
docs/reviews/pr200-codereview-codex-5-5.md:6049:docs/reviews/pr200-codereview-codex-5-5.md:5543:## Cross-cutting observations
docs/reviews/pr200-codereview-codex-5-4.md:34:# Phase Super-Swarm Review — PR #200 (head 0c20b46)
docs/reviews/pr200-codereview-codex-5-4.md:35:## SUMMARY (verdict + top concerns)
docs/reviews/pr200-codereview-codex-5-4.md:36:## HIGH severity findings
docs/reviews/pr200-codereview-codex-5-4.md:37:## MEDIUM severity findings
docs/reviews/pr200-codereview-codex-5-4.md:38:## LOW severity findings
docs/reviews/pr200-codereview-codex-5-4.md:39:## Cross-cutting observations

exec
/bin/bash -lc 'wc -l docs/reviews/pr200-codereview-codex-5-4.md docs/reviews/pr200-codereview-codex-5-5.md docs/reviews/pr200-codereview-opus-4-7.md' in /home/claude/code/clan-world
 succeeded in 0ms:
  5399 docs/reviews/pr200-codereview-codex-5-4.md
  6266 docs/reviews/pr200-codereview-codex-5-5.md
     0 docs/reviews/pr200-codereview-opus-4-7.md
 11665 total

exec
/bin/bash -lc "sed -n '5100,5300p' docs/reviews/pr200-codereview-codex-5-4.md" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "sed -n '5600,5900p' docs/reviews/pr200-codereview-codex-5-5.md" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "sed -n '1,240p' docs/reviews/pr200-codereview-opus-4-7.md" in /home/claude/code/clan-world
 succeeded in 0ms:
collab: SpawnAgent
collab: SpawnAgent
exec
/bin/bash -lc 'tail -n 180 docs/reviews/pr200-codereview-opus-4-7.md' in /home/claude/code/clan-world
 succeeded in 0ms:
exec
/bin/bash -lc 'tail -n 180 docs/reviews/pr200-codereview-codex-5-5.md' in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "sed -n '1,260p' docs/reviews/pr200-codereview-opus-4-7.md" in /home/claude/code/clan-world
 succeeded in 0ms:
exec
/bin/bash -lc "rg -n \"\\.mint\\(|MinimalERC20\\(\" packages/contracts apps packages docs --glob '"'!docs/reviews/**'"'" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/src/ClanWorld.sol:2111:        require(MinimalERC20(token).transferFrom(msg.sender, pool, amount), "ClanWorld: seed transfer failed");
packages/contracts/script/Deploy.s.sol:18:        MinimalERC20 wood = new MinimalERC20("Wood", "WOOD");
packages/contracts/script/Deploy.s.sol:19:        MinimalERC20 iron = new MinimalERC20("Iron", "IRON");
packages/contracts/script/Deploy.s.sol:20:        MinimalERC20 wheat = new MinimalERC20("Wheat", "WHEAT");
packages/contracts/script/Deploy.s.sol:21:        MinimalERC20 fish = new MinimalERC20("Fish", "FISH");
packages/contracts/script/Deploy.s.sol:22:        MinimalERC20 gold = new MinimalERC20("Gold", "GOLD");
packages/contracts/script/Deploy.s.sol:23:        MinimalERC20 blueprint = new MinimalERC20("ClanWorld Blueprint", "BPRT");
packages/contracts/src/ClanWorld.sol:2111:        require(MinimalERC20(token).transferFrom(msg.sender, pool, amount), "ClanWorld: seed transfer failed");
packages/contracts/test/ClanWorldStub.t.sol:17:            tokens[i] = address(new MinimalERC20("T", "T"));
packages/contracts/test/SeedPools.t.sol:26:        woodToken = new MinimalERC20("Wood", "WOOD");
packages/contracts/test/SeedPools.t.sol:27:        ironToken = new MinimalERC20("Iron", "IRON");
packages/contracts/test/SeedPools.t.sol:28:        wheatToken = new MinimalERC20("Wheat", "WHEAT");
packages/contracts/test/SeedPools.t.sol:29:        fishToken = new MinimalERC20("Fish", "FISH");
packages/contracts/test/SeedPools.t.sol:30:        goldToken = new MinimalERC20("Gold", "GOLD");
packages/contracts/test/SeedPools.t.sol:31:        blueprintToken = new MinimalERC20("BPRT", "BPRT");
packages/contracts/test/HeartbeatOrdering.t.sol:128:        woodToken = new MinimalERC20("Wood", "WOOD");
packages/contracts/test/HeartbeatOrdering.t.sol:129:        ironToken = new MinimalERC20("Iron", "IRON");
packages/contracts/test/HeartbeatOrdering.t.sol:130:        wheatToken = new MinimalERC20("Wheat", "WHEAT");
packages/contracts/test/HeartbeatOrdering.t.sol:131:        fishToken = new MinimalERC20("Fish", "FISH");
packages/contracts/test/HeartbeatOrdering.t.sol:132:        goldToken = new MinimalERC20("Gold", "GOLD");
packages/contracts/test/HeartbeatOrdering.t.sol:133:        blueprintToken = new MinimalERC20("BPRT", "BPRT");
packages/contracts/test/Reentrancy.t.sol:139:        woodToken = new MinimalERC20("Wood", "WOOD");
packages/contracts/test/Reentrancy.t.sol:140:        ironToken = new MinimalERC20("Iron", "IRON");
packages/contracts/test/Reentrancy.t.sol:141:        wheatToken = new MinimalERC20("Wheat", "WHEAT");
packages/contracts/test/Reentrancy.t.sol:142:        fishToken = new MinimalERC20("Fish", "FISH");
packages/contracts/test/Reentrancy.t.sol:143:        goldToken = new MinimalERC20("Gold", "GOLD");
packages/contracts/test/Reentrancy.t.sol:144:        blueprintToken = new MinimalERC20("BPRT", "BPRT");
packages/contracts/test/ResourceBoundaryTokens.t.sol:24:        woodToken = new MinimalERC20("Wood", "WOOD");
packages/contracts/test/ResourceBoundaryTokens.t.sol:25:        ironToken = new MinimalERC20("Iron", "IRON");
packages/contracts/test/ResourceBoundaryTokens.t.sol:26:        wheatToken = new MinimalERC20("Wheat", "WHEAT");
packages/contracts/test/ResourceBoundaryTokens.t.sol:27:        fishToken = new MinimalERC20("Fish", "FISH");
packages/contracts/test/ResourceBoundaryTokens.t.sol:28:        goldToken = new MinimalERC20("Gold", "GOLD");
packages/contracts/test/ResourceBoundaryTokens.t.sol:29:        blueprintToken = new MinimalERC20("BPRT", "BPRT");
packages/contracts/test/ResourceBoundaryTokens.t.sol:65:        woodToken.mint(holder, 10e18);
packages/contracts/test/ResourceBoundaryTokens.t.sol:70:        woodToken.mint(holder, 10e18);
packages/contracts/script/Deploy.s.sol:18:        MinimalERC20 wood = new MinimalERC20("Wood", "WOOD");
packages/contracts/script/Deploy.s.sol:19:        MinimalERC20 iron = new MinimalERC20("Iron", "IRON");
packages/contracts/script/Deploy.s.sol:20:        MinimalERC20 wheat = new MinimalERC20("Wheat", "WHEAT");
packages/contracts/script/Deploy.s.sol:21:        MinimalERC20 fish = new MinimalERC20("Fish", "FISH");
packages/contracts/script/Deploy.s.sol:22:        MinimalERC20 gold = new MinimalERC20("Gold", "GOLD");
packages/contracts/script/Deploy.s.sol:23:        MinimalERC20 blueprint = new MinimalERC20("ClanWorld Blueprint", "BPRT");
docs/planning/V1/05 0G/clanworld_inft_deployment_notes.md:254:   const tx = await agentNFT.connect(ownerSigner).mint(
docs/planning/V1/05 0G/clanworld_inft_deployment_notes.md:370:# - Call AgentNFT.mint() and capture tokenId
packages/contracts/test/ClanWorld.t.sol:92:        woodToken = new MinimalERC20("Wood", "WOOD");
packages/contracts/test/ClanWorld.t.sol:93:        ironToken = new MinimalERC20("Iron", "IRON");
packages/contracts/test/ClanWorld.t.sol:94:        wheatToken = new MinimalERC20("Wheat", "WHEAT");
packages/contracts/test/ClanWorld.t.sol:95:        fishToken = new MinimalERC20("Fish", "FISH");
packages/contracts/test/ClanWorld.t.sol:96:        goldToken = new MinimalERC20("Gold", "GOLD");
packages/contracts/test/ClanWorld.t.sol:97:        blueprintToken = new MinimalERC20("BPRT", "BPRT");
docs/planning/V1/05 0G/clanworld_clan_identity_spec.md:275:   d. Calls `AgentNFT.mint(ownerAddress, encryptedURI, metadataHash)` and captures the resulting `tokenId`
packages/contracts/test/ClanWorldStub.t.sol:17:            tokens[i] = address(new MinimalERC20("T", "T"));
packages/contracts/test/SeedPools.t.sol:26:        woodToken = new MinimalERC20("Wood", "WOOD");
packages/contracts/test/SeedPools.t.sol:27:        ironToken = new MinimalERC20("Iron", "IRON");
packages/contracts/test/SeedPools.t.sol:28:        wheatToken = new MinimalERC20("Wheat", "WHEAT");
packages/contracts/test/SeedPools.t.sol:29:        fishToken = new MinimalERC20("Fish", "FISH");
packages/contracts/test/SeedPools.t.sol:30:        goldToken = new MinimalERC20("Gold", "GOLD");
packages/contracts/test/SeedPools.t.sol:31:        blueprintToken = new MinimalERC20("BPRT", "BPRT");
packages/contracts/test/HeartbeatOrdering.t.sol:128:        woodToken = new MinimalERC20("Wood", "WOOD");
packages/contracts/test/HeartbeatOrdering.t.sol:129:        ironToken = new MinimalERC20("Iron", "IRON");
packages/contracts/test/HeartbeatOrdering.t.sol:130:        wheatToken = new MinimalERC20("Wheat", "WHEAT");
packages/contracts/test/HeartbeatOrdering.t.sol:131:        fishToken = new MinimalERC20("Fish", "FISH");
packages/contracts/test/HeartbeatOrdering.t.sol:132:        goldToken = new MinimalERC20("Gold", "GOLD");
packages/contracts/test/HeartbeatOrdering.t.sol:133:        blueprintToken = new MinimalERC20("BPRT", "BPRT");
packages/contracts/test/Reentrancy.t.sol:139:        woodToken = new MinimalERC20("Wood", "WOOD");
packages/contracts/test/Reentrancy.t.sol:140:        ironToken = new MinimalERC20("Iron", "IRON");
packages/contracts/test/Reentrancy.t.sol:141:        wheatToken = new MinimalERC20("Wheat", "WHEAT");
packages/contracts/test/Reentrancy.t.sol:142:        fishToken = new MinimalERC20("Fish", "FISH");
packages/contracts/test/Reentrancy.t.sol:143:        goldToken = new MinimalERC20("Gold", "GOLD");
packages/contracts/test/Reentrancy.t.sol:144:        blueprintToken = new MinimalERC20("BPRT", "BPRT");
packages/contracts/test/ResourceBoundaryTokens.t.sol:24:        woodToken = new MinimalERC20("Wood", "WOOD");
packages/contracts/test/ResourceBoundaryTokens.t.sol:25:        ironToken = new MinimalERC20("Iron", "IRON");
packages/contracts/test/ResourceBoundaryTokens.t.sol:26:        wheatToken = new MinimalERC20("Wheat", "WHEAT");
packages/contracts/test/ResourceBoundaryTokens.t.sol:27:        fishToken = new MinimalERC20("Fish", "FISH");
packages/contracts/test/ResourceBoundaryTokens.t.sol:28:        goldToken = new MinimalERC20("Gold", "GOLD");
packages/contracts/test/ResourceBoundaryTokens.t.sol:29:        blueprintToken = new MinimalERC20("BPRT", "BPRT");
packages/contracts/test/ResourceBoundaryTokens.t.sol:65:        woodToken.mint(holder, 10e18);
packages/contracts/test/ResourceBoundaryTokens.t.sol:70:        woodToken.mint(holder, 10e18);
packages/contracts/test/ClanWorld.t.sol:92:        woodToken = new MinimalERC20("Wood", "WOOD");
packages/contracts/test/ClanWorld.t.sol:93:        ironToken = new MinimalERC20("Iron", "IRON");
packages/contracts/test/ClanWorld.t.sol:94:        wheatToken = new MinimalERC20("Wheat", "WHEAT");
packages/contracts/test/ClanWorld.t.sol:95:        fishToken = new MinimalERC20("Fish", "FISH");
packages/contracts/test/ClanWorld.t.sol:96:        goldToken = new MinimalERC20("Gold", "GOLD");
packages/contracts/test/ClanWorld.t.sol:97:        blueprintToken = new MinimalERC20("BPRT", "BPRT");

exec
/bin/bash -lc "rg -n \"transferGold\\(|transferVaultResource\\(|transferBlueprint\\(|transferBundle\\(|proposeGoldTransfer\\(|acceptGoldTransfer\\(\" packages apps packages/contracts/src/IClanWorld.sol packages/contracts/abi/IClanWorld.json --glob '"'!docs/reviews/**'"'" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/src/IClanWorld.sol:690:    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
packages/contracts/src/IClanWorld.sol:692:    function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
packages/contracts/src/IClanWorld.sol:694:    function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
packages/contracts/src/IClanWorld.sol:696:    function transferBundle(
packages/contracts/src/ClanWorld.sol:2118:    function transferGold(uint32, uint32, uint256) external pure override {
packages/contracts/src/ClanWorld.sol:2122:    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
packages/contracts/src/ClanWorld.sol:2126:    function transferBlueprint(uint32, uint32, uint256) external pure override {
packages/contracts/src/ClanWorld.sol:2130:    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
packages/contracts/src/ClanWorldStub.sol:120:    function transferGold(uint32, uint32, uint256) external override {}
packages/contracts/src/ClanWorldStub.sol:122:    function transferVaultResource(uint32, uint32, ResourceType, uint256) external override {}
packages/contracts/src/ClanWorldStub.sol:124:    function transferBlueprint(uint32, uint32, uint256) external override {}
packages/contracts/src/ClanWorldStub.sol:126:    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256) external override {}
packages/contracts/src/IClanWorld.sol:690:    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
packages/contracts/src/IClanWorld.sol:692:    function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
packages/contracts/src/IClanWorld.sol:694:    function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
packages/contracts/src/IClanWorld.sol:696:    function transferBundle(

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
/bin/bash -lc "sed -n '1,130p' packages/contracts/src/MinimalERC20.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

/// @notice Minimal ERC20 boundary token. No external deps.
contract MinimalERC20 {
    string public name;
    string public symbol;
    uint8 public constant decimals = 18;

    uint256 public totalSupply;
    address public immutable DEPLOYER;
    address public engine;
    uint8 public resourceType;
    bool public boundaryConfigured;
    bool public treasurySeeded;

    mapping(address => uint256) public balanceOf;
    mapping(address => mapping(address => uint256)) public allowance;

    event Transfer(address indexed from, address indexed to, uint256 value);
    event Approval(address indexed owner_, address indexed spender, uint256 value);
    event BoundaryConfigured(uint8 indexed resourceType, address indexed engine);
    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);

    constructor(string memory name_, string memory symbol_) {
        name = name_;
        symbol = symbol_;
        DEPLOYER = msg.sender;
        resourceType = type(uint8).max;
    }

    /// @dev Wrapped per forge-lint unwrapped-modifier-logic — keeps modifier
    /// body slim so the require isn't duplicated at every call site.
    function _onlyDeployer() internal view {
        require(msg.sender == DEPLOYER, "MinimalERC20: not deployer");
    }

    function _onlyEngine() internal view {
        require(boundaryConfigured && msg.sender == engine, "MinimalERC20: not engine");
    }

    modifier onlyDeployer() {
        _onlyDeployer();
        _;
    }

    modifier onlyEngine() {
        _onlyEngine();
        _;
    }

    function configureBoundary(uint8 resourceType_, address engine_) external onlyDeployer {
        require(!boundaryConfigured, "MinimalERC20: boundary configured");
        require(engine_ != address(0), "MinimalERC20: zero engine");

        resourceType = resourceType_;
        engine = engine_;
        boundaryConfigured = true;

        emit BoundaryConfigured(resourceType_, engine_);
    }

    function seedTreasury(address treasury, uint256 amount) external onlyDeployer {
        require(!treasurySeeded, "MinimalERC20: treasury seeded");
        require(treasury != address(0), "MinimalERC20: zero treasury");

        treasurySeeded = true;
        _mint(treasury, amount);
    }

    function mint(address to, uint256 amount) external onlyEngine {
        _mint(to, amount);
        emit ResourceMinted(resourceType, to, amount);
    }

    function burn(address from, uint256 amount) external onlyEngine {
        balanceOf[from] -= amount;
        totalSupply -= amount;
        emit Transfer(from, address(0), amount);
        emit ResourceBurned(resourceType, from, amount);
    }

    function _mint(address to, uint256 amount) internal {
        totalSupply += amount;
        balanceOf[to] += amount;
        emit Transfer(address(0), to, amount);
    }

    function transfer(address to, uint256 amount) external returns (bool) {
        balanceOf[msg.sender] -= amount;
        balanceOf[to] += amount;
        emit Transfer(msg.sender, to, amount);
        return true;
    }

    function transferFrom(address from, address to, uint256 amount) external returns (bool) {
        allowance[from][msg.sender] -= amount;
        balanceOf[from] -= amount;
        balanceOf[to] += amount;
        emit Transfer(from, to, amount);
        return true;
    }

    function approve(address spender, uint256 amount) external returns (bool) {
        allowance[msg.sender][spender] = amount;
        emit Approval(msg.sender, spender, amount);
        return true;
    }
}

collab: Wait
collab: Wait
codex
# Phase Super-Swarm Review — PR #200 (head 0c20b46)

## SUMMARY (verdict + top concerns)

Verdict: **REQUEST CHANGES**.

Top concern: OTC proposals are not bound to the proposer owner or an ownership nonce. If clan ownership can change after proposal creation, an old proposal can debit the new owner’s clan when accepted. Secondary concerns: the canonical direct transfer functions still exist but revert, and this PR includes unrelated ABI/event changes that can break indexers.

## HIGH severity findings

- **Stale proposals can drain assets after clan ownership transfer.**  
  `packages/contracts/src/ClanWorld.sol` OTC `propose*Transfer` / `accept*Transfer`; `packages/contracts/src/IClanWorld.sol` proposal structs.  
  Proposals store `from`, `to`, amounts, expiry, and flags, but not the proposer address, owner at proposal time, or an owner/IFT nonce. `accept*Transfer` validates only the current target owner. If `fromClan.owner` changes after proposal creation, the target can still accept and debit the new owner’s clan. This affects gold, vault, blueprint, and bundled transfers. Store proposer/owner nonce and require it at accept, or invalidate open proposals on ownership transfer.

## MEDIUM severity findings

- **Canonical direct OTC functions remain public but revert.**  
  `packages/contracts/src/ClanWorld.sol` `transferGold`, `transferVaultResource`, `transferBlueprint`, `transferBundle`; `packages/contracts/src/IClanWorld.sol`.  
  The interface still advertises the old transfer surface, but implementation returns `"OTC transfers not implemented"`. If proposer/acceptor is now the only supported surface, remove or clearly supersede these functions; otherwise Phase 7 still has callable transfer methods that fail.

- **Gold transfer expiry ABI is inconsistent with the rest of OTC.**  
  `proposeGoldTransfer` / `GoldTransferProposed` use `uint256 expiryTick`, while stored proposals and all other OTC proposal APIs use `uint64`. The implementation bounds and casts, but event/getter data types disagree. Make gold match the other proposal types.

- **Unrelated `ResourcesDeposited` ABI change truncates tick data.**  
  `packages/contracts/src/IClanWorld.sol` `ResourcesDeposited`; `packages/contracts/src/ClanWorld.sol` `_resolveAction` / `_doDeposit`; ABI JSON.  
  The event changed from `uint64 atTick` to `uint32 tick`, with a cast from world `uint64`. This breaks existing event consumers and will wrap ticks above `uint32`. It is also outside the OTC scope.

## LOW severity findings

- **Individual OTC proposals allow empty/no-op transfers.**  
  `proposeGoldTransfer`, `proposeBlueprintTransfer`, and `proposeVaultTransfer` accept zero amounts or all-zero vault amounts. Bundled transfers reject empty bundles, so the individual paths should also require value movement.

- **Born-expired proposals are allowed.**  
  `propose*Transfer` does not require `expiryTick >= _world.currentTick`. These proposals emit lifecycle events and consume IDs but can never be accepted.

- **Stub OTC lifecycle is too thin to catch integration bugs.**  
  `packages/contracts/src/ClanWorldStub.sol` returns proposal ID `1` and makes accept/cancel no-ops. Stub-mode consumers cannot observe replay, cancellation, expiry, or proposal identity behavior.

- **Failure-path tests miss the ownership-transfer/stale-proposal case.**  
  Current tests cover balance changes, expiry, cancellation, and some wrong callers, but not the cross-phase owner-change path that creates the high-severity issue.

## Cross-cutting observations

- Accept paths are otherwise internally atomic: balances are checked before mutation, no external calls occur during accept, and `nonReentrant` is applied.
- Dead-clan restrictions are consistently enforced on propose and accept; allowing dead proposers to cancel existing proposals is reasonable.
- The PR mixes OTC work with pool/token/deposit/gathering ABI and behavior changes, which makes final merge risk higher than Phase 7 alone.
2026-04-29T22:26:17.174452Z ERROR codex_core::session: failed to record rollout items: thread 019ddb57-994d-7d70-a587-05058f3b8fc4 not found
2026-04-29T22:26:17.174463Z ERROR codex_core::session: failed to record rollout items: thread 019ddb57-99c8-7363-9f2d-4a6f7edb3776 not found
2026-04-29T22:26:17.174609Z ERROR codex_core::session: failed to record rollout items: thread 019ddb56-e2a9-7730-8a1b-d1e1175a4008 not found
tokens used
100,807
# Phase Super-Swarm Review — PR #200 (head 0c20b46)

## SUMMARY (verdict + top concerns)

Verdict: **REQUEST CHANGES**.

Top concern: OTC proposals are not bound to the proposer owner or an ownership nonce. If clan ownership can change after proposal creation, an old proposal can debit the new owner’s clan when accepted. Secondary concerns: the canonical direct transfer functions still exist but revert, and this PR includes unrelated ABI/event changes that can break indexers.

## HIGH severity findings

- **Stale proposals can drain assets after clan ownership transfer.**  
  `packages/contracts/src/ClanWorld.sol` OTC `propose*Transfer` / `accept*Transfer`; `packages/contracts/src/IClanWorld.sol` proposal structs.  
  Proposals store `from`, `to`, amounts, expiry, and flags, but not the proposer address, owner at proposal time, or an owner/IFT nonce. `accept*Transfer` validates only the current target owner. If `fromClan.owner` changes after proposal creation, the target can still accept and debit the new owner’s clan. This affects gold, vault, blueprint, and bundled transfers. Store proposer/owner nonce and require it at accept, or invalidate open proposals on ownership transfer.

## MEDIUM severity findings

- **Canonical direct OTC functions remain public but revert.**  
  `packages/contracts/src/ClanWorld.sol` `transferGold`, `transferVaultResource`, `transferBlueprint`, `transferBundle`; `packages/contracts/src/IClanWorld.sol`.  
  The interface still advertises the old transfer surface, but implementation returns `"OTC transfers not implemented"`. If proposer/acceptor is now the only supported surface, remove or clearly supersede these functions; otherwise Phase 7 still has callable transfer methods that fail.

- **Gold transfer expiry ABI is inconsistent with the rest of OTC.**  
  `proposeGoldTransfer` / `GoldTransferProposed` use `uint256 expiryTick`, while stored proposals and all other OTC proposal APIs use `uint64`. The implementation bounds and casts, but event/getter data types disagree. Make gold match the other proposal types.

- **Unrelated `ResourcesDeposited` ABI change truncates tick data.**  
  `packages/contracts/src/IClanWorld.sol` `ResourcesDeposited`; `packages/contracts/src/ClanWorld.sol` `_resolveAction` / `_doDeposit`; ABI JSON.  
  The event changed from `uint64 atTick` to `uint32 tick`, with a cast from world `uint64`. This breaks existing event consumers and will wrap ticks above `uint32`. It is also outside the OTC scope.

## LOW severity findings

- **Individual OTC proposals allow empty/no-op transfers.**  
  `proposeGoldTransfer`, `proposeBlueprintTransfer`, and `proposeVaultTransfer` accept zero amounts or all-zero vault amounts. Bundled transfers reject empty bundles, so the individual paths should also require value movement.

- **Born-expired proposals are allowed.**  
  `propose*Transfer` does not require `expiryTick >= _world.currentTick`. These proposals emit lifecycle events and consume IDs but can never be accepted.

- **Stub OTC lifecycle is too thin to catch integration bugs.**  
  `packages/contracts/src/ClanWorldStub.sol` returns proposal ID `1` and makes accept/cancel no-ops. Stub-mode consumers cannot observe replay, cancellation, expiry, or proposal identity behavior.

- **Failure-path tests miss the ownership-transfer/stale-proposal case.**  
  Current tests cover balance changes, expiry, cancellation, and some wrong callers, but not the cross-phase owner-change path that creates the high-severity issue.

## Cross-cutting observations

- Accept paths are otherwise internally atomic: balances are checked before mutation, no external calls occur during accept, and `nonReentrant` is applied.
- Dead-clan restrictions are consistently enforced on propose and accept; allowing dead proposers to cancel existing proposals is reasonable.
- The PR mixes OTC work with pool/token/deposit/gathering ABI and behavior changes, which makes final merge risk higher than Phase 7 alone.
