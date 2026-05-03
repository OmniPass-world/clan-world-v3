import { query } from "./_generated/server";

export const getRecentChainEvents = query({
  handler: async (ctx) => {
    return await ctx.db.query("chainEvents").order("desc").take(60);
  },
});
