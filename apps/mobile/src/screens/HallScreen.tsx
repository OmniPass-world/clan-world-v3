import React, { useMemo, useState } from 'react';
import { ScrollView, Text, View } from 'react-native';
import { useQuery } from 'convex/react';
import { api } from '../convexApi';
import { Inft } from '../data';
import {
  forgedToInft,
  mergeRealClan,
  REAL_CLAN_DISPLAY,
  type SnapshotForHero,
} from '../clanData';
import { EXTRA_INFTS } from '../extraInfts';
import { getForgedInfts } from '../storage';
import { Btn, Chip } from '../components/Buttons';
import { InftCard } from '../components/InftCard';
import { Parchment } from '../components/Surfaces';
import { TopBar } from '../components/TopBar';
import { colors, fonts } from '../theme';

type Filter = 'ALL' | 'ACTIVE' | 'IDLE' | 'RENTED';

type Props = {
  pubkey: string | null;
  loadedInftId: string | null;
  onOpenInft: (inft: Inft) => void;
  onForge: () => void;
};

export const HallScreen = ({ pubkey, loadedInftId, onOpenInft, onForge }: Props) => {
  const [filter, setFilter] = useState<Filter>('ALL');
  const filters: Filter[] = ['ALL', 'ACTIVE', 'IDLE', 'RENTED'];

  // Live world snapshot — drives real clan vault/levels.
  const snapshot = useQuery(api.getSnapshot.getSnapshot) as
    | SnapshotForHero
    | null
    | undefined;

  // Real clans (4) — merge cockpit-tokens display data + live snapshot + mock fallback.
  const realInfts: Inft[] = useMemo(() => {
    return REAL_CLAN_DISPLAY.map((display) => {
      const snapClan = snapshot?.clans.find(
        (c) => Number(c.id) === display.clanId,
      );
      return mergeRealClan(display, snapClan, snapshot ?? null);
    });
  }, [snapshot]);

  // Forged INFTs — read from MMKV, scoped to this user's pubkey.
  const forgedInfts: Inft[] = useMemo(
    () => getForgedInfts(pubkey).map(forgedToInft),
    [pubkey, snapshot?.tick], // tick refresh makes Hall pick up newly-forged INFTs after the seal animation
  );

  // Order: real live clans first, then user's freshly-forged Elders, then
  // the static roster of pre-populated Elders so the demo Hall has depth
  // to scroll through.
  const all = useMemo(
    () => [...realInfts, ...forgedInfts, ...EXTRA_INFTS],
    [realInfts, forgedInfts],
  );

  const list = useMemo(
    () =>
      all.filter((i) => {
        if (filter === 'ALL') return true;
        if (filter === 'ACTIVE') return i.state === 'in-game';
        if (filter === 'IDLE') return i.state === 'idle';
        if (filter === 'RENTED') return i.state === 'rented';
        return true;
      }),
    [all, filter],
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
        {snapshot === undefined && (
          <Parchment deep style={{ marginTop: 8, alignItems: 'center', padding: 18 }}>
            <Text
              style={{
                fontFamily: fonts.script,
                fontStyle: 'italic',
                fontSize: 14,
                color: colors.scriptBlueDark,
              }}
            >
              Reading the realm…
            </Text>
          </Parchment>
        )}

        {list.map((inft) => {
          const isLoaded = inft.id === loadedInftId;
          return (
            <View
              key={inft.id}
              style={
                isLoaded
                  ? {
                      // Subtle highlight for the currently-loaded clan
                      borderColor: colors.goldBright,
                      borderWidth: 1,
                    }
                  : undefined
              }
            >
              <InftCard inft={inft} onPress={() => onOpenInft(inft)} />
            </View>
          );
        })}

        {list.length === 0 && snapshot !== undefined && (
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
