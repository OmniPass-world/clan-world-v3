import React, { ReactNode } from 'react';
import { Pressable, Text, View } from 'react-native';
import { colors, fonts, typo } from '../theme';
import { GoldRule } from './Diamond';

type Props = {
  children: ReactNode;
  action?: string;
  onAction?: () => void;
  dark?: boolean;
};

export const SectionHeader = ({ children, action, onAction, dark = true }: Props) => (
  <View style={{ gap: 6 }}>
    <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }}>
      <Text style={[typo.h3, { color: dark ? colors.inkDark : colors.inkParchment }]}>
        {children}
      </Text>
      {action && (
        <Pressable onPress={onAction} hitSlop={8}>
          <Text
            style={{
              fontFamily: fonts.script,
              fontSize: 13,
              color: colors.goldPrimary,
              fontStyle: 'italic',
            }}
          >
            {action}
          </Text>
        </Pressable>
      )}
    </View>
    <GoldRule opacity={dark ? 0.7 : 0.55} />
  </View>
);
