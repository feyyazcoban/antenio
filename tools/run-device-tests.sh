#!/usr/bin/env bash
# Runs instrumentation test classes on the dedicated test emulator only (WORKFLOW: emulator-5558, AVD nextv_test_api36).
# Never the user's emulator-5554, the design emulator-5556 (real source) or a television: the serial and the AVD name are
# checked before anything is installed. Gradle's installDebug/connectedAndroidTest are not used because they install on
# every connected device.
#
#   tools/run-device-tests.sh GuideFlowTest,SearchNavigationTest   # short or fully qualified class names
#   tools/run-device-tests.sh GuideFlowTest#emptyGuideHasReachableNavigationAndNoUnattachedFocusTarget
#   tools/run-device-tests.sh --no-build PilotFlowTest            # reuse the APKs already built
#   tools/run-device-tests.sh --no-build --cold-catalog prepare   # seed only the dedicated test emulator
#   tools/run-device-tests.sh --no-build --cold-catalog verify    # fresh process, timing without screenshots
#   tools/run-device-tests.sh --no-build --cold-catalog visual    # separate frame captures; not timing comparison
#   tools/run-device-tests.sh --no-build --cold-catalog release   # shrunk release, framework-only runner
#   tools/run-device-tests.sh --no-build --cold-catalog release-visual # release captures, separate from timing
#   tools/run-device-tests.sh --no-build --cold-catalog actual-debug # same framework runner, debug comparison
#   tools/run-device-tests.sh --no-build --cold-catalog clean     # remove only the synthetic fixture
#   tools/run-device-tests.sh --font-scale 2.0 SettingsFrameworkTest  # at that text size; the emulator's comes back after
#
# Build first (or let the script do it): ./gradlew :app:assembleDebug :app:assembleDebugAndroidTest
set -euo pipefail

SERIAL=emulator-5558
EXPECTED_AVD=nextv_test_api36
PACKAGE=dev.iptvplayer.tv
RUNNER=app.antenio.tv.test/androidx.test.runner.AndroidJUnitRunner
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APP="$ROOT/apps/android-tv"

build=1
scale=""
cold=""
runner_args=()
while [ $# -gt 0 ]; do
    case "$1" in
        --no-build) build=0; shift ;;
        --cold-catalog) [ $# -ge 2 ] || { echo "--cold-catalog needs prepare, verify, visual, release[-visual], actual-debug[-visual] or clean" >&2; exit 64; }; cold="$2"; shift 2 ;;
        --font-scale) [ $# -ge 2 ] || { echo "--font-scale needs a value such as 1.5" >&2; exit 64; }; scale="$2"; shift 2 ;;
        *) break ;;
    esac
done
if [ -n "$cold" ]; then
    [ $# -eq 0 ] && [ -z "$scale" ] || { echo "Cold mode takes no class or font-scale arguments." >&2; exit 64; }
    case "$cold" in
        prepare) set -- 'CatalogColdLaunchTest#prepareEncryptedCatalogForColdLaunch'; runner_args=(-e prepareColdCatalog true) ;;
        verify|visual) set -- 'CatalogColdLaunchTest#verifyRestoredAfterProcessRestart'; runner_args=(-e verifyColdCatalog true)
            if [ "$cold" = visual ]; then runner_args+=(-e captureColdEntrance true); fi ;;
        release|release-visual|actual-debug|actual-debug-visual) set -- 'ReleaseStartupInstrumentation'; runner_args=(-e verifyReleaseCatalog true)
            RUNNER=app.antenio.tv.test/dev.iptvplayer.tv.ReleaseStartupInstrumentation
            if [ "$cold" = actual-debug ] || [ "$cold" = actual-debug-visual ]; then runner_args+=(-e expectedDebug true); fi
            if [ "$cold" = release-visual ] || [ "$cold" = actual-debug-visual ]; then runner_args+=(-e captureColdEntrance true); fi ;;
        clean) set -- 'CatalogColdLaunchTest#cleanColdCatalogFixture'; runner_args=(-e cleanColdCatalog true) ;;
        *) echo "Unknown cold mode: $cold" >&2; exit 64 ;;
    esac
fi
[ $# -ge 1 ] || { echo "usage: $0 [--no-build] [--font-scale 1.5] Class[,Class#method,...] | --cold-catalog prepare|verify|visual|release[-visual]|actual-debug[-visual]|clean" >&2; exit 64; }
case "$scale" in ""|[0-9].[0-9]|[0-9].[0-9][0-9]|[0-9]) ;; *) echo "--font-scale takes a number such as 1.5" >&2; exit 64 ;; esac

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
    tasks=(:app:assembleDebug :app:assembleDebugAndroidTest)
    if [ "$cold" = release ] || [ "$cold" = release-visual ]; then tasks+=(:app:assembleRelease); fi
    (cd "$APP" && ./gradlew --console=plain -q "${tasks[@]}")
fi
target_apk="$APP/app/build/outputs/apk/debug/app-debug.apk"
if [ "$cold" = release ] || [ "$cold" = release-visual ]; then target_apk="$APP/app/build/outputs/apk/release/app-release.apk"; fi
adb -s "$SERIAL" install -r -t "$target_apk" >/dev/null
adb -s "$SERIAL" install -r -t "$APP/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk" >/dev/null

classes=""
IFS=',' read -ra items <<< "$1"
for item in "${items[@]}"; do
    case "$item" in *.*) full="$item" ;; *) full="$PACKAGE.$item" ;; esac
    classes="${classes:+$classes,}$full"
done

if [ -n "$scale" ]; then
    # The text size of the whole emulator, as a viewer sets it; the one it had comes back however the run ends.
    before="$(adb -s "$SERIAL" shell settings get system font_scale | tr -d '\r')"
    restore() {
        if [ "$before" = "null" ] || [ -z "$before" ]; then adb -s "$SERIAL" shell settings delete system font_scale >/dev/null
        else adb -s "$SERIAL" shell settings put system font_scale "$before"; fi
    }
    trap restore EXIT
    adb -s "$SERIAL" shell settings put system font_scale "$scale"
    echo "Font scale $scale (was ${before:-unset})"
fi

if [ "$cold" = verify ] || [ "$cold" = visual ] || [ "$cold" = release ] || [ "$cold" = release-visual ] || [ "$cold" = actual-debug ] || [ "$cold" = actual-debug-visual ]; then
    adb -s "$SERIAL" shell am force-stop app.antenio.tv
    running="$(adb -s "$SERIAL" shell pidof app.antenio.tv 2>/dev/null | tr -d '\r' || true)"
    [ -z "$running" ] || { echo "Cold verifier requires a stopped target process." >&2; exit 4; }
fi
log="$(mktemp)"
adb -s "$SERIAL" shell am instrument -w -r -e class "$classes" "${runner_args[@]}" "$RUNNER" > "$log" 2>&1 || true
# Every failing test with the first lines of its stack, then the totals.
awk '/^INSTRUMENTATION_STATUS: class=/{c=substr($0,31)} /^INSTRUMENTATION_STATUS: test=/{t=substr($0,30)}
     /^INSTRUMENTATION_STATUS: stack=/{s=substr($0,31); getline n1; getline n2; st=s " | " n1 " | " n2}
     /^INSTRUMENTATION_STATUS_CODE: -2/{print "FAILED " c "#" t "\n  " st}' "$log"
grep -E "^(OK \(|Tests run:|FAILURES|INSTRUMENTATION_FAILED|INSTRUMENTATION_CODE)" "$log" || true
echo "Full log: $log"
grep -q "^OK (" "$log"
if [ -n "$cold" ]; then
    grep -q '^INSTRUMENTATION_STATUS_CODE: 0' "$log" || { echo "Cold test did not execute successfully (possibly skipped)." >&2; exit 5; }
fi
