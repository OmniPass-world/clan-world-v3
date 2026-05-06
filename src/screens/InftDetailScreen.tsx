import React, { useState } from 'react';
import { Pressable, ScrollView, Text, View } from 'react-native';
import {
  ARCHETYPE_GLYPHS,
  Inft,
  STRATEGY_AXES,
  StrategyAxisKey,
  truncAddr,
} from '../data';
import { ArchetypeGlyph } from '../components/ArchetypeGlyph';
import { Btn } from '../components/Buttons';
import { Diamond, GoldRuleSoft, ParchmentRule } from '../components/Diamond';
import { Parchment, Stone } from '../components/Surfaces';
import { SectionHeader } from '../components/SectionHeader';
import { TopBar } from '../components/TopBar';
import { colors, fonts } from '../theme';

type Props = {
  inft: Inft;
  onBack: () => void;
  onEdit?: () => void;
  onWhisper?: () => void;
  onCockpit?: () => void;
  onHire?: () => void;
  isBazaar?: boolean;
};

type DetailTab = 'DOSSIER' | 'STRATEGY' | 'MEMORY' | 'HISTORY';

export const InftDetailScreen = ({
  inft,
  onBack,
  onEdit,
  onWhisper,
  onCockpit,
  onHire,
  isBazaar = false,
}: Props) => {
  const tabs: DetailTab[] = isBazaar
    ? ['DOSSIER', 'STRATEGY', 'HISTORY']
    : ['DOSSIER', 'STRATEGY', 'MEMORY', 'HISTORY'];
  const [tab, setTab] = useState<DetailTab>('DOSSIER');

  return (
    <View style={{ flex: 1 }}>
      <TopBar
        title={inft.name.toUpperCase()}
        onBack={onBack}
        right={
          <Pressable hitSlop={8}>
            <Text style={{ color: colors.inkDark, fontSize: 18 }}>⋯</Text>
          </Pressable>
        }
      />

      <ScrollView style={{ flex: 1 }} contentContainerStyle={{ paddingBottom: 12 }}>
        {/* Hero band */}
        <Parchment
          style={{
            paddingHorizontal: 16,
            paddingVertical: 18,
            alignItems: 'center',
            gap: 8,
            borderRadius: 0,
            borderTopWidth: 0,
            borderLeftWidth: 0,
            borderRightWidth: 0,
            borderBottomWidth: 1,
            borderBottomColor: colors.goldPrimary,
          }}
        >
          <ArchetypeGlyph kind={inft.archetype} size={70} />
          <Text
            style={{
              fontFamily: fonts.display,
              fontSize: 30,
              color: colors.inkParchment,
              letterSpacing: 1.2,
            }}
          >
            {inft.name}
          </Text>
          <Text
            style={{
              fontFamily: fonts.script,
              fontStyle: 'italic',
              fontSize: 15,
              color: colors.scriptBlueDark,
            }}
          >
            {ARCHETYPE_GLYPHS[inft.archetype].name}
          </Text>
          <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8, marginTop: 4 }}>
            <Text
              style={{
                fontFamily: fonts.mono,
                fontSize: 10,
                color: colors.inkParchmentMuted,
              }}
            >
              {truncAddr(inft.tokenId, 6, 6)}
            </Text>
            <Text style={{ fontSize: 9, color: colors.inkParchmentMuted }}>⎘</Text>
          </View>
          <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6, marginTop: 2 }}>
            <Text style={{ color: colors.statusLive, fontSize: 10 }}>●</Text>
            <Text
              style={{
                fontFamily: fonts.mono,
                fontSize: 9,
                color: colors.statusLive,
                letterSpacing: 1,
              }}
            >
              TEE-ATTESTED · ERC-7857
            </Text>
          </View>
        </Parchment>

        {/* Status strip */}
        <View
          style={{
            paddingHorizontal: 16,
            paddingVertical: 8,
            backgroundColor: colors.bgElevated,
            borderBottomWidth: 1,
            borderBottomColor: 'rgba(212,175,92,0.2)',
          }}
        >
          <Text
            style={{
              fontFamily: fonts.mono,
              fontSize: 10,
              letterSpacing: 1,
              color:
                inft.state === 'in-game' ? colors.statusLive : colors.inkDarkMuted,
            }}
          >
            {inft.state === 'in-game'
              ? `● IN GAME · TICK ${inft.gameTick} · SEASON ${inft.season}`
              : inft.state === 'rented'
              ? '→ HIRED OUT · SEASON 13'
              : `○ IDLE · LAST PLAYED ${inft.history?.[0]?.when ?? '—'}`}
          </Text>
        </View>

        {/* Tab strip */}
        <View
          style={{
            flexDirection: 'row',
            backgroundColor: colors.bgCanvas,
            borderBottomWidth: 1,
            borderBottomColor: 'rgba(212,175,92,0.2)',
            paddingHorizontal: 8,
          }}
        >
          {tabs.map((t) => (
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
                  fontSize: 10,
                  letterSpacing: 1.4,
                  color: tab === t ? colors.goldBright : colors.inkDarkFaint,
                }}
              >
                {t}
              </Text>
            </Pressable>
          ))}
        </View>

        <View style={{ padding: 14, gap: 14 }}>
          {tab === 'DOSSIER' && <DossierTab inft={inft} />}
          {tab === 'STRATEGY' && (
            <StrategyReadOnly
              inft={inft}
              onEdit={!isBazaar ? onEdit : undefined}
              onWhisper={!isBazaar ? onWhisper : undefined}
            />
          )}
          {tab === 'MEMORY' && <MemoryTab inft={inft} />}
          {tab === 'HISTORY' && <HistoryTab inft={inft} />}
        </View>
      </ScrollView>

      {/* Bottom action bar */}
      <View
        style={{
          padding: 12,
          backgroundColor: colors.bgCanvas,
          borderTopWidth: 1,
          borderTopColor: 'rgba(212,175,92,0.3)',
          flexDirection: 'row',
          gap: 8,
        }}
      >
        {isBazaar ? (
          <Btn variant="primary" block onPress={onHire}>
            {`HIRE · ${inft.hireFee ?? ''}`}
          </Btn>
        ) : inft.state === 'in-game' ? (
          <Btn variant="primary" block onPress={onCockpit}>
            OPEN COCKPIT →
          </Btn>
        ) : inft.state === 'rented' ? (
          <Btn variant="secondary" block>
            WITHDRAW AT SEASON END
          </Btn>
        ) : (
          <>
            <Btn variant="secondary" style={{ flex: 1 }}>
              LIST FOR HIRE
            </Btn>
            <Btn variant="primary" style={{ flex: 1 }}>
              ENTER A SEASON
            </Btn>
          </>
        )}
      </View>
    </View>
  );
};

const MiniStat = ({ l, v }: { l: string; v: string | number }) => (
  <Parchment
    deep
    style={{ padding: 10, alignItems: 'center', flex: 1, minWidth: 90 }}
  >
    <Text
      style={{
        fontFamily: fonts.mono,
        fontSize: 8,
        color: colors.inkParchmentMuted,
        letterSpacing: 1,
      }}
    >
      {l}
    </Text>
    <Text
      style={{
        fontFamily: fonts.monoBold,
        fontSize: 18,
        color: colors.inkParchment,
        marginTop: 2,
      }}
    >
      {v}
    </Text>
  </Parchment>
);

const DossierTab = ({ inft }: { inft: Inft }) => (
  <View style={{ gap: 14 }}>
    <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 6 }}>
      <MiniStat l="ELO" v={inft.elo} />
      <MiniStat l="SEASONS" v={inft.seasons} />
      <MiniStat l="LAST 10" v={`#${inft.last10}`} />
      <MiniStat l="BEST MONUMENT" v={inft.bestMonument ?? '—'} />
      <MiniStat l="BEST BASE" v={inft.bestBase ?? '—'} />
      <MiniStat l="CASUALTY %" v={inft.casualtyPct ?? '—'} />
    </View>

    <Parchment deep style={{ padding: 14 }}>
      <Text
        style={{
          fontFamily: fonts.bodyItalic,
          fontStyle: 'italic',
          fontSize: 14,
          color: colors.inkParchment,
          lineHeight: 20,
        }}
      >
        {inft.description}
      </Text>
    </Parchment>

    <Stone>
      <View style={{ gap: 8 }}>
        <View style={{ flexDirection: 'row', justifyContent: 'space-between' }}>
          <Text style={{ fontFamily: fonts.display, fontSize: 11, color: colors.inkDarkMuted, letterSpacing: 1.4 }}>
            OWNER
          </Text>
          <Text style={{ fontFamily: fonts.mono, fontSize: 11, color: colors.inkMono }}>
            {inft.owner}
          </Text>
        </View>
        <GoldRuleSoft />
        <View style={{ flexDirection: 'row', justifyContent: 'space-between' }}>
          <Text style={{ fontFamily: fonts.display, fontSize: 11, color: colors.inkDarkMuted, letterSpacing: 1.4 }}>
            MINTED
          </Text>
          <Text style={{ fontFamily: fonts.body, fontSize: 12, color: colors.inkDark }}>
            {inft.minted}
          </Text>
        </View>
        <GoldRuleSoft />
        <View style={{ flexDirection: 'row', justifyContent: 'space-between' }}>
          <Text style={{ fontFamily: fonts.display, fontSize: 11, color: colors.inkDarkMuted, letterSpacing: 1.4 }}>
            HARNESS
          </Text>
          <Text style={{ fontFamily: fonts.mono, fontSize: 11, color: colors.inkMono }}>
            CLAUDE-CODE · v1
          </Text>
        </View>
      </View>
    </Stone>
  </View>
);

const StrategyReadOnly = ({
  inft,
  onEdit,
  onWhisper,
}: {
  inft: Inft;
  onEdit?: () => void;
  onWhisper?: () => void;
}) => (
  <View style={{ gap: 14 }}>
    <Parchment deep>
      <View style={{ gap: 14 }}>
        {STRATEGY_AXES.map((ax) => {
          const v = inft.strategy?.[ax.key as StrategyAxisKey] ?? 0;
          const pct = ((v + 3) / 6) * 100;
          return (
            <View key={ax.key} style={{ gap: 4 }}>
              <View style={{ flexDirection: 'row', justifyContent: 'space-between' }}>
                <Text
                  style={{
                    fontFamily: fonts.display,
                    fontSize: 10,
                    letterSpacing: 1.4,
                    color: colors.inkParchment,
                  }}
                >
                  {ax.key.toUpperCase()}
                </Text>
                <Text
                  style={{
                    fontFamily: fonts.monoBold,
                    fontSize: 11,
                    color: colors.inkParchment,
                  }}
                >
                  {v > 0 ? `+${v}` : v}
                </Text>
              </View>
              <View style={{ height: 18, justifyContent: 'center' }}>
                <View
                  style={{
                    position: 'absolute',
                    left: 0,
                    right: 0,
                    height: 2,
                    top: '50%',
                    marginTop: -1,
                    backgroundColor: 'rgba(26,20,16,0.25)',
                  }}
                />
                <View
                  style={{
                    position: 'absolute',
                    left: `${pct}%`,
                    marginLeft: -5,
                    width: 10,
                    height: 10,
                    transform: [{ rotate: '45deg' }],
                    backgroundColor: colors.inkParchment,
                    top: '50%',
                    marginTop: -5,
                  }}
                />
              </View>
              <View style={{ flexDirection: 'row', justifyContent: 'space-between' }}>
                <Text
                  style={{
                    fontFamily: fonts.script,
                    fontStyle: 'italic',
                    fontSize: 12,
                    color: colors.scriptBlueDark,
                  }}
                >
                  {ax.left}
                </Text>
                <Text
                  style={{
                    fontFamily: fonts.script,
                    fontStyle: 'italic',
                    fontSize: 12,
                    color: colors.scriptBlueDark,
                  }}
                >
                  {ax.right}
                </Text>
              </View>
            </View>
          );
        })}
      </View>
    </Parchment>

    {onEdit && (
      <View style={{ gap: 8 }}>
        <Btn variant="primary" block onPress={onEdit}>
          EDIT STRATEGY
        </Btn>
        <Btn
          variant="secondary"
          block
          onPress={onWhisper}
          disabled={inft.state !== 'in-game'}
        >
          {inft.state === 'in-game'
            ? 'SEND WHISPER'
            : 'WHISPER · AVAILABLE WHEN IN A SEASON'}
        </Btn>
      </View>
    )}
  </View>
);

const MemoryTab = ({ inft }: { inft: Inft }) => {
  const kv = inft.kvState ?? {};
  const mem = inft.memory ?? [];
  const kvEntries = Object.entries(kv);
  return (
    <View style={{ gap: 14 }}>
      <Stone>
        <SectionHeader>KV STATE</SectionHeader>
        <View style={{ gap: 6, marginTop: 10 }}>
          {kvEntries.map(([k, v], i) => (
            <View
              key={k}
              style={{
                flexDirection: 'row',
                justifyContent: 'space-between',
                paddingVertical: 4,
                borderBottomWidth: i < kvEntries.length - 1 ? 1 : 0,
                borderBottomColor: 'rgba(212,175,92,0.1)',
              }}
            >
              <Text style={{ fontFamily: fonts.body, fontSize: 13, color: colors.inkDark }}>
                {k}
              </Text>
              <Text
                style={{
                  fontFamily: fonts.mono,
                  fontSize: 11,
                  color:
                    typeof v === 'string' && v.startsWith('[')
                      ? colors.scriptBlue
                      : colors.inkMono,
                }}
              >
                {String(v)}
              </Text>
            </View>
          ))}
        </View>
      </Stone>

      <Stone>
        <SectionHeader>MEMORY · CRUD</SectionHeader>
        <View style={{ gap: 8, marginTop: 10 }}>
          {mem.map((m, i) => (
            <View
              key={i}
              style={{
                flexDirection: 'row',
                gap: 10,
                alignItems: 'center',
                paddingVertical: 4,
              }}
            >
              <Text
                style={{
                  fontFamily: fonts.mono,
                  fontSize: 9,
                  color: colors.inkMono,
                  minWidth: 32,
                }}
              >
                {m.t}
              </Text>
              <View
                style={{
                  paddingHorizontal: 5,
                  paddingVertical: 2,
                  minWidth: 42,
                  alignItems: 'center',
                  backgroundColor:
                    m.op === 'WRITE'
                      ? 'rgba(201,155,79,0.12)'
                      : 'rgba(90,123,168,0.12)',
                }}
              >
                <Text
                  style={{
                    fontFamily: fonts.mono,
                    fontSize: 9,
                    letterSpacing: 0.6,
                    color: m.op === 'WRITE' ? colors.statusWarn : colors.statusInfo,
                  }}
                >
                  {m.op}
                </Text>
              </View>
              <Text
                style={{
                  flex: 1,
                  fontFamily: fonts.body,
                  fontSize: 12,
                  color: colors.inkDark,
                }}
              >
                {m.text}
              </Text>
            </View>
          ))}
        </View>
      </Stone>
    </View>
  );
};

const HistoryTab = ({ inft }: { inft: Inft }) => (
  <View style={{ gap: 8 }}>
    {(inft.history ?? []).map((h, i) => (
      <Parchment key={i} deep style={{ padding: 12 }}>
        <View
          style={{
            flexDirection: 'row',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
            {h.rank === 1 && (
              <Text style={{ fontSize: 14, color: colors.goldBright }}>♛</Text>
            )}
            <Text
              style={{
                fontFamily: fonts.display,
                fontSize: 12,
                letterSpacing: 1.4,
                color: colors.inkParchment,
              }}
            >
              SEASON {h.season}
            </Text>
            <Text
              style={{
                fontFamily: fonts.mono,
                fontSize: 10,
                color: colors.inkParchmentMuted,
              }}
            >
              FINISH #{h.rank}
            </Text>
          </View>
          <Text
            style={{
              fontFamily: fonts.mono,
              fontSize: 11,
              color: h.gold > 0 ? colors.statusLiveDeep : colors.inkParchmentMuted,
            }}
          >
            {h.gold > 0 ? `+${h.gold} GOLD` : '— gold'}
          </Text>
        </View>
        <Text
          style={{
            fontFamily: fonts.bodyItalic,
            fontStyle: 'italic',
            fontSize: 12,
            color: colors.inkParchmentMuted,
            marginTop: 6,
          }}
        >
          {h.event}
        </Text>
        <Text
          style={{
            fontFamily: fonts.mono,
            fontSize: 9,
            color: colors.inkParchmentFaint,
            marginTop: 4,
          }}
        >
          {h.when}
        </Text>
      </Parchment>
    ))}
    {(!inft.history || inft.history.length === 0) && (
      <Parchment deep style={{ padding: 18, alignItems: 'center' }}>
        <Text
          style={{
            fontFamily: fonts.script,
            fontStyle: 'italic',
            fontSize: 14,
            color: colors.scriptBlueDark,
          }}
        >
          No prior seasons recorded.
        </Text>
      </Parchment>
    )}
    <View style={{ marginTop: 4, alignItems: 'center', flexDirection: 'row', justifyContent: 'center', gap: 14 }}>
      <Diamond size={4} color={colors.goldDeep} />
      <View style={{ flex: 1, height: 1, backgroundColor: colors.goldDeep, opacity: 0.4 }} />
      <Diamond size={4} color={colors.goldDeep} />
    </View>
    <ParchmentRule opacity={0.001} />
  </View>
);
