import { useEffect, useRef, useState } from 'react';
import type { ReactNode } from 'react';
import { useSafeQuery as useQuery } from '../../../hooks/useSafeQuery';
import { api } from '../../../../../server/convex/_generated/api';
import { tokens } from '../../../styles/cockpit-tokens';
import type { ElderDef } from '../../../styles/cockpit-tokens';
import type { Clan } from '@clan-world/shared';

interface Props {
  elder: ElderDef;
  testIdPrefix: string;
}

const WEI_PER_RESOURCE = '1000000000000000000';

type VaultResource = {
  glyph: string;
  label: string;
  value: number;
  status: string;
};

function wholeUnits(value: string | undefined): number {
  if (!value) return 0;
  try {
    return Number(BigInt(value) / BigInt(WEI_PER_RESOURCE));
  } catch {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : 0;
  }
}

function resourcesForClan(clan: Clan | undefined, status: string): VaultResource[] {
  return [
    { glyph: '◈', label: 'Gold', value: wholeUnits(clan?.goldBalance ?? clan?.treasury), status },
    { glyph: '⌬', label: 'Wood', value: wholeUnits(clan?.vaultWood), status },
    { glyph: '◆', label: 'Iron', value: wholeUnits(clan?.vaultIron), status },
    { glyph: '✦', label: 'Wheat', value: wholeUnits(clan?.vaultWheat), status },
    { glyph: '◇', label: 'Fish', value: wholeUnits(clan?.vaultFish), status },
    { glyph: '▣', label: 'Blueprint', value: wholeUnits(clan?.blueprintBalance), status },
  ];
}

/** Asset movement event for the log strip. */
type Movement = {
  tick: number;
  type: 'gain' | 'spend';
  amount: string;
  source: string;
};

/** Demo stub — used when Convex is cold or the live feed is empty. */
const STUB_MOVEMENTS: Movement[] = [
  { tick: 4, type: 'gain',  amount: '+45 gold',  source: 'raid · forest' },
  { tick: 3, type: 'spend', amount: '-12 wood',  source: 'mill upkeep'   },
  { tick: 2, type: 'gain',  amount: '+8 stone',  source: 'quarry'        },
  { tick: 1, type: 'gain',  amount: '+24 gold',  source: 'tribute'       },
];

function formatAmount(type: 'gain' | 'spend', amount: number, resource: string): string {
  const sign = type === 'gain' ? '+' : '-';
  return `${sign}${amount.toLocaleString()} ${resource}`;
}

export function VaultTab({ elder, testIdPrefix }: Props) {
  const snapshot = useQuery(api.getSnapshot.getSnapshot);
  const clan = snapshot?.clans.find((c) => c.id === String(elder.clanId));
  const syncLabel = snapshot === undefined ? 'Syncing...' : clan ? `T${snapshot.tick}` : 'No clan';
  const resources = resourcesForClan(clan, clan ? 'live' : snapshot === undefined ? 'sync' : 'empty');

  // Live vault movements from chainEvents; stub fallback for cold backend / demo.
  // Falls back to stub when:
  //   - useQuery returns undefined (initial load)
  //   - the live feed is empty (no chain activity yet — common in demo prep)
  const liveMovements = useQuery(api.vault.getVaultMovements, { clanId: elder.clanId });
  const movementsAreLive = Array.isArray(liveMovements) && liveMovements.length > 0;
  const movements: Movement[] = movementsAreLive
    ? liveMovements.map((m) => {
        // Server narrows to "gain" | "spend"; fall back defensively to "gain"
        // if a future schema change loosens the union (renders sign-as-positive
        // instead of crashing the demo).
        const movementType: 'gain' | 'spend' = m.type === 'spend' ? 'spend' : 'gain';
        return {
          tick: m.tick,
          type: movementType,
          amount: formatAmount(movementType, m.amount, m.resource),
          source: m.source,
        };
      })
    : STUB_MOVEMENTS;

  // Resources are live when a clan row exists in the snapshot. Movements are
  // live when the chain-events query returned at least one row. The whole tab
  // is "live" only when both are live; otherwise we mark it stub for clarity.
  const dataSource = clan && movementsAreLive ? 'live' : 'stub';

  return (
    <div
      data-testid={`${testIdPrefix}-content-vault`}
      data-source={dataSource}
      style={{
        flex: 1,
        background: tokens.bg.parchment,
        color: tokens.text.onParchment,
        padding: tokens.space.md,
        overflowY: 'auto',
        display: 'flex',
        flexDirection: 'column',
        gap: tokens.space.md,
        fontFamily: tokens.font.body,
      }}
    >
      <style>
        {`
          @keyframes clanworld-counter-floater {
            0% { opacity: 1; transform: translate(-50%, 0); }
            100% { opacity: 0; transform: translate(-50%, -16px); }
          }
        `}
      </style>
      {/* Resource grid */}
      <section>
        <SectionHeader>
          Vault — Clan {elder.clanId}
          <span
            style={{
              float: 'right',
              fontFamily: tokens.font.mono,
              fontSize: '10px',
              letterSpacing: 0,
              textTransform: 'none',
              color: tokens.text.muted,
            }}
          >
            {syncLabel}
          </span>
        </SectionHeader>
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: tokens.space.sm,
            marginTop: tokens.space.sm,
          }}
        >
          {resources.map((r) => (
            <div
              key={r.label}
              style={{
                background: 'rgba(255,255,255,0.35)',
                border: `1px solid ${tokens.border.parchmentEdge}`,
                borderRadius: tokens.radius.sm,
                padding: tokens.space.sm,
                display: 'flex',
                alignItems: 'center',
                gap: tokens.space.sm,
              }}
            >
              <span style={{ fontSize: '20px', color: elder.accent }}>{r.glyph}</span>
              <div style={{ display: 'flex', flexDirection: 'column', flex: 1, minWidth: 0 }}>
                <span
                  style={{
                    fontSize: '9px',
                    letterSpacing: '0.14em',
                    textTransform: 'uppercase',
                    color: tokens.text.onParchmentDim,
                  }}
                >
                  {r.label}
                </span>
                <span
                  style={{
                    fontFamily: tokens.font.mono,
                    fontSize: '16px',
                    fontWeight: 600,
                    color: tokens.text.onParchment,
                  }}
                >
                  <RollingNumber value={r.value} />
                </span>
              </div>
              <span
                style={{
                  fontFamily: tokens.font.mono,
                  fontSize: '10px',
                  color: tokens.text.muted,
                }}
              >
                {r.status}
              </span>
            </div>
          ))}
        </div>
      </section>

      {/* Movement log */}
      <section style={{ flex: 1, display: 'flex', flexDirection: 'column', minHeight: 0 }}>
        <SectionHeader>Asset movements</SectionHeader>
        <ul
          style={{
            listStyle: 'none',
            margin: `${tokens.space.sm} 0 0`,
            padding: 0,
            overflowY: 'auto',
            display: 'flex',
            flexDirection: 'column',
            gap: '4px',
          }}
        >
          {movements.map((m, i) => (
            <li
              key={`${m.tick}-${i}`}
              style={{
                display: 'flex',
                alignItems: 'baseline',
                gap: tokens.space.sm,
                fontSize: '11px',
                fontFamily: tokens.font.mono,
                padding: '4px 6px',
                borderLeft: `2px solid ${
                  m.type === 'gain' ? '#3a7a3a' : tokens.text.danger
                }`,
                background: 'rgba(255,255,255,0.18)',
              }}
            >
              <span style={{ color: tokens.text.muted, width: '32px' }}>T{m.tick}</span>
              <span
                style={{
                  fontWeight: 600,
                  color:
                    m.type === 'gain' ? '#3a7a3a' : tokens.text.danger,
                  width: '76px',
                }}
              >
                {m.amount}
              </span>
              <span style={{ color: tokens.text.onParchmentDim, flex: 1 }}>
                {m.source}
              </span>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}

function easeOutQuad(t: number) {
  return 1 - (1 - t) * (1 - t);
}

function RollingNumber({ value }: { value: number }) {
  const [displayValue, setDisplayValue] = useState(value);
  const [floater, setFloater] = useState<{ id: number; delta: number } | null>(null);
  const renderedValueRef = useRef(value);
  const targetValueRef = useRef(value);
  const floaterIdRef = useRef(0);

  useEffect(() => {
    const from = renderedValueRef.current;
    const to = value;
    const delta = to - targetValueRef.current;
    targetValueRef.current = to;
    if (from === to) {
      return;
    }

    if (delta !== 0) {
      floaterIdRef.current += 1;
      setFloater({ id: floaterIdRef.current, delta });
    }
    const duration = Math.min(400, 100 + Math.log2(Math.abs(to - from) + 1) * 40);
    const startedAt = performance.now();
    let frame = 0;

    const tick = (now: number) => {
      const t = Math.min(1, (now - startedAt) / duration);
      const eased = easeOutQuad(t);
      const next = Math.round(from + (to - from) * eased);
      renderedValueRef.current = next;
      setDisplayValue(next);
      if (t < 1) {
        frame = window.requestAnimationFrame(tick);
      } else {
        renderedValueRef.current = to;
        setDisplayValue(to);
      }
    };

    frame = window.requestAnimationFrame(tick);
    const clearFloater = delta !== 0 ? window.setTimeout(() => setFloater(null), 800) : null;
    return () => {
      window.cancelAnimationFrame(frame);
      if (clearFloater !== null) window.clearTimeout(clearFloater);
    };
  }, [value]);

  return (
    <span style={{ position: 'relative', display: 'inline-block', minWidth: '4ch' }}>
      {displayValue.toLocaleString()}
      {floater && (
        <span
          key={floater.id}
          style={{
            position: 'absolute',
            left: '50%',
            bottom: '100%',
            transform: 'translate(-50%, 0)',
            color: floater.delta > 0 ? '#4ade80' : '#ef4444',
            fontFamily: tokens.font.mono,
            fontSize: '10px',
            fontWeight: 700,
            pointerEvents: 'none',
            animation: 'clanworld-counter-floater 800ms ease-out forwards',
            textShadow: '0 1px 2px rgba(0,0,0,0.35)',
            whiteSpace: 'nowrap',
          }}
        >
          {floater.delta > 0 ? '+' : ''}
          {floater.delta.toLocaleString()}
        </span>
      )}
    </span>
  );
}

function SectionHeader({ children }: { children: ReactNode }) {
  return (
    <h3
      style={{
        margin: 0,
        fontFamily: tokens.font.display,
        fontSize: '11px',
        letterSpacing: '0.18em',
        textTransform: 'uppercase',
        color: tokens.text.onParchmentDim,
        borderBottom: `1px solid ${tokens.border.parchmentEdge}`,
        paddingBottom: '4px',
      }}
    >
      {children}
    </h3>
  );
}
