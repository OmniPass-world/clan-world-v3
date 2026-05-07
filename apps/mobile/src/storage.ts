import { MMKV } from 'react-native-mmkv';
import type { ArchetypeKey, Strategy } from './data';

// Single MMKV instance for all mobile-only state. Keys are namespaced under
// `cw.` to keep diagnostic dumps grouped. Reads are synchronous — render code
// can call getLoadedClanId() directly without effect/state choreography.
const mmkv = new MMKV({ id: 'clan-world' });

const KEYS = {
  // Generic INFT id — `clan-N` for one of the 4 live clans, or `forged-<sig>`
  // for a forged INFT. null/missing means "nothing loaded into the hero."
  loadedInftId: 'cw.loadedInftId',
  forgedInfts: 'cw.forgedInfts',
  freeForgeUsedByPubkey: 'cw.freeForgeUsed',
  walletPubkey: 'cw.walletPubkey',
  walletAuthToken: 'cw.walletAuthToken',
  sgtCheckByPubkey: 'cw.sgtCheck',
} as const;

export type ForgedInft = {
  /** Local id — `forged-<base58 short of mintTxSig>`. */
  id: string;
  /** Wallet that forged this. */
  pubkey: string;
  name: string;
  archetype: ArchetypeKey;
  strategy: Strategy;
  /** Solana memo-tx signature returned by MWA. */
  mintTxSig: string;
  isFreeForge: boolean;
  createdAt: number;
};

/** Cached SGT-ownership result; refreshed every 24h. */
export type SgtCheckEntry = { hasToken: boolean; checkedAt: number };
const SGT_TTL_MS = 24 * 60 * 60 * 1000;

// ── Loaded INFT ──

export const getLoadedInftId = (): string | null => {
  return mmkv.getString(KEYS.loadedInftId) ?? null;
};

export const setLoadedInftId = (inftId: string | null): void => {
  if (inftId == null) mmkv.delete(KEYS.loadedInftId);
  else mmkv.set(KEYS.loadedInftId, inftId);
};

/** Returns the integer clanId if `inftId` is a real-clan id, else null. */
export const realClanIdFromInftId = (inftId: string | null): number | null => {
  if (!inftId) return null;
  const m = inftId.match(/^clan-(\d+)$/);
  return m ? Number(m[1]) : null;
};

// ── Forged INFTs ──

export const getForgedInfts = (pubkey: string | null): ForgedInft[] => {
  if (!pubkey) return [];
  const raw = mmkv.getString(KEYS.forgedInfts);
  if (!raw) return [];
  try {
    const all = JSON.parse(raw) as ForgedInft[];
    return all.filter((f) => f.pubkey === pubkey);
  } catch {
    return [];
  }
};

export const addForgedInft = (inft: ForgedInft): void => {
  const raw = mmkv.getString(KEYS.forgedInfts) ?? '[]';
  let all: ForgedInft[];
  try {
    all = JSON.parse(raw);
  } catch {
    all = [];
  }
  all.push(inft);
  mmkv.set(KEYS.forgedInfts, JSON.stringify(all));
};

// ── Free-forge tracking (per pubkey, per device) ──

export const getFreeForgeUsed = (pubkey: string | null): boolean => {
  if (!pubkey) return false;
  const raw = mmkv.getString(KEYS.freeForgeUsedByPubkey);
  if (!raw) return false;
  try {
    const map = JSON.parse(raw) as Record<string, boolean>;
    return map[pubkey] === true;
  } catch {
    return false;
  }
};

export const setFreeForgeUsed = (pubkey: string, used: boolean): void => {
  const raw = mmkv.getString(KEYS.freeForgeUsedByPubkey) ?? '{}';
  let map: Record<string, boolean>;
  try {
    map = JSON.parse(raw);
  } catch {
    map = {};
  }
  map[pubkey] = used;
  mmkv.set(KEYS.freeForgeUsedByPubkey, JSON.stringify(map));
};

// ── Wallet (MWA) ──

export const getWalletPubkey = (): string | null =>
  mmkv.getString(KEYS.walletPubkey) ?? null;

export const setWalletPubkey = (pubkey: string | null): void => {
  if (!pubkey) mmkv.delete(KEYS.walletPubkey);
  else mmkv.set(KEYS.walletPubkey, pubkey);
};

export const getWalletAuthToken = (): string | null =>
  mmkv.getString(KEYS.walletAuthToken) ?? null;

export const setWalletAuthToken = (token: string | null): void => {
  if (!token) mmkv.delete(KEYS.walletAuthToken);
  else mmkv.set(KEYS.walletAuthToken, token);
};

// ── Seeker Genesis Token cache ──

export const getSgtCheck = (pubkey: string): SgtCheckEntry | null => {
  const raw = mmkv.getString(KEYS.sgtCheckByPubkey);
  if (!raw) return null;
  try {
    const map = JSON.parse(raw) as Record<string, SgtCheckEntry>;
    const entry = map[pubkey];
    if (!entry) return null;
    if (Date.now() - entry.checkedAt > SGT_TTL_MS) return null;
    return entry;
  } catch {
    return null;
  }
};

export const setSgtCheck = (pubkey: string, hasToken: boolean): void => {
  const raw = mmkv.getString(KEYS.sgtCheckByPubkey) ?? '{}';
  let map: Record<string, SgtCheckEntry>;
  try {
    map = JSON.parse(raw);
  } catch {
    map = {};
  }
  map[pubkey] = { hasToken, checkedAt: Date.now() };
  mmkv.set(KEYS.sgtCheckByPubkey, JSON.stringify(map));
};

// ── Reset (debug / sign-out) ──

export const clearAll = (): void => {
  mmkv.clearAll();
};
