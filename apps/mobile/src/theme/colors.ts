export const colors = {
  bgCanvas: '#0B0A0E',
  bgElevated: '#15131A',
  bgElevated2: '#1C1922',
  bgParchment: '#E8DAB1',
  bgParchmentDim: '#C7B27C',
  bgParchmentDeep: '#B59E68',
  bgTerminal: '#0F0E12',

  inkParchment: '#1A1410',
  inkParchmentMuted: '#5A4A35',
  inkParchmentFaint: '#8A7752',
  inkDark: '#E8DDC4',
  inkDarkMuted: '#9A8E72',
  inkDarkFaint: '#5C5443',
  inkMono: '#D4AF5C',

  goldPrimary: '#D4AF5C',
  goldBright: '#E8C77E',
  goldDeep: '#8C6F3A',
  goldDeeper: '#5E4920',
  aubergineDeep: '#2A1F3D',
  scriptBlue: '#6B7DB8',
  scriptBlueDark: '#3a4a7a',

  statusLive: '#6B8E5C',
  statusWarn: '#C99B4F',
  statusDanger: '#A04A3F',
  statusInfo: '#5A7BA8',
  statusLiveDeep: '#3a6a2a',
} as const;

export type AccentName = 'gold' | 'crimson' | 'verdigris';

export const accentPalettes: Record<
  AccentName,
  { primary: string; bright: string; deep: string; deeper: string }
> = {
  gold: { primary: '#D4AF5C', bright: '#E8C77E', deep: '#8C6F3A', deeper: '#5E4920' },
  crimson: { primary: '#C26A5A', bright: '#E0876F', deep: '#8B3D31', deeper: '#5A241B' },
  verdigris: { primary: '#5C9C8C', bright: '#7DBFAD', deep: '#3D6B5F', deeper: '#1F3A33' },
};
