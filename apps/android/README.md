# ClanWorld GOLD Android

Native Android shell for the GOLD token page plus a home-screen price widget.

## Build

```bash
export ANDROID_HOME=/path/to/android-sdk
export ANDROID_SDK_ROOT="$ANDROID_HOME"
./gradlew assembleDebug
```

Optional build-time overrides:

```bash
CLANWORLD_CONVEX_URL=https://valuable-kudu-985.convex.cloud
CLANWORLD_GOLD_TOKEN_URL=https://kickstart.easya.io/token/4kWysUHVqtFmxrvwPUxA66exm2iJBMkvD4EBRrNmcieL
```

## Install

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Open the app once to verify the WebView, then long-press the Android home screen and add the GOLD widget.
