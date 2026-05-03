import type { ElderId } from './types';

export interface ComposeArgs {
  elder: ElderId;
  clanId: string;
  tick: number;
}

export const CONTEXT_RESET_INTERVAL_TICKS = 10;

export function isContextResetWarningTick(tick: number): boolean {
  return tick % CONTEXT_RESET_INTERVAL_TICKS === CONTEXT_RESET_INTERVAL_TICKS - 1;
}

export function isContextResetTick(tick: number): boolean {
  return tick % CONTEXT_RESET_INTERVAL_TICKS === 0;
}

/**
 * Compose the per-tick update for a single Elder.
 *
 * Stable instructions live in the Elder runtime's CLAUDE.md/AGENTS.md. The
 * runner only nudges the live session when a tick starts.
 */
export function composeSituationBlock(args: ComposeArgs): string {
  const lines: string[] = [];
  lines.push(`TICK ${args.tick} Started`);

  if (isContextResetWarningTick(args.tick)) {
    lines.push(
      'warning: message history is about to be erased. Save important continuity with `elder memory save`.',
    );
  } else if (isContextResetTick(args.tick)) {
    lines.push(
      'warning: final tick before message history is erased. Save important continuity with `elder memory save`, then call `elder ack-clear` when done.',
    );
  }

  return lines.join('\n');
}
