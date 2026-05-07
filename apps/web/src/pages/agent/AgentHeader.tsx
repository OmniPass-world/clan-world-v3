import { motion, AnimatePresence } from 'framer-motion';
import { agentTokens as t, type AgentDef } from './agent-tokens';
import { BurnCounter } from './BurnCounter';

interface Props {
  agent: AgentDef;
  wallet: string;
  sol: number;
  gold: number;
  burned: number;
  goldBouncing: boolean;
  faucetCooling: boolean;
  onCopyWallet: () => void;
  onFaucet: () => void;
}

function truncate(addr: string): string {
  return `${addr.slice(0, 4)}…${addr.slice(-4)}`;
}

export function AgentHeader({
  agent,
  wallet,
  sol,
  gold,
  burned,
  goldBouncing,
  faucetCooling,
  onCopyWallet,
  onFaucet,
}: Props) {
  return (
    <header
      data-testid="agent-header"
      style={{
        position: 'relative',
        flex: '0 0 20%',
        minHeight: '20%',
        background: `linear-gradient(180deg, #1d1813 0%, ${t.bg.iron} 100%)`,
        borderBottom: `1px solid ${t.border.iron}`,
        boxShadow: t.shadow.panel,
        display: 'flex',
        flexDirection: 'column',
        padding: '8px 12px 6px',
        overflow: 'hidden',
      }}
    >
      <style>{`
        @keyframes goldBounce {
          0%   { transform: translateY(0)   scale(1);   }
          25%  { transform: translateY(-4px) scale(1.18); }
          55%  { transform: translateY(0)   scale(1);   }
          70%  { transform: translateY(-1px) scale(1.04);}
          100% { transform: translateY(0)   scale(1);   }
        }
        @keyframes goldShimmer {
          0%, 100% { color: ${t.gold.core};  text-shadow: 0 0 4px rgba(212,160,74,0.3); }
          50%      { color: ${t.gold.bright}; text-shadow: 0 0 14px rgba(240,193,104,0.7); }
        }
      `}</style>

      {/* Top row: agent identity + iNFT explorer */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          marginBottom: '6px',
          minWidth: 0,
        }}
      >
        <span
          style={{
            fontFamily: t.font.rune,
            fontSize: '22px',
            color: agent.accent,
            textShadow: `0 0 12px ${agent.accent}`,
            lineHeight: 1,
            flexShrink: 0,
          }}
          aria-hidden
        >
          {agent.glyph}
        </span>
        <div style={{ display: 'flex', flexDirection: 'column', minWidth: 0, flex: 1 }}>
          <div
            data-testid="agent-name"
            style={{
              fontFamily: t.font.display,
              fontSize: '14px',
              color: t.text.primary,
              letterSpacing: '0.14em',
              fontWeight: 700,
              textTransform: 'uppercase',
              lineHeight: 1.1,
              whiteSpace: 'nowrap',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
            }}
          >
            {agent.name}
          </div>
          <div
            style={{
              fontFamily: t.font.mono,
              fontSize: '8.5px',
              color: t.text.muted,
              letterSpacing: '0.22em',
              textTransform: 'uppercase',
              lineHeight: 1.2,
              whiteSpace: 'nowrap',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
            }}
          >
            ælder · {String(agent.id).padStart(2, '0')} · {agent.archetype}
          </div>
        </div>

        <a
          href={`https://explorer.0g.ai/inft/${agent.rune}-${agent.id}`}
          target="_blank"
          rel="noopener noreferrer"
          aria-label="Open iNFT in 0G explorer"
          title="0G iNFT explorer"
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '4px',
            padding: '4px 7px',
            background: t.bg.obsidian,
            border: `1px solid ${t.border.rune}`,
            borderRadius: t.radius.sm,
            color: t.rune.core,
            textDecoration: 'none',
            fontFamily: t.font.mono,
            fontSize: '9px',
            letterSpacing: '0.18em',
            textTransform: 'uppercase',
            flexShrink: 0,
          }}
        >
          <span style={{ fontSize: '10px' }}>◉</span>
          <span>0G</span>
          <span style={{ fontSize: '10px', opacity: 0.7 }}>↗</span>
        </a>
      </div>

      {/* Middle row: wallet + balances */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'minmax(0, 1fr) auto auto',
          alignItems: 'center',
          gap: '8px',
          marginBottom: '6px',
        }}
      >
        <button
          type="button"
          data-testid="wallet-copy"
          onClick={onCopyWallet}
          aria-label={`Copy wallet ${wallet}`}
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '5px',
            background: t.bg.obsidian,
            border: `1px solid ${t.border.iron}`,
            borderRadius: t.radius.sm,
            padding: '4px 8px',
            color: t.text.secondary,
            fontFamily: t.font.mono,
            fontSize: '10px',
            letterSpacing: '0.06em',
            cursor: 'pointer',
            minWidth: 0,
            justifySelf: 'start',
          }}
          title={wallet}
        >
          <span aria-hidden style={{ color: t.rune.core, fontSize: '10px' }}>⬢</span>
          <span style={{ overflow: 'hidden', textOverflow: 'ellipsis' }}>{truncate(wallet)}</span>
          <span aria-hidden style={{ color: t.text.muted, fontSize: '9px' }}>⎘</span>
        </button>

        {/* SOL */}
        <div
          data-testid="sol-balance"
          style={{
            display: 'inline-flex',
            alignItems: 'baseline',
            gap: '3px',
            fontFamily: t.font.mono,
            fontSize: '11px',
            color: t.text.primary,
            background: t.bg.obsidian,
            border: `1px solid ${t.border.iron}`,
            borderRadius: t.radius.sm,
            padding: '4px 7px',
            fontVariantNumeric: 'tabular-nums',
          }}
          title={`${sol} SOL`}
        >
          <span style={{ color: '#9c8cf5', fontSize: '9.5px', letterSpacing: '0.06em' }}>◎</span>
          <span style={{ fontWeight: 600 }}>{sol.toFixed(2)}</span>
          <span style={{ color: t.text.muted, fontSize: '8.5px', letterSpacing: '0.16em' }}>SOL</span>
        </div>

        {/* GOLD */}
        <div
          data-testid="gold-balance"
          style={{
            display: 'inline-flex',
            alignItems: 'baseline',
            gap: '3px',
            fontFamily: t.font.mono,
            fontSize: '11px',
            background: t.bg.obsidian,
            border: `1px solid ${t.gold.deep}`,
            borderRadius: t.radius.sm,
            padding: '4px 7px',
            fontVariantNumeric: 'tabular-nums',
          }}
          title={`${gold.toLocaleString()} GOLD`}
        >
          <motion.span
            key={goldBouncing ? `b-${gold}` : 'idle'}
            animate={
              goldBouncing
                ? { scale: [1, 1.25, 0.95, 1.06, 1], y: [0, -6, 0, -1, 0] }
                : { scale: 1, y: 0 }
            }
            transition={{ duration: 0.7, ease: 'easeOut' }}
            style={{
              color: goldBouncing ? t.gold.bright : t.gold.core,
              fontSize: '12px',
              display: 'inline-block',
            }}
          >
            ◉
          </motion.span>
          <span
            style={{
              fontWeight: 700,
              color: goldBouncing ? t.gold.bright : t.gold.core,
              animation: goldBouncing ? 'goldShimmer 0.7s ease-out 1' : undefined,
            }}
          >
            {gold.toLocaleString()}
          </span>
          <span style={{ color: t.text.muted, fontSize: '8.5px', letterSpacing: '0.16em' }}>GLD</span>
        </div>
      </div>

      {/* Bottom row: burn counter + faucet button */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'minmax(0, 1fr) auto',
          alignItems: 'stretch',
          gap: '8px',
          flex: 1,
          minHeight: 0,
        }}
      >
        <BurnCounter burned={burned} />
        <button
          type="button"
          data-testid="faucet-btn"
          onClick={onFaucet}
          disabled={faucetCooling}
          aria-label="Get 10000 GOLD from faucet"
          style={{
            position: 'relative',
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '5px',
            padding: '0 10px',
            background: faucetCooling
              ? t.bg.iron
              : `linear-gradient(180deg, ${t.gold.core}, ${t.gold.deep})`,
            color: faucetCooling ? t.text.muted : t.bg.obsidian,
            border: `1px solid ${faucetCooling ? t.border.iron : t.gold.core}`,
            borderRadius: t.radius.md,
            fontFamily: t.font.display,
            fontSize: '10px',
            letterSpacing: '0.18em',
            fontWeight: 700,
            textTransform: 'uppercase',
            cursor: faucetCooling ? 'not-allowed' : 'pointer',
            boxShadow: faucetCooling ? 'none' : '0 0 12px rgba(212,160,74,0.4)',
            whiteSpace: 'nowrap',
          }}
        >
          <span style={{ fontSize: '13px' }} aria-hidden>🪙</span>
          <span>+10K</span>
          <AnimatePresence>
            {faucetCooling && (
              <motion.span
                key="cool"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                style={{ fontFamily: t.font.mono, fontSize: '8px', letterSpacing: '0.18em' }}
              >
                …
              </motion.span>
            )}
          </AnimatePresence>
        </button>
      </div>
    </header>
  );
}
