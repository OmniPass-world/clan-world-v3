import React, { useState } from 'react';
import { ScrollView, Text, View } from 'react-native';
import {
  Inft,
  STRATEGY_AXES,
  STRATEGY_PRESETS,
  Strategy,
  StrategyAxisKey,
} from '../data';
import { Btn, Chip } from '../components/Buttons';
import { Parchment } from '../components/Surfaces';
import { Slider } from '../components/Slider';
import { TopBar } from '../components/TopBar';
import { colors, fonts } from '../theme';

type Props = {
  inft: Inft;
  onBack: () => void;
  onSave: (strat: Strategy) => void;
};

const PRESETS = ['DIPLOMAT', 'WARLORD', 'HERMIT', 'TRICKSTER', 'BUILDER', 'CUSTOM'];

const ZERO_STRAT: Strategy = {
  trust: 0,
  aggression: 0,
  honesty: 0,
  solo: 0,
  builder: 0,
  vengeful: 0,
  cautious: 0,
};

export const StrategyEditorScreen = ({ inft, onBack, onSave }: Props) => {
  const [strat, setStrat] = useState<Strategy>({
    ...ZERO_STRAT,
    ...(inft.strategy ?? {}),
  });
  const [preset, setPreset] = useState<string>('CUSTOM');

  const applyPreset = (p: string) => {
    setPreset(p);
    if (p === 'CUSTOM') return;
    const k = p.toLowerCase();
    const next = STRATEGY_PRESETS[k];
    if (next) setStrat({ ...next });
  };

  const update = (key: StrategyAxisKey, v: number) => {
    setStrat((s) => ({ ...s, [key]: v }));
    setPreset('CUSTOM');
  };

  return (
    <View style={{ flex: 1 }}>
      <TopBar title="EDIT STRATEGY" onBack={onBack} sub={inft.name.toUpperCase()} />

      <ScrollView
        contentContainerStyle={{ padding: 14, gap: 14, paddingBottom: 16 }}
      >
        <View>
          <Text
            style={{
              fontFamily: fonts.display,
              fontSize: 11,
              letterSpacing: 1.6,
              color: colors.inkDarkMuted,
              marginBottom: 8,
            }}
          >
            PRESETS
          </Text>
          <ScrollView
            horizontal
            showsHorizontalScrollIndicator={false}
            contentContainerStyle={{ gap: 6, paddingBottom: 4 }}
          >
            {PRESETS.map((p) => (
              <Chip key={p} active={preset === p} onPress={() => applyPreset(p)}>
                {p}
              </Chip>
            ))}
          </ScrollView>
        </View>

        <Parchment deep style={{ padding: 16 }}>
          <View style={{ gap: 16 }}>
            {STRATEGY_AXES.map((ax) => {
              const v = strat[ax.key as StrategyAxisKey] ?? 0;
              return (
                <View key={ax.key} style={{ gap: 6 }}>
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
                        fontSize: 12,
                        color: colors.inkParchment,
                      }}
                    >
                      {v > 0 ? `+${v}` : v}
                    </Text>
                  </View>
                  <Slider value={v} onChange={(nv) => update(ax.key as StrategyAxisKey, nv)} />
                  <View
                    style={{ flexDirection: 'row', justifyContent: 'space-between' }}
                  >
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
                  <Text
                    style={{
                      fontFamily: fonts.bodyItalic,
                      fontStyle: 'italic',
                      fontSize: 11,
                      color: colors.inkParchmentMuted,
                      minHeight: 14,
                    }}
                  >
                    {ax.hint[String(v)] ?? ax.hint['0']}
                  </Text>
                </View>
              );
            })}
          </View>
        </Parchment>
      </ScrollView>

      <View
        style={{
          padding: 12,
          backgroundColor: colors.bgCanvas,
          borderTopWidth: 1,
          borderTopColor: 'rgba(212,175,92,0.3)',
        }}
      >
        <Text
          style={{
            fontFamily: fonts.mono,
            fontSize: 9,
            color: colors.inkDarkMuted,
            textAlign: 'center',
            marginBottom: 8,
            letterSpacing: 1,
          }}
        >
          STRATEGY UPDATE · 1 GOLD · EFFECTIVE NEXT TICK
        </Text>
        <View style={{ flexDirection: 'row', gap: 8 }}>
          <Btn variant="secondary" style={{ flex: 1 }} onPress={onBack}>
            CANCEL
          </Btn>
          <Btn variant="primary" style={{ flex: 1 }} onPress={() => onSave(strat)}>
            SAVE · 1 GOLD
          </Btn>
        </View>
      </View>
    </View>
  );
};
