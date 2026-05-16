import { describe, expect, it } from "vitest";
import { encodeAbiParameters, encodeEventTopics, type Log } from "viem";
import { CLAN_WORLD_ABI } from "@clan-world/shared/adapters";
import {
  parseHeartbeatEngineEvents,
  snapshotRefreshBlockFromReceipt,
  validateHeartbeatReceipt,
} from "./heartbeat";

const engineAddress = "0x1111111111111111111111111111111111111111" as const;
const foreignAddress = "0x2222222222222222222222222222222222222222" as const;
const transactionHash =
  "0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" as const;

function eventAbi(name: string) {
  const event = CLAN_WORLD_ABI.find(
    (item) => item.type === "event" && item.name === name,
  );
  if (!event || event.type !== "event")
    throw new Error(`missing event ${name}`);
  return event;
}

function fixtureLog(address: `0x${string}`, logIndex: number): Log {
  const abi = eventAbi("TickAdvanced");
  const indexed = abi.inputs.filter((input) => input.indexed);
  const unindexed = abi.inputs.filter((input) => !input.indexed);
  const values = {
    closedTick: 4n,
    openedTick: 5n,
    tickSeed:
      "0x1234000000000000000000000000000000000000000000000000000000000000",
  };
  return {
    address,
    topics: encodeEventTopics({
      abi: [abi],
      eventName: "TickAdvanced" as never,
      args: Object.fromEntries(
        indexed.map((input) => [
          input.name,
          values[input.name as keyof typeof values],
        ]),
      ),
    }) as [`0x${string}`, ...`0x${string}`[]],
    data: encodeAbiParameters(
      unindexed.map((input) => ({ name: input.name, type: input.type })),
      unindexed.map((input) => values[input.name as keyof typeof values]),
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

describe("heartbeat webhook validation", () => {
  it("rejects failed receipts before ingestion", () => {
    const result = validateHeartbeatReceipt(
      {
        status: "reverted",
        to: engineAddress,
        logs: [fixtureLog(engineAddress, 0)],
        blockNumber: 123n,
      },
      engineAddress,
    );

    expect(result).toEqual({ ok: false, status: 200, body: "tx reverted" });
  });

  it("rejects receipts sent to a foreign contract", () => {
    const result = validateHeartbeatReceipt(
      {
        status: "success",
        to: foreignAddress,
        logs: [fixtureLog(engineAddress, 0)],
        blockNumber: 123n,
      },
      engineAddress,
    );

    expect(result).toEqual({ ok: false, status: 200, body: "not engine tx" });
  });

  it("rejects mismatched payload engine addresses", () => {
    const result = validateHeartbeatReceipt(
      {
        status: "success",
        to: engineAddress,
        logs: [fixtureLog(engineAddress, 0)],
        blockNumber: 123n,
      },
      engineAddress,
      foreignAddress,
    );

    expect(result).toEqual({ ok: false, status: 400, body: "engine mismatch" });
  });

  it("filters logs by engine address before decoding", () => {
    const result = validateHeartbeatReceipt(
      {
        status: "success",
        to: engineAddress,
        logs: [fixtureLog(foreignAddress, 0), fixtureLog(engineAddress, 1)],
        blockNumber: 123n,
      },
      engineAddress,
    );

    expect(result.ok).toBe(true);
    if (!result.ok) throw new Error("expected valid receipt");

    const events = parseHeartbeatEngineEvents(result.engineLogs);
    expect(events).toHaveLength(1);
    expect(events[0]?.address).toBe(engineAddress);
    expect(events[0]?.eventName).toBe("TickAdvanced");
  });

  it("uses receipt block for snapshot pin when payload block disagrees", () => {
    const payloadBlockNumber = 999;
    const receipt = {
      status: "success" as const,
      to: engineAddress,
      logs: [fixtureLog(engineAddress, 0)],
      blockNumber: 1000n,
    };

    expect(payloadBlockNumber).not.toBe(Number(receipt.blockNumber));
    expect(snapshotRefreshBlockFromReceipt(receipt)).toBe(1000);
  });
});
