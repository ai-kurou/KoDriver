#!/usr/bin/env bash
# デスクトップアプリの手動UI確認用に、テレメトリログを3件（LMU/GT7/ACE 各1件）
# ~/.kodriver/telemetry_logs.db に直接INSERTするスクリプト。
#
# 使い方: アプリを終了した状態で ./insert-test-telemetry-logs.sh を実行し、
# その後デスクトップアプリを起動してログタブを確認する。
set -euo pipefail

DB_PATH="${HOME}/.kodriver/telemetry_logs.db"

if ! command -v sqlite3 >/dev/null 2>&1; then
    echo "sqlite3 コマンドが見つかりません。インストールしてから再実行してください。" >&2
    exit 1
fi

if [ ! -f "${DB_PATH}" ]; then
    echo "DBファイルが見つかりません: ${DB_PATH}" >&2
    echo "先にデスクトップアプリを一度起動してDBを作成してください。" >&2
    exit 1
fi

now_ms=$(($(date +%s) * 1000))

sqlite3 "${DB_PATH}" <<SQL
INSERT INTO telemetry_logs (createdAt, simulatorId, readoutItemKey, telemetryJson)
VALUES
    (${now_ms}, 'lmu_windows', 'lmu_windows_vehicle_approach', '{"testData":true,"simulator":"lmu_windows","distanceMeters":12.5}'),
    (${now_ms} + 1, 'gt7_ps5', 'gt7_ps5_remaining_fuel', '{"testData":true,"simulator":"gt7_ps5","remainingFuelPercent":15.0}'),
    (${now_ms} + 2, 'ace_windows', 'ace_windows_remaining_fuel', '{"testData":true,"simulator":"ace_windows","remainingFuelLiters":8.2}');
SQL

echo "テスト用のテレメトリログを3件挿入しました: ${DB_PATH}"
