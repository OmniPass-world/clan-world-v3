import 'react-native-get-random-values';
import 'react-native-url-polyfill/auto';
import { Buffer } from 'buffer';
global.Buffer = global.Buffer || Buffer;

import { registerRootComponent } from 'expo';

import App from './App';

registerRootComponent(App);

// Register the home-screen widget task handler. Android invokes this
// in a HeadlessJS context whenever the widget needs to (re)render —
// on add, on periodic update, on click, on resize. The handler reads
// MMKV-persisted state and renders the appropriate widget variant.
//
// Defensive: the widget library throws on old dev client APKs that
// predate it being added. Wrap the import + register so the JS bundle
// still loads cleanly until a fresh APK is installed.
try {
  const { registerWidgetTaskHandler } = require('react-native-android-widget');
  const { widgetTaskHandler } = require('./src/widget/widget-task-handler');
  registerWidgetTaskHandler(widgetTaskHandler);
} catch (e) {
  if (__DEV__) {
    console.warn(
      '[widget] task handler not registered — install latest dev client APK to enable home-screen widget.',
    );
  }
}
