#!/usr/bin/env node
import readline from 'node:readline';
import type { ClanOrder } from '@clan-world/shared';
import { createChainClient, createConvexClient, type IChainClient, type IConvexClient } from '@clan-world/shared/adapters';
import { getElderN, readMemory, writeMemory, inboxFile, ackFile, UsageError } from './cli.js';
import fs from 'node:fs';
import path from 'node:path';

type JsonRpcRequest = {
  jsonrpc?: string;
  id?: string | number | null;
  method?: string;
  params?: unknown;
};

type ToolContent = { type: 'text'; text: string };
type ToolResult = { content: ToolContent[]; isError?: boolean };

export type ElderToolDeps = {
  convex: IConvexClient;
  chain: IChainClient;
  env?: Record<string, string | undefined>;
  homeBase?: string;
};

const text = (value: string): ToolResult => ({ content: [{ type: 'text', text: value }] });
const jsonText = (value: unknown): ToolResult => text(JSON.stringify(value, null, 2));

function asRecord(value: unknown): Record<string, unknown> {
  if (value === null || typeof value !== 'object' || Array.isArray(value)) return {};
  return value as Record<string, unknown>;
}

function stringArg(args: Record<string, unknown>, key: string): string | undefined {
  const value = args[key];
  return typeof value === 'string' ? value : undefined;
}

function numberArg(args: Record<string, unknown>, key: string): number | undefined {
  const value = args[key];
  return typeof value === 'number' && Number.isFinite(value) ? value : undefined;
}

function requireBody(args: Record<string, unknown>, key = 'body'): string {
  const value = stringArg(args, key)?.trim();
  if (!value) throw new UsageError(`${key} is required`);
  return value;
}

function ownClanId(env: Record<string, string | undefined>): string {
  return String(getElderN(env));
}

function assertOwnClanId(clanId: string | undefined, env: Record<string, string | undefined>): string {
  const own = ownClanId(env);
  const resolved = clanId ?? own;
  if (resolved !== own) throw new UsageError(`cross-clan access denied: expected clanId ${own}`);
  return resolved;
}

export async function callElderTool(name: string, args: unknown, deps: ElderToolDeps): Promise<ToolResult> {
  const env = deps.env ?? process.env;
  const parsed = asRecord(args);

  if (name === 'world_snapshot') {
    return jsonText(await deps.convex.getSnapshot());
  }

  if (name === 'clan_view') {
    const clanId = assertOwnClanId(stringArg(parsed, 'clanId'), env);
    return jsonText(await deps.chain.getClanFullView(clanId));
  }

  if (name === 'submit_orders') {
    const clanId = assertOwnClanId(stringArg(parsed, 'clanId'), env);
    const orders = parsed.orders;
    if (!Array.isArray(orders)) throw new UsageError('orders array is required');
    return jsonText(await deps.chain.submitOrders(clanId, orders as ClanOrder[]));
  }

  if (name === 'post_bulletin') {
    const clanId = Number(assertOwnClanId(stringArg(parsed, 'clanId'), env));
    const slot = numberArg(parsed, 'slot') ?? (await deps.convex.getSnapshot()).tick ?? 0;
    const body = requireBody(parsed);
    await deps.convex.postBulletin({ clanId, slot, body });
    return text('bulletin posted');
  }

  if (name === 'memory_recall') {
    const key = requireBody(parsed, 'key');
    const mem = readMemory(getElderN(env), deps.homeBase);
    return text(mem[key] ?? `no memory for ${key}`);
  }

  if (name === 'memory_save') {
    const key = requireBody(parsed, 'key');
    const value = requireBody(parsed, 'value');
    const elder = getElderN(env);
    const mem = readMemory(elder, deps.homeBase);
    mem[key] = value;
    writeMemory(elder, mem, deps.homeBase);
    return text(`saved ${key}`);
  }

  if (name === 'peer_inbox') {
    const file = inboxFile(getElderN(env), deps.homeBase);
    if (!fs.existsSync(file)) return text('inbox empty');
    const lines = fs.readFileSync(file, 'utf8').split('\n').filter(Boolean);
    return text(lines.length === 0 ? 'inbox empty' : lines.join('\n'));
  }

  if (name === 'peer_whisper') {
    const toClanId = requireBody(parsed, 'toClanId');
    const body = requireBody(parsed);
    return text(await import('./cli.js').then(({ runCommand }) =>
      runCommand('peer', 'whisper', [toClanId, body], deps, env, deps.homeBase),
    ).then(out => out.trim()));
  }

  if (name === 'ack_clear') {
    const elder = getElderN(env);
    const file = ackFile(elder, deps.homeBase);
    fs.mkdirSync(path.dirname(file), { recursive: true });
    fs.writeFileSync(file, new Date().toISOString() + '\n', { encoding: 'utf8', mode: 0o600 });
    fs.chmodSync(file, 0o600);
    return text('ack cleared');
  }

  if (name === 'rules') {
    return text(await import('./cli.js').then(({ runCommand }) =>
      runCommand('rules', undefined, [], deps, env, deps.homeBase),
    ));
  }

  throw new UsageError(`unknown elder MCP tool: ${name}`);
}

const tools = [
  {
    name: 'world_snapshot',
    description: 'Read current ClanWorld state.',
    inputSchema: { type: 'object', properties: {}, additionalProperties: false },
  },
  {
    name: 'clan_view',
    description: 'Read this Elder clan state. Defaults to ELDER_N.',
    inputSchema: { type: 'object', properties: { clanId: { type: 'string' } }, additionalProperties: false },
  },
  {
    name: 'submit_orders',
    description: 'Submit mission orders as direct JSON. Defaults clanId to ELDER_N.',
    inputSchema: {
      type: 'object',
      properties: { clanId: { type: 'string' }, orders: { type: 'array', items: { type: 'object' } } },
      required: ['orders'],
      additionalProperties: false,
    },
  },
  {
    name: 'post_bulletin',
    description: 'Post a public bulletin for this Elder clan.',
    inputSchema: {
      type: 'object',
      properties: { body: { type: 'string' }, clanId: { type: 'string' }, slot: { type: 'number' } },
      required: ['body'],
      additionalProperties: false,
    },
  },
  {
    name: 'memory_recall',
    description: 'Recall an Elder memory key.',
    inputSchema: { type: 'object', properties: { key: { type: 'string' } }, required: ['key'], additionalProperties: false },
  },
  {
    name: 'memory_save',
    description: 'Save an Elder memory key/value.',
    inputSchema: {
      type: 'object',
      properties: { key: { type: 'string' }, value: { type: 'string' } },
      required: ['key', 'value'],
      additionalProperties: false,
    },
  },
  {
    name: 'peer_inbox',
    description: 'Read this Elder peer inbox.',
    inputSchema: { type: 'object', properties: {}, additionalProperties: false },
  },
  {
    name: 'peer_whisper',
    description: 'Send a private whisper to another clan.',
    inputSchema: {
      type: 'object',
      properties: { toClanId: { type: 'string' }, body: { type: 'string' } },
      required: ['toClanId', 'body'],
      additionalProperties: false,
    },
  },
  {
    name: 'ack_clear',
    description: 'Signal the runner that this Elder is ready for context wipe.',
    inputSchema: { type: 'object', properties: {}, additionalProperties: false },
  },
  {
    name: 'rules',
    description: 'Read game rules, action codes, regions, and constants.',
    inputSchema: { type: 'object', properties: {}, additionalProperties: false },
  },
];

function writeResponse(id: JsonRpcRequest['id'], result: unknown): void {
  process.stdout.write(JSON.stringify({ jsonrpc: '2.0', id, result }) + '\n');
}

function writeError(id: JsonRpcRequest['id'], code: number, message: string): void {
  process.stdout.write(JSON.stringify({ jsonrpc: '2.0', id, error: { code, message } }) + '\n');
}

async function handleRequest(request: JsonRpcRequest): Promise<void> {
  const id = request.id ?? null;
  if (request.method === 'initialize') {
    writeResponse(id, {
      protocolVersion: '2024-11-05',
      capabilities: { tools: {} },
      serverInfo: { name: 'elder', version: '0.1.0' },
    });
    return;
  }

  if (request.method === 'notifications/initialized') return;

  if (request.method === 'tools/list') {
    writeResponse(id, { tools });
    return;
  }

  if (request.method === 'tools/call') {
    const params = asRecord(request.params);
    const name = stringArg(params, 'name');
    if (!name) throw new UsageError('tools/call requires name');
    const deps = { convex: createConvexClient(), chain: createChainClient() };
    writeResponse(id, await callElderTool(name, params.arguments, deps));
    return;
  }

  writeError(id, -32601, `method not found: ${request.method ?? ''}`);
}

async function main(): Promise<void> {
  const rl = readline.createInterface({ input: process.stdin, crlfDelay: Infinity });
  for await (const line of rl) {
    if (!line.trim()) continue;
    let request: JsonRpcRequest;
    try {
      request = JSON.parse(line) as JsonRpcRequest;
      await handleRequest(request);
    } catch (err) {
      const id = (() => {
        try { return (JSON.parse(line) as JsonRpcRequest).id ?? null; } catch { return null; }
      })();
      const message = err instanceof Error ? err.message : String(err);
      writeError(id, -32000, message);
    }
  }
}

if (process.argv[1]?.endsWith('/mcp.ts')) {
  main().catch(err => {
    process.stderr.write(`elder-mcp: fatal: ${String(err)}\n`);
    process.exit(1);
  });
}
