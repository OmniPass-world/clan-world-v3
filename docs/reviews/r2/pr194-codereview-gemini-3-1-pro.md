Attempt 1 failed with status 429. Retrying with backoff... _GaxiosError: [{
  "error": {
    "code": 429,
    "message": "No capacity available for model gemini-3.1-pro-preview on the server",
    "errors": [
      {
        "message": "No capacity available for model gemini-3.1-pro-preview on the server",
        "domain": "global",
        "reason": "rateLimitExceeded"
      }
    ],
    "status": "RESOURCE_EXHAUSTED",
    "details": [
      {
        "@type": "type.googleapis.com/google.rpc.ErrorInfo",
        "reason": "MODEL_CAPACITY_EXHAUSTED",
        "domain": "cloudcode-pa.googleapis.com",
        "metadata": {
          "model": "gemini-3.1-pro-preview"
        }
      }
    ]
  }
}
]
    at Gaxios._request (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:8578:19)
    at process.processTicksAndRejections (node:internal/process/task_queues:103:5)
    at async _OAuth2Client.requestAsync (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:10541:16)
    at async CodeAssistServer.requestStreamingPost (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:277484:17)
    at async CodeAssistServer.generateContentStream (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:277284:23)
    at async file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:278125:19
    at async file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:255118:23
    at async retryWithBackoff (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:275082:23)
    at async GeminiChat.makeApiCallAndProcessStream (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:310999:28)
    at async GeminiChat.streamWithRetries (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:310837:29) {
  config: {
    url: 'https://cloudcode-pa.googleapis.com/v1internal:streamGenerateContent?alt=sse',
    method: 'POST',
    params: { alt: 'sse' },
    headers: {
      'Content-Type': 'application/json',
      'User-Agent': 'GeminiCLI/0.38.2/gemini-3.1-pro-preview (linux; x64; terminal) google-api-nodejs-client/9.15.1',
      Authorization: '<<REDACTED> - See `errorRedactor` option in `gaxios` for configuration>.',
      'x-goog-api-client': 'gl-node/24.13.1'
    },
    responseType: 'stream',
    body: '<<REDACTED> - See `errorRedactor` option in `gaxios` for configuration>.',
    signal: AbortSignal { aborted: false },
    retry: false,
    paramsSerializer: [Function: paramsSerializer],
    validateStatus: [Function: validateStatus],
    errorRedactor: [Function: defaultErrorRedactor]
  },
  response: {
    config: {
      url: 'https://cloudcode-pa.googleapis.com/v1internal:streamGenerateContent?alt=sse',
      method: 'POST',
      params: [Object],
      headers: [Object],
      responseType: 'stream',
      body: '<<REDACTED> - See `errorRedactor` option in `gaxios` for configuration>.',
      signal: [AbortSignal],
      retry: false,
      paramsSerializer: [Function: paramsSerializer],
      validateStatus: [Function: validateStatus],
      errorRedactor: [Function: defaultErrorRedactor]
    },
    data: '[{\n' +
      '  "error": {\n' +
      '    "code": 429,\n' +
      '    "message": "No capacity available for model gemini-3.1-pro-preview on the server",\n' +
      '    "errors": [\n' +
      '      {\n' +
      '        "message": "No capacity available for model gemini-3.1-pro-preview on the server",\n' +
      '        "domain": "global",\n' +
      '        "reason": "rateLimitExceeded"\n' +
      '      }\n' +
      '    ],\n' +
      '    "status": "RESOURCE_EXHAUSTED",\n' +
      '    "details": [\n' +
      '      {\n' +
      '        "@type": "type.googleapis.com/google.rpc.ErrorInfo",\n' +
      '        "reason": "MODEL_CAPACITY_EXHAUSTED",\n' +
      '        "domain": "cloudcode-pa.googleapis.com",\n' +
      '        "metadata": {\n' +
      '          "model": "gemini-3.1-pro-preview"\n' +
      '        }\n' +
      '      }\n' +
      '    ]\n' +
      '  }\n' +
      '}\n' +
      ']',
    headers: {
      'alt-svc': 'h3=":443"; ma=2592000,h3-29=":443"; ma=2592000',
      'content-length': '630',
      'content-type': 'application/json; charset=UTF-8',
      date: 'Wed, 29 Apr 2026 17:00:51 GMT',
      server: 'ESF',
      'server-timing': 'gfet4t7; dur=976',
      vary: 'Origin, X-Origin, Referer',
      'x-cloudaicompanion-trace-id': 'e63d37989c52d58c',
      'x-content-type-options': 'nosniff',
      'x-frame-options': 'SAMEORIGIN',
      'x-xss-protection': '0'
    },
    status: 429,
    statusText: 'Too Many Requests',
    request: {
      responseURL: 'https://cloudcode-pa.googleapis.com/v1internal:streamGenerateContent?alt=sse'
    }
  },
  error: undefined,
  status: 429,
  Symbol(gaxios-gaxios-error): '6.7.1'
}
Attempt 2 failed with status 429. Retrying with backoff... _GaxiosError: [{
  "error": {
    "code": 429,
    "message": "No capacity available for model gemini-3.1-pro-preview on the server",
    "errors": [
      {
        "message": "No capacity available for model gemini-3.1-pro-preview on the server",
        "domain": "global",
        "reason": "rateLimitExceeded"
      }
    ],
    "status": "RESOURCE_EXHAUSTED",
    "details": [
      {
        "@type": "type.googleapis.com/google.rpc.ErrorInfo",
        "reason": "MODEL_CAPACITY_EXHAUSTED",
        "domain": "cloudcode-pa.googleapis.com",
        "metadata": {
          "model": "gemini-3.1-pro-preview"
        }
      }
    ]
  }
}
]
    at Gaxios._request (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:8578:19)
    at process.processTicksAndRejections (node:internal/process/task_queues:103:5)
    at async _OAuth2Client.requestAsync (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:10541:16)
    at async CodeAssistServer.requestStreamingPost (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:277484:17)
    at async CodeAssistServer.generateContentStream (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:277284:23)
    at async file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:278125:19
    at async file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:255118:23
    at async retryWithBackoff (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:275082:23)
    at async GeminiChat.makeApiCallAndProcessStream (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:310999:28)
    at async GeminiChat.streamWithRetries (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:310837:29) {
  config: {
    url: 'https://cloudcode-pa.googleapis.com/v1internal:streamGenerateContent?alt=sse',
    method: 'POST',
    params: { alt: 'sse' },
    headers: {
      'Content-Type': 'application/json',
      'User-Agent': 'GeminiCLI/0.38.2/gemini-3.1-pro-preview (linux; x64; terminal) google-api-nodejs-client/9.15.1',
      Authorization: '<<REDACTED> - See `errorRedactor` option in `gaxios` for configuration>.',
      'x-goog-api-client': 'gl-node/24.13.1'
    },
    responseType: 'stream',
    body: '<<REDACTED> - See `errorRedactor` option in `gaxios` for configuration>.',
    signal: AbortSignal { aborted: false },
    retry: false,
    paramsSerializer: [Function: paramsSerializer],
    validateStatus: [Function: validateStatus],
    errorRedactor: [Function: defaultErrorRedactor]
  },
  response: {
    config: {
      url: 'https://cloudcode-pa.googleapis.com/v1internal:streamGenerateContent?alt=sse',
      method: 'POST',
      params: [Object],
      headers: [Object],
      responseType: 'stream',
      body: '<<REDACTED> - See `errorRedactor` option in `gaxios` for configuration>.',
      signal: [AbortSignal],
      retry: false,
      paramsSerializer: [Function: paramsSerializer],
      validateStatus: [Function: validateStatus],
      errorRedactor: [Function: defaultErrorRedactor]
    },
    data: '[{\n' +
      '  "error": {\n' +
      '    "code": 429,\n' +
      '    "message": "No capacity available for model gemini-3.1-pro-preview on the server",\n' +
      '    "errors": [\n' +
      '      {\n' +
      '        "message": "No capacity available for model gemini-3.1-pro-preview on the server",\n' +
      '        "domain": "global",\n' +
      '        "reason": "rateLimitExceeded"\n' +
      '      }\n' +
      '    ],\n' +
      '    "status": "RESOURCE_EXHAUSTED",\n' +
      '    "details": [\n' +
      '      {\n' +
      '        "@type": "type.googleapis.com/google.rpc.ErrorInfo",\n' +
      '        "reason": "MODEL_CAPACITY_EXHAUSTED",\n' +
      '        "domain": "cloudcode-pa.googleapis.com",\n' +
      '        "metadata": {\n' +
      '          "model": "gemini-3.1-pro-preview"\n' +
      '        }\n' +
      '      }\n' +
      '    ]\n' +
      '  }\n' +
      '}\n' +
      ']',
    headers: {
      'alt-svc': 'h3=":443"; ma=2592000,h3-29=":443"; ma=2592000',
      'content-length': '630',
      'content-type': 'application/json; charset=UTF-8',
      date: 'Wed, 29 Apr 2026 17:00:56 GMT',
      server: 'ESF',
      'server-timing': 'gfet4t7; dur=635',
      vary: 'Origin, X-Origin, Referer',
      'x-cloudaicompanion-trace-id': '618aed2d06526449',
      'x-content-type-options': 'nosniff',
      'x-frame-options': 'SAMEORIGIN',
      'x-xss-protection': '0'
    },
    status: 429,
    statusText: 'Too Many Requests',
    request: {
      responseURL: 'https://cloudcode-pa.googleapis.com/v1internal:streamGenerateContent?alt=sse'
    }
  },
  error: undefined,
  status: 429,
  Symbol(gaxios-gaxios-error): '6.7.1'
}
Attempt 3 failed with status 429. Retrying with backoff... _GaxiosError: [{
  "error": {
    "code": 429,
    "message": "No capacity available for model gemini-3.1-pro-preview on the server",
    "errors": [
      {
        "message": "No capacity available for model gemini-3.1-pro-preview on the server",
        "domain": "global",
        "reason": "rateLimitExceeded"
      }
    ],
    "status": "RESOURCE_EXHAUSTED",
    "details": [
      {
        "@type": "type.googleapis.com/google.rpc.ErrorInfo",
        "reason": "MODEL_CAPACITY_EXHAUSTED",
        "domain": "cloudcode-pa.googleapis.com",
        "metadata": {
          "model": "gemini-3.1-pro-preview"
        }
      }
    ]
  }
}
]
    at Gaxios._request (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:8578:19)
    at process.processTicksAndRejections (node:internal/process/task_queues:103:5)
    at async _OAuth2Client.requestAsync (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:10541:16)
    at async CodeAssistServer.requestStreamingPost (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:277484:17)
    at async CodeAssistServer.generateContentStream (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:277284:23)
    at async file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:278125:19
    at async file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:255118:23
    at async retryWithBackoff (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:275082:23)
    at async GeminiChat.makeApiCallAndProcessStream (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:310999:28)
    at async GeminiChat.streamWithRetries (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:310837:29) {
  config: {
    url: 'https://cloudcode-pa.googleapis.com/v1internal:streamGenerateContent?alt=sse',
    method: 'POST',
    params: { alt: 'sse' },
    headers: {
      'Content-Type': 'application/json',
      'User-Agent': 'GeminiCLI/0.38.2/gemini-3.1-pro-preview (linux; x64; terminal) google-api-nodejs-client/9.15.1',
      Authorization: '<<REDACTED> - See `errorRedactor` option in `gaxios` for configuration>.',
      'x-goog-api-client': 'gl-node/24.13.1'
    },
    responseType: 'stream',
    body: '<<REDACTED> - See `errorRedactor` option in `gaxios` for configuration>.',
    signal: AbortSignal { aborted: false },
    retry: false,
    paramsSerializer: [Function: paramsSerializer],
    validateStatus: [Function: validateStatus],
    errorRedactor: [Function: defaultErrorRedactor]
  },
  response: {
    config: {
      url: 'https://cloudcode-pa.googleapis.com/v1internal:streamGenerateContent?alt=sse',
      method: 'POST',
      params: [Object],
      headers: [Object],
      responseType: 'stream',
      body: '<<REDACTED> - See `errorRedactor` option in `gaxios` for configuration>.',
      signal: [AbortSignal],
      retry: false,
      paramsSerializer: [Function: paramsSerializer],
      validateStatus: [Function: validateStatus],
      errorRedactor: [Function: defaultErrorRedactor]
    },
    data: '[{\n' +
      '  "error": {\n' +
      '    "code": 429,\n' +
      '    "message": "No capacity available for model gemini-3.1-pro-preview on the server",\n' +
      '    "errors": [\n' +
      '      {\n' +
      '        "message": "No capacity available for model gemini-3.1-pro-preview on the server",\n' +
      '        "domain": "global",\n' +
      '        "reason": "rateLimitExceeded"\n' +
      '      }\n' +
      '    ],\n' +
      '    "status": "RESOURCE_EXHAUSTED",\n' +
      '    "details": [\n' +
      '      {\n' +
      '        "@type": "type.googleapis.com/google.rpc.ErrorInfo",\n' +
      '        "reason": "MODEL_CAPACITY_EXHAUSTED",\n' +
      '        "domain": "cloudcode-pa.googleapis.com",\n' +
      '        "metadata": {\n' +
      '          "model": "gemini-3.1-pro-preview"\n' +
      '        }\n' +
      '      }\n' +
      '    ]\n' +
      '  }\n' +
      '}\n' +
      ']',
    headers: {
      'alt-svc': 'h3=":443"; ma=2592000,h3-29=":443"; ma=2592000',
      'content-length': '630',
      'content-type': 'application/json; charset=UTF-8',
      date: 'Wed, 29 Apr 2026 17:01:04 GMT',
      server: 'ESF',
      'server-timing': 'gfet4t7; dur=466',
      vary: 'Origin, X-Origin, Referer',
      'x-cloudaicompanion-trace-id': '911c1b1a810476ab',
      'x-content-type-options': 'nosniff',
      'x-frame-options': 'SAMEORIGIN',
      'x-xss-protection': '0'
    },
    status: 429,
    statusText: 'Too Many Requests',
    request: {
      responseURL: 'https://cloudcode-pa.googleapis.com/v1internal:streamGenerateContent?alt=sse'
    }
  },
  error: undefined,
  status: 429,
  Symbol(gaxios-gaxios-error): '6.7.1'
}
Attempt 4 failed with status 429. Retrying with backoff... _GaxiosError: [{
  "error": {
    "code": 429,
    "message": "No capacity available for model gemini-3.1-pro-preview on the server",
    "errors": [
      {
        "message": "No capacity available for model gemini-3.1-pro-preview on the server",
        "domain": "global",
        "reason": "rateLimitExceeded"
      }
    ],
    "status": "RESOURCE_EXHAUSTED",
    "details": [
      {
        "@type": "type.googleapis.com/google.rpc.ErrorInfo",
        "reason": "MODEL_CAPACITY_EXHAUSTED",
        "domain": "cloudcode-pa.googleapis.com",
        "metadata": {
          "model": "gemini-3.1-pro-preview"
        }
      }
    ]
  }
}
]
    at Gaxios._request (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:8578:19)
    at process.processTicksAndRejections (node:internal/process/task_queues:103:5)
    at async _OAuth2Client.requestAsync (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:10541:16)
    at async CodeAssistServer.requestStreamingPost (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:277484:17)
    at async CodeAssistServer.generateContentStream (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:277284:23)
    at async file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:278125:19
    at async file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:255118:23
    at async retryWithBackoff (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:275082:23)
    at async GeminiChat.makeApiCallAndProcessStream (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:310999:28)
    at async GeminiChat.streamWithRetries (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:310837:29) {
  config: {
    url: 'https://cloudcode-pa.googleapis.com/v1internal:streamGenerateContent?alt=sse',
    method: 'POST',
    params: { alt: 'sse' },
    headers: {
      'Content-Type': 'application/json',
      'User-Agent': 'GeminiCLI/0.38.2/gemini-3.1-pro-preview (linux; x64; terminal) google-api-nodejs-client/9.15.1',
      Authorization: '<<REDACTED> - See `errorRedactor` option in `gaxios` for configuration>.',
      'x-goog-api-client': 'gl-node/24.13.1'
    },
    responseType: 'stream',
    body: '<<REDACTED> - See `errorRedactor` option in `gaxios` for configuration>.',
    signal: AbortSignal { aborted: false },
    retry: false,
    paramsSerializer: [Function: paramsSerializer],
    validateStatus: [Function: validateStatus],
    errorRedactor: [Function: defaultErrorRedactor]
  },
  response: {
    config: {
      url: 'https://cloudcode-pa.googleapis.com/v1internal:streamGenerateContent?alt=sse',
      method: 'POST',
      params: [Object],
      headers: [Object],
      responseType: 'stream',
      body: '<<REDACTED> - See `errorRedactor` option in `gaxios` for configuration>.',
      signal: [AbortSignal],
      retry: false,
      paramsSerializer: [Function: paramsSerializer],
      validateStatus: [Function: validateStatus],
      errorRedactor: [Function: defaultErrorRedactor]
    },
    data: '[{\n' +
      '  "error": {\n' +
      '    "code": 429,\n' +
      '    "message": "No capacity available for model gemini-3.1-pro-preview on the server",\n' +
      '    "errors": [\n' +
      '      {\n' +
      '        "message": "No capacity available for model gemini-3.1-pro-preview on the server",\n' +
      '        "domain": "global",\n' +
      '        "reason": "rateLimitExceeded"\n' +
      '      }\n' +
      '    ],\n' +
      '    "status": "RESOURCE_EXHAUSTED",\n' +
      '    "details": [\n' +
      '      {\n' +
      '        "@type": "type.googleapis.com/google.rpc.ErrorInfo",\n' +
      '        "reason": "MODEL_CAPACITY_EXHAUSTED",\n' +
      '        "domain": "cloudcode-pa.googleapis.com",\n' +
      '        "metadata": {\n' +
      '          "model": "gemini-3.1-pro-preview"\n' +
      '        }\n' +
      '      }\n' +
      '    ]\n' +
      '  }\n' +
      '}\n' +
      ']',
    headers: {
      'alt-svc': 'h3=":443"; ma=2592000,h3-29=":443"; ma=2592000',
      'content-length': '630',
      'content-type': 'application/json; charset=UTF-8',
      date: 'Wed, 29 Apr 2026 17:01:21 GMT',
      server: 'ESF',
      'server-timing': 'gfet4t7; dur=516',
      vary: 'Origin, X-Origin, Referer',
      'x-cloudaicompanion-trace-id': 'b507eb46eab8c212',
      'x-content-type-options': 'nosniff',
      'x-frame-options': 'SAMEORIGIN',
      'x-xss-protection': '0'
    },
    status: 429,
    statusText: 'Too Many Requests',
    request: {
      responseURL: 'https://cloudcode-pa.googleapis.com/v1internal:streamGenerateContent?alt=sse'
    }
  },
  error: undefined,
  status: 429,
  Symbol(gaxios-gaxios-error): '6.7.1'
}
# Phase Super-Swarm Review — PR #194 (head 23f7f1a)

## SUMMARY
VERDICT: **NEEDS_FIXES**. While the Phase 9 PR successfully integrates foundational spawn chance logic and deterministic attack resolution, it suffers from critical architectural drift regarding the bandit state machine and eager-settlement scope. Bandits lack automated transitions to select targets or recover from escapes, rendering the threat entirely inert in practice. Additionally, wall defense calculations and view-only settlement simulations contain edge cases that undermine base progression and UI accuracy. 

## HIGH severity findings

**Bandit state machine halts indefinitely due to missing target selection and escape-recovery transitions**
`ClanWorld.sol` in `_advanceBanditStates()` and `_resolveBanditAttack()`
Although sub-issues 9.1 and 9.4 outline a complete state machine, the actual automated transitions driving it are missing. There is no logic in `_advanceBanditStates()` to transition a bandit from `Camped` to `Attacking` (Target Selection is completely unimplemented). Furthermore, when an attack resolves without a defeat and a bandit transitions to `Escaped`, there is no logic to transition it back to `Resting`. As a result, once spawned, a bandit will get permanently stuck in `Camped` (or, if forcefully triggered, stuck in `Escaped`).
*Suggested fix:* Implement target selection logic in `_advanceBanditStates()` to evaluate eligible `Camped` bandits, pick a valid target, and call `_transitionBanditToAttacking()`. Additionally, add a transition from `Escaped` to `Resting` either immediately during attack resolution or as an automated step in `_advanceBanditStates()`.

**Eager settlement completely skips target selection requirements for existing bandits**
`ClanWorld.sol` in `_eagerSettleForBandits()`
Sub-issue 9.3 mandates eager-settling bases and defenders "before target selection" to ensure targets are chosen based on fresh, up-to-date loot values. However, `_eagerSettleForBandits()` includes an early return `if (_activeBanditCount >= MAX_TOTAL_BANDITS) return;`, completely bypassing all settlement when the global bandit cap is reached. Furthermore, it only settles regions that pass `_isBanditSpawnRegionCandidate()`—ignoring regions that have existing `Camped` bandits waiting to select targets but aren't currently eligible for a *new* spawn. This guarantees stale target evaluation.
*Suggested fix:* Remove the early return for `MAX_TOTAL_BANDITS`. Adjust the region loop to eager-settle any region that either is a spawn candidate OR contains a `Camped` bandit ready to perform target selection.

## MEDIUM severity findings

**Wall defense logic bypasses levels by hard-capping absorption per attack**
`ClanWorld.sol` in `_applyBanditWallDamage()`
The wall damage calculation restricts the maximum damage absorbed to exactly `WALL_HP_PER_LEVEL` (100) per attack, regardless of `clan.wallLevel`. Any incoming damage exceeding 100 instantly spills over to the base and clansmen, and the wall only loses exactly 1 level. This means a fully upgraded Level 5 wall offers no more immediate damage mitigation against a large bandit attack than a Level 1 wall, heavily undermining the defensive value of upgrading walls.
*Suggested fix:* Update the logic to allow the wall to absorb up to `clan.wallLevel * WALL_HP_PER_LEVEL` total damage. Calculate the number of wall levels lost based on the actual damage absorbed, and deduct multiple levels if the damage exceeds a single level's HP threshold.

**Isolated view-only simulation creates zombie defenders for dead bases**
`ClanWorld.sol` in `_simulateSettleMissionForClansman()`
The `_simulateSettleToTick` function simulates individual clans in isolation. If a clansman is on a `DefendBase` mission and the target clan is eliminated by bandits during the unsettled window, the real `_markClanDead` correctly releases those defenders. However, the view-only simulation has no visibility into the target clan's death and will incorrectly mock the clansman as `ACTING` (defending) indefinitely. This will cause `getClanFullView` and the UI to show clansmen stuck defending a dead base until their own clan is explicitly settled.
*Suggested fix:* In `_simulateSettleMissionForClansman` (or `getDerivedClansmanState`), perform a quick read of the target clan's storage state (`_clans[m.targetClanId].clanState`). If the target base is `DEAD`, safely mock the clansman's state as `WAITING` with an inactive mission.

## LOW severity findings

**BanditAttackResolved event hardcodes stolen resources to zero**
`ClanWorld.sol` in `_resolveBanditAttack()`
The `BanditAttackResolved` event emits hardcoded `0`s for the `stolenWood`, `stolenIron`, `stolenWheat`, and `stolenFish` fields even if the bandit escapes successfully. If resource stealing is deferred to a future phase, it should be documented as a stub; otherwise, the bandit should actually drain resources from the target clan's vault upon an un-defeated attack.

**ActiveBanditView hardcodes projected target loot value**
`ClanWorld.sol` in `getActiveBanditView()`
The `ActiveBanditView` struct hardcodes `projectedTargetLootValue: 0`, leaving the UI/indexer blind to the potential value of the attack.

## Cross-cutting observations

**Test harness mocks mask missing implementation logic**
The tests `BanditAttackResolution.t.sol` and `Bandit.t.sol` manually expose and invoke `_transitionBanditToAttacking()` via their harness contracts. By forcefully mocking the transition into the `Attacking` state, the tests pass perfectly and obscure the fact that the underlying state machine lacks the autonomous target selection logic required for the feature to function in production. Avoid mocking control flow that the protocol itself is responsible for orchestrating.
