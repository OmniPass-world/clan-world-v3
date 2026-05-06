import React, { ReactNode } from 'react';
import { Pressable, Text, View } from 'react-native';
import { colors, fonts, typo } from '../theme';

type Props = {
  title: string;
  onBack?: () => void;
  right?: ReactNode;
  sub?: string;
};

export const TopBar = ({ title, onBack, right, sub }: Props) => (
  <View
    style={{
      height: 52,
      paddingHorizontal: 16,
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'space-between',
      backgroundColor: colors.bgCanvas,
      borderBottomWidth: 1,
      borderBottomColor: 'rgba(212, 175, 92, 0.15)',
    }}
  >
    <View style={{ flexDirection: 'row', alignItems: 'center', gap: 10, flex: 1 }}>
      {onBack && (
        <Pressable onPress={onBack} hitSlop={8}>
          <Text style={{ color: colors.goldPrimary, fontSize: 22, padding: 4 }}>←</Text>
        </Pressable>
      )}
      <View>
        <Text style={[typo.h3, { color: colors.inkDark }]}>{title}</Text>
        {sub && (
          <Text style={{ fontFamily: fonts.mono, fontSize: 9, color: colors.inkDarkMuted }}>
            {sub}
          </Text>
        )}
      </View>
    </View>
    <View style={{ flexDirection: 'row', alignItems: 'center', gap: 10 }}>{right}</View>
  </View>
);
