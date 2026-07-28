#!/usr/bin/env bash
set -euo pipefail

readonly apk_path="$1"
readonly artifacts_dir="maestro-artifacts"
readonly launcher_package="com.google.android.apps.nexuslauncher"

dismiss_launcher_anr_dialog() {
  if adb shell dumpsys window windows 2>/dev/null | grep -q "Application Not Responding: $launcher_package"; then
    echo "Dismissing launcher ANR dialog for $launcher_package"
    adb shell am force-stop "$launcher_package" || true
    adb shell input keyevent BACK || true
  fi
}

monitor_launcher_anr_dialog() {
  while true; do
    dismiss_launcher_anr_dialog
    sleep 1
  done
}

adb install "$apk_path"
mkdir -p "$artifacts_dir"

dismiss_launcher_anr_dialog
monitor_launcher_anr_dialog &
monitor_pid=$!

set +e
maestro test .maestro/tap-bottom-tabs.yaml
maestro_exit_code=$?
set -e

kill "$monitor_pid" || true
wait "$monitor_pid" 2>/dev/null || true

cp -R "$HOME/.maestro/tests" "$artifacts_dir/maestro-tests" || true
adb logcat -d > "$artifacts_dir/logcat.txt" || true
adb shell pidof kurou.kodriver > "$artifacts_dir/pidof-kodriver.txt" || true
adb shell dumpsys window > "$artifacts_dir/dumpsys-window.txt" || true
adb exec-out screencap -p > "$artifacts_dir/final-screen.png" || true

exit "$maestro_exit_code"
