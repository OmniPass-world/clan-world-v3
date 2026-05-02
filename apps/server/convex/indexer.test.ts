import { describe, expect, it } from "vitest";
import { encodeEventTopics, encodeAbiParameters, type Log } from "viem";
import { CLAN_WORLD_ABI } from "@clan-world/shared/adapters";
import { decodeClanWorldLogs } from "./indexer";

const address = "0x1111111111111111111111111111111111111111" as const;
const transactionHash = "0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" as const;

function eventAbi(name: string) {
  const event = CLAN_WORLD_ABI.find((item) => item.type === "event" && item.name === name);
  if (!event || event.type !== "event") throw new Error(`missing event ${name}`);
  return event;
}

function fixtureLog(name: string, values: Record<string, unknown>, logIndex: number): Log {
  const abi = eventAbi(name);
  const indexed = abi.inputs.filter((input) => input.indexed);
  const unindexed = abi.inputs.filter((input) => !input.indexed);
  return {
    address,
    topics: encodeEventTopics({
      abi: [abi],
      eventName: name as never,
      args: Object.fromEntries(indexed.map((input) => [input.name, values[input.name]])),
    }) as [`0x${string}`, ...`0x${string}`[]],
    data: encodeAbiParameters(
      unindexed.map((input) => ({ name: input.name, type: input.type })),
      unindexed.map((input) => values[input.name]),
    ),
    blockHash: "0xbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
    blockNumber: 123n,
    transactionHash,
    transactionIndex: 2,
    logIndex,
    removed: false,
  };
}

describe("decodeClanWorldLogs", () => {
  it("decodes representative ClanWorld events with bigint-safe args", () => {
    const decoded = decodeClanWorldLogs([
      fixtureLog(
        "TickAdvanced",
        {
          closedTick: 4n,
          openedTick: 5n,
          tickSeed: "0x1234000000000000000000000000000000000000000000000000000000000000",
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
