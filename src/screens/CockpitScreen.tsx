import React from 'react';
import { Pressable, ScrollView, Text, View } from 'react-native';
import { Inft } from '../data';
import { GoldRuleSoft } from '../components/Diamond';
import { colors, fonts } from '../theme';

type Props = {
  inft: Inft;
  onClose: () => void;
  onWhisper: () => void;
};

const Section = ({ title, children }: { title: string; children: React.ReactNode }) => (
  <View>
    <Text
      style={{
        color: colors.goldBright,
        fontFamily: fonts.mono,
        letterSpacing: 1.6,
        marginBottom: 6,
        fontSize: 11,
      }}
    >
      ◆ {title}
    </Text>
    <GoldRuleSoft />
    <View style={{ marginTop: 8, gap: 4 }}>{children}</View>
  </View>
);

const Line = ({ children }: { children: React.ReactNode }) => (
  <Text style={{ fontFamily: fonts.mono, fontSize: 11, color: colors.inkMono, lineHeight: 18 }}>
    {children}
  </Text>
);

export const CockpitScreen = ({ inft, onClose, onWhisper }: Props) => (
  <View style={{ flex: 1, backgroundColor: colors.bgTerminal }}>
    {/* Cockpit chrome */}
    <View
      style={{
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingHorizontal: 12,
        paddingVertical: 8,
        backgroundColor: 'rgba(15,14,18,0.92)',
        borderBottomWidth: 1,
        borderBottomColor: 'rgba(212,175,92,0.3)',
      }}
    >
      <Pressable onPress={onClose} hitSlop={8}>
        <Text
          style={{
            fontFamily: fonts.display,
            fontSize: 13,
            color: colors.goldPrimary,
            letterSpacing: 1.4,
          }}
        >
          ← HALL
        </Text>
      </Pressable>
      <Text style={{ fontFamily: fonts.mono, fontSize: 10, color: colors.inkMono }}>
        TICK {inft.gameTick} · S{inft.season} · {Math.round((inft.seasonPct ?? 0) * 100)}%
      </Text>
      <Pressable onPress={onWhisper} hitSlop={8}>
        <Text style={{ fontSize: 16, color: colors.goldBright }}>♪</Text>
      </Pressable>
    </View>

    <ScrollView contentContainerStyle={{ padding: 12, gap: 14 }}>
      <Section title={`TERM — ${inft.name.toUpperCase()}`}>
        <Line>
          <Text style={{ color: colors.inkDarkMuted }}>T259</Text> {'>'} observe.bandit_ne
        </Line>
        <Line>
          <Text style={{ color: colors.inkDarkMuted }}>T259</Text> {'>'}{' '}
          <Text style={{ color: colors.statusDanger }}>raid.detected</Text> resource=wood loss=2
        </Line>
        <Line>
          <Text style={{ color: colors.inkDarkMuted }}>T259</Text> {'>'} recall.strategy.cautious=+2 → no_pursue
        </Line>
        <Line>
          <Text style={{ color: colors.inkDarkMuted }}>T259</Text> {'>'} whisper.from(owner)="watch the bandit camp"
        </Line>
        <Line>
          <Text style={{ color: colors.inkDarkMuted }}>T259</Text> {'>'} mood.set=cautious; pact.verdant=hold
        </Line>
        <Line>
          <Text style={{ color: colors.inkDarkMuted }}>T260</Text> {'>'}{' '}
          <Text style={{ color: colors.statusLive }}>commit.tick</Text>
        </Line>
      </Section>

      <Section title="VAULT — CLAN 2">
        {[
          { k: 'GOLD', v: '4', live: true },
          { k: 'WOOD', v: '1', live: false, danger: true },
          { k: 'IRON', v: '2', live: true },
          { k: 'WHEAT', v: '5', live: true },
          { k: 'FISH', v: '0', idle: true },
          { k: 'BLUEPRINT', v: '1', live: true },
        ].map((row) => (
          <View
            key={row.k}
            style={{ flexDirection: 'row', justifyContent: 'space-between' }}
          >
            <Line>{row.k}</Line>
            <Text style={{ fontFamily: fonts.mono, fontSize: 11, color: colors.inkMono }}>
              <Text
                style={{
                  color: row.danger
                    ? colors.statusDanger
                    : row.idle
                    ? colors.inkDarkMuted
                    : colors.statusLive,
                }}
              >
                {row.v}
              </Text>{' '}
              {row.idle ? 'idle' : 'live'}
            </Text>
          </View>
        ))}
      </Section>

      <Section title="0G — ATTESTATION">
        <Text style={{ fontFamily: fonts.mono, fontSize: 11, color: colors.inkDarkMuted, lineHeight: 18 }}>
          tee.attest=ok ratchet=259
        </Text>
        <Text style={{ fontFamily: fonts.mono, fontSize: 11, color: colors.inkDarkMuted, lineHeight: 18 }}>
          kv.commit=0xa14b…cdf2
        </Text>
        <Text style={{ fontFamily: fonts.mono, fontSize: 11, color: colors.inkDarkMuted, lineHeight: 18 }}>
          memory.crud_ops=4 bytes=812
        </Text>
      </Section>

      <Text
        style={{
          fontFamily: fonts.bodyItalic,
          fontStyle: 'italic',
          color: colors.inkDarkMuted,
          textAlign: 'center',
          paddingVertical: 12,
        }}
      >
        ── live cockpit · WebView ──
      </Text>
    </ScrollView>
  </View>
);
