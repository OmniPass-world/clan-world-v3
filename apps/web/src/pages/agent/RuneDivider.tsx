import { agentTokens as t } from './agent-tokens';

interface Props {
  /** Tone — 'ember' for committed/active, 'rune' for cool/idle. Default 'rune'. */
  tone?: 'ember' | 'rune';
  /** Optional glyph centered on the divider — defaults to a forge sigil. */
  glyph?: string;
  /** Optional caption below the divider, all-caps, micro-tracked. */
  caption?: string;
}

/**
 * Signature horizontal divider — a glowing rune-string etched into iron.
 *
 * Two animated linear gradients on either side of a centered sigil. The
 * keyframes drift the gradient's bright spot from outside-in so the divider
 * looks like it's slowly being "lit" from the center. CSS-only.
 */
export function RuneDivider({ tone = 'rune', glyph = '◈', caption }: Props) {
  const accent = tone === 'ember' ? t.ember.core : t.rune.core;
  const accentDeep = tone === 'ember' ? t.ember.deep : t.rune.deep;

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        padding: '6px 0 4px',
      }}
      aria-hidden
    >
      <style>{`
        @keyframes runeDriftL {
          0%, 100% { background-position: 0% 50%; opacity: 0.7; }
          50%      { background-position: 100% 50%; opacity: 1; }
        }
        @keyframes runeDriftR {
          0%, 100% { background-position: 100% 50%; opacity: 0.7; }
          50%      { background-position: 0% 50%; opacity: 1; }
        }
        @keyframes sigilPulse {
          0%, 100% { text-shadow: 0 0 6px ${accent}, 0 0 14px ${accentDeep}; }
          50%      { text-shadow: 0 0 14px ${accent}, 0 0 28px ${accent}; }
        }
      `}</style>
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          width: '100%',
          gap: '10px',
        }}
      >
        <div
          style={{
            flex: 1,
            height: '1px',
            background: `linear-gradient(90deg, transparent 0%, ${accentDeep} 40%, ${accent} 70%, transparent 100%)`,
            backgroundSize: '200% 100%',
            animation: 'runeDriftL 5.6s ease-in-out infinite',
          }}
        />
        <span
          style={{
            color: accent,
            fontFamily: t.font.rune,
            fontSize: '14px',
            lineHeight: 1,
            animation: 'sigilPulse 4s ease-in-out infinite',
            letterSpacing: '0.04em',
          }}
        >
          {glyph}
        </span>
        <div
          style={{
            flex: 1,
            height: '1px',
            background: `linear-gradient(90deg, transparent 0%, ${accent} 30%, ${accentDeep} 60%, transparent 100%)`,
            backgroundSize: '200% 100%',
            animation: 'runeDriftR 5.6s ease-in-out infinite',
          }}
        />
      </div>
      {caption && (
        <div
          style={{
            marginTop: '5px',
            fontFamily: t.font.body,
            color: tone === 'ember' ? t.ember.glow : t.rune.glow,
            fontSize: '8.5px',
            letterSpacing: '0.34em',
            textTransform: 'uppercase',
            opacity: 0.78,
          }}
        >
          {caption}
        </div>
      )}
    </div>
  );
}
