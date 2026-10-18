#!/usr/bin/env bash
set -euxo pipefail
api_level="$1"
gradle --no-daemon --max-workers=2 -Dorg.gradle.jvmargs=-Xmx1536m installDebug installDebugAndroidTest
adb shell pm grant de.wortmonster.jbdtrigger android.permission.ACCESS_COARSE_LOCATION
adb shell pm grant de.wortmonster.jbdtrigger android.permission.ACCESS_FINE_LOCATION
if [ "$api_level" -ge 31 ]; then
  adb shell pm grant de.wortmonster.jbdtrigger android.permission.BLUETOOTH_SCAN
  adb shell pm grant de.wortmonster.jbdtrigger android.permission.BLUETOOTH_CONNECT
fi
if [ "$api_level" -ge 33 ]; then
  adb shell pm grant de.wortmonster.jbdtrigger android.permission.POST_NOTIFICATIONS
fi
if [ "$api_level" -eq 26 ]; then
  adb root
  adb wait-for-device
  adb shell settings put global airplane_mode_on 1
  adb shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true
else
  adb shell svc wifi disable
  adb shell svc data disable
fi
if [ "$api_level" -eq 26 ]; then
  adb shell am instrument -w -e mode altitude de.wortmonster.jbdtrigger.test/de.wortmonster.jbdtrigger.CockpitSmokeTest | tee altitude-result.txt
  grep 'Offline altitude conversion.*OK' altitude-result.txt
  exit 0
fi
adb shell am force-stop com.android.launcher3
adb shell am instrument -w de.wortmonster.jbdtrigger.test/de.wortmonster.jbdtrigger.CockpitSmokeTest | tee ui-result.txt
adb pull /sdcard/Android/data/de.wortmonster.jbdtrigger/files/screenshots ui-screenshots
grep 'Cockpit launch.*OK' ui-result.txt
adb shell wm size 1080x2400
adb shell am instrument -w -e mode guide de.wortmonster.jbdtrigger.test/de.wortmonster.jbdtrigger.CockpitSmokeTest | tee guide-result.txt
adb pull /sdcard/Android/data/de.wortmonster.jbdtrigger/files/screenshots ui-guide
cp ui-guide/anleitung-*.png ui-screenshots/
grep 'Guide screenshots: OK' guide-result.txt
