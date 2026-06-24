# アーキテクチャ

Kotlin Multiplatform + Clean Architecture のマルチモジュール構成。

| モジュール | 役割 | モジュール図 |
|---|---|---|
| `:app:desktopApp` | JVM デスクトップアプリのエントリーポイント。Windows 共有メモリ読み取りと Ktor サーバーを同一プロセスで起動する | [図](graphs/app-desktopApp.svg) |
| `:app:androidApp` | Android アプリのエントリーポイント。WebSocket 経由でサーバーから LMU 由来の走行情報を受信して表示する | [図](graphs/app-androidApp.svg) |
| `:app:webApp` | Web アプリ（未実装） | [図](graphs/app-webApp.svg) |
| `:app:shared` | Compose Multiplatform 共通 UI・ナビゲーション。各 feature モジュールを組み合わせた画面遷移を担当する | [図](graphs/app-shared.svg) |
| `:core:domain` | ドメインモデル・リポジトリ抽象・ユースケース | [図](graphs/core-domain.svg) |
| `:core:data` | DataStore・HTTP/WebSocketクライアント・リポジトリ実装（JVM / Android） | [図](graphs/core-data.svg) |
| `:core:lmu-windows-data` | LMU Windows共有メモリ読み取り・リポジトリ実装（JVM） | [図](graphs/core-lmu-windows-data.svg) |
| `:core:gt7-ps5-data` | GT7 PS5 UDP テレメトリ読み取り・リポジトリ実装（JVM） | [図](graphs/core-gt7-ps5-data.svg) |
| `:core:designsystem` | アプリ全体で共有する Composable コンポーネント | [図](graphs/core-designsystem.svg) |
| `:feature:lmu-windows-connection` | LMU との接続状態を監視し、接続中・未接続・エラーを UI に反映する | [図](graphs/feature-lmu-windows-connection.svg) |
| `:feature:main` | アプリ全体のメイン画面状態管理 | [図](graphs/feature-main.svg) |
| `:feature:server-connection` | KoDriver サーバー（Ktor）への接続状態確認を担当する | [図](graphs/feature-server-connection.svg) |
| `:feature:readout-list` | アナウンス項目の一覧表示・有効/無効の切り替え・優先度のドラッグ&リオーダー | [図](graphs/feature-readout-list.svg) |
| `:feature:lmu-windows-readout-vehicle-approach-detail` | 車両接近アナウンスの詳細設定 UI | [図](graphs/feature-lmu-windows-readout-vehicle-approach-detail.svg) |
| `:feature:lmu-windows-readout-vehicle-damage-detail` | 車両故障アナウンスの詳細設定 UI | [図](graphs/feature-lmu-windows-readout-vehicle-damage-detail.svg) |
| `:feature:lmu-windows-readout-flag-detail` | フラグアナウンスの詳細設定 UI | [図](graphs/feature-lmu-windows-readout-flag-detail.svg) |
| `:feature:gt7-ps5-connection` | GT7 PS5 との接続状態を監視し、接続中・未接続・エラーを UI に反映する | [図](graphs/feature-gt7-ps5-connection.svg) |
| `:feature:gt7-ps5-readout-my-bestlap-detail` | GT7 自己ベストラップアナウンスの詳細設定 UI | [図](graphs/feature-gt7-ps5-readout-my-bestlap-detail.svg) |
| `:feature:lmu-windows-narrator` | WAV 音声ファイルの再生とアナウンス制御を担当する | [図](graphs/feature-lmu-windows-narrator.svg) |
| `:feature:gt7-ps5-narrator` | GT7 PS5 向け WAV 音声ファイルの再生とアナウンス制御を担当する | [図](graphs/feature-gt7-ps5-narrator.svg) |
| `:feature:other-license-detail` | その他画面のライセンス詳細表示 | [図](graphs/feature-other-license-detail.svg) |
| `:feature:other-list` | その他画面の一覧表示・選択状態管理 | [図](graphs/feature-other-list.svg) |
| `:feature:other-readout-start-sound-detail` | その他画面の読み上げ開始音設定詳細 | [図](graphs/feature-other-readout-start-sound-detail.svg) |
| `:feature:other-server-ip-detail` | その他画面の接続先サーバー IP 設定ダイアログ | [図](graphs/feature-other-server-ip-detail.svg) |
| `:feature:other-volume-detail` | その他画面の読み上げ音量設定詳細 | [図](graphs/feature-other-volume-detail.svg) |
| `:server` | デスクトップアプリと同一プロセスで起動する Ktor サーバー。`/ws/<Simulator.id>/<feature>` WebSocket で共有メモリ由来の走行情報を配信する | [図](graphs/server.svg) |
