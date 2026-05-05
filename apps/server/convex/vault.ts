import { query, mutation } from "./_generated/server";
import { v } from "convex/values";

/**
 * Vault movement feed for a single clan.
 *
 * Reads recent `chainEvents` (decoded by the indexer) and projects them into a
 * tick-ordered list of resource gains/losses suitable for the cockpit Vault tab.
 *
 * Two indexed lookups are issued:
 *   1. `by_clan_tick` for events the indexer attributed to this clan via the
 *      `clanId` / `targetClanId` extraction in `indexer.ts`. Covers
 *      `ResourcesGathered`, `ResourcesDeposited`, `ResourcesWithdrawn`,
 *      `BlueprintAwarded`, `BlueprintEarned`, `BanditAttackResolved`,
 *      `LootDistributedToDefender`.
 *   2. `by_event_block` over a recent window for "outgoing" / "broadcast"
 *      events whose row-level `clanId` is the *other* clan or absent:
 *      `GoldTransferred`, `VaultResourceTransferred`, `LootDistributed`.
 *      We then filter by `args.fromClanId` / `args.toClanId` /
 *      `args.clanIdsRewarded` in JS.
 *
 * Returned newest-tick-first, capped at the requested limit. All amounts are
 * whole-resource `number` (wei → tokens, divided by 1e18) for friendly display.
 */

const WEI = BigInt("1000000000000000000");

function whole(value: unknown): number {
  if (value === undefined || value === null) return 0;
  try {
    const n = BigInt(String(value));
    return Number(n / WEI);
  } catch {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : 0;
  }
}

// Solidity ResourceType enum, mirrored from contracts. 0..3 = wood/iron/wheat/fish.
const RESOURCE_NAMES = ["wood", "iron", "wheat", "fish"] as const;
function resourceName(index: unknown): string {
  const i = Number(index);
  if (Number.isFinite(i) && i >= 0 && i < RESOURCE_NAMES.length) return RESOURCE_NAMES[i] ?? "resource";
  return "resource";
}

type Movement = {
  tick: number;
  type: "gain" | "spend";
  amount: number;
  resource: string;
  source: string;
  timestamp: number;
};

function pushDelta(
  out: Movement[],
  tick: number,
  type: "gain" | "spend",
  amount: number,
  resource: string,
  source: string,
  timestamp: number,
) {
  if (amount <= 0) return;
  out.push({ tick, type, amount, resource, source, timestamp });
}

function projectAttributed(
  event: { eventName: string; args: Record<string, unknown>; tick?: number; decodedAt: number; clanId?: number },
  clanId: number,
  out: Movement[],
) {
  const tick = event.tick ?? 0;
  const ts = event.decodedAt;
  const args = event.args ?? {};
  switch (event.eventName) {
    case "ResourcesGathered": {
      pushDelta(out, tick, "gain", whole(args.woodGained), "wood", "gather", ts);
      pushDelta(out, tick, "gain", whole(args.ironGained), "iron", "gather", ts);
      pushDelta(out, tick, "gain", whole(args.wheatGained), "wheat", "gather", ts);
      pushDelta(out, tick, "gain", whole(args.fishGained), "fish", "gather", ts);
      pushDelta(out, tick, "gain", whole(args.goldBonus), "gold", "gather bonus", ts);
      return;
    }
    case "ResourcesDeposited": {
      pushDelta(out, tick, "gain", whole(args.woodDelta), "wood", "deposit", ts);
      pushDelta(out, tick, "gain", whole(args.ironDelta), "iron", "deposit", ts);
      pushDelta(out, tick, "gain", whole(args.wheatDelta), "wheat", "deposit", ts);
      pushDelta(out, tick, "gain", whole(args.fishDelta), "fish", "deposit", ts);
      return;
    }
    case "ResourcesWithdrawn": {
      // Withdrawals leave the vault (clansman pulls inventory).
      pushDelta(out, tick, "spend", whole(args.woodDelta), "wood", "withdraw", ts);
      pushDelta(out, tick, "spend", whole(args.ironDelta), "iron", "withdraw", ts);
      pushDelta(out, tick, "spend", whole(args.wheatDelta), "wheat", "withdraw", ts);
      pushDelta(out, tick, "spend", whole(args.fishDelta), "fish", "withdraw", ts);
      return;
    }
    case "BlueprintAwarded":
    case "BlueprintEarned": {
      pushDelta(out, tick, "gain", whole(args.amount), "blueprint", "bandit defeat", ts);
      return;
    }
    case "BanditAttackResolved": {
      // Defender-vault losses. defended=true with stolen=0 still shows as a no-op (filtered by amount<=0).
      pushDelta(out, tick, "spend", whole(args.stolenWood), "wood", "bandit raid", ts);
      pushDelta(out, tick, "spend", whole(args.stolenIron), "iron", "bandit raid", ts);
      pushDelta(out, tick, "spend", whole(args.stolenWheat), "wheat", "bandit raid", ts);
      pushDelta(out, tick, "spend", whole(args.stolenFish), "fish", "bandit raid", ts);
      return;
    }
    case "LootDistributedToDefender": {
      pushDelta(out, tick, "gain", whole(args.wood), "wood", "defender loot", ts);
      pushDelta(out, tick, "gain", whole(args.iron), "iron", "defender loot", ts);
      pushDelta(out, tick, "gain", whole(args.wheat), "wheat", "defender loot", ts);
      pushDelta(out, tick, "gain", whole(args.fish), "fish", "defender loot", ts);
      return;
    }
    default:
      return;
  }
}

function projectBroadcast(
  event: { eventName: string; args: Record<string, unknown>; tick?: number; decodedAt: number },
  clanId: number,
  out: Movement[],
) {
  const tick = event.tick ?? 0;
  const ts = event.decodedAt;
  const args = event.args ?? {};
  switch (event.eventName) {
    case "GoldTransferred": {
      const from = Number(args.fromClanId);
      const to = Number(args.toClanId);
      const amount = whole(args.amount);
      if (Number.isFinite(from) && from === clanId)
        pushDelta(out, tick, "spend", amount, "gold", `transfer → clan ${to}`, ts);
      if (Number.isFinite(to) && to === clanId)
        pushDelta(out, tick, "gain", amount, "gold", `transfer ← clan ${from}`, ts);
      return;
    }
    case "VaultResourceTransferred": {
      const from = Number(args.fromClanId);
      const to = Number(args.toClanId);
      const res = resourceName(args.resource);
      const amount = whole(args.amount);
      if (Number.isFinite(from) && from === clanId)
        pushDelta(out, tick, "spend", amount, res, `transfer → clan ${to}`, ts);
      if (Number.isFinite(to) && to === clanId)
        pushDelta(out, tick, "gain", amount, res, `transfer ← clan ${from}`, ts);
      return;
    }
    case "LootDistributed": {
      const rewarded = Array.isArray(args.clanIdsRewarded) ? args.clanIdsRewarded.map(Number) : [];
      if (!rewarded.includes(clanId)) return;
      pushDelta(out, tick, "gain", whole(args.perClanWood), "wood", "loot share", ts);
      pushDelta(out, tick, "gain", whole(args.perClanIron), "iron", "loot share", ts);
      pushDelta(out, tick, "gain", whole(args.perClanWheat), "wheat", "loot share", ts);
      pushDelta(out, tick, "gain", whole(args.perClanFish), "fish", "loot share", ts);
      pushDelta(out, tick, "gain", whole(args.perClanGold), "gold", "loot share", ts);
      return;
    }
    default:
      return;
  }
}

export const getVaultMovements = query({
  args: {
    clanId: v.number(),
    limit: v.optional(v.number()),
  },
  handler: async (ctx, { clanId, limit }) => {
    const cap = limit ?? 24;
    const scanWindow = Math.max(cap * 4, 80);

    // 1. Indexer-attributed events: row.clanId === clanId.
    const attributed = await ctx.db
      .query("chainEvents")
      .withIndex("by_clan_tick", q => q.eq("clanId", clanId))
      .order("desc")
      .take(scanWindow);

    // 2. Cross-clan transfer / loot events. These don't have row.clanId === us
    //    on the sender side, so scan a recent slice of all events and filter.
    const recent = await ctx.db.query("chainEvents").order("desc").take(scanWindow);
    const broadcast = recent.filter(e =>
      e.eventName === "GoldTransferred" ||
      e.eventName === "VaultResourceTransferred" ||
      e.eventName === "LootDistributed",
    );

    const movements: Movement[] = [];
    for (const e of attributed) {
      projectAttributed(
        { eventName: e.eventName, args: (e.args ?? {}) as Record<string, unknown>, tick: e.tick, decodedAt: e.decodedAt, clanId: e.clanId },
        clanId,
        movements,
      );
    }
    for (const e of broadcast) {
      projectBroadcast(
        { eventName: e.eventName, args: (e.args ?? {}) as Record<string, unknown>, tick: e.tick, decodedAt: e.decodedAt },
        clanId,
        movements,
      );
    }

    // De-dup: same (tick, type, amount, resource, source) collapses if both
    // attributed + broadcast scans surfaced the same row.
    const seen = new Set<string>();
    const deduped: Movement[] = [];
    for (const m of movements) {
      const k = `${m.tick}|${m.type}|${m.amount}|${m.resource}|${m.source}`;
      if (seen.has(k)) continue;
      seen.add(k);
      deduped.push(m);
    }

    deduped.sort((a, b) => b.tick - a.tick || b.timestamp - a.timestamp);
    return deduped.slice(0, cap);
  },
});

// Seed mutation — used by mock/dev to populate the vault movement feed without
// running a chain indexer. Inserts a synthetic chainEvents row that the query
// will pick up via `by_clan_tick`.
export const seedVaultMovement = mutation({
  args: {
    clanId: v.number(),
    tick: v.number(),
    eventName: v.string(),
    args: v.any(),
  },
  handler: async (ctx, args) => {
    const stamp = Date.now();
    await ctx.db.insert("chainEvents", {
      txHash: `seed-${stamp}-${Math.random().toString(36).slice(2, 8)}`,
      logIndex: 0,
      blockNumber: 0,
      eventName: args.eventName,
      args: args.args,
      decodedAt: stamp,
      tick: args.tick,
      clanId: args.clanId,
      source: "seed",
    });
  },
});
