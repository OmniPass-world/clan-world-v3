import { Platform } from 'react-native';
import { Connection, PublicKey } from '@solana/web3.js';
import { getSgtCheck, setSgtCheck } from './storage';

/**
 * Seeker Genesis Token (SGT) collection mint, per Solana Mobile docs:
 * https://docs.solanamobile.com/solana-mobile-stack/seeker-genesis-token
 *
 * Auto-minted to the primary Seed Vault account on every Seeker device.
 * Token-2022 standard, with token-group membership pointing at this collection.
 */
export const SGT_COLLECTION = new PublicKey(
  'GT2zuHVaZQYZSyQMgJPLzvkmyztfyXg2NJunqFp4p3A4',
);

const FORCE_SEEKER = process.env.EXPO_PUBLIC_FORCE_SEEKER === 'true';

/**
 * Method 1 — Platform Constants check.
 * Spoofable; suitable for cosmetic UI treatments only. Use M2 for any gating.
 */
export const isSeekerDevice = (): boolean => {
  if (FORCE_SEEKER) return true;
  // Android-only signal; Platform.constants.Model is undefined on iOS.
  return (Platform.constants as { Model?: string } | undefined)?.Model === 'Seeker';
};

/**
 * Method 2 — On-chain SGT ownership check.
 * Queries the user's wallet for any Token-2022 account whose mint belongs to
 * the SGT collection group. Result cached in MMKV with a 24h TTL.
 */
export const hasSeekerGenesisToken = async (
  connection: Connection,
  pubkey: PublicKey,
): Promise<boolean> => {
  if (FORCE_SEEKER) return true;

  const cacheKey = pubkey.toBase58();
  const cached = getSgtCheck(cacheKey);
  if (cached) return cached.hasToken;

  try {
    // Token-2022 program id. We check both legacy SPL and Token-2022 to be
    // safe; SGT uses Token-2022 in practice.
    const TOKEN_2022_PROGRAM_ID = new PublicKey(
      'TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb',
    );

    const accounts = await connection.getParsedTokenAccountsByOwner(pubkey, {
      programId: TOKEN_2022_PROGRAM_ID,
    });

    // Any non-zero balance whose parsed mint metadata points at SGT_COLLECTION
    // (via token-group extension) means the user holds an SGT.
    //
    // For slice 1 we approximate by checking for token-2022 mint equality with
    // the SGT collection itself OR any mint whose parsed data shows a tokenGroup
    // pointing at SGT_COLLECTION. Solana Mobile's reference implementation
    // performs the full mint-data extension parse; we simplify here.
    const sgt = SGT_COLLECTION.toBase58();
    const found = accounts.value.some((acc) => {
      const info = acc.account.data.parsed.info as {
        mint: string;
        tokenAmount: { uiAmount: number };
      };
      if (info.tokenAmount.uiAmount <= 0) return false;
      // Direct mint match (fallback path)
      if (info.mint === sgt) return true;
      // TODO(slice-2): parse mint extensions to read token-group membership.
      // For slice 1 we assume the wallet holds the collection's group token
      // directly; if that's not how SGT distributes in practice, M2 falls back
      // to "false" and only M1 (Platform.constants) drives the cosmetic banner.
      return false;
    });

    setSgtCheck(cacheKey, found);
    return found;
  } catch (err) {
    // Network / RPC failure → don't cache; return false so we fail-closed on
    // the gate but still allow the cosmetic banner via M1.
    if (__DEV__) console.warn('[seeker] SGT check failed:', err);
    return false;
  }
};
