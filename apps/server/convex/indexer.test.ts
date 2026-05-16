import { describe, expect, it, vi } from "vitest";
import { encodeEventTopics, encodeAbiParameters, type Log } from "viem";
import { CLAN_WORLD_ABI } from "@clan-world/shared/adapters";
import {
  commitSnapshot,
  decodeClanWorldLogs,
  ingestEvents,
  planPollLogRange,
  pricePointFromEvent,
  stableJson,
} from "./indexer";

const address = "0x1111111111111111111111111111111111111111" as const;
const transactionHash =
  "0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" as const;

function createDb(tables: Record<string, any[]> = {}) {
  return {
    tables,
    db: {
      async insert(table: string, value: Record<string, unknown>) {
        tables[table] ??= [];
        const doc = {
          ...value,
          _id: `${table}:${tables[table].length}`,
          _creationTime: tables[table].length,
        };
        tables[table].push(doc);
        return doc._id;
      },
      async patch(id: string, value: Record<string, unknown>) {
        for (const rows of Object.values(tables)) {
          const index = rows.findIndex((row) => row._id === id);
          if (index >= 0) rows[index] = { ...rows[index], ...value };
        }
      },
      query(table: string) {
        let rows = [...(tables[table] ?? [])];
        const builder = {
          withIndex(_name: string, apply: (q: any) => unknown) {
            const clauses: Array<{ field: string; value: unknown }> = [];
            const q = {
              eq(field: string, value: unknown) {
                clauses.push({ field, value });
                return q;
              },
            };
            apply(q);
            rows = rows.filter((row) =>
              clauses.every((clause) => row[clause.field] === clause.value),
            );
            return builder;
          },
          order(direction: "asc" | "desc") {
            rows = [...rows].sort((a, b) =>
              direction === "desc"
                ? b._creationTime - a._creationTime
                : a._creationTime - b._creationTime,
            );
            return builder;
          },
          async first() {
            return rows[0] ?? null;
          },
        };
        return builder;
      },
    },
  };
}

function eventAbi(name: string) {
  const event = CLAN_WORLD_ABI.find(
    (item) => item.type === "event" && item.name === name,
  );
  if (!event || event.type !== "event")
    throw new Error(`missing event ${name}`);
  return event;
}

function fixtureLog(
  name: string,
  values: Record<string, unknown>,
  logIndex: number,
): Log {
  const abi = eventAbi(name);
  const indexed = abi.inputs.filter((input) => input.indexed);
  const unindexed = abi.inputs.filter((input) => !input.indexed);
  return {
    address,
    topics: encodeEventTopics({
      abi: [abi],
      eventName: name as never,
      args: Object.fromEntries(
        indexed.map((input) => [input.name, values[input.name]]),
      ),
    }) as [`0x${string}`, ...`0x${string}`[]],
    data: encodeAbiParameters(
      unindexed.map((input) => ({ name: input.name, type: input.type })),
      unindexed.map((input) => values[input.name]),
    ),
    blockHash:
      "0xbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
    blockNumber: 123n,
    transactionHash,
    transactionIndex: 2,
    logIndex,
    removed: false,
  };
}

describe("stableJson", () => {
  it("is key-order-independent", () => {
    const a = { b: 1, a: 2 };
    const b = { a: 2, b: 1 };
    expect(stableJson(a)).toBe(stableJson(b));
  });
  it("two worldSnapshot-like objects differing only in tick-family fields produce equal stableJson when those fields are stripped", () => {
    const strip = (o: Record<string, unknown>) => {
      const { tick, tickEpochStartedAt, tickEpochDurationMs, currentTickSeed, nextHeartbeatAtTick, lastUpdatedAt, lastUpdatedBlock, txHash, ...rest } = o;
      void tick; void tickEpochStartedAt; void tickEpochDurationMs; void currentTickSeed; void nextHeartbeatAtTick; void lastUpdatedAt; void lastUpdatedBlock; void txHash;
      return rest;
    };
    const prev = { tick: 10, tickEpochStartedAt: 1000, regions: [{ id: "A" }], clans: [] };
    const next = { tick: 11, tickEpochStartedAt: 1060, regions: [{ id: "A" }], clans: [] };
    expect(stableJson(strip(prev))).toBe(stableJson(strip(next)));
  });
});

describe("decodeClanWorldLogs", () => {
  it("decodes representative ClanWorld events with bigint-safe args", () => {
    const decoded = decodeClanWorldLogs([
      fixtureLog(
        "TickAdvanced",
        {
          closedTick: 4n,
          openedTick: 5n,
          tickSeed:
            "0x1234000000000000000000000000000000000000000000000000000000000000",
        },
        0,
      ),
      fixtureLog(
        "MissionAssigned",
        {
          clanId: 1,
          clansmanId: 7,
          missionNonce: 9n,
          action: 2,
          startRegion: 3,
          targetRegion: 4,
          startTick: 5n,
          arrivalTick: 6n,
        },
        1,
      ),
      fixtureLog(
        "ImmediateMarketActionExecuted",
        {
          clanId: 2,
          clansmanId: 8,
          action: 6,
          resourceIn: 1,
          amountIn: 100n,
          resourceOut: 2,
          amountOut: 50n,
          tick: 11n,
        },
        2,
      ),
      fixtureLog(
        "BanditAttackResolved",
        {
          banditId: 3,
          targetClanId: 4,
          defended: true,
          attackPower: 12,
          totalDefense: 13,
          wallLevelAfter: 2,
          stolenWood: 1n,
          stolenIron: 2n,
          stolenWheat: 3n,
          stolenFish: 4n,
          atTick: 20n,
        },
        3,
      ),
    ]);

    expect(decoded.map((event) => event.eventName)).toEqual([
      "TickAdvanced",
      "MissionAssigned",
      "ImmediateMarketActionExecuted",
      "BanditAttackResolved",
    ]);
    expect(decoded[0]?.args.closedTick).toBe("4");
    expect(decoded[1]?.args.missionNonce).toBe("9");
    expect(decoded[2]?.args.amountOut).toBe("50");
    expect(decoded[3]?.args.stolenFish).toBe("4");
  });
});

describe("pollLogs range planning", () => {
  it("uses INDEXER_START_BLOCK when there is no checkpoint", () => {
    const previousStartBlock = process.env.INDEXER_START_BLOCK;
    const previousDepth = process.env.INDEXER_CONFIRMATION_DEPTH;
    process.env.INDEXER_START_BLOCK = "12345";
    process.env.INDEXER_CONFIRMATION_DEPTH = "5";

    const range = planPollLogRange(null, 20_000n);

    expect(range.fromBlock).toBe(12_345n);
    // Capped at fromBlock + MAX_LOG_BLOCK_RANGE (9n for Alchemy free tier)
    // rather than safeLatest (19_995n).
    expect(range.toBlock).toBe(12_354n);
    expect(range.shouldPoll).toBe(true);

    if (previousStartBlock === undefined)
      delete process.env.INDEXER_START_BLOCK;
    else process.env.INDEXER_START_BLOCK = previousStartBlock;
    if (previousDepth === undefined)
      delete process.env.INDEXER_CONFIRMATION_DEPTH;
    else process.env.INDEXER_CONFIRMATION_DEPTH = previousDepth;
  });

  it("caps toBlock at the latest confirmed block", () => {
    const previousDepth = process.env.INDEXER_CONFIRMATION_DEPTH;
    process.env.INDEXER_CONFIRMATION_DEPTH = "5";

    const range = planPollLogRange({ lastBlock: 89 }, 100n);

    expect(range.fromBlock).toBe(90n);
    expect(range.toBlock).toBe(95n);

    if (previousDepth === undefined)
      delete process.env.INDEXER_CONFIRMATION_DEPTH;
    else process.env.INDEXER_CONFIRMATION_DEPTH = previousDepth;
  });
});

describe("pricePointFromEvent", () => {
  it("indexes buys under the bought non-gold resource", () => {
    const pricePoint = pricePointFromEvent(
      {
        eventName: "ImmediateMarketActionExecuted",
        args: {
          action: 11,
          resourceIn: 4,
          amountIn: "200",
          resourceOut: 2,
          amountOut: "100",
          tick: "7",
        },
      },
      123,
    );

    expect(pricePoint?.resourceType).toBe("2");
    expect(pricePoint?.priceWoodGold).toBe("2000000000000000000");
  });

  it("indexes sells under the sold non-gold resource", () => {
    const pricePoint = pricePointFromEvent(
      {
        eventName: "ScheduledMarketActionExecuted",
        args: {
          action: 12,
          resourceIn: 1,
          amountIn: "100",
          resourceOut: 4,
          amountOut: "300",
          settledAtTick: "8",
        },
      },
      124,
    );

    expect(pricePoint?.resourceType).toBe("1");
    expect(pricePoint?.priceWoodGold).toBe("3000000000000000000");
  });
});

describe("ingestEvents checkpoint isolation", () => {
  const event = {
    eventName: "TickAdvanced",
    args: {
      closedTick: "1",
      openedTick: "2",
      tickSeed: "0x1234",
    },
    transactionHash,
    blockNumber: 105,
    logIndex: 0,
  };

  it("does not advance the pollLogs cursor for webhook ingestion", async () => {
    const { db, tables } = createDb({
      eventCheckpoint: [
        { _id: "eventCheckpoint:0", _creationTime: 0, lastBlock: 100 },
      ],
    });

    await (ingestEvents as any)._handler(
      { db },
      {
        events: [event],
        blockNumber: 105,
        txHash: transactionHash,
        advanceCheckpoint: false,
      },
    );

    expect(tables.eventCheckpoint?.[0]?.lastBlock).toBe(100);
    expect(tables.chainEvents).toHaveLength(1);
  });

  it("advances the pollLogs cursor when requested by the poller", async () => {
    const { db, tables } = createDb({
      eventCheckpoint: [
        { _id: "eventCheckpoint:0", _creationTime: 0, lastBlock: 100 },
      ],
    });

    await (ingestEvents as any)._handler(
      { db },
      {
        events: [event],
        blockNumber: 105,
        txHash: transactionHash,
        advanceCheckpoint: true,
      },
    );

    expect(tables.eventCheckpoint?.[0]?.lastBlock).toBe(105);
  });
});

describe("legacy snapshot backfill", () => {
  it("commitSnapshot writes non-empty legacy clans from clanView rows", async () => {
    const { db, tables } = createDb();

    await (commitSnapshot as any)._handler(
      { db },
      {
        snapshot: {
          blockNumber: 99,
          world: { currentTick: 12 },
          clans: [
            {
              clan: {
                clan: {
                  clanId: 2,
                  owner: "0x0000000000000000000000000000000000000000",
                  goldBalance: "250",
                  blueprintBalance: "5",
                  vaultWood: "1000000000000000000",
                  vaultIron: "2000000000000000000",
                  vaultWheat: "3000000000000000000",
                  vaultFish: "4000000000000000000",
                },
              },
            },
          ],
        },
      },
    );

    const worldSnapshots = tables.worldSnapshot ?? [];
    expect(worldSnapshots).toHaveLength(1);
    expect(worldSnapshots[0].clans).toEqual([
      {
        id: "2",
        name: "Clan 2",
        treasury: "250",
        goldBalance: "250",
        blueprintBalance: "5",
        vaultWood: "1000000000000000000",
        vaultIron: "2000000000000000000",
        vaultWheat: "3000000000000000000",
        vaultFish: "4000000000000000000",
        baseRegion: 0,
        baseLevel: 0,
        wallLevel: 0,
        monumentLevel: 0,
        livingClansmen: 0,
        owner: "0x0000000000000000000000000000000000000000",
        clansmen: [],
      },
    ]);
    expect(worldSnapshots[0].regions).toHaveLength(8);
  });

  it("preserves tick epoch start across same-tick snapshot refreshes", async () => {
    const { db, tables } = createDb();
    const now = vi.spyOn(Date, "now");
    now.mockReturnValue(1_000_000);

    await (commitSnapshot as any)._handler(
      { db },
      {
        snapshot: {
          blockNumber: 99,
          world: { currentTick: 12 },
          clans: [],
        },
      },
    );

    now.mockReturnValue(1_015_000);
    await (commitSnapshot as any)._handler(
      { db },
      {
        snapshot: {
          blockNumber: 100,
          world: { currentTick: 12 },
          clans: [],
        },
      },
    );

    const sameTickSnapshot = tables.worldSnapshot?.[0];
    expect(sameTickSnapshot.tickEpochStartedAt).toBe(1_000);
    expect(sameTickSnapshot.tickEpochDurationMs).toBe(60_000);

    now.mockReturnValue(1_060_000);
    await (commitSnapshot as any)._handler(
      { db },
      {
        snapshot: {
          blockNumber: 101,
          world: { currentTick: 13 },
          clans: [],
        },
      },
    );

    const advancedTickSnapshot = tables.worldSnapshot?.[0];
    expect(advancedTickSnapshot.tickEpochStartedAt).toBe(1_060);
    expect(advancedTickSnapshot.tickEpochDurationMs).toBe(60_000);

    now.mockRestore();
  });

  it("skips stale same-tick snapshot refreshes from older blocks", async () => {
    const { db, tables } = createDb({
      worldSnapshot: [
        {
          _id: "worldSnapshot:0",
          _creationTime: 0,
          tick: 12,
          lastUpdatedBlock: 100,
          tickEpochStartedAt: 1_000,
          tickEpochDurationMs: 60_000,
          clans: [{ id: "current" }],
        },
      ],
    });

    const result = await (commitSnapshot as any)._handler(
      { db },
      {
        snapshot: {
          blockNumber: 99,
          world: { currentTick: 12 },
          clans: [],
        },
      },
    );

    expect(result.skipped).toBe("stale-snapshot");
    expect(tables.worldSnapshot?.[0]?.lastUpdatedBlock).toBe(100);
    expect(tables.worldSnapshot?.[0]?.clans).toEqual([{ id: "current" }]);
  });
});
