import { useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { agentTokens as t } from './agent-tokens';

interface Props {
  open: boolean;
  /** Subtitle line under "SIGN MESSAGE" — e.g. "to authenticate" or "to commit essence". */
  caption: string;
  /** The rune/glyph stamped onto the seal (e.g. agent rune). */
  sigil: string;
}

/**
 * Mock SIWS modal — a wax-seal stamp pressing down onto parchment.
 *
 * Distinctive choices:
 *   - Centered circular wax seal that scales in from above with a "stamp"
 *     bounce + simultaneous shadow drop.
 *   - Concentric rune rings rotate slowly while the seal is visible.
 *   - Two-line caption: ritual heading in Cinzel, micro-tracked subtitle.
 *   - No close button — modal auto-resolves (per spec, intentionally
 *     annoying / unstoppable).
 */
export function SignSealModal({ open, caption, sigil }: Props) {
  const dialogRef = useRef<HTMLDivElement | null>(null);
  // Save the previously-focused element BEFORE we steal focus, so we can
  // restore it on close. Refs persist across renders without triggering them.
  const prevFocusRef = useRef<HTMLElement | null>(null);

  useEffect(() => {
    if (open) {
      // Capture currently-focused element (e.g. the button that triggered
      // the modal) so we can return focus to it when the modal closes.
      const active =
        typeof document !== 'undefined' ? (document.activeElement as HTMLElement | null) : null;
      prevFocusRef.current = active;
      // Move focus into the modal so screen readers announce it and
      // keyboard users land on the dialog rather than behind the scrim.
      dialogRef.current?.focus();
    } else {
      // Restore focus to whatever held it before we opened. Guard against
      // stale references — element may have unmounted while modal was open.
      const prev = prevFocusRef.current;
      if (prev && typeof prev.focus === 'function' && document.contains(prev)) {
        prev.focus();
      }
      prevFocusRef.current = null;
    }
  }, [open]);

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          ref={dialogRef}
          data-testid="sign-seal-modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="sign-seal-title"
          tabIndex={-1}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.18 }}
          style={{
            position: 'fixed',
            inset: 0,
            background: `radial-gradient(ellipse at center, rgba(13,10,8,0.78) 0%, rgba(8,7,10,0.95) 70%)`,
            backdropFilter: 'blur(3px)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 100,
            padding: '24px',
            outline: 'none',
          }}
        >
          <style>{`
            @keyframes ringSpin     { from { transform: rotate(0); } to { transform: rotate(360deg); } }
            @keyframes ringSpinRev  { from { transform: rotate(0); } to { transform: rotate(-360deg); } }
            @keyframes sealEmber    { 0%,100% { box-shadow: 0 0 36px ${t.ember.core}, inset 0 4px 12px rgba(0,0,0,0.6); } 50% { box-shadow: 0 0 64px ${t.ember.glow}, inset 0 4px 12px rgba(0,0,0,0.6); } }
          `}</style>

          {/* Outer rotating rune ring */}
          <motion.div
            initial={{ scale: 1.6, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ type: 'spring', stiffness: 240, damping: 18, delay: 0.02 }}
            style={{
              position: 'relative',
              width: '220px',
              height: '220px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <div
              style={{
                position: 'absolute',
                inset: 0,
                borderRadius: '50%',
                border: `1px dashed ${t.border.rune}`,
                animation: 'ringSpin 22s linear infinite',
              }}
            />
            <div
              style={{
                position: 'absolute',
                inset: '14px',
                borderRadius: '50%',
                border: `1px solid ${t.border.ember}`,
                animation: 'ringSpinRev 14s linear infinite',
              }}
            />

            {/* Central wax seal */}
            <motion.div
              initial={{ scale: 2.2, y: -40, opacity: 0 }}
              animate={{ scale: 1, y: 0, opacity: 1 }}
              transition={{ type: 'spring', stiffness: 320, damping: 14, delay: 0.06 }}
              style={{
                width: '128px',
                height: '128px',
                borderRadius: '50%',
                background: `radial-gradient(circle at 35% 30%, ${t.ember.glow} 0%, ${t.ember.core} 30%, ${t.ember.deep} 70%, #4a1a08 100%)`,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: t.text.onEmber,
                fontFamily: t.font.rune,
                fontSize: '46px',
                lineHeight: 1,
                animation: 'sealEmber 2.6s ease-in-out infinite',
                border: `2px solid ${t.ember.deep}`,
                userSelect: 'none',
                position: 'relative',
              }}
            >
              <span style={{ textShadow: '0 1px 2px rgba(0,0,0,0.5)' }}>{sigil}</span>
            </motion.div>
          </motion.div>

          {/* Caption block */}
          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.18, duration: 0.32 }}
            style={{
              position: 'absolute',
              bottom: '22%',
              left: 0,
              right: 0,
              textAlign: 'center',
              padding: '0 24px',
            }}
          >
            <div
              id="sign-seal-title"
              style={{
                fontFamily: t.font.display,
                fontSize: '18px',
                color: t.text.primary,
                letterSpacing: '0.32em',
                fontWeight: 700,
                textTransform: 'uppercase',
                marginBottom: '8px',
                textShadow: `0 0 14px ${t.ember.core}`,
              }}
            >
              Sign Message
            </div>
            <div
              style={{
                fontFamily: t.font.body,
                fontSize: '11px',
                color: t.text.secondary,
                letterSpacing: '0.28em',
                textTransform: 'uppercase',
              }}
            >
              {caption}
            </div>
            <div
              style={{
                marginTop: '16px',
                fontFamily: t.font.mono,
                fontSize: '9px',
                color: t.rune.core,
                letterSpacing: '0.12em',
                opacity: 0.7,
              }}
            >
              awaiting wallet seal…
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
