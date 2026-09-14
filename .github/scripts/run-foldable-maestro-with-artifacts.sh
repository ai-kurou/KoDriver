#!/usr/bin/env bash
set -euo pipefail

readonly apk_path="$1"
readonly artifacts_dir="maestro-artifacts"
readonly launcher_package="com.google.android.apps.nexuslauncher"
readonly posture_settle_seconds=3

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

# posture 3 = opened（縦ヒンジが完全に平らに開いた状態）。
# list/detail の2ペイン表示を維持できる姿勢で detailPane を開き、表示されることを確認する。
adb emu posture 3
sleep "$posture_settle_seconds"
maestro test .maestro/foldable-open-detail.yaml
maestro_exit_code=$?

if [ "$maestro_exit_code" -eq 0 ]; then
  # posture 2 = half-opened（縦ヒンジが完全には平らに開いていない状態）。
  # shouldCollapseDetailPane が true になり、detailPane の選択が自動的に解除されることを確認する。
  adb emu posture 2
  sleep "$posture_settle_seconds"
  maestro test .maestro/foldable-detail-collapsed.yaml
  maestro_exit_code=$?
fi

set -e

kill "$monitor_pid" || true
wait "$monitor_pid" 2>/dev/null || true

cp -R "$HOME/.maestro/tests" "$artifacts_dir/maestro-tests" || true
adb logcat -d > "$artifacts_dir/logcat.txt" || true
adb shell pidof kurou.kodriver > "$artifacts_dir/pidof-kodriver.txt" || true
adb shell dumpsys window > "$artifacts_dir/dumpsys-window.txt" || true
adb exec-out screencap -p > "$artifacts_dir/final-screen.png" || true

exit "$maestro_exit_code"
