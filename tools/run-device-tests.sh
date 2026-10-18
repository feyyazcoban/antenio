#!/usr/bin/env bash
# Runs instrumentation test classes on the dedicated test emulator only (WORKFLOW: emulator-5558, AVD nextv_test_api36).
# Never the user's emulator-5554, the design emulator-5556 (real source) or a television: the serial and the AVD name are
# checked before anything is installed. Gradle's installDebug/connectedAndroidTest are not used because they install on
# every connected device.
#
#   tools/run-device-tests.sh GuideFlowTest,SearchNavigationTest   # short or fully qualified class names
#   tools/run-device-tests.sh GuideFlowTest#emptyGuideHasReachableNavigationAndNoUnattachedFocusTarget
#   tools/run-device-tests.sh --no-build PilotFlowTest            # reuse the APKs already built
#
# Build first (or let the script do it): ./gradlew :app:assembleDebug :app:assembleDebugAndroidTest
set -euo pipefail

SERIAL=emulator-5558
EXPECTED_AVD=nextv_test_api36
PACKAGE=dev.iptvplayer.tv
RUNNER=dev.iptvplayer.tv.pilot.test/androidx.test.runner.AndroidJUnitRunner
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APP="$ROOT/apps/android-tv"

build=1
if [ "${1:-}" = "--no-build" ]; then build=0; shift; fi
[ $# -ge 1 ] || { echo "usage: $0 [--no-build] Class[,Class#method,...]" >&2; exit 64; }

name="$(adb -s "$SERIAL" emu avd name 2>/dev/null | head -1 | tr -d '\r' || true)"
if [ "$name" != "$EXPECTED_AVD" ]; then
    echo "Refusing to run: $SERIAL is '${name:-not connected}', not $EXPECTED_AVD." >&2
    echo "Start it with: emulator -avd $EXPECTED_AVD -port 5558 -snapshot clean -no-snapshot-save -no-window -no-audio -gpu swiftshader_indirect" >&2
    exit 2
fi
[ "$(adb -s "$SERIAL" shell getprop sys.boot_completed | tr -d '\r')" = "1" ] || { echo "$SERIAL has not finished booting." >&2; exit 3; }

if [ $build = 1 ]; then
    # Android Studio's JDK on the Windows development machine when none is configured.
    if [ -z "${JAVA_HOME:-}" ] && [ -d "/c/Program Files/Android/Android Studio/jbr" ]; then export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"; fi
    (cd "$APP" && ./gradlew --console=plain -q :app:assembleDebug :app:assembleDebugAndroidTest)
fi
adb -s "$SERIAL" install -r -t "$APP/app/build/outputs/apk/debug/app-debug.apk" >/dev/null
adb -s "$SERIAL" install -r -t "$APP/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk" >/dev/null

classes=""
IFS=',' read -ra items <<< "$1"
for item in "${items[@]}"; do
    case "$item" in *.*) full="$item" ;; *) full="$PACKAGE.$item" ;; esac
    classes="${classes:+$classes,}$full"
done

log="$(mktemp)"
adb -s "$SERIAL" shell am instrument -w -r -e class "$classes" "$RUNNER" > "$log" 2>&1 || true
# Every failing test with the first lines of its stack, then the totals.
awk '/^INSTRUMENTATION_STATUS: class=/{c=substr($0,31)} /^INSTRUMENTATION_STATUS: test=/{t=substr($0,30)}
     /^INSTRUMENTATION_STATUS: stack=/{s=substr($0,31); getline n1; getline n2; st=s " | " n1 " | " n2}
     /^INSTRUMENTATION_STATUS_CODE: -2/{print "FAILED " c "#" t "\n  " st}' "$log"
grep -E "^(OK \(|Tests run:|FAILURES|INSTRUMENTATION_FAILED|INSTRUMENTATION_CODE)" "$log" || true
echo "Full log: $log"
grep -q "^OK (" "$log"
