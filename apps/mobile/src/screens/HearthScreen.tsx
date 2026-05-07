import React, { useMemo } from 'react';
import { Pressable, ScrollView, Text, View } from 'react-native';
import { useQuery } from 'convex/react';
import { api } from '../convexApi';
import { ARCHETYPE_GLYPHS, Inft } from '../data';
import {
  forgedToInft,
  isForgedInft,
  mergeRealClan,
  REAL_CLAN_DISPLAY,
  type SnapshotForHero,
} from '../clanData';
import { findExtraInft } from '../extraInfts';
import {
  getForgedInfts,
  getFreeForgeUsed,
  realClanIdFromInftId,
} from '../storage';
import { ArchetypeGlyph } from '../components/ArchetypeGlyph';
import { Btn } from '../components/Buttons';
import { Pill } from '../components/Pill';
import { Parchment } from '../components/Surfaces';
import { ResStrip } from '../components/Resources';
import { TopBar } from '../components/TopBar';
import { TickProgress } from '../components/TickProgress';
import { SectionHeader } from '../components/SectionHeader';
import { Diamond, ParchmentRule } from '../components/Diamond';
import { colors, fonts } from '../theme';

type Props = {
  loadedInftId: string | null;
  pubkey: string | null;
  /** True when the user is a Seeker bearer (M1 instant or M2 verified). */
  isSeekerBearer: boolean;
  /** Active raid info — banner appears at the top of the hero card while
   *  this is non-null. Cleared by entering the cockpit or dismissing. */
  raid: { victim: string; tick: number } | null;
  onEnterCockpit: (inft: Inft) => void;
  onWhispers: () => void;
  onSettings: () => void;
  navigate: (target: 'forge' | 'bazaar' | 'hearth' | 'hall') => void;
};

export const HearthScreen = ({
  loadedInftId,
  pubkey,
  isSeekerBearer,
  raid,
  onEnterCockpit,
  onWhispers,
  onSettings,
  navigate,
}: Props) => {
  const freeForgeUsed = pubkey ? getFreeForgeUsed(pubkey) : false;
  const showSeekerNudge = isSeekerBearer && !freeForgeUsed;
  const realClanId = realClanIdFromInftId(loadedInftId);
  const isForged =
    !!loadedInftId &&
    (loadedInftId.startsWith('forged-') || loadedInftId.startsWith('extra-'));

  // Live snapshot — used both for the loaded-real-clan Hero data and as a
  // ticker. Always queried so the rest of the UI stays warm.
  const snapshot = useQuery(api.getSnapshot.getSnapshot) as
    | SnapshotForHero
    | null
    | undefined;

  // Resolve the loaded INFT.
  const loadedInft: Inft | null = useMemo(() => {
    if (!loadedInftId) return null;
    if (realClanId !== null) {
      const display = REAL_CLAN_DISPLAY.find((d) => d.clanId === realClanId);
      if (!display) return null;
      const snapClan = snapshot?.clans.find((c) => Number(c.id) === realClanId);
      return mergeRealClan(display, snapClan, snapshot ?? null);
    }
    if (isForged) {
      // Static pre-populated extras (extra-…) live in code, not MMKV.
      if (loadedInftId.startsWith('extra-')) {
        return findExtraInft(loadedInftId);
      }
      // User-forged INFTs (forged-…) live in MMKV scoped by pubkey.
      if (pubkey) {
        const all = getForgedInfts(pubkey);
        const f = all.find((entry) => entry.id === loadedInftId);
        return f ? forgedToInft(f) : null;
      }
    }
    return null;
  }, [loadedInftId, realClanId, isForged, snapshot, pubkey]);

  const isEmpty = !loadedInft;

  return (
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
        {/* Active raid — full-width danger banner at the top of the hero
            scroll. Renders only while `raid` is non-null. Tap → cockpit. */}
        {raid && (
          <Pressable
            onPress={() => {
              if (loadedInft && !isForged) onEnterCockpit(loadedInft);
              else navigate('hall');
            }}
          >
            <View
              style={{
                backgroundColor: 'rgba(160, 74, 63, 0.18)',
                borderColor: colors.statusDanger,
                borderWidth: 1,
                padding: 14,
                flexDirection: 'row',
                alignItems: 'center',
                gap: 12,
              }}
            >
              <Text
                style={{
                  fontSize: 28,
                  color: colors.statusDanger,
                  textShadowColor: colors.statusDanger,
                  textShadowOffset: { width: 0, height: 0 },
                  textShadowRadius: 10,
                }}
              >
                ⚔
              </Text>
              <View style={{ flex: 1, gap: 2 }}>
                <Text
                  style={{
                    fontFamily: fonts.display,
                    fontSize: 11,
                    letterSpacing: 1.6,
                    color: colors.statusDanger,
                  }}
                >
                  BANDIT RAID · T{raid.tick}
                </Text>
                <Text
                  style={{
                    fontFamily: fonts.body,
                    fontSize: 13,
                    color: colors.inkDark,
                  }}
                >
                  {raid.victim} is under attack.
                </Text>
                <Text
                  style={{
                    fontFamily: fonts.bodyItalic,
                    fontStyle: 'italic',
                    fontSize: 11,
                    color: colors.inkDarkMuted,
                    marginTop: 2,
                  }}
                >
                  Tap to defend in the cockpit.
                </Text>
              </View>
              <Text
                style={{
                  fontFamily: fonts.display,
                  fontSize: 14,
                  color: colors.statusDanger,
                }}
              >
                →
              </Text>
            </View>
          </Pressable>
        )}

        {/* Seeker free-forge nudge — sits above the hero, only when the
            connected wallet is a Seeker bearer who hasn't yet claimed their
            free Elder. Tapping routes straight into the Forge flow. */}
        {showSeekerNudge && (
          <Pressable onPress={() => navigate('forge')}>
            <Parchment
              deep
              style={{
                padding: 0,
                overflow: 'hidden',
                shadowColor: colors.goldBright,
                shadowOpacity: 0.55,
                shadowRadius: 28,
                shadowOffset: { width: 0, height: 0 },
                elevation: 14,
              }}
              borderColor={colors.goldBright}
              borderWidth={2}
            >
              {/* Top ribbon — Seeker recognition crest */}
              <View
                style={{
                  flexDirection: 'row',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: 8,
                  paddingVertical: 8,
                  backgroundColor: 'rgba(212, 175, 92, 0.16)',
                  borderBottomWidth: 1,
                  borderBottomColor: 'rgba(212, 175, 92, 0.35)',
                }}
              >
                <Diamond size={5} color={colors.goldDeep} />
                <Text
                  style={{
                    fontFamily: fonts.display,
                    fontSize: 10,
                    letterSpacing: 2.4,
                    color: colors.goldDeep,
                  }}
                >
                  SEEKER BEARER RECOGNIZED
                </Text>
                <Diamond size={5} color={colors.goldDeep} />
              </View>

              {/* Body — large display headline + script subtitle */}
              <View
                style={{
                  paddingHorizontal: 20,
                  paddingTop: 18,
                  paddingBottom: 8,
                  alignItems: 'center',
                  gap: 6,
                }}
              >
                <Text
                  style={{
                    fontFamily: fonts.script,
                    fontStyle: 'italic',
                    fontSize: 22,
                    color: colors.scriptBlueDark,
                    textAlign: 'center',
                    lineHeight: 26,
                  }}
                >
                  Your first Elder is forged in our hearth, not yours.
                </Text>
                <Text
                  style={{
                    fontFamily: fonts.bodyItalic,
                    fontStyle: 'italic',
                    fontSize: 12,
                    color: colors.inkParchmentMuted,
                    textAlign: 'center',
                    marginTop: 2,
                  }}
                >
                  The Hearth honors those who carry the realm in their pocket.
                </Text>
              </View>

              {/* Diamond divider */}
              <View
                style={{
                  flexDirection: 'row',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: 10,
                  paddingHorizontal: 28,
                  paddingVertical: 10,
                }}
              >
                <View
                  style={{
                    flex: 1,
                    height: 1,
                    backgroundColor: colors.goldDeep,
                    opacity: 0.4,
                  }}
                />
                <Diamond size={5} color={colors.goldDeep} />
                <View
                  style={{
                    flex: 1,
                    height: 1,
                    backgroundColor: colors.goldDeep,
                    opacity: 0.4,
                  }}
                />
              </View>

              {/* Strong CTA — forge button stretches the card width */}
              <View
                style={{
                  paddingHorizontal: 16,
                  paddingTop: 4,
                  paddingBottom: 16,
                  gap: 6,
                }}
              >
                <View
                  style={{
                    backgroundColor: '#1A1410',
                    borderColor: colors.goldBright,
                    borderWidth: 1.5,
                    paddingVertical: 14,
                    alignItems: 'center',
                    shadowColor: colors.goldBright,
                    shadowOpacity: 0.4,
                    shadowRadius: 12,
                    shadowOffset: { width: 0, height: 0 },
                    elevation: 6,
                  }}
                >
                  <Text
                    style={{
                      fontFamily: fonts.display,
                      fontSize: 14,
                      letterSpacing: 2.4,
                      color: colors.goldBright,
                    }}
                  >
                    CLAIM YOUR FREE FORGE →
                  </Text>
                </View>
                <Text
                  style={{
                    fontFamily: fonts.mono,
                    fontSize: 9,
                    letterSpacing: 1.2,
                    color: colors.inkParchmentMuted,
                    textAlign: 'center',
                    marginTop: 4,
                  }}
                >
                  MINT FEE · {'̶'}5 GOLD{'̶'} · WAIVED
                </Text>
              </View>
            </Parchment>
          </Pressable>
        )}

        {/* Hero card */}
        {loadedInft && !isForged ? (
          // Loaded a live clan — render Hero with real Convex data
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
                  TICK {loadedInft.gameTick ?? '—'} · SEASON {loadedInft.season ?? '—'}
                </Text>
                <Text
                  style={{
                    fontFamily: fonts.mono,
                    fontSize: 10,
                    color: colors.inkParchmentMuted,
                  }}
                >
                  {Math.round((loadedInft.seasonPct ?? 0) * 100)}%
                </Text>
              </View>
              <View style={{ height: 8 }} />
              <TickProgress pct={loadedInft.seasonPct ?? 0} dark={false} />
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
              <ArchetypeGlyph kind={loadedInft.archetype} size={56} />
              <View style={{ flex: 1, gap: 4 }}>
                <Text
                  style={{
                    fontFamily: fonts.display,
                    fontSize: 26,
                    color: colors.inkParchment,
                    letterSpacing: 1,
                  }}
                >
                  {loadedInft.name}
                </Text>
                <Text
                  style={{
                    fontFamily: fonts.script,
                    fontStyle: 'italic',
                    fontSize: 14,
                    color: colors.scriptBlueDark,
                  }}
                >
                  {ARCHETYPE_GLYPHS[loadedInft.archetype].name}
                </Text>
              </View>
              <Pill kind="live">● LIVE</Pill>
            </View>
            {loadedInft.resources && (
              <View style={{ paddingHorizontal: 14, paddingBottom: 12 }}>
                <ResStrip values={loadedInft.resources} />
              </View>
            )}
            <ParchmentRule />
            {loadedInft.movements && loadedInft.movements.length > 0 && (
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
                  {loadedInft.movements.slice(0, 3).map((m, i) => (
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
                          flex: 1,
                          marginRight: 8,
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
            )}
            <View
              style={{
                padding: 12,
                backgroundColor: 'rgba(26,20,16,0.06)',
                borderTopWidth: 1,
                borderTopColor: 'rgba(26,20,16,0.15)',
              }}
            >
              <Btn variant="primary" block onPress={() => onEnterCockpit(loadedInft)}>
                ENTER COCKPIT →
              </Btn>
            </View>
          </Parchment>
        ) : loadedInft && isForged ? (
          // Loaded a forged INFT — show its dossier but explain it's awaiting a season
          <Parchment deep style={{ padding: 0 }}>
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
              <ArchetypeGlyph kind={loadedInft.archetype} size={56} />
              <View style={{ flex: 1, gap: 4 }}>
                <Text
                  style={{
                    fontFamily: fonts.display,
                    fontSize: 26,
                    color: colors.inkParchment,
                    letterSpacing: 1,
                  }}
                >
                  {loadedInft.name}
                </Text>
                <Text
                  style={{
                    fontFamily: fonts.script,
                    fontStyle: 'italic',
                    fontSize: 14,
                    color: colors.scriptBlueDark,
                  }}
                >
                  {ARCHETYPE_GLYPHS[loadedInft.archetype].name}
                </Text>
              </View>
              <Pill kind="idle">○ IDLE</Pill>
            </View>
            <ParchmentRule />
            <View style={{ padding: 14 }}>
              <Text
                style={{
                  fontFamily: fonts.bodyItalic,
                  fontStyle: 'italic',
                  fontSize: 12,
                  color: colors.inkParchmentMuted,
                  textAlign: 'center',
                  marginBottom: 10,
                }}
              >
                {loadedInft.name} awaits a season. Deployment opens in the next slice.
              </Text>
              <Btn variant="secondary" block disabled>
                AWAITS A SEASON
              </Btn>
            </View>
          </Parchment>
        ) : (
          // Empty state — no INFT loaded
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
                Load an Elder from your Hall, or forge a fresh one.
              </Text>
              <View style={{ height: 6 }} />
              <View style={{ flexDirection: 'row', gap: 8, alignSelf: 'stretch' }}>
                <Btn
                  variant="secondary"
                  style={{ flex: 1, borderColor: colors.inkParchmentMuted }}
                  onPress={() => navigate('hall')}
                >
                  <Text
                    style={{
                      fontFamily: fonts.display,
                      fontSize: 12,
                      letterSpacing: 1.6,
                      color: colors.inkParchment,
                    }}
                  >
                    HALL
                  </Text>
                </Btn>
                <Btn
                  variant="primary"
                  style={{ flex: 1 }}
                  onPress={() => navigate('forge')}
                >
                  FORGE
                </Btn>
              </View>
            </View>
          </Parchment>
        )}

        {/* Standing — static for slice 1 (real values come with slice 2's leaderboard wiring) */}
        <View style={{ gap: 10 }}>
          <SectionHeader>YOUR STANDING</SectionHeader>
          <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
            {[
              { l: 'TOTAL ELDERS', v: '4', d: '' },
              { l: 'GOLD WON', v: '247', d: '+14 this week' },
              { l: 'AVG ELO', v: '1289', d: '+12' },
              { l: 'ACTIVE SEASONS', v: '1', d: 'of 4 owned' },
            ].map((s, i) => (
              <Parchment key={i} deep style={{ padding: 12, width: '48%', flexGrow: 1 }}>
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
};
