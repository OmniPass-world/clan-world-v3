import React from 'react';
import { Text, View } from 'react-native';
import Svg, { Polygon } from 'react-native-svg';
import { ARCHETYPE_GLYPHS, ArchetypeKey } from '../data';
import { colors, fonts } from '../theme';

type Props = {
  kind: ArchetypeKey;
  size?: number;
  framed?: boolean;
};

export const ArchetypeGlyph = ({ kind, size = 56, framed = true }: Props) => {
  const a = ARCHETYPE_GLYPHS[kind] ?? ARCHETYPE_GLYPHS['patient-builder'];
  return (
    <View
      style={{
        width: size,
        height: size,
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      {framed && (
        <Svg
          viewBox="0 0 100 100"
          style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0 }}
          width={size}
          height={size}
        >
          <Polygon
            points="50,4 96,50 50,96 4,50"
            fill="none"
            stroke={colors.goldDeep}
            strokeWidth="1.5"
            opacity={0.7}
          />
          <Polygon
            points="50,12 88,50 50,88 12,50"
            fill="none"
            stroke={colors.goldPrimary}
            strokeWidth="0.8"
            opacity={0.5}
          />
        </Svg>
      )}
      <Text
        style={{
          fontFamily: fonts.displayBold,
          fontSize: size * 0.42,
          color: a.color,
          textShadowColor: a.color,
          textShadowOffset: { width: 0, height: 0 },
          textShadowRadius: 8,
        }}
      >
        {a.mark}
      </Text>
    </View>
  );
};
