import { useEffect, useMemo, useRef } from 'react';

/**
 * Tracks all `setTimeout` IDs registered through `tt.set(...)` and clears
 * them in a single `useEffect` cleanup on unmount. Prevents leaked timers
 * that fire after a component is gone (calling `setState` on a dead tree
 * → React 18 warning + potential bugs in the closure-captured state).
 *
 * Usage:
 *
 *   const tt = useTimeouts();
 *   tt.set(() => doThing(), 800);  // tracked, auto-cleared on unmount
 *   tt.clear(id);                   // optional manual clear
 */
export interface TimeoutHandle {
  set(fn: () => void, ms: number): number;
  clear(id: number): void;
}

export function useTimeouts(): TimeoutHandle {
  const ref = useRef<Set<number>>(new Set());

  useEffect(
    () => () => {
      for (const id of ref.current) clearTimeout(id);
      ref.current.clear();
    },
    [],
  );

  return useMemo<TimeoutHandle>(
    () => ({
      set(fn: () => void, ms: number): number {
        const id = window.setTimeout(() => {
          ref.current.delete(id);
          fn();
        }, ms);
        ref.current.add(id);
        return id;
      },
      clear(id: number): void {
        window.clearTimeout(id);
        ref.current.delete(id);
      },
    }),
    [],
  );
}
