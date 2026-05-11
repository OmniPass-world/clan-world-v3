import { action, internalMutation } from "./_generated/server";
import { internal } from "./_generated/api";
import { v } from "convex/values";

const DEFAULT_DEVNET_RPC = "https://api.devnet.solana.com";
const TOKEN_PROGRAM_ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA";
const MEMO_PROGRAM_ID = "MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr";
const GOLD_MINT = process.env.CLAN_WORLD_GOLD_MINT ?? "11111111111111111111111111111111";
const GOLD_TREASURY_OWNER = process.env.CLAN_WORLD_GOLD_TREASURY_OWNER ?? "11111111111111111111111111111111";

type ParsedInstruction = {
  program?: string;
  programId?: string;
  parsed?: string | { type?: string; info?: Record<string, any> };
};

type TxResponse = {
  meta?: { err?: unknown };
  transaction?: {
    message?: {
      accountKeys?: Array<string | { pubkey?: string; signer?: boolean }>;
      instructions?: ParsedInstruction[];
    };
  };
};

type RpcResponse = {
  error?: unknown;
  result?: unknown;
};

function rpcUrl() {
  return process.env.CLAN_WORLD_GOLD_RPC_URL ?? DEFAULT_DEVNET_RPC;
}

async function fetchParsedTransaction(signature: string): Promise<TxResponse> {
  const response = await fetch(rpcUrl(), {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({
      jsonrpc: "2.0",
      id: "clanworld",
      method: "getTransaction",
      params: [
        signature,
        {
          commitment: "confirmed",
          encoding: "jsonParsed",
          maxSupportedTransactionVersion: 0,
        },
      ],
    }),
  });
  if (!response.ok) {
    throw new Error(`Solana RPC getTransaction failed: ${response.status} ${response.statusText}`);
  }
  const payload = (await response.json()) as RpcResponse;
  if (payload.error) throw new Error(`Solana RPC rejected getTransaction: ${JSON.stringify(payload.error)}`);
  if (!payload.result) throw new Error("Solana transaction is not confirmed yet");
  return payload.result as TxResponse;
}

function signerMatches(tx: TxResponse, owner: string): boolean {
  const keys = tx.transaction?.message?.accountKeys ?? [];
  return keys.some((key) => {
    if (typeof key === "string") return key === owner;
    return key.pubkey === owner && key.signer === true;
  });
}

function instructions(tx: TxResponse): ParsedInstruction[] {
  return tx.transaction?.message?.instructions ?? [];
}

function hasMemo(tx: TxResponse, memo: string): boolean {
  return instructions(tx).some((ix) => {
    if (ix.programId !== MEMO_PROGRAM_ID && ix.program !== "spl-memo") return false;
    return ix.parsed === memo;
  });
}

function hasBurn(tx: TxResponse, owner: string, amount: number): boolean {
  return instructions(tx).some((ix) => {
    if (ix.programId !== TOKEN_PROGRAM_ID || typeof ix.parsed !== "object") return false;
    if (ix.parsed.type !== "burnChecked") return false;
    const info = ix.parsed.info ?? {};
    return info.mint === GOLD_MINT &&
      info.authority === owner &&
      Number(info.tokenAmount?.amount) === amount &&
      Number(info.tokenAmount?.decimals) === 0;
  });
}

function hasSkipTaxTransfer(tx: TxResponse, owner: string, amount: number): boolean {
  if (amount === 0) return true;
  return instructions(tx).some((ix) => {
    if (ix.programId !== TOKEN_PROGRAM_ID || typeof ix.parsed !== "object") return false;
    if (ix.parsed.type !== "transferChecked") return false;
    const info = ix.parsed.info ?? {};
    return info.mint === GOLD_MINT &&
      info.authority === owner &&
      info.destinationOwner === GOLD_TREASURY_OWNER &&
      Number(info.tokenAmount?.amount) === amount &&
      Number(info.tokenAmount?.decimals) === 0;
  });
}

async function verifyGoldTx(args: {
  signature: string;
  owner: string;
  burnAmount: number;
  skipTax: number;
  memo: string;
}) {
  const tx = await fetchParsedTransaction(args.signature);
  if (tx.meta?.err) throw new Error(`Solana transaction failed: ${JSON.stringify(tx.meta.err)}`);
  if (!signerMatches(tx, args.owner)) throw new Error("Solana tx was not signed by the connected owner");
  if (!hasMemo(tx, args.memo)) throw new Error("Solana tx memo does not match the requested game write");
  if (!hasBurn(tx, args.owner, args.burnAmount)) throw new Error("Solana tx did not burn the required GOLD amount");
  if (!hasSkipTaxTransfer(tx, args.owner, args.skipTax)) throw new Error("Solana tx did not pay the required cooldown skip tax");
}

export const recordWhisperAfterTx = action({
  args: {
    clanId: v.number(),
    body: v.string(),
    owner: v.string(),
    signature: v.string(),
    burnAmount: v.number(),
    skipTax: v.number(),
    memo: v.string(),
  },
  handler: async (ctx, args) => {
    await verifyGoldTx(args);
    await ctx.runMutation(internal.gold.commitWhisperAfterTx, args);
    return { ok: true };
  },
});

export const saveDoctrineAfterTx = action({
  args: {
    clanId: v.number(),
    strategy: v.string(),
    notes: v.string(),
    owner: v.string(),
    signature: v.string(),
    burnAmount: v.number(),
    memo: v.string(),
  },
  handler: async (ctx, args) => {
    await verifyGoldTx({ ...args, skipTax: 0 });
    await ctx.runMutation(internal.gold.commitDoctrineAfterTx, args);
    return { ok: true };
  },
});

export const commitWhisperAfterTx = internalMutation({
  args: {
    clanId: v.number(),
    body: v.string(),
    owner: v.string(),
    signature: v.string(),
    burnAmount: v.number(),
    skipTax: v.number(),
    memo: v.string(),
  },
  handler: async (ctx, args) => {
    await insertReceipt(ctx, { ...args, action: "whisper" as const });
    const snap = await ctx.db.query("worldSnapshot").order("desc").first();
    await ctx.db.insert("humanSteeringMessages", {
      tick: snap?.tick ?? 0,
      targetClanId: args.clanId,
      body: args.body,
      sentBy: args.owner,
      timestamp: Date.now(),
    });
  },
});

export const commitDoctrineAfterTx = internalMutation({
  args: {
    clanId: v.number(),
    strategy: v.string(),
    notes: v.string(),
    owner: v.string(),
    signature: v.string(),
    burnAmount: v.number(),
    memo: v.string(),
  },
  handler: async (ctx, args) => {
    await insertReceipt(ctx, { ...args, skipTax: 0, action: "doctrine" as const });
    await upsertMemory(ctx, args.clanId, "active-strategy", args.strategy, args.signature);
    await upsertMemory(ctx, args.clanId, "owner_notes", args.notes, args.signature);
    const snap = await ctx.db.query("worldSnapshot").order("desc").first();
    await ctx.db.insert("memoryEvents", {
      tick: snap?.tick ?? 0,
      clanId: args.clanId,
      op: "write",
      key: "doctrine",
      note: `owner doctrine update · ${args.signature.slice(0, 8)}`,
      timestamp: Date.now(),
    });
  },
});

async function insertReceipt(
  ctx: any,
  args: {
    signature: string;
    owner: string;
    clanId: number;
    action: "whisper" | "doctrine";
    burnAmount: number;
    skipTax: number;
    memo: string;
  },
) {
  const existing = await ctx.db
    .query("goldTxReceipts")
    .withIndex("by_signature", (q: any) => q.eq("signature", args.signature))
    .first();
  if (existing) throw new Error("GOLD transaction was already used");
  await ctx.db.insert("goldTxReceipts", { ...args, observedAt: Date.now() });
}

async function upsertMemory(
  ctx: any,
  clanId: number,
  key: string,
  value: string,
  signature: string,
) {
  const existing = await ctx.db
    .query("memoryEntries")
    .withIndex("by_clan_key", (q: any) => q.eq("clanId", clanId).eq("key", key))
    .first();
  const stamped = {
    clanId,
    key,
    value,
    source: "local" as const,
    dataHash: signature,
    txHash: signature,
    updatedAt: Date.now(),
  };
  if (existing) {
    await ctx.db.patch(existing._id, stamped);
  } else {
    await ctx.db.insert("memoryEntries", stamped);
  }
}
