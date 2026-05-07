import React, { ReactNode } from 'react';
import { View, ViewStyle, StyleSheet } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { colors } from '../theme';

// Parchment surface — approximation of the noisy parchment-light/parchment styles
// from styles.css. We layer two radial-style gradients (top-left highlight, bottom-right
// shadow) to evoke the same visual feel without the SVG noise filter.

type ParchmentProps = {
  children: ReactNode;
  deep?: boolean;
  style?: ViewStyle;
  borderColor?: string;
  borderWidth?: number;
};

export const Parchment = ({
  children,
  deep = false,
  style,
  borderColor = colors.goldPrimary,
  borderWidth = 1,
}: ParchmentProps) => {
  const base = deep ? colors.bgParchmentDim : colors.bgParchment;
  const highlight = deep ? 'rgba(255, 240, 200, 0.18)' : 'rgba(255, 245, 215, 0.4)';
  const shadow = deep ? 'rgba(120, 80, 30, 0.12)' : 'rgba(120, 80, 30, 0.06)';
  return (
    <View
      style={[
        styles.parchmentBase,
        { backgroundColor: base, borderColor, borderWidth },
        style,
      ]}
    >
      {/* Highlight wash: top-left bright */}
      <LinearGradient
        colors={[highlight, 'transparent']}
        start={{ x: 0.15, y: 0.1 }}
        end={{ x: 0.7, y: 0.7 }}
        style={StyleSheet.absoluteFillObject}
        pointerEvents="none"
      />
      {/* Shadow wash: bottom-right deep */}
      <LinearGradient
        colors={['transparent', shadow]}
        start={{ x: 0.5, y: 0.5 }}
        end={{ x: 1, y: 1 }}
        style={StyleSheet.absoluteFillObject}
        pointerEvents="none"
      />
      <View style={{ position: 'relative', zIndex: 1 }}>{children}</View>
    </View>
  );
};

type StoneProps = {
  children: ReactNode;
  style?: ViewStyle;
  borderColor?: string;
  borderWidth?: number;
};

export const Stone = ({
  children,
  style,
  borderColor = 'rgba(212, 175, 92, 0.3)',
  borderWidth = 1,
}: StoneProps) => (
  <View
    style={[
      styles.stoneBase,
      { borderColor, borderWidth },
      style,
    ]}
  >
    {children}
  </View>
);

const styles = StyleSheet.create({
  parchmentBase: {
    borderRadius: 2,
    padding: 16,
    overflow: 'hidden',
  },
  stoneBase: {
    backgroundColor: colors.bgElevated,
    borderRadius: 2,
    padding: 16,
  },
});
