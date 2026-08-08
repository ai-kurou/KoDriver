#!/usr/bin/env bash
# Android版の手動UI確認用に、テレメトリログを3件（LMU/GT7/ACE 各1件）
# 実機/エミュレータのアプリ内部DB（telemetry_logs.db）へINSERTするスクリプト。
#
# 端末上に sqlite3 バイナリが存在しない場合が多いため、
# DBファイルを一旦ホストPCへ pull し、ローカルの sqlite3 でINSERTしてから push し直す。
#
# 使い方: アプリを終了した状態で ./insert-test-telemetry-logs-android.sh [端末シリアル番号] を実行し、
# その後アプリを起動してログタブを確認する。
# 端末が複数接続されている場合はシリアル番号の指定が必須（adb devices で確認可能）。
# デバッグビルド（run-as が使えるもの）がインストール済みである必要がある。
set -euo pipefail

APPLICATION_ID="kurou.kodriver"
DB_DIR="/data/data/${APPLICATION_ID}/databases"
DB_NAME="telemetry_logs.db"
SERIAL="${1:-}"

if ! command -v adb >/dev/null 2>&1; then
    echo "adb コマンドが見つかりません。Android SDK Platform Tools をインストールしてから再実行してください。" >&2
    exit 1
fi

if ! command -v sqlite3 >/dev/null 2>&1; then
    echo "sqlite3 コマンドが見つかりません。インストールしてから再実行してください。" >&2
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

work_dir=$(mktemp -d)
trap 'rm -rf "${work_dir}"' EXIT

local_db="${work_dir}/${DB_NAME}"

# run-as 配下のファイルは adb pull で直接読めないため、一旦 /data/local/tmp 経由でコピーする。
"${adb_cmd[@]}" shell run-as "${APPLICATION_ID}" cat "${DB_DIR}/${DB_NAME}" > "${local_db}"
"${adb_cmd[@]}" shell run-as "${APPLICATION_ID}" cat "${DB_DIR}/${DB_NAME}-wal" > "${local_db}-wal" 2>/dev/null || true
"${adb_cmd[@]}" shell run-as "${APPLICATION_ID}" cat "${DB_DIR}/${DB_NAME}-shm" > "${local_db}-shm" 2>/dev/null || true

if [ ! -s "${local_db}" ]; then
    echo "DBファイルの取得に失敗しました。アプリを一度起動してDBを作成してから再実行してください。" >&2
    exit 1
fi

now_ms=$(($(date +%s) * 1000))

sqlite3 "${local_db}" <<SQL >/dev/null
INSERT INTO telemetry_logs (createdAt, simulatorId, readoutItemKey, telemetryJson)
VALUES
    (${now_ms}, 'lmu_windows', 'lmu_windows_vehicle_approach', '{"testData":true,"simulator":"lmu_windows","distanceMeters":12.5}'),
    (${now_ms} + 1, 'gt7_ps5', 'gt7_ps5_remaining_fuel', '{"testData":true,"simulator":"gt7_ps5","remainingFuelPercent":15.0}'),
    (${now_ms} + 2, 'ace_windows', 'ace_windows_remaining_fuel', '{"testData":true,"simulator":"ace_windows","remainingFuelLiters":8.2}');
PRAGMA wal_checkpoint(TRUNCATE);
SQL

# push は run-as 配下へ直接書き込めないため、一旦 /data/local/tmp へ push してから run-as 経由でコピーする。
remote_tmp="/data/local/tmp/${DB_NAME}.tmp"
"${adb_cmd[@]}" push "${local_db}" "${remote_tmp}" >/dev/null
"${adb_cmd[@]}" shell run-as "${APPLICATION_ID}" cp "${remote_tmp}" "${DB_DIR}/${DB_NAME}"
"${adb_cmd[@]}" shell run-as "${APPLICATION_ID}" rm -f "${DB_DIR}/${DB_NAME}-wal" "${DB_DIR}/${DB_NAME}-shm"
"${adb_cmd[@]}" shell rm -f "${remote_tmp}"

echo "テスト用のテレメトリログを3件挿入しました: ${DB_DIR}/${DB_NAME}"
