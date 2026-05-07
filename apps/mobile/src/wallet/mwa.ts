import { transact } from '@solana-mobile/mobile-wallet-adapter-protocol-web3js';
import {
  Connection,
  PublicKey,
  Transaction,
  TransactionInstruction,
} from '@solana/web3.js';
import {
  getWalletAuthToken,
  getWalletPubkey,
  setWalletAuthToken,
  setWalletPubkey,
} from '../storage';

/**
 * Identity presented to the wallet during authorize/reauthorize. Shows up in
 * the wallet's UI so the user knows which dApp is asking to sign.
 */
const APP_IDENTITY = {
  name: 'Clan World',
  uri: 'https://demo.clan-world.com',
  icon: 'favicon.ico', // path relative to uri; placeholder until we ship a real one
};

const CHAIN = 'solana:mainnet';

const MEMO_PROGRAM_ID = new PublicKey(
  'MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr',
);

export type Authorized = {
  pubkey: PublicKey;
  authToken: string;
};

const decodeBase64 = (s: string): Uint8Array =>
  Uint8Array.from(globalThis.atob(s), (c) => c.charCodeAt(0));

/**
 * Connect — opens the user's MWA wallet, authorizes the app, returns the
 * primary account public key and the auth token (cached in MMKV).
 *
 * On Seeker, this opens the built-in Seed Vault wallet. On other Android
 * devices it routes to whichever Solana wallet the user has installed
 * (Phantom, Solflare, Backpack).
 */
export const connectWallet = async (): Promise<Authorized> => {
  const cachedToken = getWalletAuthToken();

  return transact(async (wallet) => {
    const result = cachedToken
      ? await wallet.reauthorize({
          auth_token: cachedToken,
          identity: APP_IDENTITY,
        })
      : await wallet.authorize({
          chain: CHAIN,
          identity: APP_IDENTITY,
        });

    const account = result.accounts[0];
    if (!account) throw new Error('No account returned from MWA authorize');

    const pubkey = new PublicKey(decodeBase64(account.address));
    setWalletPubkey(pubkey.toBase58());
    setWalletAuthToken(result.auth_token);

    return { pubkey, authToken: result.auth_token };
  });
};

/**
 * Sign an arbitrary off-chain message with the connected wallet.
 * Used for the J1 connection challenge — proves the user controls the pubkey
 * without spending any SOL.
 */
export const signMessage = async (
  message: string,
): Promise<{ signature: Uint8Array; pubkey: PublicKey }> => {
  const cachedToken = getWalletAuthToken();
  if (!cachedToken) throw new Error('Wallet not connected (no cached token)');

  return transact(async (wallet) => {
    const auth = await wallet.reauthorize({
      auth_token: cachedToken,
      identity: APP_IDENTITY,
    });
    const account = auth.accounts[0];
    if (!account) throw new Error('No account on reauthorize');
    const pubkey = new PublicKey(decodeBase64(account.address));

    const messageBytes = new TextEncoder().encode(message);
    const signed = await wallet.signMessages({
      addresses: [account.address],
      payloads: [messageBytes],
    });
    return { signature: signed[0]!, pubkey };
  });
};

/**
 * Build a Solana memo-program transaction with the given memo string, then
 * have the user sign and submit it via MWA. Used for the J2 forge tx — the
 * memo records the forge metadata on-chain.
 *
 * Caller must pass a Connection (mainnet) so we can fetch a recent blockhash.
 * Returns the submitted transaction signature (base58).
 */
export const signAndSendMemoTx = async (
  connection: Connection,
  memo: string,
): Promise<string> => {
  const cachedToken = getWalletAuthToken();
  if (!cachedToken) throw new Error('Wallet not connected (no cached token)');

  return transact(async (wallet) => {
    const auth = await wallet.reauthorize({
      auth_token: cachedToken,
      identity: APP_IDENTITY,
    });
    const account = auth.accounts[0];
    if (!account) throw new Error('No account on reauthorize');
    const payer = new PublicKey(decodeBase64(account.address));

    const { blockhash, lastValidBlockHeight } =
      await connection.getLatestBlockhash('confirmed');

    const memoIx = new TransactionInstruction({
      keys: [{ pubkey: payer, isSigner: true, isWritable: true }],
      programId: MEMO_PROGRAM_ID,
      data: Buffer.from(memo, 'utf-8'),
    });

    const tx = new Transaction({
      feePayer: payer,
      blockhash,
      lastValidBlockHeight,
    }).add(memoIx);

    const sigs = await wallet.signAndSendTransactions({
      transactions: [tx],
    });
    return sigs[0]!;
  });
};

export type ForgeReceipt = {
  /** Solana base58 signature. For 'tx' kind this is a real on-chain tx;
   *  for 'message' kind it's a signed-message digest hex used as a
   *  pseudo-signature for the local record. */
  sig: string;
  /** 'tx' = real on-chain memo tx submitted, 'message' = off-chain
   *  signMessage fallback (used when wallet has insufficient SOL or the
   *  send-tx flow rejects). */
  kind: 'tx' | 'message';
};

/**
 * Forge receipt — tries to submit a real on-chain memo tx first. On any
 * failure (insufficient SOL, simulation rejection, RPC hiccup, user
 * dismissal), falls back to an off-chain signMessage so the forge UX still
 * completes. The returned `kind` lets callers decide whether to surface a
 * Solscan link.
 */
export const signForgeReceipt = async (
  connection: Connection,
  memo: string,
): Promise<ForgeReceipt> => {
  try {
    const sig = await signAndSendMemoTx(connection, memo);
    return { sig, kind: 'tx' };
  } catch (txErr) {
    if (__DEV__) {
      const m = txErr instanceof Error ? txErr.message : String(txErr);
      console.warn('[forge] tx send failed, falling back to signMessage:', m);
    }
    // Off-chain fallback. Doesn't cost SOL, doesn't appear on Solscan, but
    // still produces a wallet-signed payload we can store as the receipt.
    const { signature } = await signMessage(memo);
    const sigB58 = Buffer.from(signature).toString('base64').slice(0, 64);
    return { sig: sigB58, kind: 'message' };
  }
};

/** Read-only — does the user have a cached connection? */
export const getCachedAuth = (): { pubkey: string | null; authToken: string | null } => ({
  pubkey: getWalletPubkey(),
  authToken: getWalletAuthToken(),
});

export const disconnectWallet = (): void => {
  setWalletPubkey(null);
  setWalletAuthToken(null);
};
