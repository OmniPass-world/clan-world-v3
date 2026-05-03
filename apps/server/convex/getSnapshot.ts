import { query } from "./_generated/server";
import {
  HEARTBEAT_INTERVAL_SECONDS,
  WINTER_START_TICK,
  WINTER_DURATION_TICKS,
  WINTER_PERIOD_TICKS,
  SEASON_DURATION_TICKS,
} from "@clan-world/shared/generated/constants";

const SEASON_LEN = Number(SEASON_DURATION_TICKS);
const WINTER_START = Number(WINTER_START_TICK);
const WINTER_DUR = Number(WINTER_DURATION_TICKS);
const WINTER_PERIOD = Number(WINTER_PERIOD_TICKS);

/**
 * Derive season + winter state from `tick` per `LibSeason.sol`. Pure
 * computation — no chain or DB read required. Surfaces the four fields the
 * `<TopHud>` component reads (`seasonStartTick`, `seasonEndTick`,
 * `winterActive`, `winterStartsAtTick`) so the live HUD reflects on-chain
 * state without a separate facet view.
 */
function deriveSeasonState(tick: number): {
  seasonStartTick: number;
  seasonEndTick: number;
  winterActive: boolean;
  winterStartsAtTick: number | null;
} {
  const seasonStartTick = Math.floor(tick / SEASON_LEN) * SEASON_LEN;
  const seasonEndTick = seasonStartTick + SEASON_LEN;

  // Winter cycles after WINTER_START. Mirrors LibSeason.isWinterActive():
  // active when `(tick - WINTER_START) % WINTER_PERIOD < WINTER_DURATION`,
  // and only after the first window has opened.
  let winterActive = false;
  let winterStartsAtTick: number | null = null;
  if (tick >= WINTER_START) {
    const elapsed = tick - WINTER_START;
    const cycleIndex = Math.floor(elapsed / WINTER_PERIOD);
    const cycleOffset = elapsed % WINTER_PERIOD;
    const cycleStart = WINTER_START + cycleIndex * WINTER_PERIOD;
    winterActive = cycleOffset < WINTER_DUR;
    winterStartsAtTick = winterActive ? cycleStart : cycleStart + WINTER_PERIOD;
  } else {
    winterStartsAtTick = WINTER_START;
  }

  return { seasonStartTick, seasonEndTick, winterActive, winterStartsAtTick };
}

export const getSnapshot = query({
  handler: async (ctx) => {
    const snap = await ctx.db.query("worldSnapshot").order("desc").first();
    if (!snap) {
      return {
        tick: 0,
        tickEpoch: { startedAt: 0, durationMs: Number(HEARTBEAT_INTERVAL_SECONDS) * 1000 },
        regions: [],
        clans: [],
        ...deriveSeasonState(0),
      };
    }
    return {
      tick: snap.tick,
      tickEpoch: {
        startedAt: snap.tickEpochStartedAt,
        durationMs: snap.tickEpochDurationMs,
      },
      regions: snap.regions,
      clans: snap.clans,
      ...deriveSeasonState(snap.tick),
    };
  },
});
