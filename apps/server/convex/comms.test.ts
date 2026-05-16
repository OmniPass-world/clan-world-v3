import { afterEach, describe, expect, it } from "vitest";
import { sendWhisper } from "./comms";

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
          async first() {
            return rows[0] ?? null;
          },
        };
        return builder;
      },
    },
  };
}

describe("sendWhisper", () => {
  const originalSecret = process.env.INDEXER_SECRET;

  afterEach(() => {
    if (originalSecret === undefined) {
      delete process.env.INDEXER_SECRET;
    } else {
      process.env.INDEXER_SECRET = originalSecret;
    }
  });

  it("dedups matching fromClanId, tick, and msgId", async () => {
    process.env.INDEXER_SECRET = "test-secret";
    const { db, tables } = createDb();
    const args = {
      secret: "test-secret",
      tick: 7,
      fromClanId: 2,
      toClanIds: [3],
      body: "hold the bridge",
      msgId: "2:7:abc",
    };

    await (sendWhisper as any)._handler({ db }, args);
    await (sendWhisper as any)._handler({ db }, args);

    expect(tables.whispers).toHaveLength(1);
  });

  it("allows repeated whispers without msgId", async () => {
    process.env.INDEXER_SECRET = "test-secret";
    const { db, tables } = createDb();
    const args = {
      secret: "test-secret",
      tick: 7,
      fromClanId: 2,
      toClanIds: [3],
      body: "hold the bridge",
    };

    await (sendWhisper as any)._handler({ db }, args);
    await (sendWhisper as any)._handler({ db }, args);

    expect(tables.whispers).toHaveLength(2);
  });
});
