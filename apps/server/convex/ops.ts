import { mutation } from "./_generated/server";
import { v } from "convex/values";
import { requireIndexerSecret } from "./authShared";

const RESET_TABLES = [
  "worldSnapshot",
  "chainEvents",
  "tickHistory",
  "eventCheckpoint",
  "clanView",
  "marketState",
  "banditView",
  "pricePoint",
  "goldQuote",
  "goldQuoteSample",
  "kickstartTokens",
  "kickstartWatchedTokens",
  "agentLogs",
  "inftTokens",
  "inftTransfers",
  "memoryEntries",
  "bulletins",
  "memoryEvents",
  "whispers",
  "orchEvents",
  "humanSteeringMessages",
] as const;

export const flushGameState = mutation({
  args: {
    secret: v.string(),
    batchSize: v.optional(v.number()),
  },
  handler: async (ctx, args) => {
    requireIndexerSecret(args.secret);

    const batchSize = Math.max(1, Math.min(args.batchSize ?? 100, 500));
    const deletedByTable: Record<string, number> = {};
    let totalDeleted = 0;

    for (const table of RESET_TABLES) {
      const rows = await ctx.db.query(table).take(batchSize);
      deletedByTable[table] = rows.length;
      totalDeleted += rows.length;
      for (const row of rows) {
        await ctx.db.delete(row._id);
      }
    }

    return {
      deletedByTable,
      totalDeleted,
      batchSize,
      complete: totalDeleted === 0,
    };
  },
});

export const flushClanViewForClan = mutation({
  args: {
    secret: v.string(),
    clanId: v.number(),
    batchSize: v.optional(v.number()),
  },
  handler: async (ctx, args) => {
    requireIndexerSecret(args.secret);

    const batchSize = Math.max(1, Math.min(args.batchSize ?? 100, 500));
    const rows = await ctx.db
      .query("clanView")
      .withIndex("by_clanId", (q) => q.eq("clanId", args.clanId))
      .take(batchSize);

    for (const row of rows) {
      await ctx.db.delete(row._id);
    }

    return {
      clanId: args.clanId,
      deleted: rows.length,
      batchSize,
      complete: rows.length === 0,
    };
  },
});

export const resetCheckpoint = mutation({
  args: {
    secret: v.string(),
    lastBlock: v.number(),
  },
  handler: async (ctx, args) => {
    requireIndexerSecret(args.secret);

    const existing = await ctx.db.query("eventCheckpoint").first();
    if (existing) {
      await ctx.db.delete(existing._id);
    }

    await ctx.db.insert("eventCheckpoint", {
      lastBlock: args.lastBlock,
      lastSeenAt: Date.now(),
    });

    return { lastBlock: args.lastBlock };
  },
});
