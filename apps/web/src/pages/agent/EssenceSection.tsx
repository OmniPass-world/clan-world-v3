import { motion, AnimatePresence } from 'framer-motion';
import { agentTokens as t } from './agent-tokens';
import { DecryptingSkeleton } from './DecryptingSkeleton';
import { RuneDivider } from './RuneDivider';

interface Props {
  decrypting: boolean;
  /** Currently committed strategy (server truth). */
  committedStrategy: string;
  /** Currently committed notes. */
  committedNotes: string;
  /** Live editor strategy. */
  strategy: string;
  /** Live editor notes. */
  notes: string;
  signing: boolean;
  status: { kind: 'success' | 'error'; body: string } | null;
  onStrategyChange: (s: string) => void;
  onNotesChange: (s: string) => void;
  onCommit: () => void;
  onDismissStatus: () => void;
}

const STRATEGY_LIMIT = 800;
const NOTES_LIMIT = 400;

export function EssenceSection({
  decrypting,
  committedStrategy,
  committedNotes,
  strategy,
  notes,
  signing,
  status,
  onStrategyChange,
  onNotesChange,
  onCommit,
  onDismissStatus,
}: Props) {
  const strategyDirty = strategy !== committedStrategy;
  const notesDirty = notes !== committedNotes;
  const anyDirty = strategyDirty || notesDirty;

  return (
    <section
      data-testid="essence-section"
      style={{
        flex: '0 0 40%',
        minHeight: '40%',
        display: 'flex',
        flexDirection: 'column',
        background: `linear-gradient(180deg, ${t.bg.iron} 0%, ${t.bg.obsidian} 100%)`,
        padding: '8px 12px 6px',
        gap: '6px',
        overflow: 'hidden',
      }}
    >
      <SectionLabel
        kana="ÆLDER ESSENCE"
        sub="iNFT · persistent metadata"
        rune="ᚢᚱᛞ"
        accent={t.rune.core}
      />

      {decrypting ? (
        <DecryptingSkeleton height={158} />
      ) : (
        <>
          {/* Strategy */}
          <FieldShell
            label="STRATEGY"
            charCount={strategy.length}
            limit={STRATEGY_LIMIT}
            dirty={strategyDirty}
            testId="strategy-field"
            inputId="essence-strategy"
          >
            <textarea
              id="essence-strategy"
              data-testid="strategy-input"
              value={strategy}
              onChange={(e) => onStrategyChange(e.target.value.slice(0, STRATEGY_LIMIT))}
              maxLength={STRATEGY_LIMIT}
              rows={4}
              disabled={signing}
              placeholder="Tell your Elder what to focus on this season — economy, alliances, defense priorities. They'll read this every time their context resets."
              style={textareaStyle(76, signing)}
              spellCheck={false}
            />
          </FieldShell>

          {/* Notes */}
          <FieldShell
            label="NOTES"
            charCount={notes.length}
            limit={NOTES_LIMIT}
            dirty={notesDirty}
            testId="notes-field"
            inputId="essence-notes"
          >
            <textarea
              id="essence-notes"
              data-testid="notes-input"
              value={notes}
              onChange={(e) => onNotesChange(e.target.value.slice(0, NOTES_LIMIT))}
              maxLength={NOTES_LIMIT}
              rows={3}
              disabled={signing}
              placeholder="Free-form reminders, intel about other clans, debts owed. Anything you want them to remember."
              style={textareaStyle(54, signing)}
              spellCheck={false}
            />
          </FieldShell>
        </>
      )}

      {/* Commit button + status */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          marginTop: 'auto',
        }}
      >
        <button
          type="button"
          data-testid="commit-btn"
          disabled={decrypting || signing || !anyDirty}
          onClick={onCommit}
          style={{
            position: 'relative',
            flex: 1,
            padding: '9px 14px',
            background:
              decrypting || !anyDirty
                ? t.bg.iron
                : signing
                  ? `linear-gradient(180deg, ${t.ember.deep}, #4a1a08)`
                  : `linear-gradient(180deg, ${t.ember.core}, ${t.ember.deep})`,
            color:
              decrypting || !anyDirty
                ? t.text.muted
                : t.text.onEmber,
            border: `1px solid ${anyDirty ? t.ember.core : t.border.iron}`,
            borderRadius: t.radius.md,
            fontFamily: t.font.display,
            fontSize: '12px',
            letterSpacing: '0.28em',
            fontWeight: 700,
            textTransform: 'uppercase',
            cursor: decrypting || signing || !anyDirty ? 'not-allowed' : 'pointer',
            boxShadow: anyDirty && !signing ? `0 0 18px rgba(255,107,53,0.45)` : 'none',
            transition: 'box-shadow 220ms ease',
          }}
        >
          {signing ? (
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}>
              <SpinnerGlyph /> Sealing…
            </span>
          ) : !anyDirty ? (
            <span>Sealed · No Changes</span>
          ) : (
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}>
              <span aria-hidden style={{ fontFamily: t.font.rune, fontSize: '14px' }}>⟢</span>
              Sign to Commit
              <span
                aria-hidden
                style={{
                  fontFamily: t.font.mono,
                  fontSize: '9px',
                  letterSpacing: '0.16em',
                  opacity: 0.85,
                }}
              >
                {[strategyDirty && 'STRAT', notesDirty && 'NOTES'].filter(Boolean).join('·')}
              </span>
            </span>
          )}
        </button>
      </div>

      <div style={{ minHeight: '16px' }}>
        <AnimatePresence>
          {status && (
            <motion.div
              key={status.body}
              data-testid={`status-${status.kind}`}
              initial={{ opacity: 0, y: -4 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -4 }}
              onClick={onDismissStatus}
              style={{
                fontFamily: t.font.mono,
                fontSize: '10px',
                letterSpacing: '0.06em',
                color: status.kind === 'success' ? t.text.success : t.text.danger,
                cursor: 'pointer',
              }}
            >
              <span aria-hidden style={{ marginRight: '6px' }}>
                {status.kind === 'success' ? '✓' : '✕'}
              </span>
              {status.body}
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* Glowing rune-string section break — divides Essence from Whispers */}
      <RuneDivider tone="ember" caption="seal · then whisper" glyph="ᛟ" />
    </section>
  );
}

/* ---------------------------------------------------------------------- */

function SectionLabel({
  kana,
  sub,
  rune,
  accent,
}: {
  kana: string;
  sub: string;
  rune: string;
  accent: string;
}) {
  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'baseline',
        gap: '8px',
        marginBottom: '2px',
        minWidth: 0,
      }}
    >
      <span
        style={{
          fontFamily: t.font.rune,
          fontSize: '11px',
          color: accent,
          letterSpacing: '0.04em',
          textShadow: `0 0 8px ${accent}`,
        }}
        aria-hidden
      >
        {rune}
      </span>
      <span
        style={{
          fontFamily: t.font.display,
          fontSize: '11px',
          color: t.text.primary,
          letterSpacing: '0.34em',
          fontWeight: 700,
          textTransform: 'uppercase',
        }}
      >
        {kana}
      </span>
      <span
        style={{
          fontFamily: t.font.mono,
          fontSize: '8.5px',
          color: t.text.muted,
          letterSpacing: '0.18em',
          textTransform: 'uppercase',
          flex: 1,
          minWidth: 0,
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
        }}
      >
        — {sub}
      </span>
    </div>
  );
}

function FieldShell({
  label,
  charCount,
  limit,
  dirty,
  children,
  testId,
  inputId,
}: {
  label: string;
  charCount: number;
  limit: number;
  dirty: boolean;
  children: React.ReactNode;
  testId: string;
  /** id of the textarea inside `children` — used for <label htmlFor>. */
  inputId: string;
}) {
  const overshoot = charCount > limit * 0.9;
  return (
    <div
      data-testid={testId}
      data-dirty={dirty}
      style={{
        position: 'relative',
        background: t.bg.obsidian,
        border: `1px solid ${dirty ? t.border.ember : t.border.iron}`,
        borderRadius: t.radius.md,
        boxShadow: dirty
          ? `0 0 14px rgba(255,107,53,0.18), inset 0 0 0 1px rgba(255,107,53,0.08)`
          : 'inset 0 0 0 1px rgba(0,0,0,0.4)',
        transition: 'box-shadow 200ms ease, border-color 200ms ease',
      }}
    >
      {/* Top label bar */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '6px',
          padding: '4px 8px 0',
          fontFamily: t.font.mono,
          fontSize: '8.5px',
          letterSpacing: '0.34em',
          color: dirty ? t.ember.glow : t.text.muted,
          textTransform: 'uppercase',
        }}
      >
        <label htmlFor={inputId} style={{ cursor: 'pointer' }}>
          {label}
        </label>
        {dirty && (
          <span
            data-testid="dirty-dot"
            aria-label="unsaved changes"
            style={{
              width: '6px',
              height: '6px',
              borderRadius: '50%',
              background: t.ember.core,
              boxShadow: `0 0 6px ${t.ember.core}`,
            }}
          />
        )}
        <span style={{ flex: 1 }} />
        <span style={{ color: overshoot ? t.gold.bright : t.text.muted }}>
          {charCount}
          <span style={{ color: t.text.muted, opacity: 0.6 }}> / {limit}</span>
        </span>
      </div>
      {children}
    </div>
  );
}

function textareaStyle(minHeight: number, disabled = false): React.CSSProperties {
  return {
    width: '100%',
    minHeight: `${minHeight}px`,
    background: 'transparent',
    border: 'none',
    outline: 'none',
    resize: 'none',
    padding: '6px 8px 8px',
    color: t.text.primary,
    fontFamily: t.font.body,
    fontSize: '12px',
    lineHeight: 1.45,
    letterSpacing: '0.01em',
    boxSizing: 'border-box',
    opacity: disabled ? 0.6 : 1,
    cursor: disabled ? 'not-allowed' : 'text',
  };
}

function SpinnerGlyph() {
  return (
    <span
      aria-hidden
      style={{
        display: 'inline-block',
        width: '12px',
        height: '12px',
        borderRadius: '50%',
        border: `1.5px solid ${t.text.onEmber}`,
        borderTopColor: 'transparent',
        animation: 'spinFast 0.7s linear infinite',
      }}
    />
  );
}
