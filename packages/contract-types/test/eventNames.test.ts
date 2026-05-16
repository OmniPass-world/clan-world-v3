import { describe, expect, it } from "vitest";
import {
  IClanWorldAbiEventNames,
  isClanWorldEventName,
  iClanWorldAbi,
} from "../index.js";

describe("IClanWorldAbiEventNames", () => {
  it("size equals number of event entries in the ABI", () => {
    const expectedCount = iClanWorldAbi.filter(
      (item) => item.type === "event",
    ).length;
    expect(IClanWorldAbiEventNames.size).toBe(expectedCount);
    expect(expectedCount).toBeGreaterThan(0);
  });

  it("contains representative event names", () => {
    expect(IClanWorldAbiEventNames.has("TickAdvanced")).toBe(true);
    expect(IClanWorldAbiEventNames.has("ResourcesGathered")).toBe(true);
  });
});

describe("isClanWorldEventName", () => {
  it("returns true for canonical event names", () => {
    expect(isClanWorldEventName("TickAdvanced")).toBe(true);
    expect(isClanWorldEventName("ResourcesDeposited")).toBe(true);
  });

  it("returns false for unknown names", () => {
    expect(isClanWorldEventName("NotAnEvent")).toBe(false);
    expect(isClanWorldEventName("")).toBe(false);
  });

  it("returns false for non-string inputs", () => {
    expect(isClanWorldEventName(undefined)).toBe(false);
    expect(isClanWorldEventName(null)).toBe(false);
    expect(isClanWorldEventName(42)).toBe(false);
    expect(isClanWorldEventName({ name: "TickAdvanced" })).toBe(false);
  });
});
