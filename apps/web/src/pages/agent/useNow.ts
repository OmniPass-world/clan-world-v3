import { useSyncExternalStore } from 'react';

/**
 * Subscribe-don't-re-render-parent ticking clock.
 *
 * Backed by a single shared `setInterval` per `intervalMs` value. Components
 * that need the current Date.now() call `useNow(250)` and re-render only
 * themselves, leaving their parent subtree untouched.
 *
 * Implementation: `useSyncExternalStore` lets us return the snapshot from
 * an external store. The store keeps a ref-counted interval so it only
 * runs while at least one component is subscribed.
 */

interface Store {
  now: number;
  listeners: Set<() => void>;
  intervalId: number | null;
}

const stores = new Map<number, Store>();

function getStore(intervalMs: number): Store {
  let store = stores.get(intervalMs);
  if (!store) {
    store = { now: Date.now(), listeners: new Set(), intervalId: null };
    stores.set(intervalMs, store);
  }
  return store;
}

function ensureRunning(store: Store, intervalMs: number) {
  if (store.intervalId !== null) return;
  store.intervalId = window.setInterval(() => {
    store.now = Date.now();
    for (const listener of store.listeners) listener();
  }, intervalMs);
}

function maybeStop(store: Store) {
  if (store.listeners.size === 0 && store.intervalId !== null) {
    window.clearInterval(store.intervalId);
    store.intervalId = null;
  }
}

export function useNow(intervalMs: number = 250): number {
  return useSyncExternalStore(
    (callback) => {
      const store = getStore(intervalMs);
      store.listeners.add(callback);
      ensureRunning(store, intervalMs);
      return () => {
        store.listeners.delete(callback);
        maybeStop(store);
      };
    },
    () => getStore(intervalMs).now,
    () => Date.now(),
  );
}
