import { motion } from 'framer-motion';
import { useState } from 'react';
import {
  agentTokens as t,
  WHISPER_BURN,
  SKIP_TAX_PER_FULL_MINUTE,
} from './agent-tokens';
import { useNow } from './useNow';

const WHISPER_LIMIT = 1000;

interface Props {
  /** Multi-line draft text — controlled by the parent. */
  draft: string;
  onDraftChange: (s: string) => void;
  /** Wall-clock ms when the last whisper was sent. -1 if no sends yet. */
  lastSentAt: number;
  /** Total cooldown window length (used for the bar). */
  cooldownTotalMs: number;
  /** User's current GOLD balance — gates the send button. */
  gold: number;
  /** Network call in flight. */
  sending: boolean;
  /** Send handler. */
  onSend: () => void;
}

/**
 * LLM-style chat input — single rounded rectangle with the textarea up top
 * and the cooldown chip + send button packed inside the bottom edge.
 *
 *  ┌─────────────────────────────────────────┐
 *  │ Whisper to your Ælder…                  │
 *  │                                         │
 *  │  [▓▓▓░░ FORGE COOLING 1:42]   [⟢ SEND] │
 *  └─────────────────────────────────────────┘
 *
 * The box itself is iron-2 with a subtle hairline border and an ember
 * glow on focus. No external cooldown bar, no external cost preview —
 * cost info lives inline inside the cooldown chip.
 */
export function ChatInput({
  draft,
  onDraftChange,
  lastSentAt,
  cooldownTotalMs,
  gold,
  sending,
  onSend,
}: Props) {
  const now = useNow(250);
  const [focused, setFocused] = useState(false);

  const cooldownMs =
    lastSentAt < 0 ? 0 : Math.max(0, cooldownTotalMs - (now - lastSentAt));
  const cooldownActive = cooldownMs > 0;
  const fullMinutes = Math.ceil(cooldownMs / 60_000);
  const skipTax = cooldownActive ? fullMinutes * SKIP_TAX_PER_FULL_MINUTE : 0;
  const totalCost = WHISPER_BURN + skipTax;
  const insufficient = gold < totalCost;
  const empty = draft.trim().length === 0;
  const disabled = sending || empty || insufficient;

  const cooldownPct = cooldownTotalMs > 0 ? (cooldownMs / cooldownTotalMs) * 100 : 0;
  const cooldownLabel = formatCooldown(cooldownMs);

  // Border + glow shift between three states: idle, focused, error.
  const borderColor = insufficient
    ? t.text.danger
    : focused
      ? t.ember.core
      : t.border.hairlineStrong;
  const focusGlow = focused
    ? insufficient
      ? `0 0 0 4px rgba(255,80,80,0.18), 0 0 16px rgba(255,80,80,0.20)`
      : `0 0 0 4px rgba(255,107,53,0.18), 0 0 18px rgba(255,107,53,0.22)`
    : 'none';

  return (
    <div
      data-testid="chat-input"
      data-cooldown-active={cooldownActive}
      data-insufficient={insufficient}
      data-focused={focused}
      style={{
        position: 'relative',
        background: t.bg.iron,
        border: `1px solid ${borderColor}`,
        borderRadius: '22px',
        padding: '14px 14px 12px 14px',
        boxShadow: focusGlow,
        transition: 'border-color 200ms ease, box-shadow 220ms ease',
      }}
    >
      <textarea
        data-testid="whisper-input"
        value={draft}
        onChange={(e) => onDraftChange(e.target.value.slice(0, WHISPER_LIMIT))}
        onFocus={() => setFocused(true)}
        onBlur={() => setFocused(false)}
        maxLength={WHISPER_LIMIT}
        rows={3}
        aria-label="Whisper message to elder"
        placeholder="Whisper to your Ælder…"
        spellCheck={false}
        style={{
          display: 'block',
          width: '100%',
          minHeight: '64px',
          maxHeight: '160px',
          background: 'transparent',
          border: 'none',
          outline: 'none',
          resize: 'none',
          padding: '0 4px',
          color: t.text.primary,
          fontFamily: t.font.body,
          fontSize: '15px',
          lineHeight: 1.5,
          letterSpacing: '0.005em',
          boxSizing: 'border-box',
        }}
      />

      {/* Bottom row — cooldown chip (left, flex) + send button (right). */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '10px',
          marginTop: '8px',
        }}
      >
        <CooldownChip
          cooldownActive={cooldownActive}
          cooldownPct={cooldownPct}
          label={cooldownLabel}
          skipTax={skipTax}
          insufficient={insufficient}
          totalCost={totalCost}
        />
        <SendButton
          cooldownActive={cooldownActive}
          disabled={disabled}
          sending={sending}
          onSend={onSend}
        />
      </div>
    </div>
  );
}

function CooldownChip({
  cooldownActive,
  cooldownPct,
  label,
  skipTax,
  insufficient,
  totalCost,
}: {
  cooldownActive: boolean;
  cooldownPct: number;
  label: string;
  skipTax: number;
  insufficient: boolean;
  totalCost: number;
}) {
  return (
    <div
      data-testid="cooldown-chip"
      data-cooldown-active={cooldownActive}
      style={{
        position: 'relative',
        flex: 1,
        height: '34px',
        background: cooldownActive ? t.bg.ember : t.bg.ironRaised,
        border: `1px solid ${cooldownActive ? t.border.ember : t.border.iron}`,
        borderRadius: '17px',
        overflow: 'hidden',
        boxShadow: cooldownActive
          ? `inset 0 0 12px rgba(255,107,53,0.25)`
          : `inset 0 0 8px rgba(0,0,0,0.45)`,
      }}
    >
      {/* Drain bar — fills the chip, drains right-to-left as time ticks. */}
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
            : 'transparent',
          opacity: cooldownActive ? 0.45 : 0,
          boxShadow: cooldownActive ? `0 0 10px ${t.ember.core}` : 'none',
        }}
      />

      {/* Foreground label — varies by state. */}
      <div
        style={{
          position: 'relative',
          height: '100%',
          padding: '0 14px',
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          fontFamily: t.font.mono,
          fontSize: '10px',
          letterSpacing: '0.22em',
          color: cooldownActive ? '#fff5e8' : t.text.secondary,
          textTransform: 'uppercase',
          textShadow: cooldownActive ? '0 1px 2px rgba(0,0,0,0.6)' : 'none',
          fontVariantNumeric: 'tabular-nums',
          minWidth: 0,
        }}
      >
        <span
          aria-hidden
          style={{
            display: 'inline-block',
            width: '6px',
            height: '6px',
            borderRadius: '50%',
            background: cooldownActive ? t.ember.core : t.gold.bright,
            boxShadow: cooldownActive
              ? `0 0 8px ${t.ember.core}`
              : `0 0 6px ${t.gold.bright}`,
            flexShrink: 0,
          }}
        />
        {insufficient ? (
          <span
            data-testid="cost-insufficient"
            style={{ color: t.text.danger, fontWeight: 600, letterSpacing: '0.18em' }}
          >
            Need {totalCost.toLocaleString()} g
          </span>
        ) : cooldownActive ? (
          <>
            <span>Forge cooling</span>
            <span aria-hidden style={{ opacity: 0.5 }}>·</span>
            <span style={{ color: '#fff5e8', fontWeight: 600 }}>{label}</span>
            <span
              aria-hidden
              style={{
                marginLeft: 'auto',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '4px',
                fontSize: '9px',
                letterSpacing: '0.18em',
                color: t.gold.bright,
                opacity: 0.92,
                whiteSpace: 'nowrap',
              }}
              data-testid="cost-skip-tax"
            >
              + {skipTax.toLocaleString()} g skip-tax
            </span>
          </>
        ) : (
          <>
            <span>Whisper ready</span>
            <span aria-hidden style={{ opacity: 0.5 }}>·</span>
            <span style={{ color: t.gold.bright, fontWeight: 600 }}>5 g per send</span>
          </>
        )}
      </div>
    </div>
  );
}

function SendButton({
  cooldownActive,
  disabled,
  sending,
  onSend,
}: {
  cooldownActive: boolean;
  disabled: boolean;
  sending: boolean;
  onSend: () => void;
}) {
  // Shimmer sweep + ember glow when ready; gold halo for force-send.
  return (
    <button
      type="button"
      data-testid="send-btn"
      data-cooldown-active={cooldownActive}
      onClick={onSend}
      disabled={disabled}
      aria-label={cooldownActive ? 'Force-send whisper' : 'Send whisper'}
      style={{
        position: 'relative',
        width: '44px',
        height: '34px',
        background: disabled
          ? t.bg.ironRaised
          : cooldownActive
            ? `linear-gradient(180deg, ${t.gold.core}, ${t.gold.deep})`
            : `linear-gradient(180deg, ${t.ember.core}, ${t.ember.deep})`,
        border: `1px solid ${
          disabled
            ? t.border.iron
            : cooldownActive
              ? t.gold.core
              : t.ember.core
        }`,
        borderRadius: '17px',
        color: disabled
          ? t.text.muted
          : cooldownActive
            ? t.bg.obsidian
            : t.text.onEmber,
        cursor: disabled ? 'not-allowed' : 'pointer',
        boxShadow: disabled
          ? 'none'
          : cooldownActive
            ? '0 0 14px rgba(212,160,74,0.45), inset 0 1px 0 rgba(255,210,140,0.45)'
            : `0 0 18px rgba(255,107,53,0.50), inset 0 1px 0 rgba(255,200,140,0.50)`,
        flexShrink: 0,
        overflow: 'hidden',
        transition: 'box-shadow 220ms ease, transform 120ms ease',
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      {/* Shimmer sweep — only when ready */}
      {!disabled && !sending && (
        <span
          aria-hidden
          style={{
            position: 'absolute',
            inset: 0,
            background:
              'linear-gradient(120deg, transparent, rgba(255,255,255,0.22), transparent)',
            transform: 'translateX(-100%)',
            animation: 'agentSendShimmer 4.5s ease-in-out infinite',
          }}
        />
      )}
      {sending ? (
        <span
          aria-hidden
          style={{
            display: 'inline-block',
            width: '13px',
            height: '13px',
            borderRadius: '50%',
            border: `1.5px solid currentColor`,
            borderTopColor: 'transparent',
            animation: 'spinFast 0.7s linear infinite',
          }}
        />
      ) : (
        <span
          style={{
            position: 'relative',
            fontFamily: t.font.display,
            fontSize: '15px',
            fontWeight: 700,
            lineHeight: 1,
          }}
          aria-hidden
        >
          {cooldownActive ? '⟡' : '⟢'}
        </span>
      )}
    </button>
  );
}

function formatCooldown(ms: number): string {
  if (ms <= 0) return '';
  const total = Math.ceil(ms / 1000);
  const m = Math.floor(total / 60);
  const s = total % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}
