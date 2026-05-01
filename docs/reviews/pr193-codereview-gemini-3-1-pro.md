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
      date: 'Thu, 30 Apr 2026 02:38:15 GMT',
      server: 'ESF',
      'server-timing': 'gfet4t7; dur=703',
      vary: 'Origin, X-Origin, Referer',
      'x-cloudaicompanion-trace-id': '2d22964689bb993',
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
      date: 'Thu, 30 Apr 2026 02:38:21 GMT',
      server: 'ESF',
      'server-timing': 'gfet4t7; dur=361',
      vary: 'Origin, X-Origin, Referer',
      'x-cloudaicompanion-trace-id': '7f978c1f40d5a5ba',
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
      date: 'Thu, 30 Apr 2026 02:38:32 GMT',
      server: 'ESF',
      'server-timing': 'gfet4t7; dur=702',
      vary: 'Origin, X-Origin, Referer',
      'x-cloudaicompanion-trace-id': 'fcb37a6c09996ada',
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
      date: 'Thu, 30 Apr 2026 02:38:57 GMT',
      server: 'ESF',
      'server-timing': 'gfet4t7; dur=308',
      vary: 'Origin, X-Origin, Referer',
      'x-cloudaicompanion-trace-id': '44fbd734f5637869',
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
Error executing tool run_shell_command: Tool "run_shell_command" not found. Did you mean one of: "grep_search", "cli_help", "read_file"?
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
      date: 'Thu, 30 Apr 2026 02:39:53 GMT',
      server: 'ESF',
      'server-timing': 'gfet4t7; dur=237',
      vary: 'Origin, X-Origin, Referer',
      'x-cloudaicompanion-trace-id': 'd1f590d29509941c',
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
      date: 'Thu, 30 Apr 2026 02:40:00 GMT',
      server: 'ESF',
      'server-timing': 'gfet4t7; dur=1080',
      vary: 'Origin, X-Origin, Referer',
      'x-cloudaicompanion-trace-id': '992c6892c3338fde',
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
      date: 'Thu, 30 Apr 2026 02:40:09 GMT',
      server: 'ESF',
      'server-timing': 'gfet4t7; dur=379',
      vary: 'Origin, X-Origin, Referer',
      'x-cloudaicompanion-trace-id': 'a0142e1faa5acef7',
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
      date: 'Thu, 30 Apr 2026 02:40:25 GMT',
      server: 'ESF',
      'server-timing': 'gfet4t7; dur=677',
      vary: 'Origin, X-Origin, Referer',
      'x-cloudaicompanion-trace-id': '447aad7318b2becc',
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
Attempt 5 failed with status 429. Retrying with backoff... _GaxiosError: [{
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
      date: 'Thu, 30 Apr 2026 02:41:01 GMT',
      server: 'ESF',
      'server-timing': 'gfet4t7; dur=258',
      vary: 'Origin, X-Origin, Referer',
      'x-cloudaicompanion-trace-id': 'a33ea661899c06f2',
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
Attempt 6 failed with status 429. Retrying with backoff... _GaxiosError: [{
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
      date: 'Thu, 30 Apr 2026 02:41:27 GMT',
      server: 'ESF',
      'server-timing': 'gfet4t7; dur=771',
      vary: 'Origin, X-Origin, Referer',
      'x-cloudaicompanion-trace-id': '52f747a6f6fa7e9e',
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
Attempt 7 failed with status 429. Retrying with backoff... _GaxiosError: [{
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
      date: 'Thu, 30 Apr 2026 02:41:56 GMT',
      server: 'ESF',
      'server-timing': 'gfet4t7; dur=399',
      vary: 'Origin, X-Origin, Referer',
      'x-cloudaicompanion-trace-id': 'e15dd9c74b6d77bb',
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
Attempt 8 failed with status 429. Retrying with backoff... _GaxiosError: [{
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
      date: 'Thu, 30 Apr 2026 02:42:32 GMT',
      server: 'ESF',
      'server-timing': 'gfet4t7; dur=279',
      vary: 'Origin, X-Origin, Referer',
      'x-cloudaicompanion-trace-id': 'e89e6a9b0120cfb6',
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
Attempt 9 failed with status 429. Retrying with backoff... _GaxiosError: [{
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
      date: 'Thu, 30 Apr 2026 02:43:02 GMT',
      server: 'ESF',
      'server-timing': 'gfet4t7; dur=252',
      vary: 'Origin, X-Origin, Referer',
      'x-cloudaicompanion-trace-id': 'e2c236cab705b32d',
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
Attempt 10 failed: No capacity available for model gemini-3.1-pro-preview on the server. Max attempts reached
Error when talking to Gemini API Full report available at: /tmp/gemini-client-error-Turn.run-sendMessageStream-2026-04-30T02-43-33-175Z.json RetryableQuotaError: No capacity available for model gemini-3.1-pro-preview on the server
    at classifyGoogleError (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:274525:12)
    at retryWithBackoff (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:275105:31)
    at process.processTicksAndRejections (node:internal/process/task_queues:103:5)
    at async GeminiChat.makeApiCallAndProcessStream (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:310999:28)
    at async GeminiChat.streamWithRetries (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:310837:29)
    at async Turn.run (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:311329:24)
    at async GeminiClient.processTurn (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:315405:22)
    at async GeminiClient.sendMessageStream (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/chunk-IWSCP2GY.js:315518:14)
    at async file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/gemini.js:10168:26
    at async main (file:///home/claude/.local/lib/node_modules/@google/gemini-cli/bundle/gemini.js:15201:5) {
  cause: {
    code: 429,
    message: 'No capacity available for model gemini-3.1-pro-preview on the server',
    details: [ [Object] ]
  },
  retryDelayMs: undefined
}
An unexpected critical error occurred:[object Object]
EXIT=1
