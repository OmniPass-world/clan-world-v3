import React from 'react';
import { Pressable, ScrollView, Text, View } from 'react-native';
import { ARCHETYPE_GLYPHS, Inft, WHISPERS } from '../data';
import { ArchetypeGlyph } from '../components/ArchetypeGlyph';
import { Btn } from '../components/Buttons';
import { Pill } from '../components/Pill';
import { Parchment } from '../components/Surfaces';
import { ResStrip } from '../components/Resources';
import { TopBar } from '../components/TopBar';
import { TickProgress } from '../components/TickProgress';
import { SectionHeader } from '../components/SectionHeader';
import { ParchmentRule } from '../components/Diamond';
import { colors, fonts } from '../theme';

type Props = {
  inft: Inft;
  isEmpty?: boolean;
  onEnterCockpit: () => void;
  onWhispers: () => void;
  onSettings: () => void;
  navigate: (target: 'forge' | 'bazaar' | 'hearth') => void;
};

export const HearthScreen = ({
  inft,
  isEmpty = false,
  onEnterCockpit,
  onWhispers,
  onSettings,
  navigate,
}: Props) => (
  <>
    <TopBar
      title="HEARTH"
      right={
        <>
          <Pressable onPress={onWhispers} hitSlop={8} style={{ padding: 6 }}>
            <Text style={{ fontSize: 16, color: colors.inkDark }}>♪</Text>
            <View
              style={{
                position: 'absolute',
                top: 4,
                right: 4,
                width: 6,
                height: 6,
                borderRadius: 3,
                backgroundColor: colors.statusDanger,
              }}
            />
          </Pressable>
          <Pressable onPress={onSettings} hitSlop={8} style={{ padding: 6 }}>
            <Text style={{ fontSize: 15, color: colors.inkDark }}>⚙</Text>
          </Pressable>
        </>
      }
    />
    <ScrollView
      contentContainerStyle={{ padding: 14, paddingBottom: 20, gap: 18 }}
      showsVerticalScrollIndicator={false}
    >
      {/* Hero card */}
      {!isEmpty && inft.resources && inft.movements ? (
        <Parchment deep style={{ padding: 0 }}>
          <View style={{ paddingHorizontal: 14, paddingTop: 12, paddingBottom: 10 }}>
            <View
              style={{
                flexDirection: 'row',
                alignItems: 'center',
                justifyContent: 'space-between',
              }}
            >
              <Text
                style={{
                  fontFamily: fonts.mono,
                  fontSize: 10,
                  color: colors.inkParchmentMuted,
                }}
              >
                TICK {inft.gameTick} · SEASON {inft.season}
              </Text>
              <Text
                style={{
                  fontFamily: fonts.mono,
                  fontSize: 10,
                  color: colors.inkParchmentMuted,
                }}
              >
                {Math.round((inft.seasonPct ?? 0) * 100)}%
              </Text>
            </View>
            <View style={{ height: 8 }} />
            <TickProgress pct={inft.seasonPct ?? 0} dark={false} />
          </View>
          <View
            style={{
              paddingHorizontal: 14,
              paddingTop: 14,
              paddingBottom: 12,
              flexDirection: 'row',
              gap: 14,
              alignItems: 'center',
            }}
          >
            <ArchetypeGlyph kind={inft.archetype} size={56} />
            <View style={{ flex: 1, gap: 4 }}>
              <Text
                style={{
                  fontFamily: fonts.display,
                  fontSize: 26,
                  color: colors.inkParchment,
                  letterSpacing: 1,
                }}
              >
                {inft.name}
              </Text>
              <Text
                style={{
                  fontFamily: fonts.script,
                  fontStyle: 'italic',
                  fontSize: 14,
                  color: colors.scriptBlueDark,
                }}
              >
                {ARCHETYPE_GLYPHS[inft.archetype].name}
              </Text>
            </View>
            <Pill kind="live">● LIVE</Pill>
          </View>
          <View style={{ paddingHorizontal: 14, paddingBottom: 12 }}>
            <ResStrip values={inft.resources} />
          </View>
          <ParchmentRule />
          <View style={{ paddingHorizontal: 14, paddingTop: 10, paddingBottom: 12 }}>
            <Text
              style={{
                fontFamily: fonts.display,
                fontSize: 9,
                color: colors.inkParchmentMuted,
                letterSpacing: 1.4,
                marginBottom: 6,
              }}
            >
              ASSET MOVEMENTS
            </Text>
            <View style={{ gap: 6 }}>
              {inft.movements.map((m, i) => (
                <View
                  key={i}
                  style={{
                    flexDirection: 'row',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                  }}
                >
                  <Text
                    style={{
                      fontFamily: fonts.body,
                      fontSize: 12,
                      color:
                        m.kind === 'danger'
                          ? colors.statusDanger
                          : m.kind === 'live'
                          ? colors.statusLiveDeep
                          : colors.inkParchment,
                    }}
                  >
                    {m.text}
                  </Text>
                  <Text
                    style={{
                      fontFamily: fonts.mono,
                      fontSize: 10,
                      color: colors.inkParchmentMuted,
                    }}
                  >
                    {m.t}
                  </Text>
                </View>
              ))}
            </View>
          </View>
          <View
            style={{
              padding: 12,
              backgroundColor: 'rgba(26,20,16,0.06)',
              borderTopWidth: 1,
              borderTopColor: 'rgba(26,20,16,0.15)',
            }}
          >
            <Btn variant="primary" block onPress={onEnterCockpit}>
              ENTER COCKPIT →
            </Btn>
          </View>
        </Parchment>
      ) : (
        <Parchment deep>
          <View style={{ alignItems: 'center', gap: 10, paddingVertical: 6 }}>
            <Text
              style={{
                fontFamily: fonts.script,
                fontStyle: 'italic',
                fontSize: 18,
                color: colors.scriptBlueDark,
              }}
            >
              Your hall is silent.
            </Text>
            <Text
              style={{
                fontFamily: fonts.body,
                fontSize: 13,
                color: colors.inkParchment,
                maxWidth: 240,
                textAlign: 'center',
              }}
            >
              No Elder is in the realm. Send one, or hire from the Bazaar.
            </Text>
            <View style={{ height: 6 }} />
            <View style={{ flexDirection: 'row', gap: 8, alignSelf: 'stretch' }}>
              <Btn
                variant="secondary"
                style={{ flex: 1, borderColor: colors.inkParchmentMuted }}
                onPress={() => navigate('bazaar')}
              >
                BAZAAR
              </Btn>
              <Btn variant="primary" style={{ flex: 1 }} onPress={() => navigate('forge')}>
                FORGE
              </Btn>
            </View>
          </View>
        </Parchment>
      )}

      {/* Recent whispers */}
      <View style={{ gap: 10 }}>
        <SectionHeader action="View all →" onAction={onWhispers}>
          RECENT WHISPERS
        </SectionHeader>
        <View>
          {WHISPERS.slice(0, 4).map((w, i) => (
            <View
              key={w.id}
              style={{
                flexDirection: 'row',
                alignItems: 'center',
                gap: 10,
                paddingVertical: 10,
                paddingHorizontal: 4,
                borderBottomWidth: i < 3 ? 1 : 0,
                borderBottomColor: 'rgba(212,175,92,0.1)',
              }}
            >
              <Text
                style={{
                  fontSize: 16,
                  width: 18,
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
                <Text style={{ fontFamily: fonts.body, fontSize: 13, color: colors.inkDark }}>
                  {w.title}
                </Text>
                <Text
                  numberOfLines={1}
                  style={{ fontFamily: fonts.body, fontSize: 11, color: colors.inkDarkMuted }}
                >
                  {w.snippet}
                </Text>
              </View>
              <Text style={{ fontFamily: fonts.mono, fontSize: 10, color: colors.inkDarkFaint }}>
                {w.t}
              </Text>
            </View>
          ))}
        </View>
      </View>

      {/* Standing */}
      <View style={{ gap: 10 }}>
        <SectionHeader>YOUR STANDING</SectionHeader>
        <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
          {[
            { l: 'TOTAL ELDERS', v: '4', d: '' },
            { l: 'GOLD WON', v: '247', d: '+14 this week' },
            { l: 'AVG ELO', v: '1289', d: '+12' },
            { l: 'ACTIVE SEASONS', v: '1', d: 'of 4 owned' },
          ].map((s, i) => (
            <Parchment
              key={i}
              deep
              style={{ padding: 12, width: '48%', flexGrow: 1 }}
            >
              <Text
                style={{
                  fontFamily: fonts.mono,
                  fontSize: 9,
                  color: colors.inkParchmentMuted,
                  letterSpacing: 1,
                }}
              >
                {s.l}
              </Text>
              <Text
                style={{
                  fontFamily: fonts.monoBold,
                  fontSize: 22,
                  color: colors.inkParchment,
                  marginTop: 4,
                }}
              >
                {s.v}
              </Text>
              {s.d ? (
                <Text
                  style={{
                    fontFamily: fonts.mono,
                    fontSize: 9,
                    color: colors.statusLiveDeep,
                    marginTop: 2,
                  }}
                >
                  {s.d}
                </Text>
              ) : null}
            </Parchment>
          ))}
        </View>
      </View>
    </ScrollView>
  </>
);
