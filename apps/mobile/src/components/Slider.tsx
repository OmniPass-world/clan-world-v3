import React, { useRef } from 'react';
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
  // We measure track origin + width on layout. Touch position is reported
  // as pageX (absolute screen coords); subtract the track's pageX origin to
  // get the relative position.
  //
  // The previous implementation used PanResponder's `locationX`, which on
  // some Android devices reports 0 on touch-start (bug surfaces as the
  // thumb snapping to the far right because we'd then divide by a stale
  // width=1 fallback).
  const trackRef = useRef<View>(null);
  const layoutRef = useRef<{ x: number; width: number } | null>(null);
  const valueRef = useRef(value);
  valueRef.current = value;

  const ticks = max - min + 1;
  const pct = ((value - min) / (max - min)) * 100;

  const setFromPageX = (pageX: number) => {
    const layout = layoutRef.current;
    if (!layout || layout.width <= 0) return;
    const relativeX = pageX - layout.x;
    const p = Math.max(0, Math.min(1, relativeX / layout.width));
    const v = Math.round(min + p * (max - min));
    if (v !== valueRef.current) onChange?.(v);
  };

  const measure = () => {
    trackRef.current?.measure((_x, _y, width, _height, pageX) => {
      layoutRef.current = { x: pageX, width };
    });
  };

  const onLayout = (_e: LayoutChangeEvent) => {
    // Defer to next tick so pageX is accurate after layout settles
    setTimeout(measure, 0);
  };

  const responder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onMoveShouldSetPanResponder: () => true,
      onPanResponderGrant: (evt) => {
        measure();
        const pageX = evt.nativeEvent.pageX;
        setTimeout(() => setFromPageX(pageX), 0);
      },
      onPanResponderMove: (evt) => {
        setFromPageX(evt.nativeEvent.pageX);
      },
    })
  ).current;

  const trackColor = variant === 'parchment' ? 'rgba(26,20,16,0.3)' : colors.goldDeep;
  const thumbBg = variant === 'parchment' ? colors.inkParchment : colors.goldBright;
  const thumbHalo = variant === 'parchment' ? colors.bgParchmentDim : colors.bgCanvas;

  return (
    <View
      ref={trackRef}
      style={{ height: 26, justifyContent: 'center' }}
      {...responder.panHandlers}
      onLayout={onLayout}
    >
      <View
        pointerEvents="none"
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
            pointerEvents="none"
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
        pointerEvents="none"
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
