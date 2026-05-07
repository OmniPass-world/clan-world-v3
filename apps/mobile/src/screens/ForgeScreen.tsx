import React, { useState } from 'react';
import { Pressable, ScrollView, Text, TextInput, View } from 'react-native';
import { Connection } from '@solana/web3.js';
import * as Haptics from 'expo-haptics';
import {
  ARCHETYPE_GLYPHS,
  ArchetypeKey,
  STRATEGY_AXES,
  Strategy,
  StrategyAxisKey,
} from '../data';
import { ArchetypeGlyph } from '../components/ArchetypeGlyph';
import { Btn } from '../components/Buttons';
import { Diamond, GoldRuleSoft, ParchmentRule } from '../components/Diamond';
import { Parchment, Stone } from '../components/Surfaces';
import { Slider } from '../components/Slider';
import { TopBar } from '../components/TopBar';
import { colors, fonts } from '../theme';
import { signAndSendMemoTx } from '../wallet/mwa';
import { addForgedInft, getFreeForgeUsed, setFreeForgeUsed } from '../storage';

const ZERO_STRAT: Strategy = {
  trust: 0,
  aggression: 0,
  honesty: 0,
  solo: 0,
  builder: 0,
  vengeful: 0,
  cautious: 0,
};

const ARCHETYPES: ArchetypeKey[] = [
  'patient-builder',
  'warlord',
  'diplomat',
  'hermit',
  'trickster',
  'verdant-warden',
];

type Props = {
  pubkey: string;
  /** True when the user is a Seeker bearer AND hasn't burned their free forge yet. */
  isFreeForgeEligible: boolean;
  connection: Connection;
  onClose: () => void;
  onComplete: (data: { name: string; archetype: ArchetypeKey; strategy: Strategy; sig: string }) => void;
};

export const ForgeScreen = ({ pubkey, isFreeForgeEligible, connection, onClose, onComplete }: Props) => {
  const [step, setStep] = useState(1);
  const [name, setName] = useState('');
  const [arch, setArch] = useState<ArchetypeKey | null>(null);
  const [strat, setStrat] = useState<Strategy>(ZERO_STRAT);
  const [forging, setForging] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Eligibility resolves at the moment of forge: must be SGT bearer AND
  // freeForgeUsed flag must not yet be set for this pubkey.
  const willBeFree = isFreeForgeEligible && !getFreeForgeUsed(pubkey);

  const startForge = async () => {
    if (!arch) return;
    setForging(true);
    setError(null);
    try {
      // Memo payload — recorded on Solana mainnet so the forge has a real on-chain artifact.
      const memo = `cw:forge:v1:name=${name}:archetype=${arch}:strategy=${JSON.stringify(strat)}:t=${Math.floor(Date.now() / 1000)}`;
      const sig = await signAndSendMemoTx(connection, memo);

      // Record locally — Hall picks this up reactively via MMKV-backed read.
      addForgedInft({
        id: `forged-${sig.slice(0, 12)}`,
        pubkey,
        name,
        archetype: arch,
        strategy: strat,
        mintTxSig: sig,
        isFreeForge: willBeFree,
        createdAt: Date.now(),
      });
      if (willBeFree) setFreeForgeUsed(pubkey, true);

      try {
        await Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
      } catch {
        // haptics unavailable on emulator — non-fatal
      }

      // Trigger the seal animation, then complete.
      setForging(false);
      setDone(true);
      setTimeout(() => onComplete({ name, archetype: arch, strategy: strat, sig }), 1200);
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : 'Forge failed';
      setError(msg);
      setForging(false);
    }
  };

  return (
    <View style={{ flex: 1, backgroundColor: colors.bgCanvas }}>
      <TopBar title="SIGIL FORGE" onBack={onClose} />

      {/* Step indicator */}
      <View
        style={{
          flexDirection: 'row',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 10,
          paddingVertical: 12,
        }}
      >
        {[1, 2, 3, 4].map((i, idx) => (
          <React.Fragment key={i}>
            <Diamond
              size={i === step ? 9 : 6}
              color={i <= step ? colors.goldBright : colors.goldDeeper}
            />
            {idx < 3 && (
              <View
                style={{
                  width: 24,
                  height: 1,
                  backgroundColor: i < step ? colors.goldPrimary : colors.goldDeeper,
                }}
              />
            )}
          </React.Fragment>
        ))}
      </View>

      <ScrollView style={{ flex: 1 }} contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 16 }}>
        {step === 1 && (
          <View style={{ gap: 14 }}>
            <Text
              style={{
                fontFamily: fonts.display,
                fontSize: 16,
                color: colors.goldBright,
                textAlign: 'center',
                letterSpacing: 1.6,
                marginTop: 4,
              }}
            >
              NAME YOUR ELDER
            </Text>
            <TextInput
              value={name}
              onChangeText={(v) => setName(v.slice(0, 16))}
              placeholder="A name in the realm…"
              placeholderTextColor={colors.inkDarkFaint}
              maxLength={16}
              style={{
                backgroundColor: colors.bgElevated,
                borderColor: colors.goldDeep,
                borderWidth: 1,
                color: colors.inkDark,
                paddingHorizontal: 16,
                paddingVertical: 14,
                fontFamily: fonts.display,
                fontSize: 18,
                letterSpacing: 1,
                textAlign: 'center',
              }}
            />
            <Text
              style={{
                fontFamily: fonts.mono,
                fontSize: 9,
                color: colors.inkDarkFaint,
                textAlign: 'right',
              }}
            >
              {name.length}/16
            </Text>

            <View style={{ marginVertical: 8 }}>
              <GoldRuleSoft />
            </View>

            <Text
              style={{
                fontFamily: fonts.display,
                fontSize: 16,
                color: colors.goldBright,
                textAlign: 'center',
                letterSpacing: 1.6,
              }}
            >
              CHOOSE AN ARCHETYPE
            </Text>

            <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
              {ARCHETYPES.map((a) => {
                const active = arch === a;
                return (
                  <Pressable
                    key={a}
                    onPress={() => setArch(a)}
                    style={{ width: '48%', flexGrow: 1 }}
                  >
                    <Parchment
                      deep
                      style={{ padding: 12, alignItems: 'center' }}
                      borderColor={active ? colors.goldBright : colors.goldDeep}
                      borderWidth={active ? 2 : 1}
                    >
                      <View style={{ marginBottom: 6 }}>
                        <ArchetypeGlyph kind={a} size={42} />
                      </View>
                      <Text
                        style={{
                          fontFamily: fonts.display,
                          fontSize: 11,
                          letterSpacing: 1.4,
                          color: colors.inkParchment,
                          textTransform: 'uppercase',
                        }}
                      >
                        {ARCHETYPE_GLYPHS[a].name}
                      </Text>
                      <Text
                        style={{
                          fontFamily: fonts.bodyItalic,
                          fontStyle: 'italic',
                          fontSize: 10,
                          color: colors.inkParchmentMuted,
                          marginTop: 4,
                          lineHeight: 13,
                          textAlign: 'center',
                        }}
                      >
                        {ARCHETYPE_GLYPHS[a].short}
                      </Text>
                    </Parchment>
                  </Pressable>
                );
              })}
            </View>
          </View>
        )}

        {step === 2 && (
          <View style={{ gap: 12 }}>
            <Text
              style={{
                fontFamily: fonts.display,
                fontSize: 16,
                color: colors.goldBright,
                textAlign: 'center',
                letterSpacing: 1.6,
                marginTop: 4,
              }}
            >
              SHAPE THEIR WILL
            </Text>
            <Text
              style={{
                fontFamily: fonts.bodyItalic,
                fontStyle: 'italic',
                fontSize: 12,
                color: colors.inkDarkMuted,
                textAlign: 'center',
              }}
            >
              These can be tuned later. Choose how your Elder begins.
            </Text>
            <Parchment deep style={{ padding: 14, marginTop: 4 }}>
              <View style={{ gap: 14 }}>
                {STRATEGY_AXES.map((ax) => {
                  const v = strat[ax.key as StrategyAxisKey] ?? 0;
                  return (
                    <View key={ax.key} style={{ gap: 4 }}>
                      <View
                        style={{
                          flexDirection: 'row',
                          justifyContent: 'space-between',
                        }}
                      >
                        <Text
                          style={{
                            fontFamily: fonts.display,
                            fontSize: 9,
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
                      <Slider
                        value={v}
                        onChange={(nv) =>
                          setStrat((s) => ({ ...s, [ax.key as StrategyAxisKey]: nv }))
                        }
                      />
                      <View
                        style={{
                          flexDirection: 'row',
                          justifyContent: 'space-between',
                        }}
                      >
                        <Text
                          style={{
                            fontFamily: fonts.script,
                            fontStyle: 'italic',
                            fontSize: 11,
                            color: colors.scriptBlueDark,
                          }}
                        >
                          {ax.left}
                        </Text>
                        <Text
                          style={{
                            fontFamily: fonts.script,
                            fontStyle: 'italic',
                            fontSize: 11,
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
          </View>
        )}

        {step === 3 && (
          <View style={{ gap: 12 }}>
            <Text
              style={{
                fontFamily: fonts.display,
                fontSize: 16,
                color: colors.goldBright,
                textAlign: 'center',
                letterSpacing: 1.6,
                marginTop: 4,
              }}
            >
              BIND A MIND
            </Text>
            <Text
              style={{
                fontFamily: fonts.bodyItalic,
                fontStyle: 'italic',
                fontSize: 12,
                color: colors.inkDarkMuted,
                textAlign: 'center',
              }}
            >
              Choose the harness that will animate your Elder.
            </Text>

            <Parchment
              deep
              style={{ padding: 16, marginTop: 8 }}
              borderColor={colors.goldBright}
              borderWidth={2}
            >
              <View
                style={{
                  flexDirection: 'row',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                }}
              >
                <View style={{ gap: 4 }}>
                  <Text
                    style={{
                      fontFamily: fonts.display,
                      fontSize: 16,
                      color: colors.inkParchment,
                      letterSpacing: 1.4,
                    }}
                  >
                    CLAUDE-CODE
                  </Text>
                  <Text
                    style={{
                      fontFamily: fonts.mono,
                      fontSize: 9,
                      color: colors.inkParchmentMuted,
                    }}
                  >
                    v1 · TEE-ATTESTED
                  </Text>
                </View>
                <Text style={{ color: colors.statusLive, fontSize: 14 }}>● SELECTED</Text>
              </View>
              <Text
                style={{
                  fontFamily: fonts.bodyItalic,
                  fontStyle: 'italic',
                  fontSize: 12,
                  color: colors.inkParchmentMuted,
                  marginTop: 8,
                }}
              >
                Reasoning harness with persistent memory and tool use. Default for v1.
              </Text>
            </Parchment>

            <View style={{ gap: 6 }}>
              {['CODEX', 'GROK', 'CUSTOM API'].map((h) => (
                <Stone key={h} style={{ padding: 14, opacity: 0.5 }}>
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
                        fontSize: 13,
                        letterSpacing: 1.4,
                        color: colors.inkDarkMuted,
                      }}
                    >
                      {h}
                    </Text>
                    <Text
                      style={{
                        fontFamily: fonts.mono,
                        fontSize: 9,
                        color: colors.inkDarkFaint,
                        letterSpacing: 1,
                      }}
                    >
                      🔒 SOON
                    </Text>
                  </View>
                </Stone>
              ))}
            </View>
          </View>
        )}

        {step === 4 && !forging && !done && (
          <View style={{ gap: 12 }}>
            <Text
              style={{
                fontFamily: fonts.display,
                fontSize: 16,
                color: colors.goldBright,
                textAlign: 'center',
                letterSpacing: 1.6,
                marginTop: 4,
              }}
            >
              FORGE THE SIGIL
            </Text>
            <Parchment deep style={{ padding: 18, alignItems: 'center', marginTop: 6 }}>
              <View style={{ marginBottom: 10 }}>
                <ArchetypeGlyph kind={arch ?? 'patient-builder'} size={64} />
              </View>
              <Text
                style={{
                  fontFamily: fonts.display,
                  fontSize: 26,
                  color: colors.inkParchment,
                  letterSpacing: 1.2,
                }}
              >
                {name || '—'}
              </Text>
              <Text
                style={{
                  fontFamily: fonts.script,
                  fontStyle: 'italic',
                  fontSize: 14,
                  color: colors.scriptBlueDark,
                }}
              >
                {arch ? ARCHETYPE_GLYPHS[arch].name : ''}
              </Text>

              <View style={{ marginVertical: 14, alignSelf: 'stretch' }}>
                <ParchmentRule />
              </View>

              <Text
                style={{
                  fontFamily: fonts.display,
                  fontSize: 9,
                  letterSpacing: 1.4,
                  color: colors.inkParchmentMuted,
                  textAlign: 'left',
                  marginBottom: 8,
                  alignSelf: 'flex-start',
                }}
              >
                STRATEGY FINGERPRINT
              </Text>
              <View style={{ gap: 4, alignSelf: 'stretch' }}>
                {STRATEGY_AXES.map((ax) => (
                  <View
                    key={ax.key}
                    style={{ flexDirection: 'row', justifyContent: 'space-between' }}
                  >
                    <Text
                      style={{
                        fontFamily: fonts.mono,
                        fontSize: 10,
                        color: colors.inkParchment,
                        opacity: 0.7,
                      }}
                    >
                      {ax.key}
                    </Text>
                    <Text
                      style={{
                        fontFamily: fonts.mono,
                        fontSize: 10,
                        color: colors.inkParchment,
                      }}
                    >
                      {(strat[ax.key as StrategyAxisKey] ?? 0) > 0
                        ? `+${strat[ax.key as StrategyAxisKey]}`
                        : strat[ax.key as StrategyAxisKey] ?? 0}
                    </Text>
                  </View>
                ))}
              </View>

              <View style={{ marginVertical: 14, alignSelf: 'stretch' }}>
                <ParchmentRule />
              </View>

              <View
                style={{
                  flexDirection: 'row',
                  justifyContent: 'space-between',
                  alignSelf: 'stretch',
                }}
              >
                <Text
                  style={{
                    fontFamily: fonts.display,
                    fontSize: 11,
                    letterSpacing: 1.4,
                    color: colors.inkParchmentMuted,
                  }}
                >
                  MINT FEE
                </Text>
                {willBeFree ? (
                  <View style={{ flexDirection: 'row', alignItems: 'baseline', gap: 8 }}>
                    <Text
                      style={{
                        fontFamily: fonts.monoBold,
                        fontSize: 14,
                        color: colors.inkParchmentMuted,
                        textDecorationLine: 'line-through',
                      }}
                    >
                      5 GOLD
                    </Text>
                    <Text
                      style={{
                        fontFamily: fonts.script,
                        fontStyle: 'italic',
                        fontSize: 14,
                        color: colors.goldDeep,
                      }}
                    >
                      WAIVED · SEEKER BEARER
                    </Text>
                  </View>
                ) : (
                  <Text
                    style={{
                      fontFamily: fonts.monoBold,
                      fontSize: 14,
                      color: colors.inkParchment,
                    }}
                  >
                    5 GOLD
                  </Text>
                )}
              </View>
              {willBeFree && (
                <Text
                  style={{
                    fontFamily: fonts.bodyItalic,
                    fontStyle: 'italic',
                    fontSize: 11,
                    color: colors.inkParchmentMuted,
                    textAlign: 'center',
                    marginTop: 8,
                  }}
                >
                  One free forge per Seeker bearer. Use it on your first Elder.
                </Text>
              )}
            </Parchment>
          </View>
        )}

        {(forging || done) && (
          <View style={{ alignItems: 'center', paddingTop: 40, paddingBottom: 20 }}>
            <View
              style={{
                width: 220,
                height: 220,
                alignItems: 'center',
                justifyContent: 'center',
                shadowColor: colors.goldBright,
                shadowOpacity: forging ? 0.7 : 0.3,
                shadowRadius: forging ? 30 : 12,
                shadowOffset: { width: 0, height: 0 },
                elevation: 12,
              }}
            >
              <Parchment
                deep
                style={{
                  width: 200,
                  height: 200,
                  alignItems: 'center',
                  justifyContent: 'center',
                  padding: 0,
                }}
              >
                <ArchetypeGlyph kind={arch ?? 'patient-builder'} size={110} />
                <Diamond
                  size={6}
                  color={colors.goldBright}
                  style={{ position: 'absolute', top: 10, left: '50%', marginLeft: -3 }}
                />
                <Diamond
                  size={6}
                  color={colors.goldBright}
                  style={{ position: 'absolute', bottom: 10, left: '50%', marginLeft: -3 }}
                />
                <Diamond
                  size={6}
                  color={colors.goldBright}
                  style={{ position: 'absolute', left: 10, top: '50%', marginTop: -3 }}
                />
                <Diamond
                  size={6}
                  color={colors.goldBright}
                  style={{ position: 'absolute', right: 10, top: '50%', marginTop: -3 }}
                />
              </Parchment>
            </View>
            <Text
              style={{
                fontFamily: fonts.display,
                fontSize: 14,
                letterSpacing: 2,
                color: colors.goldBright,
                marginTop: 24,
              }}
            >
              {done ? 'WELCOME TO YOUR HALL' : 'FORGING…'}
            </Text>
            {done && (
              <Text
                style={{
                  fontFamily: fonts.script,
                  fontStyle: 'italic',
                  fontSize: 16,
                  color: colors.scriptBlue,
                  marginTop: 8,
                }}
              >
                {name}
              </Text>
            )}
          </View>
        )}
      </ScrollView>

      {!forging && !done && (
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
          {step > 1 && (
            <Btn variant="secondary" style={{ flex: 1 }} onPress={() => setStep(step - 1)}>
              ← BACK
            </Btn>
          )}
          {step < 4 && (
            <Btn
              variant="primary"
              style={{ flex: 2 }}
              disabled={step === 1 && (!name.trim() || !arch)}
              onPress={() => setStep(step + 1)}
            >
              CONTINUE →
            </Btn>
          )}
          {step === 4 && (
            <View style={{ flex: 2, gap: 6 }}>
              <Btn variant="primary" onPress={startForge}>
                {willBeFree ? 'FORGE · FREE' : 'FORGE · 5 GOLD'}
              </Btn>
              {error && (
                <Text
                  style={{
                    fontFamily: fonts.mono,
                    fontSize: 10,
                    color: colors.statusDanger,
                    textAlign: 'center',
                  }}
                >
                  {error}
                </Text>
              )}
            </View>
          )}
        </View>
      )}
    </View>
  );
};
