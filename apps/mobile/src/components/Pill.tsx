import React, { ReactNode } from 'react';
import { Text, View } from 'react-native';
import { colors, fonts } from '../theme';

export type PillKind = 'live' | 'warn' | 'danger' | 'info' | 'idle';

const PILL_STYLES: Record<PillKind, { bg: string; color: string; border: string }> = {
  live: { bg: 'rgba(107, 142, 92, 0.16)', color: colors.statusLive, border: 'rgba(107, 142, 92, 0.4)' },
  warn: { bg: 'rgba(201, 155, 79, 0.16)', color: colors.statusWarn, border: 'rgba(201, 155, 79, 0.4)' },
  danger: { bg: 'rgba(160, 74, 63, 0.16)', color: colors.statusDanger, border: 'rgba(160, 74, 63, 0.4)' },
  info: { bg: 'rgba(90, 123, 168, 0.16)', color: colors.statusInfo, border: 'rgba(90, 123, 168, 0.4)' },
  idle: { bg: 'transparent', color: colors.inkDarkMuted, border: colors.inkDarkFaint },
};

type PillProps = { kind?: PillKind; children: ReactNode };

export const Pill = ({ kind = 'live', children }: PillProps) => {
  const s = PILL_STYLES[kind];
  return (
    <View
      style={{
        backgroundColor: s.bg,
        borderColor: s.border,
        borderWidth: 1,
        paddingHorizontal: 7,
        paddingVertical: 3,
        borderRadius: 2,
        alignSelf: 'flex-start',
      }}
    >
      <Text
        style={{
          fontFamily: fonts.mono,
          fontSize: 10,
          color: s.color,
          letterSpacing: 0.4,
        }}
      >
        {children}
      </Text>
    </View>
  );
};

export type DotKind = 'live' | 'idle' | 'hired' | 'warn' | 'danger';

const DOT: Record<DotKind, { glyph: string; color: string }> = {
  live: { glyph: '●', color: colors.statusLive },
  idle: { glyph: '○', color: colors.inkDarkMuted },
  hired: { glyph: '→', color: colors.statusInfo },
  warn: { glyph: '●', color: colors.statusWarn },
  danger: { glyph: '●', color: colors.statusDanger },
};

export const StatusDot = ({ kind = 'live' }: { kind?: DotKind }) => {
  const d = DOT[kind];
  return <Text style={{ color: d.color, fontSize: 10 }}>{d.glyph}</Text>;
};
