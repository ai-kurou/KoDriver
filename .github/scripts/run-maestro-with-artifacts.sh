#!/usr/bin/env bash
set -euo pipefail

readonly apk_path="$1"
readonly artifacts_dir="maestro-artifacts"

adb install "$apk_path"
mkdir -p "$artifacts_dir"

set +e
maestro test .maestro/tap-bottom-tabs.yaml
maestro_exit_code=$?
set -e

cp -R "$HOME/.maestro/tests" "$artifacts_dir/maestro-tests" || true
adb logcat -d > "$artifacts_dir/logcat.txt" || true
adb shell pidof kurou.kodriver > "$artifacts_dir/pidof-kodriver.txt" || true
adb shell dumpsys window > "$artifacts_dir/dumpsys-window.txt" || true
adb exec-out screencap -p > "$artifacts_dir/final-screen.png" || true

exit "$maestro_exit_code"
