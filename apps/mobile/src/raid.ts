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

import * as Notifications from 'expo-notifications';
import { MMKV } from 'react-native-mmkv';

const raidStore = new MMKV({ id: 'clan-world-raid' });

const KEYS = {
  pendingRaidId: 'cw.raid.pendingId',
  pendingRaidFiredAt: 'cw.raid.firedAt',
} as const;

export const RAID_NOTIFICATION_DATA_TYPE = 'cw.raid.alert';

const ANDROID_CHANNEL_ID = 'cw-raid-alerts';

/** Configure the foreground notification handler so a raid notification
 *  fired while the app is open does NOT show a system banner — we render
 *  the themed overlay instead. */
export const installRaidNotificationHandler = (): void => {
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
 *  app start before scheduling. */
export const ensureRaidChannel = async (): Promise<void> => {
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
  const { status: existing } = await Notifications.getPermissionsAsync();
  if (existing === 'granted') return true;
  const { status } = await Notifications.requestPermissionsAsync();
  return status === 'granted';
};

/**
 * Schedule a demo raid alert to fire `delaySec` seconds from now.
 * Returns the notification ID (caller can pass it to cancelRaid).
 */
export const scheduleRaidAlert = async (
  delaySec: number,
  victimName: string,
  tick: number,
): Promise<string> => {
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

/** When a notification response is received (user tapped a notif), check
 *  if it was a raid alert. Returns metadata if so, else null. */
export const parseRaidResponse = (
  response: Notifications.NotificationResponse,
): { victim: string; tick: number } | null => {
  const data = response.notification.request.content.data as
    | { type?: string; victim?: string; tick?: number }
    | null;
  if (data?.type !== RAID_NOTIFICATION_DATA_TYPE) return null;
  return {
    victim: data.victim ?? 'your settlement',
    tick: data.tick ?? 0,
  };
};
