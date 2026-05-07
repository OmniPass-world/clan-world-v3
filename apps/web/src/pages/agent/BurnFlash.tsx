import { AnimatePresence, motion } from 'framer-motion';
import { useEffect, useState } from 'react';
import { agentTokens as t } from './agent-tokens';

interface Props {
  /** When this number changes, a flash mounts. Pass `lastBurnAt` (ms) or
   *  `sentCount` — anything that increments per send. */
  triggerKey: number;
  /** Amount of GOLD burned in the most recent send. */
  amount: number;
  /** Total flash visibility duration in ms. Defaults to 2800. */
  ttlMs?: number;
}

/**
 * Ephemeral burn-counter flash.
 *
 * Replaces the previous persistent BurnCounter widget that ticked a fake
 * total upward. This component renders only briefly after each successful
 * whisper send: scale-in + flicker, hold, fade out — total visible window
 * defaults to ~2.8s.
 */
export function BurnFlash({ triggerKey, amount, ttlMs = 2800 }: Props) {
  // Mount → unmount cycle keyed off `triggerKey`. The key resets whenever
  // the parent reports a new send.
  const [shown, setShown] = useState(false);

  useEffect(() => {
    if (triggerKey === 0) return; // skip initial mount
    setShown(true);
    const id = window.setTimeout(() => setShown(false), ttlMs);
    return () => window.clearTimeout(id);
  }, [triggerKey, ttlMs]);

  return (
    <div
      style={{
        position: 'relative',
        height: '28px',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        pointerEvents: 'none',
      }}
    >
      <AnimatePresence>
        {shown && (
          <motion.span
            data-testid="burn-flash"
            data-amount={amount}
            initial={{ opacity: 0, scale: 0.85, y: 4 }}
            animate={{
              opacity: [0, 1, 1, 0.85, 1, 0.92, 1],
              scale: [0.85, 1.06, 1, 1.02, 1],
              y: 0,
            }}
            exit={{ opacity: 0, y: -4 }}
            transition={{
              duration: ttlMs / 1000,
              times: [0, 0.05, 0.55, 0.62, 0.68, 0.74, 0.85],
              ease: 'easeOut',
            }}
            style={{
              display: 'inline-flex',
              alignItems: 'baseline',
              gap: '6px',
              fontFamily: t.font.mono,
              fontSize: '11px',
              color: t.ember.glow,
              letterSpacing: '0.32em',
              textTransform: 'uppercase',
              textShadow: `0 0 14px ${t.ember.core}, 0 0 4px ${t.ember.core}`,
              fontVariantNumeric: 'tabular-nums',
            }}
          >
            <span aria-hidden style={{ fontSize: '13px' }}>🔥</span>
            <span style={{ color: t.gold.bright, fontWeight: 700, fontSize: '12px' }}>
              +{amount.toLocaleString()}
            </span>
            <span>burned</span>
          </motion.span>
        )}
      </AnimatePresence>
    </div>
  );
}
