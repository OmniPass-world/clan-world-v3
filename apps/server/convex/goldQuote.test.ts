import { describe, expect, it } from "vitest";
import { mapJupiterAssetToGoldQuote } from "./goldQuote";

describe("mapJupiterAssetToGoldQuote", () => {
  it("maps the Jupiter GOLD asset payload into the widget quote shape", () => {
    const quote = mapJupiterAssetToGoldQuote({
      id: "4kWysUHVqtFmxrvwPUxA66exm2iJBMkvD4EBRrNmcieL",
      name: "Clan World: Aelder Whispers",
      symbol: "GOLD",
      icon: "https://example.com/gold.png",
      usdPrice: 0.000026,
      stats1h: { priceChange: 1 },
      stats6h: { priceChange: -2 },
      stats24h: { priceChange: 3 },
      stats7d: { priceChange: 4 },
      updatedAt: "2026-05-07T00:20:26.992157884Z",
    });

    expect(quote).toEqual({
      tokenMint: "4kWysUHVqtFmxrvwPUxA66exm2iJBMkvD4EBRrNmcieL",
      name: "Clan World: Aelder Whispers",
      symbol: "GOLD",
      iconUrl: "https://example.com/gold.png",
      usdPrice: 0.000026,
      priceChange1h: 1,
      priceChange6h: -2,
      priceChange24h: 3,
      priceChange7d: 4,
      sourceUpdatedAt: "2026-05-07T00:20:26.992157884Z",
    });
  });
});
