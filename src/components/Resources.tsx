import React from 'react';
import { Text, View } from 'react-native';
import { RESOURCES, ResourceKey } from '../data';
import { colors, fonts } from '../theme';

type ResIconProps = { kind: ResourceKey; size?: number };

export const ResIcon = ({ kind, size = 14 }: ResIconProps) => {
  const r = RESOURCES[kind];
  return (
    <Text
      style={{
        color: r.color,
        fontSize: size + 2,
        width: 18,
        textAlign: 'center',
      }}
    >
      {r.glyph}
    </Text>
  );
};

type ResStripProps = {
  values: Record<ResourceKey, number>;
  compact?: boolean;
  dark?: boolean;
};

export const ResStrip = ({ values, compact = false, dark = false }: ResStripProps) => (
  <View style={{ flexDirection: 'row', alignItems: 'center', gap: compact ? 8 : 12 }}>
    {(Object.entries(values) as [ResourceKey, number][]).map(([k, v]) => (
      <View key={k} style={{ flexDirection: 'row', alignItems: 'center', gap: 4 }}>
        <ResIcon kind={k} size={compact ? 12 : 13} />
        <Text
          style={{
            fontFamily: fonts.mono,
            fontSize: compact ? 10 : 11,
            color: dark ? colors.inkDark : colors.inkParchment,
          }}
        >
          {v}
        </Text>
      </View>
    ))}
  </View>
);
