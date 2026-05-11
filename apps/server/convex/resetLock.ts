export function resetLocked(): boolean {
  return process.env.CLANWORLD_RESET_LOCK === "true";
}
