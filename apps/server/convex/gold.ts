import { action, internalMutation } from "./_generated/server";
import { internal } from "./_generated/api";
import { v } from "convex/values";

const DEFAULT_DEVNET_RPC = "https://api.devnet.solana.com";
const TOKEN_PROGRAM_ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA";
const MEMO_PROGRAM_ID = "MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr";
const REQUIRED_BURN_AMOUNT = 5;
const DOCTRINE_SEPARATOR = "\n---notes---\n";
const GOLD_MINT = process.env.CLAN_WORLD_GOLD_MINT ?? "FPBZJfEgxdkKEeT8pZhLRPg1NzwhyWJRqPKpzRWZLSAF";
const GOLD_TREASURY_ATA = process.env.CLAN_WORLD_GOLD_TREASURY_ATA ?? "4jQN1PfXQ2yfU75K4pMiY4Ue62yXknjKFJ7L9wukSRn8";

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

type CryptoLike = {
  subtle: {
    digest(algorithm: string, data: Uint8Array): Promise<ArrayBuffer>;
  };
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

export async function sha256Hex(text: string): Promise<string> {
  const bytes = new TextEncoder().encode(text);
  const digest = await (globalThis.crypto as CryptoLike).subtle.digest("SHA-256", bytes);
  return Array.from(new Uint8Array(digest))
    .map((byte) => byte.toString(16).padStart(2, "0"))
    .join("");
}

export async function buildWhisperMemo(args: {
  clanId: number;
  body: string;
  owner: string;
  skipTax: number;
}): Promise<string> {
  const body = args.body.trim();
  return `clanworld:whisper:v1:${args.clanId}:${await sha256Hex(body)}:${args.owner}:${REQUIRED_BURN_AMOUNT}:${args.skipTax}`;
}

export async function buildDoctrineMemo(args: {
  clanId: number;
  strategy: string;
  notes: string;
  owner: string;
}): Promise<string> {
  const payloadHash = await sha256Hex(`${args.strategy}${DOCTRINE_SEPARATOR}${args.notes}`);
  return `clanworld:doctrine:v1:${args.clanId}:${payloadHash}:${args.owner}:${REQUIRED_BURN_AMOUNT}:0`;
}

export function hasMemo(tx: TxResponse, memo: string): boolean {
  return instructions(tx).some((ix) => {
    if (ix.programId !== MEMO_PROGRAM_ID && ix.program !== "spl-memo") return false;
    return ix.parsed === memo;
  });
}

export function hasBurn(tx: TxResponse, owner: string, amount: number): boolean {
  const expectedAmount = String(amount);
  return instructions(tx).some((ix) => {
    if (ix.programId !== TOKEN_PROGRAM_ID || typeof ix.parsed !== "object") return false;
    if (ix.parsed.type !== "burnChecked") return false;
    const info = ix.parsed.info ?? {};
    return info.mint === GOLD_MINT &&
      info.authority === owner &&
      info.tokenAmount?.amount === expectedAmount &&
      Number(info.tokenAmount?.decimals) === 0;
  });
}

export function hasSkipTaxTransfer(tx: TxResponse, owner: string, amount: number): boolean {
  if (amount === 0) return true;
  const expectedAmount = String(amount);
  return instructions(tx).some((ix) => {
    if (ix.programId !== TOKEN_PROGRAM_ID || typeof ix.parsed !== "object") return false;
    if (ix.parsed.type !== "transferChecked") return false;
    const info = ix.parsed.info ?? {};
    return info.mint === GOLD_MINT &&
      info.authority === owner &&
      info.destination === GOLD_TREASURY_ATA &&
      info.tokenAmount?.amount === expectedAmount &&
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
    skipTax: v.number(),
  },
  handler: async (ctx, args) => {
    const body = args.body.trim();
    const memo = await buildWhisperMemo({ ...args, body });
    const burnAmount = REQUIRED_BURN_AMOUNT;
    await verifyGoldTx({ signature: args.signature, owner: args.owner, burnAmount, skipTax: args.skipTax, memo });
    await ctx.runMutation(internal.gold.commitWhisperAfterTx, { ...args, body, burnAmount, memo });
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
  },
  handler: async (ctx, args) => {
    const memo = await buildDoctrineMemo(args);
    const burnAmount = REQUIRED_BURN_AMOUNT;
    await verifyGoldTx({ ...args, burnAmount, memo, skipTax: 0 });
    await ctx.runMutation(internal.gold.commitDoctrineAfterTx, { ...args, burnAmount, memo });
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
