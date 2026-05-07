import React from 'react';
import { View } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { colors } from '../theme';

type Props = { pct?: number; dark?: boolean };

export const TickProgress = ({ pct = 0.72, dark = true }: Props) => (
  <View
    style={{
      height: 3,
      backgroundColor: dark ? 'rgba(212,175,92,0.2)' : 'rgba(26,20,16,0.15)',
      overflow: 'hidden',
    }}
  >
    <LinearGradient
      colors={[colors.goldDeep, colors.goldBright]}
      start={{ x: 0, y: 0.5 }}
      end={{ x: 1, y: 0.5 }}
      style={{ width: `${pct * 100}%`, height: '100%' }}
    />
  </View>
);
