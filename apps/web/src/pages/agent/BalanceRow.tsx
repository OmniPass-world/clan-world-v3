import { motion } from 'framer-motion';
import { agentTokens as t, FAUCET_DROP } from './agent-tokens';

interface Props {
  /** Current GOLD balance in whole units. */
  gold: number;
  /** When true, the GOLD value bounces (after a faucet drop). */
  bouncing: boolean;
  /** When true, the faucet button shows a cooling state (post-tap). */
  faucetCooling: boolean;
  /** Tap handler for the faucet button. */
  onFaucet: () => void;
}

/**
 * The row beneath the chat input.
 *
 *   GOLD · 12,450 g                 [+ MINT 10K · DEVNET ONLY]
 *
 * Left: GOLD balance (mono, with inline ◈ glyph). Bounces on faucet drop.
 * Right: small secondary button — emphatically marked DEVNET so a user
 * can't mistake it for a real economic action. Cyan rune-tone (the
 * project's "AI / non-canonical" signal) + dashed border drives that
 * point home.
 */
export function BalanceRow({ gold, bouncing, faucetCooling, onFaucet }: Props) {
  return (
    <div
      data-testid="balance-row"
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: '12px',
        padding: '4px 4px 0',
      }}
    >
      <motion.span
        data-testid="gold-balance"
        animate={bouncing ? { scale: [1, 1.12, 1], y: [0, -3, 0] } : { scale: 1, y: 0 }}
        transition={{ duration: 0.5, ease: 'easeOut' }}
        style={{
          display: 'inline-flex',
          alignItems: 'baseline',
          gap: '8px',
          fontFamily: t.font.mono,
          fontSize: '12px',
          letterSpacing: '0.18em',
          color: t.text.secondary,
          textTransform: 'uppercase',
        }}
      >
        <span style={{ fontSize: '14px', color: t.gold.bright, lineHeight: 1 }} aria-hidden>◈</span>
        <span>Gold</span>
        <span aria-hidden style={{ color: t.text.muted, opacity: 0.7 }}>·</span>
        <span
          style={{
            fontSize: '14px',
            fontWeight: 600,
            color: t.gold.bright,
            letterSpacing: '0.04em',
            fontVariantNumeric: 'tabular-nums',
          }}
        >
          {gold.toLocaleString()} g
        </span>
      </motion.span>

      <DevnetFaucetButton onClick={onFaucet} cooling={faucetCooling} />
    </div>
  );
}

function DevnetFaucetButton({
  onClick,
  cooling,
}: {
  onClick: () => void;
  cooling: boolean;
}) {
  return (
    <button
      type="button"
      data-testid="devnet-faucet-btn"
      data-cooling={cooling}
      onClick={cooling ? undefined : onClick}
      disabled={cooling}
      title="Mint testnet GOLD — devnet only, not redeemable on mainnet."
      style={{
        display: 'inline-flex',
        flexDirection: 'column',
        alignItems: 'flex-end',
        gap: '1px',
        padding: '6px 12px',
        background: 'transparent',
        border: `1px dashed ${cooling ? t.rune.deep : 'rgba(95,197,212,0.45)'}`,
        borderRadius: '4px',
        color: cooling ? t.rune.deep : t.rune.core,
        fontFamily: t.font.mono,
        cursor: cooling ? 'progress' : 'pointer',
        flex: '0 0 auto',
        transition: 'border-color 220ms ease, color 220ms ease, background 220ms ease',
      }}
      onMouseEnter={(e) => {
        if (cooling) return;
        const el = e.currentTarget;
        el.style.borderStyle = 'solid';
        el.style.borderColor = t.rune.glow;
        el.style.background = 'rgba(95,197,212,0.06)';
      }}
      onMouseLeave={(e) => {
        const el = e.currentTarget;
        el.style.borderStyle = 'dashed';
        el.style.borderColor = cooling ? t.rune.deep : 'rgba(95,197,212,0.45)';
        el.style.background = 'transparent';
      }}
    >
      <span
        style={{
          fontSize: '10px',
          letterSpacing: '0.18em',
          textTransform: 'uppercase',
          fontWeight: 600,
        }}
      >
        + Mint {(FAUCET_DROP / 1000).toFixed(0)}K
      </span>
      <span
        style={{
          fontSize: '8px',
          letterSpacing: '0.32em',
          textTransform: 'uppercase',
          fontWeight: 600,
          textDecoration: 'underline',
          textUnderlineOffset: '2px',
          textDecorationColor: t.rune.glow,
          color: cooling ? t.rune.deep : t.rune.glow,
        }}
      >
        Devnet only
      </span>
    </button>
  );
}
