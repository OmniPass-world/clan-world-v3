import { agentTokens as t } from './agent-tokens';

interface Props {
  /** Cinzel uppercase label — e.g. "ÆLDER ESSENCE", "ÆLDER WHISPERS". */
  label: string;
  /** Optional flank glyph (small rune) inserted between the lozenges and label. */
  glyph?: string;
}

/**
 * Section header rule used between vertical zones on the agent page.
 * Matches the slice-1 `.eyebrow` / lozenge pattern: hairline rules on
 * either side, lozenge separators, centred Cinzel small-caps label.
 */
export function OrnamentRule({ label, glyph }: Props) {
  return (
    <div
      role="separator"
      aria-label={label}
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: '12px',
        padding: '4px 0',
        userSelect: 'none',
      }}
    >
      <Side />
      {glyph && (
        <span
          aria-hidden
          style={{
            fontFamily: t.font.rune,
            fontSize: '12px',
            color: t.ember.glow,
            textShadow: `0 0 10px ${t.ember.core}`,
          }}
        >
          {glyph}
        </span>
      )}
      <span
        style={{
          fontFamily: t.font.display,
          fontSize: '11px',
          color: t.text.primary,
          letterSpacing: '0.34em',
          textTransform: 'uppercase',
          fontWeight: 600,
          whiteSpace: 'nowrap',
        }}
      >
        {label}
      </span>
      <Side />
    </div>
  );
}

function Side() {
  return (
    <span
      aria-hidden
      style={{
        flex: 1,
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        color: t.gold.core,
      }}
    >
      <span
        style={{
          flex: 1,
          height: '1px',
          background: t.border.hairlineStrong,
        }}
      />
      <Lozenge />
      <span
        style={{
          width: '14px',
          height: '1px',
          background: t.border.hairlineMid,
        }}
      />
    </span>
  );
}

function Lozenge() {
  return (
    <span
      aria-hidden
      style={{
        width: '5px',
        height: '5px',
        background: 'currentColor',
        transform: 'rotate(45deg)',
      }}
    />
  );
}
