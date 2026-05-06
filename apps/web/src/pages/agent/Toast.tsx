import { motion, AnimatePresence } from 'framer-motion';
import { agentTokens as t } from './agent-tokens';

export interface ToastDef {
  id: number;
  kind: 'success' | 'info' | 'error';
  body: string;
}

interface Props {
  toasts: ToastDef[];
}

export function ToastStack({ toasts }: Props) {
  return (
    <div
      data-testid="toast-stack"
      style={{
        position: 'fixed',
        top: 'env(safe-area-inset-top, 8px)',
        left: 0,
        right: 0,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: '6px',
        padding: '10px 12px 0',
        pointerEvents: 'none',
        zIndex: 80,
      }}
    >
      <AnimatePresence>
        {toasts.map((toast) => {
          const kindStyles =
            toast.kind === 'success'
              ? { color: t.text.success, border: `1px solid ${t.text.success}55`, glyph: '✓' }
              : toast.kind === 'error'
                ? { color: t.text.danger, border: `1px solid ${t.text.danger}66`, glyph: '✕' }
                : { color: t.gold.bright, border: `1px solid ${t.gold.deep}`, glyph: '◈' };
          return (
            <motion.div
              key={toast.id}
              data-testid={`toast-${toast.kind}`}
              initial={{ opacity: 0, y: -14, scale: 0.96 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: -10, scale: 0.96 }}
              transition={{ type: 'spring', stiffness: 360, damping: 26 }}
              style={{
                background: t.bg.iron,
                border: kindStyles.border,
                borderRadius: t.radius.md,
                padding: '6px 11px',
                fontFamily: t.font.mono,
                fontSize: '10.5px',
                color: kindStyles.color,
                letterSpacing: '0.06em',
                boxShadow: t.shadow.panel,
                display: 'inline-flex',
                alignItems: 'center',
                gap: '7px',
                maxWidth: 'calc(100% - 24px)',
                pointerEvents: 'auto',
              }}
            >
              <span aria-hidden style={{ opacity: 0.85 }}>
                {kindStyles.glyph}
              </span>
              <span>{toast.body}</span>
            </motion.div>
          );
        })}
      </AnimatePresence>
    </div>
  );
}
