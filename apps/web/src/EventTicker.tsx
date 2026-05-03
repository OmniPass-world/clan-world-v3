import { useMemo, useRef, useEffect, useState } from 'react';
import { useQuery } from 'convex/react';
import { api } from '../../server/convex/_generated/api';
import { useAgentLogs } from './useAgentLogs';
import { DEMO_MODE } from './config/env';

// --- Region + action label tables (mirrors WorldMap.tsx) ---

const REGION_NAMES: Record<number, string> = {
  1: 'Forest',
  2: 'Mountains',
  3: 'Unicorn Town',
  4: 'West Farms',
  5: 'East Farms',
  6: 'West Docks',
  7: 'East Docks',
  8: 'Deep Sea',
};

const RESOURCE_NAMES: Record<number, string> = {
  0: 'wood',
  1: 'iron',
  2: 'wheat',
  3: 'fish',
};

const ACTION_LABELS: Record<number, string> = {
  1: 'chop wood',
  2: 'mine iron',
  3: 'fish the docks',
  4: 'fish the deep sea',
  5: 'harvest wheat',
  6: 'deposit resources',
  7: 'upgrade wall',
  8: 'upgrade base',
  9: 'upgrade monument',
  10: 'defend base',
  11: 'buy at market',
  12: 'sell at market',
  13: 'wait',
  14: 'withdraw resources',
};

// Clan color palette — matches heraldic colors in spec §1.2 + WorldMap.tsx MOCK_CLANS
const CLAN_COLORS: Record<string, string> = {
  'clan-iron':  '#4488cc',
  'clan-ember': '#cc4422',
  'clan-dawn':  '#ccaa22',
  'clan-storm': '#44aacc',
};

// Numeric clanId → hex color for live chain events
const CLAN_SLOT_COLORS = ['#b23a48', '#2c5f8d', '#d4a24c', '#3f704d', '#7b3f8c', '#a85a2c', '#e8d8b5', '#475569'];

function slotColor(clanId: number): string {
  return CLAN_SLOT_COLORS[(clanId - 1) % CLAN_SLOT_COLORS.length] ?? '#cccccc';
}

// ---- Chain event → ticker string -----------------------------------------------

type ChainEvent = {
  _id: string;
  eventName: string;
  tick?: number;
  clanId?: number;
  banditId?: number;
  args: unknown;
  blockNumber: number;
  logIndex: number;
};

function safeNum(v: unknown, fallback = 0): number {
  if (typeof v === 'number') return v;
  if (typeof v === 'bigint') return Number(v);
  if (typeof v === 'string') return Number(v) || fallback;
  return fallback;
}

function formatChainEvent(ev: ChainEvent): TickerEntry | null {
  const args = (ev.args ?? {}) as Record<string, unknown>;
  const clanId = ev.clanId ?? safeNum(args.clanId, 0);
  const clanLabel = clanId > 0 ? `Clan ${clanId}` : 'Unknown';
  const clanColor = clanId > 0 ? slotColor(clanId) : '#aaa';
  const regionId = safeNum(args.region, 0) || safeNum(args.toRegion, 0);
  const regionName = REGION_NAMES[regionId] ?? `Region ${regionId}`;

  switch (ev.eventName) {
    case 'MissionAssigned': {
      const action = safeNum(args.action, 0);
      const label = ACTION_LABELS[action] ?? 'act';
      const dest = safeNum(args.region, 0) || safeNum(args.targetRegion, 0);
      const destName = REGION_NAMES[dest] ?? regionName;
      return { text: `${clanLabel} clansman → ${destName} to ${label}`, clanColor };
    }
    case 'WorkerArrived':
      return { text: `${clanLabel} worker arrived at ${regionName}`, clanColor };
    case 'MissionCompleted': {
      const action = safeNum(args.action, 0);
      const label = ACTION_LABELS[action] ?? 'mission';
      return { text: `${clanLabel} completed: ${label}`, clanColor };
    }
    case 'ResourcesGathered': {
      const parts: string[] = [];
      ['wood', 'iron', 'wheat', 'fish'].forEach((r, i) => {
        const amt = safeNum(args[r], 0);
        if (amt > 0) parts.push(`${amt} ${RESOURCE_NAMES[i]}`);
      });
      if (!parts.length) return null;
      return { text: `${clanLabel} gathered ${parts.join(', ')}`, clanColor };
    }
    case 'ResourcesDeposited': {
      const parts: string[] = [];
      ['wood', 'iron', 'wheat', 'fish'].forEach((r, i) => {
        const raw = args[r] ?? args[`amount${r.charAt(0).toUpperCase() + r.slice(1)}`];
        const amt = safeNum(raw, 0);
        if (amt > 0) parts.push(`${amt} ${RESOURCE_NAMES[i]}`);
      });
      if (!parts.length) return { text: `${clanLabel} deposited resources`, clanColor };
      return { text: `${clanLabel} deposited ${parts.join(', ')}`, clanColor };
    }
    case 'ImmediateMarketActionExecuted':
    case 'ScheduledMarketActionExecuted': {
      const resourceIn = safeNum(args.resourceIn, -1);
      const resourceOut = safeNum(args.resourceOut, -1);
      const amtIn = safeNum(args.amountIn, 0);
      const amtOut = safeNum(args.amountOut, 0);
      if (resourceIn === -1 || resourceOut === -1) return null;
      const inName = RESOURCE_NAMES[resourceIn] ?? `res${resourceIn}`;
      const outName = RESOURCE_NAMES[resourceOut] ?? `res${resourceOut}`;
      return {
        text: `${clanLabel} traded ${amtIn} ${inName} → ${amtOut} ${outName} at Unicorn Town`,
        clanColor,
        highlight: true,
      };
    }
    case 'BanditSpawned': {
      const tier = safeNum(args.tier, 1);
      return { text: `⚠ Bandits spawned in ${regionName} — tier ${tier}`, clanColor: '#b23a48', highlight: true };
    }
    case 'BanditStateChanged': {
      const newState = safeNum(args.newState, 0);
      if (newState === 4) {
        // Attacking
        return { text: `⚔ Bandits attacking in ${regionName}!`, clanColor: '#b23a48', highlight: true };
      }
      return null;
    }
    case 'BanditAttackResolved': {
      const targetId = safeNum(args.targetClanId, clanId);
      const targetColor = targetId > 0 ? slotColor(targetId) : '#aaa';
      const defended = args.defended === true || safeNum(args.defended, 0) === 1;
      return {
        text: `⚔ Clan ${targetId} ${defended ? 'repelled the bandits!' : 'was raided by bandits!'}`,
        clanColor: defended ? '#3f704d' : '#b23a48',
        highlight: true,
        _targetColor: targetColor,
      };
    }
    case 'BanditDefeated':
      return { text: `★ Clan ${safeNum(args.targetClanId, clanId)} defeated the bandits!`, clanColor: '#d4a24c', highlight: true };
    case 'WallDamagedByBandit': {
      const newLevel = safeNum(args.wallLevel, 0);
      return { text: `${clanLabel} wall damaged → level ${newLevel}`, clanColor: '#b23a48' };
    }
    case 'ClansmanKilledByBandit':
      return { text: `${clanLabel} lost a clansman to bandits`, clanColor: '#b23a48' };
    case 'BlueprintEarned':
      return { text: `${clanLabel} earned a blueprint!`, clanColor: '#d4a24c', highlight: true };
    case 'BuildingUpgraded': {
      const building = String(args.building ?? 'structure');
      const level = safeNum(args.newLevel, 0);
      return { text: `${clanLabel} upgraded ${building} → level ${level}`, clanColor, highlight: true };
    }
    case 'LootDistributed':
      return { text: `Loot distributed after bandit raid`, clanColor: '#d4a24c' };
    default:
      return null;
  }
}

// ---- Agent log → ticker string (DEMO_MODE fallback) ----------------------------

// Clan name patterns used by the Elders in DEMO_MODE logs
const MOCK_CLAN_PATTERNS: { pattern: RegExp; name: string; color: string }[] = [
  { pattern: /iron\s*guard/i,   name: 'Iron Guard',   color: CLAN_COLORS['clan-iron']  ?? '#4488cc' },
  { pattern: /ember\s*hand/i,   name: 'Ember Hand',   color: CLAN_COLORS['clan-ember'] ?? '#cc4422' },
  { pattern: /dawn\s*watch/i,   name: 'Dawn Watch',   color: CLAN_COLORS['clan-dawn']  ?? '#ccaa22' },
  { pattern: /storm\s*riders/i, name: 'Storm Riders', color: CLAN_COLORS['clan-storm'] ?? '#44aacc' },
];

const DEMO_REGION_PATTERNS = [
  'Forest', 'Mountains', 'Unicorn Town', 'West Farms', 'East Farms',
  'West Docks', 'East Docks', 'Deep Sea',
];

function matchRegionInText(text: string): string | null {
  // Longest match first to avoid "Farms" matching before "West Farms"
  const sorted = [...DEMO_REGION_PATTERNS].sort((a, b) => b.length - a.length);
  for (const r of sorted) {
    if (text.toLowerCase().includes(r.toLowerCase())) return r;
  }
  return null;
}

function formatAgentLog(msg: string): TickerEntry | null {
  // Skip purely internal/debug lines
  if (msg.startsWith('[') || msg.startsWith('DEBUG') || msg.length < 10) return null;

  // Strip "warn|" / "info|" prefix
  const cleaned = msg.replace(/^(warn|info|error)\|\s*/i, '').trim();

  // WORLD-level events — already shown in WorldNoticePanel, but worth in ticker too
  const isWorld = cleaned.toLowerCase().startsWith('world:') ||
    /\b(bandit|raid|winter|omen)\b/i.test(cleaned);
  if (isWorld) {
    const text = cleaned.replace(/^WORLD:\s*/i, '').trim();
    return { text: `⚠ ${text}`, clanColor: '#b23a48', highlight: true };
  }

  // Identify clan
  let clanName: string | null = null;
  let clanColor = '#cccccc';
  for (const p of MOCK_CLAN_PATTERNS) {
    if (p.pattern.test(cleaned)) {
      clanName = p.name;
      clanColor = p.color;
      break;
    }
  }

  // Strip "Clan Elder:" prefix
  const body = cleaned.replace(/^[^:]+Elder:\s*/i, '').trim();
  if (!body) return null;

  // Travel dispatch
  const travelMatch = body.match(
    /\b(?:send(?:ing)?|dispatch(?:ing|ed)?|travel(?:ing)?|head(?:ing)?)\b.*?\bto\b\s+(?:the\s+)?([A-Z][a-z ]+)/i,
  );
  if (travelMatch) {
    const dest = matchRegionInText(travelMatch[1] ?? '') ?? travelMatch[1] ?? '';
    const prefix = clanName ?? 'A clansman';
    return { text: `${prefix} → ${dest}`, clanColor };
  }

  // Deposit
  if (/\bdeposit\b/i.test(body)) {
    const prefix = clanName ?? 'Clan';
    return { text: `${prefix} depositing resources at base`, clanColor };
  }

  // Trade
  if (/\b(market|trade|unicorn town|sell|buy)\b/i.test(body)) {
    const prefix = clanName ?? 'Clan';
    return { text: `${prefix} trading at Unicorn Town`, clanColor, highlight: true };
  }

  // Upgrade
  const upgradeMatch = body.match(/\bupgrad(?:ing|e)\b[^.]*?(wall|base|monument)/i);
  if (upgradeMatch) {
    const prefix = clanName ?? 'Clan';
    return { text: `${prefix} upgrading ${upgradeMatch[1]}`, clanColor, highlight: true };
  }

  return null;
}

// ---- Ticker entry type ---------------------------------------------------------

type TickerEntry = {
  text: string;
  clanColor: string;
  highlight?: boolean;
  _targetColor?: string;
};

// ---- Component -----------------------------------------------------------------

const SEPARATOR = '  •  ';
const SCROLL_PX_PER_SEC = 48;

export function EventTicker() {
  const agentLogs = useAgentLogs();
  const rawChainEvents = useQuery(api.events.getRecentChainEvents) as ChainEvent[] | undefined;

  const entries = useMemo<TickerEntry[]>(() => {
    if (!DEMO_MODE && rawChainEvents && rawChainEvents.length > 0) {
      // Live mode: format chain events, most recent first, skip nulls
      return rawChainEvents
        .slice()
        // Sort ascending (oldest first) so the ticker reads chronologically left→right
        .sort((a, b) => a.blockNumber - b.blockNumber || a.logIndex - b.logIndex)
        .map(formatChainEvent)
        .filter((e): e is TickerEntry => e !== null);
    }

    // DEMO_MODE or no chain events yet: parse agent logs
    return [...agentLogs]
      .reverse() // oldest first
      .map(l => formatAgentLog(l.message))
      .filter((e): e is TickerEntry => e !== null);
  }, [rawChainEvents, agentLogs]);

  // Compose a single long string for CSS animation
  const tickerText = useMemo(() => {
    if (entries.length === 0) return null;
    // We'll render as spans so we keep per-entry coloring
    return entries;
  }, [entries]);

  // Measure rendered width so we can set the correct animation duration
  const innerRef = useRef<HTMLDivElement>(null);
  const [animDuration, setAnimDuration] = useState(30);

  useEffect(() => {
    if (!innerRef.current) return;
    const w = innerRef.current.scrollWidth;
    setAnimDuration(Math.max(15, w / SCROLL_PX_PER_SEC));
  }, [tickerText]);

  if (!tickerText || tickerText.length === 0) return null;

  return (
    <div
      style={{
        position: 'absolute',
        bottom: 56, // sits just above the compact scoreboard panel
        left: 0,
        right: 0,
        height: 32,
        background: 'rgba(10, 16, 10, 0.78)',
        borderTop: '1px solid rgba(204, 170, 34, 0.25)',
        borderBottom: '1px solid rgba(204, 170, 34, 0.12)',
        overflow: 'hidden',
        display: 'flex',
        alignItems: 'center',
        pointerEvents: 'none',
        zIndex: 4,
      }}
    >
      <style>{`
        @keyframes cw-ticker-scroll {
          0%   { transform: translateX(100vw); }
          100% { transform: translateX(-100%); }
        }
      `}</style>
      <div
        ref={innerRef}
        style={{
          display: 'flex',
          alignItems: 'center',
          whiteSpace: 'nowrap',
          animation: `cw-ticker-scroll ${animDuration}s linear infinite`,
          willChange: 'transform',
        }}
      >
        {tickerText.map((entry, i) => (
          <span key={i} style={{ display: 'inline-flex', alignItems: 'center' }}>
            <span
              style={{
                color: entry.clanColor,
                fontFamily: '"VT323", "Courier New", monospace',
                fontSize: 16,
                letterSpacing: '0.03em',
                fontWeight: entry.highlight ? 700 : 400,
                textShadow: entry.highlight
                  ? `0 0 8px ${entry.clanColor}88`
                  : undefined,
              }}
            >
              {entry.text}
            </span>
            {i < tickerText.length - 1 && (
              <span
                style={{
                  color: 'rgba(204, 170, 34, 0.55)',
                  fontFamily: '"VT323", "Courier New", monospace',
                  fontSize: 16,
                  padding: '0 4px',
                }}
              >
                {SEPARATOR}
              </span>
            )}
          </span>
        ))}
      </div>
    </div>
  );
}
