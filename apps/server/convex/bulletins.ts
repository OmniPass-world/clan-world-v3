import { query, mutation } from "./_generated/server";
import { v } from "convex/values";

/**
 * Bulletins query layer. The bulletins table itself was added in an earlier
 * milestone (0G KV storage proofs); this file adds the cockpit-side queries
 * + a seed mutation for local testing.
 *
 * Slot semantics: each clan gets a bounded ring of bulletin slots. `slot`
 * monotonically increments with each post; `updatedAt` is the wall-clock
 * timestamp the post was last touched. Visibility (visible vs. old/hidden)
 * is computed client-side from the current tick + a TTL window.
 */

export const getByClan = query({
  args: {
    clanId: v.number(),
    limit: v.optional(v.number()),
  },
  handler: async (ctx, { clanId, limit }) => {
    return await ctx.db
      .query("bulletins")
      .withIndex("by_clan_slot", q => q.eq("clanId", clanId))
      .order("desc")
      .take(limit ?? 20);
  },
});

export const getAll = query({
  args: {
    limit: v.optional(v.number()),
  },
  handler: async (ctx, { limit }) => {
    // For the cross-clan flyout: every clan's bulletins, newest-slot first.
    return await ctx.db.query("bulletins").order("desc").take(limit ?? 60);
  },
});

export const seedBulletin = mutation({
  args: {
    clanId: v.number(),
    slot: v.number(),
    body: v.string(),
  },
  handler: async (ctx, args) => {
    await ctx.db.insert("bulletins", {
      ...args,
      updatedAt: Date.now(),
    });
  },
});
