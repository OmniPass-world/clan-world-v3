import React from 'react';
import { Pressable, Text, View } from 'react-native';
import { colors, fonts } from '../theme';

export type TabKey = 'hearth' | 'hall' | 'bazaar' | 'treasury';

type Tab = { id: TabKey; label: string; glyph: string };

const TABS: Tab[] = [
  { id: 'hearth', label: 'HEARTH', glyph: '🜂' },
  { id: 'hall', label: 'HALL', glyph: '⛨' },
  { id: 'bazaar', label: 'BAZAAR', glyph: '⚖' },
  { id: 'treasury', label: 'TREASURY', glyph: '◈' },
];

type Props = {
  active: TabKey;
  onChange: (k: TabKey) => void;
};

export const TabBar = ({ active, onChange }: Props) => (
  <View
    style={{
      height: 64,
      flexDirection: 'row',
      backgroundColor: colors.bgCanvas,
      borderTopWidth: 1,
      borderTopColor: colors.goldPrimary,
    }}
  >
    {TABS.map((t) => {
      const on = active === t.id;
      return (
        <Pressable
          key={t.id}
          onPress={() => onChange(t.id)}
          style={{
            flex: 1,
            alignItems: 'center',
            justifyContent: 'center',
            gap: 4,
            paddingVertical: 6,
          }}
        >
          <Text style={{ fontSize: 20, color: on ? colors.goldBright : colors.inkDarkFaint }}>
            {t.glyph}
          </Text>
          <Text
            style={{
              fontFamily: fonts.display,
              fontSize: 9,
              letterSpacing: 1.4,
              color: on ? colors.goldBright : colors.inkDarkFaint,
            }}
          >
            {t.label}
          </Text>
          {on && (
            <View
              style={{
                position: 'absolute',
                bottom: 6,
                height: 1,
                width: 20,
                backgroundColor: colors.goldBright,
              }}
            />
          )}
        </Pressable>
      );
    })}
  </View>
);
