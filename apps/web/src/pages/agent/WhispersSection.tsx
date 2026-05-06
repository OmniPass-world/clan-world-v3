import { motion, AnimatePresence } from 'framer-motion';
import { agentTokens as t, WHISPER_BURN, SKIP_TAX_PER_FULL_MINUTE } from './agent-tokens';
import { DecryptingSkeleton } from './DecryptingSkeleton';

const WHISPER_LIMIT = 1000;

interface Props {
  decrypting: boolean;
  draft: string;
  onDraftChange: (s: string) => void;
  /** Cooldown remaining in ms. 0 = ready. */
  cooldownMs: number;
  /** Total cooldown window length (used for the bar). */
  cooldownTotalMs: number;
  gold: number;
  sentCount: number;
  /** ms since last send. -1 if no sends yet. */
  msSinceLast: number;
  sending: boolean;
  status: { kind: 'success' | 'error'; body: string } | null;
  onSend: () => void;
}

export function WhispersSection({
  decrypting,
  draft,
  onDraftChange,
  cooldownMs,
  cooldownTotalMs,
  gold,
  sentCount,
  msSinceLast,
  sending,
  status,
  onSend,
}: Props) {
  const cooldownActive = cooldownMs > 0;
  const fullMinutesRemaining = Math.ceil(cooldownMs / 60_000);
  const skipTax = cooldownActive ? fullMinutesRemaining * SKIP_TAX_PER_FULL_MINUTE : 0;
  const totalCost = WHISPER_BURN + skipTax;
  const insufficient = gold < totalCost;
  const empty = draft.trim().length === 0;
  const disabled = sending || empty || insufficient;

  const cooldownPct = cooldownTotalMs > 0 ? (cooldownMs / cooldownTotalMs) * 100 : 0;
  const cooldownLabel = formatCooldown(cooldownMs);

  return (
    <section
      data-testid="whispers-section"
      style={{
        flex: '0 0 40%',
        minHeight: '40%',
        display: 'flex',
        flexDirection: 'column',
        background: `linear-gradient(180deg, ${t.bg.obsidian} 0%, #0a0807 100%)`,
        padding: '8px 12px 10px',
        gap: '6px',
        overflow: 'hidden',
        borderTop: `1px solid ${t.border.iron}`,
      }}
    >
      <div
        style={{
          display: 'flex',
          alignItems: 'baseline',
          gap: '8px',
          minWidth: 0,
        }}
      >
        <span
          style={{
            fontFamily: t.font.rune,
            fontSize: '11px',
            color: t.ember.glow,
            letterSpacing: '0.04em',
            textShadow: `0 0 8px ${t.ember.core}`,
          }}
          aria-hidden
        >
          ᚺᚹᛁ
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
          ÆLDER WHISPERS
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
          — single ear · costs 5 GOLD
        </span>
      </div>

      {/* Textarea */}
      {decrypting ? (
        <DecryptingSkeleton height={108} label="Awakening whisper channel" />
      ) : (
        <div
          style={{
            position: 'relative',
            background: t.bg.iron,
            border: `1px solid ${t.border.iron}`,
            borderRadius: t.radius.md,
            boxShadow: 'inset 0 0 12px rgba(0,0,0,0.4)',
          }}
        >
          <textarea
            data-testid="whisper-input"
            value={draft}
            onChange={(e) => onDraftChange(e.target.value.slice(0, WHISPER_LIMIT))}
            maxLength={WHISPER_LIMIT}
            rows={4}
            placeholder="Send a private message that only this Elder will receive. Used sparingly — burns 5 GOLD."
            style={{
              width: '100%',
              minHeight: '90px',
              maxHeight: '110px',
              background: 'transparent',
              border: 'none',
              outline: 'none',
              resize: 'none',
              padding: '6px 8px 22px',
              color: t.text.primary,
              fontFamily: t.font.body,
              fontSize: '12px',
              lineHeight: 1.45,
              letterSpacing: '0.01em',
              boxSizing: 'border-box',
            }}
            spellCheck={false}
          />
          <span
            data-testid="whisper-char-count"
            style={{
              position: 'absolute',
              right: '8px',
              bottom: '4px',
              fontFamily: t.font.mono,
              fontSize: '9px',
              color: draft.length > WHISPER_LIMIT * 0.9 ? t.gold.bright : t.text.muted,
              letterSpacing: '0.06em',
              fontVariantNumeric: 'tabular-nums',
            }}
          >
            {draft.length} / {WHISPER_LIMIT}
          </span>
        </div>
      )}

      {/* Cooldown bar — hot iron cooling */}
      <div
        data-testid="cooldown-bar"
        data-cooldown-active={cooldownActive}
        style={{
          position: 'relative',
          height: '20px',
          background: t.bg.obsidian,
          border: `1px solid ${cooldownActive ? t.border.ember : t.border.iron}`,
          borderRadius: t.radius.sm,
          overflow: 'hidden',
          boxShadow: cooldownActive
            ? 'inset 0 0 10px rgba(255,107,53,0.2)'
            : 'inset 0 0 8px rgba(0,0,0,0.5)',
        }}
      >
        <motion.div
          data-testid="cooldown-fill"
          animate={{ width: `${cooldownPct}%` }}
          transition={{ duration: 0.45, ease: 'linear' }}
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            bottom: 0,
            background: cooldownActive
              ? `linear-gradient(90deg, ${t.ember.deep} 0%, ${t.ember.core} 60%, ${t.gold.bright} 100%)`
              : `linear-gradient(90deg, ${t.rune.deep}, ${t.rune.core})`,
            boxShadow: cooldownActive
              ? `0 0 14px ${t.ember.core}`
              : `0 0 8px ${t.rune.core}`,
            transition: 'background 280ms ease',
          }}
        />
        <div
          style={{
            position: 'absolute',
            inset: 0,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '0 9px',
            fontFamily: t.font.mono,
            fontSize: '9.5px',
            color: cooldownActive ? '#fff5e8' : t.rune.glow,
            letterSpacing: '0.18em',
            textTransform: 'uppercase',
            fontVariantNumeric: 'tabular-nums',
            mixBlendMode: 'normal',
            textShadow: '0 1px 1px rgba(0,0,0,0.7)',
          }}
        >
          <span>{cooldownActive ? 'Forge Cooling' : 'Whisper Ready'}</span>
          <span data-testid="cooldown-remaining">{cooldownLabel}</span>
        </div>
      </div>

      {/* Cost-preview hero block + send */}
      <div
        data-testid="cost-preview-row"
        style={{
          display: 'grid',
          gridTemplateColumns: 'minmax(0, 1fr) auto',
          gap: '8px',
          alignItems: 'stretch',
        }}
      >
        <CostPreview
          cooldownActive={cooldownActive}
          insufficient={insufficient}
          totalCost={totalCost}
          burn={WHISPER_BURN}
          skipTax={skipTax}
          fullMinutes={fullMinutesRemaining}
        />

        <button
          type="button"
          data-testid="send-btn"
          onClick={onSend}
          disabled={disabled}
          style={{
            position: 'relative',
            padding: '0 16px',
            background: disabled
              ? t.bg.iron
              : cooldownActive
                ? `linear-gradient(180deg, ${t.gold.core}, ${t.gold.deep})`
                : `linear-gradient(180deg, ${t.ember.core}, ${t.ember.deep})`,
            color: disabled
              ? t.text.muted
              : cooldownActive
                ? t.bg.obsidian
                : t.text.onEmber,
            border: `1px solid ${
              disabled
                ? t.border.iron
                : cooldownActive
                  ? t.gold.core
                  : t.ember.core
            }`,
            borderRadius: t.radius.md,
            fontFamily: t.font.display,
            fontSize: '12px',
            letterSpacing: '0.28em',
            fontWeight: 700,
            textTransform: 'uppercase',
            cursor: disabled ? 'not-allowed' : 'pointer',
            boxShadow: disabled
              ? 'none'
              : cooldownActive
                ? '0 0 14px rgba(212,160,74,0.45)'
                : `0 0 18px rgba(255,107,53,0.5)`,
            whiteSpace: 'nowrap',
            transition: 'box-shadow 220ms ease',
          }}
        >
          {sending ? (
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
              <span
                aria-hidden
                style={{
                  display: 'inline-block',
                  width: '11px',
                  height: '11px',
                  borderRadius: '50%',
                  border: `1.5px solid currentColor`,
                  borderTopColor: 'transparent',
                  animation: 'spinFast 0.7s linear infinite',
                }}
              />
              Sending
            </span>
          ) : (
            <span>{cooldownActive ? 'Force Send' : 'Send'}</span>
          )}
        </button>
      </div>

      {/* Footer telemetry */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          fontFamily: t.font.mono,
          fontSize: '9.5px',
          color: t.text.muted,
          letterSpacing: '0.16em',
          textTransform: 'uppercase',
        }}
      >
        <span data-testid="sent-count">
          <span style={{ color: t.text.secondary }}>{sentCount}</span> sent
        </span>
        <span data-testid="last-sent">
          {msSinceLast < 0 ? 'never' : `${formatRelative(msSinceLast)} ago`}
        </span>
      </div>

      <div style={{ minHeight: '14px' }}>
        <AnimatePresence>
          {status && (
            <motion.div
              key={status.body}
              data-testid={`whisper-status-${status.kind}`}
              initial={{ opacity: 0, y: -4 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -4 }}
              style={{
                fontFamily: t.font.mono,
                fontSize: '10px',
                letterSpacing: '0.06em',
                color: status.kind === 'success' ? t.text.success : t.text.danger,
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
    </section>
  );
}

/* ---------------- subcomponents ---------------- */

function CostPreview({
  cooldownActive,
  insufficient,
  totalCost,
  burn,
  skipTax,
  fullMinutes,
}: {
  cooldownActive: boolean;
  insufficient: boolean;
  totalCost: number;
  burn: number;
  skipTax: number;
  fullMinutes: number;
}) {
  // Hero element while cooldown is active.
  const insufficientStyle: React.CSSProperties = insufficient
    ? {
        border: `1px solid ${t.text.danger}`,
        boxShadow: 'inset 0 0 0 1px rgba(255,80,80,0.2)',
      }
    : {};

  return (
    <div
      data-testid="cost-preview"
      data-cooldown-active={cooldownActive}
      style={{
        position: 'relative',
        background: cooldownActive ? `linear-gradient(180deg, ${t.bg.ember} 0%, ${t.bg.iron} 100%)` : t.bg.iron,
        border: `1px solid ${cooldownActive ? t.border.ember : t.border.iron}`,
        borderRadius: t.radius.md,
        padding: '6px 10px',
        display: 'flex',
        flexDirection: 'column',
        gap: '2px',
        justifyContent: 'center',
        minWidth: 0,
        boxShadow: cooldownActive
          ? `0 0 18px rgba(255,107,53,0.18)`
          : 'inset 0 0 0 1px rgba(0,0,0,0.4)',
        ...insufficientStyle,
      }}
    >
      <div
        style={{
          fontFamily: t.font.mono,
          fontSize: '7.5px',
          color: cooldownActive ? t.ember.glow : t.text.muted,
          letterSpacing: '0.32em',
          textTransform: 'uppercase',
        }}
      >
        {insufficient ? 'INSUFFICIENT GOLD' : cooldownActive ? 'Force-send cost' : 'Send cost'}
      </div>
      {insufficient ? (
        <div
          data-testid="cost-insufficient"
          style={{
            fontFamily: t.font.mono,
            fontSize: '11px',
            color: t.text.danger,
            fontWeight: 600,
            fontVariantNumeric: 'tabular-nums',
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
          }}
        >
          need {totalCost.toLocaleString()} GOLD
        </div>
      ) : (
        <div
          style={{
            display: 'flex',
            alignItems: 'baseline',
            gap: '6px',
            fontFamily: t.font.mono,
            fontVariantNumeric: 'tabular-nums',
            whiteSpace: 'nowrap',
            overflow: 'hidden',
          }}
        >
          <span style={{ fontSize: '14px', fontWeight: 700, color: t.gold.bright }}>
            {burn}
          </span>
          <span style={{ fontSize: '11px', color: t.ember.core }}>🔥</span>
          {cooldownActive && (
            <>
              <span style={{ fontSize: '12px', color: t.text.muted }}>+</span>
              <span
                data-testid="cost-skip-tax"
                style={{ fontSize: '14px', fontWeight: 700, color: t.gold.bright }}
              >
                {skipTax.toLocaleString()}
              </span>
              <span
                style={{
                  fontSize: '8.5px',
                  color: t.gold.core,
                  letterSpacing: '0.18em',
                  textTransform: 'uppercase',
                }}
              >
                ➡ Treasury
              </span>
            </>
          )}
        </div>
      )}
      {cooldownActive && !insufficient && (
        <div
          style={{
            fontFamily: t.font.mono,
            fontSize: '8px',
            color: t.text.muted,
            letterSpacing: '0.18em',
            textTransform: 'uppercase',
          }}
        >
          {fullMinutes} min × 1000 GOLD skip-tax
        </div>
      )}
    </div>
  );
}

function formatCooldown(ms: number): string {
  if (ms <= 0) return 'READY';
  const total = Math.ceil(ms / 1000);
  const m = Math.floor(total / 60);
  const s = total % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

function formatRelative(ms: number): string {
  const s = Math.floor(ms / 1000);
  if (s < 60) return `${s}s`;
  const m = Math.floor(s / 60);
  const sr = s % 60;
  return `${m}m ${String(sr).padStart(2, '0')}s`;
}
