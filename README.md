# KoDriver

[![On Main Merge](https://github.com/ai-kurou/KoDriver/actions/workflows/on-main-merge.yml/badge.svg?branch=main)](https://github.com/ai-kurou/KoDriver/actions/workflows/on-main-merge.yml)
[![codecov](https://codecov.io/gh/ai-kurou/KoDriver/graph/badge.svg?token=DSR32EAS87)](https://codecov.io/gh/ai-kurou/KoDriver)
[![Maintainability](https://qlty.sh/gh/ai-kurou/projects/KoDriver/maintainability.svg)](https://qlty.sh/gh/ai-kurou/projects/KoDriver)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/a22a1b1a759e48b1a4551f277d34ea6d)](https://app.codacy.com/gh/ai-kurou/KoDriver/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ai-kurou_KoDriver&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ai-kurou_KoDriver)
![CodeRabbit Pull Request Reviews](https://img.shields.io/coderabbit/prs/github/ai-kurou/KoDriver?utm_source=oss&utm_medium=github&utm_campaign=ai-kurou%2FKoDriver&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews)
[![License](https://img.shields.io/github/license/ai-kurou/KoDriver)](LICENSE)
[![GitHub Release](https://img.shields.io/github/v/release/ai-kurou/KoDriver)](https://github.com/ai-kurou/KoDriver/releases)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-7F52FF?logo=kotlin)

Le Mans Ultimate（LMU）の走行情報に応じて、WAV 音声ファイルをリアルタイムに再生する Compose Multiplatform アプリ。

## 機能

- アナウンスする項目の選択・有効/無効の切り替え
- アナウンス優先度のドラッグ＆ドロップによる並び替え
- WAV 音声ファイルによるリアルタイムアナウンス
- LMU 由来の走行情報を WebSocket で配信（Android アプリで受信・表示可能）
- クラッシュが発生した場合、改善のためにクラッシュレポートを Sentry に自動送信

## 動作要件

**デスクトップアプリ（Windows）**

- Windows 10 以降
- Le Mans Ultimate（インストール済み）

**Android アプリ**

- Android 9.0 以降
- デスクトップアプリと同一 LAN への接続

## インストール

**デスクトップアプリ（Windows）**

[Releases](https://github.com/ai-kurou/KoDriver/releases) から最新の MSI インストーラーをダウンロードして実行してください。

LMU が起動していない状態でアプリを起動しても問題ありません。LMU の起動を検知すると自動的に接続します。

インストール時に Windows SmartScreen の警告が表示される場合があります。詳しい手順、ファイアウォール設定、既知の制限は [Windows 版のインストール手順](docs/windows-install.md) を参照してください。

## アーキテクチャ

Kotlin Multiplatform + Clean Architecture のマルチモジュール構成。

<!-- MODULE-GRAPH-START -->
## Module Graph

![Module Graph](docs/graphs/full-graph.svg)
<!-- MODULE-GRAPH-END -->

| モジュール | 役割 | モジュール図 |
|---|---|---|
| `:app:desktopApp` | JVM デスクトップアプリのエントリーポイント。Windows 共有メモリ読み取りと Ktor サーバーを同一プロセスで起動する | [図](docs/graphs/app-desktopApp.svg) |
| `:app:androidApp` | Android アプリのエントリーポイント。WebSocket 経由でサーバーから LMU 由来の走行情報を受信して表示する | [図](docs/graphs/app-androidApp.svg) |
| `:app:webApp` | Web アプリ（未実装） | [図](docs/graphs/app-webApp.svg) |
| `:app:shared` | Compose Multiplatform 共通 UI・ナビゲーション。各 feature モジュールを組み合わせた画面遷移を担当する | [図](docs/graphs/app-shared.svg) |
| `:core:domain` | ドメインモデル・リポジトリ抽象・ユースケース | [図](docs/graphs/core-domain.svg) |
| `:core:data` | DataStore・HTTP/WebSocketクライアント・リポジトリ実装（JVM / Android） | [図](docs/graphs/core-data.svg) |
| `:core:lmu-windows-data` | LMU Windows共有メモリ読み取り・リポジトリ実装（JVM） | [図](docs/graphs/core-lmu-windows-data.svg) |
| `:core:designsystem` | アプリ全体で共有する Composable コンポーネント | [図](docs/graphs/core-designsystem.svg) |
| `:feature:lmu-windows-connection` | LMU との接続状態を監視し、接続中・未接続・エラーを UI に反映する | [図](docs/graphs/feature-lmu-windows-connection.svg) |
| `:feature:main` | アプリ全体のメイン画面状態管理 | [図](docs/graphs/feature-main.svg) |
| `:feature:server-connection` | KoDriver サーバー（Ktor）への接続状態確認を担当する | [図](docs/graphs/feature-server-connection.svg) |
| `:feature:readout-list` | アナウンス項目の一覧表示・有効/無効の切り替え・優先度のドラッグ&リオーダー | [図](docs/graphs/feature-readout-list.svg) |
| `:feature:lmu-windows-readout-vehicle-approach-detail` | 車両接近アナウンスの詳細設定 UI | [図](docs/graphs/feature-lmu-windows-readout-vehicle-approach-detail.svg) |
| `:feature:lmu-windows-readout-vehicle-damage-detail` | 車両故障アナウンスの詳細設定 UI | [図](docs/graphs/feature-lmu-windows-readout-vehicle-damage-detail.svg) |
| `:feature:lmu-windows-readout-flag-detail` | フラグアナウンスの詳細設定 UI | [図](docs/graphs/feature-lmu-windows-readout-flag-detail.svg) |
| `:feature:lmu-windows-narrator` | WAV 音声ファイルの再生とアナウンス制御を担当する | [図](docs/graphs/feature-lmu-windows-narrator.svg) |
| `:feature:other-license-detail` | その他画面のライセンス詳細表示 | [図](docs/graphs/feature-other-license-detail.svg) |
| `:feature:other-list` | その他画面の一覧表示・選択状態管理 | [図](docs/graphs/feature-other-list.svg) |
| `:feature:other-readout-start-sound-detail` | その他画面の読み上げ開始音設定詳細 | [図](docs/graphs/feature-other-readout-start-sound-detail.svg) |
| `:feature:other-server-ip-detail` | その他画面の接続先サーバー IP 設定ダイアログ | [図](docs/graphs/feature-other-server-ip-detail.svg) |
| `:feature:other-volume-detail` | その他画面の読み上げ音量設定詳細 | [図](docs/graphs/feature-other-volume-detail.svg) |
| `:server` | デスクトップアプリと同一プロセスで起動する Ktor サーバー。`/ws/<Simulator.id>/<feature>` WebSocket で共有メモリ由来の走行情報を配信する | [図](docs/graphs/server.svg) |

## Contributing

このプロジェクトはプルリクエストを受け付けていません。
[GPL-3.0 ライセンス](LICENSE) の範囲内で自由にフォーク・改変・再配布できます。

## クレジット

このアプリは音声合成ソフトウェア `VOICEVOX` を利用しています。

- VOICEVOX:剣崎雌雄
- VOICEVOX 公式サイト: <https://voicevox.hiroshiba.jp/>
- VOICEVOX ソフトウェア利用規約: <https://voicevox.hiroshiba.jp/term/>
- 剣崎雌雄 利用規約: <https://voicevox.hiroshiba.jp/product/kenzaki_mesuo/>

## ライセンス

[GPL-3.0](LICENSE)
