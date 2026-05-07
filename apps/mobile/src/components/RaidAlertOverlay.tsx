import React, { useEffect, useRef } from 'react';
import { Animated, Easing, Pressable, Text, View } from 'react-native';
import * as Haptics from 'expo-haptics';
import { Btn } from './Buttons';
import { Diamond, GoldRule } from './Diamond';
import { Parchment } from './Surfaces';
import { colors, fonts } from '../theme';

type Props = {
  victim: string;
  tick: number;
  onDefend: () => void;
  onDismiss: () => void;
};

/**
 * Sleek themed full-screen raid alert. Renders over the entire app when
 * the raid timer fires while foregrounded. Heavy haptic on appear,
 * pulsing ⚔ glyph, two CTAs.
 */
export const RaidAlertOverlay = ({ victim, tick, onDefend, onDismiss }: Props) => {
  const fade = useRef(new Animated.Value(0)).current;
  const scale = useRef(new Animated.Value(0.85)).current;
  const swordPulse = useRef(new Animated.Value(0.7)).current;

  useEffect(() => {
    Animated.parallel([
      Animated.timing(fade, {
        toValue: 1,
        duration: 240,
        easing: Easing.out(Easing.cubic),
        useNativeDriver: true,
      }),
      Animated.spring(scale, {
        toValue: 1,
        tension: 100,
        friction: 8,
        useNativeDriver: true,
      }),
    ]).start();

    Animated.loop(
      Animated.sequence([
        Animated.timing(swordPulse, {
          toValue: 1,
          duration: 700,
          easing: Easing.inOut(Easing.ease),
          useNativeDriver: true,
        }),
        Animated.timing(swordPulse, {
          toValue: 0.7,
          duration: 700,
          easing: Easing.inOut(Easing.ease),
          useNativeDriver: true,
        }),
      ]),
    ).start();

    try {
      Haptics.notificationAsync(Haptics.NotificationFeedbackType.Warning);
    } catch {
      /* haptics unavailable */
    }
  }, [fade, scale, swordPulse]);

  return (
    <Animated.View
      style={{
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(7, 4, 5, 0.88)',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 24,
        zIndex: 1000,
        opacity: fade,
      }}
    >
      <Pressable
        onPress={onDismiss}
        style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0 }}
      />
      <Animated.View
        style={{
          width: '100%',
          maxWidth: 380,
          transform: [{ scale }],
        }}
      >
        <Parchment
          deep
          style={{
            padding: 0,
            overflow: 'hidden',
            shadowColor: colors.statusDanger,
            shadowOpacity: 0.6,
            shadowRadius: 32,
            shadowOffset: { width: 0, height: 0 },
            elevation: 20,
          }}
          borderColor={colors.statusDanger}
          borderWidth={2}
        >
          {/* Top band — danger ribbon */}
          <View
            style={{
              backgroundColor: 'rgba(160, 74, 63, 0.18)',
              paddingHorizontal: 16,
              paddingVertical: 8,
              flexDirection: 'row',
              alignItems: 'center',
              justifyContent: 'space-between',
              borderBottomWidth: 1,
              borderBottomColor: 'rgba(160, 74, 63, 0.4)',
            }}
          >
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6 }}>
              <Diamond size={5} color={colors.statusDanger} />
              <Text
                style={{
                  fontFamily: fonts.display,
                  fontSize: 10,
                  letterSpacing: 1.6,
                  color: colors.statusDanger,
                }}
              >
                ALERT · BANDIT RAID
              </Text>
            </View>
            <Text
              style={{
                fontFamily: fonts.mono,
                fontSize: 10,
                color: colors.statusDanger,
              }}
            >
              T{tick}
            </Text>
          </View>

          <View style={{ padding: 24, alignItems: 'center', gap: 16 }}>
            <Animated.Text
              style={{
                fontSize: 64,
                color: colors.statusDanger,
                opacity: swordPulse,
                textShadowColor: colors.statusDanger,
                textShadowOffset: { width: 0, height: 0 },
                textShadowRadius: 18,
              }}
            >
              ⚔
            </Animated.Text>

            <Text
              style={{
                fontFamily: fonts.display,
                fontSize: 22,
                letterSpacing: 2,
                color: colors.inkParchment,
                textAlign: 'center',
              }}
            >
              {victim.toUpperCase()}
            </Text>

            <Text
              style={{
                fontFamily: fonts.script,
                fontStyle: 'italic',
                fontSize: 16,
                color: colors.scriptBlueDark,
                textAlign: 'center',
                lineHeight: 22,
              }}
            >
              The bandits have come for your wall.
            </Text>

            <View style={{ width: '100%' }}>
              <GoldRule opacity={0.4} />
            </View>

            <Text
              style={{
                fontFamily: fonts.bodyItalic,
                fontStyle: 'italic',
                fontSize: 13,
                color: colors.inkParchmentMuted,
                textAlign: 'center',
                lineHeight: 18,
              }}
            >
              Resources are bleeding. The cockpit can still feel your hand —
              if you act before the next tick.
            </Text>
          </View>

          <View
            style={{
              flexDirection: 'row',
              gap: 8,
              padding: 16,
              borderTopWidth: 1,
              borderTopColor: 'rgba(26,20,16,0.18)',
              backgroundColor: 'rgba(26,20,16,0.06)',
            }}
          >
            <Btn variant="secondary" style={{ flex: 1 }} onPress={onDismiss}>
              DISMISS
            </Btn>
            <Btn
              variant="primary"
              style={{ flex: 1.4, borderColor: colors.statusDanger }}
              onPress={onDefend}
            >
              DEFEND →
            </Btn>
          </View>
        </Parchment>
      </Animated.View>
    </Animated.View>
  );
};
