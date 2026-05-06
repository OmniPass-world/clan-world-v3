import React, { useEffect, useRef } from 'react';
import { Animated, Easing, Text, View } from 'react-native';
import { Diamond, DiamondRow } from '../components/Diamond';
import { colors, fonts } from '../theme';

export const BridgeScreen = () => {
  const opacity = useRef(new Animated.Value(0.4)).current;
  useEffect(() => {
    const loop = Animated.loop(
      Animated.sequence([
        Animated.timing(opacity, {
          toValue: 1,
          duration: 800,
          easing: Easing.inOut(Easing.ease),
          useNativeDriver: true,
        }),
        Animated.timing(opacity, {
          toValue: 0.4,
          duration: 800,
          easing: Easing.inOut(Easing.ease),
          useNativeDriver: true,
        }),
      ])
    );
    loop.start();
    return () => loop.stop();
  }, [opacity]);

  return (
    <View
      style={{
        flex: 1,
        backgroundColor: colors.bgTerminal,
        alignItems: 'center',
        justifyContent: 'center',
        gap: 18,
        paddingHorizontal: 30,
      }}
    >
      <DiamondRow />
      <Animated.View style={{ opacity }}>
        <Diamond size={18} color={colors.goldBright} />
      </Animated.View>
      <Text
        style={{
          fontFamily: fonts.display,
          fontSize: 13,
          letterSpacing: 2.4,
          color: colors.goldBright,
        }}
      >
        ENTERING STORM RIDERS
      </Text>
      <View style={{ alignItems: 'center', gap: 4 }}>
        <Text style={{ fontFamily: fonts.mono, fontSize: 10, color: colors.inkDarkMuted }}>
          resolving INFT context
        </Text>
        <Text style={{ fontFamily: fonts.mono, fontSize: 10, color: colors.inkDarkMuted }}>
          authorizing convex session
        </Text>
        <Text style={{ fontFamily: fonts.mono, fontSize: 10, color: colors.inkDarkMuted }}>
          injecting clanworld bridge
        </Text>
      </View>
      <DiamondRow />
    </View>
  );
};
