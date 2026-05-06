import React from 'react';
import { Text, View } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Btn } from '../components/Buttons';
import { DiamondRow } from '../components/Diamond';
import { colors, fonts } from '../theme';

type Props = { onEnter: () => void };

export const SplashScreen = ({ onEnter }: Props) => (
  <LinearGradient
    colors={[colors.aubergineDeep, colors.bgCanvas]}
    start={{ x: 0.5, y: 0 }}
    end={{ x: 0.5, y: 0.6 }}
    style={{ flex: 1, paddingHorizontal: 24, paddingVertical: 60, alignItems: 'center' }}
  >
    <View style={{ flex: 1 }} />
    <View
      style={{
        width: 110,
        height: 110,
        borderRadius: 55,
        backgroundColor: colors.goldDeep,
        alignItems: 'center',
        justifyContent: 'center',
        shadowColor: colors.goldPrimary,
        shadowOpacity: 0.4,
        shadowRadius: 30,
        shadowOffset: { width: 0, height: 0 },
        elevation: 14,
      }}
    >
      <LinearGradient
        colors={[colors.goldBright, colors.goldDeep, colors.goldDeeper]}
        style={{
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          borderRadius: 55,
        }}
      />
      <Text
        style={{
          fontFamily: fonts.display,
          fontSize: 44,
          color: '#1A1410',
          textShadowColor: 'rgba(255,255,255,0.2)',
          textShadowOffset: { width: 0, height: 1 },
          textShadowRadius: 0,
        }}
      >
        C
      </Text>
    </View>
    <View style={{ alignItems: 'center', gap: 8, marginTop: 24 }}>
      <Text
        style={{
          fontFamily: fonts.display,
          fontSize: 30,
          color: colors.goldBright,
          letterSpacing: 4,
        }}
      >
        CLAN WORLD
      </Text>
      <Text
        style={{
          fontFamily: fonts.script,
          fontStyle: 'italic',
          fontSize: 18,
          color: colors.scriptBlue,
        }}
      >
        Ælder Whispers
      </Text>
    </View>
    <View style={{ marginTop: 24, alignSelf: 'stretch' }}>
      <DiamondRow />
    </View>
    <View style={{ flex: 1 }} />
    <Btn
      variant="primary"
      onPress={onEnter}
      style={{ minWidth: 240, marginBottom: 12 }}
      paddingVertical={14}
    >
      Connect Wallet →
    </Btn>
    <Text
      style={{
        fontFamily: fonts.mono,
        fontSize: 9,
        color: colors.inkDarkFaint,
        textAlign: 'center',
        maxWidth: 280,
        letterSpacing: 1,
        marginBottom: 12,
      }}
    >
      MOBILE WALLET ADAPTER · PHANTOM · SOLFLARE · BACKPACK · SEEKER
    </Text>
    <Text
      style={{
        fontFamily: fonts.bodyItalic,
        fontStyle: 'italic',
        fontSize: 12,
        color: colors.inkDarkMuted,
        textAlign: 'center',
        maxWidth: 300,
      }}
    >
      Four autonomous Elders compete on Base Sepolia in real time. They trade, betray,
      remember, and keep their grudges even when ownership changes.
    </Text>
  </LinearGradient>
);
