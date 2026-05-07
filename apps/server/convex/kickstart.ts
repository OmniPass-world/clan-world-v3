import { action, internalAction, internalMutation, internalQuery, query } from "./_generated/server";
import { internal } from "./_generated/api";
import { v } from "convex/values";

const KICKSTART_HOME_URL = "https://kickstart.easya.io/api/home-launches";
const PAGE_SIZE = 48;
const PAGE_COUNT = 3;
const TOP_LIMIT = 100;
const WATCHED_TOKEN_LIMIT = 20;

type KickstartPool = {
  id?: string;
  liquidity?: number;
  volume24h?: number;
  updatedAt?: string;
  baseAsset?: {
    id?: string;
    name?: string;
    symbol?: string;
    icon?: string;
    usdPrice?: number;
    mcap?: number;
    liquidity?: number;
    stats1h?: { priceChange?: number };
    stats6h?: { priceChange?: number };
    stats24h?: { priceChange?: number };
    stats7d?: { priceChange?: number };
  };
};

type KickstartResponse = {
  pools?: KickstartPool[];
};

type JupiterChartResponse = {
  candles?: Array<{
    time?: number;
    close?: number;
  }>;
};

export type KickstartTokenInput = {
  tokenMint: string;
  poolAddress: string;
  name: string;
  symbol: string;
  iconUrl?: string;
  usdPrice: number;
  mcap: number;
  liquidity?: number;
  volume24h?: number;
  priceChange1h?: number;
  priceChange6h?: number;
  priceChange24h?: number;
  priceChange7d?: number;
  rank: number;
  sourceUpdatedAt?: string;
  sparkline24h?: TokenSampleInput[];
};

type TokenSampleInput = {
  price: number;
  observedAt: number;
};

function finiteOrUndefined(value: unknown): number | undefined {
  return typeof value === "number" && Number.isFinite(value) ? value : undefined;
}

function mapPool(pool: KickstartPool): Omit<KickstartTokenInput, "rank"> | null {
  const asset = pool.baseAsset;
  if (!asset?.id || !pool.id || !asset.symbol || !asset.name) return null;
  const usdPrice = finiteOrUndefined(asset.usdPrice);
  const mcap = finiteOrUndefined(asset.mcap);
  if (usdPrice === undefined || mcap === undefined) return null;
  return {
    tokenMint: asset.id,
    poolAddress: pool.id,
    name: asset.name,
    symbol: asset.symbol,
    iconUrl: asset.icon,
    usdPrice,
    mcap,
    liquidity: finiteOrUndefined(asset.liquidity ?? pool.liquidity),
    volume24h: finiteOrUndefined(pool.volume24h),
    priceChange1h: finiteOrUndefined(asset.stats1h?.priceChange),
    priceChange6h: finiteOrUndefined(asset.stats6h?.priceChange),
    priceChange24h: finiteOrUndefined(asset.stats24h?.priceChange),
    priceChange7d: finiteOrUndefined(asset.stats7d?.priceChange),
    sourceUpdatedAt: pool.updatedAt,
  };
}

export function mapKickstartPoolsToTopTokens(pools: KickstartPool[]): KickstartTokenInput[] {
  const byToken = new Map<string, Omit<KickstartTokenInput, "rank">>();
  for (const pool of pools) {
    const token = mapPool(pool);
    if (!token) continue;
    const existing = byToken.get(token.tokenMint);
    if (!existing || token.mcap > existing.mcap) byToken.set(token.tokenMint, token);
  }
  return [...byToken.values()]
    .sort((a, b) => b.mcap - a.mcap)
    .slice(0, TOP_LIMIT)
    .map((token, index) => ({ ...token, rank: index + 1 }));
}

function mapJupiterChartToSamples(payload: JupiterChartResponse): TokenSampleInput[] {
  return (payload.candles ?? [])
    .map((candle) => {
      const timestampSeconds = finiteOrUndefined(candle.time);
      const close = finiteOrUndefined(candle.close);
      if (timestampSeconds === undefined || close === undefined) return null;
      return { observedAt: timestampSeconds * 1000, price: close };
    })
    .filter((sample): sample is TokenSampleInput => sample !== null)
    .sort((a, b) => a.observedAt - b.observedAt);
}

async function fetchKickstartTopTokens(): Promise<KickstartTokenInput[]> {
  const payloads = await Promise.all(
    Array.from({ length: PAGE_COUNT }, async (_, page) => {
      const url = `${KICKSTART_HOME_URL}?page=${page}&pageSize=${PAGE_SIZE}&sort=mcap`;
      const response = await fetch(url, { headers: { accept: "application/json" } });
      if (!response.ok) throw new Error(`Kickstart leaderboard fetch failed: ${response.status}`);
      return (await response.json()) as KickstartResponse;
    }),
  );
  return mapKickstartPoolsToTopTokens(payloads.flatMap((payload) => payload.pools ?? []));
}

async function fetchJupiterChartSamples(tokenMint: string): Promise<TokenSampleInput[]> {
  const to = Date.now();
  const from = to - 24 * 60 * 60 * 1000;
  const params = new URLSearchParams({
    interval: "15_MINUTE",
    baseAsset: tokenMint,
    from: String(from),
    to: String(to),
    candles: "96",
    type: "price",
  });
  const headers: Record<string, string> = { accept: "application/json" };
  if (process.env.JUPITER_API_KEY) headers["x-api-key"] = process.env.JUPITER_API_KEY;
  const response = await fetch(`https://datapi.jup.ag/v2/charts/${tokenMint}?${params}`, { headers });
  if (!response.ok) return [];
  return mapJupiterChartToSamples((await response.json()) as JupiterChartResponse);
}

export const listKickstartTokens = query({
  args: {},
  handler: async (ctx) => {
    return await ctx.db.query("kickstartTokens").withIndex("by_rank").order("asc").take(TOP_LIMIT);
  },
});

export const getKickstartTokenQuote = query({
  args: { tokenMint: v.string() },
  handler: async (ctx, { tokenMint }) => {
    const token = await ctx.db
      .query("kickstartTokens")
      .withIndex("by_token", (q) => q.eq("tokenMint", tokenMint))
      .first();
    if (!token) return null;
    return {
      ...token,
      sparkline24h: token.sparkline24h ?? [],
    };
  },
});

export const watchAndRefreshToken = action({
  args: { tokenMint: v.string() },
  handler: async (ctx, { tokenMint }) => {
    await ctx.runMutation(internal.kickstart.markWatchedToken, { tokenMint });
    const token = await ctx.runQuery(internal.kickstart.getTokenByMint, { tokenMint });
    let hasToken = token !== null;
    if (!token) {
      const tokens = await fetchKickstartTopTokens();
      await ctx.runMutation(internal.kickstart.replaceKickstartTokens, { tokens });
      hasToken = tokens.some((entry) => entry.tokenMint === tokenMint);
    }
    if (!hasToken) return { ok: false, reason: "token-not-found" };
    const samples = await fetchJupiterChartSamples(tokenMint);
    await ctx.runMutation(internal.kickstart.updateTokenSparkline, { tokenMint, samples });
    return { ok: true, samples: samples.length };
  },
});

export const refreshKickstartLeaderboard = internalAction({
  args: {},
  handler: async (ctx) => {
    const tokens = await fetchKickstartTopTokens();
    await ctx.runMutation(internal.kickstart.replaceKickstartTokens, { tokens });
    return { tokens: tokens.length };
  },
});

export const refreshWatchedTokenCandles = internalAction({
  args: {},
  handler: async (ctx) => {
    const watched = await ctx.runQuery(internal.kickstart.listWatchedTokens, {});
    const refreshed: string[] = [];
    for (const row of watched.slice(0, WATCHED_TOKEN_LIMIT)) {
      const token = await ctx.runQuery(internal.kickstart.getTokenByMint, { tokenMint: row.tokenMint });
      if (!token) continue;
      const samples = await fetchJupiterChartSamples(token.tokenMint);
      await ctx.runMutation(internal.kickstart.updateTokenSparkline, { tokenMint: token.tokenMint, samples });
      refreshed.push(token.tokenMint);
    }
    return { refreshed };
  },
});

export const getTokenByMint = internalQuery({
  args: { tokenMint: v.string() },
  handler: async (ctx, { tokenMint }) => {
    return await ctx.db
      .query("kickstartTokens")
      .withIndex("by_token", (q) => q.eq("tokenMint", tokenMint))
      .first();
  },
});

export const listWatchedTokens = internalQuery({
  args: {},
  handler: async (ctx) => {
    return await ctx.db.query("kickstartWatchedTokens").order("desc").take(WATCHED_TOKEN_LIMIT);
  },
});

export const replaceKickstartTokens = internalMutation({
  args: { tokens: v.array(v.any()) },
  handler: async (ctx, { tokens }) => {
    const fetchedAt = Date.now();
    const existing = await ctx.db.query("kickstartTokens").take(200);
    const existingByMint = new Map(existing.map((row) => [row.tokenMint, row]));
    await Promise.all(existing.map((row) => ctx.db.delete(row._id)));
    for (const token of tokens as KickstartTokenInput[]) {
      const previous = existingByMint.get(token.tokenMint);
      await ctx.db.insert("kickstartTokens", {
        ...token,
        sparkline24h: previous?.sparkline24h ?? token.sparkline24h,
        fetchedAt,
      });
    }
  },
});

export const markWatchedToken = internalMutation({
  args: { tokenMint: v.string() },
  handler: async (ctx, { tokenMint }) => {
    const existing = await ctx.db
      .query("kickstartWatchedTokens")
      .withIndex("by_token", (q) => q.eq("tokenMint", tokenMint))
      .first();
    if (existing) await ctx.db.patch(existing._id, { watchedAt: Date.now() });
    else await ctx.db.insert("kickstartWatchedTokens", { tokenMint, watchedAt: Date.now() });
  },
});

export const updateTokenSparkline = internalMutation({
  args: {
    tokenMint: v.string(),
    samples: v.array(v.object({ price: v.number(), observedAt: v.number() })),
  },
  handler: async (ctx, { tokenMint, samples }) => {
    if (samples.length < 2) return;
    const token = await ctx.db
      .query("kickstartTokens")
      .withIndex("by_token", (q) => q.eq("tokenMint", tokenMint))
      .first();
    if (!token) return;
    await ctx.db.patch(token._id, { sparkline24h: samples.slice(-96) });
  },
});
