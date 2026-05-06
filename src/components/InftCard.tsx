import React from 'react';
import { Pressable, Text, View } from 'react-native';
import Svg, { Polyline } from 'react-native-svg';
import { ARCHETYPE_GLYPHS, Inft, truncAddr } from '../data';
import { colors, fonts } from '../theme';
import { ArchetypeGlyph } from './ArchetypeGlyph';
import { StatusDot } from './Pill';
import { Parchment } from './Surfaces';

type StatPillProps = { label: string; value: string | number };

export const StatPill = ({ label, value }: StatPillProps) => (
  <View
    style={{
      flexDirection: 'row',
      alignItems: 'center',
      gap: 6,
      backgroundColor: 'rgba(26,20,16,0.08)',
      borderColor: 'rgba(26,20,16,0.18)',
      borderWidth: 1,
      paddingHorizontal: 6,
      paddingVertical: 3,
      borderRadius: 2,
      minWidth: 64,
      justifyContent: 'space-between',
    }}
  >
    <Text style={{ color: colors.inkParchmentMuted, fontSize: 8, letterSpacing: 1 }}>
      {label}
    </Text>
    <Text
      style={{
        fontFamily: fonts.monoBold,
        color: colors.inkParchment,
        fontSize: 10,
      }}
    >
      {value}
    </Text>
  </View>
);

const Sparkline = ({ data }: { data: number[] }) => {
  const w = 56;
  const h = 18;
  const max = Math.max(...data);
  const min = Math.min(...data);
  const span = max - min || 1;
  const pts = data
    .map((v, i) => `${(i / (data.length - 1)) * w},${h - ((v - min) / span) * h}`)
    .join(' ');
  return (
    <View style={{ paddingHorizontal: 12, paddingBottom: 8, alignItems: 'flex-end' }}>
      <Svg width={w} height={h}>
        <Polyline points={pts} fill="none" stroke={colors.goldDeep} strokeWidth={1.5} />
      </Svg>
    </View>
  );
};

type Props = {
  inft: Inft;
  onPress?: () => void;
  hireFee?: string;
  sparkline?: number[];
};

export const InftCard = ({ inft, onPress, hireFee, sparkline }: Props) => {
  const stateColor =
    inft.state === 'in-game'
      ? colors.statusLive
      : inft.state === 'rented'
      ? colors.statusInfo
      : colors.inkParchmentMuted;

  return (
    <Pressable onPress={onPress}>
      <Parchment deep style={{ padding: 0 }}>
        <View style={{ flexDirection: 'row', gap: 12, padding: 12, alignItems: 'flex-start' }}>
          <View style={{ width: 60, alignItems: 'center', paddingTop: 4 }}>
            <ArchetypeGlyph kind={inft.archetype} size={48} />
          </View>
          <View style={{ flex: 1, minWidth: 0, gap: 4 }}>
            <Text
              style={{
                fontFamily: fonts.display,
                fontSize: 19,
                color: colors.inkParchment,
                letterSpacing: 0.6,
              }}
            >
              {inft.name}
            </Text>
            <Text
              style={{
                fontFamily: fonts.script,
                fontStyle: 'italic',
                fontSize: 12,
                color: colors.scriptBlueDark,
              }}
            >
              {ARCHETYPE_GLYPHS[inft.archetype].name}
            </Text>
            <Text
              style={{
                fontFamily: fonts.mono,
                fontSize: 9,
                color: colors.inkParchmentMuted,
                marginTop: 2,
              }}
            >
              {truncAddr(inft.tokenId, 4, 4)}
            </Text>
            {hireFee && (
              <Text
                style={{
                  fontFamily: fonts.monoBold,
                  fontSize: 11,
                  color: colors.goldDeep,
                  marginTop: 4,
                }}
              >
                {hireFee}
              </Text>
            )}
          </View>
          <View style={{ alignItems: 'flex-end', gap: 4 }}>
            <StatPill label="ELO" value={inft.elo} />
            <StatPill label="LAST 10" value={`#${inft.last10}`} />
            <StatPill label="SEASONS" value={inft.seasons} />
          </View>
        </View>
        {sparkline && <Sparkline data={sparkline} />}
        <View
          style={{
            paddingHorizontal: 12,
            paddingVertical: 6,
            borderTopWidth: 1,
            borderTopColor: 'rgba(26,20,16,0.18)',
            backgroundColor: 'rgba(26,20,16,0.06)',
            flexDirection: 'row',
            alignItems: 'center',
            gap: 6,
          }}
        >
          <StatusDot
            kind={
              inft.state === 'in-game' ? 'live' : inft.state === 'rented' ? 'hired' : 'idle'
            }
          />
          <Text
            style={{
              fontFamily: fonts.mono,
              fontSize: 10,
              color: stateColor,
              letterSpacing: 0.8,
            }}
          >
            {inft.state === 'in-game'
              ? `IN GAME · TICK ${inft.gameTick}`
              : inft.state === 'rented'
              ? `HIRED BY ${truncAddr(inft.rentedBy ?? '0xa1c4f7e9')}`
              : 'IDLE'}
          </Text>
        </View>
      </Parchment>
    </Pressable>
  );
};
