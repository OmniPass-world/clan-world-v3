import { describe, expect, it } from 'vitest';
import { ActionType } from '../generated/enums';
import type { ClanOrder } from '../types';
import { validateSubmitOrderPayload } from './IChainClient';

const defendOrder = (targetClanId: number): ClanOrder => ({
  kind: 'mission',
  payload: {
    clansmanId: 1,
    gotoRegion: 1,
    action: ActionType.DefendBase,
    targetClanId,
  },
});

describe('validateSubmitOrderPayload', () => {
  it('accepts DefendBase with the self-target sentinel', () => {
    expect(() => validateSubmitOrderPayload(defendOrder(0), 3)).not.toThrow();
  });

  it('accepts DefendBase with an explicit self target', () => {
    expect(() => validateSubmitOrderPayload(defendOrder(3), 3)).not.toThrow();
  });
});
