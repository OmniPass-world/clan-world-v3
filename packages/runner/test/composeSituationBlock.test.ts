import { describe, it, expect } from 'vitest';
import { composeSituationBlock } from '../src/composeSituationBlock';

describe('composeSituationBlock', () => {
  it('returns only the short tick marker for ordinary ticks', () => {
    const block = composeSituationBlock({ elder: 1, clanId: '1', tick: 8 });

    expect(block).toBe('TICK 8 Started');
  });

  it('warns on tick 9 that message history is about to be erased', () => {
    const block = composeSituationBlock({ elder: 2, clanId: '2', tick: 9 });

    expect(block).toBe(
      [
        'TICK 9 Started',
        'warning: message history is about to be erased. Save important continuity with `elder memory save`.',
      ].join('\n'),
    );
  });

  it('warns on tick 10 to save continuity and ack-clear', () => {
    const block = composeSituationBlock({ elder: 3, clanId: '3', tick: 10 });

    expect(block).toBe(
      [
        'TICK 10 Started',
        'warning: final tick before message history is erased. Save important continuity with `elder memory save`, then call `elder ack-clear` when done.',
      ].join('\n'),
    );
  });

  it('repeats the warning cycle every 10 ticks', () => {
    expect(composeSituationBlock({ elder: 4, clanId: '4', tick: 19 })).toContain(
      'message history is about to be erased',
    );
    expect(composeSituationBlock({ elder: 4, clanId: '4', tick: 20 })).toContain(
      'elder ack-clear',
    );
  });
});
