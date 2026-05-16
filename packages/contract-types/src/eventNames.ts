import { iClanWorldAbi, type IClanWorldAbiEventName } from "./IClanWorld.js";

// Allowlist derived once at module load from the generated ABI const.
// `iClanWorldAbi` is `as const`, so the literal-union IClanWorldAbiEventName
// is the compile-time source of truth; this Set is its runtime mirror.
export const IClanWorldAbiEventNames: ReadonlySet<IClanWorldAbiEventName> =
  new Set(
    iClanWorldAbi
      .filter(
        (item): item is typeof item & { type: "event"; name: string } =>
          item.type === "event" &&
          typeof (item as { name?: unknown }).name === "string",
      )
      .map((item) => item.name as IClanWorldAbiEventName),
  );

export function isClanWorldEventName(
  name: unknown,
): name is IClanWorldAbiEventName {
  return (
    typeof name === "string" &&
    (IClanWorldAbiEventNames as ReadonlySet<string>).has(name)
  );
}
