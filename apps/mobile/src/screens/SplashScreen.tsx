import React, { useState } from 'react';
import { ActivityIndicator, Text, View } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import * as Haptics from 'expo-haptics';
import { Btn } from '../components/Buttons';
import { DiamondRow } from '../components/Diamond';
import { colors, fonts } from '../theme';
import { connectWallet, signMessage } from '../wallet/mwa';

type Props = {
  /** True if Platform.constants.Model === 'Seeker' (M1 instant check). */
  seekerDetected: boolean;
  onAuthed: (pubkey: string) => void;
};

export const SplashScreen = ({ seekerDetected, onAuthed }: Props) => {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleConnect = async () => {
    if (busy) return;
    setBusy(true);
    setError(null);
    try {
      const { pubkey } = await connectWallet();
      // Sign-in challenge (J1). Off-chain, free. Proves the user controls
      // the pubkey before we let them into the realm.
      const nonce = Math.random().toString(36).slice(2, 10);
      const challenge = `Sign in to Clan World — nonce: ${nonce}`;
      await signMessage(challenge);
      try {
        await Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
      } catch {
        // haptics unavailable on emulator — non-fatal
      }
      onAuthed(pubkey.toBase58());
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : 'Connection failed';
      setError(msg);
      setBusy(false);
    }
  };

  return (
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

      {seekerDetected && (
        <View
          style={{
            marginTop: 14,
            paddingHorizontal: 16,
            paddingVertical: 8,
            borderColor: colors.goldPrimary,
            borderWidth: 1,
            backgroundColor: 'rgba(212,175,92,0.08)',
          }}
        >
          <Text
            style={{
              fontFamily: fonts.display,
              fontSize: 10,
              letterSpacing: 1.6,
              color: colors.goldBright,
              textAlign: 'center',
            }}
          >
            ◇ SEEKER BEARER · WELCOMED
          </Text>
        </View>
      )}

      <View style={{ marginTop: 24, alignSelf: 'stretch' }}>
        <DiamondRow />
      </View>
      <View style={{ flex: 1 }} />

      <Btn
        variant="primary"
        onPress={handleConnect}
        disabled={busy}
        style={{ minWidth: 240, marginBottom: 12 }}
        paddingVertical={14}
      >
        {busy ? (
          <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
            <ActivityIndicator color={colors.goldBright} size="small" />
            <Text
              style={{
                fontFamily: fonts.display,
                fontSize: 12,
                color: colors.goldBright,
                letterSpacing: 1.6,
              }}
            >
              CONNECTING…
            </Text>
          </View>
        ) : (
          'Connect Wallet →'
        )}
      </Btn>

      {error && (
        <Text
          style={{
            fontFamily: fonts.mono,
            fontSize: 11,
            color: colors.statusDanger,
            textAlign: 'center',
            maxWidth: 280,
            marginBottom: 12,
          }}
        >
          {error}
        </Text>
      )}

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
};
