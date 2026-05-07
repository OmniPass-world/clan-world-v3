import React, { ReactNode } from 'react';
import { Pressable, Text, ViewStyle, StyleSheet, View } from 'react-native';
import { colors, fonts } from '../theme';

export type BtnVariant = 'primary' | 'secondary' | 'ghost' | 'plain';

type BtnProps = {
  variant?: BtnVariant;
  onPress?: () => void;
  disabled?: boolean;
  block?: boolean;
  style?: ViewStyle;
  children: ReactNode;
  fontSize?: number;
  paddingVertical?: number;
  paddingHorizontal?: number;
};

export const Btn = ({
  variant = 'primary',
  onPress,
  disabled,
  block,
  style,
  children,
  fontSize = 12,
  paddingVertical = 12,
  paddingHorizontal = 16,
}: BtnProps) => {
  const palette = {
    primary: { bg: '#1A1410', border: colors.goldPrimary, color: colors.goldBright },
    secondary: { bg: 'transparent', border: 'rgba(212, 175, 92, 0.55)', color: colors.inkDark },
    ghost: { bg: 'transparent', border: 'transparent', color: colors.goldPrimary },
    plain: { bg: colors.bgElevated, border: colors.goldPrimary, color: colors.goldBright },
  }[variant];

  return (
    <Pressable
      onPress={onPress}
      disabled={disabled}
      style={({ pressed }) => [
        {
          backgroundColor: palette.bg,
          borderColor: palette.border,
          borderWidth: variant === 'ghost' ? 0 : 1,
          paddingVertical,
          paddingHorizontal,
          opacity: disabled ? 0.4 : pressed ? 0.85 : 1,
          alignItems: 'center',
          justifyContent: 'center',
          flexDirection: 'row',
          gap: 8,
          borderRadius: 0,
        },
        block && { width: '100%' },
        style,
      ]}
    >
      {typeof children === 'string' ? (
        <Text
          style={{
            fontFamily: fonts.display,
            fontSize,
            color: palette.color,
            letterSpacing: 1.6,
            textTransform: 'uppercase',
          }}
        >
          {children}
        </Text>
      ) : (
        children
      )}
    </Pressable>
  );
};

type ChipProps = {
  children: ReactNode;
  active?: boolean;
  onPress?: () => void;
  style?: ViewStyle;
  color?: string;
  borderColor?: string;
};

export const Chip = ({ children, active, onPress, style, color, borderColor }: ChipProps) => (
  <Pressable
    onPress={onPress}
    style={({ pressed }) => [
      styles.chipBase,
      active
        ? { backgroundColor: colors.goldPrimary, borderColor: colors.goldPrimary }
        : {
            backgroundColor: 'transparent',
            borderColor: borderColor ?? 'rgba(212, 175, 92, 0.4)',
          },
      pressed && { opacity: 0.7 },
      style,
    ]}
  >
    <Text
      style={{
        fontFamily: fonts.display,
        fontSize: 10,
        letterSpacing: 1.2,
        textTransform: 'uppercase',
        color: active ? '#1A1410' : color ?? colors.inkDarkMuted,
      }}
    >
      {children}
    </Text>
  </Pressable>
);

const styles = StyleSheet.create({
  chipBase: {
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderWidth: 1,
    borderRadius: 2,
  },
});
