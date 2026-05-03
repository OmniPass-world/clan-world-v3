import { useCallback, useEffect, useMemo, useRef, useState } from 'react';

export type TerminalFrameStatus =
  | 'connecting'
  | 'connected'
  | 'reconnecting'
  | 'disconnected';

const BACKOFF_MS = [2_000, 4_000, 8_000, 15_000];

interface Options {
  baseUrl: string;
  clanId: number;
}

interface TtydStatusMessage {
  type: 'clanworld-ttyd-status';
  clanId: number;
  status: 'open' | 'close' | 'error';
}

function isTtydStatusMessage(value: unknown): value is TtydStatusMessage {
  if (!value || typeof value !== 'object') return false;
  const msg = value as Partial<TtydStatusMessage>;
  return (
    msg.type === 'clanworld-ttyd-status' &&
    typeof msg.clanId === 'number' &&
    (msg.status === 'open' || msg.status === 'close' || msg.status === 'error')
  );
}

function withReconnectToken(baseUrl: string, token: number): string {
  const url = new URL(baseUrl);
  url.searchParams.set('cwReconnect', String(token));
  return url.toString();
}

export function useTerminalFrameReconnect({
  baseUrl,
  clanId,
}: Options): {
  src: string;
  status: TerminalFrameStatus;
  reconnectNow: () => void;
  onFrameLoad: () => void;
} {
  const [status, setStatus] = useState<TerminalFrameStatus>('connecting');
  const [reloadToken, setReloadToken] = useState(0);
  const attemptRef = useRef(0);
  const reconnectTimerRef = useRef<ReturnType<typeof setTimeout>>();
  const expectedOrigin = useMemo(() => new URL(baseUrl).origin, [baseUrl]);
  const src = useMemo(() => withReconnectToken(baseUrl, reloadToken), [baseUrl, reloadToken]);

  const clearReconnectTimer = useCallback(() => {
    if (!reconnectTimerRef.current) return;
    clearTimeout(reconnectTimerRef.current);
    reconnectTimerRef.current = undefined;
  }, []);

  const reload = useCallback(() => {
    clearReconnectTimer();
    setStatus('reconnecting');
    setReloadToken((n) => n + 1);
  }, [clearReconnectTimer]);

  const scheduleReconnect = useCallback(() => {
    clearReconnectTimer();
    const delay = BACKOFF_MS[Math.min(attemptRef.current, BACKOFF_MS.length - 1)];
    attemptRef.current += 1;
    setStatus(attemptRef.current > 1 ? 'disconnected' : 'reconnecting');
    reconnectTimerRef.current = setTimeout(reload, delay);
  }, [clearReconnectTimer, reload]);

  const reconnectNow = useCallback(() => {
    attemptRef.current = 0;
    reload();
  }, [reload]);

  const onFrameLoad = useCallback(() => {
    attemptRef.current = 0;
    clearReconnectTimer();
    setStatus('connected');
  }, [clearReconnectTimer]);

  useEffect(() => {
    const onMessage = (event: MessageEvent) => {
      if (event.origin !== expectedOrigin) return;
      if (!isTtydStatusMessage(event.data)) return;
      if (event.data.clanId !== clanId) return;

      if (event.data.status === 'open') {
        attemptRef.current = 0;
        clearReconnectTimer();
        setStatus('connected');
        return;
      }

      scheduleReconnect();
    };

    window.addEventListener('message', onMessage);
    return () => window.removeEventListener('message', onMessage);
  }, [
    clanId,
    clearReconnectTimer,
    expectedOrigin,
    scheduleReconnect,
  ]);

  useEffect(() => {
    return () => {
      clearReconnectTimer();
    };
  }, [clearReconnectTimer]);

  return { src, status, reconnectNow, onFrameLoad };
}
