// Background widget task handler. Android invokes this whenever the
// widget needs to render — on add, on the periodic update tick (every
// ~30 min by default), and on user click. We resolve the current
// widget state from MMKV and ask the library to render with it.
//
// This file lives in JS land but runs in a HeadlessJS task — it does
// NOT have access to the foreground app's React state. All state must
// be read from disk-persistent storage (MMKV).

import React from 'react';
import type { WidgetTaskHandlerProps } from 'react-native-android-widget';
import { HeroWidget } from './HeroWidget';
import { computeWidgetState } from './widgetState';

const widgetByName = {
  Hero: HeroWidget,
};

export async function widgetTaskHandler(
  props: WidgetTaskHandlerProps,
): Promise<void> {
  const widgetInfo = props.widgetInfo;
  const Widget = widgetByName[widgetInfo.widgetName as keyof typeof widgetByName];
  if (!Widget) return;

  switch (props.widgetAction) {
    case 'WIDGET_ADDED':
    case 'WIDGET_UPDATE':
    case 'WIDGET_RESIZED':
    case 'WIDGET_CLICK': {
      const state = computeWidgetState();
      props.renderWidget(<Widget state={state} />);
      break;
    }
    case 'WIDGET_DELETED':
    default:
      break;
  }
}
