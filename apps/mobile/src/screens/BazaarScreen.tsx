import React, { useState } from 'react';
import { Pressable, ScrollView, Text, View } from 'react-native';
import { Inft } from '../data';
import { BAZAAR_HIRE_INFTS, BAZAAR_SALE_INFTS } from '../bazaarInfts';
import { Chip } from '../components/Buttons';
import { InftCard } from '../components/InftCard';
import { TopBar } from '../components/TopBar';
import { colors, fonts } from '../theme';

type Tab = 'FOR HIRE' | 'FOR SALE';

type Props = { onOpenInft: (inft: Inft) => void };

export const BazaarScreen = ({ onOpenInft }: Props) => {
  const [tab, setTab] = useState<Tab>('FOR HIRE');
  const list = tab === 'FOR HIRE' ? BAZAAR_HIRE_INFTS : BAZAAR_SALE_INFTS;

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
            {tab === 'FOR HIRE' ? 'SORT: TOP RANKED ▾' : 'SORT: PRICE ▾'}
          </Chip>
        </ScrollView>
      </View>

      <ScrollView contentContainerStyle={{ paddingHorizontal: 14, paddingBottom: 20, gap: 10 }}>
        {list.map((i) => (
          <InftCard
            key={i.id}
            inft={i}
            onPress={() => onOpenInft(i)}
            hireFee={i.hireFee ?? i.salePrice}
            sparkline={i.sparkline}
          />
        ))}
      </ScrollView>
    </>
  );
};
