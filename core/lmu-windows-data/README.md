# lmu-windows-data

Le Mans Ultimate 向けのデータ実装を提供する Kotlin Multiplatform モジュール（JVM + Android）です。
`jvmMain` は Windows 共有メモリ読み取りと LMU 固有の構造体パース、`jvmAndroidMain` は DataStore による設定永続化、`androidMain` は KoDriver サーバーの WebSocket クライアントを担当します。共有メモリI/Oの汎用部分は `:core:windows-shared-memory` にあります（JVM source set のみで利用）。

共有メモリに含まれない情報（天候予報、Virtual Energy消費履歴、ピットメニューの状態等）をLMU内蔵のローカルREST APIから補えないかを調査した資料が [`docs/lmu-windows-rest-api.md`](../../docs/lmu-windows-rest-api.md) にあります（未統合の調査メモ）。

## DataStore helper の複製

`:core:data` との依存は moduleGraphAssert により data 系モジュール間で禁止されているため、LMU が使う共通 DataStore helper・WebSocket helper を `internal` のままこのモジュールに複製しています。proto の `FlagReadoutTextPreferences` と `MyBestLapPreferences` もフィールド番号・既定値を維持して複製し、ACE は `:core:ace-windows-data`、GT7 は `:core:data` 側の定義を引き続き利用します。LMU 固有コードは `kurou.kodriver.core.lmuwindowsdata.preferences` / `.websocket` パッケージに置き、同一クラス名の jar 間衝突を避けます。

## 車両クラス（mVehicleClass）の取りうる値

Scoring セグメントの `mVehicleClass` (char[32]、オフセット +200) は人間可読なクラス名を返す。
2026年8月時点で実機から確認できた値は以下の通り（今後のアップデートで追加・変更される可能性がある）。

- `GT3`
- `GTE`
- `LMP3`
- `LMP2`
- `LMP2_ELMS`
- `Hyper`

参照: `LmuWindowsMapper.readVehicleClassName`, `LmuWindowsVehicleClassData`

## TimingData のラップタイム

`LmuWindowsMapper` は Scoring セグメントのプレイヤー車両からラップタイム系フィールド（`currentLapTimeMs`, `lastLapTimeMs`, `bestLapTimeMs`, `sector1Ms`, `sector1And2Ms`）を取得する。Scoring のプレイヤー車両が見つからない場合は `0L` にフォールバックする。

## 共有メモリのオフセット情報

`LmuWindowsMapper.kt` のコメントに pyLMUSharedMemory の ctypes レイアウト（`_pack_=4`）を記載済み。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-lmu-windows-data.svg)
<!-- MODULE-GRAPH-END -->
