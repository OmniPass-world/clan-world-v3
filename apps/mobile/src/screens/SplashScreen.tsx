import React, { useState } from 'react';
import { ActivityIndicator, Image, Text, View } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import * as Haptics from 'expo-haptics';
import { Btn } from '../components/Buttons';
import { DiamondRow } from '../components/Diamond';
import { colors, fonts } from '../theme';
import { connectWallet, disconnectWallet } from '../wallet/mwa';

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
      // Single MWA prompt: authorize + SIWS sign-in in one round-trip
      // (see connectWallet for why this is one call, not two).
      const { pubkey } = await connectWallet();
      try {
        await Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
      } catch {
        // haptics unavailable on emulator — non-fatal
      }
      onAuthed(pubkey.toBase58());
    } catch (e: unknown) {
      // Connection failed or user dismissed wallet — clear any partial state
      // so the user lands cleanly back on the splash screen and a reload
      // doesn't silently auto-login from a half-completed session.
      disconnectWallet();
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
          shadowColor: colors.goldPrimary,
          shadowOpacity: 0.5,
          shadowRadius: 36,
          shadowOffset: { width: 0, height: 0 },
          elevation: 18,
        }}
      >
        <Image
          source={require('../../assets/logo-main-square.png')}
          style={{
            width: 140,
            height: 140,
            borderRadius: 16,
          }}
          resizeMode="contain"
        />
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
      {/* Smaller middle spacer pulls the Connect button up so it sits
          visually centered between the logo block above and the wallet
          adapter caption + realm description below. */}
      <View style={{ flex: 0.4 }} />

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
