import { useMemo } from 'react';
import { useQuery } from 'convex/react';
import { api } from '../../server/convex/_generated/api';
import { DEMO_MODE } from './config/env';

// Season length in ticks — matches TICKS_PER_DAY_CYCLE * seasons (hardcoded to 360
// as per spec until server exposes it). Used for progress bar.
const TICKS_PER_SEASON = 360;

// Demo bandit state mirrors WorldMap.tsx DEMO_BANDIT so both components agree.
const DEMO_BANDIT_ATTACKS_AT_TICK = 48;

// Clan heraldic color for bandit chip
const BANDIT_RED = '#b23a48';
const GOLD = '#d4a24c';
const PARCHMENT = '#e8d8b5';
const INK = '#3d2817';

function useBanditWarning(liveTick: number): { active: boolean; ticksUntil: number } {
  if (!DEMO_MODE) return { active: false, ticksUntil: 999 };
  const ticksUntil = DEMO_BANDIT_ATTACKS_AT_TICK - liveTick;
  return { active: ticksUntil <= 8 && ticksUntil >= 0, ticksUntil };
}

export function TopHud({ liveTick }: { liveTick: number }) {
  const snapshot = useQuery(api.getSnapshot.getSnapshot);

  const { seasonStartTick, seasonEndTick, winterActive, winterStartsAtTick } = useMemo(() => {
    // Try to read real season data from full worldSnapshot via getSnapshot.
    // The lightweight getSnapshot query only returns tick + tickEpoch + clans + regions —
    // season fields come from a broader snapshot query if exposed.
    // Cast as any to access optional fields the TS type doesn't expose yet.
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const s = snapshot as any;
    return {
      seasonStartTick: typeof s?.seasonStartTick === 'number' ? s.seasonStartTick : null,
      seasonEndTick: typeof s?.seasonEndTick === 'number' ? s.seasonEndTick : null,
      winterActive: s?.winterActive === true,
      winterStartsAtTick: typeof s?.winterStartsAtTick === 'number' ? s.winterStartsAtTick : null,
    };
  }, [snapshot]);

  // Season progress bar: 0..1
  const seasonProgress = useMemo(() => {
    if (seasonStartTick !== null && seasonEndTick !== null && seasonEndTick > seasonStartTick) {
      return Math.max(0, Math.min(1, (liveTick - seasonStartTick) / (seasonEndTick - seasonStartTick)));
    }
    // Fallback: derive from tick % TICKS_PER_SEASON
    return (liveTick % TICKS_PER_SEASON) / TICKS_PER_SEASON;
  }, [liveTick, seasonStartTick, seasonEndTick]);

  const seasonNumber = useMemo(() => {
    if (seasonStartTick !== null && seasonEndTick !== null) {
      return Math.floor(liveTick / TICKS_PER_SEASON) + 1;
    }
    return Math.floor(liveTick / TICKS_PER_SEASON) + 1;
  }, [liveTick, seasonStartTick, seasonEndTick]);

  // Winter approaching warning: within 20 ticks
  const winterWarning = useMemo(() => {
    if (winterActive) return false;
    if (winterStartsAtTick === null) return false;
    return winterStartsAtTick - liveTick <= 20 && winterStartsAtTick > liveTick;
  }, [winterActive, winterStartsAtTick, liveTick]);

  const { active: banditActive, ticksUntil: banditTicksUntil } = useBanditWarning(liveTick);

  // Bar fill color: green → amber → red as season ages
  const barColor = seasonProgress < 0.5
    ? '#3f704d'
    : seasonProgress < 0.8
    ? '#d4a24c'
    : '#b23a48';

  return (
    <div
      style={{
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        height: 44,
        display: 'flex',
        alignItems: 'center',
        gap: 8,
        padding: '0 10px',
        background: 'rgba(10, 16, 10, 0.82)',
        borderBottom: '1px solid rgba(204, 170, 34, 0.3)',
        boxShadow: '0 2px 8px rgba(0,0,0,0.4)',
        pointerEvents: 'none',
        zIndex: 5,
        fontFamily: '"VT323", "Courier New", monospace',
      }}
    >
      {/* Left: tick counter */}
      <div
        style={{
          color: GOLD,
          fontSize: 18,
          letterSpacing: '0.06em',
          whiteSpace: 'nowrap',
          minWidth: 72,
        }}
      >
        tick {liveTick}
      </div>

      {/* Center: season progress */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 2, minWidth: 0 }}>
        <div
          style={{
            fontSize: 10,
            color: 'rgba(232, 216, 181, 0.55)',
            letterSpacing: '0.12em',
            textTransform: 'uppercase',
            fontFamily: 'monospace',
            lineHeight: 1,
          }}
        >
          season {seasonNumber}
        </div>
        {/* Progress bar */}
        <div
          style={{
            height: 5,
            background: 'rgba(255,255,255,0.08)',
            borderRadius: 2,
            overflow: 'hidden',
            border: '1px solid rgba(255,255,255,0.06)',
          }}
        >
          <div
            style={{
              height: '100%',
              width: `${Math.round(seasonProgress * 100)}%`,
              background: barColor,
              borderRadius: 2,
              transition: 'width 0.8s linear, background 1s ease',
              boxShadow: `0 0 6px ${barColor}88`,
            }}
          />
        </div>
      </div>

      {/* Right: status chips */}
      <div style={{ display: 'flex', gap: 6, alignItems: 'center', flexShrink: 0 }}>
        {/* Winter active */}
        {winterActive && (
          <div
            style={{
              padding: '1px 7px',
              background: 'rgba(100, 150, 220, 0.18)',
              border: '1px solid rgba(140, 180, 255, 0.45)',
              borderRadius: 3,
              color: '#a8c4ff',
              fontSize: 13,
              letterSpacing: '0.06em',
            }}
          >
            ❄ WINTER
          </div>
        )}

        {/* Winter approaching */}
        {!winterActive && winterWarning && (
          <div
            style={{
              padding: '1px 7px',
              background: 'rgba(100, 150, 220, 0.10)',
              border: '1px solid rgba(140, 180, 255, 0.28)',
              borderRadius: 3,
              color: 'rgba(168, 196, 255, 0.7)',
              fontSize: 12,
              letterSpacing: '0.04em',
            }}
          >
            ❄ winter soon
          </div>
        )}

        {/* Bandit warning */}
        {banditActive && (
          <div
            style={{
              padding: '1px 8px',
              background: 'rgba(178, 58, 72, 0.22)',
              border: `1px solid ${BANDIT_RED}88`,
              borderRadius: 3,
              color: BANDIT_RED,
              fontSize: 14,
              letterSpacing: '0.06em',
              animation: 'cw-bandit-pulse 0.9s ease-in-out infinite alternate',
            }}
          >
            ⚔ RAID IN {banditTicksUntil}
          </div>
        )}

        {/* Compact tick legend */}
        <div
          style={{
            color: `${PARCHMENT}55`,
            fontSize: 11,
            fontFamily: 'monospace',
            letterSpacing: '0.04em',
          }}
        >
          {Math.round(seasonProgress * 100)}%
        </div>
      </div>

      <style>{`
        @keyframes cw-bandit-pulse {
          from { opacity: 0.7; box-shadow: 0 0 4px ${BANDIT_RED}44; }
          to   { opacity: 1;   box-shadow: 0 0 10px ${BANDIT_RED}88; }
        }
      `}</style>
    </div>
  );
}

// Lightweight version for contexts where we only have liveTick and no snapshot
// (e.g., used in cockpit header). Not exported — use TopHud directly.
void INK; // suppress unused import warning
