# アーキテクチャ

Kotlin Multiplatform + Clean Architecture のマルチモジュール構成。

このページに載っているモジュール一覧は `settings.gradle.kts` の `include(...)` と自動同期していない。モジュールを追加・削除した場合は、このページも同じ PR で更新すること（詳細は本ページ末尾の「モジュール一覧の更新漏れ防止」を参照）。

| モジュール | 役割 | モジュール図 |
|---|---|---|
| `:app:desktopApp` | JVM デスクトップアプリのエントリーポイント。Windows 共有メモリ読み取りと Ktor サーバーを同一プロセスで起動する | [図](graphs/app-desktopApp.svg) |
| `:app:androidApp` | Android アプリのエントリーポイント | [図](graphs/app-androidApp.svg) |
| `:app:androidBenchmark` | Macrobenchmark による Baseline Profile 生成（`app:androidApp` 計装テスト） | [図](graphs/app-androidBenchmark.svg) |
| `:app:webApp` | Web アプリ（Gradle ビルド設定のみ用意、独自機能は未実装） | [図](graphs/app-webApp.svg) |
| `:app:shared` | Compose Multiplatform 共通 UI・ナビゲーション。各 feature モジュールを組み合わせた画面遷移を担当する | [図](graphs/app-shared.svg) |
| `:core:domain` | ドメインモデル・リポジトリ抽象・ユースケース | [図](graphs/core-domain.svg) |
| `:core:data` | DataStore・HTTP/WebSocketクライアント・リポジトリ実装（JVM / Android） | [図](graphs/core-data.svg) |
| `:core:lmu-windows-data` | LMU Windows共有メモリ読み取り・リポジトリ実装（JVM） | [図](graphs/core-lmu-windows-data.svg) |
| `:core:gt7-ps5-data` | GT7 PS5 UDP テレメトリ読み取り・リポジトリ実装（JVM / Android） | [図](graphs/core-gt7-ps5-data.svg) |
| `:core:ace-windows-data` | Assetto Corsa EVO Windows共有メモリ読み取り・リポジトリ実装（JVM） | [図](graphs/core-ace-windows-data.svg) |
| `:core:device-volume-data` | 端末（OS）のマスター音量取得・設定のリポジトリ実装 | [図](graphs/core-device-volume-data.svg) |
| `:core:windows-startup-data` | OS起動時のKoDriver自動起動設定（Windowsレジストリ）のリポジトリ実装 | [図](graphs/core-windows-startup-data.svg) |
| `:core:windows-shared-memory` | Windows共有メモリI/Oの汎用基盤（`lmu-windows-data` / `ace-windows-data` が共通利用） | [図](graphs/core-windows-shared-memory.svg) |
| `:core:designsystem` | アプリ全体で共有する Composable コンポーネント | [図](graphs/core-designsystem.svg) |
| `:core:narrator` | WAV音声再生の共通基盤（lmu/gt7/ace の各narrator featureが共通利用） | [図](graphs/core-narrator.svg) |
| `:feature:desktop-splash` | デスクトップ起動中スプラッシュの初期化進捗管理・画面表示 | [図](graphs/feature-desktop-splash.svg) |
| `:feature:debug-state-detail` | 走行データのデバッグ表示（燃料消費・タイヤ摩耗・タイヤ温度・ピットタイミング等） | [図](graphs/feature-debug-state-detail.svg) |
| `:feature:lmu-windows-connection` | LMU との接続状態を監視し、接続中・未接続・エラーを UI に反映する | [図](graphs/feature-lmu-windows-connection.svg) |
| `:feature:main` | アプリ全体のメイン画面状態管理 | [図](graphs/feature-main.svg) |
| `:feature:server-connection` | KoDriver サーバー（Ktor）への接続状態確認を担当する | [図](graphs/feature-server-connection.svg) |
| `:feature:lmu-windows-narrator` | WAV 音声ファイルの再生とアナウンス制御を担当する | [図](graphs/feature-lmu-windows-narrator.svg) |
| `:feature:other-license-detail` | その他画面のライセンス詳細表示 | [図](graphs/feature-other-license-detail.svg) |
| `:feature:other-list` | その他画面の一覧表示・選択状態管理 | [図](graphs/feature-other-list.svg) |
| `:feature:other-readout-start-sound-detail` | その他画面の読み上げ開始音設定詳細 | [図](graphs/feature-other-readout-start-sound-detail.svg) |
| `:feature:other-server-ip-detail` | その他画面の接続先サーバー IP 設定ダイアログ | [図](graphs/feature-other-server-ip-detail.svg) |
| `:feature:other-console-ip-detail` | その他画面のゲーム機 IP 設定ダイアログ | [図](graphs/feature-other-console-ip-detail.svg) |
| `:feature:other-theme-detail` | その他画面のテーマ設定詳細 | [図](graphs/feature-other-theme-detail.svg) |
| `:feature:other-volume-detail` | その他画面の読み上げ音量設定詳細 | [図](graphs/feature-other-volume-detail.svg) |
| `:feature:other-feedback-detail` | その他画面のフィードバック送信詳細 | [図](graphs/feature-other-feedback-detail.svg) |
| `:feature:readout-list` | アナウンス項目の一覧表示・有効/無効の切り替え・優先度のドラッグ&リオーダー | [図](graphs/feature-readout-list.svg) |
| `:feature:lmu-windows-readout-flag-detail` | フラグアナウンスの詳細設定 UI | [図](graphs/feature-lmu-windows-readout-flag-detail.svg) |
| `:feature:lmu-windows-readout-my-best-lap-detail` | LMU 自己ベストラップアナウンスの詳細設定 UI | [図](graphs/feature-lmu-windows-readout-my-best-lap-detail.svg) |
| `:feature:lmu-windows-readout-vehicle-approach-detail` | 車両接近アナウンスの詳細設定 UI | [図](graphs/feature-lmu-windows-readout-vehicle-approach-detail.svg) |
| `:feature:lmu-windows-readout-vehicle-damage-detail` | 車両故障アナウンスの詳細設定 UI | [図](graphs/feature-lmu-windows-readout-vehicle-damage-detail.svg) |
| `:feature:lmu-windows-readout-tyre-temperature-detail` | タイヤ温度アナウンスの詳細設定 UI | [図](graphs/feature-lmu-windows-readout-tyre-temperature-detail.svg) |
| `:feature:lmu-windows-readout-remaining-virtual-energy-detail` | バーチャルエナジー残量アナウンスの詳細設定 UI | [図](graphs/feature-lmu-windows-readout-remaining-virtual-energy-detail.svg) |
| `:feature:lmu-windows-readout-tyre-wear-detail` | タイヤ摩耗アナウンスの詳細設定 UI | [図](graphs/feature-lmu-windows-readout-tyre-wear-detail.svg) |
| `:feature:lmu-windows-readout-pit-timing-detail` | ピットタイミングアナウンスの詳細設定 UI | [図](graphs/feature-lmu-windows-readout-pit-timing-detail.svg) |
| `:feature:gt7-ps5-connection` | GT7 PS5 との接続状態を監視し、接続中・未接続・エラーを UI に反映する | [図](graphs/feature-gt7-ps5-connection.svg) |
| `:feature:gt7-ps5-narrator` | GT7 PS5 向け WAV 音声ファイルの再生とアナウンス制御を担当する | [図](graphs/feature-gt7-ps5-narrator.svg) |
| `:feature:gt7-ps5-readout-my-best-lap-detail` | GT7 自己ベストラップアナウンスの詳細設定 UI | [図](graphs/feature-gt7-ps5-readout-my-best-lap-detail.svg) |
| `:feature:gt7-ps5-readout-remaining-fuel-detail` | GT7 燃料残量アナウンスの詳細設定 UI | [図](graphs/feature-gt7-ps5-readout-remaining-fuel-detail.svg) |
| `:feature:gt7-ps5-readout-remaining-fuel-laps-detail` | GT7 燃料残り周回数アナウンスの詳細設定 UI | [図](graphs/feature-gt7-ps5-readout-remaining-fuel-laps-detail.svg) |
| `:feature:gt7-ps5-readout-tyre-temperature-detail` | GT7 タイヤ温度アナウンスの詳細設定 UI | [図](graphs/feature-gt7-ps5-readout-tyre-temperature-detail.svg) |
| `:feature:ace-windows-connection` | ACE (Assetto Corsa EVO) との接続状態を監視し、接続中・未接続・エラーを UI に反映する | [図](graphs/feature-ace-windows-connection.svg) |
| `:feature:ace-windows-narrator` | ACE (Assetto Corsa EVO) 向け WAV 音声ファイルの再生とアナウンス制御を担当する | [図](graphs/feature-ace-windows-narrator.svg) |
| `:feature:ace-windows-readout-remaining-fuel-detail` | ACE 燃料残量アナウンスの詳細設定 UI | [図](graphs/feature-ace-windows-readout-remaining-fuel-detail.svg) |
| `:feature:ace-windows-readout-flag-detail` | ACE フラッグアナウンスの詳細設定 UI | [図](graphs/feature-ace-windows-readout-flag-detail.svg) |
| `:feature:ace-windows-readout-tyre-temperature-detail` | ACE タイヤ温度アナウンスの詳細設定 UI | [図](graphs/feature-ace-windows-readout-tyre-temperature-detail.svg) |
| `:feature:ace-windows-readout-vehicle-approach-detail` | ACE 車両接近アナウンスの詳細設定 UI | [図](graphs/feature-ace-windows-readout-vehicle-approach-detail.svg) |
| `:feature:ace-windows-readout-my-best-lap-detail` | ACE 自己ベストラップアナウンスの詳細設定 UI（タイトルと説明のみ。読み上げ判定は未配線） | [図](graphs/feature-ace-windows-readout-my-best-lap-detail.svg) |
| `:feature:telemetry-log-list` | テレメトリログの一覧表示 UI | [図](graphs/feature-telemetry-log-list.svg) |
| `:feature:telemetry-log-detail` | テレメトリログの詳細表示 UI | [図](graphs/feature-telemetry-log-detail.svg) |
| `:server` | デスクトップアプリと同一プロセスで起動する Ktor サーバー。`/ws/<Simulator.id>/<feature>` WebSocket で共有メモリ由来の走行情報を配信する | [図](graphs/server.svg) |

## モジュール一覧の更新漏れ防止

このページのモジュール一覧は手書きの表であり、`settings.gradle.kts` に `include(...)` を追加・削除しても自動で更新されない。過去に ACE (Assetto Corsa EVO) 対応一式や複数の readout detail モジュールが追加された際、このページへの反映が漏れて長期間放置されていたことがある。

モジュールを追加・削除する PR では、以下を必ず実施すること。

- `settings.gradle.kts` の `include(...)` を変更した場合、同じ PR で本ページの表も更新する（追加時は行を追加、削除時は行を削除）。
- 本ページを更新したかどうかを判断に迷う場合は、以下のコマンドで `settings.gradle.kts` の宣言数と本ページの行数を突き合わせて確認できる。

```bash
# settings.gradle.kts の include数
grep -c '^include(' settings.gradle.kts

# 本ページのモジュール行数（先頭のヘッダー・区切り行を除く）
grep -c '^| `:' docs/architecture.md
```

両者の数が一致しない場合、本ページの更新漏れの可能性が高い。
