import { query, internalMutation } from "./_generated/server";
import { v } from "convex/values";
import type { IClanWorldAbiEventName } from "@clan-world/contract-types";

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
 *      `LootDistributedToDefender`, `ImmediateMarketActionExecuted`,
 *      `ScheduledMarketActionExecuted`.
 *   2. A recent-window scan over all events for "broadcast" / cross-clan
 *      events whose row-level `clanId` is the *other* clan or absent:
 *      `GoldTransferred`, `VaultResourceTransferred`, `LootDistributed`,
 *      `BlueprintTransferred`. We then filter by `args.fromClanId` /
 *      `args.toClanId` / `args.clanIdsRewarded` in JS.
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
// Market events use resource id 4 to mean gold (the quote asset). See
// `packages/contracts/src/IClanWorld.sol` constant `RESOURCE_GOLD = 4` and
// `apps/server/convex/indexer.ts::pricePointFromEvent` for the same convention.
const RESOURCE_NAMES = ["wood", "iron", "wheat", "fish"] as const;
const GOLD_RESOURCE_ID = 4;
function resourceName(index: unknown): string {
  const i = Number(index);
  if (Number.isFinite(i) && i === GOLD_RESOURCE_ID) return "gold";
  if (Number.isFinite(i) && i >= 0 && i < RESOURCE_NAMES.length) return RESOURCE_NAMES[i] ?? "resource";
  return "resource";
}

export type Movement = {
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

export function projectAttributed(
  event: { eventName: IClanWorldAbiEventName; args: Record<string, unknown>; tick?: number; decodedAt: number; clanId?: number },
  _clanId: number,
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
    case "ResourcesInjected": {
      pushDelta(out, tick, "gain", whole(args.wood), "wood", "admin inject", ts);
      pushDelta(out, tick, "gain", whole(args.iron), "iron", "admin inject", ts);
      pushDelta(out, tick, "gain", whole(args.wheat), "wheat", "admin inject", ts);
      pushDelta(out, tick, "gain", whole(args.fish), "fish", "admin inject", ts);
      pushDelta(out, tick, "gain", whole(args.gold), "gold", "admin inject", ts);
      pushDelta(out, tick, "gain", whole(args.blueprint), "blueprint", "admin inject", ts);
      return;
    }
    case "ImmediateMarketActionExecuted":
    case "ScheduledMarketActionExecuted": {
      // Market trade: the clan spends `amountIn` of `resourceIn` and receives
      // `amountOut` of `resourceOut`. resource id 4 == gold (per
      // RESOURCE_GOLD in contracts). Emit two deltas — one spend, one gain.
      const label = event.eventName === "ImmediateMarketActionExecuted" ? "market trade" : "market settle";
      const resIn = resourceName(args.resourceIn);
      const resOut = resourceName(args.resourceOut);
      pushDelta(out, tick, "spend", whole(args.amountIn), resIn, label, ts);
      pushDelta(out, tick, "gain", whole(args.amountOut), resOut, label, ts);
      return;
    }
    default:
      return;
  }
}

export function projectBroadcast(
  event: { eventName: IClanWorldAbiEventName; args: Record<string, unknown>; tick?: number; decodedAt: number },
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
    case "BlueprintTransferred": {
      // Direct OTC blueprint move between clans. Mirrors GoldTransferred
      // shape but for the blueprint resource.
      const from = Number(args.fromClanId);
      const to = Number(args.toClanId);
      const amount = whole(args.amount);
      if (Number.isFinite(from) && from === clanId)
        pushDelta(out, tick, "spend", amount, "blueprint", `transfer → clan ${to}`, ts);
      if (Number.isFinite(to) && to === clanId)
        pushDelta(out, tick, "gain", amount, "blueprint", `transfer ← clan ${from}`, ts);
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
    //    on the sender side (or at all for LootDistributed), so we issue per-
    //    event-name lookups via `by_event_block` so unrelated chain churn
    //    can't push transfers out of our scan window.
    const broadcastEventNames: IClanWorldAbiEventName[] = [
      "GoldTransferred",
      "VaultResourceTransferred",
      "LootDistributed",
      "BlueprintTransferred",
    ];
    const broadcastBuckets = await Promise.all(
      broadcastEventNames.map(name =>
        ctx.db
          .query("chainEvents")
          .withIndex("by_event_block", q => q.eq("eventName", name))
          .order("desc")
          .take(scanWindow),
      ),
    );
    const broadcast = broadcastBuckets.flat();

    type SourceEvent = {
      eventName: IClanWorldAbiEventName;
      args: Record<string, unknown>;
      tick?: number;
      decodedAt: number;
      txHash: string;
      logIndex: number;
    };
    const movements: (Movement & { _src: string })[] = [];
    const collect = (e: SourceEvent, project: (s: SourceEvent, c: number, out: Movement[]) => void) => {
      const before = movements.length;
      const buf: Movement[] = [];
      project(e, clanId, buf);
      for (const m of buf) movements.push({ ...m, _src: `${e.txHash}:${e.logIndex}` });
      void before;
    };
    for (const e of attributed) {
      collect(
        {
          // Safe: unknown names fall through to `default: return` in projectAttributed.
          eventName: e.eventName as IClanWorldAbiEventName,
          args: (e.args ?? {}) as Record<string, unknown>,
          tick: e.tick,
          decodedAt: e.decodedAt,
          txHash: e.txHash,
          logIndex: e.logIndex,
        },
        (src, cid, out) =>
          projectAttributed(
            { eventName: src.eventName, args: src.args, tick: src.tick, decodedAt: src.decodedAt, clanId: e.clanId },
            cid,
            out,
          ),
      );
    }
    for (const e of broadcast) {
      collect(
        {
          // Safe: unknown names fall through to `default: return` in projectBroadcast.
          eventName: e.eventName as IClanWorldAbiEventName,
          args: (e.args ?? {}) as Record<string, unknown>,
          tick: e.tick,
          decodedAt: e.decodedAt,
          txHash: e.txHash,
          logIndex: e.logIndex,
        },
        (src, cid, out) =>
          projectBroadcast(
            { eventName: src.eventName, args: src.args, tick: src.tick, decodedAt: src.decodedAt },
            cid,
            out,
          ),
      );
    }

    // De-dup on the unique chain log identity + projected delta — collapses
    // entries where both index lookups surfaced the same chainEvents row,
    // while still allowing two separate transfers in the same tick of the
    // same shape to render as two distinct movements.
    const seen = new Set<string>();
    const deduped: Movement[] = [];
    for (const m of movements) {
      const k = `${m._src}|${m.type}|${m.amount}|${m.resource}|${m.source}`;
      if (seen.has(k)) continue;
      seen.add(k);
      const { _src, ...clean } = m;
      void _src;
      deduped.push(clean);
    }

    deduped.sort((a, b) => b.tick - a.tick || b.timestamp - a.timestamp);
    return deduped.slice(0, cap);
  },
});

// Seed mutation — used by demo prep / vitest fixtures to populate the vault
// movement feed without running a real chain indexer. INTERNAL ONLY: writes
// directly into `chainEvents` (the indexer's truth table), so a public
// surface here would let any caller forge vault history. Invoke from another
// mutation/action via `internal.vault.seedVaultMovement`, or via
// `pnpm convex run --internal vault:seedVaultMovement`.
export const seedVaultMovement = internalMutation({
  args: {
    clanId: v.number(),
    tick: v.number(),
    eventName: v.string(),
    // TODO(post-demo): tighten validator (M-5 audit). `args` is a free-form
    // event-payload blob persisted directly into `chainEvents.args`, which
    // is itself v.any() in schema by design — narrowing here would over-
    // constrain the event-name discriminated union (dozens of variants).
    // Internal-only seed mutation; not reachable from public surface.
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
