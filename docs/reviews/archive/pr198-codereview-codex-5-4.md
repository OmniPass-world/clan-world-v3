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
session id: 019ddcc0-23f0-70d0-b6d9-f018fab08e00
--------
user
Read the prompt and diff from stdin. Use parallel tool calls and subagents to thoroughly review. Output the full review to stdout in the requested format.

<stdin>
You are a senior staff engineer doing the FINAL pre-merge code review for a multi-issue phase release PR.

PR: Phase 6 — Unicorn Town Market
Head SHA: 9291964
Repo: OmniPass-world/clan-world
Branch: dev-phase-6-market → dev

## Phase context
Phase 6 — Unicorn Town Market — implements the player-facing market system for the on-chain RTS game. Scope:
- Immediate market actions (buy/sell at current spot)
- Scheduled market actions (queued buy/sell with future tick execution)
- Carry-based market: orders execute against the player's carry inventory (wheelbarrow/vault separation)
- ERC20 cleanup (token plumbing for sponsor demo)
- Market failure semantics (insufficient resources, price slippage, cancellation paths)
- Market events surface (emit log entries for off-chain indexer)

Phase iterated through 4 review rounds (R1→R4). Key changes during the cycle:
- R3 added wheelbarrow vault→carry mechanism (Path A1) via `WithdrawResources` action and a sell-side submit validation
- R4 fixed `ActionType` enum stability (appended `WithdrawResources` to END instead of inserting mid-enum — preserves stored on-chain action type values), `MarketBuy` now returns OK on error path so market failures correctly propagate as failures, `uintValue` JSON adapter robustness for boundary cases.

R3 + R4 together: ~12 sub-issues merged; this is the cohesive release PR for the whole phase.

## Your task

This is a COHESIVE PHASE — multiple sub-issues already merged and reviewed individually; you are reviewing the integrated whole. Look for:

1. CROSS-CUTTING bugs that only surface when sub-issues integrate (race conditions between newly-added paths, state-machine inconsistencies, broken invariants across components — especially the wheelbarrow/vault/carry triangle and its interaction with scheduled market actions)
2. ARCHITECTURAL drift — does the phase actually deliver its stated goal? Any sub-issue that diverged from plan?
3. SECURITY surface — auth, input validation, prompt injection, TOCTOU, resource leaks, secrets handling, ERC20 reentrancy or approve-race, integer overflow on price math, signature replay
4. DATA-flow correctness — schema consistency, migration safety (especially `ActionType` enum stability), idempotency, error paths, event emission completeness
5. Integration risks — newly-added code's effect on existing code paths, regression surface (action queue, tick processing, indexer state)
6. Missing test coverage on the integration seams (especially: enum stability, withdraw→carry→sell flow, MarketBuy failure propagation)
7. Operational risk — deploy ordering, rollback safety, runtime config gaps

USE PARALLEL TOOL CALLS / SUB-AGENTS aggressively. You have full repo read access at /home/claude/code/clan-world. Read all changed files. Look up callers of new functions. Trace state machines end-to-end. Don't just skim the diff — understand the SHIPPING SURFACE.

## Output format (write the entire review to stdout)

# Phase Super-Swarm Review — PR #198 (head 9291964)

## SUMMARY
2-4 sentences: overall verdict (CLEAN | NEEDS_FIXES), top concerns, merge recommendation.

## HIGH severity findings
(real bugs, security issues, data corruption, broken invariants. Each with file:line and one-paragraph explanation + suggested fix.)

## MEDIUM severity findings
(should-fix; design quality, missing edge cases, operational issues)

## LOW severity findings
(defer-OK; nits, style, minor cleanups, follow-up issues to file)

## Cross-cutting observations
(things that don't fit the per-finding format — patterns across the diff, architectural drift, suggested refactors)

If clean, say "CLEAN — no findings" under each section.

DIFF FOLLOWS BELOW.
---
---
diff --git a/packages/contracts/abi/IClanWorld.json b/packages/contracts/abi/IClanWorld.json
index 2ca0d2c..5756259 100644
--- a/packages/contracts/abi/IClanWorld.json
+++ b/packages/contracts/abi/IClanWorld.json
@@ -7,6 +7,25 @@
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
@@ -226,89 +245,39 @@
               "name": "maxGoldIn",
               "type": "uint256",
               "internalType": "uint256"
+            },
+            {
+              "name": "withdrawResources",
+              "type": "tuple",
+              "internalType": "struct WithdrawResourcesData",
+              "components": [
+                {
+                  "name": "wood",
+                  "type": "uint256",
+                  "internalType": "uint256"
+                },
+                {
+                  "name": "iron",
+                  "type": "uint256",
+                  "internalType": "uint256"
+                },
+                {
+                  "name": "wheat",
+                  "type": "uint256",
+                  "internalType": "uint256"
+                },
+                {
+                  "name": "fish",
+                  "type": "uint256",
+                  "internalType": "uint256"
+                }
+              ]
             }
           ]
         }
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
@@ -816,6 +785,33 @@
                           "name": "maxGoldIn",
                           "type": "uint256",
                           "internalType": "uint256"
+                        },
+                        {
+                          "name": "withdrawResources",
+                          "type": "tuple",
+                          "internalType": "struct WithdrawResourcesData",
+                          "components": [
+                            {
+                              "name": "wood",
+                              "type": "uint256",
+                              "internalType": "uint256"
+                            },
+                            {
+                              "name": "iron",
+                              "type": "uint256",
+                              "internalType": "uint256"
+                            },
+                            {
+                              "name": "wheat",
+                              "type": "uint256",
+                              "internalType": "uint256"
+                            },
+                            {
+                              "name": "fish",
+                              "type": "uint256",
+                              "internalType": "uint256"
+                            }
+                          ]
                         }
                       ]
                     },
@@ -925,6 +921,33 @@
                       "name": "maxGoldIn",
                       "type": "uint256",
                       "internalType": "uint256"
+                    },
+                    {
+                      "name": "withdrawResources",
+                      "type": "tuple",
+                      "internalType": "struct WithdrawResourcesData",
+                      "components": [
+                        {
+                          "name": "wood",
+                          "type": "uint256",
+                          "internalType": "uint256"
+                        },
+                        {
+                          "name": "iron",
+                          "type": "uint256",
+                          "internalType": "uint256"
+                        },
+                        {
+                          "name": "wheat",
+                          "type": "uint256",
+                          "internalType": "uint256"
+                        },
+                        {
+                          "name": "fish",
+                          "type": "uint256",
+                          "internalType": "uint256"
+                        }
+                      ]
                     }
                   ]
                 }
@@ -1389,6 +1412,33 @@
                   "name": "maxGoldIn",
                   "type": "uint256",
                   "internalType": "uint256"
+                },
+                {
+                  "name": "withdrawResources",
+                  "type": "tuple",
+                  "internalType": "struct WithdrawResourcesData",
+                  "components": [
+                    {
+                      "name": "wood",
+                      "type": "uint256",
+                      "internalType": "uint256"
+                    },
+                    {
+                      "name": "iron",
+                      "type": "uint256",
+                      "internalType": "uint256"
+                    },
+                    {
+                      "name": "wheat",
+                      "type": "uint256",
+                      "internalType": "uint256"
+                    },
+                    {
+                      "name": "fish",
+                      "type": "uint256",
+                      "internalType": "uint256"
+                    }
+                  ]
                 }
               ]
             },
@@ -1639,6 +1689,83 @@
       ],
       "stateMutability": "view"
     },
+    {
+      "type": "function",
+      "name": "getMissionTiming",
+      "inputs": [
+        {
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
     {
       "type": "function",
       "name": "getRegionPopulation",
@@ -1685,6 +1812,25 @@
       ],
       "stateMutability": "view"
     },
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
     {
       "type": "function",
       "name": "getScheduledMarketActionsForTick",
@@ -1751,6 +1897,30 @@
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
@@ -1929,6 +2099,16 @@
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
@@ -2036,6 +2216,16 @@
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
@@ -2270,6 +2460,19 @@
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
@@ -2318,6 +2521,33 @@
               "name": "maxGoldIn",
               "type": "uint256",
               "internalType": "uint256"
+            },
+            {
+              "name": "withdrawResources",
+              "type": "tuple",
+              "internalType": "struct WithdrawResourcesData",
+              "components": [
+                {
+                  "name": "wood",
+                  "type": "uint256",
+                  "internalType": "uint256"
+                },
+                {
+                  "name": "iron",
+                  "type": "uint256",
+                  "internalType": "uint256"
+                },
+                {
+                  "name": "wheat",
+                  "type": "uint256",
+                  "internalType": "uint256"
+                },
+                {
+                  "name": "fish",
+                  "type": "uint256",
+                  "internalType": "uint256"
+                }
+              ]
             }
           ]
         }
@@ -2347,6 +2577,11 @@
               "name": "missionNonce",
               "type": "uint64",
               "internalType": "uint64"
+            },
+            {
+              "name": "marketMode",
+              "type": "uint8",
+              "internalType": "enum MarketExecutionMode"
             }
           ]
         }
@@ -2970,22 +3205,22 @@
           "internalType": "uint32"
         },
         {
-          "name": "clansmanId",
+          "name": "csId",
           "type": "uint32",
-          "indexed": true,
+          "indexed": false,
           "internalType": "uint32"
         },
         {
-          "name": "tokenIn",
-          "type": "address",
+          "name": "action",
+          "type": "uint8",
           "indexed": false,
-          "internalType": "address"
+          "internalType": "enum ActionType"
         },
         {
-          "name": "tokenOut",
-          "type": "address",
+          "name": "resourceIn",
+          "type": "uint8",
           "indexed": false,
-          "internalType": "address"
+          "internalType": "uint8"
         },
         {
           "name": "amountIn",
@@ -2993,6 +3228,12 @@
           "indexed": false,
           "internalType": "uint256"
         },
+        {
+          "name": "resourceOut",
+          "type": "uint8",
+          "indexed": false,
+          "internalType": "uint8"
+        },
         {
           "name": "amountOut",
           "type": "uint256",
@@ -3000,7 +3241,7 @@
           "internalType": "uint256"
         },
         {
-          "name": "atTick",
+          "name": "tick",
           "type": "uint64",
           "indexed": false,
           "internalType": "uint64"
@@ -3068,9 +3309,9 @@
           "internalType": "uint32"
         },
         {
-          "name": "clansmanId",
+          "name": "csId",
           "type": "uint32",
-          "indexed": true,
+          "indexed": false,
           "internalType": "uint32"
         },
         {
@@ -3079,11 +3320,23 @@
           "indexed": false,
           "internalType": "enum ActionType"
         },
+        {
+          "name": "mode",
+          "type": "uint8",
+          "indexed": false,
+          "internalType": "enum MarketExecutionMode"
+        },
         {
           "name": "reason",
           "type": "uint8",
           "indexed": false,
           "internalType": "enum StatusCode"
+        },
+        {
+          "name": "tick",
+          "type": "uint64",
+          "indexed": false,
+          "internalType": "uint64"
         }
       ],
       "anonymous": false
@@ -3267,6 +3520,56 @@
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
@@ -3284,34 +3587,34 @@
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
@@ -3377,6 +3680,55 @@
       ],
       "anonymous": false
     },
+    {
+      "type": "event",
+      "name": "ResourcesWithdrawn",
+      "inputs": [
+        {
+          "name": "clanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "clansmanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "woodDelta",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "ironDelta",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "wheatDelta",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "fishDelta",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "tick",
+          "type": "uint32",
+          "indexed": false,
+          "internalType": "uint32"
+        }
+      ],
+      "anonymous": false
+    },
     {
       "type": "event",
       "name": "ScheduledMarketActionCommitted",
@@ -3436,18 +3788,6 @@
       "type": "event",
       "name": "ScheduledMarketActionExecuted",
       "inputs": [
-        {
-          "name": "executeAtTick",
-          "type": "uint64",
-          "indexed": true,
-          "internalType": "uint64"
-        },
-        {
-          "name": "commitSequence",
-          "type": "uint64",
-          "indexed": true,
-          "internalType": "uint64"
-        },
         {
           "name": "clanId",
           "type": "uint32",
@@ -3455,22 +3795,22 @@
           "internalType": "uint32"
         },
         {
-          "name": "clansmanId",
+          "name": "csId",
           "type": "uint32",
           "indexed": false,
           "internalType": "uint32"
         },
         {
-          "name": "tokenIn",
-          "type": "address",
+          "name": "action",
+          "type": "uint8",
           "indexed": false,
-          "internalType": "address"
+          "internalType": "enum ActionType"
         },
         {
-          "name": "tokenOut",
-          "type": "address",
+          "name": "resourceIn",
+          "type": "uint8",
           "indexed": false,
-          "internalType": "address"
+          "internalType": "uint8"
         },
         {
           "name": "amountIn",
@@ -3478,11 +3818,23 @@
           "indexed": false,
           "internalType": "uint256"
         },
+        {
+          "name": "resourceOut",
+          "type": "uint8",
+          "indexed": false,
+          "internalType": "uint8"
+        },
         {
           "name": "amountOut",
           "type": "uint256",
           "indexed": false,
           "internalType": "uint256"
+        },
+        {
+          "name": "settledAtTick",
+          "type": "uint64",
+          "indexed": false,
+          "internalType": "uint64"
         }
       ],
       "anonymous": false
diff --git a/packages/contracts/script/Deploy.s.sol b/packages/contracts/script/Deploy.s.sol
index 0219458..5e54cd7 100644
--- a/packages/contracts/script/Deploy.s.sol
+++ b/packages/contracts/script/Deploy.s.sol
@@ -10,15 +10,16 @@ import {PoolSeedConfig} from "../src/IClanWorld.sol";
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
@@ -28,11 +29,11 @@ contract Deploy is Script {
         console.log("gold:     ", address(gold));
         console.log("blueprint:", address(blueprint));
 
-        // 3. Deploy ClanWorld first (needed as engine arg for pools)
+        // 2. Deploy ClanWorld first (needed as engine arg for pools).
         ClanWorld game = new ClanWorld();
         console.log("CLAN_WORLD_CONTRACT_ADDRESS:", address(game));
 
-        // 2. Deploy 4 AMM pools (Phase 2: real constant-product pools)
+        // 3. Deploy 4 AMM pools (Phase 6.2: constant-product pools).
         StubPool woodGold = new StubPool(address(wood), address(gold), address(game));
         StubPool wheatGold = new StubPool(address(wheat), address(gold), address(game));
         StubPool fishGold = new StubPool(address(fish), address(gold), address(game));
@@ -49,8 +50,22 @@ contract Deploy is Script {
 
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
index 945490b..e1eb6c7 100644
--- a/packages/contracts/src/ClanWorld.sol
+++ b/packages/contracts/src/ClanWorld.sol
@@ -26,6 +26,7 @@ import {
     DerivedClanState,
     DerivedClansmanState,
     ClanOrder,
+    WithdrawResourcesData,
     OrderResult,
     PoolSeedConfig,
     LeaderboardEntry,
@@ -37,6 +38,7 @@ import {
     ActiveBanditView,
     RegionOccupant
 } from "./IClanWorld.sol";
+import {MinimalERC20} from "./MinimalERC20.sol";
 import {StubPool} from "./StubPool.sol";
 import {ReentrancyGuard} from "./util/ReentrancyGuard.sol";
 
@@ -58,6 +60,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     mapping(uint32 => Mission) private _missions; // keyed by clansmanId
     mapping(uint32 => WheatPlot[2]) private _wheatPlots; // [0]=west [1]=east
     mapping(uint64 => ScheduledMarketAction[]) private _scheduledMarketActions; // keyed by tick
+    mapping(uint32 => uint64) private _marketMissionCommitSequence; // clansmanId => FIFO sequence captured at submit
     mapping(uint8 => uint32[]) private _defendingClansByRegion; // home region => unique defending clanIds
     mapping(uint8 => mapping(uint32 => uint256)) private _defenderCountByRegionClan; // region => clanId => clansmen count
     mapping(uint32 => uint8) private _clansmanDefendingRegion; // clansmanId => defended home region
@@ -74,10 +77,10 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     // CONSTANTS — Wheat harvest rate (not in IClanWorld constants)
     // =========================================================================
 
+    uint64 private constant DEPOSIT_DURATION_TICKS = 1;
     uint256 private constant WHEAT_HARVEST_RATE = 20e18;
-    /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
-    uint256 public constant MAX_MARKET_ACTIONS_PER_TICK = 32;
-
+    uint256 public constant INITIAL_RESOURCE_POOL_SEED = 100_000e18;
+    uint256 public constant INITIAL_GOLD_POOL_SEED = 50_000e18;
     // =========================================================================
     // CONSTRUCTOR
     // =========================================================================
@@ -91,8 +94,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         _world.nextHeartbeatAtTick = 1; // first heartbeat will open tick 1
         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
         // i.e. ticks [100, 110)
-        _world.winterStartsAtTick =
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
         _world.winterActive = false;
         _treasury.treasuryOwner = msg.sender;
@@ -429,7 +431,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
     }
 
-    /// @dev Resolve one tick of action for a clansman that is in ACTING state.
+    /// @dev Resolve an action for a clansman that is in ACTING state.
     function _resolveAction(
         Clan storage clan,
         Clansman storage cs,
@@ -452,7 +454,13 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         } else if (action == ActionType.HarvestWheat) {
             _gatherWheat(clan, cs, m, clanId, tick, starving);
         } else if (action == ActionType.DepositResources) {
-            _doDeposit(clan, cs, m, clanId, tick);
+            // Deposit event ABI intentionally stores the current tick as uint32.
+            // forge-lint: disable-next-line(unsafe-typecast)
+            _doDeposit(clan, cs, m, clanId, uint32(tick));
+        } else if (action == ActionType.WithdrawResources) {
+            // Withdraw event ABI intentionally mirrors ResourcesDeposited's uint32 tick.
+            // forge-lint: disable-next-line(unsafe-typecast)
+            _doWithdrawResources(clan, cs, m, clanId, uint32(tick));
         } else if (action == ActionType.Wait) {
             // NOOP — worker stays ACTING (continuous), no transition needed
             // Wait mission is effectively persistent until interrupted
@@ -464,9 +472,6 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             // Phase 1 stub: check homebase, check resources; if ok, stub success
             _doBuilding(clan, cs, m, clanId, tick, action);
         } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
-            // Scheduled market actions: already enqueued at submitClanOrders time.
-            // Settlement resolves this action slot — just complete the mission.
-            // (Actual execution happened or will happen at heartbeat.)
             _completeMission(cs, m);
         }
     }
@@ -484,28 +489,27 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         bool starving,
         bytes32 tickSeed
     ) internal {
-        uint256 remaining = ClanWorldConstants.WOOD_CAP - cs.carryWood;
-        if (remaining == 0) {
+        if (cs.carryWood >= ClanWorldConstants.WOOD_CARRY_CAP) {
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
+        uint256 remaining = ClanWorldConstants.WOOD_CARRY_CAP - cs.carryWood;
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
+        if (cs.carryWood >= ClanWorldConstants.WOOD_CARRY_CAP) {
             _completeMission(cs, m);
         }
-        // else continuous — worker stays ACTING
     }
 
     function _gatherIron(
@@ -665,7 +669,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         // else continuous
     }
 
-    function _doDeposit(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint64 tick)
+    function _doDeposit(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint32 tick)
         internal
     {
         // Must be at homebase region
@@ -675,26 +679,55 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
+        _completeMission(cs, m);
+    }
+
+    function _doWithdrawResources(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint32 tick)
+        internal
+    {
+        if (cs.currentRegion != clan.baseRegion) {
+            _completeMission(cs, m);
+            return;
+        }
+
+        WithdrawResourcesData memory req = m.withdrawResources;
+        if (!_hasWithdrawRequest(req) || !_hasVaultResources(clan, req) || !_hasCarryCapacityForWithdraw(cs, req)) {
+            _completeMission(cs, m);
+            return;
+        }
+
+        clan.vaultWood -= req.wood;
+        clan.vaultIron -= req.iron;
+        clan.vaultWheat -= req.wheat;
+        clan.vaultFish -= req.fish;
+
+        cs.carryWood += req.wood;
+        cs.carryIron += req.iron;
+        cs.carryWheat += req.wheat;
+        cs.carryFish += req.fish;
+
+        emit ResourcesWithdrawn(clanId, cs.clansmanId, req.wood, req.iron, req.wheat, req.fish, tick);
         _completeMission(cs, m);
     }
 
@@ -1100,7 +1133,8 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
                         clansmanId: orders[i].clansmanId,
                         status: StatusCode.ERR_INVALID_ACTION,
                         cooldownEndsAtTs: 0,
-                        missionNonce: 0
+                        missionNonce: 0,
+                        marketMode: MarketExecutionMode.None
                     });
                 }
                 return results;
@@ -1168,7 +1202,10 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             }
         }
 
-        // Cooldown check
+        bool isMarketAction = order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell;
+
+        // Cooldown check. Market orders may still fall back to the scheduled path;
+        // only the immediate path requires the worker to be off cooldown.
         if (block.timestamp < cs.cooldownEndsAtTs) {
             result.status = StatusCode.ERR_COOLDOWN_ACTIVE;
             result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
@@ -1202,6 +1239,28 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             return result;
         }
 
+        if (
+            isMarketAction && ctx.fromRegion == ClanWorldConstants.REGION_UNICORN_TOWN
+                && cs.state == ClansmanState.WAITING && block.timestamp >= cs.cooldownEndsAtTs
+        ) {
+            ctx.newNonce = cs.lastMissionNonce + 1;
+            cs.lastMissionNonce = ctx.newNonce;
+
+            StatusCode marketStatus = _executeImmediateMarket(clanId, order, cs.clansmanId);
+
+            if (marketStatus == StatusCode.OK) {
+                cs.state = ClansmanState.WAITING;
+                cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
+            }
+            _clearDefender(cs.clansmanId);
+
+            result.status = marketStatus;
+            result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
+            result.missionNonce = ctx.newNonce;
+            result.marketMode = MarketExecutionMode.Immediate;
+            return result;
+        }
+
         // Capture existing mission state
         Mission storage existingM = _missions[order.clansmanId];
         ctx.wasActive = existingM.active;
@@ -1232,12 +1291,6 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
 
         _clearDefender(cs.clansmanId);
 
-        // v4.2 §8 L758: "executes at heartbeat closing tick T" where T = arrivalTick.
-        // executeAtTick = arrivalTick (not arrivalTick+1).
-        if (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell) {
-            _enqueueScheduledMarketAction(clanId, order, cs.clansmanId, ctx.arrivalTick, ctx.newNonce);
-        }
-
         if (order.action == ActionType.DefendBase && ctx.travelTicks == 0) {
             _registerDefender(ctx.gotoRegion, clanId, cs.clansmanId);
         }
@@ -1260,6 +1313,10 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         result.status = StatusCode.OK;
         result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
         result.missionNonce = ctx.newNonce;
+        result.marketMode = isMarketAction ? MarketExecutionMode.Scheduled : MarketExecutionMode.None;
+        if (isMarketAction) {
+            _enqueueScheduledMarketAction(clanId, cs.clansmanId, existingM);
+        }
         return result;
     }
 
@@ -1288,36 +1345,31 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         m.marketToken = order.marketToken;
         m.marketAmount = order.marketAmount;
         m.maxGoldIn = order.maxGoldIn;
+        m.withdrawResources = order.withdrawResources;
+
+        if (m.marketMode == MarketExecutionMode.Scheduled) {
+            _marketMissionCommitSequence[cs.clansmanId] = _world.nextCommitSequence++;
+        } else {
+            delete _marketMissionCommitSequence[cs.clansmanId];
+        }
     }
 
-    function _enqueueScheduledMarketAction(
-        uint32 clanId,
-        ClanOrder calldata order,
-        uint32 clansmanId,
-        uint64 executeAtTick,
-        uint64 missionNonce
-    ) internal {
+    function _enqueueScheduledMarketAction(uint32 clanId, uint32 clansmanId, Mission storage m) internal {
+        uint64 executeAtTick = m.settlesAtTick;
         ScheduledMarketAction memory sma = ScheduledMarketAction({
             executeAtTick: executeAtTick,
-            commitSequence: _world.nextCommitSequence++,
-            missionNonce: missionNonce,
+            commitSequence: _marketMissionCommitSequence[clansmanId],
+            missionNonce: m.nonce,
             clanId: clanId,
             clansmanId: clansmanId,
-            action: order.action,
-            marketToken: order.marketToken,
-            marketAmount: order.marketAmount,
-            maxGoldIn: order.maxGoldIn
+            action: m.action,
+            marketToken: m.marketToken,
+            marketAmount: m.marketAmount,
+            maxGoldIn: m.maxGoldIn
         });
         _scheduledMarketActions[executeAtTick].push(sma);
         emit ScheduledMarketActionCommitted(
-            executeAtTick,
-            sma.commitSequence,
-            clanId,
-            clansmanId,
-            order.action,
-            order.marketToken,
-            order.marketAmount,
-            order.maxGoldIn
+            executeAtTick, sma.commitSequence, clanId, clansmanId, m.action, m.marketToken, m.marketAmount, m.maxGoldIn
         );
     }
 
@@ -1359,22 +1411,28 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     // MARKET EXECUTION (Phase 2)
     // =========================================================================
 
-    /// @dev Execute up to MAX_MARKET_ACTIONS_PER_TICK scheduled market actions for the given tick.
-    ///      Overflow is appended to the next tick to keep heartbeat gas bounded.
+    /// @dev Execute the full scheduled market queue for the given tick, then delete it.
     function _executeScheduledMarketActions(uint64 tick) internal {
         ScheduledMarketAction[] storage actions = _scheduledMarketActions[tick];
         uint256 len = actions.length;
         if (len == 0) return;
 
-        uint256 processCount = len > MAX_MARKET_ACTIONS_PER_TICK ? MAX_MARKET_ACTIONS_PER_TICK : len;
+        _sortScheduledMarketActionsByCommitSequence(actions);
 
-        for (uint256 i = 0; i < processCount; i++) {
+        for (uint256 i = 0; i < len; i++) {
             ScheduledMarketAction storage sma = actions[i];
 
             // Validate clansman still belongs to the clan
             Clansman storage cs = _clansmen[sma.clansmanId];
             if (cs.clanId != sma.clanId || cs.state == ClansmanState.DEAD) {
-                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_CLANSMAN);
+                _emitMarketActionFailed(
+                    sma.clanId,
+                    sma.clansmanId,
+                    sma.action,
+                    MarketExecutionMode.Scheduled,
+                    StatusCode.ERR_INVALID_CLANSMAN,
+                    tick
+                );
                 continue;
             }
 
@@ -1383,59 +1441,75 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             // cannot use m.active as a validity signal here — check action type and nonce.
             Mission storage m = _missions[sma.clansmanId];
             if (m.action != sma.action || m.nonce != sma.missionNonce) {
-                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
+                _emitMarketActionFailed(
+                    sma.clanId,
+                    sma.clansmanId,
+                    sma.action,
+                    MarketExecutionMode.Scheduled,
+                    StatusCode.ERR_INVALID_ACTION,
+                    tick
+                );
                 continue;
             }
 
             if (sma.action == ActionType.MarketSell) {
                 try this._executeMarketSellExternal(
-                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.commitSequence
+                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount
+                ) returns (
+                    StatusCode marketStatus
                 ) {
-                // success
-                }
-                catch {
-                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
+                    marketStatus;
+                } catch {
+                    _handleMarketFailure(
+                        sma.clanId,
+                        sma.clansmanId,
+                        sma.action,
+                        MarketExecutionMode.Scheduled,
+                        StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
+                        tick
+                    );
                 }
             } else if (sma.action == ActionType.MarketBuy) {
                 try this._executeMarketBuyExternal(
-                    tick,
-                    sma.clanId,
-                    sma.clansmanId,
-                    sma.marketToken,
-                    sma.marketAmount,
-                    sma.maxGoldIn,
-                    sma.commitSequence
+                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.maxGoldIn
+                ) returns (
+                    StatusCode marketStatus
                 ) {
-                // success
-                }
-                catch {
-                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
+                    marketStatus;
+                } catch {
+                    _handleMarketFailure(
+                        sma.clanId,
+                        sma.clansmanId,
+                        sma.action,
+                        MarketExecutionMode.Scheduled,
+                        StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
+                        tick
+                    );
                 }
             }
         }
 
-        if (len > processCount) {
-            ScheduledMarketAction[] storage nextActions = _scheduledMarketActions[tick + 1];
-            for (uint256 i = processCount; i < len; i++) {
-                nextActions.push(actions[i]);
-            }
-            // Invariant: each tick queue executes in global commitSequence order, including
-            // older overflow actions merged into a tick that already has native actions.
-            _sortScheduledMarketActionsByCommitSequence(nextActions);
-        }
-
         delete _scheduledMarketActions[tick];
     }
 
     function _sortScheduledMarketActionsByCommitSequence(ScheduledMarketAction[] storage actions) internal {
-        for (uint256 i = 1; i < actions.length; i++) {
-            ScheduledMarketAction memory key = actions[i];
+        uint256 len = actions.length;
+        if (len <= 1) return;
+        ScheduledMarketAction[] memory arr = new ScheduledMarketAction[](len);
+        for (uint256 i = 0; i < len; i++) {
+            arr[i] = actions[i];
+        }
+        for (uint256 i = 1; i < len; i++) {
+            ScheduledMarketAction memory key = arr[i];
             uint256 j = i;
-            while (j > 0 && actions[j - 1].commitSequence > key.commitSequence) {
-                actions[j] = actions[j - 1];
+            while (j > 0 && arr[j - 1].commitSequence > key.commitSequence) {
+                arr[j] = arr[j - 1];
                 j--;
             }
-            actions[j] = key;
+            arr[j] = key;
+        }
+        for (uint256 i = 0; i < len; i++) {
+            actions[i] = arr[i];
         }
     }
 
@@ -1445,11 +1519,10 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         uint32 clanId,
         uint32 clansmanId,
         address token,
-        uint256 amount,
-        uint64 commitSequence
-    ) external {
+        uint256 amount
+    ) external returns (StatusCode) {
         require(msg.sender == address(this), "ClanWorld: internal only");
-        _executeMarketSell(closedTick, clanId, clansmanId, token, amount, commitSequence);
+        return _executeMarketSell(closedTick, clanId, clansmanId, token, amount);
     }
 
     /// @dev External wrapper for _executeMarketBuy — enables try/catch from heartbeat loop.
@@ -1459,11 +1532,10 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         uint32 clansmanId,
         address token,
         uint256 amountOut,
-        uint256 maxGoldIn,
-        uint64 commitSequence
-    ) external {
+        uint256 maxGoldIn
+    ) external returns (StatusCode) {
         require(msg.sender == address(this), "ClanWorld: internal only");
-        _executeMarketBuy(closedTick, clanId, clansmanId, token, amountOut, maxGoldIn, commitSequence);
+        return _executeMarketBuy(closedTick, clanId, clansmanId, token, amountOut, maxGoldIn);
     }
 
     /// @dev Map a resource token address to its pool address.
@@ -1475,6 +1547,27 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         return address(0);
     }
 
+    /// @dev Map a market token address to the canonical uint8 resource id used by market events.
+    function _marketResourceForToken(address token) internal view returns (uint8) {
+        if (token == _treasury.woodToken) return uint8(ResourceType.Wood);
+        if (token == _treasury.ironToken) return uint8(ResourceType.Iron);
+        if (token == _treasury.wheatToken) return uint8(ResourceType.Wheat);
+        if (token == _treasury.fishToken) return uint8(ResourceType.Fish);
+        if (token == _treasury.goldToken) return ClanWorldConstants.RESOURCE_GOLD;
+        revert("ClanWorld: invalid market resource");
+    }
+
+    function _emitMarketActionFailed(
+        uint32 clanId,
+        uint32 clansmanId,
+        ActionType action,
+        MarketExecutionMode mode,
+        StatusCode reason,
+        uint64 tick
+    ) internal {
+        emit MarketActionFailed(clanId, clansmanId, action, mode, reason, tick);
+    }
+
     /// @dev Add an amount of a resource token to the clan vault.
     function _addToVault(Clan storage clan, address token, uint256 amount) internal {
         if (token == _treasury.woodToken) {
@@ -1520,37 +1613,375 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         return false;
     }
 
-    /// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.
-    function _executeMarketSell(
-        uint64 closedTick,
+    function _hasCarryBalance(Clansman storage cs, address token, uint256 amount) internal view returns (bool) {
+        if (token == _treasury.woodToken) return cs.carryWood >= amount;
+        if (token == _treasury.ironToken) return cs.carryIron >= amount;
+        if (token == _treasury.wheatToken) return cs.carryWheat >= amount;
+        if (token == _treasury.fishToken) return cs.carryFish >= amount;
+        return false;
+    }
+
+    function _deductFromCarry(Clansman storage cs, address token, uint256 amount) internal returns (bool) {
+        if (token == _treasury.woodToken) {
+            if (cs.carryWood < amount) return false;
+            cs.carryWood -= amount;
+            return true;
+        }
+        if (token == _treasury.ironToken) {
+            if (cs.carryIron < amount) return false;
+            cs.carryIron -= amount;
+            return true;
+        }
+        if (token == _treasury.wheatToken) {
+            if (cs.carryWheat < amount) return false;
+            cs.carryWheat -= amount;
+            return true;
+        }
+        if (token == _treasury.fishToken) {
+            if (cs.carryFish < amount) return false;
+            cs.carryFish -= amount;
+            return true;
+        }
+        return false;
+    }
+
+    function _addToCarry(Clansman storage cs, address token, uint256 amount) internal {
+        if (token == _treasury.woodToken) {
+            cs.carryWood += amount;
+            return;
+        }
+        if (token == _treasury.ironToken) {
+            cs.carryIron += amount;
+            return;
+        }
+        if (token == _treasury.wheatToken) {
+            cs.carryWheat += amount;
+            return;
+        }
+        if (token == _treasury.fishToken) {
+            cs.carryFish += amount;
+            return;
+        }
+    }
+
+    /// @dev Check a clan vault balance without mutating storage.
+    function _hasVaultBalance(Clan storage clan, address token, uint256 amount) internal view returns (bool) {
+        if (token == _treasury.woodToken) return clan.vaultWood >= amount;
+        if (token == _treasury.ironToken) return clan.vaultIron >= amount;
+        if (token == _treasury.wheatToken) return clan.vaultWheat >= amount;
+        if (token == _treasury.fishToken) return clan.vaultFish >= amount;
+        return false;
+    }
+
+    function _hasWithdrawRequest(WithdrawResourcesData memory req) internal pure returns (bool) {
+        return req.wood > 0 || req.iron > 0 || req.wheat > 0 || req.fish > 0;
+    }
+
+    function _hasVaultResources(Clan storage clan, WithdrawResourcesData memory req) internal view returns (bool) {
+        return clan.vaultWood >= req.wood && clan.vaultIron >= req.iron && clan.vaultWheat >= req.wheat
+            && clan.vaultFish >= req.fish;
+    }
+
+    function _hasCarryCapacityForWithdraw(Clansman storage cs, WithdrawResourcesData memory req)
+        internal
+        view
+        returns (bool)
+    {
+        return req.wood <= _remainingCapacity(cs.carryWood, ClanWorldConstants.WOOD_CAP)
+            && req.iron <= _remainingCapacity(cs.carryIron, ClanWorldConstants.IRON_CAP)
+            && req.wheat <= _remainingCapacity(cs.carryWheat, ClanWorldConstants.WHEAT_CAP)
+            && req.fish <= _remainingCapacity(cs.carryFish, ClanWorldConstants.FISH_CAP);
+    }
+
+    function _remainingCapacity(uint256 carried, uint256 cap) internal pure returns (uint256) {
+        if (carried >= cap) return 0;
+        return cap - carried;
+    }
+
+    function _handleMarketFailure(
+        uint32 clanId,
+        uint32 clansmanId,
+        ActionType action,
+        MarketExecutionMode mode,
+        StatusCode reason,
+        uint64 tick
+    ) internal returns (StatusCode) {
+        Clansman storage cs = _clansmen[clansmanId];
+        if (cs.clansmanId != 0 && cs.state != ClansmanState.DEAD) {
+            cs.state = ClansmanState.WAITING;
+            cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
+        }
+        _emitMarketActionFailed(clanId, clansmanId, action, mode, reason, tick);
+        return reason;
+    }
+
+    function _remainingCarryForToken(Clansman storage cs, address token) internal view returns (uint256) {
+        uint256 carried;
+        uint256 cap;
+        if (token == _treasury.woodToken) {
+            carried = cs.carryWood;
+            cap = ClanWorldConstants.WOOD_CAP;
+        } else if (token == _treasury.ironToken) {
+            carried = cs.carryIron;
+            cap = ClanWorldConstants.IRON_CAP;
+        } else if (token == _treasury.wheatToken) {
+            carried = cs.carryWheat;
+            cap = ClanWorldConstants.WHEAT_CAP;
+        } else if (token == _treasury.fishToken) {
+            carried = cs.carryFish;
+            cap = ClanWorldConstants.FISH_CAP;
+        } else {
+            return 0;
+        }
+
+        if (carried >= cap) return 0;
+        return cap - carried;
+    }
+
+    function _executeImmediateMarket(uint32 clanId, ClanOrder calldata order, uint32 clansmanId)
+        internal
+        returns (StatusCode)
+    {
+        if (order.action == ActionType.MarketSell) {
+            return _executeImmediateMarketSell(clanId, clansmanId, order.marketToken, order.marketAmount);
+        }
+        return _executeImmediateMarketBuy(clanId, clansmanId, order.marketToken, order.marketAmount, order.maxGoldIn);
+    }
+
+    function _executeImmediateMarketSell(uint32 clanId, uint32 clansmanId, address token, uint256 amount)
+        internal
+        returns (StatusCode)
+    {
+        if (!_treasury.poolsSeeded) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketSell,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
+                _world.currentTick
+            );
+        }
+        address poolAddr = _poolFor(token);
+        if (poolAddr == address(0)) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketSell,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
+                _world.currentTick
+            );
+        }
+
+        Clan storage clan = _clans[clanId];
+        Clansman storage cs = _clansmen[clansmanId];
+        if (!_hasCarryBalance(cs, token, amount)) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketSell,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_MISSING_RESOURCES,
+                _world.currentTick
+            );
+        }
+
+        // CEI: deduct carry before external call
+        if (!_deductFromCarry(cs, token, amount)) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketSell,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_MISSING_RESOURCES,
+                _world.currentTick
+            );
+        }
+
+        try StubPool(poolAddr).swapExactInForOut(amount, 1) returns (uint256 goldOut) {
+            clan.goldBalance += goldOut;
+            emit ImmediateMarketActionExecuted(
+                clanId,
+                clansmanId,
+                ActionType.MarketSell,
+                _marketResourceForToken(token),
+                amount,
+                ClanWorldConstants.RESOURCE_GOLD,
+                goldOut,
+                _world.currentTick
+            );
+            return StatusCode.OK;
+        } catch {
+            _addToCarry(cs, token, amount); // restore carry on swap failure
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketSell,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
+                _world.currentTick
+            );
+        }
+    }
+
+    function _executeImmediateMarketBuy(
         uint32 clanId,
         uint32 clansmanId,
         address token,
-        uint256 amount,
-        uint64 commitSequence
-    ) internal {
+        uint256 amountOut,
+        uint256 maxGoldIn
+    ) internal returns (StatusCode) {
         if (!_treasury.poolsSeeded) {
-            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
-            return;
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
+                _world.currentTick
+            );
         }
         address poolAddr = _poolFor(token);
         if (poolAddr == address(0)) {
-            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
-            return;
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
+                _world.currentTick
+            );
+        }
+
+        Clansman storage cs = _clansmen[clansmanId];
+        if (amountOut > _remainingCarryForToken(cs, token)) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_CARRY_FULL,
+                _world.currentTick
+            );
+        }
+
+        uint256 goldIn;
+        try StubPool(poolAddr).getAmountInForExactOut(amountOut) returns (uint256 quotedGoldIn) {
+            goldIn = quotedGoldIn;
+        } catch {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
+                _world.currentTick
+            );
         }
 
         Clan storage clan = _clans[clanId];
-        if (!_deductFromVault(clan, token, amount)) {
-            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MISSING_RESOURCES);
-            return;
+        if (goldIn > maxGoldIn) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_MAX_GOLD_IN_EXCEEDED,
+                _world.currentTick
+            );
+        }
+        if (clan.goldBalance < goldIn) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_NOT_ENOUGH_GOLD,
+                _world.currentTick
+            );
+        }
+
+        try StubPool(poolAddr).swapExactOutForInWithMaxIn(amountOut, maxGoldIn) returns (uint256 actualGoldIn) {
+            clan.goldBalance -= actualGoldIn;
+            _addToCarry(cs, token, amountOut);
+            emit ImmediateMarketActionExecuted(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                ClanWorldConstants.RESOURCE_GOLD,
+                actualGoldIn,
+                _marketResourceForToken(token),
+                amountOut,
+                _world.currentTick
+            );
+            return StatusCode.OK;
+        } catch {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
+                _world.currentTick
+            );
+        }
+    }
+
+    /// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.
+    function _executeMarketSell(uint64 closedTick, uint32 clanId, uint32 clansmanId, address token, uint256 amount)
+        internal
+        returns (StatusCode)
+    {
+        if (!_treasury.poolsSeeded) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketSell,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
+                closedTick
+            );
+        }
+        address poolAddr = _poolFor(token);
+        if (poolAddr == address(0)) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketSell,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
+                closedTick
+            );
+        }
+
+        Clan storage clan = _clans[clanId];
+        Clansman storage cs = _clansmen[clansmanId];
+        if (!_deductFromCarry(cs, token, amount)) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketSell,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_MISSING_RESOURCES,
+                closedTick
+            );
         }
 
         uint256 goldOut = StubPool(poolAddr).sellResource(amount);
         clan.goldBalance += goldOut;
 
         emit ScheduledMarketActionExecuted(
-            closedTick, commitSequence, clanId, clansmanId, token, _treasury.goldToken, amount, goldOut
+            clanId,
+            clansmanId,
+            ActionType.MarketSell,
+            _marketResourceForToken(token),
+            amount,
+            ClanWorldConstants.RESOURCE_GOLD,
+            goldOut,
+            closedTick
         );
+        return StatusCode.OK;
     }
 
     /// @dev Execute a scheduled market buy: deduct gold from purse, credit resource to vault.
@@ -1560,43 +1991,107 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         uint32 clansmanId,
         address token,
         uint256 amountOut,
-        uint256 maxGoldIn,
-        uint64 commitSequence
-    ) internal {
+        uint256 maxGoldIn
+    ) internal returns (StatusCode) {
         if (!_treasury.poolsSeeded) {
-            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
-            return;
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
+                closedTick
+            );
         }
         address poolAddr = _poolFor(token);
         if (poolAddr == address(0)) {
-            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
-            return;
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
+                closedTick
+            );
         }
 
-        // Quote gold cost without updating reserves
-        uint256 goldIn = StubPool(poolAddr).quoteBuy(amountOut);
+        Clansman storage cs = _clansmen[clansmanId];
+        if (amountOut > _remainingCarryForToken(cs, token)) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_CARRY_FULL,
+                closedTick
+            );
+        }
+
+        uint256 goldIn;
+        try StubPool(poolAddr).quoteBuy(amountOut) returns (uint256 quotedGoldIn) {
+            goldIn = quotedGoldIn;
+        } catch {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
+                closedTick
+            );
+        }
 
         Clan storage clan = _clans[clanId];
 
         if (goldIn > maxGoldIn) {
-            emit MarketActionFailed(
-                clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_BUY_MAX_GOLD_EXCEEDED
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_MAX_GOLD_IN_EXCEEDED,
+                closedTick
             );
-            return;
         }
         if (clan.goldBalance < goldIn) {
-            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_NOT_ENOUGH_GOLD);
-            return;
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_NOT_ENOUGH_GOLD,
+                closedTick
+            );
+        }
+
+        uint256 actualGoldIn;
+        try StubPool(poolAddr).swapExactOutForInWithMaxIn(amountOut, maxGoldIn) returns (uint256 spentGold) {
+            actualGoldIn = spentGold;
+        } catch {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
+                closedTick
+            );
         }
 
-        // Execute — use return value to guard against any future pool divergence
-        uint256 actualGoldIn = StubPool(poolAddr).buyResource(amountOut);
         clan.goldBalance -= actualGoldIn;
-        _addToVault(clan, token, amountOut);
+        _addToCarry(cs, token, amountOut);
 
         emit ScheduledMarketActionExecuted(
-            closedTick, commitSequence, clanId, clansmanId, _treasury.goldToken, token, actualGoldIn, amountOut
+            clanId,
+            clansmanId,
+            ActionType.MarketBuy,
+            ClanWorldConstants.RESOURCE_GOLD,
+            actualGoldIn,
+            _marketResourceForToken(token),
+            amountOut,
+            closedTick
         );
+        return StatusCode.OK;
     }
 
     function _validateAction(Clan storage clan, Clansman storage cs, ClanOrder calldata order, uint8 gotoRegion)
@@ -1608,9 +2103,16 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
 
         if (action == ActionType.None) return StatusCode.ERR_INVALID_ACTION;
 
-        // DepositResources: must go to homebase
+        // DepositResources / WithdrawResources: must go to homebase
         if (action == ActionType.DepositResources) {
+            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
+        }
+        if (action == ActionType.WithdrawResources) {
             if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
+            WithdrawResourcesData memory req = order.withdrawResources;
+            if (!_hasWithdrawRequest(req)) return StatusCode.ERR_EMPTY_CARGO;
+            if (!_hasVaultResources(clan, req)) return StatusCode.ERR_MISSING_RESOURCES;
+            if (!_hasCarryCapacityForWithdraw(cs, req)) return StatusCode.ERR_CARRY_FULL;
         }
 
         // BuildWall / UpgradeBase / UpgradeMonument: must go to homebase
@@ -1670,10 +2172,18 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             ) {
                 return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
             }
-            // Market orders are always enqueued for the arrivalTick FIFO queue.
-            // _resolveAction records mission completion but does not execute any swap.
+            // Over-capacity buys are rejected at submission; no partial fills or overflow refunds.
+            if (action == ActionType.MarketBuy && order.marketAmount > _remainingCarryForToken(cs, tok)) {
+                return StatusCode.ERR_CARRY_FULL;
+            }
+            if (action == ActionType.MarketSell && !_hasCarryBalance(cs, tok, order.marketAmount)) {
+                return StatusCode.ERR_MISSING_RESOURCES;
+            }
+            // Immediate market orders execute during submitClanOrders when the
+            // worker is waiting in Unicorn Town and off cooldown. Other valid
+            // market orders enqueue when the scheduled mission resolves.
             if (cs.currentRegion == ClanWorldConstants.REGION_UNICORN_TOWN && cs.state == ClansmanState.WAITING) {
-                // Already at Unicorn Town — arrivalTick == currentTick, queued for next heartbeat.
+                // Already at Unicorn Town — immediate if off cooldown, scheduled otherwise.
             }
         }
 
@@ -1720,6 +2230,15 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
@@ -1732,6 +2251,11 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
@@ -1768,6 +2292,30 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
@@ -1801,10 +2349,13 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             return 4;
         }
 
+        if (action == ActionType.DepositResources || action == ActionType.WithdrawResources) {
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
diff --git a/packages/contracts/src/ClanWorldStub.sol b/packages/contracts/src/ClanWorldStub.sol
index 36ce29b..a3a58fe 100644
--- a/packages/contracts/src/ClanWorldStub.sol
+++ b/packages/contracts/src/ClanWorldStub.sol
@@ -26,6 +26,7 @@ import {
     DerivedClanState,
     DerivedClansmanState,
     ClanOrder,
+    WithdrawResourcesData,
     OrderResult,
     PoolSeedConfig,
     LeaderboardEntry,
@@ -51,8 +52,7 @@ contract ClanWorldStub is IClanWorld {
         _world.seasonEndTick = ClanWorldConstants.SEASON_DURATION_TICKS;
         _world.currentSeasonNumber = 1;
         _world.nextHeartbeatAtTick = _world.currentTick + 1;
-        _world.winterStartsAtTick =
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
         _world.winterActive = false;
 
@@ -137,6 +137,26 @@ contract ClanWorldStub is IClanWorld {
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
@@ -194,7 +214,8 @@ contract ClanWorldStub is IClanWorld {
             targetClanId: 0,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
     }
 
diff --git a/packages/contracts/src/IClanWorld.sol b/packages/contracts/src/IClanWorld.sol
index 2b80fbe..3b70ec2 100644
--- a/packages/contracts/src/IClanWorld.sol
+++ b/packages/contracts/src/IClanWorld.sol
@@ -40,15 +40,17 @@ library ClanWorldConstants {
     uint64 internal constant CLANSMAN_COOLDOWN_SECONDS = 60;
 
     // Carry caps (per clansman)
-    uint256 internal constant WOOD_CAP = 15e18;
+    uint256 internal constant WOOD_CARRY_CAP = 10e18;
+    uint256 internal constant WOOD_CAP = WOOD_CARRY_CAP;
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
@@ -82,6 +84,10 @@ library ClanWorldConstants {
     uint8 internal constant REGION_EAST_DOCKS = 7;
     uint8 internal constant REGION_DEEP_SEA = 8;
 
+    // Market event resource ids. ResourceType covers the four vault resources;
+    // gold is the quote asset emitted as resource id 4.
+    uint8 internal constant RESOURCE_GOLD = 4;
+
     // Sentinels
     uint32 internal constant CLAN_ID_NULL = 0; // valid clan IDs start at 1
     uint32 internal constant BANDIT_ID_NULL = 0;
@@ -139,7 +145,8 @@ enum ActionType {
     DefendBase,
     MarketBuy,
     MarketSell,
-    Wait
+    Wait,
+    WithdrawResources
 }
 
 enum MarketExecutionMode {
@@ -171,12 +178,13 @@ enum StatusCode {
     ERR_MARKET_UNSUPPORTED_TOKEN,
     ERR_IMMEDIATE_MARKET_NOT_ELIGIBLE,
     ERR_MARKET_BUY_OVER_CAPACITY,
-    ERR_MARKET_BUY_MAX_GOLD_EXCEEDED,
     ERR_WORLD_TICK_MISMATCH,
     ERR_NO_ACTIVE_BANDIT,
     ERR_SEASON_ENDED,
     ERR_NOT_ENOUGH_GOLD,
-    ERR_CARRY_FULL
+    ERR_CARRY_FULL,
+    ERR_LIQUIDITY_INSUFFICIENT,
+    ERR_MAX_GOLD_IN_EXCEEDED
 }
 
 // =============================================================================
@@ -188,8 +196,8 @@ struct WorldState {
     uint64 seasonStartTick;
     uint64 seasonEndTick;
     bool seasonFinalized;
-    uint64 currentSeasonNumber;   // 1-indexed; incremented each time seasonEndTick is crossed
-    uint64 nextHeartbeatAtTick;   // estimated tick that will be opened by the next heartbeat (for off-chain UI)
+    uint64 currentSeasonNumber; // 1-indexed; incremented each time seasonEndTick is crossed
+    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)
 
     uint64 nextHeartbeatAtTs;
     uint64 nextBanditSpawnEligibleTick;
@@ -295,6 +303,8 @@ struct Mission {
     address marketToken; // market token for buy/sell
     uint256 marketAmount; // exact-in for sell, exact-out for buy
     uint256 maxGoldIn; // market_buy only, 0 otherwise
+
+    WithdrawResourcesData withdrawResources; // WithdrawResources only
 }
 
 struct BanditTroop {
@@ -361,6 +371,20 @@ struct DerivedClansmanState {
 // WRITE INPUT / OUTPUT STRUCTS
 // =============================================================================
 
+struct DepositResourcesData {
+    uint256 wood;
+    uint256 iron;
+    uint256 wheat;
+    uint256 fish;
+}
+
+struct WithdrawResourcesData {
+    uint256 wood;
+    uint256 iron;
+    uint256 wheat;
+    uint256 fish;
+}
+
 struct ClanOrder {
     uint32 clansmanId;
     uint8 gotoRegion;
@@ -370,6 +394,8 @@ struct ClanOrder {
     address marketToken;
     uint256 marketAmount;
     uint256 maxGoldIn;
+
+    WithdrawResourcesData withdrawResources;
 }
 
 struct OrderResult {
@@ -377,6 +403,7 @@ struct OrderResult {
     StatusCode status;
     uint64 cooldownEndsAtTs;
     uint64 missionNonce;
+    MarketExecutionMode marketMode;
 }
 
 struct PoolSeedConfig {
@@ -533,11 +560,20 @@ interface IClanWorldEvents {
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
+    );
+    event ResourcesWithdrawn(
+        uint32 indexed clanId,
+        uint32 indexed clansmanId,
+        uint256 woodDelta,
+        uint256 ironDelta,
+        uint256 wheatDelta,
+        uint256 fishDelta,
+        uint32 tick
     );
 
     // ----- building -----
@@ -548,22 +584,26 @@ interface IClanWorldEvents {
     // ----- market -----
     event ImmediateMarketActionExecuted(
         uint32 indexed clanId,
-        uint32 indexed clansmanId,
-        address tokenIn,
-        address tokenOut,
+        uint32 csId,
+        ActionType action,
+        uint8 resourceIn,
         uint256 amountIn,
+        uint8 resourceOut,
         uint256 amountOut,
-        uint64 atTick
+        uint64 tick
     );
     event ScheduledMarketActionExecuted(
-        uint64 indexed executeAtTick,
-        uint64 indexed commitSequence,
         uint32 indexed clanId,
-        uint32 clansmanId,
-        address tokenIn,
-        address tokenOut,
+        uint32 csId,
+        ActionType action,
+        uint8 resourceIn,
         uint256 amountIn,
-        uint256 amountOut
+        uint8 resourceOut,
+        uint256 amountOut,
+        uint64 settledAtTick
+    );
+    event MarketActionFailed(
+        uint32 indexed clanId, uint32 csId, ActionType action, MarketExecutionMode mode, StatusCode reason, uint64 tick
     );
     event ScheduledMarketActionCommitted(
         uint64 indexed executeAtTick,
@@ -575,7 +615,8 @@ interface IClanWorldEvents {
         uint256 marketAmount,
         uint256 maxGoldIn
     );
-    event MarketActionFailed(uint32 indexed clanId, uint32 indexed clansmanId, ActionType action, StatusCode reason);
+    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
+    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);
 
     // ----- bandits -----
     event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
@@ -699,6 +740,12 @@ interface IClanWorld is IClanWorldEvents {
 
     function getTreasuryState() external view returns (TreasuryState memory);
 
+    function getResourceToken(uint8 resourceType) external view returns (address);
+
+    function getPool(uint8 resourceType) external view returns (address);
+
+    function getPrice(uint8 resourceType, uint256 amountIn) external view returns (uint256 amountOut);
+
     function getClan(uint32 clanId) external view returns (Clan memory);
 
     function getClansman(uint32 clansmanId) external view returns (Clansman memory);
diff --git a/packages/contracts/src/MinimalERC20.sol b/packages/contracts/src/MinimalERC20.sol
index b9ac0b8..386e48e 100644
--- a/packages/contracts/src/MinimalERC20.sol
+++ b/packages/contracts/src/MinimalERC20.sol
@@ -1,14 +1,15 @@
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
+    bool public treasurySeeded;
 
     mapping(address => uint256) public balanceOf;
     mapping(address => mapping(address => uint256)) public allowance;
@@ -19,21 +20,29 @@ contract MinimalERC20 {
     constructor(string memory name_, string memory symbol_) {
         name = name_;
         symbol = symbol_;
-        OWNER = msg.sender;
+        DEPLOYER = msg.sender;
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
+    modifier onlyDeployer() {
+        _onlyDeployer();
         _;
     }
 
-    function mint(address to, uint256 amount) external onlyOwner {
+    function seedTreasury(address treasury, uint256 amount) external onlyDeployer {
+        require(!treasurySeeded, "MinimalERC20: treasury seeded");
+        require(treasury != address(0), "MinimalERC20: zero treasury");
+
+        treasurySeeded = true;
+        _mint(treasury, amount);
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
diff --git a/packages/contracts/test/ActionTypeEnumStability.t.sol b/packages/contracts/test/ActionTypeEnumStability.t.sol
new file mode 100644
index 0000000..0c6ee16
--- /dev/null
+++ b/packages/contracts/test/ActionTypeEnumStability.t.sol
@@ -0,0 +1,25 @@
+// SPDX-License-Identifier: MIT
+pragma solidity ^0.8.34;
+
+import {Test} from "forge-std/Test.sol";
+import {ActionType} from "../src/IClanWorld.sol";
+
+contract ActionTypeEnumStabilityTest is Test {
+    function test_actionTypeNumericValuesAreStable() public pure {
+        assertEq(uint8(ActionType.None), 0, "None");
+        assertEq(uint8(ActionType.ChopWood), 1, "ChopWood");
+        assertEq(uint8(ActionType.MineIron), 2, "MineIron");
+        assertEq(uint8(ActionType.FishDocks), 3, "FishDocks");
+        assertEq(uint8(ActionType.FishDeepSea), 4, "FishDeepSea");
+        assertEq(uint8(ActionType.HarvestWheat), 5, "HarvestWheat");
+        assertEq(uint8(ActionType.DepositResources), 6, "DepositResources");
+        assertEq(uint8(ActionType.BuildWall), 7, "BuildWall");
+        assertEq(uint8(ActionType.UpgradeBase), 8, "UpgradeBase");
+        assertEq(uint8(ActionType.UpgradeMonument), 9, "UpgradeMonument");
+        assertEq(uint8(ActionType.DefendBase), 10, "DefendBase");
+        assertEq(uint8(ActionType.MarketBuy), 11, "MarketBuy");
+        assertEq(uint8(ActionType.MarketSell), 12, "MarketSell");
+        assertEq(uint8(ActionType.Wait), 13, "Wait");
+        assertEq(uint8(ActionType.WithdrawResources), 14, "WithdrawResources");
+    }
+}
diff --git a/packages/contracts/test/ClanWorld.t.sol b/packages/contracts/test/ClanWorld.t.sol
index 92781d3..2405ed7 100644
--- a/packages/contracts/test/ClanWorld.t.sol
+++ b/packages/contracts/test/ClanWorld.t.sol
@@ -25,12 +25,12 @@ import {
     Clansman,
     Mission,
     BanditTroop,
-    ScheduledMarketAction,
     DefenseContribution,
     PackedRoute,
     DerivedClanState,
     DerivedClansmanState,
     ClanOrder,
+    WithdrawResourcesData,
     OrderResult,
     PoolSeedConfig,
     LeaderboardEntry,
@@ -48,6 +48,26 @@ contract ClanWorldTestHarness is ClanWorld {
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
+
+    function setVault(uint32 clanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish) external {
+        _clans[clanId].vaultWood = wood;
+        _clans[clanId].vaultIron = iron;
+        _clans[clanId].vaultWheat = wheat;
+        _clans[clanId].vaultFish = fish;
+    }
+
+    function setClansmanForTest(uint32 csId, ClansmanState state, uint8 region, uint64 cooldownEndsAtTs) external {
+        _clansmen[csId].state = state;
+        _clansmen[csId].currentRegion = region;
+        _clansmen[csId].cooldownEndsAtTs = cooldownEndsAtTs;
+    }
 }
 
 contract ClanWorldTest is Test {
@@ -73,6 +93,10 @@ contract ClanWorldTest is Test {
 
     /// @dev Deploy tokens + pools, call initTreasury + seedPools. Returns wood token address.
     function _setupMarket() internal returns (address woodAddr) {
+        return _setupMarketWithWoodSeed(world.INITIAL_RESOURCE_POOL_SEED());
+    }
+
+    function _setupMarketWithWoodSeed(uint256 woodSeed) internal returns (address woodAddr) {
         woodToken = new MinimalERC20("Wood", "WOOD");
         ironToken = new MinimalERC20("Iron", "IRON");
         wheatToken = new MinimalERC20("Wheat", "WHEAT");
@@ -98,11 +122,25 @@ contract ClanWorldTest is Test {
 
         world.initTreasury(tokens, pools);
 
-        // Seed: 1000 wood + 1000 gold per pool (spot price 1 gold / 1 wood)
-        uint256 resSeed = 1000e18;
-        uint256 goldSeed = 1000e18;
+        // Seed: 100,000 resource + 50,000 gold per pool (spot price 0.5 gold / resource).
+        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
+        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
+        uint256 totalGoldSeed = goldSeed * 4;
+
+        woodToken.seedTreasury(address(this), woodSeed);
+        wheatToken.seedTreasury(address(this), resSeed);
+        fishToken.seedTreasury(address(this), resSeed);
+        ironToken.seedTreasury(address(this), resSeed);
+        goldToken.seedTreasury(address(this), totalGoldSeed);
+
+        woodToken.approve(address(world), woodSeed);
+        wheatToken.approve(address(world), resSeed);
+        fishToken.approve(address(world), resSeed);
+        ironToken.approve(address(world), resSeed);
+        goldToken.approve(address(world), totalGoldSeed);
+
         PoolSeedConfig memory cfg = PoolSeedConfig({
-            woodSeed: resSeed,
+            woodSeed: woodSeed,
             wheatSeed: resSeed,
             fishSeed: resSeed,
             ironSeed: resSeed,
@@ -116,6 +154,34 @@ contract ClanWorldTest is Test {
         return address(woodToken);
     }
 
+    function _initMarketWithoutSeed() internal returns (address woodAddr) {
+        woodToken = new MinimalERC20("Wood", "WOOD");
+        ironToken = new MinimalERC20("Iron", "IRON");
+        wheatToken = new MinimalERC20("Wheat", "WHEAT");
+        fishToken = new MinimalERC20("Fish", "FISH");
+        goldToken = new MinimalERC20("Gold", "GOLD");
+        blueprintToken = new MinimalERC20("BPRT", "BPRT");
+
+        address wAddr = address(world);
+        woodPool = new StubPool(address(woodToken), address(goldToken), wAddr);
+        ironPool = new StubPool(address(ironToken), address(goldToken), wAddr);
+        wheatPool = new StubPool(address(wheatToken), address(goldToken), wAddr);
+        fishPool = new StubPool(address(fishToken), address(goldToken), wAddr);
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
+        return address(woodToken);
+    }
+
     // -------------------------------------------------------------------------
     // Helpers
     // -------------------------------------------------------------------------
@@ -142,12 +208,38 @@ contract ClanWorldTest is Test {
             targetClanId: 0,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         vm.prank(elder);
         return world.submitClanOrders(clanId, orders);
     }
 
+    function _submitWithdrawOrder(
+        ClanWorldTestHarness harness,
+        uint32 clanId,
+        uint32 csId,
+        uint8 gotoRegion,
+        uint256 wood,
+        uint256 iron,
+        uint256 wheat,
+        uint256 fish
+    ) internal returns (OrderResult[] memory) {
+        ClanOrder[] memory orders = new ClanOrder[](1);
+        orders[0] = ClanOrder({
+            clansmanId: csId,
+            gotoRegion: gotoRegion,
+            action: ActionType.WithdrawResources,
+            targetClanId: 0,
+            marketToken: address(0),
+            marketAmount: 0,
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: wood, iron: iron, wheat: wheat, fish: fish})
+        });
+        vm.prank(elder);
+        return harness.submitClanOrders(clanId, orders);
+    }
+
     // -------------------------------------------------------------------------
     // Test 1: heartbeat rate limit
     // -------------------------------------------------------------------------
@@ -301,7 +393,8 @@ contract ClanWorldTest is Test {
             targetClanId: 0,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         // Invalid order: non-existent clansmanId
         orders[1] = ClanOrder({
@@ -311,7 +404,8 @@ contract ClanWorldTest is Test {
             targetClanId: 0,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
 
         vm.prank(elder);
@@ -359,7 +453,8 @@ contract ClanWorldTest is Test {
             targetClanId: 0,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
 
         vm.prank(elder);
@@ -447,7 +542,7 @@ contract ClanWorldTest is Test {
         vm.warp(block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS + 1);
         _submitOrder(clanId, cs0, homeRegion, ActionType.DepositResources);
 
-        // Advance travel back to homebase + deposit duration.
+        // Advance through travel back to homebase and the deposit's 1-tick transfer.
         (uint8 travelBack,) = world.quoteTravel(targetRegion, homeRegion);
         for (uint256 i = 0; i < uint256(travelBack) + world.getActionDuration(ActionType.DepositResources) + 1; i++) {
             _advanceTick();
@@ -465,6 +560,207 @@ contract ClanWorldTest is Test {
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
+    function test_withdrawResources_vaultToCarry_happyPath() public {
+        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
+        uint8 homeRegion = harness.getClan(clanId).baseRegion;
+        harness.setVault(clanId, 8e18, 3e18, 4e18, 2e18);
+
+        OrderResult[] memory r = _submitWithdrawOrder(harness, clanId, csId, homeRegion, 5e18, 2e18, 1e18, 1e18);
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "withdraw order should be accepted");
+
+        _advanceTickHarness(harness);
+        vm.expectEmit(true, true, false, true);
+        emit IClanWorldEvents.ResourcesWithdrawn(clanId, csId, 5e18, 2e18, 1e18, 1e18, 1);
+        _advanceTickHarness(harness);
+
+        Clan memory clan = harness.getClan(clanId);
+        Clansman memory cs = harness.getClansman(csId);
+        assertEq(clan.vaultWood, 3e18, "wood leaves vault");
+        assertEq(clan.vaultIron, 1e18, "iron leaves vault");
+        assertEq(clan.vaultWheat, 3e18, "wheat leaves vault");
+        assertEq(clan.vaultFish, 1e18, "fish leaves vault");
+        assertEq(cs.carryWood, 5e18, "wood enters carry");
+        assertEq(cs.carryIron, 2e18, "iron enters carry");
+        assertEq(cs.carryWheat, 1e18, "wheat enters carry");
+        assertEq(cs.carryFish, 1e18, "fish enters carry");
+    }
+
+    function test_withdrawResources_failsWhenInsufficientVault() public {
+        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
+        uint8 homeRegion = harness.getClan(clanId).baseRegion;
+        harness.setVault(clanId, 1e18, 0, 0, 0);
+
+        OrderResult[] memory r = _submitWithdrawOrder(harness, clanId, csId, homeRegion, 2e18, 0, 0, 0);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_MISSING_RESOURCES), "vault must cover request");
+        assertFalse(harness.getActiveMission(csId).active, "rejected withdraw should not install mission");
+    }
+
+    function test_withdrawResources_failsWhenCarryAtCap() public {
+        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
+        uint8 homeRegion = harness.getClan(clanId).baseRegion;
+        harness.setVault(clanId, 2e18, 0, 0, 0);
+        harness.setCarry(csId, ClanWorldConstants.WOOD_CAP, 0, 0, 0);
+
+        OrderResult[] memory r = _submitWithdrawOrder(harness, clanId, csId, homeRegion, 1e18, 0, 0, 0);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CARRY_FULL), "withdraw must fit carry cap");
+        assertFalse(harness.getActiveMission(csId).active, "rejected withdraw should not install mission");
+    }
+
+    function test_withdrawResources_failsWhenNotAtHomebase() public {
+        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
+        uint8 homeRegion = harness.getClan(clanId).baseRegion;
+        uint8 nonHomeRegion = homeRegion == ClanWorldConstants.REGION_FOREST
+            ? ClanWorldConstants.REGION_MOUNTAINS
+            : ClanWorldConstants.REGION_FOREST;
+        harness.setVault(clanId, 2e18, 0, 0, 0);
+
+        OrderResult[] memory r = _submitWithdrawOrder(harness, clanId, csId, nonHomeRegion, 1e18, 0, 0, 0);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_NOT_AT_HOMEBASE), "withdraw must target homebase");
+        assertFalse(harness.getActiveMission(csId).active, "rejected withdraw should not install mission");
+    }
+
+    function test_withdrawThenMarketSell_endToEnd() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        address woodAddr = _setupMarket();
+        uint32 clanId = _mintClan();
+        uint32 csId = _firstCs(clanId);
+        uint8 homeRegion = harness.getClan(clanId).baseRegion;
+        harness.setVault(clanId, 6e18, 0, 0, 0);
+
+        uint256 goldBefore = harness.getClan(clanId).goldBalance;
+        OrderResult[] memory withdraw = _submitWithdrawOrder(harness, clanId, csId, homeRegion, 5e18, 0, 0, 0);
+        assertEq(uint8(withdraw[0].status), uint8(StatusCode.OK), "withdraw should start wheelbarrow flow");
+
+        _advanceTickHarness(harness);
+        _advanceTickHarness(harness);
+        assertEq(harness.getClansman(csId).carryWood, 5e18, "worker loaded from vault");
+        assertEq(harness.getClan(clanId).vaultWood, 1e18, "vault stockpile reduced");
+
+        vm.warp(block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS + 1);
+        OrderResult[] memory sell = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 5e18, 0);
+        assertEq(uint8(sell[0].status), uint8(StatusCode.OK), "loaded worker can sell at market");
+
+        uint64 executeAtTick = world.getActiveMission(csId).settlesAtTick;
+        while (world.getWorldState().currentTick <= executeAtTick) {
+            _advanceTick();
+        }
+
+        assertEq(harness.getClansman(csId).carryWood, 0, "sold wood leaves carry");
+        assertGt(harness.getClan(clanId).goldBalance, goldBefore, "market sale credits clan gold");
+
+        vm.warp(block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS + 1);
+        OrderResult[] memory backHome = _submitOrder(clanId, csId, homeRegion, ActionType.Wait);
+        assertEq(uint8(backHome[0].status), uint8(StatusCode.OK), "worker can return home after sale");
+        uint64 returnTick = world.getActiveMission(csId).settlesAtTick;
+        while (world.getWorldState().currentTick <= returnTick) {
+            _advanceTick();
+        }
+        assertEq(harness.getClansman(csId).currentRegion, homeRegion, "worker returns to homebase");
+    }
+
     function test_quoteTravel_outOfBounds_returnsZero() public {
         (uint8 ticks, bytes8 path) = world.quoteTravel(9, 1);
         assertEq(ticks, 0, "out-of-range src should return 0 ticks");
@@ -511,7 +807,8 @@ contract ClanWorldTest is Test {
             targetClanId: 0,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         vm.prank(elder);
         OrderResult[] memory results = world.submitClanOrders(clanId, orders);
@@ -576,7 +873,8 @@ contract ClanWorldTest is Test {
             targetClanId: 0,
             marketToken: token,
             marketAmount: amount,
-            maxGoldIn: maxGold
+            maxGoldIn: maxGold,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         vm.prank(elder);
         return world.submitClanOrders(clanId, orders);
@@ -600,7 +898,8 @@ contract ClanWorldTest is Test {
                 targetClanId: 0,
                 marketToken: token,
                 marketAmount: 1e18,
-                maxGoldIn: 0
+                maxGoldIn: 0,
+                withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
             });
         }
         vm.prank(elder);
@@ -611,38 +910,357 @@ contract ClanWorldTest is Test {
         return count;
     }
 
+    function _advanceUntilNextHeartbeatCloses(uint64 tick) internal {
+        while (world.getWorldState().currentTick < tick) {
+            _advanceTick();
+        }
+    }
+
     // Helper: get the first clansman id for a clan
     function _firstCs(uint32 clanId) internal view returns (uint32) {
         return world.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
     }
 
+    function _setupHarnessClanAt(ClansmanState state, uint8 region, uint64 cooldownEndsAtTs)
+        internal
+        returns (ClanWorldTestHarness harness, address woodAddr, uint32 clanId, uint32 csId)
+    {
+        return _setupHarnessClanAtWithWoodSeed(state, region, cooldownEndsAtTs, world.INITIAL_RESOURCE_POOL_SEED());
+    }
+
+    function _setupHarnessClanAtWithWoodSeed(
+        ClansmanState state,
+        uint8 region,
+        uint64 cooldownEndsAtTs,
+        uint256 woodSeed
+    ) internal returns (ClanWorldTestHarness harness, address woodAddr, uint32 clanId, uint32 csId) {
+        harness = new ClanWorldTestHarness();
+        world = harness;
+        woodAddr = _setupMarketWithWoodSeed(woodSeed);
+        clanId = _mintClan();
+        csId = _firstCs(clanId);
+        harness.setClansmanForTest(csId, state, region, cooldownEndsAtTs);
+    }
+
+    function test_immediateMarketSell_executesInSubmitTx() public {
+        (ClanWorldTestHarness harness, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
+        harness.setCarry(csId, 10e18, 0, 0, 0);
+
+        Clan memory beforeClan = world.getClan(clanId);
+        Clansman memory beforeCs = world.getClansman(csId);
+        uint256 expectedGoldOut = woodPool.getAmountOutForExactIn(5e18);
+
+        vm.expectEmit(true, false, false, true);
+        emit IClanWorldEvents.ImmediateMarketActionExecuted(
+            clanId,
+            csId,
+            ActionType.MarketSell,
+            uint8(ResourceType.Wood),
+            5e18,
+            ClanWorldConstants.RESOURCE_GOLD,
+            expectedGoldOut,
+            world.getWorldState().currentTick
+        );
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 5e18, 0);
+
+        Clan memory afterClan = world.getClan(clanId);
+        Clansman memory cs = world.getClansman(csId);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "immediate sell should be accepted");
+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "sell should be immediate");
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "vault wood should be unchanged");
+        assertEq(cs.carryWood, beforeCs.carryWood - 5e18, "carry wood should be sold immediately");
+        assertGt(afterClan.goldBalance, beforeClan.goldBalance, "gold should be credited immediately");
+        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "worker should remain waiting");
+        assertEq(cs.currentRegion, ClanWorldConstants.REGION_UNICORN_TOWN, "worker should stay in town");
+        assertGt(cs.cooldownEndsAtTs, block.timestamp, "immediate action should consume cooldown");
+        assertFalse(world.getActiveMission(csId).active, "immediate action should not install a mission");
+        assertEq(world.getScheduledMarketActionsForTick(world.getWorldState().currentTick).length, 0, "no queue entry");
+    }
+
+    function test_immediateMarket_townOnCooldown_fallsBackToScheduled() public {
+        uint64 cooldownEndsAt = uint64(block.timestamp + 30);
+        (, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, cooldownEndsAt);
+
+        uint256 goldBefore = world.getClan(clanId).goldBalance;
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_COOLDOWN_ACTIVE), "market order on cooldown must be rejected");
+        assertEq(world.getClan(clanId).goldBalance, goldBefore, "cooldown rejection should not trade");
+    }
+
+    function test_immediateMarket_notInTown_fallsBackToScheduled() public {
+        (ClanWorldTestHarness harness, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_FOREST, 0);
+        harness.setCarry(csId, 2e18, 0, 0, 0);
+
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
+        Mission memory m = world.getActiveMission(csId);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "out-of-town market order should schedule");
+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Scheduled), "out-of-town should be scheduled");
+        assertTrue(m.active, "scheduled fallback should install a mission");
+        assertEq(world.getScheduledMarketActionsForTick(m.settlesAtTick).length, 1, "scheduled action queues at submit");
+    }
+
+    function test_immediateMarket_busyWorker_fallsBackToScheduled() public {
+        (ClanWorldTestHarness harness, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.ACTING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
+        harness.setCarry(csId, 2e18, 0, 0, 0);
+
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
+        Mission memory m = world.getActiveMission(csId);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "busy market worker should schedule");
+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Scheduled), "busy worker should be scheduled");
+        assertTrue(m.active, "scheduled fallback should install a mission");
+        assertEq(world.getScheduledMarketActionsForTick(m.settlesAtTick).length, 1, "scheduled action queues at submit");
+    }
+
+    function test_scheduledMarket_queueVisibleAtSubmit() public {
+        (ClanWorldTestHarness harness, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_FOREST, 0);
+        harness.setCarry(csId, 2e18, 0, 0, 0);
+
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
+        Mission memory m = world.getActiveMission(csId);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "scheduled sell should be accepted");
+        assertEq(world.getScheduledMarketActionsForTick(m.settlesAtTick).length, 1, "queue visible immediately");
+        assertEq(world.getMarketState().nextTickQueue.length, 0, "future queue stays off next tick when not next tick");
+    }
+
+    function test_immediateMarketBuy_executesWhenMaxGoldSatisfied() public {
+        (, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
+
+        Clan memory beforeClan = world.getClan(clanId);
+        Clansman memory beforeCs = world.getClansman(csId);
+        uint256 expectedGoldIn = woodPool.getAmountInForExactOut(1e18);
+
+        vm.expectEmit(true, false, false, true);
+        emit IClanWorldEvents.ImmediateMarketActionExecuted(
+            clanId,
+            csId,
+            ActionType.MarketBuy,
+            ClanWorldConstants.RESOURCE_GOLD,
+            expectedGoldIn,
+            uint8(ResourceType.Wood),
+            1e18,
+            world.getWorldState().currentTick
+        );
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 2e18);
+
+        Clan memory afterClan = world.getClan(clanId);
+        Clansman memory afterCs = world.getClansman(csId);
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "immediate buy should be accepted");
+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "buy should be immediate");
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "vault wood should be unchanged");
+        assertGt(afterCs.carryWood, beforeCs.carryWood, "carry wood should increase immediately");
+        assertLt(afterClan.goldBalance, beforeClan.goldBalance, "gold should be debited immediately");
+        assertFalse(world.getActiveMission(csId).active, "immediate buy should not install a mission");
+    }
+
+    function test_immediateMarket_insufficientLiquidityFailsAndConsumesCooldown() public {
+        (, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAtWithWoodSeed(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0, 5e18);
+
+        Clan memory beforeClan = world.getClan(clanId);
+
+        vm.expectEmit(true, false, false, true);
+        emit IClanWorldEvents.MarketActionFailed(
+            clanId,
+            csId,
+            ActionType.MarketBuy,
+            MarketExecutionMode.Immediate,
+            StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
+            world.getWorldState().currentTick
+        );
+        OrderResult[] memory r =
+            _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 6e18, type(uint256).max);
+
+        Clan memory afterClan = world.getClan(clanId);
+        Clansman memory cs = world.getClansman(csId);
+
+        assertEq(
+            uint8(r[0].status), uint8(StatusCode.ERR_LIQUIDITY_INSUFFICIENT), "failed immediate buy should propagate"
+        );
+        assertEq(
+            uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failed immediate action stays immediate"
+        );
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "failed buy should not credit resources");
+        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
+        assertGt(cs.cooldownEndsAtTs, block.timestamp, "failed immediate action should consume cooldown");
+        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "failed immediate worker waits");
+        assertFalse(world.getActiveMission(csId).active, "failed immediate action should not schedule fallback");
+    }
+
+    function test_immediateMarketBuy_maxGoldExceededFailsAndConsumesCooldown() public {
+        (, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
+
+        Clan memory beforeClan = world.getClan(clanId);
+
+        vm.expectEmit(true, false, false, true);
+        emit IClanWorldEvents.MarketActionFailed(
+            clanId,
+            csId,
+            ActionType.MarketBuy,
+            MarketExecutionMode.Immediate,
+            StatusCode.ERR_MAX_GOLD_IN_EXCEEDED,
+            world.getWorldState().currentTick
+        );
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 0);
+
+        Clan memory afterClan = world.getClan(clanId);
+        Clansman memory cs = world.getClansman(csId);
+
+        assertEq(
+            uint8(r[0].status), uint8(StatusCode.ERR_MAX_GOLD_IN_EXCEEDED), "failed immediate buy should propagate"
+        );
+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failed buy should report immediate");
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "failed buy should not credit resources");
+        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
+        assertGt(cs.cooldownEndsAtTs, block.timestamp, "failed immediate action should consume cooldown");
+        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "failed immediate worker waits");
+        assertFalse(world.getActiveMission(csId).active, "failed immediate action should not schedule fallback");
+    }
+
+    function test_immediateMarketBuy_insufficientGoldFailsAndConsumesCooldown() public {
+        (, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
+
+        Clan memory beforeClan = world.getClan(clanId);
+
+        vm.expectEmit(true, false, false, true);
+        emit IClanWorldEvents.MarketActionFailed(
+            clanId,
+            csId,
+            ActionType.MarketBuy,
+            MarketExecutionMode.Immediate,
+            StatusCode.ERR_NOT_ENOUGH_GOLD,
+            world.getWorldState().currentTick
+        );
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 7e18, 10e18);
+
+        Clan memory afterClan = world.getClan(clanId);
+        Clansman memory cs = world.getClansman(csId);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_NOT_ENOUGH_GOLD), "failed immediate buy should propagate");
+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failed buy should report immediate");
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "failed buy should not credit resources");
+        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
+        assertGt(cs.cooldownEndsAtTs, block.timestamp, "failed immediate action should consume cooldown");
+        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "failed immediate worker waits");
+        assertFalse(world.getActiveMission(csId).active, "failed immediate action should not schedule fallback");
+    }
+
+    function test_immediateMarketBuy_overCapacityRejectsAtSubmit() public {
+        (ClanWorldTestHarness harness, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
+        harness.setCarry(csId, ClanWorldConstants.WOOD_CAP - 5e17, 0, 0, 0);
+
+        Clan memory beforeClan = world.getClan(clanId);
+        Clansman memory beforeCs = world.getClansman(csId);
+
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 10e18);
+
+        Clan memory afterClan = world.getClan(clanId);
+        Clansman memory cs = world.getClansman(csId);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CARRY_FULL), "over-capacity buy should be rejected");
+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.None), "rejected buy has no mode");
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "failed buy should not credit resources");
+        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
+        assertEq(cs.cooldownEndsAtTs, beforeCs.cooldownEndsAtTs, "rejected buy should not consume cooldown");
+        assertEq(uint8(cs.state), uint8(beforeCs.state), "rejected buy should not change worker state");
+        assertFalse(world.getActiveMission(csId).active, "rejected buy should not install a mission");
+    }
+
+    function test_marketSell_emptyCarry_rejectsAtSubmit_noTickConsumed() public {
+        (, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
+
+        Clansman memory beforeCs = world.getClansman(csId);
+        uint64 tickBefore = world.getWorldState().currentTick;
+        uint256 goldBefore = world.getClan(clanId).goldBalance;
+
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
+
+        Clansman memory afterCs = world.getClansman(csId);
+        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_MISSING_RESOURCES), "empty carry sell rejects at submit");
+        assertEq(world.getWorldState().currentTick, tickBefore, "submit rejection should not advance tick");
+        assertEq(afterCs.cooldownEndsAtTs, beforeCs.cooldownEndsAtTs, "submit rejection should not consume cooldown");
+        assertEq(uint8(afterCs.state), uint8(beforeCs.state), "submit rejection should not change worker state");
+        assertEq(world.getClan(clanId).goldBalance, goldBefore, "submit rejection should not trade");
+        assertFalse(world.getActiveMission(csId).active, "submit rejection should not install mission");
+    }
+
+    function test_immediateMarketSell_failurePropagatesStatus() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        address woodAddr = _initMarketWithoutSeed();
+        uint32 clanId = _mintClan();
+        uint32 csId = _firstCs(clanId);
+        harness.setClansmanForTest(csId, ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
+        harness.setCarry(csId, 2e18, 0, 0, 0);
+
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
+
+        assertEq(
+            uint8(r[0].status),
+            uint8(StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN),
+            "immediate sell failure should propagate"
+        );
+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failure still used immediate path");
+        assertFalse(world.getActiveMission(csId).active, "failed immediate sell should not install a mission");
+    }
+
     // -------------------------------------------------------------------------
     // Test 11: sell_creditsGold — after scheduled sell, clan.goldBalance > starter gold
     // -------------------------------------------------------------------------
 
     function test_sell_creditsGold() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
         address woodAddr = _setupMarket();
         uint32 clanId = _mintClan();
         uint32 csId = _firstCs(clanId);
+        // Give clansman carry wood so scheduled sell has something to sell
+        harness.setCarry(csId, 10e18, 0, 0, 0);
 
-        // Clan starts with 20 wood in vault (starter pack)
         uint256 goldBefore = world.getClan(clanId).goldBalance;
 
-        // Submit sell order — clansman travels to Unicorn Town then executes at actionStartTick
+        // Submit sell order — clansman travels to Unicorn Town then executes when the market mission settles
         OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 5e18, 0);
         assertEq(uint8(r[0].status), uint8(StatusCode.OK), "sell order should be accepted");
 
         // Find out which tick the action fires
         Mission memory m = world.getActiveMission(csId);
-        uint64 executeAtTick = m.actionStartTick;
+        uint64 executeAtTick = m.settlesAtTick;
+        uint256 expectedGoldOut = woodPool.getAmountOutForExactIn(5e18);
 
-        // Advance ticks until heartbeat closes executeAtTick
+        // Advance ticks until the heartbeat before executeAtTick
         uint64 curTick = world.getWorldState().currentTick;
-        uint256 ticksNeeded = uint256(executeAtTick - curTick) + 1;
-        for (uint256 i = 0; i < ticksNeeded; i++) {
+        for (uint256 i = 0; i < uint256(executeAtTick - curTick); i++) {
             _advanceTick();
         }
 
+        vm.expectEmit(true, false, false, true);
+        emit IClanWorldEvents.ScheduledMarketActionExecuted(
+            clanId,
+            csId,
+            ActionType.MarketSell,
+            uint8(ResourceType.Wood),
+            5e18,
+            ClanWorldConstants.RESOURCE_GOLD,
+            expectedGoldOut,
+            executeAtTick
+        );
+        _advanceTick();
+
         // Settle clan to apply any mission resolution
         world.settleClan(clanId);
 
@@ -665,13 +1283,14 @@ contract ClanWorldTest is Test {
 
         uint256 goldBefore = world.getClan(clanId).goldBalance;
         uint256 vaultWoodBefore = world.getClan(clanId).vaultWood;
+        uint256 carryWoodBefore = world.getClansman(csId).carryWood;
 
         // Submit buy order for 1e18 wood, maxGoldIn = generous 500e18
         OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 500e18);
         assertEq(uint8(r[0].status), uint8(StatusCode.OK), "buy order should be accepted");
 
         Mission memory m = world.getActiveMission(csId);
-        uint64 executeAtTick = m.actionStartTick;
+        uint64 executeAtTick = m.settlesAtTick;
 
         uint64 curTick = world.getWorldState().currentTick;
         uint256 ticksNeeded = uint256(executeAtTick - curTick) + 1;
@@ -679,48 +1298,151 @@ contract ClanWorldTest is Test {
             _advanceTick();
         }
 
-        world.settleClan(clanId);
+        world.settleClan(clanId);
+
+        uint256 goldAfter = world.getClan(clanId).goldBalance;
+        uint256 vaultWoodAfter = world.getClan(clanId).vaultWood;
+        uint256 carryWoodAfter = world.getClansman(csId).carryWood;
+        assertLt(goldAfter, goldBefore, "gold should decrease after buy");
+        assertEq(vaultWoodAfter, vaultWoodBefore, "vault wood should be unchanged after buy");
+        assertGt(carryWoodAfter, carryWoodBefore, "carry wood should increase after buy");
+    }
+
+    // -------------------------------------------------------------------------
+    // Test 13: buy_maxGoldIn — buy fails with ERR_MAX_GOLD_IN_EXCEEDED
+    // -------------------------------------------------------------------------
+
+    function test_buy_maxGoldIn() public {
+        address woodAddr = _setupMarket();
+        uint32 clanId = _mintClan();
+        uint32 csId = _firstCs(clanId);
+
+        // maxGoldIn = 0 (will always be exceeded for any nonzero buy)
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 0);
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "order submission should succeed");
+
+        Mission memory m = world.getActiveMission(csId);
+        uint64 executeAtTick = m.settlesAtTick;
+        uint64 curTick = world.getWorldState().currentTick;
+
+        // Advance all ticks UP TO (but not including) the execute tick
+        if (executeAtTick > curTick) {
+            for (uint256 i = 0; i < uint256(executeAtTick - curTick); i++) {
+                _advanceTick();
+            }
+        }
+
+        // Now the next heartbeat will close executeAtTick — that's when MarketActionFailed fires
+        // Place expectEmit right before the final heartbeat
+        vm.expectEmit(true, false, false, true);
+        emit IClanWorldEvents.MarketActionFailed(
+            clanId,
+            csId,
+            ActionType.MarketBuy,
+            MarketExecutionMode.Scheduled,
+            StatusCode.ERR_MAX_GOLD_IN_EXCEEDED,
+            executeAtTick
+        );
+        _advanceTick();
+
+        Clansman memory cs = world.getClansman(csId);
+
+        // Verify gold balance unchanged (buy failed)
+        assertEq(world.getClan(clanId).goldBalance, 3e18, "gold should be unchanged after failed buy");
+        assertGt(cs.cooldownEndsAtTs, block.timestamp, "failed scheduled buy should consume cooldown");
+        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "failed scheduled worker waits");
+        assertEq(world.getScheduledMarketActionsForTick(executeAtTick).length, 0, "queue should be deleted");
+    }
+
+    function test_scheduledMarketBuy_insufficientGoldFailsAndConsumesCooldown() public {
+        address woodAddr = _setupMarket();
+        uint32 clanId = _mintClan();
+        uint32 csId = _firstCs(clanId);
+
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 7e18, 10e18);
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "buy order should be accepted");
+
+        Mission memory m = world.getActiveMission(csId);
+        uint64 executeAtTick = m.settlesAtTick;
+        Clan memory beforeClan = world.getClan(clanId);
+
+        _advanceUntilNextHeartbeatCloses(executeAtTick);
+        vm.expectEmit(true, false, false, true);
+        emit IClanWorldEvents.MarketActionFailed(
+            clanId,
+            csId,
+            ActionType.MarketBuy,
+            MarketExecutionMode.Scheduled,
+            StatusCode.ERR_NOT_ENOUGH_GOLD,
+            executeAtTick
+        );
+        _advanceTick();
+
+        Clan memory afterClan = world.getClan(clanId);
+        Clansman memory cs = world.getClansman(csId);
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "failed buy should not credit resources");
+        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
+        assertGt(cs.cooldownEndsAtTs, block.timestamp, "failed scheduled buy should consume cooldown");
+        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "failed scheduled worker waits");
+        assertEq(world.getScheduledMarketActionsForTick(executeAtTick).length, 0, "queue should be deleted");
+    }
+
+    function test_scheduledMarketBuy_overCapacityRejectsAtSubmit() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        address woodAddr = _setupMarket();
+        uint32 clanId = _mintClan();
+        uint32 csId = _firstCs(clanId);
+        harness.setCarry(csId, ClanWorldConstants.WOOD_CAP - 5e17, 0, 0, 0);
+
+        Clan memory beforeClan = world.getClan(clanId);
+        Clansman memory beforeCs = world.getClansman(csId);
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 10e18);
 
-        uint256 goldAfter = world.getClan(clanId).goldBalance;
-        uint256 vaultWoodAfter = world.getClan(clanId).vaultWood;
-        assertLt(goldAfter, goldBefore, "gold should decrease after buy");
-        assertGt(vaultWoodAfter, vaultWoodBefore, "vault wood should increase after buy");
-    }
+        Clan memory afterClan = world.getClan(clanId);
+        Clansman memory cs = world.getClansman(csId);
 
-    // -------------------------------------------------------------------------
-    // Test 13: buy_maxGoldIn — buy fails with ERR_MARKET_BUY_MAX_GOLD_EXCEEDED
-    // -------------------------------------------------------------------------
+        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CARRY_FULL), "over-capacity buy should be rejected");
+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.None), "rejected buy has no mode");
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "rejected buy should not credit resources");
+        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "rejected buy should not debit gold");
+        assertEq(cs.cooldownEndsAtTs, beforeCs.cooldownEndsAtTs, "rejected buy should not consume cooldown");
+        assertEq(uint8(cs.state), uint8(beforeCs.state), "rejected buy should not change worker state");
+        assertFalse(world.getActiveMission(csId).active, "rejected buy should not install a mission");
+    }
 
-    function test_buy_maxGoldIn() public {
-        address woodAddr = _setupMarket();
+    function test_scheduledMarketBuy_insufficientLiquidityFailsAndConsumesCooldown() public {
+        address woodAddr = _setupMarketWithWoodSeed(5e18);
         uint32 clanId = _mintClan();
         uint32 csId = _firstCs(clanId);
 
-        // maxGoldIn = 0 (will always be exceeded for any nonzero buy)
-        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 0);
-        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "order submission should succeed");
+        OrderResult[] memory r =
+            _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 6e18, type(uint256).max);
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "buy order should be accepted");
 
         Mission memory m = world.getActiveMission(csId);
-        uint64 executeAtTick = m.actionStartTick;
-        uint64 curTick = world.getWorldState().currentTick;
-
-        // Advance all ticks UP TO (but not including) the execute tick
-        if (executeAtTick > curTick) {
-            for (uint256 i = 0; i < uint256(executeAtTick - curTick); i++) {
-                _advanceTick();
-            }
-        }
+        uint64 executeAtTick = m.settlesAtTick;
+        Clan memory beforeClan = world.getClan(clanId);
 
-        // Now the next heartbeat will close executeAtTick — that's when MarketActionFailed fires
-        // Place expectEmit right before the final heartbeat
-        vm.expectEmit(true, true, false, true);
+        _advanceUntilNextHeartbeatCloses(executeAtTick);
+        vm.expectEmit(true, false, false, true);
         emit IClanWorldEvents.MarketActionFailed(
-            clanId, csId, ActionType.MarketBuy, StatusCode.ERR_MARKET_BUY_MAX_GOLD_EXCEEDED
+            clanId,
+            csId,
+            ActionType.MarketBuy,
+            MarketExecutionMode.Scheduled,
+            StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
+            executeAtTick
         );
         _advanceTick();
 
-        // Verify gold balance unchanged (buy failed)
-        assertEq(world.getClan(clanId).goldBalance, 3e18, "gold should be unchanged after failed buy");
+        Clan memory afterClan = world.getClan(clanId);
+        Clansman memory cs = world.getClansman(csId);
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "failed buy should not credit resources");
+        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
+        assertGt(cs.cooldownEndsAtTs, block.timestamp, "failed scheduled buy should consume cooldown");
+        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "failed scheduled worker waits");
+        assertEq(world.getScheduledMarketActionsForTick(executeAtTick).length, 0, "queue should be deleted");
     }
 
     // -------------------------------------------------------------------------
@@ -728,18 +1450,18 @@ contract ClanWorldTest is Test {
     // -------------------------------------------------------------------------
 
     function test_scheduledMarket_deletedAfterHeartbeat() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
         address woodAddr = _setupMarket();
         uint32 clanId = _mintClan();
         uint32 csId = _firstCs(clanId);
+        harness.setCarry(csId, 2e18, 0, 0, 0);
 
         OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
         assertEq(uint8(r[0].status), uint8(StatusCode.OK));
 
         Mission memory m = world.getActiveMission(csId);
-        uint64 executeAtTick = m.actionStartTick;
-
-        // Verify queue has entry before heartbeat
-        assertGt(world.getScheduledMarketActionsForTick(executeAtTick).length, 0, "queue should have entry");
+        uint64 executeAtTick = m.settlesAtTick;
 
         uint64 curTick = world.getWorldState().currentTick;
         uint256 ticksNeeded = uint256(executeAtTick - curTick) + 1;
@@ -754,14 +1476,18 @@ contract ClanWorldTest is Test {
     }
 
     function test_scheduledMarket_sameTypeRetask_skipsStaleNonce() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
         address woodAddr = _setupMarket();
         uint32 clanId = _mintClan();
         uint32 csId = _firstCs(clanId);
+        // Give carry wood so the replacement sell executes
+        harness.setCarry(csId, 10e18, 0, 0, 0);
 
         OrderResult[] memory r1 = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
         assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "first sell order should be accepted");
         Mission memory oldMission = world.getActiveMission(csId);
-        uint64 oldExecuteAtTick = oldMission.actionStartTick;
+        uint64 oldExecuteAtTick = oldMission.settlesAtTick;
 
         vm.warp(block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS + 1);
         _advanceTick();
@@ -769,28 +1495,32 @@ contract ClanWorldTest is Test {
         OrderResult[] memory r2 = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 2e18, 0);
         assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "replacement sell order should be accepted");
         Mission memory newMission = world.getActiveMission(csId);
-        uint64 newExecuteAtTick = newMission.actionStartTick;
+        uint64 newExecuteAtTick = newMission.settlesAtTick;
         assertGt(newMission.nonce, oldMission.nonce, "replacement should bump nonce");
 
-        ScheduledMarketAction[] memory oldQueue = world.getScheduledMarketActionsForTick(oldExecuteAtTick);
-        ScheduledMarketAction[] memory newQueue = world.getScheduledMarketActionsForTick(newExecuteAtTick);
-        assertEq(oldQueue[0].missionNonce, oldMission.nonce, "old queue captures old nonce");
-        assertEq(newQueue[0].missionNonce, newMission.nonce, "new queue captures new nonce");
+        if (oldExecuteAtTick == newExecuteAtTick) {
+            assertEq(world.getScheduledMarketActionsForTick(oldExecuteAtTick).length, 2, "both missions queued");
+        } else {
+            assertEq(world.getScheduledMarketActionsForTick(oldExecuteAtTick).length, 1, "old mission queued at submit");
+            assertEq(world.getScheduledMarketActionsForTick(newExecuteAtTick).length, 1, "new mission queued at submit");
+        }
 
         uint256 goldBefore = world.getClan(clanId).goldBalance;
 
-        _advanceTick(); // close tick before the stale entry
-
-        vm.expectEmit(true, true, false, true);
-        emit IClanWorldEvents.MarketActionFailed(clanId, csId, ActionType.MarketSell, StatusCode.ERR_INVALID_ACTION);
-        _advanceTick(); // close stale entry tick
+        while (world.getWorldState().currentTick <= oldExecuteAtTick) {
+            _advanceTick();
+        }
         assertEq(world.getClan(clanId).goldBalance, goldBefore, "stale sell must not execute");
 
-        _advanceTick(); // close replacement entry tick
+        while (world.getWorldState().currentTick <= newExecuteAtTick) {
+            _advanceTick();
+        }
         assertGt(world.getClan(clanId).goldBalance, goldBefore, "replacement sell should execute");
     }
 
-    function test_scheduledMarket_defersActionsAbovePerTickCap() public {
+    function test_scheduledMarket_executesAllActionsForClosedTick() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
         address woodAddr = _setupMarket();
         uint32[] memory distOneClans = new uint32[](12);
         uint32[] memory distTwoClans = new uint32[](12);
@@ -800,6 +1530,10 @@ contract ClanWorldTest is Test {
         for (uint256 i = 0; i < 12; i++) {
             uint32 clanId = _mintClan();
             ClanFullView memory view_ = world.getClanFullView(clanId);
+            // Give every clansman carry wood so their scheduled sell can execute
+            for (uint256 j = 0; j < view_.clansmen.length; j++) {
+                harness.setCarry(view_.clansmen[j].clansman.clansman.clansmanId, 10e18, 0, 0, 0);
+            }
             (uint8 travelTicks,) = world.quoteTravel(view_.clan.clan.baseRegion, ClanWorldConstants.REGION_UNICORN_TOWN);
             if (travelTicks == 1) {
                 distOneClans[distOneCount++] = clanId;
@@ -819,199 +1553,197 @@ contract ClanWorldTest is Test {
             totalQueued += _submitAllClanMarketSells(distOneClans[i], woodAddr);
         }
 
-        uint64 executeAtTick = 2;
-        uint256 cap = world.MAX_MARKET_ACTIONS_PER_TICK();
-        assertGt(totalQueued, cap, "test setup must exceed cap");
-        assertEq(
-            world.getScheduledMarketActionsForTick(executeAtTick).length,
-            totalQueued,
-            "all aligned actions should share tick 2"
-        );
+        uint64 executeAtTick = 3;
+        bytes32 executedSig =
+            keccak256("ScheduledMarketActionExecuted(uint32,uint32,uint8,uint8,uint256,uint8,uint256,uint64)");
 
         _advanceTick(); // close tick 1
-        _advanceTick(); // close tick 2, process cap and defer remainder
-
-        assertEq(world.getScheduledMarketActionsForTick(executeAtTick).length, 0, "original tick queue cleared");
-        assertEq(
-            world.getScheduledMarketActionsForTick(executeAtTick + 1).length,
-            totalQueued - cap,
-            "overflow actions deferred to next tick"
-        );
-
-        _advanceTick(); // close tick 3, process deferred actions
-        assertEq(world.getScheduledMarketActionsForTick(executeAtTick + 1).length, 0, "deferred queue cleared");
-    }
+        _advanceTick(); // close tick 2
 
-    function test_scheduledMarket_overflowExecutesBeforeNativeNextTickActions() public {
-        address woodAddr = _setupMarket();
-        uint32[] memory distOneClans = new uint32[](12);
-        uint32[] memory distTwoClans = new uint32[](12);
-        uint256 distOneCount;
-        uint256 distTwoCount;
+        vm.recordLogs();
+        _advanceTick(); // close tick 3 and execute every scheduled action for the tick
+        Vm.Log[] memory logs = vm.getRecordedLogs();
 
-        for (uint256 i = 0; i < 12; i++) {
-            uint32 clanId = _mintClan();
-            ClanFullView memory view_ = world.getClanFullView(clanId);
-            (uint8 travelTicks,) = world.quoteTravel(view_.clan.clan.baseRegion, ClanWorldConstants.REGION_UNICORN_TOWN);
-            if (travelTicks == 1) {
-                distOneClans[distOneCount++] = clanId;
-            } else if (travelTicks == 2) {
-                distTwoClans[distTwoCount++] = clanId;
+        uint256 executedCount;
+        for (uint256 i = 0; i < logs.length; i++) {
+            if (logs[i].emitter != address(world) || logs[i].topics.length == 0 || logs[i].topics[0] != executedSig) {
+                continue;
             }
+            (,,,,,, uint64 settledAtTick) =
+                abi.decode(logs[i].data, (uint32, ActionType, uint8, uint256, uint8, uint256, uint64));
+            if (settledAtTick != executeAtTick) continue;
+            executedCount++;
         }
-        assertGt(distTwoCount, 0, "test setup needs a distance-two clan");
-
-        uint32 nativeClanId = distTwoClans[0];
-        ClanFullView memory nativeView = world.getClanFullView(nativeClanId);
-        uint32 nativeCsId = nativeView.clansmen[3].clansman.clansman.clansmanId;
 
-        uint256 totalQueuedForTickTwo = _submitFirstClanMarketSells(nativeClanId, woodAddr, 3);
-        for (uint256 i = 1; i < distTwoCount; i++) {
-            totalQueuedForTickTwo += _submitAllClanMarketSells(distTwoClans[i], woodAddr);
-        }
+        assertEq(executedCount, totalQueued, "all same-tick actions should execute");
+        assertEq(world.getScheduledMarketActionsForTick(executeAtTick).length, 0, "queue should be deleted");
+    }
 
-        _advanceTick();
+    // -------------------------------------------------------------------------
+    // Test 15: scheduledMarket_fifo — same-tick sells execute in submission order
+    // -------------------------------------------------------------------------
 
-        for (uint256 i = 0; i < distOneCount; i++) {
-            totalQueuedForTickTwo += _submitAllClanMarketSells(distOneClans[i], woodAddr);
+    function test_scheduledMarket_fifo() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        address woodAddr = _setupMarket();
+        uint32 clanId = _mintClan();
+        ClanFullView memory view_ = world.getClanFullView(clanId);
+        // Give each clansman carry wood (must be >= their respective sell amount)
+        for (uint256 i = 0; i < 3; i++) {
+            harness.setCarry(view_.clansmen[i].clansman.clansman.clansmanId, 10e18, 0, 0, 0);
         }
 
-        OrderResult[] memory nativeResult =
-            _submitMarketOrder(nativeClanId, nativeCsId, ActionType.MarketSell, woodAddr, 1e18, 0);
-        assertEq(uint8(nativeResult[0].status), uint8(StatusCode.OK), "native next-tick action should enqueue");
-
-        uint64 overflowTick = 2;
-        uint64 nextTick = overflowTick + 1;
-        uint256 cap = world.MAX_MARKET_ACTIONS_PER_TICK();
-        assertGt(totalQueuedForTickTwo, cap, "test setup must exceed cap");
-
-        ScheduledMarketAction[] memory nativeQueueBefore = world.getScheduledMarketActionsForTick(nextTick);
-        assertEq(nativeQueueBefore.length, 1, "native next-tick queue should exist before overflow merge");
-        uint64 nativeSeq = nativeQueueBefore[0].commitSequence;
+        ClanOrder[] memory orders = new ClanOrder[](3);
+        for (uint256 i = 0; i < orders.length; i++) {
+            orders[i] = ClanOrder({
+                clansmanId: view_.clansmen[i].clansman.clansman.clansmanId,
+                gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
+                action: ActionType.MarketSell,
+                targetClanId: 0,
+                marketToken: woodAddr,
+                marketAmount: uint256(i + 1) * 1e18,
+                maxGoldIn: 0,
+                withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
+            });
+        }
+        vm.prank(elder);
+        OrderResult[] memory results = world.submitClanOrders(clanId, orders);
+        for (uint256 i = 0; i < results.length; i++) {
+            assertEq(uint8(results[i].status), uint8(StatusCode.OK), "sell order ok");
+        }
 
-        _advanceTick(); // close tick 1
-        _advanceTick(); // close tick 2, process cap and defer overflow into tick 3
+        Mission memory m = world.getActiveMission(orders[0].clansmanId);
+        uint64 executeAtTick = m.settlesAtTick;
+        bytes32 executedSig =
+            keccak256("ScheduledMarketActionExecuted(uint32,uint32,uint8,uint8,uint256,uint8,uint256,uint64)");
 
-        uint256 overflowCount = totalQueuedForTickTwo - cap;
-        ScheduledMarketAction[] memory mergedQueue = world.getScheduledMarketActionsForTick(nextTick);
-        assertEq(mergedQueue.length, overflowCount + 1, "overflow should merge with native next-tick action");
-        assertEq(
-            mergedQueue[mergedQueue.length - 1].commitSequence,
-            nativeSeq,
-            "native action must stay after older overflow"
-        );
-        for (uint256 i = 1; i < mergedQueue.length; i++) {
-            assertGt(mergedQueue[i].commitSequence, mergedQueue[i - 1].commitSequence, "merged queue must be FIFO");
+        while (world.getWorldState().currentTick < executeAtTick) {
+            _advanceTick();
         }
 
-        bytes32 executedSig =
-            keccak256("ScheduledMarketActionExecuted(uint64,uint64,uint32,uint32,address,address,uint256,uint256)");
         vm.recordLogs();
-        _advanceTick(); // close tick 3 and execute the sorted merged queue
+        _advanceTick();
         Vm.Log[] memory logs = vm.getRecordedLogs();
 
-        bool sawExecution;
-        uint64 previousSeq;
         uint256 executedCount;
         for (uint256 i = 0; i < logs.length; i++) {
             if (logs[i].emitter != address(world) || logs[i].topics.length == 0 || logs[i].topics[0] != executedSig) {
                 continue;
             }
-            uint64 executedTick = uint64(uint256(logs[i].topics[1]));
-            if (executedTick != nextTick) continue;
+            (,, uint8 resourceIn, uint256 amountIn,,, uint64 settledAtTick) =
+                abi.decode(logs[i].data, (uint32, ActionType, uint8, uint256, uint8, uint256, uint64));
+            if (settledAtTick != executeAtTick) continue;
 
-            uint64 seq = uint64(uint256(logs[i].topics[2]));
-            if (sawExecution) assertGt(seq, previousSeq, "execution events must be FIFO");
-            sawExecution = true;
-            previousSeq = seq;
+            assertEq(uint32(uint256(logs[i].topics[1])), clanId, "same clan should execute");
+            assertEq(resourceIn, uint8(ResourceType.Wood), "sell should input wood");
+            assertEq(amountIn, uint256(executedCount + 1) * 1e18, "execution should be FIFO");
             executedCount++;
         }
 
-        assertEq(executedCount, overflowCount + 1, "all merged actions should execute");
-        assertEq(previousSeq, nativeSeq, "native action should execute after older overflow actions");
+        assertEq(executedCount, 3, "three same-tick sells should execute");
+        assertGt(world.getClan(clanId).goldBalance, 3e18, "clan should gain gold from sells");
     }
 
     // -------------------------------------------------------------------------
-    // Test 15: scheduledMarket_fifo — two clans queue sells; commitSequence is FIFO
+    // Test 16: twoClan_sellBuyCycle — clan A sells wood, clan B buys wood; both succeed
     // -------------------------------------------------------------------------
 
-    function test_scheduledMarket_fifo() public {
+    function test_twoClan_sellBuyCycle() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
         address woodAddr = _setupMarket();
 
-        // Mint two clans — they get region 1 and region 2 respectively
         uint32 clanId1 = _mintClan();
         vm.prank(elder2);
         (uint32 clanId2,) = world.mintClan(elder2);
 
         uint32 csId1 = _firstCs(clanId1);
         uint32 csId2 = world.getClanFullView(clanId2).clansmen[0].clansman.clansman.clansmanId;
+        // Give csId1 carry wood so scheduled sell has something to sell
+        harness.setCarry(csId1, 10e18, 0, 0, 0);
 
-        // Submit clan1's sell order first (commitSequence = 0)
-        ClanOrder[] memory orders1 = new ClanOrder[](1);
-        orders1[0] = ClanOrder({
+        // Clan 1: sell 5e18 wood
+        ClanOrder[] memory sellOrders = new ClanOrder[](1);
+        sellOrders[0] = ClanOrder({
             clansmanId: csId1,
             gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
             action: ActionType.MarketSell,
             targetClanId: 0,
             marketToken: woodAddr,
-            marketAmount: 2e18,
-            maxGoldIn: 0
+            marketAmount: 5e18,
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         vm.prank(elder);
-        OrderResult[] memory r1 = world.submitClanOrders(clanId1, orders1);
-        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "clan1 sell order ok");
+        world.submitClanOrders(clanId1, sellOrders);
 
-        // Submit clan2's sell order second (commitSequence = 1)
-        ClanOrder[] memory orders2 = new ClanOrder[](1);
-        orders2[0] = ClanOrder({
+        // Clan 2: buy 1e18 wood with maxGoldIn = 100e18
+        ClanOrder[] memory buyOrders = new ClanOrder[](1);
+        buyOrders[0] = ClanOrder({
             clansmanId: csId2,
             gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
-            action: ActionType.MarketSell,
+            action: ActionType.MarketBuy,
             targetClanId: 0,
             marketToken: woodAddr,
-            marketAmount: 2e18,
-            maxGoldIn: 0
+            marketAmount: 1e18,
+            maxGoldIn: 100e18,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         vm.prank(elder2);
-        OrderResult[] memory r2 = world.submitClanOrders(clanId2, orders2);
-        assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "clan2 sell order ok");
+        world.submitClanOrders(clanId2, buyOrders);
 
-        // Verify FIFO: clan1 committed before clan2 so has lower commitSequence
         Mission memory m1 = world.getActiveMission(csId1);
         Mission memory m2 = world.getActiveMission(csId2);
 
-        // Check commitSequence in the queues for each clan's respective tick
-        ScheduledMarketAction[] memory q1 = world.getScheduledMarketActionsForTick(m1.actionStartTick);
-        ScheduledMarketAction[] memory q2 = world.getScheduledMarketActionsForTick(m2.actionStartTick);
-
-        uint64 seq1;
-        uint64 seq2;
-        for (uint256 i = 0; i < q1.length; i++) {
-            if (q1[i].clanId == clanId1) seq1 = q1[i].commitSequence;
-        }
-        for (uint256 i = 0; i < q2.length; i++) {
-            if (q2[i].clanId == clanId2) seq2 = q2[i].commitSequence;
-        }
-        assertLt(seq1, seq2, "clan1 submitted first: lower commitSequence");
-
-        // Advance ticks to cover both actions
+        uint64 maxTick = m1.settlesAtTick > m2.settlesAtTick ? m1.settlesAtTick : m2.settlesAtTick;
         uint64 curTick = world.getWorldState().currentTick;
-        uint64 maxTick = m1.actionStartTick > m2.actionStartTick ? m1.actionStartTick : m2.actionStartTick;
         uint256 ticksNeeded = uint256(maxTick - curTick) + 1;
+        bytes32 scheduledSig =
+            keccak256("ScheduledMarketActionExecuted(uint32,uint32,uint8,uint8,uint256,uint8,uint256,uint64)");
+        vm.recordLogs();
         for (uint256 i = 0; i < ticksNeeded; i++) {
             _advanceTick();
         }
+        Vm.Log[] memory logs = vm.getRecordedLogs();
 
-        // Both clans should have gained gold (starter 3e18 + sell proceeds)
-        assertGt(world.getClan(clanId1).goldBalance, 3e18, "clan1 should have gained gold from sell");
-        assertGt(world.getClan(clanId2).goldBalance, 3e18, "clan2 should have gained gold from sell");
-    }
+        bool sawClan1SellEvent;
+        bool sawClan2BuyEvent;
+        for (uint256 i = 0; i < logs.length; i++) {
+            if (logs[i].emitter != address(world) || logs[i].topics.length == 0 || logs[i].topics[0] != scheduledSig) {
+                continue;
+            }
 
-    // -------------------------------------------------------------------------
-    // Test 16: twoClan_sellBuyCycle — clan A sells wood, clan B buys wood; both succeed
-    // -------------------------------------------------------------------------
+            uint32 eventClanId = uint32(uint256(logs[i].topics[1]));
+            (uint32 eventCsId, ActionType eventAction,,,,,) =
+                abi.decode(logs[i].data, (uint32, ActionType, uint8, uint256, uint8, uint256, uint64));
 
-    function test_twoClan_sellBuyCycle() public {
+            if (eventClanId == clanId1 && eventCsId == csId1 && eventAction == ActionType.MarketSell) {
+                sawClan1SellEvent = true;
+            }
+            if (eventClanId == clanId2 && eventCsId == csId2 && eventAction == ActionType.MarketBuy) {
+                sawClan2BuyEvent = true;
+            }
+        }
+
+        world.settleClan(clanId1);
+        world.settleClan(clanId2);
+
+        assertTrue(sawClan1SellEvent, "sell event should carry clan1 id");
+        assertTrue(sawClan2BuyEvent, "buy event should carry clan2 id");
+        // Clan1 sold wood → gold should increase
+        assertGt(world.getClan(clanId1).goldBalance, 3e18, "clan1 should have more gold after sell");
+        // Clan2 bought wood → carry wood should increase
+        assertGt(world.getClansman(csId2).carryWood, 0, "clan2 clansman should carry wood after buy");
+        // Clan2 vault is unchanged
+        assertEq(world.getClan(clanId2).vaultWood, 20e18, "clan2 vault wood should be unchanged after buy");
+        // Clan2 spent gold
+        assertLt(world.getClan(clanId2).goldBalance, 3e18, "clan2 should have less gold after buy");
+    }
+
+    function test_scheduledMarketFailure_doesNotAffectAnotherClan() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
         address woodAddr = _setupMarket();
 
         uint32 clanId1 = _mintClan();
@@ -1020,54 +1752,41 @@ contract ClanWorldTest is Test {
 
         uint32 csId1 = _firstCs(clanId1);
         uint32 csId2 = world.getClanFullView(clanId2).clansmen[0].clansman.clansman.clansmanId;
+        // Give csId2 carry wood so the sell succeeds
+        harness.setCarry(csId2, 10e18, 0, 0, 0);
+
+        OrderResult[] memory buyFail = _submitMarketOrder(clanId1, csId1, ActionType.MarketBuy, woodAddr, 1e18, 0);
+        assertEq(uint8(buyFail[0].status), uint8(StatusCode.OK), "failing buy should enqueue");
 
-        // Clan 1: sell 5e18 wood
         ClanOrder[] memory sellOrders = new ClanOrder[](1);
         sellOrders[0] = ClanOrder({
-            clansmanId: csId1,
+            clansmanId: csId2,
             gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
             action: ActionType.MarketSell,
             targetClanId: 0,
             marketToken: woodAddr,
             marketAmount: 5e18,
-            maxGoldIn: 0
-        });
-        vm.prank(elder);
-        world.submitClanOrders(clanId1, sellOrders);
-
-        // Clan 2: buy 1e18 wood with maxGoldIn = 100e18
-        ClanOrder[] memory buyOrders = new ClanOrder[](1);
-        buyOrders[0] = ClanOrder({
-            clansmanId: csId2,
-            gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
-            action: ActionType.MarketBuy,
-            targetClanId: 0,
-            marketToken: woodAddr,
-            marketAmount: 1e18,
-            maxGoldIn: 100e18
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         vm.prank(elder2);
-        world.submitClanOrders(clanId2, buyOrders);
+        OrderResult[] memory sellOk = world.submitClanOrders(clanId2, sellOrders);
+        assertEq(uint8(sellOk[0].status), uint8(StatusCode.OK), "other clan sell should enqueue");
 
-        Mission memory m1 = world.getActiveMission(csId1);
-        Mission memory m2 = world.getActiveMission(csId2);
+        uint64 tick1 = world.getActiveMission(csId1).settlesAtTick;
+        uint64 tick2 = world.getActiveMission(csId2).settlesAtTick;
+        uint64 maxTick = tick1 > tick2 ? tick1 : tick2;
+        Clan memory failingBefore = world.getClan(clanId1);
+        uint256 sellerGoldBefore = world.getClan(clanId2).goldBalance;
 
-        uint64 maxTick = m1.actionStartTick > m2.actionStartTick ? m1.actionStartTick : m2.actionStartTick;
-        uint64 curTick = world.getWorldState().currentTick;
-        uint256 ticksNeeded = uint256(maxTick - curTick) + 1;
-        for (uint256 i = 0; i < ticksNeeded; i++) {
+        while (world.getWorldState().currentTick <= maxTick) {
             _advanceTick();
         }
 
-        world.settleClan(clanId1);
-        world.settleClan(clanId2);
-
-        // Clan1 sold wood → gold should increase
-        assertGt(world.getClan(clanId1).goldBalance, 3e18, "clan1 should have more gold after sell");
-        // Clan2 bought wood → vault wood should increase beyond starter 20e18
-        assertGt(world.getClan(clanId2).vaultWood, 20e18, "clan2 should have more vault wood after buy");
-        // Clan2 spent gold
-        assertLt(world.getClan(clanId2).goldBalance, 3e18, "clan2 should have less gold after buy");
+        Clan memory failingAfter = world.getClan(clanId1);
+        assertEq(failingAfter.vaultWood, failingBefore.vaultWood, "failed buy should not credit resources");
+        assertEq(failingAfter.goldBalance, failingBefore.goldBalance, "failed buy should not debit gold");
+        assertGt(world.getClan(clanId2).goldBalance, sellerGoldBefore, "other clan sell should execute");
     }
 
     // -------------------------------------------------------------------------
@@ -1088,7 +1807,8 @@ contract ClanWorldTest is Test {
             targetClanId: 0,
             marketToken: woodAddr,
             marketAmount: 1e18,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         vm.prank(elder);
         OrderResult[] memory results = world.submitClanOrders(clanId, orders);
@@ -1110,7 +1830,8 @@ contract ClanWorldTest is Test {
             targetClanId: 0,
             marketToken: address(0xBEEF),
             marketAmount: 1e18,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         orders[1] = ClanOrder({
             clansmanId: noopCsId,
@@ -1119,7 +1840,8 @@ contract ClanWorldTest is Test {
             targetClanId: 0,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
 
         vm.prank(elder);
@@ -1152,7 +1874,8 @@ contract ClanWorldTest is Test {
             targetClanId: clanId,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         vm.prank(elder);
         OrderResult[] memory r1 = world.submitClanOrders(clanId, orders);
@@ -1224,7 +1947,8 @@ contract ClanWorldTest is Test {
             targetClanId: 0,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         vm.prank(elder);
         OrderResult[] memory r = world.submitClanOrders(clanId1, orders);
@@ -1256,7 +1980,8 @@ contract ClanWorldTest is Test {
             targetClanId: 0,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         vm.prank(elder);
         OrderResult[] memory r = harness.submitClanOrders(clanId, orders);
@@ -1293,15 +2018,14 @@ contract ClanWorldTest is Test {
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
 
@@ -1379,7 +2103,8 @@ contract ClanWorldTest is Test {
             targetClanId: clanId,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         vm.prank(elder);
         OrderResult[] memory r = world.submitClanOrders(clanId, orders);
@@ -1501,7 +2226,8 @@ contract ClanWorldTest is Test {
             targetClanId: 0,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         vm.prank(elder);
         return harness.submitClanOrders(clanId, orders);
@@ -1688,7 +2414,8 @@ contract ClanWorldTest is Test {
             targetClanId: clanId,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         vm.prank(elder);
         OrderResult[] memory r = world.submitClanOrders(clanId, orders);
@@ -1759,7 +2486,8 @@ contract ClanWorldTest is Test {
             targetClanId: clanId,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         vm.prank(elder);
         OrderResult[] memory r = harness.submitClanOrders(clanId, orders);
@@ -1814,7 +2542,11 @@ contract ClanWorldTest is Test {
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
@@ -1859,7 +2591,8 @@ contract ClanWorldTest is Test {
             targetClanId: 0,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
 
         // Order 1: invalid clansmanId 9999
@@ -1870,7 +2603,8 @@ contract ClanWorldTest is Test {
             targetClanId: 0,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
 
         // Order 2: valid — cs1 Wait at homebase (NOOP)
@@ -1881,7 +2615,8 @@ contract ClanWorldTest is Test {
             targetClanId: 0,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
 
         // Order 3: invalid — cs2 ChopWood to Mountains (wrong region for ChopWood)
@@ -1892,7 +2627,8 @@ contract ClanWorldTest is Test {
             targetClanId: 0,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
 
         vm.prank(elder);
@@ -1921,8 +2657,7 @@ contract ClanWorldTest is Test {
         assertEq(ws.seasonStartTick, 0);
         assertEq(ws.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS);
         assertEq(
-            ws.winterStartsAtTick,
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
         );
         assertFalse(ws.winterActive);
     }
@@ -1931,8 +2666,7 @@ contract ClanWorldTest is Test {
         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
         // Advance 99 heartbeats so currentTick=99, then one more heartbeat fires WinterStarted at tick 100.
-        uint64 winterStart =
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
         for (uint64 i = 0; i < winterStart - 1; i++) {
             vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
             world.heartbeat();
@@ -1997,4 +2731,140 @@ contract ClanWorldTest is Test {
         assertEq(ws1.currentTick, 1);
         assertEq(ws1.nextHeartbeatAtTick, 2, "after tick 1, next = tick 2");
     }
+
+    function test_marketSell_deductsFromCarry_notVault() public {
+        // Use harness to directly set carry and test immediate sell
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        _setupMarket();
+        uint32 clanId = _mintClan();
+        ClanFullView memory v = world.getClanFullView(clanId);
+        uint32 csId = v.clansmen[0].clansman.clansman.clansmanId;
+
+        // Place worker at UT WAITING with carry wood
+        harness.setClansmanForTest(csId, ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
+        harness.setCarry(csId, 10e18, 0, 0, 0);
+
+        uint256 carryBefore = world.getClansman(csId).carryWood;
+        uint256 vaultBefore = world.getClan(clanId).vaultWood;
+        uint256 goldBefore = world.getClan(clanId).goldBalance;
+
+        address woodTok = world.getTreasuryState().woodToken;
+        ClanOrder[] memory orders = new ClanOrder[](1);
+        orders[0] = ClanOrder({
+            clansmanId: csId,
+            gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
+            action: ActionType.MarketSell,
+            targetClanId: 0,
+            marketToken: woodTok,
+            marketAmount: 5e18,
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
+        });
+        vm.prank(elder);
+        OrderResult[] memory results = world.submitClanOrders(clanId, orders);
+
+        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "immediate sell must succeed");
+        assertEq(world.getClansman(csId).carryWood, carryBefore - 5e18, "sell: carry must decrease by sell amount");
+        assertEq(world.getClan(clanId).vaultWood, vaultBefore, "sell: vault must be unchanged");
+        assertGt(world.getClan(clanId).goldBalance, goldBefore, "sell: gold must increase");
+    }
+
+    function test_marketSell_fails_emptyCarry_fullVault() public {
+        // Setup: worker in UT with empty carry but vault has wood
+        _setupMarket();
+        uint32 clanId = _mintClan();
+        ClanFullView memory v = world.getClanFullView(clanId);
+        uint32 csId = v.clansmen[0].clansman.clansman.clansmanId;
+        // Advance cs to UT with no carry (no gather)
+        _submitOrder(clanId, csId, ClanWorldConstants.REGION_UNICORN_TOWN, ActionType.Wait);
+        (uint8 travelToUT,) = world.quoteTravel(v.clan.clan.baseRegion, ClanWorldConstants.REGION_UNICORN_TOWN);
+        for (uint256 i = 0; i < uint256(travelToUT) + 2; i++) {
+            _advanceTick();
+        }
+        world.settleClansman(csId);
+        // Clan starts with vault wood from minting; carry is 0
+        assertEq(world.getClansman(csId).carryWood, 0, "carry must be 0");
+        assertGt(world.getClan(clanId).vaultWood, 0, "vault must have wood");
+
+        address woodTok = world.getTreasuryState().woodToken;
+        ClanOrder[] memory orders = new ClanOrder[](1);
+        orders[0] = ClanOrder({
+            clansmanId: csId,
+            gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
+            action: ActionType.MarketSell,
+            targetClanId: 0,
+            marketToken: woodTok,
+            marketAmount: 1e18,
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
+        });
+        vm.prank(elder);
+        OrderResult[] memory results = world.submitClanOrders(clanId, orders);
+        assertEq(uint8(results[0].status), uint8(StatusCode.ERR_MISSING_RESOURCES), "empty carry sell must fail");
+    }
+
+    function test_marketCooldown_noBypassViaScheduled() public {
+        // Use harness to set worker directly at UT with active cooldown
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        _setupMarket();
+        uint32 clanId = _mintClan();
+        ClanFullView memory v = world.getClanFullView(clanId);
+        uint32 csId = v.clansmen[0].clansman.clansman.clansmanId;
+
+        // Place worker at UT, WAITING, with cooldown active
+        uint64 cooldownEnds = uint64(block.timestamp + 120); // 2 ticks of cooldown
+        harness.setClansmanForTest(csId, ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, cooldownEnds);
+
+        // Submit a market sell — with cooldown active, must be rejected
+        address woodTok = world.getTreasuryState().woodToken;
+        ClanOrder[] memory o2 = new ClanOrder[](1);
+        o2[0] = ClanOrder({
+            clansmanId: csId,
+            gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
+            action: ActionType.MarketSell,
+            targetClanId: 0,
+            marketToken: woodTok,
+            marketAmount: 1e18,
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
+        });
+        vm.prank(elder);
+        OrderResult[] memory results = world.submitClanOrders(clanId, o2);
+        assertEq(
+            uint8(results[0].status),
+            uint8(StatusCode.ERR_COOLDOWN_ACTIVE),
+            "market order on cooldown must be rejected, not scheduled"
+        );
+    }
+
+    function test_marketBuy_creditsCarry_notVault() public {
+        (, address woodTok, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
+
+        uint256 vaultWoodBefore = world.getClan(clanId).vaultWood;
+        uint256 carryWoodBefore = world.getClansman(csId).carryWood;
+        uint256 goldBefore = world.getClan(clanId).goldBalance;
+
+        uint256 buyAmt = 1e18;
+        ClanOrder[] memory orders = new ClanOrder[](1);
+        orders[0] = ClanOrder({
+            clansmanId: csId,
+            gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
+            action: ActionType.MarketBuy,
+            targetClanId: 0,
+            marketToken: woodTok,
+            marketAmount: buyAmt,
+            maxGoldIn: type(uint256).max,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
+        });
+        vm.prank(elder);
+        OrderResult[] memory results = world.submitClanOrders(clanId, orders);
+
+        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "buy: immediate market buy must succeed");
+        assertGt(world.getClansman(csId).carryWood, carryWoodBefore, "buy: carry must increase");
+        assertEq(world.getClan(clanId).vaultWood, vaultWoodBefore, "buy: vault must be unchanged");
+        assertLt(world.getClan(clanId).goldBalance, goldBefore, "buy: gold must decrease");
+    }
 }
diff --git a/packages/contracts/test/DefendBase.t.sol b/packages/contracts/test/DefendBase.t.sol
index c05139f..dba245f 100644
--- a/packages/contracts/test/DefendBase.t.sol
+++ b/packages/contracts/test/DefendBase.t.sol
@@ -12,6 +12,7 @@ import {
     Clansman,
     Mission,
     ClanOrder,
+    WithdrawResourcesData,
     OrderResult
 } from "../src/IClanWorld.sol";
 
@@ -41,7 +42,8 @@ contract DefendBaseTest is Test {
             targetClanId: targetClanId,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         return orders;
     }
@@ -55,7 +57,8 @@ contract DefendBaseTest is Test {
             targetClanId: 0,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         return orders;
     }
diff --git a/packages/contracts/test/Gathering.t.sol b/packages/contracts/test/Gathering.t.sol
new file mode 100644
index 0000000..34b87fc
--- /dev/null
+++ b/packages/contracts/test/Gathering.t.sol
@@ -0,0 +1,136 @@
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
+    WithdrawResourcesData,
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
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
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
+        world.setCarryWood(csId, ClanWorldConstants.WOOD_CARRY_CAP - 1e18);
+
+        Clansman memory cs = _settleChopWood(clanId, csId);
+
+        assertEq(cs.carryWood, ClanWorldConstants.WOOD_CARRY_CAP, "wood carry cap");
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
diff --git a/packages/contracts/test/HeartbeatOrdering.t.sol b/packages/contracts/test/HeartbeatOrdering.t.sol
index 9d012e8..b4c1241 100644
--- a/packages/contracts/test/HeartbeatOrdering.t.sol
+++ b/packages/contracts/test/HeartbeatOrdering.t.sol
@@ -13,6 +13,7 @@ import {
     StatusCode,
     WorldState,
     ClanOrder,
+    WithdrawResourcesData,
     OrderResult,
     Mission,
     Clan,
@@ -96,7 +97,8 @@ contract HeartbeatOrderingTest is Test {
             targetClanId: 0,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         vm.prank(elder);
         return world.submitClanOrders(clanId, orders);
@@ -118,7 +120,8 @@ contract HeartbeatOrderingTest is Test {
             targetClanId: 0,
             marketToken: token,
             marketAmount: amount,
-            maxGoldIn: maxGold
+            maxGoldIn: maxGold,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         vm.prank(elder);
         return world.submitClanOrders(clanId, orders);
@@ -149,8 +152,22 @@ contract HeartbeatOrderingTest is Test {
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
@@ -169,10 +186,10 @@ contract HeartbeatOrderingTest is Test {
     //
     // Proves Step 1 (settle) fires before Step 2 (market execute) within the SAME
     // heartbeat closing tick T:
-    //   - cs0 is placed at Mountains (region 2). Deposit to homebase Forest (region 1)
-    //     = 1 tick travel. arrivalTick = T0+1, settlesAtTick = T0+2.
+    //   - cs0 is placed at Unicorn Town (region 3). Deposit to homebase Forest
+    //     (region 1) = 2 ticks travel. arrivalTick = T0+2, settlesAtTick = T0+3.
     //   - cs1 at Forest submits MarketSell to UT (region 3) = 2 ticks travel.
-    //     executeAtTick = T0+2. (Same tick as cs0 settles.)
+    //     settlesAtTick = T0+3. (Same tick as cs0 settles.)
     //   - vault starts at 0; cs0 has carry wood.
     //   - Heartbeat at T0+2: Step 1 deposits cs0 carry wood to vault, Step 2 sells
     //     it. Gold increases only if Step 1 ran first (vault was 0 before Step 1).
@@ -186,35 +203,38 @@ contract HeartbeatOrderingTest is Test {
 
         uint64 t0 = world.getWorldState().currentTick;
 
-        // Place cs0 at Mountains (region 2). Homebase = Forest (region 1).
-        // Deposit from Mountains to Forest: travel = 1 tick.
-        world.setClansmanRegion(csId0, ClanWorldConstants.REGION_MOUNTAINS);
+        // Place cs0 at Unicorn Town (region 3). Homebase = Forest (region 1).
+        // Deposit from Unicorn Town to Forest: travel = 2 ticks.
+        world.setClansmanRegion(csId0, ClanWorldConstants.REGION_UNICORN_TOWN);
 
-        // cs0: submit Deposit. arrivalTick = t0+1, settlesAtTick = t0+2.
-        OrderResult[] memory r0 = _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
+        // cs0: submit Deposit. arrivalTick = t0+2, settlesAtTick = t0+3.
+        OrderResult[] memory r0 =
+            _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
         assertEq(uint8(r0[0].status), uint8(StatusCode.OK), "deposit order must succeed");
         Mission memory depositMission = world.getActiveMission(csId0);
-        assertEq(depositMission.settlesAtTick, t0 + 2, "deposit settlesAtTick must be t0+2");
+        assertEq(depositMission.settlesAtTick, t0 + 3, "deposit settlesAtTick must be t0+3");
+
+        // Give cs0 carry wood (for deposit) and cs1 carry wood (for sell).
+        // Sell now draws from carry, not vault — zero vault to confirm vault is not involved.
+        world.setCarryWood(csId0, 10e18);
+        world.setCarryWood(csId1, 5e18);
+        world.setVaultWood(clanId, 0);
+        assertEq(world.getClan(clanId).vaultWood, 0, "vault wood must be 0 before test tick");
 
         // cs1: at Forest. Submit MarketSell to UT. Forest→UT = 2 ticks.
-        // executeAtTick = t0+2. Same tick as deposit settles.
+        // settlesAtTick = t0+3. Same tick as deposit settles.
         OrderResult[] memory r1 = _submitMarketOrder(clanId, csId1, ActionType.MarketSell, address(woodToken), 5e18, 0);
         assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell order must enqueue");
         Mission memory sellMission = world.getActiveMission(csId1);
         assertEq(sellMission.arrivalTick, t0 + 2, "sell arrivalTick must be t0+2");
-
-        // Give cs0 carry wood. Zero vault so market sell only succeeds if step1 ran first.
-        world.setCarryWood(csId0, 10e18);
-        world.setVaultWood(clanId, 0);
-        assertEq(world.getClan(clanId).vaultWood, 0, "vault wood must be 0 before test tick");
+        assertEq(sellMission.settlesAtTick, t0 + 3, "sell settlesAtTick must be t0+3");
 
         uint256 goldBefore = world.getClan(clanId).goldBalance;
 
-        // Advance to tick t0+2. The heartbeat closing t0+2 runs:
-        //   Step 1: _settleCompletingMissions(t0+2) → deposit fires, cs0 carry 10e18 → vault
-        //   Step 2: _executeScheduledMarketActions(t0+2) → sell fires, 5e18 vault wood → gold
-        // If reversed: sell would fail (vault=0), gold unchanged.
-        _advanceToTick(t0 + 3);
+        // Advance to tick t0+4. The heartbeat closing t0+3 runs:
+        //   Step 1: _settleCompletingMissions(t0+3) → deposit fires, cs0 carry 10e18 → vault
+        //   Step 2: _executeScheduledMarketActions(t0+3) → sell fires, cs1 carry 5e18 → gold
+        _advanceToTick(t0 + 4);
 
         uint256 goldAfter = world.getClan(clanId).goldBalance;
         assertGt(goldAfter, goldBefore, "gold must increase: settlement ran before market sell");
@@ -302,9 +322,9 @@ contract HeartbeatOrderingTest is Test {
     //
     // Proves that when a mission settles at tick T AND a market action is queued at
     // tick T, both fire in the same heartbeat without conflict.
-    //   - cs0 placed at Mountains, deposits to Forest homebase: settlesAtTick = T0+2
-    //   - cs1 sells wood to UT from Forest: executeAtTick = T0+2
-    //   Both succeed in the same heartbeat call at T0+2.
+    //   - cs0 placed at Unicorn Town, deposits to Forest homebase: settlesAtTick = T0+3
+    //   - cs1 sells wood to UT from Forest: settlesAtTick = T0+3
+    //   Both succeed in the same heartbeat call at T0+3.
     // -------------------------------------------------------------------------
     function test_heartbeat_multipleStepsInOneTick() public {
         _setupMarket();
@@ -314,26 +334,29 @@ contract HeartbeatOrderingTest is Test {
 
         uint64 t0 = world.getWorldState().currentTick;
 
-        // cs0: placed at Mountains (1 tick from Forest homebase).
-        // Deposit: arrivalTick = t0+1, settlesAtTick = t0+2.
-        world.setClansmanRegion(csId0, ClanWorldConstants.REGION_MOUNTAINS);
+        // cs0: placed at Unicorn Town (2 ticks from Forest homebase).
+        // Deposit: arrivalTick = t0+2, settlesAtTick = t0+3.
+        world.setClansmanRegion(csId0, ClanWorldConstants.REGION_UNICORN_TOWN);
         world.setCarryWood(csId0, 10e18);
-        OrderResult[] memory r0 = _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
+        OrderResult[] memory r0 =
+            _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
         assertEq(uint8(r0[0].status), uint8(StatusCode.OK), "deposit must succeed");
-        assertEq(world.getActiveMission(csId0).settlesAtTick, t0 + 2, "deposit settlesAtTick must be t0+2");
+        assertEq(world.getActiveMission(csId0).settlesAtTick, t0 + 3, "deposit settlesAtTick must be t0+3");
 
-        // cs1: at Forest, sells wood to UT. Forest→UT = 2 ticks. executeAtTick = t0+2.
-        // Vault has 20e18 starter wood — sell always has enough.
+        // cs1: at Forest, sells wood to UT. Forest→UT = 2 ticks. settlesAtTick = t0+3.
+        // Give cs1 carry wood — sell draws from carry (not vault).
+        world.setCarryWood(csId1, 10e18);
         OrderResult[] memory r1 = _submitMarketOrder(clanId, csId1, ActionType.MarketSell, address(woodToken), 5e18, 0);
         assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell must enqueue");
-        assertEq(world.getActiveMission(csId1).arrivalTick, t0 + 2, "sell executeAtTick must be t0+2");
+        assertEq(world.getActiveMission(csId1).arrivalTick, t0 + 2, "sell arrivalTick must be t0+2");
+        assertEq(world.getActiveMission(csId1).settlesAtTick, t0 + 3, "sell settlesAtTick must be t0+3");
 
         uint256 goldBefore = world.getClan(clanId).goldBalance;
 
-        // Advance to tick t0+3 (the heartbeat closing t0+2 runs step1+step2 together).
-        _advanceToTick(t0 + 3);
+        // Advance to tick t0+4 (the heartbeat closing t0+3 runs step1+step2 together).
+        _advanceToTick(t0 + 4);
 
-        // Both cs0 deposit (settled at T0+2) and cs1 sell (at T0+2) must have fired.
+        // Both cs0 deposit (settled at T0+3) and cs1 sell (at T0+3) must have fired.
         assertEq(world.getClansman(csId0).carryWood, 0, "deposit settled: carry cleared");
         assertGt(world.getClan(clanId).goldBalance, goldBefore, "sell executed: gold increased");
         assertFalse(world.getActiveMission(csId0).active, "deposit mission must be complete");
diff --git a/packages/contracts/test/MissionTiming.t.sol b/packages/contracts/test/MissionTiming.t.sol
index 2e8b0ed..033e6af 100644
--- a/packages/contracts/test/MissionTiming.t.sol
+++ b/packages/contracts/test/MissionTiming.t.sol
@@ -11,6 +11,7 @@ import {
     ClanFullView,
     Clansman,
     ClanOrder,
+    WithdrawResourcesData,
     OrderResult,
     Mission
 } from "../src/IClanWorld.sol";
@@ -56,7 +57,8 @@ contract MissionTimingTest is Test {
             targetClanId: 0,
             marketToken: address(0),
             marketAmount: 0,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         vm.prank(elder);
         return world.submitClanOrders(clanId, orders);
diff --git a/packages/contracts/test/Reentrancy.t.sol b/packages/contracts/test/Reentrancy.t.sol
index 30632fe..b77c253 100644
--- a/packages/contracts/test/Reentrancy.t.sol
+++ b/packages/contracts/test/Reentrancy.t.sol
@@ -11,6 +11,7 @@ import {
     ActionType,
     StatusCode,
     ClanOrder,
+    WithdrawResourcesData,
     OrderResult,
     Mission,
     PoolSeedConfig
@@ -94,8 +95,14 @@ contract MockReentrantPool {
     }
 }
 
+contract ClanWorldReentrantHarness is ClanWorld {
+    function setCarryWood(uint32 csId, uint256 amount) external {
+        _clansmen[csId].carryWood = amount;
+    }
+}
+
 contract ReentrancyTest is Test {
-    ClanWorld world;
+    ClanWorldReentrantHarness world;
     address elder = address(0xA1);
 
     MinimalERC20 woodToken;
@@ -110,13 +117,15 @@ contract ReentrancyTest is Test {
     StubPool fishPool;
 
     function setUp() public {
-        world = new ClanWorld();
+        world = new ClanWorldReentrantHarness();
     }
 
     function test_marketPoolHeartbeatCallback_revertsWithReentrancyGuard() public {
         _setupReentrantMarket();
         uint32 clanId = _mintClan();
         uint32 csId = _firstCs(clanId);
+        // Give clansman carry wood so the scheduled sell has something to sell
+        world.setCarryWood(csId, 10e18);
 
         uint256 goldBefore = world.getClan(clanId).goldBalance;
 
@@ -125,7 +134,7 @@ contract ReentrancyTest is Test {
 
         Mission memory mission = world.getActiveMission(csId);
         uint64 currentTick = world.getWorldState().currentTick;
-        uint256 ticksNeeded = uint256(mission.actionStartTick - currentTick) + 1;
+        uint256 ticksNeeded = uint256(mission.settlesAtTick - currentTick) + 1;
         for (uint256 i = 0; i < ticksNeeded; i++) {
             _advanceTick();
         }
@@ -160,8 +169,22 @@ contract ReentrancyTest is Test {
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
@@ -201,7 +224,8 @@ contract ReentrancyTest is Test {
             targetClanId: 0,
             marketToken: token,
             marketAmount: amount,
-            maxGoldIn: 0
+            maxGoldIn: 0,
+            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
         });
         vm.prank(elder);
         return world.submitClanOrders(clanId, orders);
diff --git a/packages/contracts/test/ResourceBoundaryTokens.t.sol b/packages/contracts/test/ResourceBoundaryTokens.t.sol
new file mode 100644
index 0000000..0f60d32
--- /dev/null
+++ b/packages/contracts/test/ResourceBoundaryTokens.t.sol
@@ -0,0 +1,76 @@
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
diff --git a/packages/shared/package.json b/packages/shared/package.json
index 6519a84..7727e19 100644
--- a/packages/shared/package.json
+++ b/packages/shared/package.json
@@ -12,11 +12,13 @@
   "scripts": {
     "build": "tsc -b",
     "typecheck": "tsc --noEmit",
+    "test": "vitest run",
     "lint": "echo 'lint stub'",
     "clean": "rm -rf dist .turbo"
   },
   "devDependencies": {
-    "typescript": "5.9.3"
+    "typescript": "5.9.3",
+    "vitest": "^3.2.0"
   },
   "dependencies": {
     "convex": "1.17.4",
diff --git a/packages/shared/src/adapters/IChainClient.ts b/packages/shared/src/adapters/IChainClient.ts
index bf97929..56a594f 100644
--- a/packages/shared/src/adapters/IChainClient.ts
+++ b/packages/shared/src/adapters/IChainClient.ts
@@ -1,16 +1,54 @@
 import fs from 'node:fs';
-import { createPublicClient, createWalletClient, http, fallback, defineChain } from 'viem';
+import {
+  createPublicClient,
+  createWalletClient,
+  http,
+  fallback,
+  defineChain,
+} from 'viem';
 import { privateKeyToAccount } from 'viem/accounts';
 import type { ClanFullView, ClanOrder, Tick } from '../types';
 import { readEnv } from './_env';
 
+export function uintValue(value: unknown): bigint {
+  if (typeof value === 'bigint') {
+    return value;
+  }
+
+  if (typeof value === 'number') {
+    if (!Number.isInteger(value)) {
+      throw new Error(`uintValue: non-integer number ${value}`);
+    }
+    if (!Number.isSafeInteger(value)) {
+      throw new Error(`uintValue: unsafe integer number ${value}`);
+    }
+    if (value < 0) {
+      throw new Error(`uintValue: negative number ${value}`);
+    }
+    return BigInt(value);
+  }
+
+  if (typeof value === 'string') {
+    if (!/^\d+$/.test(value)) {
+      throw new Error(`uintValue: not a non-negative integer string: ${value}`);
+    }
+    return BigInt(value);
+  }
+
+  throw new Error(`uintValue: unsupported type ${typeof value}`);
+}
+
 export interface IChainClient {
   getCurrentTick(): Promise<Tick>;
-  submitOrders(clanId: string, orders: ClanOrder[]): Promise<{ txHash: string }>;
+  submitOrders(
+    clanId: string,
+    orders: ClanOrder[],
+  ): Promise<{ txHash: string }>;
   getClanFullView(clanId: string): Promise<ClanFullView>;
 }
 
-const DEFAULT_CONTRACT_ADDRESS = '0x1BF5649f29CbB53E117a5aE969A18A71790f83E8' as const;
+const DEFAULT_CONTRACT_ADDRESS =
+  '0x1BF5649f29CbB53E117a5aE969A18A71790f83E8' as const;
 
 export const baseSepolia = defineChain({
   id: 84532,
@@ -146,6 +184,16 @@ const CLAN_WORLD_ABI = [
                       { name: 'marketToken', type: 'address' },
                       { name: 'marketAmount', type: 'uint256' },
                       { name: 'maxGoldIn', type: 'uint256' },
+                      {
+                        name: 'withdrawResources',
+                        type: 'tuple',
+                        components: [
+                          { name: 'wood', type: 'uint256' },
+                          { name: 'iron', type: 'uint256' },
+                          { name: 'wheat', type: 'uint256' },
+                          { name: 'fish', type: 'uint256' },
+                        ],
+                      },
                     ],
                   },
                   { name: 'effectiveRegion', type: 'uint8' },
@@ -171,6 +219,16 @@ const CLAN_WORLD_ABI = [
                   { name: 'marketToken', type: 'address' },
                   { name: 'marketAmount', type: 'uint256' },
                   { name: 'maxGoldIn', type: 'uint256' },
+                  {
+                    name: 'withdrawResources',
+                    type: 'tuple',
+                    components: [
+                      { name: 'wood', type: 'uint256' },
+                      { name: 'iron', type: 'uint256' },
+                      { name: 'wheat', type: 'uint256' },
+                      { name: 'fish', type: 'uint256' },
+                    ],
+                  },
                 ],
               },
             ],
@@ -218,6 +276,16 @@ const CLAN_WORLD_ABI = [
           { name: 'marketToken', type: 'address' },
           { name: 'marketAmount', type: 'uint256' },
           { name: 'maxGoldIn', type: 'uint256' },
+          {
+            name: 'withdrawResources',
+            type: 'tuple',
+            components: [
+              { name: 'wood', type: 'uint256' },
+              { name: 'iron', type: 'uint256' },
+              { name: 'wheat', type: 'uint256' },
+              { name: 'fish', type: 'uint256' },
+            ],
+          },
         ],
       },
     ],
@@ -230,6 +298,7 @@ const CLAN_WORLD_ABI = [
           { name: 'status', type: 'uint8' },
           { name: 'cooldownEndsAtTs', type: 'uint64' },
           { name: 'missionNonce', type: 'uint64' },
+          { name: 'marketMode', type: 'uint8' },
         ],
       },
     ],
@@ -241,7 +310,10 @@ class StubChainClient implements IChainClient {
   async getCurrentTick(): Promise<Tick> {
     return 0;
   }
-  async submitOrders(_clanId: string, _orders: ClanOrder[]): Promise<{ txHash: string }> {
+  async submitOrders(
+    _clanId: string,
+    _orders: ClanOrder[],
+  ): Promise<{ txHash: string }> {
     return { txHash: '0xstub' };
   }
   async getClanFullView(clanId: string): Promise<ClanFullView> {
@@ -257,7 +329,9 @@ class StubChainClient implements IChainClient {
 class RealChainClient implements IChainClient {
   private readonly client: ReturnType<typeof createPublicClient>;
   private readonly contractAddress: `0x${string}`;
-  private readonly transport: ReturnType<typeof http> | ReturnType<typeof fallback>;
+  private readonly transport:
+    | ReturnType<typeof http>
+    | ReturnType<typeof fallback>;
 
   constructor() {
     const primaryRpc = readEnv('RPC_URL_PRIMARY');
@@ -286,38 +360,67 @@ class RealChainClient implements IChainClient {
     return Number(snapshot.currentTick); // safe: tick values are small enough to fit Number precisely in Wave 0
   }
 
-  async submitOrders(clanId: string, orders: ClanOrder[]): Promise<{ txHash: string }> {
+  async submitOrders(
+    clanId: string,
+    orders: ClanOrder[],
+  ): Promise<{ txHash: string }> {
     // Wave 0: single-Elder only — concurrent nonce coordination deferred to Wave 1
     const parsedClanId = parseInt(clanId, 10);
     if (isNaN(parsedClanId) || String(parsedClanId) !== clanId.trim()) {
-      throw new Error(`submitOrders: clanId must be a decimal integer, got '${clanId}'`);
+      throw new Error(
+        `submitOrders: clanId must be a decimal integer, got '${clanId}'`,
+      );
     }
 
     for (const order of orders) {
       if (order.kind === 'mission') {
         const { clansmanId, gotoRegion, action } = order.payload;
-        if (clansmanId === undefined || gotoRegion === undefined || action === undefined) {
-          throw new Error(`submitOrders: mission order missing required payload fields (clansmanId, gotoRegion, action)`);
+        if (
+          clansmanId === undefined ||
+          gotoRegion === undefined ||
+          action === undefined
+        ) {
+          throw new Error(
+            `submitOrders: mission order missing required payload fields (clansmanId, gotoRegion, action)`,
+          );
         }
       }
     }
 
-    const nonMissionOrders = orders.filter(o => o.kind !== 'mission');
+    const nonMissionOrders = orders.filter((o) => o.kind !== 'mission');
     if (nonMissionOrders.length > 0) {
-      console.warn(`[RealChainClient] submitOrders: ${nonMissionOrders.length} non-mission order(s) skipped (Wave 0 only supports 'mission' kind)`);
+      console.warn(
+        `[RealChainClient] submitOrders: ${nonMissionOrders.length} non-mission order(s) skipped (Wave 0 only supports 'mission' kind)`,
+      );
     }
 
     const contractOrders = orders
-      .filter(o => o.kind === 'mission')
-      .map(o => ({
-        clansmanId: Number(o.payload.clansmanId),
-        gotoRegion: Number(o.payload.gotoRegion),
-        action: Number(o.payload.action),
-        targetClanId: 0,
-        marketToken: '0x0000000000000000000000000000000000000000' as `0x${string}`,
-        marketAmount: 0n,
-        maxGoldIn: 0n,
-      }));
+      .filter((o) => o.kind === 'mission')
+      .map((o) => {
+        const withdraw =
+          o.payload.withdrawResources &&
+          typeof o.payload.withdrawResources === 'object'
+            ? (o.payload.withdrawResources as Record<string, unknown>)
+            : o.payload;
+        const optionalUintValue = (value: unknown): bigint =>
+          value === undefined || value === null ? 0n : uintValue(value);
+        return {
+          clansmanId: Number(o.payload.clansmanId),
+          gotoRegion: Number(o.payload.gotoRegion),
+          action: Number(o.payload.action),
+          targetClanId: 0,
+          marketToken:
+            '0x0000000000000000000000000000000000000000' as `0x${string}`,
+          marketAmount: 0n,
+          maxGoldIn: 0n,
+          withdrawResources: {
+            wood: optionalUintValue(withdraw.wood),
+            iron: optionalUintValue(withdraw.iron),
+            wheat: optionalUintValue(withdraw.wheat),
+            fish: optionalUintValue(withdraw.fish),
+          },
+        };
+      });
 
     if (contractOrders.length === 0) {
       throw new Error('submitOrders: no valid mission orders to submit');
@@ -331,23 +434,35 @@ class RealChainClient implements IChainClient {
         pk = fs.readFileSync(keyPath, 'utf8').trim();
         pkSource = `ELDER_WALLET_KEY_PATH file at ${keyPath}`;
       } catch (err) {
-        if (err && typeof err === 'object' && 'code' in err && err.code === 'ENOENT') {
+        if (
+          err &&
+          typeof err === 'object' &&
+          'code' in err &&
+          err.code === 'ENOENT'
+        ) {
           throw new Error(
             `ELDER_WALLET_KEY_PATH file not found at ${keyPath}; either set DEPLOYER_PRIVATE_KEY env var or provide a key file`,
           );
         }
         const msg = err instanceof Error ? err.message : String(err);
-        throw new Error(`Failed to read ELDER_WALLET_KEY_PATH file at ${keyPath}: ${msg}`);
+        throw new Error(
+          `Failed to read ELDER_WALLET_KEY_PATH file at ${keyPath}: ${msg}`,
+        );
       }
     } else {
       const fallbackKey = readEnv('DEPLOYER_PRIVATE_KEY');
       if (fallbackKey) {
-        console.warn('[RealChainClient] ELDER_WALLET_KEY_PATH not set; falling back to DEPLOYER_PRIVATE_KEY (deprecated)');
+        console.warn(
+          '[RealChainClient] ELDER_WALLET_KEY_PATH not set; falling back to DEPLOYER_PRIVATE_KEY (deprecated)',
+        );
         pk = fallbackKey;
         pkSource = 'DEPLOYER_PRIVATE_KEY env var';
       }
     }
-    if (!pk) throw new Error('Neither ELDER_WALLET_KEY_PATH nor DEPLOYER_PRIVATE_KEY is set');
+    if (!pk)
+      throw new Error(
+        'Neither ELDER_WALLET_KEY_PATH nor DEPLOYER_PRIVATE_KEY is set',
+      );
 
     // Normalize: add 0x prefix if missing
     if (!pk.startsWith('0x')) pk = '0x' + pk;
diff --git a/packages/shared/test/uintValue.test.ts b/packages/shared/test/uintValue.test.ts
new file mode 100644
index 0000000..6bd5f06
--- /dev/null
+++ b/packages/shared/test/uintValue.test.ts
@@ -0,0 +1,30 @@
+import { describe, expect, it } from 'vitest';
+import { uintValue } from '../src/adapters/IChainClient';
+
+describe('uintValue', () => {
+  it('accepts bigint, safe integer numbers, and integer strings', () => {
+    expect(uintValue(12n)).toBe(12n);
+    expect(uintValue(12)).toBe(12n);
+    expect(uintValue('12')).toBe(12n);
+  });
+
+  it('rejects non-integer numbers', () => {
+    expect(() => uintValue(1.5)).toThrow('non-integer number');
+  });
+
+  it('rejects unsafe integer numbers', () => {
+    expect(() => uintValue(Number.MAX_SAFE_INTEGER + 1)).toThrow(
+      'unsafe integer number',
+    );
+  });
+
+  it('rejects non-integer strings', () => {
+    expect(() => uintValue('1e18')).toThrow('not a non-negative integer string');
+    expect(() => uintValue('1.5')).toThrow('not a non-negative integer string');
+  });
+
+  it('rejects unsupported values', () => {
+    expect(() => uintValue(null)).toThrow('unsupported type object');
+    expect(() => uintValue(undefined)).toThrow('unsupported type undefined');
+  });
+});
diff --git a/pnpm-lock.yaml b/pnpm-lock.yaml
index 09dc3ad..c12d415 100644
--- a/pnpm-lock.yaml
+++ b/pnpm-lock.yaml
@@ -191,6 +191,9 @@ importers:
       typescript:
         specifier: 5.9.3
         version: 5.9.3
+      vitest:
+        specifier: ^3.2.0
+        version: 3.2.4(@types/node@25.6.0)(lightningcss@1.32.0)
 
 packages:
 
</stdin>
warning: Codex could not find bubblewrap on PATH. Install bubblewrap with your OS package manager. See the sandbox prerequisites: https://developers.openai.com/codex/concepts/sandboxing#prerequisites. Codex will use the vendored bubblewrap in the meantime.
codex
Reviewing the integrated Phase 6 market surface now. I’m pulling the package-level guidance first, then I’ll split the work across parallel explorers for contracts and shared/client seams while I trace the tick/state-machine paths end to end.
  → Read repo guidance and changed contract/shared files for full context
  • Delegate parallel reviews for contract integration risks and shared client/data-shape risks
  • Trace market/mission state machine, scheduled queue, carry/vault interactions, and event/data flows
  • Synthesize findings into final pre-merge review format
exec
/bin/bash -lc 'git rev-parse --verify 9291964''^{commit}' in /home/claude/code/clan-world
 succeeded in 0ms:
9291964678d30021794cb49aadecb4a9264c9a43

exec
/bin/bash -lc "sed -n '1,220p' packages/shared/AGENTS.md" in /home/claude/code/clan-world
 succeeded in 0ms:
# packages/shared — AGENTS.md

Shared TypeScript surface for the monorepo. Two responsibilities: data types and adapter interfaces. Every other workspace depends on this one.

## What this package does

- Exports core game types: `WorldSnapshot`, `ClanFullView`, `Tick`, `Region`, `Clan`, `ClanOrder`, `Whisper`, `TickEpoch`.
- Exports four adapter interfaces (`IChainClient`, `IConvexClient`, `IKeeper`, `ILLMClient`) plus stub + real implementations and a factory function per interface.
- The factory reads an env var (`CLAN_WORLD_USE_STUB_*` or `KEEPER_MODE`) and returns the chosen impl. Browser-safe via `_env.ts` helper.

## Wave 0 status

All types exist as **minimal placeholders** matching the frontend spec — expand as streams need them. All adapter interfaces are wired with stub implementations that return mock data; real implementations throw `not implemented` and land in Wave 1+.

## Key files

- `src/index.ts` — public entry, re-exports types and adapters.
- `src/types.ts` — game data types.
- `src/adapters/index.ts` — adapter barrel.
- `src/adapters/IChainClient.ts` — chain seam.
- `src/adapters/IConvexClient.ts` — backend seam.
- `src/adapters/IKeeper.ts` — heartbeat driver seam (3 impls: Foundry loop / KeeperHub / Convex cron).
- `src/adapters/ILLMClient.ts` — non-Elder LLM uses (narrator etc.); Anthropic + ZeroG impls.
- `src/adapters/_env.ts` — `readEnv(name)` helper that works in Node and Vite.

## Local conventions

- **Types are the source of truth.** If the contract spec or frontend spec disagrees, fix the disagreement here first, then update consumers.
- **Adapters are interfaces first, factories second.** Stubs are reference impls — they always return something sensible so consumers can run end-to-end with `CLAN_WORLD_USE_STUB_*=true` set.
- **`process.env` is forbidden in this package** — use `readEnv()` from `_env.ts`. The frontend bundles this code via Vite, which doesn't define `process`.
- **No runtime deps** beyond `@clan-world/*` — keep this package zero-dep so it bundles cleanly into both Node and browser consumers.
- **`bigint` for chain values** (treasury, gas, amounts). Number-to-bigint conversions happen at the adapter boundary.

## How it interacts with adapters

This package DEFINES the adapters; everyone else consumes them. Updates to an adapter interface ripple to every workspace — coordinate via `docs/conventions/adapter-interfaces.md`.

## Running

```bash
pnpm --filter @clan-world/shared typecheck
pnpm --filter @clan-world/shared build
```

See `../../docs/conventions/adapter-interfaces.md` for the worked example of how to add a new adapter or a new method to an existing one.

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
/bin/bash -lc 'git status --short' in /home/claude/code/clan-world
 succeeded in 0ms:
?? docs/planning/gold-bridge-monorepo.zip
?? docs/reviews/pr153-review-claude-opus.md
?? docs/reviews/pr153-review-codex-5-3.md
?? docs/reviews/pr193-codereview-codex-5-3.md
?? docs/reviews/pr193-codereview-codex-5-4.md
?? docs/reviews/pr193-codereview-codex-5-5.md
?? docs/reviews/pr193-codereview-gemini-3-1-pro.md
?? docs/reviews/pr193-codereview-opus-4-6.md
?? docs/reviews/pr193-codereview-opus-4-7.md
?? docs/reviews/pr193-synthesis.md
?? docs/reviews/pr194-r2-codereview-codex-5-4.md
?? docs/reviews/pr194-r2-codereview-codex-5-5.md
?? docs/reviews/pr194-r2-codereview-gemini-3-1-pro.md
?? docs/reviews/pr194-r2-codereview-opus-4-7.md
?? docs/reviews/pr194-r2-synthesis.md
?? docs/reviews/pr198-codereview-codex-5-3.md
?? docs/reviews/pr198-codereview-codex-5-4.md
?? docs/reviews/pr198-codereview-codex-5-5.md
?? docs/reviews/pr198-codereview-gemini-3-1-pro.md
?? docs/reviews/pr198-codereview-opus-4-6.md
?? docs/reviews/pr198-codereview-opus-4-7.md
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
?? docs/reviews/pr200-r2-synthesis.md
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
?? docs/reviews/pr250-r3-synthesis.md
?? docs/reviews/r2-phase10/
?? docs/reviews/r2-phase6/
?? docs/reviews/r2/

