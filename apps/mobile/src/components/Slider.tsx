import React, { useRef, useState } from 'react';
import { View, PanResponder, LayoutChangeEvent } from 'react-native';
import { colors } from '../theme';

type Props = {
  value: number;
  onChange?: (v: number) => void;
  min?: number;
  max?: number;
  variant?: 'parchment' | 'dark';
};

export const Slider = ({
  value,
  onChange,
  min = -3,
  max = 3,
  variant = 'parchment',
}: Props) => {
  const [width, setWidth] = useState(1);
  const valueRef = useRef(value);
  valueRef.current = value;

  const ticks = max - min + 1;
  const pct = ((value - min) / (max - min)) * 100;

  const setFromX = (x: number) => {
    const p = Math.max(0, Math.min(1, x / Math.max(width, 1)));
    const v = Math.round(min + p * (max - min));
    if (v !== valueRef.current) onChange?.(v);
  };

  const responder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onMoveShouldSetPanResponder: () => true,
      onPanResponderGrant: (evt) => {
        const { locationX } = evt.nativeEvent;
        setFromX(locationX);
      },
      onPanResponderMove: (evt) => {
        const { locationX } = evt.nativeEvent;
        setFromX(locationX);
      },
    })
  ).current;

  const trackColor = variant === 'parchment' ? 'rgba(26,20,16,0.3)' : colors.goldDeep;
  const thumbBg = variant === 'parchment' ? colors.inkParchment : colors.goldBright;
  const thumbHalo = variant === 'parchment' ? colors.bgParchmentDim : colors.bgCanvas;

  const onLayout = (e: LayoutChangeEvent) => setWidth(e.nativeEvent.layout.width);

  return (
    <View
      style={{ height: 26, justifyContent: 'center' }}
      {...responder.panHandlers}
      onLayout={onLayout}
    >
      <View
        style={{
          position: 'absolute',
          left: 0,
          right: 0,
          height: 2,
          backgroundColor: trackColor,
          top: '50%',
          marginTop: -1,
        }}
      />
      {variant === 'parchment' &&
        Array.from({ length: ticks }).map((_, i) => (
          <View
            key={i}
            style={{
              position: 'absolute',
              left: `${(i / (ticks - 1)) * 100}%`,
              width: 1,
              height: 6,
              backgroundColor: 'rgba(26,20,16,0.25)',
              top: '50%',
              marginTop: -3,
              marginLeft: -0.5,
            }}
          />
        ))}
      <View
        style={{
          position: 'absolute',
          left: `${pct}%`,
          marginLeft: -7,
          width: 14,
          height: 14,
          backgroundColor: thumbBg,
          transform: [{ rotate: '45deg' }],
          borderColor: thumbHalo,
          borderWidth: 2,
          top: '50%',
          marginTop: -7,
        }}
      />
    </View>
  );
};
