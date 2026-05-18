import { expect, test } from '@playwright/test';
import { formatChainEvent, type ChainEventForTicker } from './EventTicker';

const event = (input: ChainEventForTicker): ChainEventForTicker => input;

test.describe('EventTicker chain event formatters', () => {
  test('formats WorldPaused as a global event', () => {
    const entry = formatChainEvent(event({
      eventName: 'WorldPaused',
      args: { tick: 12 },
    }));

    expect(entry).not.toBeNull();
    expect(entry?.text).toBe('World paused at tick 12');
    expect(entry?.text).not.toContain('Clan');
  });

  test('formats WorldUnpaused as a global event', () => {
    const entry = formatChainEvent(event({
      eventName: 'WorldUnpaused',
      args: {},
      tick: 13,
    }));

    expect(entry).not.toBeNull();
    expect(entry?.text).toBe('World unpaused at tick 13');
    expect(entry?.text).not.toContain('Clan');
  });

  test('formats ClansmanRevived with clan name and clansman id', () => {
    const entry = formatChainEvent(event({
      eventName: 'ClansmanRevived',
      args: {},
      clanId: 2,
      clansmanId: 7,
    }));

    expect(entry).not.toBeNull();
    expect(entry?.text).toBe('Clan Iron Guard revived clansman #7');
  });
});
