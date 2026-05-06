import React, { useState } from 'react';
import { Pressable, ScrollView, Text, View } from 'react-native';
import { BAZAAR_INFTS, Inft } from '../data';
import { Chip } from '../components/Buttons';
import { InftCard } from '../components/InftCard';
import { Parchment } from '../components/Surfaces';
import { TopBar } from '../components/TopBar';
import { colors, fonts } from '../theme';

type Tab = 'FOR HIRE' | 'FOR SALE';

type Props = { onOpenInft: (inft: Inft) => void };

export const BazaarScreen = ({ onOpenInft }: Props) => {
  const [tab, setTab] = useState<Tab>('FOR HIRE');
  return (
    <>
      <TopBar title="THE BAZAAR" />

      <View
        style={{
          flexDirection: 'row',
          borderBottomWidth: 1,
          borderBottomColor: 'rgba(212,175,92,0.2)',
        }}
      >
        {(['FOR HIRE', 'FOR SALE'] as Tab[]).map((t) => (
          <Pressable
            key={t}
            onPress={() => setTab(t)}
            style={{
              flex: 1,
              paddingVertical: 12,
              alignItems: 'center',
              borderBottomWidth: 2,
              borderBottomColor: tab === t ? colors.goldBright : 'transparent',
            }}
          >
            <Text
              style={{
                fontFamily: fonts.display,
                fontSize: 11,
                letterSpacing: 1.6,
                color: tab === t ? colors.goldBright : colors.inkDarkFaint,
              }}
            >
              {t}
            </Text>
          </Pressable>
        ))}
      </View>

      <View style={{ paddingHorizontal: 14, paddingVertical: 10 }}>
        <ScrollView
          horizontal
          showsHorizontalScrollIndicator={false}
          contentContainerStyle={{ gap: 6 }}
        >
          <Chip>ARCHETYPES ▾</Chip>
          <Chip>ELO 1000+ ▾</Chip>
          <Chip color={colors.goldPrimary} borderColor={colors.goldDeep}>
            SORT: TOP RANKED ▾
          </Chip>
        </ScrollView>
      </View>

      <ScrollView contentContainerStyle={{ paddingHorizontal: 14, paddingBottom: 20, gap: 10 }}>
        {tab === 'FOR HIRE' ? (
          BAZAAR_INFTS.map((i) => (
            <InftCard
              key={i.id}
              inft={i}
              onPress={() => onOpenInft(i)}
              hireFee={i.hireFee}
              sparkline={i.sparkline}
            />
          ))
        ) : (
          <Parchment deep style={{ marginTop: 20, alignItems: 'center', padding: 28 }}>
            <Text
              style={{
                fontFamily: fonts.script,
                fontStyle: 'italic',
                fontSize: 17,
                color: colors.scriptBlueDark,
              }}
            >
              The Bazaar is quiet.
            </Text>
            <Text
              style={{
                fontFamily: fonts.body,
                fontSize: 12,
                color: colors.inkParchment,
                marginTop: 8,
              }}
            >
              No Elders are for sale today.
            </Text>
          </Parchment>
        )}
      </ScrollView>
    </>
  );
};
