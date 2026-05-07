import React from 'react';
import { Pressable, ScrollView, Text, View } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { TXS } from '../data';
import { Btn } from '../components/Buttons';
import { Diamond, ParchmentRule } from '../components/Diamond';
import { Parchment } from '../components/Surfaces';
import { ResIcon } from '../components/Resources';
import { SectionHeader } from '../components/SectionHeader';
import { TopBar } from '../components/TopBar';
import { colors, fonts } from '../theme';

type Props = { onBuyGold: () => void; onTx: () => void };

export const TreasuryScreen = ({ onBuyGold, onTx }: Props) => (
  <>
    <TopBar title="TREASURY" />
    <ScrollView contentContainerStyle={{ paddingHorizontal: 14, paddingVertical: 12, gap: 16 }}>
      {/* Identity */}
      <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 4 }}>
        <View
          style={{
            width: 44,
            height: 44,
            borderRadius: 22,
            alignItems: 'center',
            justifyContent: 'center',
            borderWidth: 1,
            borderColor: colors.goldPrimary,
            overflow: 'hidden',
          }}
        >
          <LinearGradient
            colors={[colors.aubergineDeep, colors.goldDeeper]}
            style={{
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
            }}
          />
          <Diamond size={12} color={colors.goldBright} />
        </View>
        <View style={{ flex: 1, gap: 2 }}>
          <Text style={{ fontFamily: fonts.display, fontSize: 16, color: colors.inkDark }}>
            aelder.sol
          </Text>
          <Text style={{ fontFamily: fonts.mono, fontSize: 10, color: colors.inkMono }}>
            0x4f2a…81d3 ⎘
          </Text>
        </View>
      </View>

      <Parchment deep style={{ padding: 0 }}>
        <View style={{ paddingHorizontal: 16, paddingTop: 14, paddingBottom: 12 }}>
          <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
            <ResIcon kind="gold" size={16} />
            <Text
              style={{
                fontFamily: fonts.display,
                fontSize: 11,
                letterSpacing: 1.6,
                color: colors.inkParchmentMuted,
              }}
            >
              GOLD
            </Text>
          </View>
          <View style={{ flexDirection: 'row', alignItems: 'baseline', gap: 6, marginTop: 4 }}>
            <Text
              style={{
                fontFamily: fonts.monoBold,
                fontSize: 38,
                color: colors.inkParchment,
                lineHeight: 40,
              }}
            >
              247
            </Text>
            <Text
              style={{ fontFamily: fonts.mono, fontSize: 11, color: colors.inkParchmentMuted }}
            >
              ≈ $24.70
            </Text>
          </View>
          <View style={{ marginVertical: 12 }}>
            <ParchmentRule />
          </View>
          <View
            style={{
              flexDirection: 'row',
              justifyContent: 'space-between',
              alignItems: 'center',
            }}
          >
            <Text
              style={{
                fontFamily: fonts.display,
                fontSize: 11,
                letterSpacing: 1.6,
                color: colors.inkParchmentMuted,
              }}
            >
              SOL · BASE SEPOLIA
            </Text>
            <Text style={{ fontFamily: fonts.mono, fontSize: 13, color: colors.inkParchment }}>
              0.42 / 0.018
            </Text>
          </View>
        </View>
        <View
          style={{
            padding: 12,
            backgroundColor: 'rgba(26,20,16,0.06)',
            borderTopWidth: 1,
            borderTopColor: 'rgba(26,20,16,0.18)',
          }}
        >
          <Btn variant="primary" block onPress={onBuyGold}>
            BUY GOLD ↗
          </Btn>
        </View>
      </Parchment>

      <View style={{ gap: 8 }}>
        <SectionHeader>RECENT MOVEMENTS</SectionHeader>
        <View>
          {TXS.map((t, i) => (
            <Pressable
              key={t.id}
              onPress={onTx}
              style={{
                flexDirection: 'row',
                alignItems: 'center',
                gap: 10,
                paddingVertical: 10,
                paddingHorizontal: 4,
                borderBottomWidth: i < TXS.length - 1 ? 1 : 0,
                borderBottomColor: 'rgba(212,175,92,0.1)',
              }}
            >
              <Text
                style={{
                  width: 20,
                  textAlign: 'center',
                  fontSize: 14,
                  color:
                    t.status === 'danger'
                      ? colors.statusDanger
                      : t.status === 'live'
                      ? colors.statusLive
                      : colors.goldPrimary,
                }}
              >
                {t.icon}
              </Text>
              <View style={{ flex: 1 }}>
                <Text style={{ fontFamily: fonts.body, fontSize: 13, color: colors.inkDark }}>
                  {t.title}
                </Text>
                <Text
                  style={{ fontFamily: fonts.body, fontSize: 11, color: colors.inkDarkMuted }}
                >
                  {t.sub}
                </Text>
              </View>
              <View style={{ alignItems: 'flex-end' }}>
                <Text
                  style={{
                    fontFamily: fonts.mono,
                    fontSize: 12,
                    color: t.amount.startsWith('+')
                      ? colors.statusLive
                      : t.amount.startsWith('−')
                      ? colors.statusDanger
                      : colors.inkDarkMuted,
                  }}
                >
                  {t.amount}
                </Text>
                <Text
                  style={{ fontFamily: fonts.mono, fontSize: 9, color: colors.inkDarkFaint }}
                >
                  {t.t}
                </Text>
              </View>
            </Pressable>
          ))}
        </View>
      </View>
    </ScrollView>
  </>
);
