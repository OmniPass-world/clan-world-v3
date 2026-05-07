// Home-screen widget UI for Clan World. Defined in JSX-style markup using
// react-native-android-widget primitives (FlexWidget, TextWidget, etc.) —
// these compile to Android RemoteViews at runtime, so styling is more
// constrained than regular React Native (no custom fonts, limited
// effects). System fonts only; layout via flex with rem-style sizes.

import React from 'react';
import { FlexWidget, TextWidget } from 'react-native-android-widget';
import type { WidgetState } from './widgetState';

const COLORS = {
  bgCanvas: '#0B0A0E',
  bgElevated: '#15131A',
  goldPrimary: '#D4AF5C',
  goldBright: '#E8C77E',
  goldDeep: '#8C6F3A',
  inkDark: '#E8DDC4',
  inkDarkMuted: '#9A8E72',
  inkDarkFaint: '#5C5443',
  scriptBlue: '#6B7DB8',
  statusDanger: '#A04A3F',
  statusLive: '#6B8E5C',
} as const;

type Props = { state: WidgetState };

export const HeroWidget = ({ state }: Props) => {
  if (state.mode === 'raid') return <RaidWidget state={state} />;
  if (state.mode === 'loaded') return <LoadedWidget state={state} />;
  return <EmptyWidget />;
};

// ─── Raid mode ─────────────────────────────────────────────────────────

const RaidWidget = ({ state }: { state: WidgetState }) => (
  <FlexWidget
    style={{
      width: 'match_parent',
      height: 'match_parent',
      flexDirection: 'column',
      backgroundColor: COLORS.bgCanvas,
      borderRadius: 12,
      borderWidth: 2,
      borderColor: COLORS.statusDanger,
      padding: 14,
    }}
    clickAction="OPEN_APP"
  >
    {/* Top ribbon */}
    <FlexWidget
      style={{
        width: 'match_parent',
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 10,
      }}
    >
      <TextWidget
        text="⚔  BANDIT RAID"
        style={{
          fontSize: 12,
          color: COLORS.statusDanger,
          fontWeight: 'bold',
        }}
      />
      <TextWidget
        text={`T${state.raidTick ?? '—'}`}
        style={{ fontSize: 11, color: COLORS.statusDanger, fontFamily: 'monospace' }}
      />
    </FlexWidget>

    <TextWidget
      text={`${state.raidVictim ?? 'Your settlement'} is under attack.`}
      style={{ fontSize: 16, color: COLORS.inkDark, marginBottom: 6 }}
      maxLines={2}
    />

    <TextWidget
      text="Tap to defend in the cockpit."
      style={{ fontSize: 12, color: COLORS.inkDarkMuted, fontStyle: 'italic' }}
    />
  </FlexWidget>
);

// ─── Loaded INFT mode ──────────────────────────────────────────────────

const LoadedWidget = ({ state }: { state: WidgetState }) => (
  <FlexWidget
    style={{
      width: 'match_parent',
      height: 'match_parent',
      flexDirection: 'column',
      backgroundColor: COLORS.bgCanvas,
      borderRadius: 12,
      borderWidth: 1,
      borderColor: COLORS.goldPrimary,
      padding: 14,
    }}
    clickAction="OPEN_APP"
  >
    {/* Top: tick + season info if available */}
    {state.tick !== undefined && (
      <FlexWidget
        style={{
          width: 'match_parent',
          flexDirection: 'row',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: 8,
        }}
      >
        <TextWidget
          text={`TICK ${state.tick} · S${state.season ?? 1}`}
          style={{
            fontSize: 10,
            color: COLORS.goldDeep,
            fontFamily: 'monospace',
          }}
        />
        <TextWidget
          text={`${Math.round((state.seasonPct ?? 0) * 100)}%`}
          style={{
            fontSize: 10,
            color: COLORS.goldDeep,
            fontFamily: 'monospace',
          }}
        />
      </FlexWidget>
    )}

    {/* Identity row */}
    <FlexWidget
      style={{
        width: 'match_parent',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 8,
      }}
    >
      <TextWidget
        text={state.archetypeGlyph ?? '◆'}
        style={{
          fontSize: 32,
          color: (state.archetypeColor ?? COLORS.goldBright) as `#${string}`,
          marginRight: 12,
        }}
      />
      <FlexWidget style={{ flex: 1, flexDirection: 'column' }}>
        <TextWidget
          text={state.clanName ?? 'Elder'}
          style={{ fontSize: 18, color: COLORS.inkDark, fontWeight: 'bold' }}
          maxLines={1}
        />
        <TextWidget
          text={state.archetypeName ?? ''}
          style={{ fontSize: 12, color: COLORS.scriptBlue, fontStyle: 'italic' }}
          maxLines={1}
        />
      </FlexWidget>
    </FlexWidget>

    {/* Resource line */}
    {state.resourceLine && (
      <TextWidget
        text={state.resourceLine}
        style={{
          fontSize: 11,
          color: COLORS.inkDarkMuted,
          fontFamily: 'monospace',
          marginTop: 4,
        }}
      />
    )}
  </FlexWidget>
);

// ─── Empty mode ────────────────────────────────────────────────────────

const EmptyWidget = () => (
  <FlexWidget
    style={{
      width: 'match_parent',
      height: 'match_parent',
      flexDirection: 'column',
      justifyContent: 'center',
      alignItems: 'center',
      backgroundColor: COLORS.bgCanvas,
      borderRadius: 12,
      borderWidth: 1,
      borderColor: COLORS.goldDeep,
      padding: 18,
    }}
    clickAction="OPEN_APP"
  >
    <TextWidget
      text="◇  CLAN WORLD"
      style={{
        fontSize: 12,
        color: COLORS.goldDeep,
        fontWeight: 'bold',
        marginBottom: 8,
      }}
    />
    <TextWidget
      text="Your hall is silent."
      style={{
        fontSize: 16,
        color: COLORS.scriptBlue,
        fontStyle: 'italic',
        marginBottom: 4,
      }}
    />
    <TextWidget
      text="Tap to load an Elder."
      style={{ fontSize: 12, color: COLORS.inkDarkFaint }}
    />
  </FlexWidget>
);
