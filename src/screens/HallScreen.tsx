import React, { useMemo, useState } from 'react';
import { ScrollView, Text, View } from 'react-native';
import { Inft, MOCK_INFTS } from '../data';
import { Btn, Chip } from '../components/Buttons';
import { InftCard } from '../components/InftCard';
import { Parchment } from '../components/Surfaces';
import { TopBar } from '../components/TopBar';
import { colors, fonts } from '../theme';

type Filter = 'ALL' | 'ACTIVE' | 'IDLE' | 'RENTED';

type Props = {
  onOpenInft: (inft: Inft) => void;
  onForge: () => void;
};

export const HallScreen = ({ onOpenInft, onForge }: Props) => {
  const [filter, setFilter] = useState<Filter>('ALL');
  const filters: Filter[] = ['ALL', 'ACTIVE', 'IDLE', 'RENTED'];
  const list = useMemo(
    () =>
      MOCK_INFTS.filter((i) => {
        if (filter === 'ALL') return true;
        if (filter === 'ACTIVE') return i.state === 'in-game';
        if (filter === 'IDLE') return i.state === 'idle';
        if (filter === 'RENTED') return i.state === 'rented';
        return true;
      }),
    [filter]
  );

  return (
    <>
      <TopBar
        title="MY HALL"
        right={
          <Btn
            variant="plain"
            paddingHorizontal={10}
            paddingVertical={6}
            fontSize={10}
            onPress={onForge}
          >
            + FORGE
          </Btn>
        }
      />

      <View style={{ paddingHorizontal: 14, paddingTop: 12, paddingBottom: 8 }}>
        <ScrollView
          horizontal
          showsHorizontalScrollIndicator={false}
          contentContainerStyle={{ gap: 6, paddingBottom: 4 }}
        >
          {filters.map((f) => (
            <Chip key={f} active={filter === f} onPress={() => setFilter(f)}>
              {f}
            </Chip>
          ))}
          <View style={{ flex: 1 }} />
          <Chip color={colors.goldPrimary} borderColor={colors.goldDeep}>
            BY ELO ▾
          </Chip>
        </ScrollView>
      </View>

      <ScrollView
        contentContainerStyle={{ paddingHorizontal: 14, paddingBottom: 20, gap: 10 }}
      >
        {list.map((inft) => (
          <InftCard key={inft.id} inft={inft} onPress={() => onOpenInft(inft)} />
        ))}
        {list.length === 0 && (
          <Parchment deep style={{ marginTop: 20, alignItems: 'center', padding: 24 }}>
            <Text
              style={{
                fontFamily: fonts.script,
                fontStyle: 'italic',
                fontSize: 16,
                color: colors.scriptBlueDark,
              }}
            >
              No Elders match this filter.
            </Text>
          </Parchment>
        )}
      </ScrollView>
    </>
  );
};
