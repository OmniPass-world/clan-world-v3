import { agentTokens as t } from './agent-tokens';

interface Props {
  /** Display name of the current agent — "Storm Riders" etc. Renders italic. */
  agentName: string;
  /** Tap handler; defaults to history.back(). */
  onBack?: () => void;
}

/**
 * Top-of-page breadcrumb row. Mirrors slice-1 `.detail-back`:
 *
 *   ◀  HALL  ·  the writ of {agent name}
 *
 * "HALL" in mono uppercase tracked, agent name in script italic. Arrow
 * glyph is gold; the whole row is tappable to go back.
 */
export function DetailBack({ agentName, onBack }: Props) {
  return (
    <button
      type="button"
      data-testid="detail-back"
      onClick={onBack ?? defaultBack}
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: '10px',
        padding: '14px 22px 10px',
        background: 'transparent',
        border: 'none',
        cursor: 'pointer',
        textAlign: 'left',
        width: '100%',
      }}
    >
      <Arrow />
      <span
        style={{
          fontFamily: t.font.mono,
          fontSize: '10px',
          letterSpacing: '0.32em',
          color: t.text.secondary,
          textTransform: 'uppercase',
          fontWeight: 500,
        }}
      >
        Hall
      </span>
      <Sep />
      <span
        style={{
          fontFamily: t.font.script,
          fontStyle: 'italic',
          fontSize: '14px',
          color: t.text.secondary,
          letterSpacing: '0.01em',
        }}
      >
        the writ of {agentName.toLowerCase()}
      </span>
    </button>
  );
}

function defaultBack() {
  if (typeof window !== 'undefined') {
    if (window.history.length > 1) window.history.back();
    else window.location.assign('/');
  }
}

function Arrow() {
  return (
    <svg
      width="14"
      height="14"
      viewBox="0 0 14 14"
      fill="none"
      aria-hidden
      style={{ color: t.gold.core, flexShrink: 0 }}
    >
      <path
        d="M9 11 L4 7 L9 3"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function Sep() {
  return (
    <span
      aria-hidden
      style={{
        color: t.text.muted,
        opacity: 0.6,
        fontFamily: t.font.mono,
        fontSize: '10px',
      }}
    >
      ·
    </span>
  );
}
