import { action, internalMutation, internalQuery } from "./_generated/server";
import { internal } from "./_generated/api";
import { v } from "convex/values";
import { resetLocked } from "./resetLock";

const DEFAULT_DEVNET_RPC = "https://api.devnet.solana.com";
const TOKEN_PROGRAM_ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA";
const MEMO_PROGRAM_ID = "MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr";
const REQUIRED_BURN_AMOUNT = 5;
const MAX_CLANS = 12;
const DOCTRINE_SEPARATOR = "\n---notes---\n";
const GOLD_MINT = process.env.CLAN_WORLD_GOLD_MINT ?? "FPBZJfEgxdkKEeT8pZhLRPg1NzwhyWJRqPKpzRWZLSAF";
const GOLD_TREASURY_ATA = process.env.CLAN_WORLD_GOLD_TREASURY_ATA ?? "4jQN1PfXQ2yfU75K4pMiY4Ue62yXknjKFJ7L9wukSRn8";
const TX_FETCH_ATTEMPTS = 5;
const TX_FETCH_TIMEOUT_MS = 2000;
const TX_FETCH_INITIAL_BACKOFF_MS = 500;

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

function sleep(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function rpcErrorText(error: unknown): string {
  if (!error) return "";
  if (typeof error === "string") return error;
  if (typeof error === "object" && "message" in error && typeof error.message === "string") {
    return error.message;
  }
  return JSON.stringify(error);
}

function isTransactionNotVisibleError(error: unknown): boolean {
  const message = rpcErrorText(error).toLowerCase();
  if (!message) return false;
  return message.includes("not found") ||
    message.includes("not confirmed") ||
    message.includes("not available") ||
    message.includes("could not find");
}

function isTxIndexed(tx: TxResponse): boolean {
  return tx.meta != null || tx.transaction != null;
}

export async function fetchParsedTransaction(signature: string): Promise<TxResponse> {
  let lastRetryableError: Error | undefined;

  for (let attempt = 0; attempt < TX_FETCH_ATTEMPTS; attempt += 1) {
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
      signal: AbortSignal.timeout(TX_FETCH_TIMEOUT_MS),
    }).catch(async (err) => {
      if (err.name !== "TimeoutError" && err.name !== "AbortError") throw err;
      lastRetryableError = new Error("Solana RPC slow or unreachable — please retry");
      return null;
    });

    if (response?.ok === false) {
      throw new Error(`Solana RPC getTransaction failed: ${response.status} ${response.statusText}`);
    }

    if (response) {
      const payload = (await response.json()) as RpcResponse;
      if (payload.error) {
        if (!isTransactionNotVisibleError(payload.error)) {
          throw new Error(`Solana RPC rejected getTransaction: ${JSON.stringify(payload.error)}`);
        }
        lastRetryableError = new Error(`Solana transaction is not visible yet: ${rpcErrorText(payload.error)}`);
      } else if (!payload.result) {
        lastRetryableError = new Error("Solana transaction is not confirmed yet");
      } else {
        const tx = payload.result as TxResponse;
        if (isTxIndexed(tx)) return tx;
        lastRetryableError = new Error("Solana transaction is not indexed yet");
      }
    }

    if (attempt < TX_FETCH_ATTEMPTS - 1) {
      await sleep(TX_FETCH_INITIAL_BACKOFF_MS * (2 ** attempt));
    }
  }

  throw new Error(
    `Solana RPC has not yet indexed this transaction; please retry shortly (${lastRetryableError?.message ?? "not visible"})`,
  );
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

function assertValidClanId(clanId: number) {
  if (!Number.isInteger(clanId) || clanId < 1 || clanId > MAX_CLANS) {
    throw new Error(`Invalid clanId ${clanId}; expected 1-${MAX_CLANS}`);
  }
}

async function verifyClanExists(ctx: any, clanId: number) {
  assertValidClanId(clanId);
  const clan = await ctx.runQuery(internal.gold.getClanOwner, { clanId });
  if (!clan) throw new Error(`Invalid clanId ${clanId} — clan does not exist (post-reset?)`);
}

async function verifyClanExistsInMutation(ctx: any, clanId: number) {
  assertValidClanId(clanId);
  const clan = await ctx.db
    .query("clanView")
    .withIndex("by_clanId", (q: any) => q.eq("clanId", clanId))
    .order("desc")
    .first();
  if (!clan) throw new Error(`Invalid clanId ${clanId} — clan does not exist (post-reset?)`);
}

async function verifyClanOwnership(ctx: any, clanId: number, owner: string) {
  // Reserved for v2: once a Solana-pubkey → EVM-owner binding exists
  // (e.g. SIWS-linked wallet table), call this from the action handlers.
  // In v1, `clanView.owner` is an EVM hex address from the on-chain `getClanFullView`,
  // while `args.owner` is a Solana base58 pubkey, so this comparison is unsafe today.
  const clan = await ctx.runQuery(internal.gold.getClanOwner, { clanId });
  if (!clan) throw new Error(`No clanView record for clanId ${clanId}`);
  if (clan.owner !== owner) throw new Error(`owner ${owner} does not own clan ${clanId}`);
}

export const getClanOwner = internalQuery({
  args: {
    clanId: v.number(),
  },
  handler: async (ctx, args) => {
    const clan = await ctx.db
      .query("clanView")
      .withIndex("by_clanId", (q) => q.eq("clanId", args.clanId))
      .order("desc")
      .first();
    return clan ? { owner: clan.owner } : null;
  },
});

/**
 * SECURITY-DEMO-POSTURE: ClanWorld v1 demo accepts that any wallet with 5 GOLD
 * may write any clan's steering / doctrine. The per-clan ownership boundary moves
 * to v2 once Solana↔EVM wallet binding ships. The Solana burn already binds
 * (clanId, body, owner, burnAmount) into the memo for replay protection; we
 * intentionally do not require owner == clanView.owner because those identifiers
 * live in different address spaces (Solana base58 vs EVM hex).
 */
export const recordWhisperAfterTx = action({
  args: {
    clanId: v.number(),
    body: v.string(),
    owner: v.string(),
    signature: v.string(),
    skipTax: v.number(),
  },
  handler: async (ctx, args) => {
    if (resetLocked()) throw new Error("Game reset in progress — please retry in a few moments");
    await verifyClanExists(ctx, args.clanId);
    const body = args.body.trim();
    const memo = await buildWhisperMemo({ ...args, body });
    const burnAmount = REQUIRED_BURN_AMOUNT;
    await verifyGoldTx({ signature: args.signature, owner: args.owner, burnAmount, skipTax: args.skipTax, memo });
    const result = await ctx.runMutation(internal.gold.commitWhisperAfterTx, {
      clanId: args.clanId,
      body,
      owner: args.owner,
      signature: args.signature,
      burnAmount,
      skipTax: args.skipTax,
      memo,
    });
    return result ?? { ok: true };
  },
});

/**
 * SECURITY-DEMO-POSTURE: ClanWorld v1 demo accepts that any wallet with 5 GOLD
 * may write any clan's steering / doctrine. The per-clan ownership boundary moves
 * to v2 once Solana↔EVM wallet binding ships. The Solana burn already binds
 * (clanId, body, owner, burnAmount) into the memo for replay protection; we
 * intentionally do not require owner == clanView.owner because those identifiers
 * live in different address spaces (Solana base58 vs EVM hex).
 */
export const saveDoctrineAfterTx = action({
  args: {
    clanId: v.number(),
    strategy: v.string(),
    notes: v.string(),
    owner: v.string(),
    signature: v.string(),
  },
  handler: async (ctx, args) => {
    if (resetLocked()) throw new Error("Game reset in progress — please retry in a few moments");
    await verifyClanExists(ctx, args.clanId);
    const memo = await buildDoctrineMemo(args);
    const burnAmount = REQUIRED_BURN_AMOUNT;
    await verifyGoldTx({ signature: args.signature, owner: args.owner, burnAmount, skipTax: 0, memo });
    const result = await ctx.runMutation(internal.gold.commitDoctrineAfterTx, {
      clanId: args.clanId,
      strategy: args.strategy,
      notes: args.notes,
      owner: args.owner,
      signature: args.signature,
      burnAmount,
      memo,
    });
    return result ?? { ok: true };
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
    if (resetLocked()) throw new Error("Game reset in progress — please retry in a few moments");
    await verifyClanExistsInMutation(ctx, args.clanId);
    const snap = await ctx.db.query("worldSnapshot").order("desc").first();
    if (!snap) throw new Error("World snapshot is not ready — burn refunded once reset completes");
    const receipt = await insertReceipt(ctx, { ...args, action: "whisper" as const });
    if (receipt.alreadyRecorded) return { ok: true, alreadyRecorded: true };
    await ctx.db.insert("humanSteeringMessages", {
      tick: snap.tick,
      targetClanId: args.clanId,
      body: args.body,
      sentBy: args.owner,
      txHash: args.signature,
      timestamp: Date.now(),
    });
    return { ok: true };
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
    if (resetLocked()) throw new Error("Game reset in progress — please retry in a few moments");
    await verifyClanExistsInMutation(ctx, args.clanId);
    const snap = await ctx.db.query("worldSnapshot").order("desc").first();
    if (!snap) throw new Error("World snapshot is not ready — burn refunded once reset completes");
    const receipt = await insertReceipt(ctx, { ...args, skipTax: 0, action: "doctrine" as const });
    if (receipt.alreadyRecorded) return { ok: true, alreadyRecorded: true };
    await upsertMemory(ctx, args.clanId, "active-strategy", args.strategy, args.signature);
    await upsertMemory(ctx, args.clanId, "owner_notes", args.notes, args.signature);
    await ctx.db.insert("memoryEvents", {
      tick: snap.tick,
      clanId: args.clanId,
      op: "write",
      key: "doctrine",
      note: `owner doctrine update · ${args.signature.slice(0, 8)}`,
      timestamp: Date.now(),
    });
    return { ok: true };
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
    body?: string;
  },
) {
  const existing = await ctx.db
    .query("goldTxReceipts")
    .withIndex("by_signature", (q: any) => q.eq("signature", args.signature))
    .first();
  if (existing) {
    if (!receiptMatches(existing, args)) {
      throw new Error("GOLD transaction was already used");
    }
    if (await correspondingWriteExists(ctx, args)) {
      return { alreadyRecorded: true };
    }
    return { alreadyRecorded: false };
  }
  await ctx.db.insert("goldTxReceipts", {
    signature: args.signature,
    owner: args.owner,
    clanId: args.clanId,
    action: args.action,
    burnAmount: args.burnAmount,
    skipTax: args.skipTax,
    memo: args.memo,
    observedAt: Date.now(),
  });
  return { alreadyRecorded: false };
}

function receiptMatches(existing: any, args: {
  signature: string;
  owner: string;
  clanId: number;
  action: "whisper" | "doctrine";
  burnAmount: number;
  skipTax: number;
  memo: string;
}) {
  return existing.owner === args.owner &&
    existing.clanId === args.clanId &&
    existing.action === args.action &&
    existing.burnAmount === args.burnAmount &&
    existing.skipTax === args.skipTax &&
    existing.memo === args.memo;
}

async function correspondingWriteExists(
  ctx: any,
  args: {
    signature: string;
    owner: string;
    clanId: number;
    action: "whisper" | "doctrine";
    body?: string;
  },
) {
  if (args.action === "whisper") {
    const message = await ctx.db
      .query("humanSteeringMessages")
      .withIndex("by_target_clan", (q: any) => q.eq("targetClanId", args.clanId))
      .filter((q: any) => q.eq(q.field("txHash"), args.signature))
      .first();
    return Boolean(message);
  }

  const activeStrategy = await ctx.db
    .query("memoryEntries")
    .withIndex("by_clan_key", (q: any) => q.eq("clanId", args.clanId).eq("key", "active-strategy"))
    .first();
  const ownerNotes = await ctx.db
    .query("memoryEntries")
    .withIndex("by_clan_key", (q: any) => q.eq("clanId", args.clanId).eq("key", "owner_notes"))
    .first();
  return activeStrategy?.txHash === args.signature && ownerNotes?.txHash === args.signature;
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
