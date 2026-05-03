/**
 * IRunnerInbox — contract between the runner daemon and each Elder session.
 *
 * The runner pushes per-tick updates into the Elder's tmux session;
 * the Elder acknowledges consolidation readiness before a context reset.
 * Implementations must be idempotent: delivering the same tick update
 * twice for the same tick must not cause duplicate processing.
 */
export interface IRunnerInbox {
  /**
   * Deliver a per-tick update to the Elder.
   *
   * The block is a short free-text marker that tells the Elder a tick started.
   * Stable game-loop instructions live in the Elder runtime CLAUDE.md/AGENTS.md.
   *
   * Contract:
   * - Must complete (resolve or reject) within the runner's per-Elder delivery timeout.
   * - Must not throw on transient Elder session unavailability — surface via returned status.
   * - Idempotent: re-delivering tick N's block while the Elder is still processing tick N
   *   must be a no-op (same tick, same block = skip).
   */
  deliverSituationBlock(tick: number, block: string, signal?: AbortSignal): Promise<DeliveryStatus>;

  /**
   * Wait for the Elder's ack-clear signal, then trigger a context reset.
   *
   * Flow:
   * 1. Runner sends final-tick warning to Elder.
   * 2. Runner calls waitForAckAndClear(timeout).
   * 3. If Elder calls `elder ack-clear` within timeout: runner issues /clear then bootstraps.
   * 4. If timeout elapses with no ack: runner issues /clear anyway (Elder loses the turn's reasoning).
   *
   * Contract:
   * - timeoutMs is the maximum wait; implementations must not block beyond it.
   * - Returns 'ack' if Elder acknowledged, 'timeout' if not.
   * - Must never deadlock the tick loop.
   */
  waitForAckAndClear(timeoutMs: number): Promise<'ack' | 'timeout'>;
}

export type DeliveryStatus =
  | { ok: true }
  | { ok: false; reason: 'session-down' | 'timeout' | 'duplicate-tick' | 'aborted' };
