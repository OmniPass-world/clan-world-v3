import React from 'react';
import { ScrollView, Text, View } from 'react-native';
import { Whisper, WHISPERS } from '../data';
import { Chip } from '../components/Buttons';
import { Diamond } from '../components/Diamond';
import { TopBar } from '../components/TopBar';
import { colors, fonts } from '../theme';

const DAY_LABELS: Record<Whisper['day'], string> = {
  today: 'TODAY',
  yesterday: 'YESTERDAY',
  'wed-6-may': 'WED · 6 MAY',
};

type Props = { onBack: () => void };

export const WhispersScreen = ({ onBack }: Props) => {
  const groups = WHISPERS.reduce<Record<string, Whisper[]>>((acc, w) => {
    acc[w.day] = acc[w.day] ?? [];
    acc[w.day].push(w);
    return acc;
  }, {});

  return (
    <>
      <TopBar title="WHISPERS" onBack={onBack} />
      <View style={{ paddingHorizontal: 14, paddingVertical: 10 }}>
        <ScrollView
          horizontal
          showsHorizontalScrollIndicator={false}
          contentContainerStyle={{ gap: 6 }}
        >
          <Chip active>ALL</Chip>
          <Chip>GAME</Chip>
          <Chip>MARKET</Chip>
          <Chip>SYSTEM</Chip>
        </ScrollView>
      </View>

      <ScrollView contentContainerStyle={{ paddingHorizontal: 14, paddingBottom: 20 }}>
        {Object.entries(groups).map(([day, items]) => (
          <View key={day} style={{ marginBottom: 14, gap: 4 }}>
            <Text
              style={{
                fontFamily: fonts.display,
                fontSize: 11,
                letterSpacing: 1.6,
                color: colors.inkDarkMuted,
                paddingVertical: 8,
              }}
            >
              {DAY_LABELS[day as Whisper['day']]}
            </Text>
            {items.map((w) => (
              <View
                key={w.id}
                style={{
                  flexDirection: 'row',
                  alignItems: 'center',
                  gap: 10,
                  paddingVertical: 10,
                  borderBottomWidth: 1,
                  borderBottomColor: 'rgba(212,175,92,0.1)',
                }}
              >
                {w.unread ? (
                  <Diamond size={5} color={colors.goldBright} />
                ) : (
                  <View style={{ width: 5 }} />
                )}
                <Text
                  style={{
                    fontSize: 16,
                    width: 20,
                    textAlign: 'center',
                    color:
                      w.kind === 'danger'
                        ? colors.statusDanger
                        : w.kind === 'live'
                        ? colors.statusLive
                        : w.kind === 'warn'
                        ? colors.statusWarn
                        : colors.goldPrimary,
                  }}
                >
                  {w.icon}
                </Text>
                <View style={{ flex: 1, minWidth: 0 }}>
                  <Text
                    style={{
                      fontFamily: w.unread ? fonts.bodyMed : fonts.body,
                      fontSize: 13,
                      color: colors.inkDark,
                    }}
                  >
                    {w.title}
                  </Text>
                  <Text
                    numberOfLines={1}
                    style={{ fontFamily: fonts.body, fontSize: 11, color: colors.inkDarkMuted }}
                  >
                    {w.snippet}
                  </Text>
                </View>
                <Text
                  style={{ fontFamily: fonts.mono, fontSize: 9, color: colors.inkDarkFaint }}
                >
                  {w.t}
                </Text>
              </View>
            ))}
          </View>
        ))}
      </ScrollView>
    </>
  );
};
