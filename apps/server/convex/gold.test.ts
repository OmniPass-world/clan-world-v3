import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import {
  buildDoctrineMemo,
  buildWhisperMemo,
  fetchParsedTransaction,
  hasBurn,
  hasSkipTaxTransfer,
  sha256Hex,
} from "./gold";
import { resetLocked } from "./resetLock";

describe("GOLD tx verification helpers", () => {
  const originalResetLock = process.env.CLANWORLD_RESET_LOCK;

  afterEach(() => {
    if (originalResetLock === undefined) {
      delete process.env.CLANWORLD_RESET_LOCK;
    } else {
      process.env.CLANWORLD_RESET_LOCK = originalResetLock;
    }
  });

  it("honors CLANWORLD_RESET_LOCK only when explicitly true", () => {
    delete process.env.CLANWORLD_RESET_LOCK;
    expect(resetLocked()).toBe(false);

    process.env.CLANWORLD_RESET_LOCK = "false";
    expect(resetLocked()).toBe(false);

    process.env.CLANWORLD_RESET_LOCK = "true";
    expect(resetLocked()).toBe(true);
  });

  it("derives canonical whisper memos from trimmed body and required burn", async () => {
    expect(await buildWhisperMemo({
      clanId: 3,
      body: "  press east  ",
      owner: "owner111",
      skipTax: 20,
    })).toBe(`clanworld:whisper:v1:3:${await sha256Hex("press east")}:owner111:5:20`);
  });

  it("derives canonical doctrine memos from strategy and notes", async () => {
    expect(await buildDoctrineMemo({
      clanId: 7,
      strategy: "hold the gate",
      notes: "watch the river",
      owner: "owner222",
    })).toBe(`clanworld:doctrine:v1:7:${await sha256Hex("hold the gate\n---notes---\nwatch the river")}:owner222:5:0`);
  });

  it("matches GOLD burn amounts as raw token amount strings", () => {
    const tx = parsedTx([{
      programId: "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA",
      parsed: {
        type: "burnChecked",
        info: {
          mint: "FPBZJfEgxdkKEeT8pZhLRPg1NzwhyWJRqPKpzRWZLSAF",
          authority: "owner333",
          tokenAmount: { amount: "5", decimals: 0 },
        },
      },
    }]);

    expect(hasBurn(tx, "owner333", 5)).toBe(true);
    tx.transaction.message.instructions[0].parsed.info.tokenAmount.amount = "0005";
    expect(hasBurn(tx, "owner333", 5)).toBe(false);
  });

  it("requires skip tax transfers to land in the treasury token account", () => {
    const tx = parsedTx([{
      programId: "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA",
      parsed: {
        type: "transferChecked",
        info: {
          mint: "FPBZJfEgxdkKEeT8pZhLRPg1NzwhyWJRqPKpzRWZLSAF",
          authority: "owner444",
          destination: "4jQN1PfXQ2yfU75K4pMiY4Ue62yXknjKFJ7L9wukSRn8",
          destinationOwner: "not-checked-here",
          tokenAmount: { amount: "100", decimals: 0 },
        },
      },
    }]);

    expect(hasSkipTaxTransfer(tx, "owner444", 100)).toBe(true);
    expect(hasSkipTaxTransfer(tx, "owner444", 101)).toBe(false);
  });
});

describe("fetchParsedTransaction", () => {
  const originalFetch = globalThis.fetch;

  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.restoreAllMocks();
    globalThis.fetch = originalFetch;
  });

  it("retries until Solana RPC has indexed a parsed transaction", async () => {
    const tx = parsedTx([]);
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(rpcOk(null))
      .mockResolvedValueOnce(rpcOk({ meta: null, transaction: null }))
      .mockResolvedValueOnce(rpcOk(tx));
    globalThis.fetch = fetchMock as any;

    const promise = fetchParsedTransaction("sig111");
    await vi.runAllTimersAsync();

    await expect(promise).resolves.toBe(tx);
    expect(fetchMock).toHaveBeenCalledTimes(3);
  });
});

function parsedTx(instructions: any[]) {
  return { transaction: { message: { instructions } } };
}

function rpcOk(result: unknown) {
  return {
    ok: true,
    json: async () => ({ result }),
  };
}
