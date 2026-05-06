import { useEffect, useRef, useState } from 'react';
import { agentTokens as t, BURN_TARGET } from './agent-tokens';

interface Props {
  /** Live target — counter animates toward this on change. */
  burned: number;
}

/**
 * Header burn-counter widget.
 *
 *   - Number animates count-up from previous value (eased, ~900ms).
 *   - Tiny furnace-flame SVG to the left, intensity scales with progress.
 *   - Progress micro-bar below: filled portion is hot ember, rest is cold iron.
 *   - On every burn delta, briefly punches a glow halo (ember pulse).
 */
export function BurnCounter({ burned }: Props) {
  const [display, setDisplay] = useState<number>(burned);
  const [pulse, setPulse] = useState(false);
  const fromRef = useRef(burned);

  useEffect(() => {
    fromRef.current = display;
    const target = burned;
    if (target === display) return;

    setPulse(true);
    const start = performance.now();
    const dur = 900;
    let raf = 0;

    const step = (now: number) => {
      const tt = Math.min(1, (now - start) / dur);
      const eased = 1 - Math.pow(1 - tt, 3); // easeOutCubic
      const v = Math.round(fromRef.current + (target - fromRef.current) * eased);
      setDisplay(v);
      if (tt < 1) {
        raf = requestAnimationFrame(step);
      } else {
        setTimeout(() => setPulse(false), 240);
      }
    };
    raf = requestAnimationFrame(step);
    return () => cancelAnimationFrame(raf);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [burned]);

  const pct = Math.min(100, (display / BURN_TARGET) * 100);
  // Flame intensity scales roughly with pct, capped at 1.
  const intensity = Math.min(1, 0.35 + pct / 50);

  return (
    <div
      data-testid="burn-counter"
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: '7px',
        padding: '5px 9px',
        background: t.bg.iron,
        border: `1px solid ${t.border.ember}`,
        borderRadius: t.radius.md,
        boxShadow: pulse ? `0 0 14px ${t.ember.core}` : 'inset 0 0 8px rgba(255,107,53,0.06)',
        transition: 'box-shadow 240ms ease',
        minWidth: 0,
      }}
      title={`${display.toLocaleString()} of ${BURN_TARGET.toLocaleString()} GOLD burned`}
    >
      {/* Furnace flame SVG */}
      <svg
        width="13"
        height="16"
        viewBox="0 0 13 16"
        aria-hidden
        style={{
          filter: `drop-shadow(0 0 ${4 * intensity}px ${t.ember.core})`,
          flexShrink: 0,
        }}
      >
        <defs>
          <linearGradient id="flame-grad" x1="0" y1="1" x2="0" y2="0">
            <stop offset="0%" stopColor={t.ember.deep} />
            <stop offset="55%" stopColor={t.ember.core} />
            <stop offset="100%" stopColor={t.gold.bright} />
          </linearGradient>
        </defs>
        <path
          d="M6.5 0.5 C 7 3, 9.5 4, 9.5 7 C 9.5 9, 8 9, 8 10.5 C 8 11.4, 8.6 12, 8.6 12.6 C 9.5 11.5, 11 10, 11 8 C 11.6 9, 12.2 11, 11.7 12.6 C 11.1 14.6, 8.8 15.5, 6.5 15.5 C 4.2 15.5, 1.9 14.6, 1.3 12.6 C 0.8 11, 1.4 9, 2 8 C 2 10, 3.5 11.5, 4.4 12.6 C 4.4 12, 5 11.4, 5 10.5 C 5 9, 3.5 9, 3.5 7 C 3.5 4, 6 3, 6.5 0.5 Z"
          fill="url(#flame-grad)"
        >
          <animate
            attributeName="opacity"
            values="0.85;1;0.9;1;0.85"
            dur="2.4s"
            repeatCount="indefinite"
          />
        </path>
      </svg>

      <div style={{ display: 'flex', flexDirection: 'column', minWidth: 0, flex: 1 }}>
        <div style={{ display: 'flex', alignItems: 'baseline', gap: '4px' }}>
          <span
            data-testid="burn-counter-value"
            style={{
              fontFamily: t.font.mono,
              fontSize: '11px',
              color: t.ember.glow,
              fontWeight: 600,
              fontVariantNumeric: 'tabular-nums',
              letterSpacing: '0.02em',
            }}
          >
            {display.toLocaleString()}
          </span>
          <span
            style={{
              fontFamily: t.font.mono,
              fontSize: '8.5px',
              color: t.text.muted,
              letterSpacing: '0.04em',
            }}
          >
            / 1B
          </span>
        </div>
        <div
          style={{
            marginTop: '3px',
            height: '2px',
            width: '100%',
            background: t.bg.obsidian,
            borderRadius: '1px',
            overflow: 'hidden',
            boxShadow: 'inset 0 0 0 1px rgba(0,0,0,0.4)',
          }}
        >
          <div
            style={{
              width: `${Math.max(0.5, pct)}%`,
              height: '100%',
              background: `linear-gradient(90deg, ${t.ember.deep}, ${t.ember.core}, ${t.gold.bright})`,
              boxShadow: `0 0 6px ${t.ember.core}`,
              transition: 'width 600ms cubic-bezier(0.22, 1, 0.36, 1)',
            }}
          />
        </div>
        <div
          style={{
            marginTop: '2px',
            fontFamily: t.font.body,
            fontSize: '7.5px',
            color: t.text.muted,
            letterSpacing: '0.22em',
            textTransform: 'uppercase',
          }}
        >
          GOLD burned · {pct.toFixed(2)}%
        </div>
      </div>
    </div>
  );
}
