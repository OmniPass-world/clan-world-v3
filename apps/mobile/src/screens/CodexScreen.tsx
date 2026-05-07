import React, { useState } from 'react';
import { Pressable, ScrollView, Text, View } from 'react-native';
import { Btn } from '../components/Buttons';
import { GoldRuleSoft } from '../components/Diamond';
import { TopBar } from '../components/TopBar';
import { colors, fonts } from '../theme';

type Props = {
  onBack: () => void;
  pubkey: string | null;
  onDisconnect: () => void;
  onResetForgeState: () => void;
  onTriggerRaid: () => void;
};

const truncPub = (pk: string | null) => {
  if (!pk) return '—';
  if (pk.length <= 10) return pk;
  return `${pk.slice(0, 4)}…${pk.slice(-4)}`;
};

export const CodexScreen = ({
  onBack,
  pubkey,
  onDisconnect,
  onResetForgeState,
  onTriggerRaid,
}: Props) => {
  const [haptics, setHaptics] = useState(true);
  const [game, setGame] = useState(true);
  const [market, setMarket] = useState(true);
  const [system, setSystem] = useState(false);

  return (
    <>
      <TopBar title="CODEX" onBack={onBack} />
      <ScrollView
        contentContainerStyle={{ paddingHorizontal: 14, paddingBottom: 20, paddingTop: 12, gap: 18 }}
      >
        <SettingGroup label="WALLET">
          <SettingRow label="Connected address" value={truncPub(pubkey)} mono />
          <SettingRow
            label=""
            right={
              <Btn
                variant="secondary"
                paddingHorizontal={10}
                paddingVertical={6}
                fontSize={10}
                onPress={onDisconnect}
              >
                DISCONNECT
              </Btn>
            }
          />
        </SettingGroup>

        <SettingGroup label="NOTIFICATIONS">
          <SettingRow
            label="Game (raids, prizes)"
            right={<Toggle on={game} onPress={() => setGame(!game)} />}
          />
          <SettingRow
            label="Market (hire offers)"
            right={<Toggle on={market} onPress={() => setMarket(!market)} />}
          />
          <SettingRow
            label="System"
            right={<Toggle on={system} onPress={() => setSystem(!system)} />}
          />
          <SettingRow label="Quiet hours" value="22:00 → 08:00" mono />
        </SettingGroup>

        <SettingGroup label="HAPTICS">
          <SettingRow
            label="Master haptics"
            right={<Toggle on={haptics} onPress={() => setHaptics(!haptics)} />}
          />
          <SettingRow label="Intensity" value="MEDIUM" mono />
        </SettingGroup>

        <SettingGroup label="WIDGET">
          <SettingRow label="Mirror game" value="STORM RIDERS" mono />
          <SettingRow label="Refresh" value="60s" mono />
        </SettingGroup>

        <SettingGroup label="DEMO TOOLS">
          <SettingRow
            label="Trigger raid (10s)"
            right={
              <Btn
                variant="secondary"
                paddingHorizontal={10}
                paddingVertical={6}
                fontSize={10}
                onPress={onTriggerRaid}
              >
                FIRE
              </Btn>
            }
          />
          <Text
            style={{
              fontFamily: fonts.bodyItalic,
              fontStyle: 'italic',
              fontSize: 11,
              color: colors.inkDarkMuted,
              paddingVertical: 6,
              lineHeight: 16,
            }}
          >
            Schedules a bandit raid alert ten seconds from now. If the app
            is open you&apos;ll see a themed overlay; if it&apos;s closed
            you&apos;ll get an Android notification — tap to return.
          </Text>
          <SettingRow
            label="Reset forge state"
            right={
              <Btn
                variant="secondary"
                paddingHorizontal={10}
                paddingVertical={6}
                fontSize={10}
                onPress={onResetForgeState}
              >
                RESET
              </Btn>
            }
          />
          <Text
            style={{
              fontFamily: fonts.bodyItalic,
              fontStyle: 'italic',
              fontSize: 11,
              color: colors.inkDarkMuted,
              paddingVertical: 6,
              lineHeight: 16,
            }}
          >
            Clears forged Elders and the free-forge claim so the demo can
            run again. Wallet stays connected.
          </Text>
        </SettingGroup>

        <SettingGroup label="ABOUT">
          <SettingRow label="Version" value="0.4.1 · ALPHA" mono />
          <SettingRow label="Network" value="BASE SEPOLIA" mono />
          <SettingRow
            label="View source"
            right={<Text style={{ color: colors.goldPrimary, fontSize: 13 }}>↗</Text>}
          />
        </SettingGroup>
      </ScrollView>
    </>
  );
};

const SettingGroup = ({ label, children }: { label: string; children: React.ReactNode }) => (
  <View style={{ gap: 6 }}>
    <Text
      style={{
        fontFamily: fonts.display,
        fontSize: 11,
        letterSpacing: 1.6,
        color: colors.inkDarkMuted,
      }}
    >
      {label}
    </Text>
    <GoldRuleSoft />
    <View>{children}</View>
  </View>
);

const SettingRow = ({
  label,
  value,
  mono,
  right,
}: {
  label: string;
  value?: string;
  mono?: boolean;
  right?: React.ReactNode;
}) => (
  <View
    style={{
      flexDirection: 'row',
      justifyContent: 'space-between',
      alignItems: 'center',
      paddingVertical: 10,
      borderBottomWidth: 1,
      borderBottomColor: 'rgba(212,175,92,0.07)',
    }}
  >
    <Text style={{ fontFamily: fonts.body, fontSize: 13, color: colors.inkDark, flex: 1 }}>
      {label}
    </Text>
    {value && (
      <Text
        style={{
          fontFamily: mono ? fonts.mono : fonts.body,
          fontSize: mono ? 11 : 13,
          color: mono ? colors.inkMono : colors.inkDarkMuted,
        }}
      >
        {value}
      </Text>
    )}
    {right}
  </View>
);

const Toggle = ({ on, onPress }: { on: boolean; onPress: () => void }) => (
  <Pressable
    onPress={onPress}
    style={{
      width: 36,
      height: 20,
      borderRadius: 10,
      backgroundColor: on ? colors.goldPrimary : colors.bgElevated2,
      justifyContent: 'center',
    }}
  >
    <View
      style={{
        position: 'absolute',
        top: 2,
        left: on ? 18 : 2,
        width: 16,
        height: 16,
        borderRadius: 8,
        backgroundColor: on ? '#1A1410' : colors.inkDarkMuted,
      }}
    />
  </Pressable>
);
