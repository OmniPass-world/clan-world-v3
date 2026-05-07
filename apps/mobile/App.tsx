import React, { useEffect, useState } from 'react';
import { ActivityIndicator, View } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider, SafeAreaView } from 'react-native-safe-area-context';
import { useFonts } from 'expo-font';
import { ConvexProvider, ConvexReactClient } from 'convex/react';

// Cinzel
import {
  Cinzel_400Regular,
  Cinzel_500Medium,
  Cinzel_600SemiBold,
  Cinzel_700Bold,
} from '@expo-google-fonts/cinzel';
// Cormorant Garamond
import {
  CormorantGaramond_400Regular_Italic,
  CormorantGaramond_500Medium_Italic,
  CormorantGaramond_600SemiBold_Italic,
} from '@expo-google-fonts/cormorant-garamond';
// EB Garamond
import {
  EBGaramond_400Regular,
  EBGaramond_500Medium,
  EBGaramond_400Regular_Italic,
} from '@expo-google-fonts/eb-garamond';
// JetBrains Mono
import {
  JetBrainsMono_400Regular,
  JetBrainsMono_500Medium,
  JetBrainsMono_600SemiBold,
} from '@expo-google-fonts/jetbrains-mono';
import { Connection } from '@solana/web3.js';

import { colors } from './src/theme';
import { TabBar, TabKey } from './src/components/TabBar';
import { Toast } from './src/components/Toast';
import { HireModal } from './src/components/HireModal';
import { Inft } from './src/data';
import {
  getLoadedInftId,
  setLoadedInftId,
  getWalletPubkey,
} from './src/storage';
import { hasSeekerGenesisToken, isSeekerDevice } from './src/seeker';
import { PublicKey } from '@solana/web3.js';

import { SplashScreen } from './src/screens/SplashScreen';
import { HearthScreen } from './src/screens/HearthScreen';
import { HallScreen } from './src/screens/HallScreen';
import { BazaarScreen } from './src/screens/BazaarScreen';
import { TreasuryScreen } from './src/screens/TreasuryScreen';
import { InftDetailScreen } from './src/screens/InftDetailScreen';
import { StrategyEditorScreen } from './src/screens/StrategyEditorScreen';
import { SteeringConsoleScreen } from './src/screens/SteeringConsoleScreen';
import { ForgeScreen } from './src/screens/ForgeScreen';
import { CockpitScreen } from './src/screens/CockpitScreen';
import { BridgeScreen } from './src/screens/BridgeScreen';
import { WhispersScreen } from './src/screens/WhispersScreen';
import { CodexScreen } from './src/screens/CodexScreen';

const CONVEX_URL =
  process.env.EXPO_PUBLIC_CONVEX_URL ?? 'https://valuable-kudu-985.convex.cloud';
const SOLANA_RPC =
  process.env.EXPO_PUBLIC_SOLANA_RPC ?? 'https://api.mainnet-beta.solana.com';

const convex = new ConvexReactClient(CONVEX_URL, { unsavedChangesWarning: false });
const solana = new Connection(SOLANA_RPC, 'confirmed');

type StackEntry =
  | { kind: 'whispers' }
  | { kind: 'codex' }
  | { kind: 'inft'; inft: Inft }
  | { kind: 'bazaar-inft'; inft: Inft }
  | { kind: 'strategy-edit'; inft: Inft }
  | { kind: 'steering'; inft: Inft };

export default function App() {
  const [fontsLoaded] = useFonts({
    Cinzel_400Regular,
    Cinzel_500Medium,
    Cinzel_600SemiBold,
    Cinzel_700Bold,
    CormorantGaramond_400Regular_Italic,
    CormorantGaramond_500Medium_Italic,
    CormorantGaramond_600SemiBold_Italic,
    EBGaramond_400Regular,
    EBGaramond_500Medium,
    EBGaramond_400Regular_Italic,
    JetBrainsMono_400Regular,
    JetBrainsMono_500Medium,
    JetBrainsMono_600SemiBold,
  });

  // Auth — pubkey from MMKV cache; if present we skip Splash.
  const [pubkey, setPubkey] = useState<string | null>(() => getWalletPubkey());
  const splashSeen = pubkey !== null;

  // Seeker detection — M1 instant (sync), M2 async after auth resolves.
  const [seekerM1] = useState<boolean>(() => isSeekerDevice());
  const [seekerM2, setSeekerM2] = useState<boolean | null>(null);

  useEffect(() => {
    if (!pubkey) return;
    let cancelled = false;
    hasSeekerGenesisToken(solana, new PublicKey(pubkey)).then((has) => {
      if (!cancelled) setSeekerM2(has);
    });
    return () => {
      cancelled = true;
    };
  }, [pubkey]);

  const isSeekerBearer = seekerM1 || seekerM2 === true;

  // Routing state
  const [tab, setTab] = useState<TabKey>('hearth');
  const [stack, setStack] = useState<StackEntry[]>([]);
  const [toast, setToast] = useState<string | null>(null);
  const [hire, setHire] = useState<Inft | null>(null);
  const [bridge, setBridge] = useState(false);
  const [cockpit, setCockpit] = useState<Inft | null>(null);
  const [forge, setForge] = useState(false);
  const [loadedInftIdState, setLoadedInftIdState] = useState<string | null>(
    () => getLoadedInftId(),
  );

  const top = stack[stack.length - 1];
  const push = (s: StackEntry) => setStack((prev) => [...prev, s]);
  const pop = () => setStack((prev) => prev.slice(0, -1));
  const showToast = (text: string) => setToast(text);

  const navigate = (target: 'forge' | 'bazaar' | 'hearth') => {
    if (target === 'forge') setForge(true);
    else if (target === 'bazaar') {
      setStack([]);
      setTab('bazaar');
    } else if (target === 'hearth') {
      setStack([]);
      setTab('hearth');
    }
  };

  const enterCockpit = (inft: Inft) => {
    setBridge(true);
    setTimeout(() => {
      setBridge(false);
      setCockpit(inft);
    }, 1400);
  };

  /** Load/Unload toggle — writes through to MMKV so it survives reloads. */
  const updateLoadedInft = (inftId: string | null) => {
    setLoadedInftId(inftId);
    setLoadedInftIdState(inftId);
  };

  if (!fontsLoaded) {
    return (
      <SafeAreaProvider>
        <View
          style={{
            flex: 1,
            backgroundColor: colors.bgCanvas,
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <ActivityIndicator color={colors.goldPrimary} />
        </View>
      </SafeAreaProvider>
    );
  }

  let body: React.ReactNode = null;

  if (!splashSeen) {
    body = (
      <SplashScreen
        seekerDetected={seekerM1}
        onAuthed={(pk) => {
          setPubkey(pk);
          showToast('Welcome. Your hall awaits.');
        }}
      />
    );
  } else if (cockpit) {
    body = (
      <CockpitScreen
        inft={cockpit}
        onClose={() => setCockpit(null)}
        onWhisper={() => push({ kind: 'steering', inft: cockpit })}
      />
    );
  } else if (bridge) {
    body = <BridgeScreen />;
  } else if (forge) {
    body = (
      <ForgeScreen
        pubkey={pubkey!}
        isFreeForgeEligible={isSeekerBearer}
        connection={solana}
        onClose={() => setForge(false)}
        onComplete={() => {
          setForge(false);
          showToast('Forged. Welcome a new Elder to your hall.');
        }}
      />
    );
  } else if (top?.kind === 'whispers') {
    body = <WhispersScreen onBack={pop} />;
  } else if (top?.kind === 'codex') {
    body = <CodexScreen onBack={pop} />;
  } else if (top?.kind === 'steering') {
    body = (
      <SteeringConsoleScreen
        inft={top.inft}
        onBack={pop}
        onSend={() => showToast('Whisper queued for next tick.')}
      />
    );
  } else if (top?.kind === 'strategy-edit') {
    const editingInft = top.inft;
    body = (
      <StrategyEditorScreen
        inft={editingInft}
        onBack={pop}
        onSave={() => {
          showToast('Strategy editor — wired in slice 2.');
          pop();
        }}
      />
    );
  } else if (top?.kind === 'inft') {
    body = (
      <InftDetailScreen
        inft={top.inft}
        loadedInftId={loadedInftIdState}
        onBack={pop}
        onEdit={() => push({ kind: 'strategy-edit', inft: top.inft })}
        onWhisper={() => push({ kind: 'steering', inft: top.inft })}
        onCockpit={() => enterCockpit(top.inft)}
        onLoad={(inftId) => {
          updateLoadedInft(inftId);
          showToast(
            inftId == null
              ? 'Unloaded from your hall.'
              : `Loaded ${top.inft.name} into your hall.`,
          );
        }}
      />
    );
  } else if (top?.kind === 'bazaar-inft') {
    body = (
      <InftDetailScreen
        inft={top.inft}
        loadedInftId={loadedInftIdState}
        onBack={pop}
        isBazaar
        onHire={() => setHire(top.inft)}
      />
    );
  } else {
    if (tab === 'hearth') {
      body = (
        <HearthScreen
          loadedInftId={loadedInftIdState}
          pubkey={pubkey}
          onEnterCockpit={(inft) => enterCockpit(inft)}
          onWhispers={() => push({ kind: 'whispers' })}
          onSettings={() => push({ kind: 'codex' })}
          navigate={navigate}
        />
      );
    } else if (tab === 'hall') {
      body = (
        <HallScreen
          pubkey={pubkey}
          loadedInftId={loadedInftIdState}
          onOpenInft={(inft) => push({ kind: 'inft', inft })}
          onForge={() => setForge(true)}
        />
      );
    } else if (tab === 'bazaar') {
      body = (
        <BazaarScreen onOpenInft={(inft) => push({ kind: 'bazaar-inft', inft })} />
      );
    } else if (tab === 'treasury') {
      body = (
        <TreasuryScreen
          onBuyGold={() => showToast('Opening Kickstart in WebView…')}
          onTx={() => showToast('Transaction details coming.')}
        />
      );
    }
  }

  const onRoot = !top && !cockpit && !bridge && !forge && splashSeen;

  return (
    <ConvexProvider client={convex}>
      <SafeAreaProvider>
        <StatusBar style="light" />
        <SafeAreaView
          style={{ flex: 1, backgroundColor: colors.bgCanvas }}
          edges={['top', 'bottom']}
        >
          <View style={{ flex: 1, position: 'relative' }}>
            <View style={{ flex: 1 }}>{body}</View>
            {onRoot && <TabBar active={tab} onChange={setTab} />}
            {toast && <Toast text={toast} onDismiss={() => setToast(null)} />}
            <HireModal
              visible={!!hire}
              inft={hire}
              onCancel={() => setHire(null)}
              onConfirm={() => {
                const hiredInft = hire!;
                setHire(null);
                showToast(
                  `Hire flow — wired in slice 2. (${hiredInft.name})`,
                );
              }}
            />
          </View>
        </SafeAreaView>
      </SafeAreaProvider>
    </ConvexProvider>
  );
}
