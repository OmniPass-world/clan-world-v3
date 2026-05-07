import React from 'react';
import { View, ViewStyle } from 'react-native';
import { colors } from '../theme';

type Props = {
  size?: number;
  color?: string;
  style?: ViewStyle;
};

export const Diamond = ({ size = 6, color = colors.goldPrimary, style }: Props) => (
  <View
    style={[
      { width: size, height: size, backgroundColor: color, transform: [{ rotate: '45deg' }] },
      style,
    ]}
  />
);

type RowProps = { count?: number; color?: string; size?: number };

export const DiamondRow = ({ count = 3, color = colors.goldPrimary, size = 6 }: RowProps) => (
  <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'center' }}>
    <View
      style={{
        flex: 1,
        height: 1,
        maxWidth: 80,
        backgroundColor: color,
        opacity: 0.5,
      }}
    />
    <View style={{ flexDirection: 'row', alignItems: 'center', gap: 14, paddingHorizontal: 14 }}>
      {Array.from({ length: count }).map((_, i) => (
        <Diamond key={i} size={size} color={color} />
      ))}
    </View>
    <View style={{ flex: 1, height: 1, maxWidth: 80, backgroundColor: color, opacity: 0.5 }} />
  </View>
);

export const GoldRule = ({ opacity = 0.9 }: { opacity?: number }) => (
  <View style={{ height: 1, backgroundColor: colors.goldPrimary, opacity }} />
);

export const GoldRuleSoft = ({ opacity = 0.5 }: { opacity?: number }) => (
  <View style={{ height: 1, backgroundColor: colors.goldDeep, opacity }} />
);

export const ParchmentRule = ({ opacity = 0.25 }: { opacity?: number }) => (
  <View style={{ height: 1, backgroundColor: colors.inkParchment, opacity }} />
);
