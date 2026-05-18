import { ResourceType } from '../generated/enums';

export const RESOURCE_NAMES_BY_ENUM: Record<number, string> = Object.fromEntries(
  Object.entries(ResourceType)
    .filter(([, value]) => typeof value === 'number')
    .map(([key, value]) => [value as number, key.toLowerCase()]),
) as Record<number, string>;
