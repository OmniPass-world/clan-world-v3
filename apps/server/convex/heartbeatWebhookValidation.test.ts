import { describe, expect, it, vi } from "vitest";
import { encodeAbiParameters, encodeEventTopics, type Log } from "viem";
import { CLAN_WORLD_ABI } from "@clan-world/shared/adapters";
import {
  parseHeartbeatEngineEvents,
  snapshotRefreshBlockFromReceipt,
  validateHeartbeatReceipt,
} from "./heartbeat";

// ---------------------------------------------------------------------------
// HMAC signature helpers (mirrors webhook-sign.sh + heartbeatWebhook logic)
// ---------------------------------------------------------------------------
async function signPayload(secret: string, timestamp: number, body: string): Promise<string> {
  const enc = new TextEncoder();
  const key = await crypto.subtle.importKey(
    "raw", enc.encode(secret), { name: "HMAC", hash: "SHA-256" }, false, ["sign"],
  );
  const sigBuf = await crypto.subtle.sign("HMAC", key, enc.encode(`${timestamp}.${body}`));
  const hex = Array.from(new Uint8Array(sigBuf)).map(b => b.toString(16).padStart(2, "0")).join("");
  return `t=${timestamp},v1=${hex}`;
}

function makeRequest(sig: string | null, body: string): Request {
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  if (sig !== null) headers["x-heartbeat-signature"] = sig;
  return new Request("http://localhost/api/heartbeat-webhook", {
    method: "POST",
    headers,
    body,
  });
}

/** Import the handler function without invoking Convex runtime */
async function invokeWebhookHandler(
  request: Request,
  secret: string,
): Promise<Response> {
  vi.stubEnv("WEBHOOK_SHARED_SECRET", secret);
  vi.stubEnv("CLANWORLD_USE_REAL_INDEXER", "false");
  // Dynamically import after env stub so process.env is picked up
  const { heartbeatWebhook } = await import("./heartbeat");
  // heartbeatWebhook is an httpAction — call its internal _handler
  const ctx = {} as any;
  return (heartbeatWebhook as any)._handler(ctx, request);
}

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

describe("heartbeatWebhook HMAC signature validation", () => {
  const SECRET = "testsecret1234567890abcdef123456";
  const BODY = JSON.stringify({ txHash: "0x" + "a".repeat(64), source: "test" });

  it("valid HMAC within window → 200", async () => {
    const timestamp = Math.floor(Date.now() / 1000);
    const sig = await signPayload(SECRET, timestamp, BODY);
    const request = makeRequest(sig, BODY);
    const response = await invokeWebhookHandler(request, SECRET);
    expect(response.status).toBe(200);
  });

  it("valid HMAC but timestamp too old (> 60s) → 401 replay window", async () => {
    const timestamp = Math.floor(Date.now() / 1000) - 120; // 2 minutes ago
    const sig = await signPayload(SECRET, timestamp, BODY);
    const request = makeRequest(sig, BODY);
    const response = await invokeWebhookHandler(request, SECRET);
    expect(response.status).toBe(401);
    const json = await response.json() as { error: string };
    expect(json.error).toMatch(/replay window/);
  });

  it("wrong HMAC value → 401 invalid signature", async () => {
    const timestamp = Math.floor(Date.now() / 1000);
    const sig = `t=${timestamp},v1=${"0".repeat(64)}`; // all-zero HMAC
    const request = makeRequest(sig, BODY);
    const response = await invokeWebhookHandler(request, SECRET);
    expect(response.status).toBe(401);
    const json = await response.json() as { error: string };
    expect(json.error).toMatch(/invalid signature/);
  });

  it("missing X-Heartbeat-Signature header → 401 missing signature", async () => {
    const request = makeRequest(null, BODY);
    const response = await invokeWebhookHandler(request, SECRET);
    expect(response.status).toBe(401);
    const json = await response.json() as { error: string };
    expect(json.error).toMatch(/missing signature/);
  });
});
