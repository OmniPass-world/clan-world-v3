import { internalAction, internalMutation, query } from "./_generated/server";
import { internal } from "./_generated/api";
import { v } from "convex/values";

const GOLD_TOKEN_MINT = "4kWysUHVqtFmxrvwPUxA66exm2iJBMkvD4EBRrNmcieL";
const JUPITER_ASSET_SEARCH_URL =
  "https://datapi.jup.ag/v1/assets/search?query=4kWysUHVqtFmxrvwPUxA66exm2iJBMkvD4EBRrNmcieL";
const GECKO_GOLD_POOL_ADDRESS = "52FmihUahL1T2e1716weZ4SdBVyrSg915NmRPD5m9jfF";
const GECKO_GOLD_OHLCV_URL =
  `https://api.geckoterminal.com/api/v2/networks/solana/pools/${GECKO_GOLD_POOL_ADDRESS}/ohlcv/minute?aggregate=15&limit=96&currency=usd&token=base`;

type JupiterAsset = {
  id?: string;
  name?: string;
  symbol?: string;
  icon?: string;
  usdPrice?: number;
  stats1h?: { priceChange?: number };
  stats6h?: { priceChange?: number };
  stats24h?: { priceChange?: number };
  stats7d?: { priceChange?: number };
  updatedAt?: string;
};

type GeckoOhlcvResponse = {
  data?: {
    attributes?: {
      ohlcv_list?: unknown[][];
    };
  };
};

export type GoldQuoteInput = {
  tokenMint: string;
  symbol: string;
  name: string;
  usdPrice: number;
  priceChange1h?: number;
  priceChange6h?: number;
  priceChange24h?: number;
  priceChange7d?: number;
  iconUrl?: string;
  sourceUpdatedAt?: string;
};

type GoldQuoteSampleInput = {
  price: number;
  observedAt: number;
};

function finiteOrUndefined(value: unknown): number | undefined {
  return typeof value === "number" && Number.isFinite(value) ? value : undefined;
}

function mapGeckoOhlcvToSamples(payload: GeckoOhlcvResponse): GoldQuoteSampleInput[] {
  const rows = payload.data?.attributes?.ohlcv_list ?? [];
  return rows
    .map((row) => {
      const timestampSeconds = finiteOrUndefined(row[0]);
      const close = finiteOrUndefined(row[4]);
      if (timestampSeconds === undefined || close === undefined) return null;
      return { observedAt: timestampSeconds * 1000, price: close };
    })
    .filter((sample): sample is GoldQuoteSampleInput => sample !== null)
    .sort((a, b) => a.observedAt - b.observedAt);
}

export function mapJupiterAssetToGoldQuote(asset: JupiterAsset): GoldQuoteInput {
  if (asset.id !== GOLD_TOKEN_MINT) {
    throw new Error("Jupiter response did not include the configured GOLD token");
  }
  if (typeof asset.usdPrice !== "number" || !Number.isFinite(asset.usdPrice)) {
    throw new Error("Jupiter response did not include a valid usdPrice");
  }
  return {
    tokenMint: GOLD_TOKEN_MINT,
    symbol: asset.symbol || "GOLD",
    name: asset.name || "Clan World: Aelder Whispers",
    usdPrice: asset.usdPrice,
    priceChange1h: finiteOrUndefined(asset.stats1h?.priceChange),
    priceChange6h: finiteOrUndefined(asset.stats6h?.priceChange),
    priceChange24h: finiteOrUndefined(asset.stats24h?.priceChange),
    priceChange7d: finiteOrUndefined(asset.stats7d?.priceChange),
    iconUrl: asset.icon,
    sourceUpdatedAt: asset.updatedAt,
  };
}

export const getGoldQuote = query({
  args: {},
  handler: async (ctx) => {
    const quote = await ctx.db
      .query("goldQuote")
      .withIndex("by_token", (q) => q.eq("tokenMint", GOLD_TOKEN_MINT))
      .first();
    if (!quote) return null;

    const samples = await ctx.db
      .query("goldQuoteSample")
      .withIndex("by_token_observed", (q) => q.eq("tokenMint", GOLD_TOKEN_MINT))
      .order("desc")
      .take(96);

    return {
      tokenMint: quote.tokenMint,
      symbol: quote.symbol,
      name: quote.name,
      usdPrice: quote.usdPrice,
      priceChange1h: quote.priceChange1h ?? null,
      priceChange6h: quote.priceChange6h ?? null,
      priceChange24h: quote.priceChange24h ?? null,
      priceChange7d: quote.priceChange7d ?? null,
      iconUrl: quote.iconUrl ?? null,
      sourceUpdatedAt: quote.sourceUpdatedAt ?? null,
      fetchedAt: quote.fetchedAt,
      sparkline24h: samples
        .reverse()
        .map((sample) => ({ price: sample.usdPrice, observedAt: sample.observedAt })),
    };
  },
});

export const refreshGoldQuote = internalAction({
  args: {},
  handler: async (ctx) => {
    const headers: Record<string, string> = { accept: "application/json" };
    if (process.env.JUPITER_API_KEY) {
      headers["x-api-key"] = process.env.JUPITER_API_KEY;
    }

    const response = await fetch(JUPITER_ASSET_SEARCH_URL, { headers });
    if (!response.ok) {
      throw new Error(`Jupiter GOLD quote fetch failed: ${response.status} ${response.statusText}`);
    }

    const payload = (await response.json()) as JupiterAsset[];
    const asset = payload.find((entry) => entry.id === GOLD_TOKEN_MINT);
    if (!asset) {
      throw new Error("Jupiter response did not contain GOLD token data");
    }

    const quote = mapJupiterAssetToGoldQuote(asset);
    const ohlcvResponse = await fetch(GECKO_GOLD_OHLCV_URL, {
      headers: { accept: "application/json" },
    });
    const sparkline24h = ohlcvResponse.ok
      ? mapGeckoOhlcvToSamples((await ohlcvResponse.json()) as GeckoOhlcvResponse)
      : [];

    await ctx.runMutation(internal.goldQuote.upsertGoldQuote, { quote, sparkline24h });
    return { ...quote, sparklinePoints: sparkline24h.length };
  },
});

export const upsertGoldQuote = internalMutation({
  args: {
    quote: v.object({
      tokenMint: v.string(),
      symbol: v.string(),
      name: v.string(),
      usdPrice: v.number(),
      priceChange1h: v.optional(v.number()),
      priceChange6h: v.optional(v.number()),
      priceChange24h: v.optional(v.number()),
      priceChange7d: v.optional(v.number()),
      iconUrl: v.optional(v.string()),
      sourceUpdatedAt: v.optional(v.string()),
    }),
    sparkline24h: v.optional(
      v.array(
        v.object({
          price: v.number(),
          observedAt: v.number(),
        }),
      ),
    ),
  },
  handler: async (ctx, { quote, sparkline24h }) => {
    const fetchedAt = Date.now();
    const existing = await ctx.db
      .query("goldQuote")
      .withIndex("by_token", (q) => q.eq("tokenMint", quote.tokenMint))
      .first();

    if (existing) {
      await ctx.db.patch(existing._id, { ...quote, fetchedAt });
    } else {
      await ctx.db.insert("goldQuote", { ...quote, fetchedAt });
    }

    if (sparkline24h && sparkline24h.length > 1) {
      const existingSamples = await ctx.db
        .query("goldQuoteSample")
        .withIndex("by_token_observed", (q) => q.eq("tokenMint", quote.tokenMint))
        .take(120);
      await Promise.all(existingSamples.map((sample) => ctx.db.delete(sample._id)));
      await Promise.all(
        sparkline24h.map((sample) =>
          ctx.db.insert("goldQuoteSample", {
            tokenMint: quote.tokenMint,
            usdPrice: sample.price,
            observedAt: sample.observedAt,
          }),
        ),
      );
      return;
    }

    await ctx.db.insert("goldQuoteSample", {
      tokenMint: quote.tokenMint,
      usdPrice: quote.usdPrice,
      observedAt: fetchedAt,
    });

    const staleBefore = fetchedAt - 25 * 60 * 60 * 1000;
    const staleSamples = await ctx.db
      .query("goldQuoteSample")
      .withIndex("by_token_observed", (q) => q.eq("tokenMint", quote.tokenMint).lt("observedAt", staleBefore))
      .take(50);
    await Promise.all(staleSamples.map((sample) => ctx.db.delete(sample._id)));
  },
});
