import React, { useEffect } from 'react';
import { Text, View } from 'react-native';
import { colors, fonts } from '../theme';

type Props = { text: string; onDismiss: () => void };

export const Toast = ({ text, onDismiss }: Props) => {
  useEffect(() => {
    const t = setTimeout(onDismiss, 2600);
    return () => clearTimeout(t);
  }, [onDismiss]);
  return (
    <View
      pointerEvents="none"
      style={{
        position: 'absolute',
        top: 16,
        left: 0,
        right: 0,
        alignItems: 'center',
        zIndex: 100,
      }}
    >
      <View
        style={{
          backgroundColor: colors.bgElevated,
          borderTopWidth: 1,
          borderTopColor: colors.goldPrimary,
          borderLeftWidth: 1,
          borderRightWidth: 1,
          borderBottomWidth: 1,
          borderColor: 'rgba(212,175,92,0.2)',
          paddingHorizontal: 14,
          paddingVertical: 8,
          maxWidth: 320,
          shadowColor: '#000',
          shadowOpacity: 0.5,
          shadowRadius: 12,
          shadowOffset: { width: 0, height: 8 },
          elevation: 10,
        }}
      >
        <Text
          style={{
            fontFamily: fonts.body,
            fontSize: 13,
            color: colors.inkDark,
            textAlign: 'center',
          }}
        >
          {text}
        </Text>
      </View>
    </View>
  );
};
