import { useCallback, useEffect, useRef, useState } from 'react';
import {
  agentTokens as t,
  AGENTS,
  findAgent,
  type AgentDef,
  INITIAL_BURN_COUNT,
  INITIAL_SOL,
  INITIAL_GOLD,
  MOCK_WALLET,
  MOCK_ESSENCE,
  WHISPER_BURN,
  WHISPER_COOLDOWN_MS,
  SKIP_TAX_PER_FULL_MINUTE,
  FAUCET_DROP,
} from './agent-tokens';
import { ConnectGate } from './ConnectGate';
import { AgentHeader } from './AgentHeader';
import { EssenceSection } from './EssenceSection';
import { WhispersSection } from './WhispersSection';
import { SignSealModal } from './SignSealModal';
import { ToastStack, type ToastDef } from './Toast';
import { useTimeouts } from './useTimeouts';

/**
 * Single-Agent Control Page.
 *
 * Three-zone vertical layout (mobile-portrait first, 100dvh):
 *
 *   ┌──────────────────────────────────┐  20%  Header
 *   │ wallet · sol · gold · burn · 🪙  │
 *   ├──────────────────────────────────┤  40%  Ælder Essence (iNFT metadata)
 *   │ Strategy textarea                │
 *   │ Notes    textarea                │
 *   │ [ Sign to Commit ]               │
 *   ├ ◈ rune divider ──────────────────┤
 *   │                                  │  40%  Ælder Whispers (chat)
 *   │ Whisper textarea                 │
 *   │ ▰▰▰▰▰▰▰▱▱▱  cooldown bar       │
 *   │ cost · [ Send ]                  │
 *   └──────────────────────────────────┘
 *
 * Auth: any tap on Connect = mock-success after 800ms wax-seal animation.
 * Mock ONLY — no real wallet, no Convex queries.
 */

interface Props {
  agentId: number;
}

type AuthState = 'gated' | 'gateConnecting' | 'decrypting' | 'live';

export function AgentControlPage({ agentId }: Props) {
  const agent = findAgent(agentId);

  // Invalid agent id — show graceful 404.
  if (!agent) {
    return <NotFound id={agentId} />;
  }

  return <AgentControlInner agent={agent} />;
}

function AgentControlInner({ agent }: { agent: AgentDef }) {
  const [auth, setAuth] = useState<AuthState>('gated');
  const [signSeal, setSignSeal] = useState<{ open: boolean; caption: string }>(
    { open: false, caption: 'to authenticate' },
  );

  // Wallet / balances
  const [sol] = useState<number>(INITIAL_SOL);
  const [gold, setGold] = useState<number>(INITIAL_GOLD);
  const [burned, setBurned] = useState<number>(INITIAL_BURN_COUNT);
  const [goldBouncing, setGoldBouncing] = useState(false);
  const [faucetCooling, setFaucetCooling] = useState(false);

  // Essence — server truth + editor draft
  const [committedStrategy, setCommittedStrategy] = useState<string>('');
  const [committedNotes, setCommittedNotes] = useState<string>('');
  const [strategy, setStrategy] = useState<string>('');
  const [notes, setNotes] = useState<string>('');
  const [signing, setSigning] = useState(false);
  const [essenceStatus, setEssenceStatus] = useState<{
    kind: 'success' | 'error';
    body: string;
  } | null>(null);

  // Whispers
  const [draft, setDraft] = useState<string>('');
  const [sentCount, setSentCount] = useState<number>(0);
  const [lastSentAt, setLastSentAt] = useState<number>(-1);
  const [sending, setSending] = useState(false);
  const [whisperStatus, setWhisperStatus] = useState<{
    kind: 'success' | 'error';
    body: string;
  } | null>(null);

  // Toasts
  const [toasts, setToasts] = useState<ToastDef[]>([]);
  const toastIdRef = useRef(0);

  // Centralized setTimeout tracker — auto-clears all pending IDs on unmount.
  const tt = useTimeouts();

  /* ---------- test hook (Playwright only) ---------------------------- */
  // Exposes a tiny mutator for deterministic screenshots. Lives on
  // window.__agentTestHook so it's invisible to anything that doesn't go
  // looking. Reads from URL: only attaches when ?test=1 is present.
  useEffect(() => {
    if (typeof window === 'undefined') return;
    const hasFlag = new URLSearchParams(window.location.search).has('test');
    if (!hasFlag) return;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (window as any).__agentTestHook = {
      setCooldownAgo(secondsAgo: number, sentCount: number) {
        const t0 = Date.now() - secondsAgo * 1000;
        setLastSentAt(t0);
        setSentCount(sentCount);
      },
      setGold(g: number) {
        setGold(g);
      },
    };
  }, []);

  /* ---------- toast helper ---------- */
  const pushToast = useCallback(
    (kind: ToastDef['kind'], body: string, ttl = 2400) => {
      const id = ++toastIdRef.current;
      setToasts((arr) => [...arr, { id, kind, body }]);
      tt.set(() => setToasts((arr) => arr.filter((t) => t.id !== id)), ttl);
    },
    [tt],
  );

  /* ---------- ambient burn-counter drift ---------- */
  // Slow background increment so the counter looks "alive" even without user
  // actions — every ~1.8s nudges it by 7-23 GOLD. Looks like other players
  // burning around the world.
  useEffect(() => {
    if (auth !== 'live') return;
    const id = setInterval(() => {
      setBurned((b) => b + Math.floor(7 + Math.random() * 17));
    }, 1800);
    return () => clearInterval(id);
  }, [auth]);

  /* ---------- auto-dismiss success status ---------- */
  useEffect(() => {
    if (essenceStatus?.kind === 'success') {
      const id = setTimeout(() => setEssenceStatus(null), 5000);
      return () => clearTimeout(id);
    }
  }, [essenceStatus]);
  useEffect(() => {
    if (whisperStatus?.kind === 'success') {
      const id = setTimeout(() => setWhisperStatus(null), 5000);
      return () => clearTimeout(id);
    }
  }, [whisperStatus]);

  /* ---------- handlers ---------- */

  const handleConnect = useCallback(() => {
    setAuth('gateConnecting');
    setSignSeal({ open: true, caption: 'to authenticate' });
    // 800ms fake SIWS, then 1.5-3s decrypting skeleton (or shortened via
    // ?fast query param for deterministic Playwright screenshots).
    const fast =
      typeof window !== 'undefined' &&
      new URLSearchParams(window.location.search).has('fast');
    const sealDelay = fast ? 200 : 800;
    tt.set(() => {
      setSignSeal({ open: false, caption: 'to authenticate' });
      setAuth('decrypting');
      const decryptDuration = fast ? 400 : 1500 + Math.random() * 1500;
      tt.set(() => {
        // Hydrate essence from mock
        const ess = MOCK_ESSENCE[agent.id] ?? { strategy: '', notes: '' };
        setCommittedStrategy(ess.strategy);
        setCommittedNotes(ess.notes);
        setStrategy(ess.strategy);
        setNotes(ess.notes);
        setAuth('live');
        pushToast('success', 'iNFT unsealed · welcome, ælder-keeper');
      }, decryptDuration);
    }, sealDelay);
  }, [agent.id, pushToast, tt]);

  const handleCopyWallet = useCallback(() => {
    if (typeof navigator !== 'undefined' && navigator.clipboard?.writeText) {
      navigator.clipboard.writeText(MOCK_WALLET).catch(() => {});
    }
    pushToast('info', `wallet copied · ${MOCK_WALLET.slice(0, 6)}…`);
  }, [pushToast]);

  const handleFaucet = useCallback(() => {
    if (faucetCooling) return;
    setFaucetCooling(true);
    setGold((g) => g + FAUCET_DROP);
    setGoldBouncing(true);
    pushToast('success', `${FAUCET_DROP.toLocaleString()} GOLD added to your wallet`);
    tt.set(() => setGoldBouncing(false), 800);
    tt.set(() => setFaucetCooling(false), 1200);
  }, [faucetCooling, pushToast, tt]);

  const handleCommitEssence = useCallback(() => {
    if (signing) return;
    setSigning(true);
    setEssenceStatus(null);
    setSignSeal({ open: true, caption: 'to commit ælder essence' });

    tt.set(() => {
      setSignSeal({ open: false, caption: 'to commit ælder essence' });
      setCommittedStrategy(strategy);
      setCommittedNotes(notes);
      setSigning(false);
      const fields: string[] = [];
      if (strategy !== committedStrategy) fields.push('strategy');
      if (notes !== committedNotes) fields.push('notes');
      setEssenceStatus({
        kind: 'success',
        body: `essence sealed · ${fields.join(' + ')} written to 0G`,
      });
    }, 900);
  }, [signing, strategy, notes, committedStrategy, committedNotes, tt]);

  const handleSendWhisper = useCallback(() => {
    const empty = draft.trim().length === 0;
    if (empty || sending) return;
    // Re-derive cooldown at click-time from lastSentAt — no longer carried
    // as parent state (LOW-1 fix: clock is owned by WhispersSection).
    const cooldownMs =
      lastSentAt < 0 ? 0 : Math.max(0, WHISPER_COOLDOWN_MS - (Date.now() - lastSentAt));
    const fullMinutes = Math.ceil(cooldownMs / 60_000);
    const skipTax = cooldownMs > 0 ? fullMinutes * SKIP_TAX_PER_FULL_MINUTE : 0;
    const total = WHISPER_BURN + skipTax;
    if (gold < total) {
      setWhisperStatus({ kind: 'error', body: `Insufficient GOLD (need ${total.toLocaleString()})` });
      return;
    }

    setSending(true);
    setWhisperStatus(null);

    tt.set(() => {
      setGold((g) => g - total);
      setBurned((b) => b + WHISPER_BURN); // only the 5-burn counts toward burn target
      setSentCount((n) => n + 1);
      setLastSentAt(Date.now());
      setDraft('');
      setSending(false);
      setWhisperStatus({
        kind: 'success',
        body:
          skipTax > 0
            ? `whisper sent · 5 burned · ${skipTax.toLocaleString()} → treasury`
            : 'whisper sent · 5 GOLD burned',
      });
      pushToast('success', 'whisper delivered');
    }, 700);
  }, [draft, sending, lastSentAt, gold, pushToast, tt]);

  /* ---------- render ---------- */

  return (
    <div
      data-testid="agent-control-page"
      data-agent-id={agent.id}
      data-auth-state={auth}
      style={{
        position: 'relative',
        width: '100vw',
        height: '100dvh',
        background: t.bg.obsidian,
        color: t.text.primary,
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column',
        fontFamily: t.font.body,
      }}
    >
      <GlobalKeyframes />

      {/* Subtle vignette + grain texture */}
      <div
        aria-hidden
        style={{
          position: 'absolute',
          inset: 0,
          pointerEvents: 'none',
          background:
            `radial-gradient(ellipse at top, rgba(255,107,53,0.05) 0%, transparent 50%),
             radial-gradient(ellipse at bottom, rgba(95,197,212,0.04) 0%, transparent 60%)`,
        }}
      />

      {auth === 'gated' || auth === 'gateConnecting' ? (
        <ConnectGate
          agent={agent}
          loading={auth === 'gateConnecting'}
          onConnect={handleConnect}
        />
      ) : (
        <>
          <AgentHeader
            agent={agent}
            wallet={MOCK_WALLET}
            sol={sol}
            gold={gold}
            burned={burned}
            goldBouncing={goldBouncing}
            faucetCooling={faucetCooling}
            onCopyWallet={handleCopyWallet}
            onFaucet={handleFaucet}
          />

          <EssenceSection
            decrypting={auth === 'decrypting'}
            committedStrategy={committedStrategy}
            committedNotes={committedNotes}
            strategy={strategy}
            notes={notes}
            signing={signing}
            status={essenceStatus}
            onStrategyChange={setStrategy}
            onNotesChange={setNotes}
            onCommit={handleCommitEssence}
            onDismissStatus={() => setEssenceStatus(null)}
          />

          <WhispersSection
            decrypting={auth === 'decrypting'}
            draft={draft}
            onDraftChange={setDraft}
            lastSentAt={lastSentAt}
            cooldownTotalMs={WHISPER_COOLDOWN_MS}
            gold={gold}
            sentCount={sentCount}
            sending={sending}
            status={whisperStatus}
            onSend={handleSendWhisper}
          />
        </>
      )}

      <SignSealModal
        open={signSeal.open}
        caption={signSeal.caption}
        sigil={agent.glyph}
      />
      <ToastStack toasts={toasts} />
    </div>
  );
}

/* ---------------------------------------------------------------------- */

function NotFound({ id }: { id: number }) {
  return (
    <main
      style={{
        background: t.bg.obsidian,
        color: t.text.primary,
        width: '100vw',
        height: '100dvh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '12px',
        padding: '24px',
        textAlign: 'center',
        fontFamily: t.font.body,
      }}
    >
      <div style={{ fontFamily: t.font.rune, fontSize: '52px', color: t.ember.core }}>⟁</div>
      <div
        style={{
          fontFamily: t.font.display,
          fontSize: '18px',
          letterSpacing: '0.32em',
          textTransform: 'uppercase',
        }}
      >
        Unknown ælder
      </div>
      <div
        style={{
          fontFamily: t.font.mono,
          fontSize: '11px',
          color: t.text.muted,
          letterSpacing: '0.18em',
        }}
      >
        agent #{id} · not in the covenant
      </div>
      <div style={{ marginTop: '20px', display: 'flex', flexDirection: 'column', gap: '6px' }}>
        {AGENTS.map((a) => (
          <a
            key={a.id}
            href={`/agents/${a.id}`}
            style={{
              fontFamily: t.font.body,
              fontSize: '12px',
              color: t.rune.core,
              letterSpacing: '0.12em',
              textDecoration: 'none',
              padding: '6px 12px',
              border: `1px solid ${t.border.rune}`,
              borderRadius: t.radius.md,
              background: t.bg.iron,
            }}
          >
            <span style={{ color: a.accent, marginRight: '8px' }}>{a.glyph}</span>
            ælder #{a.id} · {a.name}
          </a>
        ))}
      </div>
    </main>
  );
}

function GlobalKeyframes() {
  return (
    <style>{`
      @keyframes spinFast { to { transform: rotate(360deg); } }
      [data-testid="agent-control-page"] {
        scrollbar-width: thin;
        scrollbar-color: ${t.border.iron} transparent;
      }
      [data-testid="agent-control-page"] *::selection {
        background: ${t.ember.core};
        color: ${t.bg.obsidian};
      }
      [data-testid="agent-control-page"] textarea::placeholder {
        color: ${t.text.muted};
        opacity: 0.55;
        font-style: italic;
      }
      [data-testid="agent-control-page"] textarea:focus {
        outline: none;
      }
    `}</style>
  );
}
