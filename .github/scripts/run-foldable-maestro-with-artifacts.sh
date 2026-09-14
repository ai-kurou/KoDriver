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

# adb emu posture はエミュレータのセンサー層のみを制御し、androidx.window の
# FoldingFeature には反映されない場合があるため、DeviceStateManager を直接
# 操作する adb shell cmd device_state state を使う。状態IDはAVDの
# device_state 設定に依存するため、print-states の出力から動的に取得する。
device_states="$(adb shell cmd device_state print-states)"
echo "=== device_state print-states ==="
echo "$device_states"

opened_state_id="$(echo "$device_states" | grep -E "name='OPENED'" | grep -oE 'identifier=[0-9]+' | head -1 | cut -d= -f2)"
half_opened_state_id="$(echo "$device_states" | grep -E "name='HALF_OPENED'" | grep -oE 'identifier=[0-9]+' | head -1 | cut -d= -f2)"

if [ -z "$opened_state_id" ] || [ -z "$half_opened_state_id" ]; then
  echo "OPENED/HALF_OPENED の device state を print-states から特定できませんでした" >&2
  kill "$monitor_pid" || true
  wait "$monitor_pid" 2>/dev/null || true
  exit 1
fi

set +e

# 縦ヒンジが完全に平らに開いた状態（OPENED）。
# list/detail の2ペイン表示を維持できる姿勢で detailPane を開き、表示されることを確認する。
adb shell cmd device_state state "$opened_state_id"
sleep "$posture_settle_seconds"
maestro test .maestro/foldable-open-detail.yaml
maestro_exit_code=$?

if [ "$maestro_exit_code" -eq 0 ]; then
  # 縦ヒンジが完全には平らに開いていない状態（HALF_OPENED）。
  # shouldCollapseDetailPane が true になり、detailPane の選択が自動的に解除されることを確認する。
  adb shell cmd device_state state "$half_opened_state_id"
  sleep "$posture_settle_seconds"
  maestro test .maestro/foldable-detail-collapsed.yaml
  maestro_exit_code=$?
fi

adb shell cmd device_state state reset

set -e

kill "$monitor_pid" || true
wait "$monitor_pid" 2>/dev/null || true

cp -R "$HOME/.maestro/tests" "$artifacts_dir/maestro-tests" || true
adb logcat -d > "$artifacts_dir/logcat.txt" || true
adb shell pidof kurou.kodriver > "$artifacts_dir/pidof-kodriver.txt" || true
adb shell dumpsys window > "$artifacts_dir/dumpsys-window.txt" || true
adb exec-out screencap -p > "$artifacts_dir/final-screen.png" || true

exit "$maestro_exit_code"
