#!/usr/bin/env bash
# Verify build and install script for composeApp

set -euo pipefail

ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)
APP_APK="$ROOT_DIR/composeApp/build/outputs/apk/debug/composeApp-debug.apk"

echo "Cleaning and assembling composeApp debug APK..."
./gradlew :composeApp:clean :composeApp:assembleDebug

if [ ! -f "$APP_APK" ]; then
  echo "APK not found at $APP_APK"
  exit 2
fi

echo "Installing APK on first connected device/emulator..."
adb devices
adb install -r "$APP_APK"

echo "Clearing logcat and starting MainActivity..."
adb logcat -c
adb shell am start -n com.lpstudio.bolaodagalera/.MainActivity || true

echo "Streaming logcat (Ctrl+C to stop). To limit output run: adb logcat --pid=
$(adb shell pidof -s com.lpstudio.bolaodagalera) | sed -n '1,200p'"
adb logcat
