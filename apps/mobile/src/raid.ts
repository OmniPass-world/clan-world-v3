// Demo raid trigger.
//
// Two delivery paths so the user feels the raid land regardless of where the
// app is when the timer fires:
//
//   - Foreground: an in-app overlay (RaidAlertOverlay). The notification
//     handler suppresses the system banner because we own the visual.
//
//   - Background: a regular Android notification. Tapping it opens the app
//     with a payload that App.tsx reads to flip into the Hearth tab with
//     a raid banner active.
//
// We schedule the notification *and* a JS-side timer that flips raidActive
// when the app is foregrounded. If both fire (foreground case) the notif
// is silently dropped by the handler.
//
// Note: expo-notifications is loaded defensively. If the dev client APK
// doesn't have the native module (e.g. user is running the new JS bundle
// against the old APK), every raid function becomes a safe no-op and the
// rest of the app keeps working. Install the latest APK to get the
// raid demo back online.

import { MMKV } from 'react-native-mmkv';

type NotificationsModule = typeof import('expo-notifications');

let Notifications: NotificationsModule | null = null;
try {
  // eslint-disable-next-line @typescript-eslint/no-require-imports
  Notifications = require('expo-notifications');
} catch {
  Notifications = null;
  if (__DEV__) {
    console.warn(
      '[raid] expo-notifications native module not available — install the latest dev client APK to enable raid alerts.',
    );
  }
}

const raidStore = new MMKV({ id: 'clan-world-raid' });

const KEYS = {
  pendingRaidId: 'cw.raid.pendingId',
  pendingRaidFiredAt: 'cw.raid.firedAt',
} as const;

export const RAID_NOTIFICATION_DATA_TYPE = 'cw.raid.alert';

const ANDROID_CHANNEL_ID = 'cw-raid-alerts';

/** True when expo-notifications loaded successfully — i.e. the dev client
 *  APK has the native module. False when running an older APK that
 *  predates expo-notifications being added. */
export const isNotificationsAvailable = (): boolean => Notifications !== null;

/** Configure the foreground notification handler so a raid notification
 *  fired while the app is open does NOT show a system banner — we render
 *  the themed overlay instead. */
export const installRaidNotificationHandler = (): void => {
  if (!Notifications) return;
  Notifications.setNotificationHandler({
    handleNotification: async (notif) => {
      const data = notif.request.content.data as { type?: string } | null;
      const isRaid = data?.type === RAID_NOTIFICATION_DATA_TYPE;
      if (isRaid) {
        // We render our own overlay; suppress the system one entirely.
        return {
          shouldShowAlert: false,
          shouldShowBanner: false,
          shouldShowList: false,
          shouldPlaySound: false,
          shouldSetBadge: false,
        };
      }
      // Default: show normally
      return {
        shouldShowAlert: true,
        shouldShowBanner: true,
        shouldShowList: true,
        shouldPlaySound: true,
        shouldSetBadge: false,
      };
    },
  });
};

/** Ensure the Android notification channel exists. Needs to run once on
 *  app start before scheduling. No-op when expo-notifications isn't available. */
export const ensureRaidChannel = async (): Promise<void> => {
  if (!Notifications) return;
  try {
    await Notifications.setNotificationChannelAsync(ANDROID_CHANNEL_ID, {
      name: 'Raid alerts',
      description: 'Bandit raids on your settlement',
      importance: Notifications.AndroidImportance.HIGH,
      vibrationPattern: [0, 250, 250, 250],
      lightColor: '#A04A3F',
      sound: 'default',
    });
  } catch (e) {
    if (__DEV__) console.warn('[raid] channel setup failed:', e);
  }
};

/** Request notification permission. Returns true when granted. */
export const requestRaidPermission = async (): Promise<boolean> => {
  if (!Notifications) return false;
  const { status: existing } = await Notifications.getPermissionsAsync();
  if (existing === 'granted') return true;
  const { status } = await Notifications.requestPermissionsAsync();
  return status === 'granted';
};

/**
 * Schedule a demo raid alert to fire `delaySec` seconds from now.
 * Returns the notification ID (caller can pass it to cancelRaid).
 * Returns null when expo-notifications isn't available — caller should
 * still arm a foreground JS timer.
 */
export const scheduleRaidAlert = async (
  delaySec: number,
  victimName: string,
  tick: number,
): Promise<string | null> => {
  if (!Notifications) return null;
  await ensureRaidChannel();
  const granted = await requestRaidPermission();
  if (!granted && __DEV__) {
    console.warn('[raid] notification permission not granted; in-app overlay only');
  }

  const id = await Notifications.scheduleNotificationAsync({
    content: {
      title: '⚔ Bandit raid',
      body: `${victimName} is under attack at T${tick}.`,
      data: { type: RAID_NOTIFICATION_DATA_TYPE, victim: victimName, tick },
      sound: 'default',
      priority: Notifications.AndroidNotificationPriority.HIGH,
    },
    trigger: {
      type: Notifications.SchedulableTriggerInputTypes.TIME_INTERVAL,
      seconds: delaySec,
      channelId: ANDROID_CHANNEL_ID,
    },
  });

  raidStore.set(KEYS.pendingRaidId, id);
  raidStore.set(KEYS.pendingRaidFiredAt, Date.now() + delaySec * 1000);
  return id;
};

/** Cancel a pending raid notification (used when foreground timer wins
 *  and we want to drop the system notif before it fires). */
export const cancelRaidAlert = async (): Promise<void> => {
  if (!Notifications) return;
  const id = raidStore.getString(KEYS.pendingRaidId);
  if (id) {
    try {
      await Notifications.cancelScheduledNotificationAsync(id);
    } catch {
      /* notif may have already fired; safe to ignore */
    }
    raidStore.delete(KEYS.pendingRaidId);
    raidStore.delete(KEYS.pendingRaidFiredAt);
  }
};

export type NotificationResponseLike = {
  notification: {
    request: {
      content: {
        data: { type?: string; victim?: string; tick?: number } | null;
      };
    };
  };
};

/** When a notification response is received (user tapped a notif), check
 *  if it was a raid alert. Returns metadata if so, else null. */
export const parseRaidResponse = (
  response: NotificationResponseLike,
): { victim: string; tick: number } | null => {
  const data = response.notification.request.content.data ?? null;
  if (!data || data.type !== RAID_NOTIFICATION_DATA_TYPE) return null;
  return {
    victim: data.victim ?? 'your settlement',
    tick: data.tick ?? 0,
  };
};

/** Subscribe to incoming notification taps. Returns a cleanup function.
 *  No-op when expo-notifications isn't available. */
export const subscribeToRaidResponses = (
  handler: (meta: { victim: string; tick: number }) => void,
): (() => void) => {
  if (!Notifications) return () => {};

  // Cold-start: app launched by tapping the notification
  Notifications.getLastNotificationResponseAsync().then((response) => {
    if (!response) return;
    const meta = parseRaidResponse(response);
    if (meta) handler(meta);
  });

  // Warm-start: app already running when notification tapped
  const sub = Notifications.addNotificationResponseReceivedListener((response) => {
    const meta = parseRaidResponse(response);
    if (meta) handler(meta);
  });

  return () => sub.remove();
};
