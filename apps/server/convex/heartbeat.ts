// v1.0.1: webhook accepts tx pings and logs them; chain state is read by
// runner/Elder paths via getWorldSnapshot/getRankings. v1.1 will replace
// this with a real event-decoder that reads logs from the heartbeat tx
// and refreshes Convex snapshots from chain state. Tracked: GH issue #TBD
import { httpAction, internalMutation } from "./_generated/server";
import { internal } from "./_generated/api";
import { baseSepolia, CLAN_WORLD_ABI } from "@clan-world/shared/adapters";
import { createPublicClient, http, parseEventLogs, type Hex } from "viem";

const indexerApi = (internal as any).indexer;

type HeartbeatWebhookPayload = {
  txHash?: unknown;
  blockNumber?: unknown;
  engineAddress?: unknown;
  firedAtTs?: unknown;
  chain?: unknown;
  source?: unknown;
};

const isHexHash = (value: unknown): value is Hex =>
  typeof value === "string" && /^0x[0-9a-fA-F]{64}$/.test(value);

const numberFromPayload = (value: unknown): number | undefined => {
  if (typeof value === "number" && Number.isFinite(value)) return value;
  if (typeof value === "string" && value !== "") return Number(value);
  return undefined;
};

const bigintSafe = (value: unknown): unknown => {
  if (typeof value === "bigint") return value.toString();
  if (Array.isArray(value)) return value.map(bigintSafe);
  if (!value || typeof value !== "object") return value;
  return Object.fromEntries(
    Object.entries(value).map(([key, nested]) => [key, bigintSafe(nested)]),
  );
};

export const heartbeatWebhook = httpAction(async (ctx, request) => {
  if (request.method !== "POST") {
    return new Response(JSON.stringify({ error: "Method not allowed" }), {
      status: 405,
      headers: { "Content-Type": "application/json", Allow: "POST" },
    });
  }

  const secret = process.env.WEBHOOK_SHARED_SECRET;
  const auth = request.headers.get("Authorization");
  if (!secret || auth !== `Bearer ${secret}`) {
    return new Response(JSON.stringify({ error: "Unauthorized" }), {
      status: 401,
      headers: { "Content-Type": "application/json" },
    });
  }

  const payload = (await request.json()) as HeartbeatWebhookPayload;
  const txData = {
    txHash: payload.txHash,
    blockNumber: payload.blockNumber,
    engineAddress: payload.engineAddress,
    chain: payload.chain,
    source: payload.source,
  };

  console.log("heartbeat webhook tx ping", txData);

  if (process.env.CLANWORLD_USE_REAL_INDEXER === "true") {
    if (!isHexHash(payload.txHash)) {
      return new Response(JSON.stringify({ error: "txHash is required" }), {
        status: 400,
        headers: { "Content-Type": "application/json" },
      });
    }
    const rpcUrl = process.env.RPC_URL_PRIMARY;
    if (!rpcUrl) {
      return new Response(JSON.stringify({ error: "RPC_URL_PRIMARY is required" }), {
        status: 500,
        headers: { "Content-Type": "application/json" },
      });
    }

    const publicClient = createPublicClient({
      chain: baseSepolia,
      transport: http(rpcUrl),
    });
    const receipt = await publicClient.waitForTransactionReceipt({ hash: payload.txHash });
    const parsed = parseEventLogs({
      abi: CLAN_WORLD_ABI,
      logs: receipt.logs,
      strict: false,
    }).map((event) => ({
      eventName: event.eventName,
      args: bigintSafe(event.args ?? {}),
      address: event.address,
      blockHash: event.blockHash,
      blockNumber: event.blockNumber,
      transactionHash: event.transactionHash,
      transactionIndex: event.transactionIndex,
      logIndex: event.logIndex,
    }));
    const blockNumber = numberFromPayload(payload.blockNumber) ?? Number(receipt.blockNumber);
    await ctx.runMutation(indexerApi.ingestEvents, {
      events: parsed,
      blockNumber,
      txHash: payload.txHash,
      firedAtTs: numberFromPayload(payload.firedAtTs),
    });
    await ctx.runAction(indexerApi.refreshSnapshot, {});

    return new Response(
      JSON.stringify({ status: "ok", received: txData, decodedEvents: parsed.length }),
      {
        status: 200,
        headers: { "Content-Type": "application/json" },
      },
    );
  }

  return new Response(JSON.stringify({ status: "ok", received: txData }), {
    status: 200,
    headers: { "Content-Type": "application/json" },
  });
});

type AdvanceTickResult =
  | { status: "ok"; tick: number }
  | { status: "no-op"; reason: string };

export const advanceTick = internalMutation({
  handler: async (ctx): Promise<AdvanceTickResult> => {
    const snap = await ctx.db.query("worldSnapshot").order("desc").first();
    if (!snap) return { status: "no-op", reason: "no snapshot to refresh" };

    // Staleness gate: only advance if the current epoch has elapsed.
    // Prevents cron from double-advancing (fires 4x/epoch) and concurrent
    // calls from inserting duplicate tick rows.
    const nowSeconds = Math.floor(Date.now() / 1000);
    const epochEndSeconds =
      snap.tickEpochStartedAt +
      Math.floor(snap.tickEpochDurationMs / 1000);
    if (nowSeconds < epochEndSeconds) {
      return { status: "no-op", reason: "epoch not yet elapsed" };
    }

    const newTick = snap.tick + 1;
    await ctx.db.insert("worldSnapshot", {
      tick: newTick,
      tickEpochStartedAt: Math.floor(Date.now() / 1000),
      tickEpochDurationMs: snap.tickEpochDurationMs,
      regions: snap.regions,
      clans: snap.clans,
    });
    await ctx.db.insert("agentLogs", {
      level: "info",
      message: `heartbeat: tick ${snap.tick} → ${newTick}`,
      timestamp: Date.now(),
    });

    return { status: "ok", tick: newTick };
  },
});
