#!/usr/bin/env bash
# Android版の手動UI確認用に、テレメトリログを3件（LMU/GT7/ACE 各1件）
# 実機/エミュレータのアプリ内部DB（telemetry_logs.db）へ直接INSERTするスクリプト。
#
# 使い方: ./insert-test-telemetry-logs-android.sh [端末シリアル番号]
# 端末が複数接続されている場合はシリアル番号の指定が必須（adb devices で確認可能）。
# デバッグビルド（run-as が使えるもの）がインストール済みである必要がある。
set -euo pipefail

APPLICATION_ID="kurou.kodriver"
DB_PATH="/data/data/${APPLICATION_ID}/databases/telemetry_logs.db"
SERIAL="${1:-}"

if ! command -v adb >/dev/null 2>&1; then
    echo "adb コマンドが見つかりません。Android SDK Platform Tools をインストールしてから再実行してください。" >&2
    exit 1
fi

device_count=$(adb devices | tail -n +2 | grep -c "device$" || true)

if [ -z "${SERIAL}" ]; then
    if [ "${device_count}" -eq 0 ]; then
        echo "接続されている端末がありません。'adb devices' で確認してください。" >&2
        exit 1
    elif [ "${device_count}" -gt 1 ]; then
        echo "複数の端末が接続されています。対象端末のシリアル番号を引数で指定してください。" >&2
        adb devices >&2
        exit 1
    fi
fi

adb_cmd=(adb)
if [ -n "${SERIAL}" ]; then
    adb_cmd+=(-s "${SERIAL}")
fi

now_ms=$(($(date +%s) * 1000))

"${adb_cmd[@]}" shell run-as "${APPLICATION_ID}" sqlite3 "${DB_PATH}" <<SQL
INSERT INTO telemetry_logs (createdAt, simulatorId, readoutItemKey, telemetryJson)
VALUES
    (${now_ms}, 'lmu_windows', 'lmu_windows_vehicle_approach', '{"testData":true,"simulator":"lmu_windows","distanceMeters":12.5}'),
    (${now_ms} + 1, 'gt7_ps5', 'gt7_ps5_remaining_fuel', '{"testData":true,"simulator":"gt7_ps5","remainingFuelPercent":15.0}'),
    (${now_ms} + 2, 'ace_windows', 'ace_windows_remaining_fuel', '{"testData":true,"simulator":"ace_windows","remainingFuelLiters":8.2}');
SQL

echo "テスト用のテレメトリログを3件挿入しました: ${DB_PATH}"
