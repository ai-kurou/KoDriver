# data

`:core:domain` および各シミュレーターのドメインモジュール（`:core:lmu-windows-domain` / `:core:ace-windows-domain` / `:core:gt7-ps5-domain`）で定義された Repository インターフェースの実装を提供するモジュール。JVM（デスクトップ）/ Android の両ターゲットを持つ `kotlinMultiplatform` モジュールで、プラットフォーム固有の実装は `androidMain` / `jvmMain` に、共通実装は `jvmAndroidMain` に置く。

自動読み上げ詳細設定のファイル名にはシミュレーター接頭辞の直後に `auto_` を置き、共通の一覧設定は Desktop では `auto_readout_preferences.pb`、Android では `auto_readout_preferences.preferences_pb` に保存する。導入時はユーザー了承のもと設定を意図的に初期化し、旧ファイルの移行は行わない。開始音・ホットキーなどの共通設定ファイル名は維持する。

`CoreAutoReadoutSettingsResetter` は自動読み上げの有効状態・順序、キュー、開始音の有効状態を端末ローカルで既定値に戻します。開始音種別・音量・手動読み上げなどの設定は対象外で、Desktop / Android の Koin モジュールに `AutoReadoutSettingsResetter` として登録します。

## Koin モジュール

Android は `androidDataModule(context)`、Desktop は `desktopDataModule(directory)` が共通の Repository を登録する。
GT7 PS5 の DataStore 設定は `:core:gt7-ps5-data` の `gt7Ps5DataModule` で登録する。共通のカード順序・手動読み上げセットは本モジュールの DataModule が登録する。ACE Windows の設定と Android WebSocket 実装は `:core:ace-windows-data` の `desktopAceWindowsPreferencesDataModule(directory)`・`androidAceWindowsDataModule(context)` で登録する。LMU Windows の設定と Android WebSocket
実装は `:core:lmu-windows-data` の `desktopLmuWindowsPreferencesDataModule(directory)`・
`androidLmuWindowsDataModule(context)` で登録する。WebSocket 用 `HttpClient` は Android の共通モジュールで生成し、LMU・ACE の各モジュールの Android WebSocket リポジトリが Koin の `get()` で解決して共有する。

## サブパッケージの責務

- `preferences`: Jetpack DataStore（Proto DataStore）によるユーザー設定の永続化。共通設定項目ごとに `XxxPreferences`（proto定義）・`XxxPreferencesSerializer`・`XxxPreferencesDataStoreFactory`・`XxxPreferencesRepositoryImpl`・`XxxPreferencesRepositoryFactory` の5点セットを増やしていくパターンを取る。新しい設定項目を追加する際は、既存の類似設定（同じシミュレーター向けのもの）をコピーして書くのが最も安全。
- `websocket`: `:server` が配信する WebSocket エンドポイント（`/ws/<Simulator.id>/<feature>`）に接続し、`Flow` として購読するクライアント側実装。`WebSocketHttpClientFactory` が LMU・ACE の Android WebSocket が共有する `HttpClient` を生成し、各シミュレーターの `WebSocketXxxRepository` がエンドポイントごとのデシリアライズを担当する。LMU Windows の WebSocket 実装は `:core:lmu-windows-data` に配置し、`/ws/lmu_windows/brake_wear` から REST API 由来のブレーキ残り厚さを受信する。JSON `null` もそのまま流す。
- `telemetrylog`: Room（`TelemetryLogDatabase` / `TelemetryLogDao` / `TelemetryLogEntity`）によるテレメトリログの永続化。DB バージョンは 6。5→6 の移行で読み上げの起点 `narrationSource`（`auto` / `manual`）を追加し、既存行は `auto` として保持する。4→5 の移行で `readoutItemKey` の `lmu_windows_` / `ace_windows_` / `gt7_ps5_` 直後に `auto_` を追加し、既存ログを保持する。変更済みのキーは維持する。
- `device`: 端末固有機能（ローカルネットワークアクセス許可の確認、触覚フィードバック対応可否）へのアクセス。
- `release`: サーバー・アプリのバージョン情報の取得。`ServerVersionRepository`（`:server` のバージョン確認、Android 版アプリのみ登録。`:server` は Windows デスクトップアプリと同一プロセスで動作するため）は Android では `HttpServerVersionRepository` で `:server` へ HTTP アクセスして取得し、`AppUpdateRepository`（アプリ自体の最新リリース確認）は両プラットフォームで `GitHubAppReleaseRepository` により GitHub Releases API から取得する。
- `feedback`: Sentry User Feedback API へのフィードバック送信（`SentryFeedbackSenderRepository`）。

## オーバーレイ文字サイズのプレビュー

`OverlayTextSizePreferencesRepositoryImpl` は保存値のFlowとメモリ上の nullable な
`MutableStateFlow` を `combine` し、プレビュー値を優先して配信する。プレビューはDataStoreへ書き込まない。
`null` で解除すると最新の保存値へ戻る。Android／DesktopのDataModuleはRepositoryを `single` 登録し、
設定ダイアログとオーバーレイが同じ一時値を共有する。

## オーバーレイ背景透明度のプレビュー

`OverlayBackgroundOpacityPreferencesRepositoryImpl` も文字サイズと同様に、保存値のFlowとメモリ上の
nullable な `MutableStateFlow<Int?>` を `combine` し、プレビュー値を優先して配信する。
プレビューはDataStoreへ書き込まず、`null` で解除すると最新の保存値へ戻る。
Android／DesktopのDataModuleはRepositoryを `single` 登録し、設定ペインとオーバーレイで一時値を共有する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-data.svg)
<!-- MODULE-GRAPH-END -->

GT7・ACE・LMU の自由文言は各データモジュールに `MyBestLapPreferences` の定義を複製し、元のフィールド番号と保存形式を維持する。GT7 は番号1 `voiceType` と番号2 `readoutText`、LMU は番号1と3、ACE は番号1と4を使用する。

LMUピットタイミング設定は `:core:lmu-windows-data` に配置する。

LMUタイヤ温度設定は `:core:lmu-windows-data` に配置する。

オーバーレイ文字色は `OverlayTextColorPreferences` の ARGB Long として `overlay_text_color_preferences.pb` に永続化する。
詳細ダイアログのOKで保存し、一覧とオーバーレイが保存値のFlowを購読する。
既定値はドメインの `OVERLAY_TEXT_COLOR_DEFAULT` を参照し、既存の保存形式は維持する。

## デバッグ読み上げ

`jvmAndroidMain` の `InMemoryDebugReadoutRequestRepository` は replay なしの `MutableSharedFlow` で要求をプロセス内配信する。購読前の要求は保持せず、送信中の要求は現在の全購読者に渡す。DesktopDataModule と AndroidDataModule で同じ実装を single 登録する。永続化やテレメトリソースへの送信は行わない。

LMU のデバッグ専用の現在値読み上げホットキーは `ReadoutHotkeyPreferencesRepository` が
`lmu_windows_readout_hotkey_preferences.pb` に仮想キーコードを保存する。既定値は domain の F1 定数を参照する。
Desktop・Android 両方で設定 Repository を登録し、Android の `HotkeyRepository` は `Unsupported` 状態のみを提供する。

## 手動読み上げセット

固定表示番号を protobuf フィールド7（`number`、既定値0）に保存する。追加時に未採番（0）なら、`updateData` 内で保存済みセット数と保存済み番号の最大値の大きい方+1（空なら1）を採番し、未採番の既存セットの位置番号と重ならないようにする。指定済みの番号は保持し、削除後も他セットを詰め直さない。既存データの移行は行わず、0は表示側で一覧上の位置にフォールバックする。

`ManualReadoutSetPreferencesRepository` は手動読み上げセットを
`manual_readout_set_preferences_${simulator.id}.pb` にシミュレーターごとに保存する。
Desktop／Android とも `named(simulator.id)` で登録し、セット識別子・項目キーの順序・項目キーごとの文言テンプレート・ホットキーの仮想キーコード・セット名（protobuf フィールド6、未設定は空文字）を保持する。
文言は `ManualReadoutSetPreference` の protobuf フィールド3に `Map<String, String>` として保存する。
旧データの文言フィールドがないセットは空 Map として読み込み、Narrator と詳細画面で既定文言を使う。

修飾キーは protobuf フィールド5に nullable な `Int` として保存する。読み込み時に `ManualReadoutHotkeyModifiers` へ変換し、フィールドのない旧データ・null・不明値（削除済みの保存値7を含む）は Ctrl + Alt を使う。

ホットキーは protobuf フィールド4に nullable な `Int` として保存する。null は割り当てなしを表し、フィールド4のない旧データも null として読み込む。

## 手動読み上げ依頼

`InMemoryManualReadoutRequestRepository` は `jvmAndroidMain` の共通実装で、Android / Desktop の
DataModule にシングルトンとして登録する。`MutableSharedFlow` の追加バッファは64件で、
満杯なら `emit` が待機し、購読中の Narrator へ連続依頼を順番通り届ける。
依頼は永続化せず、購読開始前・終了後の依頼を再配信しない。

Android の `ManualReadoutHotkeyRepository` は `UnsupportedManualReadoutHotkeyRepository` を提供し、押下は通知せず、登録状態は `Unsupported`、再試行は何もしない。

音量低下警告の有効状態は `LowVolumeWarningEnabledPreferencesRepository` が
`low_volume_warning_enabled_preferences.pb` に保存し、既定値は domain の
`LOW_VOLUME_WARNING_ENABLED_DEFAULT` を参照する。Android・Desktop の DataModule に登録する。


手動読み上げ一覧の表示設定は `ManualReadoutPlayButtonLayoutEnabledPreferencesRepository` が `manual_readout_play_button_layout_preferences.pb` に全シミュレーター共通で保存し、既定値は domain の `MANUAL_READOUT_PLAY_BUTTON_LAYOUT_ENABLED_DEFAULT`（false）を参照する。Android・Desktop の DataModule に登録する。
