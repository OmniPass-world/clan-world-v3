import { describe, expect, it } from "vitest";
import { projectAttributed, projectBroadcast, type Movement } from "./vault";
import type { IClanWorldAbiEventName } from "@clan-world/contract-types";

const WEI = "1000000000000000000"; // 1e18
const wei = (n: number) => `${n}${"0".repeat(18)}`;

const baseAttributed = (eventName: IClanWorldAbiEventName, args: Record<string, unknown>, tick = 5) => ({
  eventName,
  args,
  tick,
  decodedAt: 1000,
  clanId: 7,
});

const baseBroadcast = (eventName: IClanWorldAbiEventName, args: Record<string, unknown>, tick = 5) => ({
  eventName,
  args,
  tick,
  decodedAt: 1000,
});

describe("projectAttributed", () => {
  it("projects ResourcesGathered into per-resource gains + gold bonus", () => {
    const out: Movement[] = [];
    projectAttributed(
      baseAttributed("ResourcesGathered", {
        woodGained: wei(3),
        ironGained: wei(0),
        wheatGained: wei(2),
        fishGained: wei(0),
        goldBonus: wei(1),
      }),
      7,
      out,
    );
    // Filters zero-amount entries, keeps wood/wheat/gold gains
    expect(out.map(m => `${m.type}:${m.amount}:${m.resource}`)).toEqual([
      "gain:3:wood",
      "gain:2:wheat",
      "gain:1:gold",
    ]);
    expect(out.every(m => m.tick === 5)).toBe(true);
    expect(out.every(m => m.source === "gather" || m.source === "gather bonus")).toBe(true);
  });

  it("projects ResourcesDeposited as gains; ResourcesWithdrawn as spends", () => {
    const out: Movement[] = [];
    projectAttributed(baseAttributed("ResourcesDeposited", { woodDelta: wei(5) }), 7, out);
    projectAttributed(baseAttributed("ResourcesWithdrawn", { ironDelta: wei(2) }), 7, out);
    expect(out).toEqual([
      expect.objectContaining({ type: "gain", amount: 5, resource: "wood", source: "deposit" }),
      expect.objectContaining({ type: "spend", amount: 2, resource: "iron", source: "withdraw" }),
    ]);
  });

  it("projects BlueprintAwarded / BlueprintEarned as blueprint gains", () => {
    const out: Movement[] = [];
    projectAttributed(baseAttributed("BlueprintAwarded", { amount: wei(4) }), 7, out);
    projectAttributed(baseAttributed("BlueprintEarned", { amount: wei(2) }), 7, out);
    expect(out.map(m => m.amount)).toEqual([4, 2]);
    expect(out.every(m => m.resource === "blueprint" && m.type === "gain")).toBe(true);
  });

  it("projects BanditAttackResolved stolen amounts as spends", () => {
    const out: Movement[] = [];
    projectAttributed(
      baseAttributed("BanditAttackResolved", {
        stolenWood: wei(3),
        stolenIron: wei(0),
        stolenWheat: wei(0),
        stolenFish: wei(1),
      }),
      7,
      out,
    );
    expect(out).toEqual([
      expect.objectContaining({ type: "spend", amount: 3, resource: "wood", source: "bandit raid" }),
      expect.objectContaining({ type: "spend", amount: 1, resource: "fish", source: "bandit raid" }),
    ]);
  });

  it("projects market events using gold resource id 4", () => {
    const out: Movement[] = [];
    // Buy: spend gold (resourceIn=4), gain wood (resourceOut=0)
    projectAttributed(
      baseAttributed("ImmediateMarketActionExecuted", {
        resourceIn: 4,
        amountIn: wei(10),
        resourceOut: 0,
        amountOut: wei(5),
      }),
      7,
      out,
    );
    expect(out).toEqual([
      expect.objectContaining({ type: "spend", amount: 10, resource: "gold", source: "market trade" }),
      expect.objectContaining({ type: "gain", amount: 5, resource: "wood", source: "market trade" }),
    ]);

    // Sell scheduled: spend wheat, gain gold; uses "market settle" label
    out.length = 0;
    projectAttributed(
      baseAttributed("ScheduledMarketActionExecuted", {
        resourceIn: 2,
        amountIn: wei(8),
        resourceOut: 4,
        amountOut: wei(11),
      }),
      7,
      out,
    );
    expect(out.map(m => `${m.type}:${m.amount}:${m.resource}:${m.source}`)).toEqual([
      "spend:8:wheat:market settle",
      "gain:11:gold:market settle",
    ]);
  });

  it("ignores unknown event names", () => {
    const out: Movement[] = [];
    projectAttributed(baseAttributed("TickAdvanced", { closedTick: 1 }), 7, out);
    expect(out).toEqual([]);
  });
});

describe("projectBroadcast", () => {
  it("emits a spend on the sender side and a gain on the recipient side for GoldTransferred", () => {
    const out: Movement[] = [];
    // Sender perspective: clanId === fromClanId
    projectBroadcast(
      baseBroadcast("GoldTransferred", { fromClanId: 7, toClanId: 9, amount: wei(20) }),
      7,
      out,
    );
    expect(out).toHaveLength(1);
    expect(out[0]).toMatchObject({ type: "spend", amount: 20, resource: "gold", source: "transfer → clan 9" });

    // Recipient perspective
    out.length = 0;
    projectBroadcast(
      baseBroadcast("GoldTransferred", { fromClanId: 7, toClanId: 9, amount: wei(20) }),
      9,
      out,
    );
    expect(out).toHaveLength(1);
    expect(out[0]).toMatchObject({ type: "gain", amount: 20, resource: "gold", source: "transfer ← clan 7" });
  });

  it("projects VaultResourceTransferred with resource enum mapping", () => {
    const out: Movement[] = [];
    projectBroadcast(
      // resource id 2 = wheat
      baseBroadcast("VaultResourceTransferred", { fromClanId: 1, toClanId: 9, resource: 2, amount: wei(7) }),
      9,
      out,
    );
    expect(out[0]).toMatchObject({ type: "gain", amount: 7, resource: "wheat", source: "transfer ← clan 1" });
  });

  it("projects BlueprintTransferred (the missing-event finding from review)", () => {
    const out: Movement[] = [];
    projectBroadcast(
      baseBroadcast("BlueprintTransferred", { fromClanId: 7, toClanId: 3, amount: wei(2) }),
      7,
      out,
    );
    expect(out[0]).toMatchObject({ type: "spend", amount: 2, resource: "blueprint", source: "transfer → clan 3" });
  });

  it("projects LootDistributed only for clans in clanIdsRewarded", () => {
    const out: Movement[] = [];
    const args = {
      clanIdsRewarded: [3, 5, 9],
      perClanWood: wei(2),
      perClanIron: wei(0),
      perClanWheat: wei(1),
      perClanFish: wei(0),
      perClanGold: wei(4),
    };
    // Non-rewarded clan: skip
    projectBroadcast(baseBroadcast("LootDistributed", args), 7, out);
    expect(out).toEqual([]);

    // Rewarded clan: emits gains for the non-zero per-resource fields
    projectBroadcast(baseBroadcast("LootDistributed", args), 5, out);
    expect(out.map(m => `${m.amount}:${m.resource}`)).toEqual(["2:wood", "1:wheat", "4:gold"]);
    expect(out.every(m => m.type === "gain" && m.source === "loot share")).toBe(true);
  });

  it("ignores transfers where the clan is neither sender nor recipient", () => {
    const out: Movement[] = [];
    projectBroadcast(
      baseBroadcast("GoldTransferred", { fromClanId: 1, toClanId: 2, amount: wei(20) }),
      9,
      out,
    );
    expect(out).toEqual([]);
  });
});
