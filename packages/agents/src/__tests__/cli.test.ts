import { describe, it, expect, beforeEach, vi } from 'vitest';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import type { IConvexClient, IChainClient } from '@clan-world/shared/adapters';
import type { WorldSnapshot, ClanFullView, Whisper } from '@clan-world/shared';
import { runCommand, readMemory, writeMemory, UsageError } from '../cli.js';

// Minimal stub factory helpers — only methods the CLI uses
function makeConvex(overrides: Partial<IConvexClient> = {}): IConvexClient {
  return {
    getSnapshot: vi.fn(async () => ({
      tick: 42,
      tickEpoch: { startedAt: 1000, durationMs: 20_000 },
      regions: [],
      clans: [],
    } satisfies WorldSnapshot)),
    getClanFullView: vi.fn(async (clanId: string) => ({
      clan: { id: clanId, name: `Test Clan ${clanId}`, treasury: '100' },
      controlledRegions: [],
      pendingOrders: [],
      whispers: [],
    } satisfies ClanFullView)),
    postLog: vi.fn(async () => {}),
    subscribeWhispers: vi.fn((_clanId: string, _onWhisper: (w: Whisper) => void) => () => {}),
    ...overrides,
  };
}

function makeChain(overrides: Partial<IChainClient> = {}): IChainClient {
  return {
    getCurrentTick: vi.fn(async () => 0),
    submitOrders: vi.fn(async () => ({ txHash: '0xdeadbeef' })),
    getClanFullView: vi.fn(async (clanId: string) => ({
      clan: { id: clanId, name: `Chain Clan ${clanId}`, treasury: '0' },
      controlledRegions: [],
      pendingOrders: [],
      whispers: [],
    } satisfies ClanFullView)),
    ...overrides,
  };
}

function tmpHome(): string {
  return fs.mkdtempSync(path.join(os.tmpdir(), 'elder-test-'));
}

describe('world snapshot', () => {
  it('returns data from mocked convex client', async () => {
    const convex = makeConvex();
    const out = await runCommand('world', 'snapshot', [], { convex, chain: makeChain() });
    const parsed = JSON.parse(out) as WorldSnapshot;
    expect(parsed.tick).toBe(42);
    expect(convex.getSnapshot).toHaveBeenCalledOnce();
  });
});

describe('clan view', () => {
  it('returns data from mocked convex client', async () => {
    const convex = makeConvex();
    const out = await runCommand('clan', 'view', ['clan-1'], { convex, chain: makeChain() });
    const parsed = JSON.parse(out) as ClanFullView;
    expect(parsed.clan.id).toBe('clan-1');
    expect(convex.getClanFullView).toHaveBeenCalledWith('clan-1');
  });

  it('throws UsageError when clanId is missing', async () => {
    await expect(
      runCommand('clan', 'view', [], { convex: makeConvex(), chain: makeChain() }),
    ).rejects.toBeInstanceOf(UsageError);
  });
});

describe('clan submit-orders', () => {
  it('parses json file and calls chain client', async () => {
    const home = tmpHome();
    const ordersFile = path.join(home, 'orders.json');
    fs.writeFileSync(
      ordersFile,
      JSON.stringify({
        clanId: '1',
        orders: [{ kind: 'mission', payload: { clansmanId: 5, gotoRegion: 2, action: 1 } }],
      }),
    );
    const chain = makeChain();
    const out = await runCommand('clan', 'submit-orders', [ordersFile], {
      convex: makeConvex(),
      chain,
    });
    const result = JSON.parse(out) as { txHash: string };
    expect(result.txHash).toBe('0xdeadbeef');
    expect(chain.submitOrders).toHaveBeenCalledWith('1', expect.arrayContaining([expect.objectContaining({ kind: 'mission' })]));
  });
});

describe('memory save/recall', () => {
  let home: string;
  const env = { ELDER_N: '2' };

  beforeEach(() => {
    home = tmpHome();
  });

  it('round-trips a value', async () => {
    const deps = { convex: makeConvex(), chain: makeChain() };
    await runCommand('memory', 'save', ['plan', 'expand east'], deps, env, home);
    const out = await runCommand('memory', 'recall', ['plan'], deps, env, home);
    expect(out.trim()).toBe('expand east');
  });

  it('reports missing topic', async () => {
    const out = await runCommand('memory', 'recall', ['nonexistent'], { convex: makeConvex(), chain: makeChain() }, env, home);
    expect(out.trim()).toBe('no memory for nonexistent');
  });

  it('persists across readMemory calls', async () => {
    const deps = { convex: makeConvex(), chain: makeChain() };
    await runCommand('memory', 'save', ['key1', 'val1'], deps, env, home);
    await runCommand('memory', 'save', ['key2', 'val2'], deps, env, home);
    const mem = readMemory(2, home);
    expect(mem['key1']).toBe('val1');
    expect(mem['key2']).toBe('val2');
  });
});

describe('peer whisper/inbox', () => {
  let home: string;
  const env = { ELDER_N: '3' };

  beforeEach(() => {
    home = tmpHome();
  });

  it('appends whisper and reads it from inbox', async () => {
    const deps = { convex: makeConvex(), chain: makeChain() };
    const whisperOut = await runCommand('peer', 'whisper', ['clan-7', 'hello there'], deps, env, home);
    expect(whisperOut.trim()).toBe('whisper sent');

    const inboxOut = await runCommand('peer', 'inbox', [], deps, env, home);
    expect(inboxOut).toContain('from=3');
    expect(inboxOut).toContain('to=clan-7');
    expect(inboxOut).toContain('hello there');
  });

  it('prints inbox empty when no messages', async () => {
    const out = await runCommand('peer', 'inbox', [], { convex: makeConvex(), chain: makeChain() }, env, home);
    expect(out.trim()).toBe('inbox empty');
  });

  it('appends multiple whispers', async () => {
    const deps = { convex: makeConvex(), chain: makeChain() };
    await runCommand('peer', 'whisper', ['clan-1', 'msg one'], deps, env, home);
    await runCommand('peer', 'whisper', ['clan-2', 'msg two'], deps, env, home);
    const out = await runCommand('peer', 'inbox', [], deps, env, home);
    expect(out).toContain('msg one');
    expect(out).toContain('msg two');
  });
});

describe('ack-clear', () => {
  let home: string;
  const env = { ELDER_N: '1' };

  beforeEach(() => {
    home = tmpHome();
  });

  it('creates marker file with timestamp', async () => {
    const deps = { convex: makeConvex(), chain: makeChain() };
    const out = await runCommand('ack-clear', undefined, [], deps, env, home);
    expect(out.trim()).toBe('ack cleared');

    const flagPath = path.join(home, '.world', 'clanworld-runner', 'state', 'elder-1-ack.flag');
    expect(fs.existsSync(flagPath)).toBe(true);
    const content = fs.readFileSync(flagPath, 'utf8').trim();
    expect(new Date(content).getTime()).toBeGreaterThan(0);
  });
});

describe('unknown command', () => {
  it('throws UsageError', async () => {
    await expect(
      runCommand('unknown', 'cmd', [], { convex: makeConvex(), chain: makeChain() }),
    ).rejects.toBeInstanceOf(UsageError);
  });
});
