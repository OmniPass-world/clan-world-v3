import { createConfig, http } from 'wagmi';
import { baseSepolia } from 'wagmi/chains';
import { injected, walletConnect } from 'wagmi/connectors';

const WALLETCONNECT_PROJECT_ID = import.meta.env.VITE_WALLETCONNECT_PROJECT_ID as
  | string
  | undefined;

// WalletConnect is optional in dev-ui — when no project ID is configured we
// skip the connector entirely rather than attribute the dev environment to a
// hardcoded WC account's quota. Injected (MetaMask/Rabby/etc.) still works.
if (!WALLETCONNECT_PROJECT_ID) {
  // eslint-disable-next-line no-console
  console.warn(
    '[dev-ui] VITE_WALLETCONNECT_PROJECT_ID is not set — WalletConnect connector will be disabled. ' +
      'Set it in .env.local to enable WC. Injected wallets still work.',
  );
}

const connectors = WALLETCONNECT_PROJECT_ID
  ? [
      injected(),
      walletConnect({ projectId: WALLETCONNECT_PROJECT_ID, showQrModal: true }),
    ]
  : [injected()];

export const config = createConfig({
  chains: [baseSepolia],
  connectors,
  transports: {
    [baseSepolia.id]: http(),
  },
});

declare module 'wagmi' {
  interface Register {
    config: typeof config;
  }
}
