import { TextStyle } from 'react-native';
import { colors } from './colors';
import { fonts } from './fonts';

// Typographic helpers translated from styles.css (.t-display, .t-script, .h1, etc.).
// React Native does not support letter-spacing the way CSS does (em units); we use
// pixel approximations that read close to the design.
export const typo = {
  display: { fontFamily: fonts.display, letterSpacing: 1.2, textTransform: 'uppercase' as const },
  displayMixed: { fontFamily: fonts.display, letterSpacing: 0.6 },
  script: { fontFamily: fonts.script, fontStyle: 'italic' as const },
  body: { fontFamily: fonts.body },
  bodyItalic: { fontFamily: fonts.bodyItalic, fontStyle: 'italic' as const },
  mono: { fontFamily: fonts.mono, letterSpacing: 0.3 },

  h1: {
    fontFamily: fonts.display,
    fontSize: 22,
    letterSpacing: 1.6,
    textTransform: 'uppercase' as const,
    color: colors.inkDark,
  } as TextStyle,
  h2: {
    fontFamily: fonts.display,
    fontSize: 16,
    letterSpacing: 1.4,
    textTransform: 'uppercase' as const,
    color: colors.inkDark,
  } as TextStyle,
  h3: {
    fontFamily: fonts.display,
    fontSize: 11,
    letterSpacing: 1.6,
    textTransform: 'uppercase' as const,
    color: colors.inkDark,
  } as TextStyle,
};
