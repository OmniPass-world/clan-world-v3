import { describe, expect, it } from "vitest";
import { mapKickstartPoolsToTopTokens } from "./kickstart";

describe("mapKickstartPoolsToTopTokens", () => {
  it("dedupes pools by token and sorts by market cap", () => {
    const tokens = mapKickstartPoolsToTopTokens([
      {
        id: "pool-low",
        volume24h: 20,
        baseAsset: {
          id: "mint-a",
          name: "Alpha",
          symbol: "ALP",
          usdPrice: 0.1,
          mcap: 100,
          stats24h: { priceChange: 2 },
        },
      },
      {
        id: "pool-b",
        volume24h: 10,
        baseAsset: {
          id: "mint-b",
          name: "Beta",
          symbol: "BET",
          usdPrice: 0.2,
          mcap: 300,
        },
      },
      {
        id: "pool-high",
        volume24h: 40,
        baseAsset: {
          id: "mint-a",
          name: "Alpha",
          symbol: "ALP",
          usdPrice: 0.12,
          mcap: 200,
        },
      },
    ]);

    expect(tokens.map((token) => `${token.rank}:${token.symbol}:${token.poolAddress}:${token.mcap}`)).toEqual([
      "1:BET:pool-b:300",
      "2:ALP:pool-high:200",
    ]);
  });
});
